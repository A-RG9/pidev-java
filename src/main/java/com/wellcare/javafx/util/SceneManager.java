package com.wellcare.javafx.util;

import com.wellcare.javafx.MainApplication;
import com.wellcare.javafx.model.User;
import com.wellcare.javafx.service.UserService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * SceneManager handles navigation between different screens in the WellCare application.
 * Provides centralized scene switching with dependency injection for services.
 */
public class SceneManager {
    private static SceneManager instance;
    private Stage primaryStage;
    private User currentUser;
    private UserService userService;
    private String pendingMessage;
    
    // Navigation History
    private Stack<String> navigationHistory = new Stack<>();
    private String currentFxmlPath;

    // Cache for loaded FXML to improve performance
    private Map<String, Parent> sceneCache = new HashMap<>();
    private Map<String, Object> controllerCache = new HashMap<>();

    // Scene paths
    public static final String LOGIN = "/fxml/auth/login.fxml";
    public static final String REGISTER_PATIENT = "/fxml/auth/register-patient.fxml";
    
    public static final String VERIFY_EMAIL = "/fxml/auth/verify-email.fxml";
    public static final String FORGOT_PASSWORD = "/fxml/auth/forgot-password.fxml";
    public static final String RESET_PASSWORD = "/fxml/auth/reset-password.fxml";
    public static final String PROFESSIONAL_TYPE_CHOICE = "/fxml/auth/ProfessionalTypeChoiceView.fxml";
    public static final String PROFESSIONAL_REGISTRATION = "/fxml/auth/ProfessionalRegistrationView.fxml";
    public static final String PROFILE = "/fxml/profile.fxml";
    public static final String ADMIN_USERS = "/fxml/admin/admin-users.fxml";
    public static final String ADMIN_PROFESSIONALS = "/fxml/admin/professional-management.fxml";
    public static final String ADMIN_VERIFICATION = "/fxml/admin/verification-queue.fxml";
    public static final String PATIENT_DASHBOARD = "/fxml/dashboard/patient-dashboard.fxml";
    public static final String DOCTOR_DASHBOARD = "/fxml/dashboard/doctor-dashboard.fxml";
    public static final String COACH_DASHBOARD = "/fxml/dashboard/coach-dashboard.fxml";
    public static final String NUTRITIONIST_DASHBOARD = "/fxml/dashboard/nutritionist-dashboard.fxml";
    public static final String ADMIN_DASHBOARD = "/fxml/dashboard/admin-dashboard.fxml";

    private SceneManager() {}

    public static SceneManager getInstance() {
        if (instance == null) {
            instance = new SceneManager();
        }
        return instance;
    }

    public void initialize(Stage stage, UserService userService) {
        this.primaryStage = stage;
        this.userService = userService;

        // Set minimum window size
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);

        // Set window title
        primaryStage.setTitle("WellCare - Healthcare Management System");
    }

    /**
     * Switch to a new scene with FXML path
     */
    public void switchTo(String fxmlPath) {
        if (currentFxmlPath != null && !currentFxmlPath.equals(fxmlPath)) {
            navigationHistory.push(currentFxmlPath);
        }
        this.currentFxmlPath = fxmlPath;
        
        if (fxmlPath.equals(LOGIN)) {
            navigationHistory.clear();
        }
        try {
            Parent root;
            boolean shouldCache = !fxmlPath.contains("/admin/");

            if (shouldCache && sceneCache.containsKey(fxmlPath)) {
                root = sceneCache.get(fxmlPath);
            } else {
                // Load FXML
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                root = loader.load();

                // Inject dependencies
                Object controller = loader.getController();
                injectDependencies(controller);

                // Cache the scene only if it's not an admin view
                if (shouldCache) {
                    sceneCache.put(fxmlPath, root);
                    controllerCache.put(fxmlPath, controller);
                }
            }

            // Create or update scene
            if (primaryStage.getScene() == null) {
                Scene scene = new Scene(root, 1000, 700);
                scene.getStylesheets().add(getClass().getResource("/css/wellcare.css").toExternalForm());
                primaryStage.setScene(scene);
            } else {
                primaryStage.getScene().setRoot(root);
            }

            primaryStage.show();

        } catch (IOException e) {
            System.err.println("Error loading FXML: " + fxmlPath);
            e.printStackTrace();
        }
    }

    /**
     * Switch to a new scene with FXML path and pass state to its controller
     */
    public void switchTo(String fxmlPath, Object state) {
        if (currentFxmlPath != null && !currentFxmlPath.equals(fxmlPath)) {
            navigationHistory.push(currentFxmlPath);
        }
        this.currentFxmlPath = fxmlPath;

        try {
            Parent root;
            boolean shouldCache = !fxmlPath.contains("/admin/");
            FXMLLoader loader;

            if (shouldCache && sceneCache.containsKey(fxmlPath)) {
                root = sceneCache.get(fxmlPath);
                // We cached the Node, but we need the controller. 
                // Using controllerCache to retrieve the controller.
                Object controller = controllerCache.get(fxmlPath);
                if (controller instanceof StatefulController) {
                    ((StatefulController) controller).setControllerState(state);
                }
            } else {
                loader = new FXMLLoader(getClass().getResource(fxmlPath));
                root = loader.load();
                Object controller = loader.getController();
                injectDependencies(controller);
                
                if (controller instanceof StatefulController) {
                    ((StatefulController) controller).setControllerState(state);
                }

                if (shouldCache) {
                    sceneCache.put(fxmlPath, root);
                    controllerCache.put(fxmlPath, controller);
                }
            }

            if (primaryStage.getScene() == null) {
                Scene scene = new Scene(root, 1000, 700);
                scene.getStylesheets().add(getClass().getResource("/css/wellcare.css").toExternalForm());
                primaryStage.setScene(scene);
            } else {
                primaryStage.getScene().setRoot(root);
            }

            primaryStage.show();

        } catch (IOException e) {
            System.err.println("Error loading FXML with state: " + fxmlPath);
            e.printStackTrace();
        }
    }

    /**
     * Inject dependencies into controller
     */
    private void injectDependencies(Object controller) {
        if (controller instanceof ServiceAware) {
            ((ServiceAware) controller).setUserService(userService);
        }
        if (controller instanceof UserAware) {
            ((UserAware) controller).setCurrentUser(currentUser);
        }
    }

    /**
     * Switch to appropriate dashboard based on user role
     */
    public void switchToDashboard() {
        if (currentUser == null) {
            switchTo(LOGIN);
            return;
        }

        String role = currentUser.getRole();

        // Check if user is a professional and not verified by admin
        if (isProfessionalRole(role) && !currentUser.isVerifiedByAdmin()) {
            // Professional not verified - redirect to login with message
            pendingMessage = "Your account is pending admin verification. You will receive an email notification once your account is activated.";
            switchTo(LOGIN);
            return;
        }

        switch (role) {
            case "ROLE_PATIENT":
                switchTo(PATIENT_DASHBOARD);
                break;
            case "ROLE_MEDECIN":
                switchTo(DOCTOR_DASHBOARD);
                break;
            case "ROLE_COACH":
                switchTo(COACH_DASHBOARD);
                break;
            case "ROLE_NUTRITIONIST":
                switchTo(NUTRITIONIST_DASHBOARD);
                break;
            case "ROLE_ADMIN":
                switchTo(ADMIN_DASHBOARD);
                break;
            default:
                switchTo(LOGIN);
                break;
        }
    }

    /**
     * Check if the role is a professional role that requires admin verification
     */
    private boolean isProfessionalRole(String role) {
        return "ROLE_MEDECIN".equals(role) ||
               "ROLE_COACH".equals(role) ||
               "ROLE_NUTRITIONIST".equals(role);
    }

    /**
     * Get pending message and clear it
     */
    public String getAndClearPendingMessage() {
        String message = pendingMessage;
        pendingMessage = null;
        return message;
    }

    /**
     * Show alert for verified professionals whose dashboards aren't implemented yet
     */
    private void showProfessionalDashboardAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Professional Dashboard");
        alert.setHeaderText("Welcome to WellCare Connect!");
        alert.setContentText("Your professional account has been verified and is ready for use.\n\n"
                          + "Professional dashboards (Doctor, Coach, Nutritionist) are currently under development.\n"
                          + "You will receive access once they are completed.\n\n"
                          + "Please contact your administrator for any assistance.");
        alert.showAndWait();
    }

    /**
     * Login user and navigate to dashboard
     */
    public void login(User user) {
        this.currentUser = user;
        switchToDashboard();
    }

    /**
     * Switch to professional type choice screen
     */
    public void switchToProfessionalTypeChoice() {
        switchTo(PROFESSIONAL_TYPE_CHOICE);
    }

    /**
     * Switch to professional registration screen with selected type
     */
    public void switchToProfessionalRegistration(String professionalType) {
        try {
            // Always load fresh for professional registration to handle type parameter
            FXMLLoader loader = new FXMLLoader(getClass().getResource(PROFESSIONAL_REGISTRATION));
            Parent root = loader.load();

            // Inject dependencies if controller supports it
            Object controller = loader.getController();
            if (controller instanceof ServiceAware) {
                ((ServiceAware) controller).setUserService(userService);
            }
            if (controller instanceof UserAware) {
                ((UserAware) controller).setCurrentUser(currentUser);
            }
            if (controller instanceof ProfessionalTypeAware) {
                ((ProfessionalTypeAware) controller).setProfessionalType(professionalType);
            }

            // Create or update scene
            if (primaryStage.getScene() == null) {
                Scene scene = new Scene(root, 1000, 700);
                scene.getStylesheets().add(getClass().getResource("/css/wellcare.css").toExternalForm());
                primaryStage.setScene(scene);
            } else {
                primaryStage.getScene().setRoot(root);
            }

            primaryStage.show();

        } catch (IOException e) {
            System.err.println("Error loading professional registration FXML: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Switch to login screen
     */
    public void switchToLogin() {
        switchTo(LOGIN);
    }

    /**
     * Logout user and return to login
     */
    public void logout() {
        this.currentUser = null;
        this.sceneCache.clear();
        this.controllerCache.clear();
        this.navigationHistory.clear();
        this.currentFxmlPath = LOGIN;
        switchTo(LOGIN);
    }

    /**
     * Switches to the previously viewed scene.
     */
    public void switchToPrevious() {
        if (!navigationHistory.isEmpty()) {
            String previous = navigationHistory.pop();
            // Use internal switchTo without pushing to history to avoid loops
            internalSwitchTo(previous);
        } else {
            switchToDashboard();
        }
    }

    private void internalSwitchTo(String fxmlPath) {
        try {
            Parent root = sceneCache.get(fxmlPath);
            if (root == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                root = loader.load();
                Object controller = loader.getController();
                
                if (controller instanceof ServiceAware) {
                    ((ServiceAware) controller).setUserService(userService);
                }
                if (controller instanceof UserAware) {
                    ((UserAware) controller).setCurrentUser(currentUser);
                }
                
                sceneCache.put(fxmlPath, root);
                controllerCache.put(fxmlPath, controller);
            }
            
            currentFxmlPath = fxmlPath;
            primaryStage.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
            switchToDashboard();
        }
    }

    // Getters and setters
    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public UserService getUserService() {
        return userService;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * Interface for controllers that need UserService
     */
    public interface ServiceAware {
        void setUserService(UserService userService);
    }

    /**
     * Interface for controllers that need current user
     */
    public interface UserAware {
        void setCurrentUser(User user);
    }

    /**
     * Interface for controllers that need professional type
     */
    public interface ProfessionalTypeAware {
        void setProfessionalType(String professionalType);
    }

    /**
     * Interface for controllers that receive state information during transition
     */
    public interface StatefulController {
        void setControllerState(Object state);
    }
}
