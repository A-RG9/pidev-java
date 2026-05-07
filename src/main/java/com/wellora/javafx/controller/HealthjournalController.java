package com.wellora.javafx.controller;

import com.wellora.controllers.HealthNavigationProxy;
import com.wellora.dao.HealthentryDAO;
import com.wellora.dao.HealthjournalDAO;
import com.wellora.model.Healthentry;
import com.wellora.model.Healthjournal;
import com.wellcare.javafx.util.SceneManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Healthjournal Controller - Full CRUD
 */
public class HealthjournalController {

    private MainController mainController;

    private final HealthjournalDAO journalDAO = new HealthjournalDAO();
    private final HealthentryDAO entryDAO = new HealthentryDAO();
    private final ObservableList<Healthjournal> data = FXCollections.observableArrayList();
    private final ObservableList<Healthentry> entryData = FXCollections.observableArrayList();

    @FXML private TableView<Healthjournal> tableView;
    @FXML private TextField searchField;
    @FXML private Label totalJournalsLabel;
    @FXML private Label activeJournalsLabel;
    @FXML private Label totalJournalEntriesLabel;
    @FXML private TextField nameField;
    @FXML private DatePicker datedebutPicker;
    @FXML private DatePicker datefinPicker;
    @FXML private Label statusLabel;
    
    // Entry table
    @FXML private TableView<Healthentry> entryTable;

    @FXML
    private void initialize() {
        // Journal table columns (no ID column)
        TableColumn<Healthjournal, String> nameCol = new TableColumn<>("Nom");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(150);
        
        TableColumn<Healthjournal, LocalDate> datedebutCol = new TableColumn<>("Date Debut");
        datedebutCol.setCellValueFactory(new PropertyValueFactory<>("datedebut"));
        datedebutCol.setPrefWidth(120);
        
        TableColumn<Healthjournal, LocalDate> datefinCol = new TableColumn<>("Date Fin");
        datefinCol.setCellValueFactory(new PropertyValueFactory<>("datefin"));
        datefinCol.setPrefWidth(120);
        
        tableView.getColumns().clear();
        tableView.getColumns().addAll(nameCol, datedebutCol, datefinCol);
        tableView.setItems(data);
        
        // Entry table columns
        TableColumn<Healthentry, LocalDate> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateCol.setPrefWidth(100);
        
        TableColumn<Healthentry, Double> poidsCol = new TableColumn<>("Weight");
        poidsCol.setCellValueFactory(new PropertyValueFactory<>("poids"));
        poidsCol.setPrefWidth(60);
        
        TableColumn<Healthentry, Double> glycemieCol = new TableColumn<>("BloodSugar");
        glycemieCol.setCellValueFactory(new PropertyValueFactory<>("glycemie"));
        glycemieCol.setPrefWidth(70);
        
        TableColumn<Healthentry, Integer> sommeilCol = new TableColumn<>("Sleep");
        sommeilCol.setCellValueFactory(new PropertyValueFactory<>("sommeil"));
        sommeilCol.setPrefWidth(60);
        
        entryTable.getColumns().addAll(dateCol, poidsCol, glycemieCol, sommeilCol);
        entryTable.setItems(entryData);
        
        loadTable();
    }

    @FXML
    private void loadTable() {
        try {
            String userId = SceneManager.getInstance().getCurrentUser().getUuid();
            List<Healthjournal> journals = journalDAO.findAll(userId);
            data.clear();
            data.addAll(journals);
            setStatus("Loaded " + journals.size() + " journals");
        } catch (Exception e) {
            showError("Error loading: " + e.getMessage());
        }
    }

    @FXML
    private void search() {
        try {
            String userId = SceneManager.getInstance().getCurrentUser().getUuid();
            String search = searchField.getText();
            List<Healthjournal> journals = journalDAO.search(
                search.isEmpty() ? null : search, 
                null, "datedebut", "DESC", userId
            );
            data.clear();
            data.addAll(journals);
            setStatus("Found " + journals.size() + " results");
        } catch (Exception e) {
            showError("Error searching: " + e.getMessage());
        }
    }

    @FXML
    private void selectRow(MouseEvent event) {
        Healthjournal selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            fillForm(selected);
            loadEntriesForJournal(selected);
        }
    }

    private void loadEntriesForJournal(Healthjournal journal) {
        try {
            entryData.clear();
            List<Healthentry> entries = entryDAO.findByJournalId(journal.getId());
            entryData.addAll(entries);
        } catch (Exception e) {
            // Ignore
        }
    }

    @FXML
    private void add() {
        try {
            if (!validate()) return;
            Healthjournal journal = createFromForm();
            journalDAO.save(journal);
            clearForm();
            loadTable();
            setStatus("Added journal");
        } catch (Exception e) {
            showError("Error adding: " + e.getMessage());
        }
    }

    @FXML
    private void update() {
        try {
            Healthjournal selected = tableView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showError("Select a journal to update");
                return;
            }
            if (!validate()) return;
            Healthjournal journal = createFromForm();
            journal.setId(selected.getId());
            journalDAO.save(journal);
            clearForm();
            loadTable();
            setStatus("Journal updated");
        } catch (Exception e) {
            showError("Error updating: " + e.getMessage());
        }
    }

    @FXML
    private void delete() {
        Healthjournal selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Select a journal to delete");
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Confirmation");
        alert.setHeaderText("Delete this journal?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                journalDAO.delete(selected.getId());
                clearForm();
                loadTable();
                setStatus("Journal deleted");
            } catch (Exception e) {
                showError("Error deleting: " + e.getMessage());
            }
        }
    }

    @FXML
    private void clearForm() {
        nameField.clear();
        datedebutPicker.setValue(null);
        datefinPicker.setValue(null);
        entryData.clear();
        statusLabel.setText("Nouveau formulaire");
    }

    /**
     * Show/View entries for the selected journal
     */
    @FXML
    private void showEntries() {
        Healthjournal selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Sélectionnez un journal pour voir ses entrées");
            return;
        }
        try {
            // Load entries for this journal
            entryData.clear();
            List<Healthentry> entries = entryDAO.findByJournalId(selected.getId());
            entryData.addAll(entries);
            setStatus("Affichage de " + selected.getName() + " - " + entries.size() + " entrées");
        } catch (Exception e) {
            showError("Erreur: " + e.getMessage());
        }
    }

    @FXML
    private void refresh() {
        searchField.clear();
        loadTable();
    }

    private void fillForm(Healthjournal journal) {
        nameField.setText(journal.getName());
        datedebutPicker.setValue(journal.getDatedebut());
        datefinPicker.setValue(journal.getDatefin());
    }

    private Healthjournal createFromForm() {
        String userId = SceneManager.getInstance().getCurrentUser().getUuid();
        Healthjournal journal = new Healthjournal(
            0,
            nameField.getText().trim(),
            datedebutPicker.getValue(),
            datefinPicker.getValue()
        );
        journal.setUserId(userId);
        return journal;
    }

    private boolean validate() {
        String name = nameField.getText();
        LocalDate datedebut = datedebutPicker.getValue();
        LocalDate datefin = datefinPicker.getValue();
        
        if (name == null || name.isEmpty()) {
            showError("Name is required");
            return false;
        }
        
        if (datedebut == null) {
            showError("Start date is required");
            return false;
        }
        
        if (datefin == null) {
            showError("End date is required");
            return false;
        }
        
        if (datefin.isBefore(datedebut)) {
            showError("End date must be after start date");
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

    /**
     * Set proxy for health sub-module navigation
     * HealthjournalController uses direct DAO operations, not proxy navigation
     * This method exists for interface compatibility
     */
    public void setMainControllerProxy(HealthNavigationProxy proxy) {
        // HealthjournalController uses direct DAO operations, not proxy navigation
        // This method exists for interface compatibility
    }
}