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
 * Controller for the Nutritionist Dashboard
 * Displays nutritionist-specific information and client dietary management
 */
public class NutritionistDashboardController implements Initializable {

    @FXML private MenuButton userMenu;
    @FXML private MenuItem profileMenuItem;
    @FXML private MenuItem settingsMenuItem;
    @FXML private MenuItem logoutMenuItem;

    // Navigation buttons
    @FXML private Button dashboardBtn;
    @FXML private Button clientsBtn;
    @FXML private Button mealPlansBtn;
    @FXML private Button consultationsBtn;
    @FXML private Button progressBtn;
    @FXML private Button messagesBtn;
    @FXML private Button recipesBtn;

    // Dashboard content
    @FXML private Label welcomeLabel;
    @FXML private Label currentDateLabel;
    @FXML private Label todayConsultationsCount;
    @FXML private Label activeClientsCount;
    @FXML private Label mealPlansCreatedCount;

    @FXML private ListView<String> todayConsultationsList;
    @FXML private ListView<String> recentAchievementsList;

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
            currentUser = createMockNutritionist();
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
        welcomeLabel.setText("Good morning, Nutritionist " + currentUser.getLastName() + "!");

        // Set user menu text
        userMenu.setText("🥗 Nutritionist " + currentUser.getFirstName() + " " + currentUser.getLastName());
    }

    private void setupEventHandlers() {
        // User menu actions
        profileMenuItem.setOnAction(e -> handleProfile());
        settingsMenuItem.setOnAction(e -> handleSettings());
        logoutMenuItem.setOnAction(e -> handleLogout());

        // Navigation actions
        dashboardBtn.setOnAction(e -> handleDashboard());
        clientsBtn.setOnAction(e -> handleClients());
        mealPlansBtn.setOnAction(e -> handleMealPlans());
        consultationsBtn.setOnAction(e -> handleConsultations());
        progressBtn.setOnAction(e -> handleProgress());
        messagesBtn.setOnAction(e -> handleMessages());
        recipesBtn.setOnAction(e -> handleRecipes());

        // Update sidebar active state
        updateSidebarActiveState(dashboardBtn);
    }

    private void loadDashboardData() {
        // Load mock data for demonstration
        loadTodayConsultations();
        loadRecentAchievements();
        loadStatistics();
    }

    private void loadTodayConsultations() {
        ObservableList<String> consultations = FXCollections.observableArrayList(
            "🕐 09:00 AM - Ahmed Ben Salah (Weight Management)",
            "🕐 10:00 AM - Fatima Al-Zahra (Diabetes Nutrition)",
            "🕐 11:30 AM - Mohamed Chérif (Sports Nutrition)",
            "🕐 14:00 PM - Amina Boudhraa (Meal Planning Review)",
            "🕐 16:00 PM - Karim Mansouri (Dietary Consultation)"
        );
        todayConsultationsList.setItems(consultations);
    }

    private void loadRecentAchievements() {
        ObservableList<String> achievements = FXCollections.observableArrayList(
            "🏆 Ahmed Ben Salah: Lost 5kg in 6 weeks - Goal achieved!",
            "💪 Fatima Al-Zahra: Stabilized blood sugar levels",
            "🥇 Mohamed Chérif: Improved athletic performance by 20%",
            "🌟 Amina Boudhraa: Completed 8-week meal plan perfectly",
            "📈 Karim Mansouri: Reduced cholesterol by 15%",
            "🎯 New client: Nour El-Houda started weight loss program",
            "⭐ Client feedback: 4.8/5 average satisfaction rating",
            "📊 Created 3 new personalized meal plans this week"
        );
        recentAchievementsList.setItems(achievements);
    }

    private void loadStatistics() {
        todayConsultationsCount.setText("5");
        activeClientsCount.setText("31");
        mealPlansCreatedCount.setText("12");
    }

    private void updateSidebarActiveState(Button activeButton) {
        // Reset all buttons
        Button[] navButtons = {dashboardBtn, clientsBtn, mealPlansBtn, consultationsBtn,
                               progressBtn, messagesBtn, recipesBtn};

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
        showAlert("Settings", "Account Settings", "Nutritional practice and personalized settings will be available in the next version.");
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
        showAlert("Clients", "Client Roster", "Nutritional tracking profiles and client management tools are in development.");
    }

    @FXML
    private void handleMealPlans() {
        updateSidebarActiveState(mealPlansBtn);
        showAlert("Meal Plans", "Nutrition Planning", "Digital meal plan builder and nutrient analysis tools are coming soon.");
    }

    @FXML
    private void handleConsultations() {
        updateSidebarActiveState(consultationsBtn);
        showAlert("Consultations", "Nutrition Consultations", "Session booking and dietary assessment tools are being finalized.");
    }

    @FXML
    private void handleProgress() {
        updateSidebarActiveState(progressBtn);
        showAlert("Progress", "Progress Tracking", "Client biometric and dietary progress tracking are coming soon.");
    }

    @FXML
    private void handleMessages() {
        updateSidebarActiveState(messagesBtn);
        showAlert("Messages", "Message Center", "Secure messaging with your clients is currently being implemented.");
    }

    @FXML
    private void handleRecipes() {
        updateSidebarActiveState(recipesBtn);
        showAlert("Recipes", "Recipe Database", "A comprehensive recipe database and dietary filter tools are coming soon.");
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Mock data for development
    private User createMockNutritionist() {
        User nutritionist = new User();
        nutritionist.setUuid("nutritionist-uuid-789");
        nutritionist.setEmail("nutritionist@wellcare.com");
        nutritionist.setFirstName("Leila");
        nutritionist.setLastName("Trabelsi");
        nutritionist.setRole("ROLE_NUTRITIONIST");
        nutritionist.setSpecialite("Clinical Nutrition");
        nutritionist.setLicenseNumber("NUTRI-11111");
        nutritionist.setActive(true);
        nutritionist.setVerifiedByAdmin(true);
        return nutritionist;
    }
}