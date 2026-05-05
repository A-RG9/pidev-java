package com.wellcare.javafx.controller.admin;

import com.wellcare.javafx.model.User;
import com.wellcare.javafx.service.UserService;
import com.wellcare.javafx.util.ValidationUtils;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;

public class AdminEditUserController {

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private ComboBox<String> statusComboBox;
    
    @FXML private VBox professionalInfoBox;
    @FXML private TextField specialtyField;
    @FXML private TextField licenseField;
    @FXML private TextField experienceField;
    
    @FXML private Label errorLabel;
    @FXML private Button saveBtn;
    @FXML private Button resetPasswordBtn;

    private User user;
    private UserService userService;
    private boolean saved = false;

    @FXML
    public void initialize() {
        roleComboBox.setItems(FXCollections.observableArrayList(
            "ROLE_PATIENT", "ROLE_MEDECIN", "ROLE_COACH", "ROLE_NUTRITIONIST", "ROLE_ADMIN"
        ));
        
        statusComboBox.setItems(FXCollections.observableArrayList("Active", "Inactive"));
        
        roleComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean isProf = newVal != null && !newVal.equals("ROLE_PATIENT") && !newVal.equals("ROLE_ADMIN");
            professionalInfoBox.setVisible(isProf);
            professionalInfoBox.setManaged(isProf);
            if (isProf) {
                validateProfessionalFields();
            } else {
                ValidationUtils.clearValidationStyle(specialtyField);
                ValidationUtils.clearValidationStyle(licenseField);
            }
        });

        // Setup real-time validation
        firstNameField.textProperty().addListener((obs, oldVal, newVal) -> validateFirstName());
        lastNameField.textProperty().addListener((obs, oldVal, newVal) -> validateLastName());
        emailField.textProperty().addListener((obs, oldVal, newVal) -> validateEmail());
        phoneField.textProperty().addListener((obs, oldVal, newVal) -> validatePhone());
        specialtyField.textProperty().addListener((obs, oldVal, newVal) -> validateProfessionalFields());
        licenseField.textProperty().addListener((obs, oldVal, newVal) -> validateProfessionalFields());
    }

    private boolean validateFirstName() {
        String error = ValidationUtils.validateName(firstNameField.getText(), "First name");
        ValidationUtils.applyValidationStyle(firstNameField, error == null);
        return error == null;
    }

    private boolean validateLastName() {
        String error = ValidationUtils.validateName(lastNameField.getText(), "Last name");
        ValidationUtils.applyValidationStyle(lastNameField, error == null);
        return error == null;
    }

    private boolean validateEmail() {
        String error = ValidationUtils.validateEmail(emailField.getText());
        ValidationUtils.applyValidationStyle(emailField, error == null);
        return error == null;
    }

    private boolean validatePhone() {
        String error = ValidationUtils.validatePhone(phoneField.getText());
        ValidationUtils.applyValidationStyle(phoneField, error == null);
        return error == null;
    }

    private boolean validateProfessionalFields() {
        if (!professionalInfoBox.isVisible()) return true;

        boolean specValid = specialtyField.getText() != null && !specialtyField.getText().trim().isEmpty();
        boolean licValid = licenseField.getText() != null && !licenseField.getText().trim().isEmpty();

        ValidationUtils.applyValidationStyle(specialtyField, specValid);
        ValidationUtils.applyValidationStyle(licenseField, licValid);

        return specValid && licValid;
    }

    public void setUser(User user, UserService userService) {
        this.user = user;
        this.userService = userService;
        
        firstNameField.setText(user.getFirstName());
        lastNameField.setText(user.getLastName());
        emailField.setText(user.getEmail());
        phoneField.setText(user.getPhone());
        roleComboBox.setValue(user.getRole());
        statusComboBox.setValue(user.isActive() ? "Active" : "Inactive");
        
        if (user.getSpecialite() != null) specialtyField.setText(user.getSpecialite());
        if (user.getLicenseNumber() != null) licenseField.setText(user.getLicenseNumber());
        experienceField.setText(String.valueOf(user.getYearsOfExperience()));
    }

    @FXML
    private void handleSave() {
        if (!validateInput()) return;
        
        try {
            updateUserFromFields();
            if (userService.adminUpdateUser(user)) {
                saved = true;
                closeStage();
            } else {
                showError("Failed to update user in database.");
            }
        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
        }
    }

    @FXML
    private void handleResetPassword() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Reset Password");
        confirm.setHeaderText("Generate temporary password for " + user.getEmail() + "?");
        confirm.setContentText("A new 8-character password will be generated and the user will be notified.");
        
        confirm.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                try {
                    String newPass = userService.adminResetPassword(user.getUuid());
                    if (newPass != null) {
                        Alert info = new Alert(Alert.AlertType.INFORMATION);
                        info.setTitle("Success");
                        info.setHeaderText("Password Reset Successful");
                        info.setContentText("New Temporary Password: " + newPass + "\n\nPlease provide this to the user.");
                        info.showAndWait();
                    }
                } catch (Exception e) {
                    showError("Reset failed: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleCancel() {
        closeStage();
    }

    private boolean validateInput() {
        boolean fnValid = validateFirstName();
        boolean lnValid = validateLastName();
        boolean emValid = validateEmail();
        boolean phValid = validatePhone();
        boolean profValid = validateProfessionalFields();
        
        if (!fnValid || !lnValid || !emValid) {
            showError("Please correct the highlighted fields.");
            return false;
        }

        if (professionalInfoBox.isVisible() && !profValid) {
            showError("Specialty and License are required for professionals.");
            return false;
        }
        
        errorLabel.setVisible(false);
        return true;
    }

    private void updateUserFromFields() {
        user.setFirstName(firstNameField.getText().trim());
        user.setLastName(lastNameField.getText().trim());
        user.setEmail(emailField.getText().trim());
        user.setPhone(phoneField.getText().trim());
        user.setRole(roleComboBox.getValue());
        user.setActive(statusComboBox.getValue().equals("Active"));
        
        if (professionalInfoBox.isVisible()) {
            user.setSpecialite(specialtyField.getText().trim());
            user.setLicenseNumber(licenseField.getText().trim());
            try {
                user.setYearsOfExperience(Integer.parseInt(experienceField.getText().trim()));
            } catch (NumberFormatException e) {
                user.setYearsOfExperience(0);
            }
        }
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
    }

    public boolean isSaved() {
        return saved;
    }

    private void closeStage() {
        ((Stage) saveBtn.getScene().getWindow()).close();
    }
}
