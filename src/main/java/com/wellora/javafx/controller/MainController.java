package com.wellora.javafx.controller;

import com.wellora.javafx.WelloraApp;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;

/**
 * MainController - Handles application navigation
 * Uses dynamic content switching (no new windows)
 */
public class MainController {

    // Navigation Buttons - Health
    @FXML private Button btnHome;
    @FXML private Button btnDashboard;
    @FXML private Button btnJournals;
    @FXML private Button btnEntries;
    @FXML private Button btnSymptoms;
    @FXML private Button btnCalendar;
    @FXML private Button btnPrediction;
    @FXML private Button btnTheme;
    @FXML private Button btnLogout;

    // Navigation Buttons - Nutrition
    @FXML private Button btnNutritionDashboard;
    @FXML private Button btnNutritionJournal;
    @FXML private Button btnNutritionObjectifs;
    @FXML private Button btnNutritionPlanificateur;
    @FXML private Button btnNutritionRecettes;
    @FXML private Button btnNutritionAnalyse;

    // Navigation Buttons - Health Trail
    @FXML private Button btnAfficherParcours;
    @FXML private Button btnAjouterParcours;
    @FXML private Button btnToutesPublications;

    // Content Area - where views are loaded
    @FXML private VBox contentArea;

    // Current active button for styling
    private Button activeButton;
    
    // Theme tracking - shared across all views
    private static boolean isDarkTheme = false;

    /**
     * Initialize - called automatically after FXML load
     * Loads the homepage by default
     */
    @FXML
    private void initialize() {
        // Set the theme button text based on current theme
        if (btnTheme != null) {
            btnTheme.setText(isDarkTheme ? "🌙  Dark Mode" : "☀️  Light Mode");
        }
        
        // Apply theme when the scene becomes available (since initialize() runs before scene is set)
        contentArea.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                applyCurrentTheme();
            }
        });
        
        // Set home as active by default
        setActiveButton(btnHome);
        showHomepage();
    }

    /**
     * Get the current dark theme state
     */
    public boolean isDarkTheme() {
        return isDarkTheme;
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
     * Show Calendar view
     */
    @FXML
    public void showCalendar() {
        setActiveButton(btnCalendar);
        loadView("/fxml/calendar.fxml");
    }

    /**
     * Show AI Prediction view
     */
    @FXML
    public void showPrediction() {
        setActiveButton(btnPrediction);
        loadView("/fxml/prediction.fxml");
    }

    // ========== NUTRITION NAVIGATION METHODS ==========

    @FXML
    public void showNutritionDashboard() {
        setActiveButton(btnNutritionDashboard);
        loadNutritionView("/com/wellora/views/Dashboard.fxml");
    }

    @FXML
    public void showNutritionJournal() {
        setActiveButton(btnNutritionJournal);
        loadNutritionView("/com/wellora/views/Journal.fxml");
    }

    @FXML
    public void showNutritionObjectifs() {
        setActiveButton(btnNutritionObjectifs);
        loadNutritionView("/com/wellora/views/Objectif.fxml");
    }

    @FXML
    public void showNutritionPlanificateur() {
        setActiveButton(btnNutritionPlanificateur);
        loadNutritionView("/com/wellora/views/Planificateur.fxml");
    }

    @FXML
    public void showNutritionRecettes() {
        setActiveButton(btnNutritionRecettes);
        loadNutritionView("/com/wellora/views/Recettes.fxml");
    }

    @FXML
    public void showNutritionAnalyse() {
        setActiveButton(btnNutritionAnalyse);
        loadNutritionView("/com/wellora/views/Analyse.fxml");
    }

    // ========== HEALTH TRAIL NAVIGATION METHODS ==========

    @FXML
    public void showAfficherParcours() {
        setActiveButton(btnAfficherParcours);
        loadView("/fxml/AfficherParcours.fxml");
    }

    @FXML
    public void showAjouterParcours() {
        setActiveButton(btnAjouterParcours);
        loadView("/fxml/AjouterParcoursDeSante.fxml");
    }

    @FXML
    public void showToutesPublications() {
        setActiveButton(btnToutesPublications);
        loadView("/fxml/ToutesPublications.fxml");
    }

    /**
     * Load a Nutrition FXML view (from PIDEV-JAVA) into the content area.
     */
    private void loadNutritionView(String fxmlPath) {
        try {
            contentArea.getChildren().clear();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            javafx.scene.Parent view = loader.load();
            contentArea.getChildren().add(view);
            applyCurrentTheme();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to load nutrition view: " + fxmlPath + "\n" + e.getMessage());
        }
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

    /**
     * Toggle between light and dark theme
     */
    @FXML
    private void toggleTheme() {
        isDarkTheme = !isDarkTheme;
        
        // Update the theme button text
        if (btnTheme != null) {
            btnTheme.setText(isDarkTheme ? "🌙  Dark Mode" : "☀️  Light Mode");
        }
        
        // Apply theme to scene
        applyCurrentTheme();
    }
    
    /**
     * Apply the current theme to the entire scene
     */
    private void applyCurrentTheme() {
        if (contentArea != null && contentArea.getScene() != null) {
            Scene scene = contentArea.getScene();
            var root = scene.getRoot();
            
            // Clear existing stylesheets
            scene.getStylesheets().clear();
            
            // Add base CSS files in order: style.css (sidebar/base), dashboard.css (light theme base)
            scene.getStylesheets().add(WelloraApp.class.getResource("/com/wellora/css/style.css").toExternalForm());
            scene.getStylesheets().add(WelloraApp.class.getResource("/fxml/dashboard.css").toExternalForm());
            
            // Add theme-specific CSS for dashboard dark mode
            if (isDarkTheme) {
                String darkCss = "/fxml/dashboard-dark.css";
                scene.getStylesheets().add(WelloraApp.class.getResource(darkCss).toExternalForm());
            }
            
            // Apply dark-theme or light-theme class to BorderPane root
            root.getStyleClass().remove("dark-theme");
            root.getStyleClass().remove("light-theme");
            if (isDarkTheme) {
                root.getStyleClass().add("dark-theme");
            } else {
                root.getStyleClass().add("light-theme");
            }
            
            // Apply theme to the content area specifically
            applyThemeToNode(contentArea, isDarkTheme);
        }
    }
    
    /**
     * Apply theme class to a node and all its children
     */
    private void applyThemeToNode(javafx.scene.Node node, boolean isDark) {
        if (node == null) return;
        
        // Remove both theme classes first
        node.getStyleClass().remove("dark-theme");
        node.getStyleClass().remove("light-theme");
        
        // Add the appropriate theme class
        if (isDark) {
            node.getStyleClass().add("dark-theme");
        } else {
            node.getStyleClass().add("light-theme");
        }
        
        // Recursively apply to children
        if (node instanceof javafx.scene.Parent) {
            ((javafx.scene.Parent) node).getChildrenUnmodifiable().forEach(child -> {
                applyThemeToNode(child, isDark);
            });
        }
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

            // Apply current theme to newly loaded content
            applyCurrentTheme();

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
            } else if (controller instanceof CalendarController) {
                ((CalendarController) controller).setMainController(this);
            } else if (controller instanceof PredictionController) {
                ((PredictionController) controller).setMainController(this);
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