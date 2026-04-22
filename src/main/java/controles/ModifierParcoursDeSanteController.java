package controles;

import com.sothawo.mapjfx.Coordinate;
import com.sothawo.mapjfx.MapType;
import com.sothawo.mapjfx.MapView;
import com.sothawo.mapjfx.Marker;
import com.sothawo.mapjfx.event.MapViewEvent;
import entities.parcours_de_sante;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import org.json.JSONObject;
import services.ParcoursDeSanteServices;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;

public class ModifierParcoursDeSanteController {

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

    // MAPJFX
    @FXML private MapView mapView;
    private Marker clickMarker;
    private URL modernMarkerUrl;

    private final ParcoursDeSanteServices ps = new ParcoursDeSanteServices();
    private String selectedImagePath = "default.png";
    private parcours_de_sante currentParcours;

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

        btnSave.setOnAction(event -> handleModifierParcours());
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
                mapView.setZoom(12);


                if (currentParcours != null) {
                    setMapToCurrentParcours();
                } else {
                    mapView.setCenter(new Coordinate(36.8065, 10.1815));
                }
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

    public void initData(parcours_de_sante p) {
        this.currentParcours = p;

        nomField.setText(p.getNom_parcours());
        locationField.setText(p.getLocalisation_parcours());
        latField.setText(String.valueOf(p.getLatitude_parcours()));
        longField.setText(String.valueOf(p.getLongitude_parcours()));
        distanceField.setText(String.valueOf(p.getDistance_parcours()));

        try {
            datePicker.setValue(LocalDate.parse(p.getDate_creation()));
        } catch (Exception e) {
            System.err.println("Erreur parsing date: " + e.getMessage());
        }

        selectedImagePath = p.getImage_parcours();
        if (selectedImagePath != null && !selectedImagePath.equals("default.png")) {
            File f = new File(selectedImagePath);
            fileLabel.setText("✔ " + f.getName());
        }


        if (mapView.getInitialized()) {
            setMapToCurrentParcours();
        }
    }

    private void setMapToCurrentParcours() {
        Coordinate existingCoord = new Coordinate(currentParcours.getLatitude_parcours(), currentParcours.getLongitude_parcours());
        mapView.setCenter(existingCoord);

        if (clickMarker != null) {
            mapView.removeMarker(clickMarker);
        }
        clickMarker = new Marker(modernMarkerUrl, -12, -41).setPosition(existingCoord).setVisible(true);
        mapView.addMarker(clickMarker);
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
            return "";
        }
    }

    private void handleModifierParcours() {
        if (isInputValid()) {
            try {
                currentParcours.setNom_parcours(nomField.getText().trim());
                currentParcours.setLocalisation_parcours(locationField.getText().trim());
                currentParcours.setLatitude_parcours(Double.parseDouble(latField.getText()));
                currentParcours.setLongitude_parcours(Double.parseDouble(longField.getText()));
                currentParcours.setDistance_parcours(Double.parseDouble(distanceField.getText()));
                currentParcours.setDate_creation(datePicker.getValue().toString());
                currentParcours.setImage_parcours(selectedImagePath);

                ps.modifier(currentParcours);

                showInformation("Succès", "Le parcours a été modifié avec succès !");
                returnToDisplay();

            } catch (SQLException e) {
                showError("Erreur Database", e.getMessage());
            }
        }
    }

    private void returnToDisplay() {
        try {
            mapView.close();
            Parent root = FXMLLoader.load(getClass().getResource("/AfficherParcours.fxml"));
            btnCancel.getScene().setRoot(root);
        } catch (IOException e) {
            System.err.println("[Navigation Error] Impossible de charger l'affichage : " + e.getMessage());
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
        fileChooser.setTitle("Sélectionner une nouvelle image");

        String userHome = System.getProperty("user.home");
        File downloadsDirectory = new File(userHome, "Downloads");

        if (downloadsDirectory.exists()) {
            fileChooser.setInitialDirectory(downloadsDirectory);
        } else {
            fileChooser.setInitialDirectory(new File(userHome));
        }

        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(btnChooseFile.getScene().getWindow());

        if (selectedFile != null) {
            this.selectedImagePath = selectedFile.getAbsolutePath().replace("\\", "/");
            fileLabel.setText("✔ " + selectedFile.getName());
        }
    }

    private boolean isInputValid() {
        StringBuilder errorMsg = new StringBuilder();

        if (nomField.getText().trim().length() < 5) {
            errorMsg.append("- Le nom doit contenir au moins 5 lettres.\n");
        }
        if (locationField.getText().trim().isEmpty()) {
            errorMsg.append("- La localisation ne peut pas être vide.\n");
        }
        try {
            double dist = Double.parseDouble(distanceField.getText());
            if (dist <= 0 || dist > 20) {
                errorMsg.append("- La distance doit être entre 0.1 et 20 km.\n");
            }
        } catch (NumberFormatException e) {
            errorMsg.append("- Distance invalide.\n");
        }
        try {
            double lat = Double.parseDouble(latField.getText());
            double lon = Double.parseDouble(longField.getText());
            if (lat < -90 || lat > 90) errorMsg.append("- Latitude invalide (-90 à 90).\n");
            if (lon < -180 || lon > 180) errorMsg.append("- Longitude invalide (-180 à 180).\n");
        } catch (NumberFormatException e) {
            errorMsg.append("- Veuillez cliquer sur la carte pour sélectionner des coordonnées.\n");
        }
        if (datePicker.getValue() == null) {
            errorMsg.append("- Veuillez choisir une date.\n");
        } else if (datePicker.getValue().isAfter(LocalDate.now())) {
            errorMsg.append("- La date de création ne peut pas être dans le futur.\n");
        }

        if (errorMsg.length() > 0) {
            showError("Données Invalides", errorMsg.toString());
            return false;
        }
        return true;
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