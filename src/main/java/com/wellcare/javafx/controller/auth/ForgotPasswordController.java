package com.wellcare.javafx.controller.auth;

import com.wellcare.javafx.service.UserService;
import com.wellcare.javafx.util.SceneManager;
import com.wellcare.javafx.util.ValidationUtils;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

public class ForgotPasswordController {

    @FXML private TextField emailField;
    @FXML private Label errorLabel;
    @FXML private Label successLabel;
    @FXML private HBox loadingBox;
    @FXML private Button sendResetButton;
    @FXML private Hyperlink backToLoginLink;

    private UserService userService;

    @FXML
    public void initialize() {
        try {
            userService = new UserService();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Setup real-time validation
        emailField.textProperty().addListener((obs, oldVal, newVal) -> validateEmail());
    }

    private boolean validateEmail() {
        String email = emailField.getText().trim();
        String error = ValidationUtils.validateEmail(email);
        ValidationUtils.applyValidationStyle(emailField, error == null);
        if (error == null) clearMessages();
        return error == null;
    }

    @FXML
    private void handleSendReset() {
        String email = emailField.getText().trim();
        
        if (!validateEmail()) {
            showError("Please enter a valid email address");
            return;
        }

        setLoading(true);

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                // Request abstract reset. Prevents enumeration.
                userService.initiatePasswordReset(email);
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            setLoading(false);
            // Wait slightly before redirecting
            showSuccess("If an account exists, we have sent a reset link to your email.");
            
            new Thread(() -> {
                try {
                    Thread.sleep(2500);
                    Platform.runLater(() -> SceneManager.getInstance().switchTo(SceneManager.RESET_PASSWORD, email));
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }).start();
        });

        task.setOnFailed(e -> {
            setLoading(false);
            Throwable ex = task.getException();
            if (ex instanceof IllegalStateException) {
                showError(ex.getMessage()); // Rate limiting error
            } else {
                // For security reasons, don't expose DB errors
                showSuccess("If an account exists, we have sent a reset link to your email.");
                new Thread(() -> {
                    try {
                        Thread.sleep(2500);
                        Platform.runLater(() -> SceneManager.getInstance().switchTo(SceneManager.RESET_PASSWORD, email));
                    } catch (InterruptedException ex2) {
                        ex2.printStackTrace();
                    }
                }).start();
            }
        });

        new Thread(task).start();
    }

    @FXML
    private void handleBackToLogin() {
        SceneManager.getInstance().switchTo(SceneManager.LOGIN);
    }

    private void setLoading(boolean loading) {
        loadingBox.setVisible(loading);
        loadingBox.setManaged(loading);
        sendResetButton.setDisable(loading);
        emailField.setDisable(loading);
        backToLoginLink.setDisable(loading);
        if (loading) clearMessages();
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
}
