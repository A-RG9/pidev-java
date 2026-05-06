package com.wellora.javafx.controller;

import com.wellora.controllers.HealthNavigationProxy;
import com.wellora.dao.HealthentryDAO;
import com.wellora.dao.HealthjournalDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;

public class HomepageController {

    private HealthNavigationProxy proxy;
    private MainController mainController;

    private final HealthjournalDAO journalDAO = new HealthjournalDAO();
    private final HealthentryDAO entryDAO = new HealthentryDAO();

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

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setMainControllerProxy(HealthNavigationProxy proxy) {
        this.proxy = proxy;
    }

    @FXML
    private void openDashboard() {
        if (proxy != null) {
            proxy.showDashboard();
        } else {
            System.err.println("Dashboard navigation failed: Proxy is null");
        }
    }

    @FXML
    private void openJournals() {
        if (proxy != null) {
            proxy.showHealthJournals();
        } else {
            System.err.println("Journals navigation failed: Proxy is null");
        }
    }

    @FXML
    private void openEntries() {
        if (proxy != null) {
            proxy.showHealthEntries();
        } else {
            System.err.println("Entries navigation failed: Proxy is null");
        }
    }

    @FXML
    private void openSymptoms() {
        if (proxy != null) {
            proxy.showSymptoms();
        } else {
            System.err.println("Symptoms navigation failed: Proxy is null");
        }
    }

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
}