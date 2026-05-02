package com.wellora.javafx.controller;

import com.wellora.dao.SymptomDAO;
import com.wellora.model.Symptom;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;

import java.util.List;
import java.util.Optional;

public class SymptomController {

    private MainController mainController;

    private final SymptomDAO symptomDAO = new SymptomDAO();
    private final ObservableList<Symptom> data = FXCollections.observableArrayList();

    @FXML private TableView<Symptom> tableView;
    @FXML private TextField searchField;
    @FXML private TextField idField;
    @FXML private ComboBox<String> typeCombo;
    @FXML private Slider intensiteSlider;
    @FXML private Label intensiteLabel;
    @FXML private TextField zoneField;
    @FXML private TextField entryIdField;
    @FXML private Label statusLabel;

    @FXML
    private void initialize() {
        // FIX: Clear existing columns from FXML before adding new ones
        tableView.getColumns().clear();
        
        TableColumn<Symptom, Integer> idCol = new TableColumn<>("ID");
        TableColumn<Symptom, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        typeCol.setPrefWidth(150);
        
        TableColumn<Symptom, Integer> intensiteCol = new TableColumn<>("Intensity");
        intensiteCol.setCellValueFactory(new PropertyValueFactory<>("intensite"));
        intensiteCol.setPrefWidth(80);
        
        TableColumn<Symptom, String> zoneCol = new TableColumn<>("Zone");
        zoneCol.setCellValueFactory(new PropertyValueFactory<>("zone"));
        zoneCol.setPrefWidth(150);
        
        tableView.getColumns().addAll(typeCol, intensiteCol, zoneCol);
        tableView.setItems(data);
        
        typeCombo.setItems(FXCollections.observableArrayList(Symptom.TYPE_CHOICES));
        
        intensiteSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            intensiteLabel.setText(String.valueOf(newVal.intValue()));
        });
        
        intensiteSlider.setValue(5);
        loadTable();
    }

    @FXML
    private void loadTable() {
        try {
            List<Symptom> symptoms = symptomDAO.findAll();
            data.clear();
            data.addAll(symptoms);
            setStatus("Loaded " + symptoms.size() + " symptoms");
        } catch (Exception e) {
            showError("Error loading: " + e.getMessage());
        }
    }

    @FXML
    private void search() {
        try {
            String search = searchField.getText();
            List<Symptom> symptoms = symptomDAO.findAll();
            
            if (search != null && !search.isEmpty()) {
                symptoms.removeIf(s -> !s.getType().toLowerCase().contains(search.toLowerCase()));
            }
            
            data.clear();
            data.addAll(symptoms);
            setStatus("Found " + symptoms.size() + " results");
        } catch (Exception e) {
            showError("Error searching: " + e.getMessage());
        }
    }

    @FXML
    private void selectRow(MouseEvent event) {
        Symptom selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            fillForm(selected);
        }
    }

    @FXML
    private void add() {
        try {
            if (!validate()) return;
            Symptom symptom = createFromForm();
            symptomDAO.save(symptom);
            clearForm();
            loadTable();
            setStatus("Added symptom with ID: " + symptom.getId());
        } catch (Exception e) {
            showError("Error adding: " + e.getMessage());
        }
    }

    @FXML
    private void update() {
        try {
            String idStr = idField.getText();
            if (idStr == null || idStr.isEmpty()) {
                showError("Select a symptom to update");
                return;
            }
            if (!validate()) return;
            int id = Integer.parseInt(idStr);
            Symptom symptom = createFromForm();
            symptom.setId(id);
            symptomDAO.save(symptom);
            clearForm();
            loadTable();
            setStatus("Symptom updated");
        } catch (Exception e) {
            showError("Error updating: " + e.getMessage());
        }
    }

    @FXML
    private void delete() {
        String idStr = idField.getText();
        if (idStr == null || idStr.isEmpty()) {
            showError("Select a symptom to delete");
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Confirmation");
        alert.setHeaderText("Delete this symptom?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                int id = Integer.parseInt(idStr);
                symptomDAO.delete(id);
                clearForm();
                loadTable();
                setStatus("Symptom deleted");
            } catch (Exception e) {
                showError("Error deleting: " + e.getMessage());
            }
        }
    }

    @FXML
    private void clearForm() {
        idField.clear();
        typeCombo.getSelectionModel().clearSelection();
        intensiteSlider.setValue(5);
        zoneField.clear();
        entryIdField.clear();
        statusLabel.setText("");
    }

    @FXML
    private void refresh() {
        searchField.clear();
        loadTable();
    }

    private void fillForm(Symptom symptom) {
        idField.setText(String.valueOf(symptom.getId()));
        typeCombo.getSelectionModel().select(symptom.getType());
        intensiteSlider.setValue(symptom.getIntensite());
        intensiteLabel.setText(String.valueOf(symptom.getIntensite()));
        zoneField.setText(symptom.getZone());
        entryIdField.setText(String.valueOf(symptom.getEntryId()));
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
            showError("Type is required");
            return false;
        }
        
        int intensite = (int) intensiteSlider.getValue();
        if (intensite < 1 || intensite > 10) {
            showError("Intensity must be between 1 and 10");
            return false;
        }
        
        try {
            int entryId = Integer.parseInt(entryIdField.getText());
            if (entryId <= 0) {
                showError("Entry ID must be positive");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Invalid entry ID");
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