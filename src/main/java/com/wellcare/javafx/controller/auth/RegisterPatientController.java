package com.wellcare.javafx.controller.auth;

import com.wellcare.javafx.service.CaptchaService;
import com.wellcare.javafx.model.User;
import com.wellcare.javafx.service.UserService;
import com.wellcare.javafx.util.SceneManager;
import com.wellcare.javafx.util.ValidationUtils;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

/**
 * Controller for Patient Registration
 * Handles patient account creation with validation
 */
public class RegisterPatientController implements SceneManager.ServiceAware {

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private DatePicker birthDatePicker;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private CheckBox termsCheckBox;
    @FXML private Button registerButton;
    @FXML private Label errorLabel;
    @FXML private Label successLabel;
    @FXML private Label firstNameErrorLabel;
    @FXML private Label lastNameErrorLabel;
    @FXML private Label emailErrorLabel;
    @FXML private Label phoneErrorLabel;
    @FXML private Label birthDateErrorLabel;
    @FXML private Label passwordErrorLabel;
    @FXML private Label confirmPasswordErrorLabel;
    @FXML private Label termsErrorLabel;
    @FXML private HBox loadingBox;
    @FXML private Hyperlink loginLink;
    @FXML private Hyperlink registerProfessionalLink;

    // Captcha components
    @FXML private ImageView captchaImageView;
    @FXML private Button refreshCaptchaButton;
    @FXML private TextField captchaField;
    @FXML private Label captchaErrorLabel;

    private UserService userService;
    private CaptchaService captchaService;
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

        // Set up event handlers
        setupEventHandlers();

        // Initialize Captcha
        refreshCaptcha();

        // Focus on first name field
        Platform.runLater(() -> firstNameField.requestFocus());
    }

    private void setupEventHandlers() {
        // Navigation links
        loginLink.setOnAction(e -> handleLogin());
        registerProfessionalLink.setOnAction(e -> handleRegisterProfessional());

        // Register button
        registerButton.setOnAction(e -> handleRegister());

        // Captcha refresh
        refreshCaptchaButton.setOnAction(e -> refreshCaptcha());

        // Real-time validation
        setupValidationListeners();
    }

    private void setupValidationListeners() {
        firstNameField.textProperty().addListener((obs, oldVal, newVal) -> validateFirstName());
        lastNameField.textProperty().addListener((obs, oldVal, newVal) -> validateLastName());
        emailField.textProperty().addListener((obs, oldVal, newVal) -> validateEmail());
        phoneField.textProperty().addListener((obs, oldVal, newVal) -> validatePhone());
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> validatePassword());
        confirmPasswordField.textProperty().addListener((obs, oldVal, newVal) -> validateConfirmPassword());
        termsCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> validateTerms());

        // Captcha real-time validation
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

    @FXML
    private void handleRegister() {
        // Clear previous messages
        clearMessages();

        // Validate all fields
        if (!validateAllFields()) {
            return;
        }

        // Show loading state
        setLoading(true);

        // Perform registration in background thread
        Task<User> registerTask = new Task<User>() {
            @Override
            protected User call() throws Exception {
                // Create user object
                User user = new User();
                user.setFirstName(firstNameField.getText().trim());
                user.setLastName(lastNameField.getText().trim());
                user.setEmail(emailField.getText().trim());
                user.setPhone(phoneField.getText().trim());
                user.setBirthdate(birthDatePicker.getValue());
                user.setRole("ROLE_PATIENT");

                // Register user
                return userService.registerPatient(user, passwordField.getText());
            }

            @Override
            protected void succeeded() {
                User user = getValue();
                showSuccess("Account created successfully! Welcome " + user.getFirstName() + "!");

                // Redirect to verify email after short delay
                new Thread(() -> {
                    try {
                        Thread.sleep(2000);
                        Platform.runLater(() -> sceneManager.switchTo(SceneManager.VERIFY_EMAIL, user.getEmail()));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }).start();

                setLoading(false);
            }

            @Override
            protected void failed() {
                Throwable exception = getException();
                showError(getFriendlyMessage(exception));
                setLoading(false);
                refreshCaptcha();
            }
        };

        // Start the registration task
        Thread registerThread = new Thread(registerTask);
        registerThread.setDaemon(true);
        registerThread.start();
    }

    @FXML
    private void handleLogin() {
        sceneManager.switchTo(SceneManager.LOGIN);
    }

    @FXML
    private void handleRegisterProfessional() {
        sceneManager.switchTo(SceneManager.PROFESSIONAL_TYPE_CHOICE);
    }

    // Validation methods
    private boolean validateAllFields() {
        boolean firstNameValid = validateFirstName();
        boolean lastNameValid = validateLastName();
        boolean emailValid = validateEmail();
        boolean phoneValid = validatePhone();
        boolean birthDateValid = validateBirthDate();
        boolean passwordValid = validatePassword();
        boolean confirmPasswordValid = validateConfirmPassword();
        boolean termsValid = validateTerms();
        
        // Final Captcha check
        if (!isCaptchaValid) {
            showCaptchaError("Please complete the security verification correctly");
        }

        return firstNameValid && lastNameValid && emailValid && phoneValid &&
               birthDateValid && passwordValid && confirmPasswordValid && termsValid && isCaptchaValid;
    }

    private boolean validateFirstName() {
        String firstName = firstNameField.getText().trim();
        String error = ValidationUtils.validateName(firstName, "First name");
        showFieldError(firstNameErrorLabel, error);
        ValidationUtils.applyValidationStyle(firstNameField, error == null);
        return error == null;
    }

    private boolean validateLastName() {
        String lastName = lastNameField.getText().trim();
        String error = ValidationUtils.validateName(lastName, "Last name");
        showFieldError(lastNameErrorLabel, error);
        ValidationUtils.applyValidationStyle(lastNameField, error == null);
        return error == null;
    }

    private boolean validateEmail() {
        String email = emailField.getText().trim();
        String error = ValidationUtils.validateEmail(email);
        showFieldError(emailErrorLabel, error);
        ValidationUtils.applyValidationStyle(emailField, error == null);
        return error == null;
    }

    private boolean validatePhone() {
        String phone = phoneField.getText().trim();
        String error = phone.isEmpty() ? null : ValidationUtils.validatePhone(phone);
        showFieldError(phoneErrorLabel, error);
        ValidationUtils.applyValidationStyle(phoneField, error == null || phone.isEmpty());
        return error == null;
    }

    private boolean validateBirthDate() {
        if (birthDatePicker.getValue() == null) {
            showFieldError(birthDateErrorLabel, "Date of birth is required");
            ValidationUtils.applyValidationStyle(birthDatePicker, false);
            return false;
        }
        showFieldError(birthDateErrorLabel, null);
        ValidationUtils.applyValidationStyle(birthDatePicker, true);
        return true;
    }

    private boolean validatePassword() {
        String password = passwordField.getText();
        String error = ValidationUtils.validatePassword(password);
        showFieldError(passwordErrorLabel, error);
        ValidationUtils.applyValidationStyle(passwordField, error == null);
        return error == null;
    }

    private boolean validateConfirmPassword() {
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String error = ValidationUtils.validateConfirmPassword(password, confirmPassword);
        showFieldError(confirmPasswordErrorLabel, error);
        ValidationUtils.applyValidationStyle(confirmPasswordField, error == null);
        return error == null;
    }

    private boolean validateTerms() {
        if (!termsCheckBox.isSelected()) {
            showFieldError(termsErrorLabel, "You must accept the terms and conditions");
            return false;
        }
        showFieldError(termsErrorLabel, null);
        return true;
    }

    private void showFieldError(Label errorLabel, String error) {
        if (error != null) {
            errorLabel.setText(error);
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        } else {
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
        }
    }

    private String getFriendlyMessage(Throwable e) {
        if (e == null) return "Une erreur inattendue s'est produite.";
        String msg = e.getMessage();
        if (msg == null) return "Une erreur inattendue s'est produite.";
        if (msg.contains("Duplicate entry") || msg.contains("UNIQ")) {
            return "\u26a0\ufe0f Cette adresse email est d\u00e9j\u00e0 utilis\u00e9e. Veuillez vous connecter ou utiliser une autre adresse.";
        }
        if (msg.contains("Communications link failure") || msg.contains("Connection refused")) {
            return "\u26a0\ufe0f Impossible de contacter le serveur. V\u00e9rifiez que WAMP/XAMPP est d\u00e9marr\u00e9.";
        }
        // IllegalArgumentException messages are already user-friendly (from our service layer)
        if (e instanceof IllegalArgumentException) return msg;
        return "\u26a0\ufe0f Une erreur s'est produite. Veuillez r\u00e9essayer.";
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
        successLabel.setVisible(false);
        successLabel.setManaged(false);
    }

    private void showSuccess(String message) {
        successLabel.setText(message);
        successLabel.setVisible(true);
        successLabel.setManaged(true);
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    private void clearMessages() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        successLabel.setVisible(false);
        successLabel.setManaged(false);
    }

    private void setLoading(boolean loading) {
        registerButton.setDisable(loading);
        loadingBox.setVisible(loading);
        loadingBox.setManaged(loading);
        if (loading) {
            clearMessages();
        }
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