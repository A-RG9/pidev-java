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

public class DoctorDashboardController implements Initializable {

    @FXML private MenuButton userMenu;
    @FXML private MenuItem profileMenuItem;
    @FXML private MenuItem settingsMenuItem;
    @FXML private MenuItem logoutMenuItem;

    // Navigation buttons
    @FXML private Button dashboardBtn;
    @FXML private Button agendaBtn;
    @FXML private Button clinicalnotesBtn;

    // Dashboard content
    @FXML private Label welcomeLabel;
    @FXML private Label currentDateLabel;
    @FXML private Label todayAppointmentsCount;
    @FXML private Label activePatientsCount;
    @FXML private Label pendingTasksCount;

    @FXML private ListView<String> todayScheduleList;
    @FXML private ListView<String> recentActivityList;

    private User currentUser;
    private Button activeButton;

    // Référence au conteneur principal (sera trouvé dynamiquement)
    private VBox mainContentContainer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Get current user from SceneManager
        currentUser = SceneManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            currentUser = createMockDoctor();
        }

        setupUI();
        setupEventHandlers();
        loadDashboardData();

        // Trouver le conteneur principal après l'initialisation
        findMainContentContainer();
    }

    private void findMainContentContainer() {
        // Chercher le VBox principal qui contient le contenu du dashboard
        Scene scene = dashboardBtn.getScene();
        if (scene != null) {
            // Le VBox principal est le 2ème enfant du HBox principal
            Parent root = scene.getRoot();
            if (root instanceof VBox) {
                VBox mainVBox = (VBox) root;
                for (var child : mainVBox.getChildren()) {
                    if (child instanceof HBox) {
                        HBox mainHBox = (HBox) child;
                        // Le contenu principal est le 2ème enfant du HBox (index 1)
                        if (mainHBox.getChildren().size() > 1) {
                            var contentChild = mainHBox.getChildren().get(1);
                            if (contentChild instanceof VBox) {
                                mainContentContainer = (VBox) contentChild;
                                System.out.println("✅ Main content container found!");
                                return;
                            }
                        }
                    }
                }
            }
        }
        System.err.println("❌ Could not find main content container");
    }

    private void setupUI() {
        // Set current date
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");
        currentDateLabel.setText(today.format(formatter));

        // Set welcome message
        welcomeLabel.setText("Good morning, Dr. " + currentUser.getLastName() + "!");

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
        clinicalnotesBtn.setOnAction(e -> handleClinicalNotes());
    }

    private void loadDashboardData() {
        loadTodaySchedule();
        loadRecentActivity();
        loadStatistics();
    }

    private void loadTodaySchedule() {
        ObservableList<String> schedule = FXCollections.observableArrayList(
                "🕐 09:00 AM - Sarah Johnson (Annual Checkup)",
                "🕐 10:00 AM - Mike Chen (Blood Pressure)",
                "🕐 11:00 AM - Emma Wilson (Consultation)",
                "🕐 02:00 PM - David Brown (Follow-up)",
                "🕐 03:30 PM - Lisa Garcia (Vaccination)",
                "🕐 04:30 PM - Tom Anderson (Test Results)"
        );
        todayScheduleList.setItems(schedule);
    }

    private void loadRecentActivity() {
        ObservableList<String> activity = FXCollections.observableArrayList(
                "📋 Completed patient report - Sarah Johnson",
                "📊 Updated medical records - Mike Chen",
                "💊 Prescription renewed - Emma Wilson",
                "📅 Scheduled follow-up - David Brown",
                "🔬 Lab results reviewed - Lisa Garcia",
                "📝 Clinical notes added - Tom Anderson",
                "🩺 New patient registered - Maria Rodriguez"
        );
        recentActivityList.setItems(activity);
    }

    private void loadStatistics() {
        todayAppointmentsCount.setText("6");
        activePatientsCount.setText("47");
        pendingTasksCount.setText("3");
    }

    private void updateSidebarActiveState(Button activeButton) {
        // Reset all buttons
        Button[] navButtons = {dashboardBtn, agendaBtn, clinicalnotesBtn};

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
    private void handleAgenda() {
        updateSidebarActiveState(agendaBtn);
        loadView("/fxml/doctor-schedule-week.fxml");
    }

    @FXML
    private void handleClinicalNotes() {
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

    private void loadView(String fxmlPath) {
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

            // Trouver le conteneur principal si pas encore trouvé
            if (mainContentContainer == null) {
                findMainContentContainer();
            }

            // Remplacer le contenu
            if (mainContentContainer != null) {
                mainContentContainer.getChildren().clear();
                mainContentContainer.getChildren().add(view);
                System.out.println("✅ View added to main container");
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