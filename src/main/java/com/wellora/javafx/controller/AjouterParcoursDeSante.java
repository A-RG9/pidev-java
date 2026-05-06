package com.wellora.javafx.controller;

import com.sothawo.mapjfx.Coordinate;
import com.sothawo.mapjfx.MapType;
import com.sothawo.mapjfx.MapView;
import com.sothawo.mapjfx.Marker;
import com.sothawo.mapjfx.event.MapViewEvent;
import com.wellora.model.parcours_de_sante;
import com.wellora.controllers.HealthShellController;
import com.wellora.javafx.controller.MainController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.json.JSONObject;
import com.wellora.dao.ParcoursDeSanteDAO;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

public class AjouterParcoursDeSante {

    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML private TextField nomField;
    @FXML private TextField locationField;
    @FXML private TextField latField;
    @FXML private TextField longField;
    @FXML private TextField distanceField;
    @FXML private DatePicker datePicker;
    @FXML private Button btnSave;
    @FXML private Button btnCancel;
    @FXML private Button btnChooseFile;
    @FXML private Label fileLabel;

    @FXML private MapView mapView;
    private Marker clickMarker;
    private URL modernMarkerUrl;

    private final ParcoursDeSanteDAO ps = new ParcoursDeSanteDAO();
    private String selectedImagePath = "default.png";

    @FXML
    void initialize() {
        setupNumericValidation(latField);
        setupNumericValidation(longField);
        setupNumericValidation(distanceField);

        nomField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("[a-zA-Z\\s]*")) {
                nomField.setText(oldValue);
            }
        });

        btnSave.setOnAction(event -> handleAjouterParcours());
        btnCancel.setOnAction(event -> returnToDisplay());
        btnChooseFile.setOnAction(event -> handleChooseFile());

        try {

            modernMarkerUrl = new URL("https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        initializeMap();
    }

    private void initializeMap() {
        mapView.initialize();

        mapView.initializedProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue) {
                mapView.setMapType(MapType.OSM);
                mapView.setZoom(8);
                mapView.setCenter(new Coordinate(36.8065, 10.1815));
            }
        });

        mapView.addEventHandler(MapViewEvent.MAP_CLICKED, event -> {
            Coordinate coord = event.getCoordinate();
            if (coord != null) {
                double latitude = coord.getLatitude();
                double longitude = coord.getLongitude();

                if (clickMarker != null) {
                    mapView.removeMarker(clickMarker);
                }


                clickMarker = new Marker(modernMarkerUrl, -12, -41).setPosition(coord).setVisible(true);
                mapView.addMarker(clickMarker);

                latField.setText(String.format("%.6f", latitude).replace(",", "."));
                longField.setText(String.format("%.6f", longitude).replace(",", "."));

                new Thread(() -> {
                    String address = getLocationName(latitude, longitude);
                    Platform.runLater(() -> locationField.setText(address));
                }).start();
            }
        });
    }

    public String getLocationName(double latitude, double longitude) {
        try {

            String urlStr = "https://nominatim.openstreetmap.org/reverse?format=json&lat=" + latitude + "&lon=" + longitude + "&accept-language=fr";

            HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "JavaFX-App");

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            br.close();

            JSONObject jsonResponse = new JSONObject(response.toString());
            return jsonResponse.optString("display_name", "");

        } catch (Exception e) {
            System.err.println("Erreur Reverse Geocoding: " + e.getMessage());
            return "";
        }
    }

    private void returnToDisplay() {
        if (mainController != null) {
            mapView.close();
            mainController.loadView("/fxml/AfficherParcours.fxml");
        }
    }

    private void setupNumericValidation(TextField field) {
        field.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("-?\\d*(\\.\\d*)?")) {
                field.setText(oldValue);
            }
        });
    }

    private void handleChooseFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        File selectedFile = fileChooser.showOpenDialog(btnChooseFile.getScene().getWindow());

        if (selectedFile != null) {
            this.selectedImagePath = selectedFile.getAbsolutePath().replace("\\", "/");
            fileLabel.setText("✔ " + selectedFile.getName());
        }
    }

    private void handleAjouterParcours() {
        if (isInputValid()) {
            try {
                parcours_de_sante p = new parcours_de_sante(
                        nomField.getText().trim(),
                        locationField.getText().trim(),
                        Double.parseDouble(latField.getText()),
                        Double.parseDouble(longField.getText()),
                        Double.parseDouble(distanceField.getText()),
                        datePicker.getValue().toString(),
                        selectedImagePath
                );
                com.wellcare.javafx.model.User currentUser = com.wellcare.javafx.util.SceneManager.getInstance().getCurrentUser();
                if(currentUser != null) {
                    p.setOwner_patient_uuid(currentUser.getUuid());
                }

                ps.ajouter(p);
                showInformation("Succès", "Le parcours a été ajouté !");
                clearForm();

            } catch (SQLException e) {
                showError("Erreur Database", e.getMessage());
            }
        }
    }

    private boolean isInputValid() {
        StringBuilder errorMsg = new StringBuilder();

        if (nomField.getText().trim().length() < 5) errorMsg.append("- Le nom doit contenir au moins 5 lettres.\n");
        if (locationField.getText().trim().isEmpty()) errorMsg.append("- La localisation ne peut pas être vide.\n");

        try {
            double dist = Double.parseDouble(distanceField.getText());
            if (dist <= 0 || dist > 20) errorMsg.append("- La distance doit être entre 0.1 et 20 km.\n");
        } catch (NumberFormatException e) { errorMsg.append("- Distance invalide.\n"); }

        if (latField.getText().isEmpty() || longField.getText().isEmpty()) {
            errorMsg.append("- Veuillez cliquer sur la carte pour sélectionner des coordonnées.\n");
        }

        if (datePicker.getValue() == null) errorMsg.append("- Veuillez choisir une date.\n");
        else if (datePicker.getValue().isAfter(LocalDate.now())) errorMsg.append("- La date ne peut pas être dans le futur.\n");

        if (errorMsg.length() > 0) {
            showError("Données Invalides", errorMsg.toString());
            return false;
        }
        return true;
    }

    private void clearForm() {
        nomField.clear();
        locationField.clear();
        latField.clear();
        longField.clear();
        distanceField.clear();
        datePicker.setValue(null);
        fileLabel.setText(" Aucun fichier sélectionné");
        selectedImagePath = "default.png";

        if (clickMarker != null) {
            mapView.removeMarker(clickMarker);
            clickMarker = null;
        }
        mapView.setCenter(new Coordinate(36.8065, 10.1815));
    }

    private void showInformation(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText("Erreur de saisie");
        alert.setContentText(content);
        alert.showAndWait();
    }
}