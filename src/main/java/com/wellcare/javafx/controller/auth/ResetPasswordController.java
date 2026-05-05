package com.wellcare.javafx.controller.auth;

import com.wellcare.javafx.service.UserService;
import com.wellcare.javafx.util.SceneManager;
import com.wellcare.javafx.util.ValidationUtils;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

public class ResetPasswordController {

    @FXML private TextField tokenField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label errorLabel;
    @FXML private Label successLabel;
    @FXML private HBox loadingBox;
    @FXML private Button resetPasswordButton;
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
        tokenField.textProperty().addListener((obs, oldVal, newVal) -> validateToken());
        newPasswordField.textProperty().addListener((obs, oldVal, newVal) -> validatePasswords());
        confirmPasswordField.textProperty().addListener((obs, oldVal, newVal) -> validatePasswords());
    }

    private void validateToken() {
        boolean isValid = !tokenField.getText().trim().isEmpty();
        ValidationUtils.applyValidationStyle(tokenField, isValid);
    }

    private void validatePasswords() {
        String pass = newPasswordField.getText();
        String confirm = confirmPasswordField.getText();
        
        String passError = ValidationUtils.validatePassword(pass);
        ValidationUtils.applyValidationStyle(newPasswordField, passError == null);
        
        String confirmError = ValidationUtils.validateConfirmPassword(pass, confirm);
        ValidationUtils.applyValidationStyle(confirmPasswordField, confirmError == null);
        
        if (passError == null && confirmError == null) clearMessages();
    }

    @FXML
    private void handleResetPassword() {
        String token = tokenField.getText().trim();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (token.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showError("Please fill in all fields");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showError("Passwords do not match");
            return;
        }

        setLoading(true);

        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                // If token invalid/expired, this could fail natively
                return userService.resetPasswordByToken(token, newPassword);
            }
        };

        task.setOnSucceeded(e -> {
            setLoading(false);
            if (task.getValue()) {
                showSuccess("Password updated successfully! Redirecting...");
                new Thread(() -> {
                    try {
                        Thread.sleep(2500);
                        Platform.runLater(() -> SceneManager.getInstance().switchTo(SceneManager.LOGIN));
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }).start();
            } else {
                showError("The security token is invalid or expired.");
            }
        });

        task.setOnFailed(e -> {
            setLoading(false);
            Throwable ex = task.getException();
            if (ex instanceof IllegalArgumentException) {
                showError(ex.getMessage()); // Password strength validation failure
            } else {
                showError("System error during reset.");
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
        resetPasswordButton.setDisable(loading);
        tokenField.setDisable(loading);
        newPasswordField.setDisable(loading);
        confirmPasswordField.setDisable(loading);
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
