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
 * Controller for the Patient Dashboard
 * Displays patient-specific information and navigation
 */
public class PatientDashboardController implements Initializable, SceneManager.UserAware, SceneManager.ServiceAware {

    @FXML private MenuButton userMenu;
    @FXML private MenuItem profileMenuItem;
    @FXML private MenuItem settingsMenuItem;
    @FXML private MenuItem logoutMenuItem;

    // Navigation buttons
    @FXML private Button dashboardBtn;
    @FXML private Button appointmentsBtn;
    @FXML private Button medicalRecordsBtn;
    @FXML private Button prescriptionsBtn;
    @FXML private Button messagesBtn;
    @FXML private Button billingBtn;

    // Dashboard content
    @FXML private Label welcomeLabel;
    @FXML private Label currentDateLabel;
    @FXML private Label upcomingAppointmentsCount;
    @FXML private Label activePrescriptionsCount;
    @FXML private Label unreadMessagesCount;

    @FXML private ListView<String> upcomingAppointmentsList;
    @FXML private ListView<String> recentActivityList;

    private UserService userService;
    private User currentUser;

    @Override
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (welcomeLabel != null) {
            Platform.runLater(this::setupUI);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // If user already set (via dependency injection), setup UI
        if (currentUser != null) {
            Platform.runLater(this::setupUI);
        }
        
        setupEventHandlers();
        loadDashboardData();
    }

    private void setupUI() {
        // Set current date
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");
        currentDateLabel.setText(today.format(formatter));

        // Set welcome message
        welcomeLabel.setText("Welcome back, " + currentUser.getFirstName() + "!");

        // Set user menu text
        userMenu.setText("👤 " + currentUser.getFirstName() + " " + currentUser.getLastName());
    }

    private void setupEventHandlers() {
        // User menu actions
        profileMenuItem.setOnAction(e -> handleProfile());
        settingsMenuItem.setOnAction(e -> handleSettings());
        logoutMenuItem.setOnAction(e -> handleLogout());

        // Navigation actions
        dashboardBtn.setOnAction(e -> handleDashboard());
        appointmentsBtn.setOnAction(e -> handleAppointments());
        medicalRecordsBtn.setOnAction(e -> handleMedicalRecords());
        prescriptionsBtn.setOnAction(e -> handlePrescriptions());
        messagesBtn.setOnAction(e -> handleMessages());
        billingBtn.setOnAction(e -> handleBilling());

        // Update sidebar active state
        updateSidebarActiveState(dashboardBtn);
    }

    private void loadDashboardData() {
        // Load mock data for demonstration
        loadUpcomingAppointments();
        loadRecentActivity();
        loadStatistics();
    }

    private void loadUpcomingAppointments() {
        ObservableList<String> appointments = FXCollections.observableArrayList(
            "📅 Dr. Smith - Cardiology - Tomorrow 10:00 AM",
            "📅 Dr. Johnson - General Checkup - Friday 2:30 PM",
            "📅 Dr. Williams - Follow-up - Next Monday 9:15 AM"
        );
        upcomingAppointmentsList.setItems(appointments);
    }

    private void loadRecentActivity() {
        ObservableList<String> activities = FXCollections.observableArrayList(
            "💊 Prescription renewed: Lisinopril 10mg",
            "📋 Lab results received: Blood work normal",
            "📅 Appointment scheduled: Cardiology follow-up",
            "💬 Message from Dr. Smith: Results look good",
            "📊 Health metrics updated: Blood pressure 120/80"
        );
        recentActivityList.setItems(activities);
    }

    private void loadStatistics() {
        upcomingAppointmentsCount.setText("3");
        activePrescriptionsCount.setText("2");
        unreadMessagesCount.setText("5");
    }

    private void updateSidebarActiveState(Button activeButton) {
        // Reset all buttons
        Button[] navButtons = {dashboardBtn, appointmentsBtn, medicalRecordsBtn,
                              prescriptionsBtn, messagesBtn, billingBtn};

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
        showAlert("Settings", "Account Settings", "Settings and privacy management is coming soon in the next update!");
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
    private void handleAppointments() {
        updateSidebarActiveState(appointmentsBtn);
        showAlert("Appointments", "My Appointments", "Appointment booking and management will be available shortly.");
    }

    @FXML
    private void handleMedicalRecords() {
        updateSidebarActiveState(medicalRecordsBtn);
        showAlert("Medical Records", "My Health Records", "Digital medical records and history tracking are coming soon.");
    }

    @FXML
    private void handlePrescriptions() {
        updateSidebarActiveState(prescriptionsBtn);
        showAlert("Prescriptions", "My Prescriptions", "Prescription management and renewal requests are coming soon.");
    }

    @FXML
    private void handleMessages() {
        updateSidebarActiveState(messagesBtn);
        showAlert("Messages", "Message Center", "Secure messaging with your healthcare providers is in development.");
    }

    @FXML
    private void handleBilling() {
        updateSidebarActiveState(billingBtn);
        showAlert("Billing", "Payments & Billing", "Billing history and online payment features are coming soon.");
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}