package com.wellora.javafx.controller;

import com.wellora.controllers.HealthNavigationProxy;

import com.wellora.dao.SymptomDAO;
import com.wellora.model.Symptom;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;

import java.util.List;
import java.util.Optional;

/**
 * SymptomListController - Handles the list view for Symptoms
 */
public class SymptomListController {

    private HealthNavigationProxy proxy;

    private MainController mainController;
    private final SymptomDAO symptomDAO = new SymptomDAO();
    private final ObservableList<Symptom> data = FXCollections.observableArrayList();

    // Table
    @FXML private TableView<Symptom> tableView;
    @FXML private TableColumn<Symptom, String> typeCol;
    @FXML private TableColumn<Symptom, Integer> intensiteCol;
    @FXML private TableColumn<Symptom, String> zoneCol;
    
    // Search
    @FXML private TextField searchField;
    
    // Status
    @FXML private Label statusLabel;

    @FXML
    private void initialize() {
        // Clear FXML columns first (fixes duplicate column issue)
        tableView.getColumns().clear();
        
        // Setup columns
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        intensiteCol.setCellValueFactory(new PropertyValueFactory<>("intensite"));
        zoneCol.setCellValueFactory(new PropertyValueFactory<>("zone"));
        
        tableView.setItems(data);
        loadTable();
    }

    @FXML
    private void loadTable() {
        try {
            List<Symptom> symptoms = symptomDAO.findAll();
            data.clear();
            data.addAll(symptoms);
            setStatus("Affiché " + symptoms.size() + " symptômes");
        } catch (Exception e) {
            showError("Erreur: " + e.getMessage());
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
            setStatus("Trouvé " + symptoms.size() + " résultats");
        } catch (Exception e) {
            showError("Erreur: " + e.getMessage());
        }
    }

    @FXML
    private void refresh() {
        searchField.clear();
        loadTable();
    }

    @FXML
    private void selectRow(MouseEvent event) {}

    @FXML
    private void createNew() {
        navigateToForm(null);
    }

    @FXML
    private void editSelected() {
        Symptom selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Sélectionnez un symptôme à modifier");
            return;
        }
        navigateToForm(selected);
    }

    @FXML
    private void deleteSelected() {
        Symptom selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Sélectionnez un symptôme à supprimer");
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer ce symptôme?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                symptomDAO.delete(selected.getId());
                loadTable();
                setStatus("Symptôme supprimé");
            } catch (Exception e) {
                showError("Erreur: " + e.getMessage());
            }
        }
    }

    @FXML
    private void clearSelection() {
        tableView.getSelectionModel().clearSelection();
        setStatus("");
    }

    private void navigateToForm(Symptom symptom) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/symptom-form.fxml"));
            Parent formView = loader.load();
            
            SymptomFormController formController = loader.getController();
            formController.setSymptom(symptom);
            formController.setMainController(this.mainController);
            
            if (proxy != null) { proxy.loadContentView(formView); }
        } catch (Exception e) {
            showError("Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void refreshAfterSave() {
        loadTable();
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    private void setStatus(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
    }

    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
    }

    public void setMainControllerProxy(HealthNavigationProxy proxy) {
        this.proxy = proxy;
    }

}