package com.wellcare.javafx.controller.auth;

import com.wellcare.javafx.service.UserService;
import com.wellcare.javafx.util.SceneManager;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

/**
 * Controller for the Email Verification screen.
 */
public class VerifyEmailController implements SceneManager.ServiceAware, SceneManager.StatefulController {

    @FXML private Label emailDisplayLabel;
    @FXML private TextField codeField;
    @FXML private Button verifyButton;
    @FXML private Hyperlink resendLink;
    @FXML private Hyperlink backToLoginLink;
    @FXML private Label errorLabel;
    @FXML private Label successLabel;
    @FXML private HBox loadingBox;

    private UserService userService;
    private SceneManager sceneManager;
    private String userEmail;

    @Override
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void setControllerState(Object state) {
        if (state instanceof String) {
            this.userEmail = (String) state;
            if (emailDisplayLabel != null) {
                emailDisplayLabel.setText(userEmail);
            }
        }
    }

    @FXML
    public void initialize() {
        sceneManager = SceneManager.getInstance();

        // Setup Event Handlers
        backToLoginLink.setOnAction(e -> sceneManager.switchTo(SceneManager.LOGIN));
        verifyButton.setOnAction(e -> handleVerification());
        
        // TextFormatter to only allow numbers and max 6 chars
        codeField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                codeField.setText(newValue.replaceAll("[^\\d]", ""));
            }
            if (codeField.getText().length() > 6) {
                String s = codeField.getText().substring(0, 6);
                codeField.setText(s);
            }
        });

        // Trigger verify on Enter key
        codeField.setOnAction(e -> handleVerification());
        
        // Allow resend (but only visually display success/error for now)
        resendLink.setOnAction(e -> handleResendCode());

        Platform.runLater(() -> codeField.requestFocus());
    }

    private void handleVerification() {
        String code = codeField.getText();
        if (code == null || code.length() != 6) {
            showError("Please enter the 6-digit code.");
            return;
        }

        setLoading(true);

        Task<Boolean> verifyTask = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                // Here we call the token verification
                return userService.verifyEmailByToken(code);
            }

            @Override
            protected void succeeded() {
                boolean success = getValue();
                if (success) {
                    showSuccess("Email verified successfully! You can now log in.");
                    new Thread(() -> {
                        try {
                            Thread.sleep(1500);
                            Platform.runLater(() -> sceneManager.switchTo(SceneManager.LOGIN));
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }).start();
                } else {
                    showError("Invalid or expired verification code.");
                }
                setLoading(false);
            }

            @Override
            protected void failed() {
                showError("An error occurred during verification.");
                getException().printStackTrace();
                setLoading(false);
            }
        };

        Thread t = new Thread(verifyTask);
        t.setDaemon(true);
        t.start();
    }

    private void handleResendCode() {
        if (userEmail == null || userEmail.isEmpty()) {
            showError("Cannot resend: user email not found.");
            return;
        }
        
        setLoading(true);
        
        Task<Void> resendTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                // Fetch the user to get their UUID, then generate a new token
                com.wellcare.javafx.model.User user = userService.getUserByEmail(userEmail);
                if (user != null) {
                    String newToken = userService.generateEmailVerificationToken(user.getUuid());
                    com.wellcare.javafx.service.EmailService emailService = new com.wellcare.javafx.service.EmailService();
                    boolean sent = emailService.sendVerificationEmail(userEmail, newToken);
                    if (!sent) {
                        throw new RuntimeException("Failed to dispatch email");
                    }
                } else {
                    throw new RuntimeException("User not found");
                }
                return null;
            }

            @Override
            protected void succeeded() {
                showSuccess("A new code has been sent!");
                setLoading(false);
            }

            @Override
            protected void failed() {
                showError("Failed to resend email. Please try again.");
                setLoading(false);
            }
        };

        Thread t = new Thread(resendTask);
        t.setDaemon(true);
        t.start();
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

    private void setLoading(boolean loading) {
        verifyButton.setDisable(loading);
        loadingBox.setVisible(loading);
        loadingBox.setManaged(loading);
        if (loading) {
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
            successLabel.setVisible(false);
            successLabel.setManaged(false);
        }
    }
}
