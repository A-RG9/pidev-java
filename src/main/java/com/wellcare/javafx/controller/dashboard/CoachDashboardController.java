package com.wellcare.javafx.controller.dashboard;

import com.wellcare.javafx.model.User;
import com.wellcare.javafx.service.UserService;
import com.wellcare.javafx.util.SceneManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.Alert;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
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

    // Content area for dynamic loading
    @FXML private VBox contentArea;

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
    }

    // ========== NOUVELLES MÉTHODES POUR LES BOUTONS FITNESS ==========

    @FXML
    private void loadDailyPlanEditor() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DailyPlanEditor.fxml"));
            Parent view = loader.load();

            // Injecter le contrôleur si nécessaire
            Object controller = loader.getController();
            if (controller != null) {
                try {
                    // Essayer d'appeler setMainController
                    java.lang.reflect.Method method = controller.getClass().getMethod("setMainController", Object.class);
                    method.invoke(controller, this);
                } catch (NoSuchMethodException e) {
                    // La méthode n'existe pas, c'est normal
                    System.out.println("No setMainController method found in " + controller.getClass().getSimpleName());
                } catch (IllegalAccessException | java.lang.reflect.InvocationTargetException e) {
                    System.err.println("Could not invoke setMainController: " + e.getMessage());
                }
            }

            // Remplacer le contenu principal
            replaceMainContent(view);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Cannot load view", "Failed to load Daily Plan Editor: " + e.getMessage());
        }
    }

    @FXML
    private void loadExerciseLibrary() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ExerciseLibrary.fxml"));
            Parent view = loader.load();

            // Injecter le contrôleur si nécessaire
            Object controller = loader.getController();
            if (controller != null) {
                try {
                    java.lang.reflect.Method method = controller.getClass().getMethod("setMainController", Object.class);
                    method.invoke(controller, this);
                } catch (NoSuchMethodException e) {
                    // Ignorer
                } catch (IllegalAccessException | java.lang.reflect.InvocationTargetException e) {
                    System.err.println("Could not invoke setMainController: " + e.getMessage());
                }
            }

            replaceMainContent(view);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Cannot load view", "Failed to load Exercise Library: " + e.getMessage());
        }
    }

    @FXML
    private void loadClientsList() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ClientsList.fxml"));
            Parent view = loader.load();

            // Injecter le contrôleur si nécessaire
            Object controller = loader.getController();
            if (controller != null) {
                try {
                    java.lang.reflect.Method method = controller.getClass().getMethod("setMainController", Object.class);
                    method.invoke(controller, this);
                } catch (NoSuchMethodException e) {
                    // Ignorer
                } catch (IllegalAccessException | java.lang.reflect.InvocationTargetException e) {
                    System.err.println("Could not invoke setMainController: " + e.getMessage());
                }
            }

            replaceMainContent(view);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Cannot load view", "Failed to load Clients List: " + e.getMessage());
        }
    }

    /**
     * Remplace le contenu principal du dashboard
     */
    private void replaceMainContent(Parent newContent) {
        // Chercher le VBox principal dans la scène actuelle
        if (contentArea == null) {
            // Essayer de trouver le contentArea dans la vue parente
            Parent root = dashboardBtn.getScene().getRoot();
            if (root instanceof BorderPane) {
                VBox center = (VBox) ((BorderPane) root).getCenter();
                if (center != null && center.getId() != null && center.getId().equals("contentArea")) {
                    contentArea = center;
                }
            }
        }

        if (contentArea != null) {
            contentArea.getChildren().clear();
            contentArea.getChildren().add(newContent);
        } else {
            // Fallback: remplacer toute la scène
            dashboardBtn.getScene().setRoot(newContent);
        }
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
        // Recharger le dashboard
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CoachDashboard.fxml"));
            Parent view = loader.load();
            replaceMainContent(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleClients() {
        updateSidebarActiveState(clientsBtn);
        loadClientsList();
    }

    @FXML
    private void handleSessions() {
        updateSidebarActiveState(sessionsBtn);
        showAlert("Sessions", "Training Sessions", "Session planning and logging tools are coming soon.");
    }

    @FXML
    private void handlePrograms() {
        updateSidebarActiveState(programsBtn);
        loadDailyPlanEditor();
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