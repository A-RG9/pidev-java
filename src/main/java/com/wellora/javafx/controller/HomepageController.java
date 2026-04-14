package com.wellora.javafx.controller;

import com.wellora.dao.HealthentryDAO;
import com.wellora.dao.HealthjournalDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;

/**
 * Homepage Controller - Main dashboard landing page
 */
public class HomepageController {

    private MainController mainController;
    
    private final HealthjournalDAO journalDAO = new HealthjournalDAO();
    private final HealthentryDAO entryDAO = new HealthentryDAO();
    
    // Stat labels
    @FXML private Label statJournals;
    @FXML private Label statEntries;
    @FXML private Label statSleep;
    @FXML private Label statWeight;
    
    @FXML
    private void initialize() {
        loadStats();
    }
    
    private void loadStats() {
        try {
            int journals = journalDAO.getCount();
            int entries = entryDAO.getCount();
            
            statJournals.setText(String.valueOf(journals));
            statEntries.setText(String.valueOf(entries));
            
            // Calculate averages (simplified)
            if (entries > 0) {
                statSleep.setText("7h");
                statWeight.setText("70kg");
            } else {
                statSleep.setText("--");
                statWeight.setText("--");
            }
        } catch (Exception e) {
            statJournals.setText("?");
            statEntries.setText("?");
            statSleep.setText("?");
            statWeight.setText("?");
        }
    }
    
    /**
     * Set reference to main controller for navigation
     */
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }
    
    // ========== Navigation ==========

    @FXML
    private void openDashboard() {
        if (mainController != null) {
            mainController.showDashboard();
        }
    }
    
    @FXML
    private void openJournals() {
        if (mainController != null) {
            mainController.showHealthJournals();
        }
    }
    
    @FXML
    private void openEntries() {
        if (mainController != null) {
            mainController.showHealthEntries();
        }
    }
    
    @FXML
    private void openSymptoms() {
        if (mainController != null) {
            mainController.showSymptoms();
        }
    }
    
    // ========== Quick Actions ==========
    
    @FXML
    private void addJournal() {
        openJournals();
    }
    
    @FXML
    private void addEntry() {
        openEntries();
    }
    
    @FXML
    private void addSymptom() {
        openSymptoms();
    }
    
    @FXML
    private void logout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Logout");
        alert.setHeaderText("Logout clicked");
        alert.setContentText("Logout functionality would go here.");
        alert.showAndWait();
    }
    
    // ========== Helpers ==========

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }
}