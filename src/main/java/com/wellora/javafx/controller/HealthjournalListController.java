package com.wellora.javafx.controller;

import com.wellora.controllers.HealthNavigationProxy;

import com.wellora.dao.HealthjournalDAO;
import com.wellora.javafx.WelloraApp;
import com.wellora.model.Healthjournal;
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
 * HealthjournalListController - Handles the list view for Health Journals
 * 
 * Responsibilities:
 * - Display all journals in TableView
 * - Handle search and refresh
 * - Navigate to form for create/edit
 * - Handle delete
 */
public class HealthjournalListController {

    private HealthNavigationProxy proxy;

    private MainController mainController;
    private final HealthjournalDAO journalDAO = new HealthjournalDAO();
    private final ObservableList<Healthjournal> data = FXCollections.observableArrayList();

    // Table
    @FXML private TableView<Healthjournal> tableView;
    @FXML private TableColumn<Healthjournal, String> nameCol;
    @FXML private TableColumn<Healthjournal, LocalDate> datedebutCol;
    @FXML private TableColumn<Healthjournal, LocalDate> datefinCol;
    
    // Search
    @FXML private TextField searchField;
    
    // Status
    @FXML private Label statusLabel;

    @FXML
    private void initialize() {
        // Setup column cell value factories
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        datedebutCol.setCellValueFactory(new PropertyValueFactory<>("datedebut"));
        datefinCol.setCellValueFactory(new PropertyValueFactory<>("datefin"));
        
        tableView.setItems(data);
        loadTable();
    }

    /**
     * Load all journals from database
     */
    @FXML
    private void refresh() {
        searchField.clear();
        loadTable();
    }

    @FXML
    private void loadTable() {
        try {
            List<Healthjournal> journals = journalDAO.findAll();
            data.clear();
            data.addAll(journals);
            setStatus("Affiché " + journals.size() + " journaux");
        } catch (Exception e) {
            showError("Erreur lors du chargement: " + e.getMessage());
        }
    }

    /**
     * Search journals by name
     */
    @FXML
    private void search() {
        try {
            String search = searchField.getText();
            List<Healthjournal> journals = journalDAO.search(
                search.isEmpty() ? null : search, 
                null, "datedebut", "DESC"
            );
            data.clear();
            data.addAll(journals);
            setStatus("Trouvé " + journals.size() + " résultats");
        } catch (Exception e) {
            showError("Erreur lors de la recherche: " + e.getMessage());
        }
    }

    /**
     * Handle row selection
     */
    @FXML
    private void selectRow(MouseEvent event) {
        // Just for visual feedback - can add edit on double-click if needed
    }

    /**
     * Open the form to create a new journal
     */
    @FXML
    private void createNew() {
        navigateToForm(null); // null = create mode
    }

    /**
     * Open the form to edit the selected journal
     */
    @FXML
    private void editSelected() {
        Healthjournal selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Sélectionnez un journal à modifier");
            return;
        }
        navigateToForm(selected); // Pass the selected journal
    }

    /**
     * Delete the selected journal
     */
    @FXML
    private void deleteSelected() {
        Healthjournal selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Sélectionnez un journal à supprimer");
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer ce journal?");
        alert.setContentText("Cliquez OK pour confirmer ou Annuler pour annuler.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                journalDAO.delete(selected.getId());
                clearSelection();
                loadTable();
                setStatus("Journal supprimé");
            } catch (Exception e) {
                showError("Erreur lors de la suppression: " + e.getMessage());
            }
        }
    }

    /**
     * Clear table selection
     */
    @FXML
    private void clearSelection() {
        tableView.getSelectionModel().clearSelection();
        setStatus("");
    }

    /**
     * Navigate to the form page (create or edit mode)
     */
    private void navigateToForm(Healthjournal journal) {
        try {
            FXMLLoader loader = new FXMLLoader(WelloraApp.class.getResource("/fxml/healthjournal-form.fxml"));
            Parent formView = loader.load();
            
            // Get the form controller and pass the journal (or null for create)
            HealthjournalFormController formController = loader.getController();
            formController.setJournal(journal); // null = create, object = edit
            formController.setMainController(this.mainController); // Pass main controller for navigation
            
            // Ask MainController to load this view
            if (proxy != null) { proxy.loadContentView(formView); }
        } catch (Exception e) {
            showError("Erreur lors du chargement du formulaire: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Refresh the list after form save (called by form controller)
     */
    public void refreshAfterSave() {
        loadTable();
    }

    /**
     * Set reference to MainController for navigation
     */
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    /**
     * Navigate back to the journals list view (called by form controller after save)
     */
    public void navigateBackToList() {
        if (mainController != null) {
            if (proxy != null) proxy.showHealthJournals();
        }
    }

    // ========== Helper Methods ==========

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

    public void setMainControllerProxy(HealthNavigationProxy proxy) {
        this.proxy = proxy;
    }

}