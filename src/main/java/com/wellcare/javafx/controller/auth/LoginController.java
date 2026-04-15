package com.wellcare.javafx.controller.auth;

import com.wellcare.javafx.model.User;
import com.wellcare.javafx.service.UserService;
import com.wellcare.javafx.util.SceneManager;
import com.wellcare.javafx.util.ValidationUtils;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;

/**
 * Controller for the Login page
 * Handles user authentication and navigation to appropriate dashboards
 */
public class LoginController implements SceneManager.ServiceAware {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private CheckBox rememberMeCheckBox;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;
    @FXML private Label emailErrorLabel;
    @FXML private Label passwordErrorLabel;
    @FXML private HBox loadingBox;
    @FXML private Hyperlink forgotPasswordLink;
    @FXML private Hyperlink registerPatientLink;
    @FXML private Hyperlink registerProfessionalLink;

    private UserService userService;
    private SceneManager sceneManager;

    @Override
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @FXML
    public void initialize() {
        sceneManager = SceneManager.getInstance();

        // Set up event handlers
        setupEventHandlers();

        // Check for pending messages (e.g., from failed dashboard navigation)
        String pendingMessage = sceneManager.getAndClearPendingMessage();
        if (pendingMessage != null) {
            showError(pendingMessage);
        }

        // Focus on email field
        Platform.runLater(() -> emailField.requestFocus());

        // Real-time validation listeners
        emailField.textProperty().addListener((obs, oldVal, newVal) -> validateEmail());
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> validatePassword());
    }

    private void setupEventHandlers() {
        // Login on Enter key
        emailField.setOnKeyPressed(this::handleKeyPress);
        passwordField.setOnKeyPressed(this::handleKeyPress);

        // Login button action
        loginButton.setOnAction(e -> handleLogin());

        // Navigation links
        forgotPasswordLink.setOnAction(e -> handleForgotPassword());
        registerPatientLink.setOnAction(e -> handleRegisterPatient());
        registerProfessionalLink.setOnAction(e -> handleRegisterProfessional());
    }

    private void handleKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            handleLogin();
        }
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        // Validate input
        String validationError = ValidationUtils.validateLogin(email, password);
        if (validationError != null) {
            showError(validationError);
            return;
        }

        // Show loading state
        setLoading(true);

        // Perform login in background thread
        Task<User> loginTask = new Task<User>() {
            @Override
            protected User call() throws Exception {
                return userService.authenticate(email, password);
            }

            @Override
            protected void succeeded() {
                User user = getValue();
                if (user != null) {
                    if (!user.isEmailVerified() && !user.getRole().equals("ROLE_ADMIN")) {
                        // Email not verified, redirect to verification screen
                        sceneManager.switchTo(SceneManager.VERIFY_EMAIL, user.getEmail());
                    } else {
                        // Login successful - navigate to appropriate dashboard
                        navigateToDashboard(user);
                    }
                } else {
                    showError("Invalid email or password");
                }
                setLoading(false);
            }

            @Override
            protected void failed() {
                Throwable exception = getException();
                showError("Login failed: " + exception.getMessage());
                setLoading(false);
            }
        };

        // Start the login task
        Thread loginThread = new Thread(loginTask);
        loginThread.setDaemon(true);
        loginThread.start();
    }

    private void navigateToDashboard(User user) {
        sceneManager.login(user);
    }

    @FXML
    private void handleForgotPassword() {
        SceneManager.getInstance().switchTo(SceneManager.FORGOT_PASSWORD);
    }

    @FXML
    private void handleRegisterPatient() {
        sceneManager.switchTo(SceneManager.REGISTER_PATIENT);
    }

    @FXML
    private void handleRegisterProfessional() {
        sceneManager.switchTo(SceneManager.PROFESSIONAL_TYPE_CHOICE);
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private boolean validateEmail() {
        String email = emailField.getText().trim();
        String error = ValidationUtils.validateEmail(email);
        
        emailErrorLabel.setText(error != null ? error : "");
        emailErrorLabel.setVisible(error != null);
        ValidationUtils.applyValidationStyle(emailField, error == null);
        
        return error == null;
    }

    private boolean validatePassword() {
        String password = passwordField.getText();
        boolean isValid = password != null && !password.isEmpty();
        
        passwordErrorLabel.setText(isValid ? "" : "Password is required");
        passwordErrorLabel.setVisible(!isValid);
        ValidationUtils.applyValidationStyle(passwordField, isValid);
        
        return isValid;
    }

    private void setLoading(boolean loading) {
        loginButton.setDisable(loading);
        loadingBox.setVisible(loading);
        loadingBox.setManaged(loading);
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}