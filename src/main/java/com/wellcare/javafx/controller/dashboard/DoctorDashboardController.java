package com.wellcare.javafx.controller.dashboard;

import com.wellcare.javafx.model.User;
import com.wellcare.javafx.util.SceneManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import com.wellora.javafx.controller.DashboardInjectedController;

public class DoctorDashboardController implements Initializable {

    @FXML private MenuButton userMenu;
    @FXML private MenuItem profileMenuItem;
    @FXML private MenuItem settingsMenuItem;
    @FXML private MenuItem logoutMenuItem;

    // Navigation buttons
    @FXML private Button dashboardBtn;
    @FXML private Button agendaBtn;
    @FXML private Button pendingBtn;
    @FXML private Button clinicalnotesBtn;

    @FXML private VBox mainContentContainer;

    private User currentUser;
    private Button activeButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Get current user from SceneManager
        currentUser = SceneManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            currentUser = createMockDoctor();
        }

        setupUI();
        setupEventHandlers();
        
        // Load the default dashboard view
        handleDashboard();
    }

    private void setupUI() {
        // Set user menu text
        userMenu.setText("⚕️ Dr. " + currentUser.getFirstName() + " " + currentUser.getLastName());
    }

    private void setupEventHandlers() {
        // User menu actions
        profileMenuItem.setOnAction(e -> handleProfile());
        settingsMenuItem.setOnAction(e -> handleSettings());
        logoutMenuItem.setOnAction(e -> handleLogout());

        // Navigation actions
        dashboardBtn.setOnAction(e -> handleDashboard());
        agendaBtn.setOnAction(e -> handleAgenda());
        pendingBtn.setOnAction(e -> handlePending());
        clinicalnotesBtn.setOnAction(e -> handleClinicalNotes());
    }


    private void updateSidebarActiveState(Button activeButton) {
        // Reset all buttons
        Button[] navButtons = {dashboardBtn, agendaBtn, pendingBtn, clinicalnotesBtn};

        for (Button btn : navButtons) {
            btn.getStyleClass().remove("sidebar-item.active");
        }

        // Set active button
        activeButton.getStyleClass().add("sidebar-item.active");
        this.activeButton = activeButton;
    }

    // ========== NAVIGATION METHODS ==========

    @FXML

    private void handleDashboard() {
        updateSidebarActiveState(dashboardBtn);
        loadView("/fxml/DoctorDashboard.fxml");
    }

    @FXML
    public void handleAgenda() {
        updateSidebarActiveState(agendaBtn);
        loadView("/fxml/doctor-schedule-week.fxml");
    }

    @FXML
    private void handlePending() {
        updateSidebarActiveState(pendingBtn);
        loadView("/fxml/doctor-pending.fxml");
    }

    @FXML
    public void handleClinicalNotes() {
        updateSidebarActiveState(clinicalnotesBtn);
        loadView("/fxml/clinical-notes.fxml");
    }

    @FXML
    private void handleProfile() {
        loadView("/fxml/Profile.fxml");
    }

    @FXML
    private void handleSettings() {
        showAlert("Settings", "Account Settings", "Doctor profile and practice settings will be available soon.");
    }

    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout");
        alert.setHeaderText("Are you sure you want to logout?");
        alert.setContentText("Click OK to logout or Cancel to stay.");

        alert.showAndWait().ifPresent(response -> {
            if (response.getButtonData().isDefaultButton()) {
                SceneManager.getInstance().logout();
            }
        });
    }

    // ========== HELPER METHODS ==========

    public void loadView(String fxmlPath) {
        try {
            System.out.println("🔍 Loading: " + fxmlPath);

            // Vérifier si le fichier existe
            if (getClass().getResource(fxmlPath) == null) {
                System.err.println("❌ FXML not found: " + fxmlPath);
                showAlert("Error", "File not found", "Cannot find: " + fxmlPath);
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();

            System.out.println("✅ View loaded successfully");

            // Remplacer le contenu
            if (mainContentContainer != null) {
                mainContentContainer.getChildren().clear();
                mainContentContainer.getChildren().add(view);
                javafx.scene.layout.VBox.setVgrow(view, javafx.scene.layout.Priority.ALWAYS);
                System.out.println("✅ View added to main container");
                
                // Inject dashboard controller into sub-controller if needed
                Object controller = loader.getController();
                if (controller instanceof DashboardInjectedController) {
                    ((DashboardInjectedController) controller).setDashboardController(this);
                }
            } else {
                System.err.println("❌ Main container is null - cannot add view");
                showAlert("Error", "Navigation Error", "Cannot find content container");
            }

        } catch (IOException e) {
            System.err.println("❌ Error loading: " + fxmlPath);
            e.printStackTrace();
            showAlert("Error", "Cannot load view", "Failed to load: " + fxmlPath + "\n" + e.getMessage());
        }
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Mock data for development
    private User createMockDoctor() {
        User doctor = new User();
        doctor.setUuid("doctor-uuid-123");
        doctor.setEmail("doctor@wellcare.com");
        doctor.setFirstName("Karim");
        doctor.setLastName("Ben Ali");
        doctor.setRole("ROLE_DOCTOR");
        doctor.setSpecialite("General Medicine");
        doctor.setActive(true);
        doctor.setVerifiedByAdmin(true);
        return doctor;
    }
}