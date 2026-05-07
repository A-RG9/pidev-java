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
import javafx.scene.layout.StackPane;
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

    // Dashboard content
    @FXML private Label welcomeLabel;
    @FXML private Label currentDateLabel;
    @FXML private Label todaySessionsCount;
    @FXML private Label activeClientsCount;
    @FXML private Label pendingReviewsCount;

    @FXML private ListView<String> todaySessionsList;
    @FXML private ListView<String> recentProgressList;

    // Content area for dynamic loading
    @FXML private StackPane contentArea;
    @FXML private VBox dashboardContent;

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

        // Import des exercices une seule fois en arrière-plan au démarrage coach
        importExercisesOnce();
    }

    /**
     * Importe les exercices depuis l'API WorkoutX (ou fallback mock) une seule fois.
     * S'exécute en arrière-plan pour ne pas bloquer l'UI.
     */
    private void importExercisesOnce() {
        new Thread(() -> {
            try {
                com.wellcare.javafx.util.WorkoutXService workoutXService = new com.wellcare.javafx.util.WorkoutXService();
                int localCount = workoutXService.getLocalExerciseCount();
                if (localCount == 0) {
                    System.out.println("📥 Première connexion coach: import des exercices...");
                    int imported = workoutXService.importNewExercises();
                    System.out.println("✅ " + imported + " exercices importés au démarrage");
                } else {
                    System.out.println("✅ " + localCount + " exercices déjà en base, pas d'import nécessaire");
                }
            } catch (Exception e) {
                System.err.println("⚠️ Erreur import exercices au démarrage: " + e.getMessage());
            }
        }).start();
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/CoachDashboard.fxml"));
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
        if (contentArea != null) {
            contentArea.getChildren().clear();
            contentArea.getChildren().add(newContent);
        }
    }

    private void loadDashboardData() {
        if (currentUser == null || currentUser.getUuid() == null) return;
        String coachId = currentUser.getUuid();

        loadTodaySessions(coachId);
        loadRecentProgress(coachId);
        loadStatistics(coachId);
    }

    private void loadTodaySessions(String coachId) {
        ObservableList<String> sessions = FXCollections.observableArrayList();
        String sql = "SELECT p.titre, p.duree_min, u.first_name, u.last_name " +
                     "FROM daily_plan p " +
                     "JOIN goal g ON p.goal_id = g.id " +
                     "JOIN users u ON g.patient_id = u.uuid " +
                     "WHERE g.coach_id = ? AND p.date = CURRENT_DATE()";

        try (java.sql.Connection conn = com.wellcare.javafx.util.Database.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, coachId);
            java.sql.ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                String titre = rs.getString("titre");
                int duree = rs.getInt("duree_min");
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");
                sessions.add(String.format("🕐 %d min - %s %s (%s)", duree, firstName, lastName, titre));
            }
            
            if (sessions.isEmpty()) {
                sessions.add("Aucune session prévue aujourd'hui");
            }
            
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
            sessions.add("Erreur lors du chargement des sessions");
        }
        
        todaySessionsList.setItems(sessions);
    }

    private void loadRecentProgress(String coachId) {
        ObservableList<String> progress = FXCollections.observableArrayList();
        String sql = "SELECT g.title, g.progress, u.first_name, u.last_name " +
                     "FROM goal g " +
                     "JOIN users u ON g.patient_id = u.uuid " +
                     "WHERE g.coach_id = ? AND g.progress > 0 " +
                     "ORDER BY g.progress DESC LIMIT 10";

        try (java.sql.Connection conn = com.wellcare.javafx.util.Database.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, coachId);
            java.sql.ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                String title = rs.getString("title");
                int prog = rs.getInt("progress");
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");
                progress.add(String.format("📈 %s %s: %s - %d%% terminé", firstName, lastName, title, prog));
            }
            
            if (progress.isEmpty()) {
                progress.add("Aucune progression récente enregistrée");
            }
            
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
            progress.add("Erreur lors du chargement des progressions");
        }
        
        recentProgressList.setItems(progress);
    }

    private void loadStatistics(String coachId) {
        int todaySessions = 0;
        int activeClients = 0;
        int pendingReviews = 0;

        try (java.sql.Connection conn = com.wellcare.javafx.util.Database.getConnection()) {
            
            // Sessions d'aujourd'hui
            String sqlSessions = "SELECT COUNT(*) as count FROM daily_plan p " +
                                 "JOIN goal g ON p.goal_id = g.id " +
                                 "WHERE g.coach_id = ? AND p.date = CURRENT_DATE()";
            try (java.sql.PreparedStatement pstmt = conn.prepareStatement(sqlSessions)) {
                pstmt.setString(1, coachId);
                java.sql.ResultSet rs = pstmt.executeQuery();
                if (rs.next()) todaySessions = rs.getInt("count");
            }

            // Clients actifs (nombre de patients uniques gérés)
            String sqlClients = "SELECT COUNT(DISTINCT patient_id) as count FROM goal WHERE coach_id = ?";
            try (java.sql.PreparedStatement pstmt = conn.prepareStatement(sqlClients)) {
                pstmt.setString(1, coachId);
                java.sql.ResultSet rs = pstmt.executeQuery();
                if (rs.next()) activeClients = rs.getInt("count");
            }

            // Objectifs en cours (progress < 100)
            String sqlPending = "SELECT COUNT(*) as count FROM goal WHERE coach_id = ? AND progress < 100 AND progress > 0";
            try (java.sql.PreparedStatement pstmt = conn.prepareStatement(sqlPending)) {
                pstmt.setString(1, coachId);
                java.sql.ResultSet rs = pstmt.executeQuery();
                if (rs.next()) pendingReviews = rs.getInt("count");
            }

        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }

        todaySessionsCount.setText(String.valueOf(todaySessions));
        activeClientsCount.setText(String.valueOf(activeClients));
        pendingReviewsCount.setText(String.valueOf(pendingReviews));
    }

    private void updateSidebarActiveState(Button activeButton) {
        // Optionnel: Gérer l'état actif des boutons ici
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
        // Remettre le contenu original du dashboard
        if (contentArea != null && dashboardContent != null) {
            contentArea.getChildren().clear();
            contentArea.getChildren().add(dashboardContent);
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