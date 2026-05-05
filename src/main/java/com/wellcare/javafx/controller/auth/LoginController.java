package com.wellcare.javafx.controller.auth;

import com.wellcare.javafx.service.CaptchaService;
import com.wellcare.javafx.service.GoogleAuthService;
import com.wellcare.javafx.model.User;
import com.wellcare.javafx.service.UserService;
import com.wellcare.javafx.util.ValidationUtils;
import com.wellcare.javafx.util.SceneManager;
import java.util.Map;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

/**
 * Controller for the Login page
 * Handles user authentication and navigation to appropriate dashboards
 */
public class LoginController implements SceneManager.ServiceAware {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private CheckBox rememberMeCheckBox;
    @FXML private Button loginButton;
    @FXML private Button googleLoginButton;
    @FXML private Label errorLabel;
    @FXML private Label emailErrorLabel;
    @FXML private Label passwordErrorLabel;
    @FXML private HBox loadingBox;
    @FXML private Hyperlink forgotPasswordLink;
    @FXML private Hyperlink registerPatientLink;
    @FXML private Hyperlink registerProfessionalLink;

    // Captcha components
    @FXML private ImageView captchaImageView;
    @FXML private Button refreshCaptchaButton;
    @FXML private TextField captchaField;
    @FXML private Label captchaErrorLabel;

    private UserService userService;
    private CaptchaService captchaService;
    private GoogleAuthService googleAuthService;
    private SceneManager sceneManager;
    private boolean isCaptchaValid = false;

    @Override
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @FXML
    public void initialize() {
        sceneManager = SceneManager.getInstance();
        captchaService = new CaptchaService();
        googleAuthService = new GoogleAuthService();

        // Set up event handlers
        setupEventHandlers();

        // Initialize Captcha
        refreshCaptcha();

        // Focus on email field
        Platform.runLater(() -> emailField.requestFocus());

        // Real-time validation listeners
        emailField.textProperty().addListener((obs, oldVal, newVal) -> validateEmail());
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> validatePassword());
        
        // Captcha real-time validation (auto-check at 6 chars as per procedure)
        captchaField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.length() == 6) {
                validateCaptcha();
            } else {
                isCaptchaValid = false;
                captchaErrorLabel.setVisible(false);
                ValidationUtils.applyValidationStyle(captchaField, true);
            }
        });
    }

    private void setupEventHandlers() {
        // Login on Enter key
        emailField.setOnKeyPressed(this::handleKeyPress);
        passwordField.setOnKeyPressed(this::handleKeyPress);
        captchaField.setOnKeyPressed(this::handleKeyPress);

        // Login button action
        loginButton.setOnAction(e -> handleLogin());

        // Captcha refresh
        refreshCaptchaButton.setOnAction(e -> refreshCaptcha());

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

        // Validate Captcha first
        if (!isCaptchaValid) {
            showCaptchaError("Please complete the security verification correctly");
            return;
        }

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
                        // Check for 2FA before dashboard
                        navigateToDashboard(user);
                    }
                } else {
                    showError("Invalid email or password");
                    refreshCaptcha();
                }
                setLoading(false);
            }

            @Override
            protected void failed() {
                Throwable exception = getException();
                showError("Login failed: " + exception.getMessage());
                setLoading(false);
                refreshCaptcha();
            }
        };

        // Start the login task
        Thread loginThread = new Thread(loginTask);
        loginThread.setDaemon(true);
        loginThread.start();
    }

    @FXML
    private void handleGoogleLogin() {
        setLoading(true);
        errorLabel.setVisible(false);

        googleAuthService.login().thenAccept(profile -> {
            Platform.runLater(() -> {
                try {
                    System.out.println("Google Auth Profile Received: " + profile.get("email"));
                    String googleId = profile.get("sub");
                    String email = profile.get("email");
                    String firstName = profile.get("given_name");
                    String lastName = profile.get("family_name");
                    String picture = profile.get("picture");

                    System.out.println("Processing Google Login in UserService...");
                    User user = userService.processGoogleLogin(googleId, email, firstName, lastName, picture);
                    if (user != null) {
                        System.out.println("User processed successfully: " + user.getEmail() + " (2FA enabled: " + user.isTwoFactorEnabled() + ")");
                        navigateToDashboard(user);
                    } else {
                        System.err.println("Google login failed: user is null");
                        showError("Échec de la synchronisation du compte Google.");
                    }
                } catch (Exception e) {
                    System.err.println("Exception during Google Login: " + e.getMessage());
                    e.printStackTrace();
                    showError("Erreur Google Login: " + e.getMessage());
                } finally {
                    setLoading(false);
                }
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> {
                System.err.println("Google Auth failed: " + ex.getMessage());
                showError("Authentification Google annulée ou échouée.");
                setLoading(false);
            });
            return null;
        });
    }

    private void navigateToDashboard(User user) {
        if (user.isTwoFactorEnabled()) {
            sceneManager.switchTo(SceneManager.TWO_FACTOR_VERIFY, user);
        } else {
            sceneManager.login(user);
        }
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

    private void refreshCaptcha() {
        captchaField.clear();
        isCaptchaValid = false;
        captchaErrorLabel.setVisible(false);
        ValidationUtils.applyValidationStyle(captchaField, true);
        
        captchaService.generateCode();
        captchaImageView.setImage(captchaService.generateCaptchaImage(200, 50));
    }

    private void validateCaptcha() {
        String input = captchaField.getText().trim();
        if (captchaService.validate(input)) {
            isCaptchaValid = true;
            captchaErrorLabel.setText("✓ Code correct");
            captchaErrorLabel.setStyle("-fx-text-fill: #28a745;"); // Green
            captchaErrorLabel.setVisible(true);
            captchaErrorLabel.setManaged(true);
            ValidationUtils.applyValidationStyle(captchaField, true);
        } else {
            isCaptchaValid = false;
            captchaErrorLabel.setText("✗ Code incorrect");
            captchaErrorLabel.setStyle("-fx-text-fill: #dc3545;"); // Red
            captchaErrorLabel.setVisible(true);
            captchaErrorLabel.setManaged(true);
            ValidationUtils.applyValidationStyle(captchaField, false);
            
            // Auto-refresh after 1.5s on wrong input as per Step 3
            PauseTransition pause = new PauseTransition(Duration.seconds(1.5));
            pause.setOnFinished(e -> {
                if (!isCaptchaValid) {
                    refreshCaptcha();
                }
            });
            pause.play();
        }
    }

    private void showCaptchaError(String message) {
        captchaErrorLabel.setText(message);
        captchaErrorLabel.setStyle("-fx-text-fill: #dc3545;");
        captchaErrorLabel.setVisible(true);
        captchaErrorLabel.setManaged(true);
    }
}