package controles;

import entities.parcours_de_sante;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import services.ParcoursDeSanteServices;

import java.io.File;
import java.sql.SQLException;
import java.time.LocalDate;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

public class AjouterParcoursDeSante {

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

    private final ParcoursDeSanteServices ps = new ParcoursDeSanteServices();
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
    }
    private void returnToDisplay() {
        try {

            Parent root = FXMLLoader.load(getClass().getResource("/AfficherParcours.fxml"));
            btnCancel.getScene().setRoot(root);
        } catch (IOException e) {
            System.err.println("[Navigation Error] Impossible de charger l'affichage : " + e.getMessage());
        }
    }
    private void setupNumericValidation(TextField field) {
        field.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                field.setText(oldValue);
            }
        });
    }

    private void handleChooseFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image du parcours");

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
            // 🛠️ THE FIX IS HERE: Replace \ with / so SQL doesn't destroy the path
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
            errorMsg.append("- Coordonnées GPS invalides.\n");
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

    private void clearForm() {
        nomField.clear();
        locationField.clear();
        latField.clear();
        longField.clear();
        distanceField.clear();
        datePicker.setValue(null);
        fileLabel.setText(" Aucun fichier sélectionné");
        selectedImagePath = "default.png";
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