package com.wellora.javafx.controller;

import com.wellora.javafx.WelloraApp;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

/**
 * MainController - Handles application navigation
 * Uses dynamic content switching (no new windows)
 */
public class MainController {

    // Navigation Buttons
    @FXML private Button btnHome;
    @FXML private Button btnDashboard;
    @FXML private Button btnJournals;
    @FXML private Button btnEntries;
    @FXML private Button btnSymptoms;

    // Content Area - where views are loaded
    @FXML private VBox contentArea;

    // Current active button for styling
    private Button activeButton;

    /**
     * Initialize - called automatically after FXML load
     * Loads the homepage by default
     */
    @FXML
    private void initialize() {
        // Set home as active by default
        setActiveButton(btnHome);
        showHomepage();
    }

    // ========== NAVIGATION METHODS ==========

    /**
     * Show Homepage
     */
    @FXML
    public void showHomepage() {
        setActiveButton(btnHome);
        loadView("/fxml/homepage.fxml");
    }

    /**
     * Show Dashboard with charts
     */
    @FXML
    public void showDashboard() {
        setActiveButton(btnDashboard);
        loadView("/fxml/dashboard.fxml");
    }

    /**
     * Show Health Journals management (List View)
     */
    @FXML
    public void showHealthJournals() {
        setActiveButton(btnJournals);
        loadView("/fxml/healthjournal-list.fxml");
    }

    /**
     * Show Health Entries management (List View)
     */
    @FXML
    public void showHealthEntries() {
        setActiveButton(btnEntries);
        loadView("/fxml/healthentry-list.fxml");
    }

    /**
     * Show Symptoms management (List View)
     */
    @FXML
    public void showSymptoms() {
        setActiveButton(btnSymptoms);
        loadView("/fxml/symptom-list.fxml");
    }

    /**
     * Logout action
     */
    @FXML
    private void logout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout");
        alert.setHeaderText("Are you sure you want to logout?");
        alert.setContentText("Click OK to logout or Cancel to stay.");

        alert.showAndWait().ifPresent(response -> {
            if (response.getButtonData().isDefaultButton()) {
                // Close the application
                System.exit(0);
            }
        });
    }

    // ========== HELPER METHODS ==========

    /**
     * Load a pre-loaded Parent view into the content area
     * Used by form controllers to load views
     */
    public void loadContentView(Parent view) {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(view);
    }

    /**
     * Load FXML view into the content area
     * @param fxmlPath path to FXML file
     */
    private void loadView(String fxmlPath) {
        try {
            // Clear current content
            contentArea.getChildren().clear();

            // Load new view - use WelloraApp class for stable classpath reference
            FXMLLoader loader = new FXMLLoader(WelloraApp.class.getResource(fxmlPath));
            Parent view = loader.load();

            // Add to content area
            contentArea.getChildren().add(view);

            // If the loaded controller has a setMainController method, pass reference
            Object controller = loader.getController();
            if (controller instanceof HomepageController) {
                ((HomepageController) controller).setMainController(this);
            } else if (controller instanceof DashboardController) {
                ((DashboardController) controller).setMainController(this);
            } else if (controller instanceof HealthjournalListController) {
                ((HealthjournalListController) controller).setMainController(this);
            } else if (controller instanceof HealthentryListController) {
                ((HealthentryListController) controller).setMainController(this);
            } else if (controller instanceof SymptomListController) {
                ((SymptomListController) controller).setMainController(this);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to load view: " + fxmlPath + "\n" + e.getMessage());
        }
    }

    /**
     * Update active button styling
     */
    private void setActiveButton(Button button) {
        // Reset previous active button
        if (activeButton != null) {
            activeButton.getStyleClass().remove("active");
        }

        // Set new active button
        activeButton = button;
        if (activeButton != null) {
            activeButton.getStyleClass().add("active");
        }
    }

    /**
     * Show error alert
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Something went wrong");
        alert.setContentText(message);
        alert.showAndWait();
    }
}