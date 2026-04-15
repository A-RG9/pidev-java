package com.wellcare.javafx.controller.admin;

import com.wellcare.javafx.model.User;
import com.wellcare.javafx.service.UserService;
import com.wellcare.javafx.util.SceneManager;
import com.wellcare.javafx.util.SceneManager.ServiceAware;
import com.wellcare.javafx.util.SceneManager.UserAware;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class VerificationQueueController implements Initializable, ServiceAware, UserAware {

    // Service and user
    private UserService userService;
    private User currentUser;

    // FXML injected fields
    @FXML private Button refreshBtn;
    @FXML private Label pendingCountLabel;
    @FXML private Label approvedTodayLabel;
    @FXML private Label rejectedTodayLabel;
    @FXML private Label avgReviewTimeLabel;

    // Filter controls
    @FXML private ToggleButton allBtn;
    @FXML private ToggleButton doctorsBtn;
    @FXML private ToggleButton coachesBtn;
    @FXML private ToggleButton nutritionistsBtn;
    @FXML private TextField searchField;

    // Applications list and details
    @FXML private ListView<User> applicationsList;
    @FXML private VBox detailsCard;
    @FXML private VBox emptyStateCard;

    // Detail labels
    @FXML private Label detailNameLabel;
    @FXML private Label detailRoleLabel;
    @FXML private Label detailEmailLabel;
    @FXML private Label detailStatusLabel;
    @FXML private Label detailDateLabel;
    @FXML private Label detailLicenseLabel;
    @FXML private Label detailExperienceLabel;
    @FXML private Label detailPhoneLabel;
    @FXML private Label detailLocationLabel;

    // Document buttons
    @FXML private Button viewDiplomaBtn;
    @FXML private Button viewLicenseBtn;
    @FXML private Button viewCertificationsBtn;

    // Review and action controls
    @FXML private TextArea reviewNotesArea;
    @FXML private Button rejectBtn;
    @FXML private Button approveBtn;
    @FXML private Button bulkApproveBtn;
    @FXML private Button backToDashboardBtn;

    // Data
    private ObservableList<User> pendingApplications = FXCollections.observableArrayList();
    private ToggleGroup filterGroup = new ToggleGroup();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupFilters();
        setupApplicationsList();
        setupEventHandlers();
        // Don't load data here - wait for services to be injected
    }

    private void setupFilters() {
        // Setup toggle group
        filterGroup = new ToggleGroup();
        allBtn.setToggleGroup(filterGroup);
        doctorsBtn.setToggleGroup(filterGroup);
        coachesBtn.setToggleGroup(filterGroup);
        nutritionistsBtn.setToggleGroup(filterGroup);
        allBtn.setSelected(true);

        // Add listeners
        filterGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> applyFilters());
        searchField.textProperty().addListener((obs, oldText, newText) -> applyFilters());
    }

    private void setupApplicationsList() {
        applicationsList.setItems(pendingApplications);
        applicationsList.setCellFactory(listView -> new ListCell<User>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    String roleDisplay = getRoleDisplay(user.getRole());
                    String fullName = user.getFirstName() + " " + user.getLastName();
                    String specialty = user.getSpecialty() != null ? " - " + user.getSpecialty() : "";
                    String appliedDate = user.getCreatedAt() != null ?
                        user.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) : "Unknown";

                    setText(String.format("%s (%s%s)\nApplied: %s",
                        fullName, roleDisplay, specialty, appliedDate));

                    // Style based on priority (doctors first, then urgent applications)
                    if ("ROLE_MEDECIN".equals(user.getRole())) {
                        getStyleClass().add("priority-high");
                    }
                }
            }
        });

        // Selection listener
        applicationsList.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                showApplicationDetails(newSelection);
            } else {
                showEmptyState();
            }
        });
    }

    private void setupEventHandlers() {
        refreshBtn.setOnAction(e -> loadPendingApplications());
        backToDashboardBtn.setOnAction(e -> SceneManager.getInstance().switchTo(SceneManager.ADMIN_DASHBOARD));

        approveBtn.setOnAction(e -> approveSelectedApplication());
        rejectBtn.setOnAction(e -> rejectSelectedApplication());

        viewDiplomaBtn.setOnAction(e -> handleViewDiploma());
        viewLicenseBtn.setOnAction(e -> viewDocument("License"));
        viewCertificationsBtn.setOnAction(e -> viewDocument("Certifications"));
    }

    private void loadPendingApplications() {
        try {
            List<User> allUsers = userService.getAllUsers();
            List<User> pendingUsers = allUsers.stream()
                .filter(user -> !user.isVerifiedByAdmin() &&
                               (user.getRole().equals("ROLE_MEDECIN") ||
                                user.getRole().equals("ROLE_COACH") ||
                                user.getRole().equals("ROLE_NUTRITIONIST")))
                .collect(java.util.stream.Collectors.toList());
            pendingApplications.clear();
            pendingApplications.addAll(pendingUsers);

            // Update statistics
            pendingCountLabel.setText(String.valueOf(pendingUsers.size()));

            // Mock statistics (in real implementation, these would come from database)
            approvedTodayLabel.setText("0");
            rejectedTodayLabel.setText("0");
            avgReviewTimeLabel.setText("2.5h");

            // Apply current filters
            applyFilters();

            // Show empty state if no applications
            if (pendingApplications.isEmpty()) {
                showEmptyState();
            }

        } catch (Exception e) {
            showAlert("Error", "Failed to load applications", e.getMessage());
        }
    }

    private void applyFilters() {
        String searchText = searchField.getText().toLowerCase();
        Toggle selectedFilter = filterGroup.getSelectedToggle();

        applicationsList.setItems(pendingApplications.filtered(user -> {
            // Search filter
            if (!searchText.isEmpty()) {
                String fullName = (user.getFirstName() + " " + user.getLastName()).toLowerCase();
                String email = user.getEmail().toLowerCase();
                if (!fullName.contains(searchText) && !email.contains(searchText)) {
                    return false;
                }
            }

            // Role filter
            if (selectedFilter == doctorsBtn && !"ROLE_MEDECIN".equals(user.getRole())) {
                return false;
            } else if (selectedFilter == coachesBtn && !"ROLE_COACH".equals(user.getRole())) {
                return false;
            } else if (selectedFilter == nutritionistsBtn && !"ROLE_NUTRITIONIST".equals(user.getRole())) {
                return false;
            }

            return true;
        }));
    }

    private void showApplicationDetails(User user) {
        // Hide empty state, show details
        emptyStateCard.setVisible(false);
        detailsCard.setVisible(true);

        // Populate details
        detailNameLabel.setText(user.getFirstName() + " " + user.getLastName());
        detailRoleLabel.setText(getRoleDisplay(user.getRole()) +
            (user.getSpecialty() != null ? " - " + user.getSpecialty() : ""));
        detailEmailLabel.setText(user.getEmail());
        detailStatusLabel.setText("⏳ PENDING");

        String appliedDate = user.getCreatedAt() != null ?
            user.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) : "Unknown";
        detailDateLabel.setText("Applied: " + appliedDate);

        detailLicenseLabel.setText(user.getLicenseNumber() != null ? user.getLicenseNumber() : "Not provided");
        detailExperienceLabel.setText(user.getYearsOfExperience() > 0 ?
            user.getYearsOfExperience() + " years" : "Not specified");
        detailPhoneLabel.setText(user.getPhone() != null ? user.getPhone() : "Not provided");
        detailLocationLabel.setText(user.getAddress() != null ? user.getAddress() : "Not provided");

        // Clear review notes
        reviewNotesArea.clear();
    }

    private void showEmptyState() {
        detailsCard.setVisible(false);
        emptyStateCard.setVisible(true);
    }

    private void approveSelectedApplication() {
        User selectedUser = applicationsList.getSelectionModel().getSelectedItem();
        if (selectedUser == null) return;

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Approve Application");
        confirmation.setHeaderText("Approve " + selectedUser.getFirstName() + " " + selectedUser.getLastName() + "?");
        confirmation.setContentText("This will grant the professional access to the platform and notify them via email.");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // Add review notes if provided
                    String notes = reviewNotesArea.getText().trim();
                    if (!notes.isEmpty()) {
                        // In a real implementation, you'd save these notes
                        System.out.println("Review notes for " + selectedUser.getEmail() + ": " + notes);
                    }

                    userService.verifyProfessional(selectedUser.getUuid());
                    showAlert("Success", "Application Approved",
                        selectedUser.getFirstName() + " " + selectedUser.getLastName() +
                        " has been verified and granted access to the platform.");

                    loadPendingApplications(); // Refresh the list
                    showEmptyState(); // Clear details

                } catch (Exception e) {
                    showAlert("Error", "Approval Failed", e.getMessage());
                }
            }
        });
    }

    private void rejectSelectedApplication() {
        User selectedUser = applicationsList.getSelectionModel().getSelectedItem();
        if (selectedUser == null) return;

        TextInputDialog reasonDialog = new TextInputDialog();
        reasonDialog.setTitle("Reject Application");
        reasonDialog.setHeaderText("Reject " + selectedUser.getFirstName() + " " + selectedUser.getLastName());
        reasonDialog.setContentText("Please provide a reason for rejection:");
        reasonDialog.getEditor().setPromptText("Reason for rejection...");

        reasonDialog.showAndWait().ifPresent(reason -> {
            if (!reason.trim().isEmpty()) {
                Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
                confirmation.setTitle("Confirm Rejection");
                confirmation.setHeaderText("Are you sure you want to reject this application?");
                confirmation.setContentText("Reason: " + reason);

                confirmation.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        try {
                            userService.unverifyProfessional(selectedUser.getUuid());
                            showAlert("Application Rejected",
                                selectedUser.getFirstName() + " " + selectedUser.getLastName() +
                                " has been rejected and notified.",
                                "Reason: " + reason);

                            loadPendingApplications(); // Refresh the list
                            showEmptyState(); // Clear details

                        } catch (Exception e) {
                            showAlert("Error", "Rejection Failed", e.getMessage());
                        }
                    }
                });
            } else {
                showAlert("Error", "Rejection Failed", "Please provide a reason for rejection.");
            }
        });
    }

    private void handleViewDiploma() {
        User selectedUser = applicationsList.getSelectionModel().getSelectedItem();
        if (selectedUser == null || selectedUser.getDiplomaUrl() == null || selectedUser.getDiplomaUrl().isEmpty()) {
            showAlert("Info", "No Diploma", "This professional has not uploaded a diploma.");
            return;
        }

        java.io.File file = userService.getDiplomaFile(selectedUser.getDiplomaUrl());
        if (file == null || !file.exists()) {
            showAlert("Error", "File Not Found", "The diploma file could not be located on the server.");
            return;
        }

        try {
            java.awt.Desktop.getDesktop().open(file);
        } catch (Exception e) {
            showAlert("Error", "Viewer Error", "Could not open the file viewer: " + e.getMessage());
        }
    }

    private void viewDocument(String documentType) {
        User selectedUser = applicationsList.getSelectionModel().getSelectedItem();
        if (selectedUser == null) return;

        String message = documentType + " for " + selectedUser.getFirstName() + " " + selectedUser.getLastName();

        switch (documentType) {
            case "License":
                message += "\nLicense Number: " + (selectedUser.getLicenseNumber() != null ?
                    selectedUser.getLicenseNumber() : "Not provided");
                break;
            case "Certifications":
                message += "\nCertifications: " + (selectedUser.getCertifications() != null ?
                    selectedUser.getCertifications() : "Not provided");
                break;
        }

        showAlert("Document Info", documentType + " Details", message);
    }
    private String getRoleDisplay(String role) {
        return switch (role) {
            case "ROLE_MEDECIN" -> "Doctor";
            case "ROLE_COACH" -> "Coach";
            case "ROLE_NUTRITIONIST" -> "Nutritionist";
            default -> role;
        };
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @Override
    public void setUserService(UserService userService) {
        this.userService = userService;
        // Load data after service is injected
        if (this.userService != null) {
            loadPendingApplications();
        }
    }

    @Override
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }
}