package com.wellora.javafx.controller;

import com.wellora.javafx.WelloraApp;
import com.wellora.controllers.HealthNavigationProxy;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import com.wellcare.javafx.util.SceneManager;
import com.wellcare.javafx.model.User;

/**
 * MainController - Handles application navigation
 * Uses dynamic content switching (no new windows)
 */
public class MainController implements SceneManager.UserAware {

    private User currentUser;

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

    // User Profile Labels
    @FXML private Label lblUserInitials;
    @FXML private Label lblUserName;
    @FXML private Label lblUserEmail;

    // Content Area - where views are loaded
    @FXML private VBox contentArea;

    // Current active button for styling
    private Button activeButton;

    // Proxy for health sub-module navigation from homepage quick-links
    private HealthNavigationProxy healthProxy;
    
    // Theme tracking - shared across all views
    private static boolean isDarkTheme = false;

    @Override
    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user != null) {
            String firstName = user.getFirstName() != null ? user.getFirstName() : "";
            String lastName = user.getLastName() != null ? user.getLastName() : "";
            String fullName = firstName + " " + lastName;
            
            lblUserName.setText(fullName.trim().isEmpty() ? "Utilisateur" : fullName);
            lblUserEmail.setText(user.getEmail() != null ? user.getEmail() : "");
            
            String initials = "";
            if (!firstName.isEmpty()) initials += firstName.substring(0, 1).toUpperCase();
            if (!lastName.isEmpty()) initials += lastName.substring(0, 1).toUpperCase();
            if (initials.isEmpty()) initials = "U";
            
            lblUserInitials.setText(initials);
        }
    }

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
            
            // Remove the inner sidebar if the view is a BorderPane
            if (view instanceof javafx.scene.layout.BorderPane) {
                ((javafx.scene.layout.BorderPane) view).setLeft(null);
            }
            
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
    public void showProfile() {
        // Reset all buttons' active state
        setActiveButton(null);
        try {
            // Load profile view manually to inject UserService
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/profile.fxml"));
            javafx.scene.Parent view = loader.load();
            com.wellcare.javafx.controller.auth.ProfileController profileCtrl = loader.getController();
            profileCtrl.setUserService(new com.wellcare.javafx.service.UserService());
            profileCtrl.setMainController(this);
            
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
            applyThemeToContentArea();
        } catch (Exception e) {
            System.err.println("❌ Error navigating to Profile: " + e.getMessage());
            e.printStackTrace();
        }
    }

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
        if (contentArea == null) return;
        
        Scene scene = contentArea.getScene();
        if (scene == null) return;
        
        Parent root = scene.getRoot();
        if (root == null) return;
        
        // Clear existing stylesheets
        scene.getStylesheets().clear();
        
        // Add base CSS file: style.css (includes all dashboard and theme styles)
        try {
            scene.getStylesheets().add(WelloraApp.class.getResource("/com/wellora/css/style.css").toExternalForm());
        } catch (Exception e) {
            System.err.println("Could not load style.css: " + e.getMessage());
        }
        
        // Apply theme class to root
        root.getStyleClass().removeAll("dark-theme", "light-theme");
        root.getStyleClass().add(isDarkTheme ? "dark-theme" : "light-theme");
        
        // Update the theme button text if it exists
        if (btnTheme != null) {
            btnTheme.setText(isDarkTheme ? "🌙  Dark Mode" : "☀️  Light Mode");
        }
        
        // Apply theme to content area
        applyThemeToContentArea();
    }
    
    /**
     * Apply theme class only to the root element, not to individual children.
     * The CSS uses descendant selectors like .light-theme .header-card,
     * so the theme class must be on an ancestor, not on the elements themselves.
     */
    private void applyThemeToContentArea() {
        if (contentArea != null) {
            // Remove theme classes from all children of contentArea
            // We only want the theme class on the root, not on individual content nodes
            contentArea.getChildren().forEach(child -> {
                if (child != null) {
                    child.getStyleClass().removeAll("dark-theme", "light-theme");
                }
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
    public void loadView(String fxmlPath) {
        try {
            // Clear current content
            contentArea.getChildren().clear();

            // Load new view - use WelloraApp class for stable classpath reference
            FXMLLoader loader = new FXMLLoader(WelloraApp.class.getResource(fxmlPath));
            Parent view = loader.load();

            // Remove the inner sidebar if the view is a BorderPane
            if (view instanceof javafx.scene.layout.BorderPane) {
                ((javafx.scene.layout.BorderPane) view).setLeft(null);
            }

            // Add to content area
            contentArea.getChildren().add(view);

            // Apply current theme to newly loaded content
            applyCurrentTheme();

            // If the loaded controller has a setMainController method, pass reference
            Object controller = loader.getController();
            if (controller instanceof HomepageController hc) {
                hc.setMainController(this);
                // Inject proxy so homepage quick-links can navigate
                if (healthProxy == null) {
                    healthProxy = new HealthNavigationProxy(contentArea, null);
                }
                hc.setMainControllerProxy(healthProxy);
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
            } else if (controller instanceof AfficherParcoursController) {
                ((AfficherParcoursController) controller).setMainController(this);
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