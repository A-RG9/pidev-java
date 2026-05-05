package com.wellcare.javafx.controller.dashboard;

import com.wellcare.javafx.model.User;
import com.wellcare.javafx.service.UserService;
import com.wellcare.javafx.util.SceneManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Alert;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

/**
 * Controller for the Coach Dashboard
 * Displays coach-specific information and client training management
 */
public class CoachDashboardController implements Initializable {

    @FXML private MenuButton userMenu;
    @FXML private MenuItem profileMenuItem;
    @FXML private MenuItem settingsMenuItem;
    @FXML private MenuItem logoutMenuItem;

    // Navigation buttons
    @FXML private Button dashboardBtn;
    @FXML private Button clientsBtn;
    @FXML private Button sessionsBtn;
    @FXML private Button programsBtn;
    @FXML private Button progressBtn;
    @FXML private Button messagesBtn;
    @FXML private Button scheduleBtn;

    // Dashboard content
    @FXML private Label welcomeLabel;
    @FXML private Label currentDateLabel;
    @FXML private Label todaySessionsCount;
    @FXML private Label activeClientsCount;
    @FXML private Label pendingReviewsCount;

    @FXML private ListView<String> todaySessionsList;
    @FXML private ListView<String> recentProgressList;

    private UserService userService;
    private User currentUser;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            userService = new UserService();
        } catch (java.sql.SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Database Connection Failed");
            alert.setHeaderText("Unable to connect to database");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
            return;
        }

        // Get current user from SceneManager
        currentUser = SceneManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            // Fallback to mock user for development
            currentUser = createMockCoach();
        }

        setupUI();
        setupEventHandlers();
        loadDashboardData();
    }

    private void setupUI() {
        // Set current date
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");
        currentDateLabel.setText(today.format(formatter));

        // Set welcome message
        welcomeLabel.setText("Good morning, Coach " + currentUser.getLastName() + "!");

        // Set user menu text
        userMenu.setText("🏃 Coach " + currentUser.getFirstName() + " " + currentUser.getLastName());
    }

    private void setupEventHandlers() {
        // User menu actions
        profileMenuItem.setOnAction(e -> handleProfile());
        settingsMenuItem.setOnAction(e -> handleSettings());
        logoutMenuItem.setOnAction(e -> handleLogout());

        // Navigation actions
        dashboardBtn.setOnAction(e -> handleDashboard());
        clientsBtn.setOnAction(e -> handleClients());
        sessionsBtn.setOnAction(e -> handleSessions());
        programsBtn.setOnAction(e -> handlePrograms());
        progressBtn.setOnAction(e -> handleProgress());
        messagesBtn.setOnAction(e -> handleMessages());
        scheduleBtn.setOnAction(e -> handleSchedule());

        // Update sidebar active state
        updateSidebarActiveState(dashboardBtn);
    }

    private void loadDashboardData() {
        // Load mock data for demonstration
        loadTodaySessions();
        loadRecentProgress();
        loadStatistics();
    }

    private void loadTodaySessions() {
        ObservableList<String> sessions = FXCollections.observableArrayList(
            "🕐 08:00 AM - Sarah Johnson (HIIT Training)",
            "🕐 09:00 AM - Mike Chen (Strength Training)",
            "🕐 10:30 AM - Emma Wilson (Yoga Session)",
            "🕐 14:00 PM - David Brown (Cardio Workout)",
            "🕐 15:30 PM - Lisa Garcia (Personal Training)",
            "🕐 17:00 PM - Tom Anderson (Group Class)"
        );
        todaySessionsList.setItems(sessions);
    }

    private void loadRecentProgress() {
        ObservableList<String> progress = FXCollections.observableArrayList(
            "📈 Sarah Johnson: Lost 2kg this month - Goal achieved!",
            "💪 Mike Chen: Increased bench press by 15kg",
            "🧘 Emma Wilson: Improved flexibility - 30% increase",
            "❤️ David Brown: Improved cardio endurance",
            "🏆 Lisa Garcia: Completed 10-week program",
            "🎯 Tom Anderson: Met all fitness goals",
            "📊 Group Class: 85% attendance rate this month",
            "⭐ New client: Maria Rodriguez joined program"
        );
        recentProgressList.setItems(progress);
    }

    private void loadStatistics() {
        todaySessionsCount.setText("6");
        activeClientsCount.setText("23");
        pendingReviewsCount.setText("4");
    }

    private void updateSidebarActiveState(Button activeButton) {
        // Reset all buttons
        Button[] navButtons = {dashboardBtn, clientsBtn, sessionsBtn, programsBtn,
                               progressBtn, messagesBtn, scheduleBtn};

        for (Button btn : navButtons) {
            btn.getStyleClass().remove("sidebar-item.active");
        }

        // Set active button
        activeButton.getStyleClass().add("sidebar-item.active");
    }

    // Event handlers
    @FXML
    private void handleProfile() {
        SceneManager.getInstance().switchTo(SceneManager.PROFILE);
    }

    @FXML
    private void handleSettings() {
        showAlert("Settings", "Account Settings", "Training practice and personal settings will be available in the next version.");
    }

    @FXML
    private void handleLogout() {
        // Navigate back to login
        SceneManager.getInstance().logout();
    }

    @FXML
    private void handleDashboard() {
        updateSidebarActiveState(dashboardBtn);
        // Already on dashboard
    }

    @FXML
    private void handleClients() {
        updateSidebarActiveState(clientsBtn);
        showAlert("Clients", "Client Roster", "Full client profiles and management tools are currently in development.");
    }

    @FXML
    private void handleSessions() {
        updateSidebarActiveState(sessionsBtn);
        showAlert("Sessions", "Training Sessions", "Session planning and logging tools are coming soon.");
    }

    @FXML
    private void handlePrograms() {
        updateSidebarActiveState(programsBtn);
        showAlert("Programs", "Training Programs", "Custom training program builder is being finalized.");
    }

    @FXML
    private void handleProgress() {
        updateSidebarActiveState(progressBtn);
        showAlert("Progress", "Progress Tracking", "Client measurement and performance tracking are coming soon.");
    }

    @FXML
    private void handleMessages() {
        updateSidebarActiveState(messagesBtn);
        showAlert("Messages", "Message Center", "Secure messaging with your clients is currently being implemented.");
    }

    @FXML
    private void handleSchedule() {
        updateSidebarActiveState(scheduleBtn);
        showAlert("Schedule", "Working Schedule", "Full availability management and calendar sync are coming soon.");
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Mock data for development
    private User createMockCoach() {
        User coach = new User();
        coach.setUuid("coach-uuid-456");
        coach.setEmail("coach@wellcare.com");
        coach.setFirstName("Karim");
        coach.setLastName("Ben Ali");
        coach.setRole("ROLE_COACH");
        coach.setSpecialite("Fitness Coaching");
        coach.setLicenseNumber("COACH-67890");
        coach.setActive(true);
        coach.setVerifiedByAdmin(true);
        return coach;
    }
}