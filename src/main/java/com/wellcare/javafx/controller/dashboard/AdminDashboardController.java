package com.wellcare.javafx.controller.dashboard;

import com.wellcare.javafx.model.User;
import com.wellcare.javafx.service.UserService;
import com.wellcare.javafx.util.SceneManager;
import com.wellcare.javafx.util.SceneManager.ServiceAware;
import com.wellcare.javafx.util.SceneManager.UserAware;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class AdminDashboardController implements Initializable, ServiceAware, UserAware {

    // Service and user
    private UserService userService;
    private User currentUser;

    // FXML injected fields
    @FXML private MenuButton userMenu;
    @FXML private MenuItem profileMenuItem;
    @FXML private MenuItem settingsMenuItem;
    @FXML private MenuItem logoutMenuItem;

    // Navigation buttons
    @FXML private Button dashboardBtn;
    @FXML private Button usersBtn;
    @FXML private Button professionalsBtn;
    @FXML private Button verificationBtn;
    @FXML private Button reportsBtn;
    @FXML private Button systemBtn;

    // Dashboard content
    @FXML private VBox mainContent;
    @FXML private Label pageTitleLabel;
    @FXML private Label currentDateLabel;

    // Statistics
    @FXML private Label totalUsersLabel;
    @FXML private Label activeProfessionalsLabel;
    @FXML private Label pendingVerificationsLabel;
    @FXML private ProgressBar usersProgress;
    @FXML private ProgressBar professionalsProgress;
    @FXML private ProgressBar verificationsProgress;

    // Charts
    @FXML private LineChart<String, Number> registrationChart;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupEventHandlers();
        setupUserMenu();
        updateCurrentDate();
        loadDashboardData();
        initializeCharts();
    }

    private void setupEventHandlers() {
        // Navigation buttons
        dashboardBtn.setOnAction(e -> showDashboard());
        usersBtn.setOnAction(e -> showUserManagement());
        professionalsBtn.setOnAction(e -> showProfessionals());
        verificationBtn.setOnAction(e -> showVerificationQueue());
        reportsBtn.setOnAction(e -> showReports());
        systemBtn.setOnAction(e -> showSystemSettings());

        // Menu items
        logoutMenuItem.setOnAction(e -> handleLogout());
        profileMenuItem.setOnAction(e -> handleProfile());
        settingsMenuItem.setOnAction(e -> handleSettings());
    }

    private void setupUserMenu() {
        if (currentUser != null) {
            userMenu.setText(currentUser.getFirstName() + " " + currentUser.getLastName());
        }
    }

    private void updateCurrentDate() {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");
        currentDateLabel.setText(today.format(formatter));
    }

    private void loadDashboardData() {
        try {
            // Load statistics (mock data for now - would be real data from service)
            loadStatistics();
        } catch (Exception e) {
            System.err.println("Error loading dashboard data: " + e.getMessage());
            showAlert("Error", "Failed to load dashboard data", e.getMessage());
        }
    }

    private void loadStatistics() {
        // Mock data - in real implementation, this would come from UserService
        totalUsersLabel.setText("1,247");
        activeProfessionalsLabel.setText("94");
        pendingVerificationsLabel.setText("8");

        usersProgress.setProgress(0.78);
        professionalsProgress.setProgress(0.72);
        verificationsProgress.setProgress(0.25);
    }

    private void initializeCharts() {
        // Registration trends chart
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("User Registrations");

        // Mock data for the last 6 months
        series.getData().add(new XYChart.Data<>("Jul", 45));
        series.getData().add(new XYChart.Data<>("Aug", 52));
        series.getData().add(new XYChart.Data<>("Sep", 38));
        series.getData().add(new XYChart.Data<>("Oct", 67));
        series.getData().add(new XYChart.Data<>("Nov", 89));
        series.getData().add(new XYChart.Data<>("Dec", 124));

        registrationChart.getData().add(series);
        registrationChart.setLegendVisible(false);
    }

    private void showDashboard() {
        pageTitleLabel.setText("Admin Dashboard");
        // Dashboard content is already showing
    }

    private void showUserManagement() {
        pageTitleLabel.setText("User Management");
        SceneManager.getInstance().switchTo(SceneManager.ADMIN_USERS);
    }

    private void showProfessionals() {
        SceneManager.getInstance().switchTo(SceneManager.ADMIN_PROFESSIONALS);
    }

    private void showVerificationQueue() {
        SceneManager.getInstance().switchTo(SceneManager.ADMIN_VERIFICATION);
    }

    private void showReports() {
        pageTitleLabel.setText("Reports & Analytics");
        // TODO: Implement reports view
        showAlert("Info", "Reports", "This feature is coming soon!");
    }

    private void showSystemSettings() {
        pageTitleLabel.setText("System Settings");
        // TODO: Implement system settings view
        showAlert("Info", "System Settings", "This feature is coming soon!");
    }

    private void handleLogout() {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Logout Confirmation");
        confirmation.setHeaderText("Are you sure you want to logout?");
        confirmation.setContentText("You will be redirected to the login screen.");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                SceneManager.getInstance().logout();
            }
        });
    }

    private void handleProfile() {
        SceneManager.getInstance().switchTo(SceneManager.PROFILE);
    }

    private void handleSettings() {
        // TODO: Implement settings view
        showAlert("Info", "Settings", "Settings management coming soon!");
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @Override
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (userMenu != null) {
            Platform.runLater(this::setupUserMenu);
        }
    }
}