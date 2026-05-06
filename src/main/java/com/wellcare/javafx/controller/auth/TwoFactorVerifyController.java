package com.wellcare.javafx.controller.auth;

import com.wellcare.javafx.model.User;
import com.wellcare.javafx.service.UserService;
import com.wellcare.javafx.util.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import java.sql.SQLException;

/**
 * Controller for the 2FA verification screen during login.
 */
public class TwoFactorVerifyController implements SceneManager.ServiceAware, SceneManager.StatefulController {

    @FXML private TextField codeField;
    @FXML private Label errorLabel;

    private UserService userService;
    private User pendingUser;

    @Override
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void setControllerState(Object state) {
        if (state instanceof User) {
            this.pendingUser = (User) state;
        }
    }

    @FXML
    public void handleVerify() {
        String code = codeField.getText().trim();
        if (code.isEmpty()) {
            showError("Veuillez entrer le code de vérification");
            return;
        }

        System.out.println("DEBUG: 2FA handleVerify called with code: " + code);
        try {
            if (pendingUser == null) {
                System.out.println("DEBUG: pendingUser is null!");
                showError("Session expirée. Veuillez vous reconnecter.");
                return;
            }
            boolean result = userService.verify2FA(pendingUser, code);
            System.out.println("DEBUG: verify2FA result: " + result);
            if (result) {
                // Verification successful, complete login
                SceneManager.getInstance().login(pendingUser);
            } else {
                showError("Code invalide. Veuillez réessayer.");
            }
        } catch (SQLException e) {
            showError("Erreur lors de la vérification : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void handleCancel() {
        SceneManager.getInstance().switchToLogin();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
}
