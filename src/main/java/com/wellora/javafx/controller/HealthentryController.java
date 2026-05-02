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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class HealthentryController {

    private MainController mainController;

    private final HealthentryDAO entryDAO = new HealthentryDAO();
    private final HealthjournalDAO journalDAO = new HealthjournalDAO();
    private final SymptomDAO symptomDAO = new SymptomDAO();
    private final ObservableList<Healthentry> data = FXCollections.observableArrayList();
    private final ObservableList<Healthjournal> journals = FXCollections.observableArrayList();
    private final ObservableList<Symptom> symptomData = FXCollections.observableArrayList();

    @FXML private TableView<Healthentry> tableView;
    @FXML private TextField searchField;
    @FXML private DatePicker filterDatePicker;
    @FXML private DatePicker datePicker;
    @FXML private TextField poidsField;
    @FXML private TextField glycemieField;
    @FXML private TextField tensionField;
    @FXML private TextField sommeilField;
    @FXML private ComboBox<Healthjournal> journalCombo;
    @FXML private Label statusLabel;
    
    // Symptom table
    @FXML private TableView<Symptom> symptomTable;
    
    // Symptom fields
    @FXML private ComboBox<String> symptomTypeCombo;
    @FXML private Slider symptomIntensiteSlider;
    @FXML private Label symptomIntensiteLabel;
    @FXML private TextField symptomZoneField;

    @FXML
    private void initialize() {
        // Setup table columns
        tableView.getColumns().clear();
        tableView.getColumns().addAll(
            createColumn("Date", "date", 120),
            createColumn("Weight", "poids", 70),
            createColumn("BloodSugar", "glycemie", 80),
            createColumn("Pressure", "tension", 80),
            createColumn("Sleep", "sommeil", 70)
        );
        tableView.setItems(data);
        
        // Setup symptom table columns
        symptomTable.getColumns().clear();
        symptomTable.getColumns().addAll(
            createSymptomColumn("Type", "type", 100),
            createSymptomColumn("Intensity", "intensite", 60),
            createSymptomColumn("Zone", "zone", 100)
        );
        symptomTable.setItems(symptomData);
        
        // Load journals
        loadJournals();
        
        // Setup symptom type combo
        symptomTypeCombo.setItems(FXCollections.observableArrayList(Symptom.TYPE_CHOICES));
        symptomIntensiteSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            symptomIntensiteLabel.setText(String.valueOf(newVal.intValue()));
        });
        symptomIntensiteSlider.setValue(5);
        
        // Auto-assign journal when date changes
        datePicker.valueProperty().addListener((obs, oldDate, newDate) -> {
            if (newDate != null) {
                autoAssignJournal(newDate);
            }
        });
        
        loadTable();
    }

    private TableColumn<Healthentry, ?> createColumn(String title, String property, int width) {
        TableColumn<Healthentry, ?> col = new TableColumn<>(title);
        col.setCellValueFactory(new PropertyValueFactory<>(property));
        col.setPrefWidth(width);
        return col;
    }
    
    private TableColumn<Symptom, ?> createSymptomColumn(String title, String property, int width) {
        TableColumn<Symptom, ?> col = new TableColumn<>(title);
        col.setCellValueFactory(new PropertyValueFactory<>(property));
        col.setPrefWidth(width);
        return col;
    }

    private void loadJournals() {
        try {
            List<Healthjournal> list = journalDAO.findAll();
            journals.clear();
            journals.addAll(list);
            journalCombo.setItems(journals);
        } catch (Exception e) {
            showError("Error loading journals: " + e.getMessage());
        }
    }

    /**
     * Auto-assign journal based on date
     */
    private void autoAssignJournal(LocalDate date) {
        try {
            Optional<Healthjournal> journal = journalDAO.findByDate(date);
            if (journal.isPresent()) {
                journalCombo.getSelectionModel().select(journal.get());
                setStatus("Auto-assigned to: " + journal.get().getName());
            } else {
                setStatus("No journal found for this date");
            }
        } catch (Exception e) {
            // Ignore errors
        }
    }

    @FXML
    private void loadTable() {
        try {
            List<Healthentry> entries = entryDAO.findAll();
            data.clear();
            data.addAll(entries);
            setStatus("Loaded " + entries.size() + " entries");
        } catch (Exception e) {
            showError("Error loading: " + e.getMessage());
        }
    }

    @FXML
    private void search() {
        try {
            String search = searchField.getText();
            LocalDate filterDate = filterDatePicker.getValue();
            List<Healthentry> entries = entryDAO.search(
                search.isEmpty() ? null : search, 
                filterDate, "date", "DESC"
            );
            data.clear();
            data.addAll(entries);
            setStatus("Found " + entries.size() + " results");
        } catch (Exception e) {
            showError("Error searching: " + e.getMessage());
        }
    }

    @FXML
    private void selectRow(MouseEvent event) {
        Healthentry selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            fillForm(selected);
        }
    }

    @FXML
    private void add() {
        try {
            if (!validate()) return;
            
            // Create entry
            Healthentry entry = createFromForm();
            entryDAO.save(entry);
            
            // Add symptom if provided
            String symptomType = symptomTypeCombo.getSelectionModel().getSelectedItem();
            if (symptomType != null && !symptomType.isEmpty()) {
                Symptom symptom = new Symptom();
                symptom.setType(symptomType);
                symptom.setIntensite((int) symptomIntensiteSlider.getValue());
                symptom.setZone(symptomZoneField.getText().isEmpty() ? null : symptomZoneField.getText());
                symptom.setEntryId(entry.getId());
                symptomDAO.save(symptom);
            }
            
            clearForm();
            loadTable();
            setStatus("Added entry with ID: " + entry.getId());
        } catch (Exception e) {
            showError("Error adding: " + e.getMessage());
        }
    }

    @FXML
    private void update() {
        try {
            Healthentry selected = tableView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showError("Select an entry to update");
                return;
            }
            if (!validate()) return;
            
            Healthentry entry = createFromForm();
            entry.setId(selected.getId());
            entryDAO.save(entry);
            
            clearForm();
            loadTable();
            setStatus("Entry updated");
        } catch (Exception e) {
            showError("Error updating: " + e.getMessage());
        }
    }

    @FXML
    private void delete() {
        Healthentry selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Select an entry to delete");
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Confirmation");
        alert.setHeaderText("Delete this entry?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                entryDAO.delete(selected.getId());
                clearForm();
                loadTable();
                setStatus("Entry deleted");
            } catch (Exception e) {
                showError("Error deleting: " + e.getMessage());
            }
        }
    }

    @FXML
    private void clearForm() {
        datePicker.setValue(null);
        poidsField.clear();
        glycemieField.clear();
        tensionField.clear();
        sommeilField.clear();
        journalCombo.getSelectionModel().clearSelection();
        
        // Clear symptom fields
        symptomTypeCombo.getSelectionModel().clearSelection();
        symptomIntensiteSlider.setValue(5);
        symptomZoneField.clear();
        
        // Clear symptom table
        symptomData.clear();
        
        statusLabel.setText("");
    }

    @FXML
    private void refresh() {
        searchField.clear();
        filterDatePicker.setValue(null);
        loadTable();
    }

    private void fillForm(Healthentry entry) {
        datePicker.setValue(entry.getDate());
        poidsField.setText(String.valueOf(entry.getPoids()));
        glycemieField.setText(String.valueOf(entry.getGlycemie()));
        tensionField.setText(entry.getTension());
        sommeilField.setText(String.valueOf(entry.getSommeil()));
        
        for (Healthjournal j : journals) {
            if (j.getId() == entry.getJournalId()) {
                journalCombo.getSelectionModel().select(j);
                break;
            }
        }
        
        // Load symptoms for this entry into symptom table
        try {
            List<Symptom> symptoms = symptomDAO.findByEntryId(entry.getId());
            symptomData.clear();
            symptomData.addAll(symptoms);
            
            // Also fill the symptom form fields if there are symptoms
            if (!symptoms.isEmpty()) {
                Symptom s = symptoms.get(0);
                symptomTypeCombo.getSelectionModel().select(s.getType());
                symptomIntensiteSlider.setValue(s.getIntensite());
                symptomZoneField.setText(s.getZone() != null ? s.getZone() : "");
            }
        } catch (Exception e) {
            // Ignore
        }
    }

    private Healthentry createFromForm() {
        Healthentry entry = new Healthentry();
        entry.setDate(datePicker.getValue());
        entry.setPoids(Double.parseDouble(poidsField.getText()));
        entry.setGlycemie(Double.parseDouble(glycemieField.getText()));
        entry.setTension(tensionField.getText());
        entry.setSommeil(Integer.parseInt(sommeilField.getText()));
        
        Healthjournal selected = journalCombo.getSelectionModel().getSelectedItem();
        if (selected != null) {
            entry.setJournalId(selected.getId());
        }
        
        return entry;
    }

    private boolean validate() {
        if (datePicker.getValue() == null) {
            showError("Date is required");
            return false;
        }
        
        try {
            double poids = Double.parseDouble(poidsField.getText());
            if (poids < 30 || poids > 200) {
                showError("Weight must be between 30 and 200 kg");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Invalid weight");
            return false;
        }
        
        try {
            double glycemie = Double.parseDouble(glycemieField.getText());
            if (glycemie < 0.5 || glycemie > 3) {
                showError("Blood sugar must be between 0.5 and 3 g/l");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Invalid blood sugar");
            return false;
        }
        
        try {
            double tension = Double.parseDouble(tensionField.getText());
            if (tension < 40 || tension > 120) {
                showError("Blood pressure must be between 40 and 120 mmHg");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Invalid blood pressure");
            return false;
        }
        
        try {
            int sommeil = Integer.parseInt(sommeilField.getText());
            if (sommeil < 0 || sommeil > 12) {
                showError("Sleep must be between 0 and 12 hours");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Invalid sleep value");
            return false;
        }
        
        // Journal is auto-assigned but can be overridden
        if (journalCombo.getSelectionModel().getSelectedItem() == null) {
            // Try auto-assignment
            LocalDate date = datePicker.getValue();
            try {
                Optional<Healthjournal> journal = journalDAO.findByDate(date);
                if (journal.isEmpty()) {
                    showError("No journal exists for this date. Create a journal first.");
                    return false;
                }
            } catch (Exception e) {
                showError("Error checking journal: " + e.getMessage());
                return false;
            }
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
        alert.setTitle("Error");
        alert.setHeaderText(message);
        alert.showAndWait();
    }
    
    /**
     * Set reference to main controller for navigation
     */
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }
}