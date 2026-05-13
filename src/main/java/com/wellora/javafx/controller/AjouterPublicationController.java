package com.wellora.javafx.controller;

import com.wellora.controllers.BaseController;
import com.wellora.controllers.HealthShellController;
import com.wellora.model.parcours_de_sante;
import com.wellora.model.publication_parcours;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import com.wellora.dao.PublicationDAO;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import com.wellcare.javafx.util.AppConfig;
import java.time.LocalDate;

public class AjouterPublicationController extends BaseController {

    @FXML private TextField trailNameField;
    @FXML private TextArea pubTextArea;
    @FXML private TextField ambianceField;
    @FXML private TextField safetyField;
    @FXML private ComboBox<String> experienceCombo;
    @FXML private ComboBox<String> typeCombo;
    @FXML private DatePicker datePicker;
    @FXML private Button btnChooseFile;
    @FXML private Label fileLabel;
    @FXML private Button btnSave;
    @FXML private Button btnCancel;

    private parcours_de_sante currentParcours;
    private String selectedImagePath = "";
    private final PublicationDAO pubService = new PublicationDAO();

    @FXML
    public void initialize() {


        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);

                if (date != null && !empty && date.isAfter(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #f0f0f0; -fx-text-fill: #b2bec3;");
                }
            }
        });

        experienceCombo.getItems().addAll("Bad", "Good", "Excellent");
        typeCombo.getItems().addAll("Review", "Event");

        btnCancel.setOnAction(e -> goBackToPublications());

        btnChooseFile.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choisir une image");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
            );
            File selectedFile = fileChooser.showOpenDialog(btnChooseFile.getScene().getWindow());
            if (selectedFile != null) {
                selectedImagePath = selectedFile.getAbsolutePath();
                fileLabel.setText(" " + selectedFile.getName());
            }
        });

        btnSave.setOnAction(e -> savePublication());
    }

    public void initData(parcours_de_sante p) {
        this.currentParcours = p;
        if (p != null) {
            trailNameField.setText(p.getNom_parcours());
        }
    }

    private void savePublication() {
        try {

            if (pubTextArea.getText().trim().isEmpty() || datePicker.getValue() == null ||
                    experienceCombo.getValue() == null || typeCombo.getValue() == null) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Veuillez remplir tous les champs obligatoires.");
                alert.show();
                return;
            }

            int ambiance = Integer.parseInt(ambianceField.getText().trim());
            int safety = Integer.parseInt(safetyField.getText().trim());

            if (ambiance < 1 || ambiance > 5 || safety < 1 || safety > 5) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "L'ambiance et la sécurité doivent être entre 1 et 5.");
                alert.show();
                return;
            }


            if (datePicker.getValue().isAfter(LocalDate.now())) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "La date ne peut pas être dans le futur.");
                alert.show();
                return;
            }

            publication_parcours pub = new publication_parcours();
            pub.setParcours_de_sante_id(currentParcours.getId());
            pub.setText_publication(pubTextArea.getText().trim());
            pub.setAmbiance(ambiance);
            pub.setSecurite(safety);
            pub.setExperience(experienceCombo.getValue());
            pub.setType_publication(typeCombo.getValue());
            pub.setDate_publication(datePicker.getValue().toString());
            
            String finalImagePath = selectedImagePath;
            if (selectedImagePath != null && !selectedImagePath.isEmpty()) {
                try {
                    String sharedPath = AppConfig.getSharedUploadDir();
                    if (sharedPath != null && !sharedPath.isEmpty()) {
                        File sourceFile = new File(selectedImagePath);
                        if (sourceFile.exists()) {
                            String fileName = UUID.randomUUID().toString() + "_" + sourceFile.getName();
                            Path targetDir = Paths.get(sharedPath, "parcours"); // Using 'parcours' subfolder as seen in Symfony
                            if (!Files.exists(targetDir)) {
                                Files.createDirectories(targetDir);
                            }
                            Path targetPath = targetDir.resolve(fileName);
                            Files.copy(sourceFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                            finalImagePath = "/uploads/parcours/" + fileName;
                        }
                    }
                } catch (IOException e) {
                    System.err.println("Erreur copie image (Publication): " + e.getMessage());
                }
            }
            pub.setImage_publication(finalImagePath);
            
            com.wellcare.javafx.model.User currentUser = com.wellcare.javafx.util.SceneManager.getInstance().getCurrentUser();
            if(currentUser != null) {
                pub.setOwner_patient_uuid(currentUser.getUuid());
            }

            pubService.ajouter(pub);

            goBackToPublications();

        } catch (NumberFormatException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Ambiance et Sécurité doivent être des nombres.");
            alert.show();
        } catch (SQLException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur base de données : " + ex.getMessage());
            alert.show();
            ex.printStackTrace();
        }
    }

    private void goBackToPublications() {
        if (mainController != null) {
            mainController.loadViewWithData("/fxml/AfficherPublications.fxml", currentParcours);
        }
    }

    @Override
    public Parent getRoot() {
        return pubTextArea != null ? pubTextArea.getScene() != null ? pubTextArea.getScene().getRoot() : null : null;
    }
}