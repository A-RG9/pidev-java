package com.wellora.javafx.controller;

import com.wellora.controllers.HealthNavigationProxy;

import com.wellora.dao.HealthentryDAO;
import com.wellora.dao.HealthjournalDAO;
import com.wellora.dao.SymptomDAO;
import com.wellora.javafx.WelloraApp;
import com.wellora.model.Healthentry;
import com.wellora.model.Healthjournal;
import com.wellora.model.Symptom;
import com.wellcare.javafx.util.SceneManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * HealthentryListController - Handles the list view for Health Entries
 */
public class HealthentryListController {

    private HealthNavigationProxy proxy;

    private MainController mainController;
    private final HealthentryDAO entryDAO = new HealthentryDAO();
    private final HealthjournalDAO journalDAO = new HealthjournalDAO();
    private final SymptomDAO symptomDAO = new SymptomDAO();
    private final ObservableList<Healthentry> data = FXCollections.observableArrayList();

    // Table
    @FXML private TableView<Healthentry> tableView;
    @FXML private TableColumn<Healthentry, LocalDate> dateCol;
    @FXML private TableColumn<Healthentry, Double> poidsCol;
    @FXML private TableColumn<Healthentry, Double> glycemieCol;
    @FXML private TableColumn<Healthentry, String> tensionCol;
    @FXML private TableColumn<Healthentry, Integer> sommeilCol;
    @FXML private TableColumn<Healthentry, String> symptomCol;
    
    // Search
    @FXML private TextField searchField;
    
    // Status
    @FXML private Label statusLabel;

    @FXML
    private void initialize() {
        // Setup columns
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        poidsCol.setCellValueFactory(new PropertyValueFactory<>("poids"));
        glycemieCol.setCellValueFactory(new PropertyValueFactory<>("glycemie"));
        tensionCol.setCellValueFactory(new PropertyValueFactory<>("tension"));
        sommeilCol.setCellValueFactory(new PropertyValueFactory<>("sommeil"));
        symptomCol.setCellFactory(col -> new javafx.scene.control.TableCell<Healthentry, String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                } else {
                    setText(item);
                }
            }
        });
        symptomCol.setCellValueFactory(cellData -> {
            int entryId = cellData.getValue().getId();
            try {
                List<Symptom> symptoms = symptomDAO.findByEntryId(entryId);
                if (symptoms.isEmpty()) {
                    return new SimpleStringProperty("none");
                }
                // Join symptom types with + between them
                String types = symptoms.stream()
                    .map(Symptom::getType)
                    .reduce((a, b) -> a + " + " + b)
                    .orElse("");
                return new SimpleStringProperty(types);
            } catch (Exception e) {
                return new SimpleStringProperty("Error");
            }
        });
        
        tableView.setItems(data);
        loadTable();
    }

    @FXML
    private void loadTable() {
        try {
            String userId = SceneManager.getInstance().getCurrentUser().getUuid();
            List<Healthentry> entries = entryDAO.findAll(userId);
            data.clear();
            data.addAll(entries);
            setStatus("Affiché " + entries.size() + " entrées");
        } catch (Exception e) {
            showError("Erreur: " + e.getMessage());
        }
    }

    @FXML
    private void search() {
        try {
            String userId = SceneManager.getInstance().getCurrentUser().getUuid();
            String search = searchField.getText();
            List<Healthentry> entries = entryDAO.search(
                search.isEmpty() ? null : search, null, "date", "DESC", userId
            );
            data.clear();
            data.addAll(entries);
            setStatus("Trouvé " + entries.size() + " résultats");
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
        Healthentry selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Sélectionnez une entrée à modifier");
            return;
        }
        navigateToForm(selected);
    }

    @FXML
    private void deleteSelected() {
        Healthentry selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Sélectionnez une entrée à supprimer");
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer cette entrée?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                entryDAO.delete(selected.getId());
                loadTable();
                setStatus("Entrée supprimée");
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

    private void navigateToForm(Healthentry entry) {
        try {
            FXMLLoader loader = new FXMLLoader(WelloraApp.class.getResource("/fxml/healthentry-form.fxml"));
            Parent formView = loader.load();
            
            HealthentryFormController formController = loader.getController();
            formController.setEntry(entry);
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
        statusLabel.getStyleClass().setAll("status-label", "status-success");
    }

    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.getStyleClass().setAll("status-label", "status-error");
    }

    public void setMainControllerProxy(HealthNavigationProxy proxy) {
        this.proxy = proxy;
    }

}