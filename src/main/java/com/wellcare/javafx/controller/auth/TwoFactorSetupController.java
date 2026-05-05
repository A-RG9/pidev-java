package com.wellcare.javafx.controller.auth;

import com.wellcare.javafx.model.User;
import com.wellcare.javafx.service.UserService;
import com.wellcare.javafx.util.SceneManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import java.sql.SQLException;
import java.util.List;

/**
 * Controller for setting up and managing 2FA.
 */
public class TwoFactorSetupController implements SceneManager.ServiceAware, SceneManager.UserAware {

    @FXML private VBox setupSection;
    @FXML private VBox manageSection;
    @FXML private VBox backupCodesBox;
    @FXML private FlowPane backupCodesFlowPane;
    @FXML private FlowPane activeBackupCodesFlowPane;
    @FXML private ImageView qrImageView;
    @FXML private Label secretLabel;
    @FXML private TextField verifyField;
    @FXML private Label errorLabel;
    @FXML private Button activateButton;

    private UserService userService;
    private User currentUser;
    private String currentSecret;

    @Override
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void setCurrentUser(User user) {
        this.currentUser = user;
        Platform.runLater(this::refreshUI);
    }

    private void refreshUI() {
        if (currentUser.isTwoFactorEnabled()) {
            setupSection.setVisible(false);
            setupSection.setManaged(false);
            manageSection.setVisible(true);
            manageSection.setManaged(true);
            displayActiveBackupCodes();
        } else {
            setupSection.setVisible(true);
            setupSection.setManaged(true);
            manageSection.setVisible(false);
            manageSection.setManaged(false);
            backupCodesBox.setVisible(false);
            backupCodesBox.setManaged(false);
            verifyField.setDisable(false);
            activateButton.setDisable(false);
            verifyField.clear();
            initializeSetup();
        }
    }

    private void initializeSetup() {
        try {
            String otpUri = userService.initiate2FASetup(currentUser.getUuid());
            
            int start = otpUri.indexOf("secret=") + 7;
            int end = otpUri.indexOf("&", start);
            if (end == -1) end = otpUri.length();
            currentSecret = otpUri.substring(start, end);
            secretLabel.setText(currentSecret);

            String qrUrl = "https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=" + 
                           java.net.URLEncoder.encode(otpUri, java.nio.charset.StandardCharsets.UTF_8);
            qrImageView.setImage(new Image(qrUrl, true));
            
        } catch (SQLException e) {
            showError("Erreur d'initialisation : " + e.getMessage());
        }
    }

    private void displayActiveBackupCodes() {
        activeBackupCodesFlowPane.getChildren().clear();
        List<String> codes = currentUser.getBackupCodes();
        if (codes != null) {
            for (String code : codes) {
                Label label = new Label(code);
                label.setStyle("-fx-font-family: 'monospace'; -fx-padding: 8 15; -fx-background-color: white; -fx-border-color: #dee2e6; -fx-border-radius: 6; -fx-font-weight: bold; -fx-text-fill: #002F5C;");
                activeBackupCodesFlowPane.getChildren().add(label);
            }
        }
    }

    @FXML
    public void handleActivate() {
        String code = verifyField.getText().trim();
        if (code.isEmpty()) {
            showError("Veuillez entrer le code de vérification");
            return;
        }

        try {
            List<String> backupCodes = userService.activate2FA(currentUser.getUuid(), currentSecret, code);
            
            // Update local user object
            currentUser.setTwoFactorEnabled(true);
            currentUser.setTotpSecret(currentSecret);
            currentUser.setBackupCodes(backupCodes);
            
            showSuccessBackupCodes(backupCodes);
            
        } catch (SQLException e) {
            showError("Erreur lors de l'activation : " + e.getMessage());
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        }
    }

    private void showSuccessBackupCodes(List<String> codes) {
        backupCodesFlowPane.getChildren().clear();
        for (String code : codes) {
            Label label = new Label(code);
            label.setStyle("-fx-font-family: 'monospace'; -fx-padding: 8 15; -fx-background-color: #e9ecef; -fx-background-radius: 6; -fx-font-weight: bold;");
            backupCodesFlowPane.getChildren().add(label);
        }
        backupCodesBox.setVisible(true);
        backupCodesBox.setManaged(true);
        activateButton.setDisable(true);
        verifyField.setDisable(true);
    }

    @FXML
    public void handleDisable() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Désactiver la 2FA");
        alert.setHeaderText("Êtes-vous sûr de vouloir désactiver la 2FA ?");
        alert.setContentText("Votre compte sera moins sécurisé.");
        
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                if (userService.disable2FA(currentUser.getUuid())) {
                    currentUser.setTwoFactorEnabled(false);
                    currentUser.setTotpSecret(null);
                    currentUser.setBackupCodes(null);
                    refreshUI();
                }
            } catch (SQLException e) {
                showError("Erreur lors de la désactivation : " + e.getMessage());
            }
        }
    }

    @FXML
    public void handleDone() {
        refreshUI();
    }

    @FXML
    public void handleCancel() {
        SceneManager.getInstance().switchTo(SceneManager.PROFILE);
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
}
