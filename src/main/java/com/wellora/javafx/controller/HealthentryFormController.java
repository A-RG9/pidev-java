package com.wellora.javafx.controller;

import com.wellora.dao.HealthentryDAO;
import com.wellora.dao.HealthjournalDAO;
import com.wellora.dao.SymptomDAO;
import com.wellora.model.Healthentry;
import com.wellora.model.Healthjournal;
import com.wellora.model.Symptom;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * HealthentryFormController - Handles the form for Health Entries
 */
public class HealthentryFormController {

    private HealthentryDAO entryDAO = new HealthentryDAO();
    private HealthjournalDAO journalDAO = new HealthjournalDAO();
    private SymptomDAO symptomDAO = new SymptomDAO();
    private Healthentry currentEntry;
    private MainController mainController;
    private ObservableList<Symptom> symptomsList = FXCollections.observableArrayList();

    // Form Fields
    @FXML private Label headerLabel;
    @FXML private TextField idField;
    @FXML private DatePicker datePicker;
    @FXML private TextField poidsField;
    @FXML private TextField glycemieField;
    @FXML private TextField tensionField;
    @FXML private TextField sommeilField;
    @FXML private Label statusLabel;
    // Symptom Fields
    @FXML private ComboBox<String> symptomTypeCombo;
    @FXML private Slider symptomIntensiteSlider;
    @FXML private Label symptomIntensiteLabel;
    @FXML private TextField symptomZoneField;
    @FXML private TableView<Symptom> symptomTableView;

    @FXML
    private void initialize() {
        headerLabel.setText("Créer une Entrée");
        symptomTypeCombo.setItems(FXCollections.observableArrayList(
            "Headache", "Nausea", "Fatigue", "Dizziness", "Pain", "Fever", "Cough", "Other"));
        symptomIntensiteSlider.setValue(5);
        symptomIntensiteLabel.setText("5");
        symptomIntensiteSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            symptomIntensiteLabel.setText(String.valueOf(newVal.intValue()));
        });
        
        // Bind the TableView to the symptoms list
        if (symptomTableView != null) {
            symptomTableView.setItems(symptomsList);
        }
    }
    
    // Auto-add symptom when selecting from dropdown
    private void autoAddSymptom(String type) {
        if (type == null || type.trim().isEmpty()) return;
        type = type.trim();
        
        // Check if it's a new custom type and add to dropdown list
        ObservableList<String> items = symptomTypeCombo.getItems();
        boolean found = false;
        for (String existing : items) {
            if (existing.equalsIgnoreCase(type)) {
                found = true;
                break;
            }
        }
        if (!found) {
            symptomTypeCombo.getItems().add(type);
            setStatus("Nouveau type ajouté: " + type);
        }
        
        int intensite = (int) symptomIntensiteSlider.getValue();
        String zone = symptomZoneField.getText();
        
        Symptom symptom = new Symptom();
        symptom.setType(type);
        symptom.setIntensite(intensite);
        symptom.setZone(zone.isEmpty() ? null : zone);
        symptomsList.add(symptom);
        
        // Clear for next entry
        symptomTypeCombo.getSelectionModel().clearSelection();
        symptomTypeCombo.setValue(null);
        symptomZoneField.setText("");
        symptomIntensiteSlider.setValue(5);
        symptomIntensiteLabel.setText("5");
    }
    
    @FXML
    private void removeSelectedSymptom() {
        Symptom selected = symptomTableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            symptomsList.remove(selected);
            setStatus("Symptôme supprimé");
        } else {
            showError("Sélectionnez un symptôme à supprimer");
        }
    }
    
    @FXML
    private void addSymptom() {
        // Get type from combo box (allows custom types)
        String type = symptomTypeCombo.getValue();
        if (type == null || type.trim().isEmpty()) {
            showError("Entrez un type de symptôme");
            return;
        }
        type = type.trim();
        
        // Check if it's a new custom type and add to dropdown list
        ObservableList<String> items = symptomTypeCombo.getItems();
        boolean found = false;
        for (String existing : items) {
            if (existing.equalsIgnoreCase(type)) {
                found = true;
                break;
            }
        }
        if (!found) {
            // Add new custom type to list
            symptomTypeCombo.getItems().add(type);
            setStatus("Nouveau type ajouté: " + type);
        }
        
        int intensite = (int) symptomIntensiteSlider.getValue();
        String zone = symptomZoneField.getText();
        
        Symptom symptom = new Symptom();
        symptom.setType(type);
        symptom.setIntensite(intensite);
        symptom.setZone(zone.isEmpty() ? null : zone);
        symptomsList.add(symptom);
        
        // Clear form for next entry
        symptomTypeCombo.getSelectionModel().clearSelection();
        symptomTypeCombo.setValue(null);
        symptomZoneField.setText("");
        symptomIntensiteSlider.setValue(5);
        symptomIntensiteLabel.setText("5");
    }
    
    // Auto-assign journal based on date range
    private Healthjournal findJournalByDate(LocalDate date) {
        try {
            Optional<Healthjournal> journal = journalDAO.findByDate(date);
            return journal.orElse(null);
        } catch (Exception e) {
            showError("Erreur recherche journal: " + e.getMessage());
            return null;
        }
    }

    public void setEntry(Healthentry entry) {
        this.currentEntry = entry;
        
        if (entry != null) {
            headerLabel.setText("Modifier l'Entrée");
            idField.setText(String.valueOf(entry.getId()));
            datePicker.setValue(entry.getDate());
            poidsField.setText(String.valueOf(entry.getPoids()));
            glycemieField.setText(String.valueOf(entry.getGlycemie()));
            tensionField.setText(entry.getTension());
            sommeilField.setText(String.valueOf(entry.getSommeil()));
            
            // Load existing symptoms for this entry
            try {
                List<Symptom> existingSymptoms = symptomDAO.findByEntryId(entry.getId());
                symptomsList.clear();
                symptomsList.addAll(existingSymptoms);
            } catch (Exception e) {
                // Ignore - symptoms will be empty
            }
            
            // Journal is automatically determined by date in the save method
        } else {
            headerLabel.setText("Créer une Entrée");
            idField.setText("");
            datePicker.setValue(null);
            poidsField.setText("");
            glycemieField.setText("");
            tensionField.setText("");
            sommeilField.setText("");
            symptomsList.clear();
        }
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void save() {
        if (!validate()) return;

        try {
            Healthentry entry = createFromForm();
            if (currentEntry != null) {
                entry.setId(currentEntry.getId());
            }
            
            // Set symptoms on entry before saving (enables cascade save)
            entry.setSymptoms(new java.util.ArrayList<>(symptomsList));
            
            entryDAO.save(entry);
            
            setStatus("Entrée enregistrée avec " + symptomsList.size() + " symptôme(s)!");
            
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
            mainController.showHealthEntries();
        }
    }

    private Healthentry createFromForm() {
        Healthentry entry = new Healthentry();
        entry.setDate(datePicker.getValue());
        entry.setPoids(Double.parseDouble(poidsField.getText()));
        entry.setGlycemie(Double.parseDouble(glycemieField.getText()));
        entry.setTension(tensionField.getText());
        entry.setSommeil(Integer.parseInt(sommeilField.getText()));
        
        // Auto-assign journal based on date range
        Healthjournal journal = findJournalByDate(datePicker.getValue());
        if (journal != null) {
            entry.setJournalId(journal.getId());
        }
        
        return entry;
    }

    private boolean validate() {
        if (datePicker.getValue() == null) {
            showError("La date est requise");
            return false;
        }
        
        try {
            double poids = Double.parseDouble(poidsField.getText());
            if (poids < 30 || poids > 200) {
                showError("Poids doit être entre 30 et 200 kg");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Poids invalide");
            return false;
        }
        
        try {
            double glycemie = Double.parseDouble(glycemieField.getText());
            if (glycemie < 0.5 || glycemie > 3) {
                showError("Glycémie doit être entre 0.5 et 3 g/l");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Glycémie invalide");
            return false;
        }
        
        try {
            int sommeil = Integer.parseInt(sommeilField.getText());
            if (sommeil < 0 || sommeil > 12) {
                showError("Sommeil doit être entre 0 et 12 heures");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Sommeil invalide");
            return false;
        }
        
        // Auto-assign journal based on date range
        Healthjournal journal = findJournalByDate(datePicker.getValue());
        if (journal == null) {
            showError("Aucun journal pour cette date. Créez un journal avec cette période.");
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
