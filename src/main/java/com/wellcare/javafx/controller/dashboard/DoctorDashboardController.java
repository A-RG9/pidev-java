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
 * Controller for the Doctor Dashboard
 * Displays doctor-specific information and practice management tools
 */
public class DoctorDashboardController implements Initializable {

    @FXML private MenuButton userMenu;
    @FXML private MenuItem profileMenuItem;
    @FXML private MenuItem settingsMenuItem;
    @FXML private MenuItem logoutMenuItem;

    // Navigation buttons
    @FXML private Button dashboardBtn;
    @FXML private Button patientsBtn;
    @FXML private Button appointmentsBtn;
    @FXML private Button recordsBtn;
    @FXML private Button prescriptionsBtn;
    @FXML private Button messagesBtn;
    @FXML private Button scheduleBtn;

    // Dashboard content
    @FXML private Label welcomeLabel;
    @FXML private Label currentDateLabel;
    @FXML private Label todayAppointmentsCount;
    @FXML private Label activePatientsCount;
    @FXML private Label pendingTasksCount;

    @FXML private ListView<String> todayScheduleList;
    @FXML private ListView<String> recentActivityList;

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
            currentUser = createMockDoctor();
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
        String title = currentUser.getSpecialite() != null ? currentUser.getSpecialite() : "Doctor";
        welcomeLabel.setText("Good morning, Dr. " + currentUser.getLastName() + "!");

        // Set user menu text
        userMenu.setText("👨‍⚕️ Dr. " + currentUser.getFirstName() + " " + currentUser.getLastName());
    }

    private void setupEventHandlers() {
        // User menu actions
        profileMenuItem.setOnAction(e -> handleProfile());
        settingsMenuItem.setOnAction(e -> handleSettings());
        logoutMenuItem.setOnAction(e -> handleLogout());

        // Navigation actions
        dashboardBtn.setOnAction(e -> handleDashboard());
        patientsBtn.setOnAction(e -> handlePatients());
        appointmentsBtn.setOnAction(e -> handleAppointments());
        recordsBtn.setOnAction(e -> handleRecords());
        prescriptionsBtn.setOnAction(e -> handlePrescriptions());
        messagesBtn.setOnAction(e -> handleMessages());
        scheduleBtn.setOnAction(e -> handleSchedule());

        // Update sidebar active state
        updateSidebarActiveState(dashboardBtn);
    }

    private void loadDashboardData() {
        // Load mock data for demonstration
        loadTodaySchedule();
        loadRecentActivity();
        loadStatistics();
    }

    private void loadTodaySchedule() {
        ObservableList<String> schedule = FXCollections.observableArrayList(
            "🕐 09:00 AM - John Smith (Cardiology Consultation)",
            "🕐 09:30 AM - Sarah Johnson (Follow-up)",
            "🕐 10:00 AM - Michael Brown (New Patient)",
            "🕐 10:30 AM - Emily Davis (Blood Pressure Check)",
            "🕐 11:00 AM - Robert Wilson (Diabetes Review)",
            "🕐 14:00 PM - Lisa Anderson (Vaccination)",
            "🕐 14:30 PM - David Miller (Physical Exam)",
            "🕐 15:00 PM - Maria Garcia (Results Discussion)"
        );
        todayScheduleList.setItems(schedule);
    }

    private void loadRecentActivity() {
        ObservableList<String> activities = FXCollections.observableArrayList(
            "📋 Completed consultation: John Smith - Normal results",
            "💊 Prescribed medication: Sarah Johnson - Hypertension treatment",
            "📅 Scheduled follow-up: Michael Brown - Next month",
            "💬 Patient message: Emily Davis - Thank you for the care",
            "📊 Updated records: Robert Wilson - Lab results reviewed",
            "👥 New patient registered: Lisa Anderson",
            "📋 Lab results received: David Miller - All normal",
            "💊 Prescription renewed: Maria Garcia - Diabetes medication"
        );
        recentActivityList.setItems(activities);
    }

    private void loadStatistics() {
        todayAppointmentsCount.setText("8");
        activePatientsCount.setText("47");
        pendingTasksCount.setText("3");
    }

    private void updateSidebarActiveState(Button activeButton) {
        // Reset all buttons
        Button[] navButtons = {dashboardBtn, patientsBtn, appointmentsBtn, recordsBtn,
                               prescriptionsBtn, messagesBtn, scheduleBtn};

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
        showAlert("Settings", "Account Settings", "Practice settings and account management will be available in the next version.");
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
    private void handlePatients() {
        updateSidebarActiveState(patientsBtn);
        showAlert("Patients", "Patient Management", "Comprehensive patient records and management tools are in development.");
    }

    @FXML
    private void handleAppointments() {
        updateSidebarActiveState(appointmentsBtn);
        showAlert("Appointments", "Schedule & Appointments", "Appointment scheduling and confirmation tools are coming soon.");
    }

    @FXML
    private void handleRecords() {
        updateSidebarActiveState(recordsBtn);
        showAlert("Medical Records", "Clinical Records", "Digital clinical records and analysis tools are under development.");
    }

    @FXML
    private void handlePrescriptions() {
        updateSidebarActiveState(prescriptionsBtn);
        showAlert("Prescriptions", "E-Prescriptions", "Electronic prescription issuing and pharmacy coordination are coming soon.");
    }

    @FXML
    private void handleMessages() {
        updateSidebarActiveState(messagesBtn);
        showAlert("Messages", "Message Center", "Secure messaging with your patients and colleagues is being finalized.");
    }

    @FXML
    private void handleSchedule() {
        updateSidebarActiveState(scheduleBtn);
        showAlert("Schedule", "Working Schedule", "Full schedule management and availability settings are coming soon.");
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
        doctor.setFirstName("Ahmed");
        doctor.setLastName("Ben Salah");
        doctor.setRole("ROLE_MEDECIN");
        doctor.setSpecialite("Cardiologie");
        doctor.setLicenseNumber("DOC-12345");
        doctor.setActive(true);
        doctor.setVerifiedByAdmin(true);
        return doctor;
    }
}