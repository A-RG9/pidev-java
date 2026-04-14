package com.wellora.javafx.controller;

import com.wellora.dao.SymptomDAO;
import com.wellora.model.Symptom;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;
import java.util.Optional;

/**
 * SymptomFormController - Handles the form for Symptoms
 */
public class SymptomFormController {

    private SymptomDAO symptomDAO = new SymptomDAO();
    private Symptom currentSymptom;
    private MainController mainController;

    // Form Fields
    @FXML private Label headerLabel;
    @FXML private TextField idField;
    @FXML private ComboBox<String> typeCombo;
    @FXML private Slider intensiteSlider;
    @FXML private Label intensiteLabel;
    @FXML private TextField zoneField;
    @FXML private TextField entryIdField;
    @FXML private Label statusLabel;

    private ObservableList<String> typeChoices = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        // Load symptom types
        typeChoices.addAll(List.of(Symptom.TYPE_CHOICES));
        typeCombo.setItems(typeChoices);
        
        // Setup slider listener
        intensiteSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            intensiteLabel.setText(String.valueOf(newVal.intValue()));
        });
        intensiteSlider.setValue(5);
        
        headerLabel.setText("Créer un Symptôme");
    }

    public void setSymptom(Symptom symptom) {
        this.currentSymptom = symptom;
        
        if (symptom != null) {
            headerLabel.setText("Modifier le Symptôme");
            idField.setText(String.valueOf(symptom.getId()));
            typeCombo.getSelectionModel().select(symptom.getType());
            intensiteSlider.setValue(symptom.getIntensite());
            intensiteLabel.setText(String.valueOf(symptom.getIntensite()));
            zoneField.setText(symptom.getZone());
            entryIdField.setText(String.valueOf(symptom.getEntryId()));
        } else {
            headerLabel.setText("Créer un Symptôme");
            idField.setText("");
            typeCombo.getSelectionModel().clearSelection();
            intensiteSlider.setValue(5);
            intensiteLabel.setText("5");
            zoneField.setText("");
            entryIdField.setText("");
        }
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void save() {
        if (!validate()) return;

        try {
            Symptom symptom = createFromForm();
            if (currentSymptom != null) {
                symptom.setId(currentSymptom.getId());
            }
            symptomDAO.save(symptom);
            
            setStatus("Symptôme enregistré!");
            
            // Go back to list after short delay
            new java.util.Timer().schedule(new java.util.TimerTask() {
                @Override
                public void run() {
                    cancel();
                }
            }, 1000);
            
        } catch (Exception e) {
            showError("Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void cancel() {
        if (mainController != null) {
            mainController.showSymptoms();
        }
    }

    private Symptom createFromForm() {
        String type = typeCombo.getSelectionModel().getSelectedItem();
        int intensite = (int) intensiteSlider.getValue();
        String zone = zoneField.getText().isEmpty() ? null : zoneField.getText().trim();
        int entryId = Integer.parseInt(entryIdField.getText());
        
        return new Symptom(0, type, intensite, zone, entryId);
    }

    private boolean validate() {
        String type = typeCombo.getSelectionModel().getSelectedItem();
        if (type == null || type.isEmpty()) {
            showError("Le type est requis");
            return false;
        }
        
        int intensite = (int) intensiteSlider.getValue();
        if (intensite < 1 || intensite > 10) {
            showError("L'intensité doit être entre 1 et 10");
            return false;
        }
        
        try {
            int entryId = Integer.parseInt(entryIdField.getText());
            if (entryId <= 0) {
                showError("L'ID de l'entrée doit être positif");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("ID de l'entrée invalide");
            return false;
        }
        
        return true;
    }

    private void setStatus(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
    }

    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(message);
        alert.showAndWait();
    }
}
