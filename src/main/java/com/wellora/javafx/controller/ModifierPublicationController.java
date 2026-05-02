package com.wellora.javafx.controller;

import com.wellora.model.parcours_de_sante;
import com.wellora.model.publication_parcours;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import com.wellora.dao.PublicationDAO;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;

public class ModifierPublicationController {

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
    private publication_parcours currentPublication;
    private String selectedImagePath = "";
    private final PublicationDAO pubService = new PublicationDAO();

    @FXML
    public void initialize() {
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
                selectedImagePath = selectedFile.getAbsolutePath().replace("\\", "/");
                fileLabel.setText(" " + selectedFile.getName());
            }
        });

        btnSave.setOnAction(e -> updatePublication());
    }


    public void initData(parcours_de_sante p, publication_parcours pub) {
        this.currentParcours = p;
        this.currentPublication = pub;

        if (p != null) {
            trailNameField.setText(p.getNom_parcours());
        }

        if (pub != null) {
            pubTextArea.setText(pub.getText_publication());
            ambianceField.setText(String.valueOf(pub.getAmbiance()));
            safetyField.setText(String.valueOf(pub.getSecurite()));
            experienceCombo.setValue(pub.getExperience());
            typeCombo.setValue(pub.getType_publication());

            try {

                String dateStr = pub.getDate_publication().split(" ")[0];
                datePicker.setValue(LocalDate.parse(dateStr));
            } catch (Exception e) {
                System.out.println("Could not parse date: " + pub.getDate_publication());
            }

            selectedImagePath = pub.getImage_publication();
            if (selectedImagePath != null && !selectedImagePath.isEmpty()) {
                File f = new File(selectedImagePath);
                fileLabel.setText("✔ " + f.getName());
            }
        }
    }

    private void updatePublication() {
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


            currentPublication.setText_publication(pubTextArea.getText().trim());
            currentPublication.setAmbiance(ambiance);
            currentPublication.setSecurite(safety);
            currentPublication.setExperience(experienceCombo.getValue());
            currentPublication.setType_publication(typeCombo.getValue());
            currentPublication.setDate_publication(datePicker.getValue().toString());
            currentPublication.setImage_publication(selectedImagePath);

            pubService.modifier(currentPublication);

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
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AfficherPublications.fxml"));
            Parent root = loader.load();

            AfficherPublicationsController controller = loader.getController();
            controller.initData(currentParcours);

            btnCancel.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}