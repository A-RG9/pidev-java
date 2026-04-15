package com.wellcare.javafx.controller.auth;

import com.wellcare.javafx.model.User;
import com.wellcare.javafx.service.UserService;
import com.wellcare.javafx.util.SceneManager;
import com.wellcare.javafx.util.ValidationUtils;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the Profile page.
 * Handles viewing and editing user information.
 */
public class ProfileController implements Initializable, SceneManager.ServiceAware {

    @FXML private Label fullNameLabel;
    @FXML private Label roleLabel;
    @FXML private Label memberSinceLabel;
    
    // Basic Info
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private DatePicker birthdatePicker;
    @FXML private TextField addressField;
    
    // Professional Info (Only visible for professionals)
    @FXML private VBox professionalSection;
    @FXML private TextField specialiteField;
    @FXML private TextField experienceField;
    @FXML private TextField educationField;
    @FXML private TextField consultationPriceField;
    @FXML private TextArea aboutArea;
    
    @FXML private Button saveButton;
    @FXML private Button backButton;
    @FXML private Label statusLabel;
    @FXML private ProgressIndicator loadingIndicator;

    private UserService userService;
    private User currentUser;

    @Override
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentUser = SceneManager.getInstance().getCurrentUser();
        
        if (currentUser != null) {
            populateFields();
            setupVisibility();
        }
        
        backButton.setOnAction(e -> navigateBack());
        saveButton.setOnAction(e -> handleSave());
        
        setupValidationListeners();
    }

    private void setupValidationListeners() {
        firstNameField.textProperty().addListener((obs, oldV, newV) -> validateForm());
        lastNameField.textProperty().addListener((obs, oldV, newV) -> validateForm());
        phoneField.textProperty().addListener((obs, oldV, newV) -> validateForm());
        birthdatePicker.valueProperty().addListener((obs, oldV, newV) -> validateForm());
        
        if (professionalSection.isVisible()) {
            experienceField.textProperty().addListener((obs, oldV, newV) -> validateForm());
            consultationPriceField.textProperty().addListener((obs, oldV, newV) -> validateForm());
        }
        
        // Initial validation
        validateForm();
    }

    private boolean validateForm() {
        boolean isValid = true;
        
        // Validate First Name
        String fnError = ValidationUtils.validateName(firstNameField.getText(), "First name");
        ValidationUtils.applyValidationStyle(firstNameField, fnError == null);
        if (fnError != null) isValid = false;
        
        // Validate Last Name
        String lnError = ValidationUtils.validateName(lastNameField.getText(), "Last name");
        ValidationUtils.applyValidationStyle(lastNameField, lnError == null);
        if (lnError != null) isValid = false;
        
        // Validate Phone (optional)
        String phError = ValidationUtils.validatePhone(phoneField.getText());
        ValidationUtils.applyValidationStyle(phoneField, phError == null);
        if (phError != null) isValid = false;
        
        // Professional specific validation
        if (professionalSection.isVisible()) {
            String expError = ValidationUtils.validateNumericRange(experienceField.getText(), 0, 50, "Experience");
            ValidationUtils.applyValidationStyle(experienceField, expError == null);
            if (expError != null) isValid = false;
            
            String priceError = consultationPriceField.getText().isEmpty() ? null : 
                               ValidationUtils.validateNumericRange(consultationPriceField.getText(), 0, 1000, "Price");
            ValidationUtils.applyValidationStyle(consultationPriceField, priceError == null);
            if (priceError != null) isValid = false;
        }
        
        saveButton.setDisable(!isValid);
        return isValid;
    }

    private void populateFields() {
        fullNameLabel.setText(currentUser.getFullName());
        roleLabel.setText(currentUser.getDisplayRole());
        memberSinceLabel.setText("Member since: " + currentUser.getCreatedAt().toLocalDate().toString());
        
        firstNameField.setText(currentUser.getFirstName());
        lastNameField.setText(currentUser.getLastName());
        emailField.setText(currentUser.getEmail());
        phoneField.setText(currentUser.getPhone());
        birthdatePicker.setValue(currentUser.getBirthdate());
        addressField.setText(currentUser.getAddress());
        
        // Professional fields
        specialiteField.setText(currentUser.getSpecialite());
        experienceField.setText(String.valueOf(currentUser.getYearsOfExperience()));
        educationField.setText(currentUser.getEducation());
        consultationPriceField.setText(currentUser.getConsultationPrice() != null ? String.valueOf(currentUser.getConsultationPrice()) : "");
        aboutArea.setText(currentUser.getAbout());
    }

    private void setupVisibility() {
        // Only show professional section for non-patients
        boolean isProfessional = currentUser.getRole() != null && !currentUser.getRole().equals("ROLE_PATIENT");
        professionalSection.setVisible(isProfessional);
        professionalSection.setManaged(isProfessional);
        
        // Email is not editable in simple profile view
        emailField.setEditable(false);
        emailField.setDisable(true);
    }

    private void handleSave() {
        setLoading(true);
        updateUserObject();

        Task<Boolean> saveTask = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                return userService.updateProfile(currentUser);
            }

            @Override
            protected void succeeded() {
                if (getValue()) {
                    showStatus("Profile updated successfully!", false);
                } else {
                    showStatus("Failed to update profile.", true);
                }
                setLoading(false);
            }

            @Override
            protected void failed() {
                showStatus("Error: " + getException().getMessage(), true);
                setLoading(false);
            }
        };

        new Thread(saveTask).start();
    }

    private void updateUserObject() {
        currentUser.setFirstName(firstNameField.getText());
        currentUser.setLastName(lastNameField.getText());
        currentUser.setPhone(phoneField.getText());
        currentUser.setBirthdate(birthdatePicker.getValue());
        currentUser.setAddress(addressField.getText());
        currentUser.setAbout(aboutArea.getText());
        
        if (professionalSection.isVisible()) {
            currentUser.setSpecialite(specialiteField.getText());
            currentUser.setEducation(educationField.getText());
            try {
                currentUser.setYearsOfExperience(Integer.parseInt(experienceField.getText()));
                if (!consultationPriceField.getText().isEmpty()) {
                    currentUser.setConsultationPrice(Integer.parseInt(consultationPriceField.getText()));
                }
            } catch (NumberFormatException e) {
                // Ignore invalid numbers for now
            }
        }
    }

    private void navigateBack() {
        SceneManager.getInstance().switchToPrevious();
    }

    private void setLoading(boolean loading) {
        saveButton.setDisable(loading);
        loadingIndicator.setVisible(loading);
    }

    private void showStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setStyle(isError ? "-fx-text-fill: #dc3545;" : "-fx-text-fill: #28a745;");
        statusLabel.setVisible(true);
        
        // Hide after 3 seconds
        new Thread(() -> {
            try {
                Thread.sleep(3000);
                Platform.runLater(() -> statusLabel.setVisible(false));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
}
