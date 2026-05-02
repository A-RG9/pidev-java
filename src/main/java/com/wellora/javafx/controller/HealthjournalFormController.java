package com.wellora.javafx.controller;

import javafx.application.Platform;

import com.wellora.controllers.HealthNavigationProxy;

import com.wellora.dao.HealthjournalDAO;
import com.wellora.model.Healthjournal;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;

/**
 * HealthjournalFormController - Handles the form for Health Journals
 * 
 * Responsibilities:
 * - Handle both Create and Update modes
 * - Validate input
 * - Save to database
 * - Navigate back to list after save
 * 
 * Usage:
 * - Call setJournal(null) for Create mode
 * - Call setJournal(journal) for Edit mode
 */
public class HealthjournalFormController {

    private HealthNavigationProxy proxy;

    private HealthjournalDAO journalDAO = new HealthjournalDAO();
    private Healthjournal currentJournal;
    private MainController mainController;

    // Form Fields
    @FXML private Label headerLabel;
    @FXML private TextField idField;
    @FXML private TextField nameField;
    @FXML private DatePicker datedebutPicker;
    @FXML private DatePicker datefinPicker;
    @FXML private Label statusLabel;

    /**
     * Initialize - called automatically when FXML is loaded
     */
    @FXML
    private void initialize() {
        // Set default mode text (will be updated by setJournal)
        headerLabel.setText("Créer un Journal");
    }

    /**
     * Set the journal to edit (null for create mode)
     * Call this BEFORE the view is shown
     */
    public void setJournal(Healthjournal journal) {
        this.currentJournal = journal;
        
        if (journal != null) {
            // Edit mode - pre-fill form
            headerLabel.setText("Modifier le Journal");
            idField.setText(String.valueOf(journal.getId()));
            nameField.setText(journal.getName());
            datedebutPicker.setValue(journal.getDatedebut());
            datefinPicker.setValue(journal.getDatefin());
        } else {
            // Create mode - clear form
            headerLabel.setText("Créer un Journal");
            idField.setText("");
            nameField.setText("");
            datedebutPicker.setValue(null);
            datefinPicker.setValue(null);
        }
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    /**
     * Save the journal (create or update)
     */
    @FXML
    private void save() {
        if (!validate()) {
            return;
        }

        try {
            Healthjournal journal = createFromForm();
            
            // If we have an ID, it's an update
            if (currentJournal != null) {
                journal.setId(currentJournal.getId());
            }
            
            journalDAO.save(journal);
            
            setStatus("Journal enregistré avec succès!");
            // Navigate back to list
            Platform.runLater(() -> {
                if (proxy != null) proxy.showHealthJournals();
            });
            
        } catch (Exception e) {
            showError("Erreur lors de l'enregistrement: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Cancel and go back to list
     */
    @FXML
    private void cancel() {
        if (proxy != null) proxy.showHealthJournals();
    }

    /**
     * Create Healthjournal object from form fields
     */
    private Healthjournal createFromForm() {
        String name = nameField.getText().trim();
        LocalDate datedebut = datedebutPicker.getValue();
        LocalDate datefin = datefinPicker.getValue();
        
        return new Healthjournal(0, name, datedebut, datefin);
    }

    /**
     * Validate form input
     */
    private boolean validate() {
        String name = nameField.getText();
        LocalDate datedebut = datedebutPicker.getValue();
        LocalDate datefin = datefinPicker.getValue();

        if (name == null || name.isEmpty()) {
            showError("Le nom du journal est requis");
            return false;
        }

        if (datedebut == null) {
            showError("La date de début est requise");
            return false;
        }

        if (datefin == null) {
            showError("La date de fin est requise");
            return false;
        }

        if (datefin.isBefore(datedebut)) {
            showError("La date de fin doit être après la date de début");
            return false;
        }

        return true;
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