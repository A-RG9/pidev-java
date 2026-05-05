package com.wellcare.javafx.controller.admin;

import com.wellcare.javafx.model.User;
import com.wellcare.javafx.service.UserService;
import com.wellcare.javafx.util.SceneManager;
import com.wellcare.javafx.util.SceneManager.ServiceAware;
import com.wellcare.javafx.util.SceneManager.UserAware;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.awt.Desktop;
import java.io.File;

/**
 * Controller for the Admin User Management interface
 * Provides comprehensive user management with data tables, filtering, bulk operations, and verification workflow
 */
public class UserManagementController implements Initializable, ServiceAware, UserAware {

    // Service and user
    private UserService userService;
    private User currentUser;

    // Menu components
    @FXML private MenuButton userMenu;
    @FXML private MenuItem profileMenuItem;
    @FXML private MenuItem settingsMenuItem;
    @FXML private MenuItem logoutMenuItem;
    @FXML private MenuItem backToDashboardMenuItem;

    // Search and filters
    @FXML private TextField searchField;
    @FXML private ComboBox<String> roleFilterComboBox;
    @FXML private ComboBox<String> statusFilterComboBox;

    // Bulk actions
    @FXML private Button activateSelectedBtn;
    @FXML private Button deactivateSelectedBtn;
    @FXML private Button deleteSelectedBtn;
    @FXML private Button verifySelectedBtn;
    @FXML private Label selectionCountLabel;

    // Table components
    @FXML private TableView<UserTableItem> usersTable;
    @FXML private TableColumn<UserTableItem, Boolean> selectColumn;
    @FXML private TableColumn<UserTableItem, String> idColumn;
    @FXML private TableColumn<UserTableItem, String> nameColumn;
    @FXML private TableColumn<UserTableItem, String> emailColumn;
    @FXML private TableColumn<UserTableItem, String> roleColumn;
    @FXML private TableColumn<UserTableItem, String> statusColumn;
    @FXML private TableColumn<UserTableItem, String> specialtyColumn;
    @FXML private TableColumn<UserTableItem, String> registeredColumn;
    @FXML private TableColumn<UserTableItem, String> actionsColumn;

    // Pagination
    @FXML private ComboBox<String> pageSizeComboBox;
    @FXML private TextField pageNumberField;
    @FXML private Label totalPagesLabel;
    @FXML private Button firstPageBtn;
    @FXML private Button prevPageBtn;
    @FXML private Button nextPageBtn;
    @FXML private Button lastPageBtn;
    @FXML private Label paginationInfoLabel;

    // User details panel
    @FXML private VBox userDetailsPanel;
    @FXML private Label detailFullNameLabel;
    @FXML private Label detailEmailLabel;
    @FXML private Label detailRoleLabel;
    @FXML private Label detailStatusLabel;
    @FXML private Label detailPhoneLabel;
    @FXML private Label detailBirthDateLabel;
    @FXML private Label detailLicenseLabel;
    @FXML private Label detailSpecialtyLabel;
    @FXML private Label detailExperienceLabel;
    @FXML private Label detailRegistrationDateLabel;
    @FXML private Button viewDiplomaBtn;
    @FXML private Button editUserBtn;
    @FXML private Button resetPasswordBtn;
    @FXML private Button closeDetailsBtn;

    // Data management
    private ObservableList<UserTableItem> usersList;
    private FilteredList<UserTableItem> filteredUsers;
    private int currentPage = 1;
    private int pageSize = 10;

    /**
     * Wrapper class for User to support table checkbox functionality
     */
    public static class UserTableItem {
        private final User user;
        private boolean selected;

        public UserTableItem(User user) {
            this.user = user;
            this.selected = false;
        }

        public User getUser() { return user; }
        public boolean isSelected() { return selected; }
        public void setSelected(boolean selected) { this.selected = selected; }

        // JavaFX property for table binding
        public javafx.beans.property.BooleanProperty selectedProperty() {
            return new javafx.beans.property.SimpleBooleanProperty(selected) {
                @Override
                public void set(boolean newValue) {
                    super.set(newValue);
                    UserTableItem.this.selected = newValue;
                }
            };
        }

        // Delegate methods to user for table binding
        public String getUuid() { return user.getUuid(); }
        public String getFullName() { return user.getFirstName() + " " + user.getLastName(); }
        public String getEmail() { return user.getEmail(); }
        public String getRoleDisplay() {
            switch (user.getRole()) {
                case "ROLE_PATIENT": return "Patient";
                case "ROLE_MEDECIN": return "Physician";
                case "ROLE_COACH": return "Coach";
                case "ROLE_NUTRITIONIST": return "Nutritionist";
                case "ROLE_ADMIN": return "Administrator";
                default: return user.getRole();
            }
        }
        public String getStatusDisplay() {
            if (!user.isActive()) return "Inactive";
            if (!user.isVerifiedByAdmin()) return "Pending Verification";
            return "Active";
        }
        public String getSpecialtyOrLicense() {
            if (user.getSpecialite() != null && !user.getSpecialite().isEmpty()) {
                return user.getSpecialite();
            }
            if (user.getLicenseNumber() != null && !user.getLicenseNumber().isEmpty()) {
                return "License: " + user.getLicenseNumber();
            }
            return "-";
        }
        public String getCreatedAtDisplay() {
            return user.getCreatedAt() != null ?
                user.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) : "Unknown";
        }
    }

    private User currentAdmin;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        usersList = FXCollections.observableArrayList();
        filteredUsers = new javafx.collections.transformation.FilteredList<>(usersList, p -> true);

        setupTable();
        setupFilters();
        setupPagination();
        setupEventHandlers();
        // Removed loadUsers() from here because userService is not yet injected
        
        updateBulkActionButtons();
        updatePaginationControls();
    }

    private void setupTable() {
        // Configure table columns for UserTableItem
        selectColumn.setCellFactory(CheckBoxTableCell.forTableColumn(selectColumn));
        selectColumn.setCellValueFactory(cellData ->
            cellData.getValue().selectedProperty());

        idColumn.setCellValueFactory(new PropertyValueFactory<>("uuid"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("roleDisplay"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("statusDisplay"));
        specialtyColumn.setCellValueFactory(new PropertyValueFactory<>("specialtyOrLicense"));
        registeredColumn.setCellValueFactory(new PropertyValueFactory<>("createdAtDisplay"));

        // Actions column with buttons
        actionsColumn.setCellFactory(col -> new TableCell<UserTableItem, String>() {
            private final Button viewBtn = new Button("👁️ View");
            private final Button editBtn = new Button("✏️ Edit");
            private final Button deleteBtn = new Button("🗑️ Delete");
            private final HBox container = new HBox(5, viewBtn, editBtn, deleteBtn);

            {
                viewBtn.getStyleClass().addAll("btn", "btn-info");
                viewBtn.setStyle("-fx-font-size: 11px; -fx-padding: 4 8 4 8;");
                editBtn.getStyleClass().addAll("btn", "btn-primary");
                editBtn.setStyle("-fx-font-size: 11px; -fx-padding: 4 8 4 8;");
                deleteBtn.getStyleClass().addAll("btn", "btn-danger");
                deleteBtn.setStyle("-fx-font-size: 11px; -fx-padding: 4 8 4 8;");

                viewBtn.setOnAction(e -> {
                    UserTableItem item = getTableView().getItems().get(getIndex());
                    showUserDetails(item.getUser());
                });

                editBtn.setOnAction(e -> {
                    UserTableItem item = getTableView().getItems().get(getIndex());
                    handleEditUser(item.getUser());
                });

                deleteBtn.setOnAction(e -> {
                    UserTableItem item = getTableView().getItems().get(getIndex());
                    handleDeleteUser(item.getUser());
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(container);
                }
            }
        });
    }

    private void setupFilters() {
        // Initialize filter combo boxes
        roleFilterComboBox.setItems(FXCollections.observableArrayList(
            "All Roles", "ROLE_PATIENT", "ROLE_MEDECIN", "ROLE_COACH", "ROLE_NUTRITIONIST", "ROLE_ADMIN"));
        roleFilterComboBox.setValue("All Roles");

        statusFilterComboBox.setItems(FXCollections.observableArrayList(
            "All Status", "Active", "Inactive", "Pending Verification", "Suspended"));
        statusFilterComboBox.setValue("All Status");
    }

    private void setupPagination() {
        pageSizeComboBox.setItems(FXCollections.observableArrayList("10", "25", "50", "100"));
        pageSizeComboBox.setValue("10");
        pageSizeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                pageSize = Integer.parseInt(newVal);
                currentPage = 1;
                updateTableView();
                updatePaginationControls();
            }
        });
    }

    private void setupEventHandlers() {
        // User menu actions
        profileMenuItem.setOnAction(e -> handleProfile());
        settingsMenuItem.setOnAction(e -> handleSettings());
        logoutMenuItem.setOnAction(e -> handleLogout());
        backToDashboardMenuItem.setOnAction(e -> handleBackToDashboard());

        // Search and filter events
        searchField.textProperty().addListener((obs, oldText, newText) -> applyFilters());
        roleFilterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        statusFilterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        // Bulk actions
        activateSelectedBtn.setOnAction(e -> handleBulkActivate());
        deactivateSelectedBtn.setOnAction(e -> handleBulkDeactivate());
        deleteSelectedBtn.setOnAction(e -> handleBulkDelete());
        verifySelectedBtn.setOnAction(e -> handleBulkVerify());

        // Pagination
        firstPageBtn.setOnAction(e -> goToFirstPage());
        prevPageBtn.setOnAction(e -> goToPreviousPage());
        nextPageBtn.setOnAction(e -> goToNextPage());
        lastPageBtn.setOnAction(e -> goToLastPage());
        pageNumberField.setOnAction(e -> goToPage());

        // User details panel
        closeDetailsBtn.setOnAction(e -> hideUserDetails());
        viewDiplomaBtn.setOnAction(e -> handleViewDiploma());
        editUserBtn.setOnAction(e -> handleEditUserDetails());
        resetPasswordBtn.setOnAction(e -> handleResetPassword());
    }

    private void loadUsers() {
        try {
            // Load users from service
            List<User> users = userService.getAllUsers();
            usersList.clear();

            // Convert to UserTableItem
            for (User user : users) {
                usersList.add(new UserTableItem(user));
            }

            // Create filtered list
            filteredUsers = new FilteredList<>(usersList, p -> true);

            // Apply initial filters and pagination
            applyFilters();
            updateTableView();
            updatePaginationControls();

        } catch (Exception e) {
            System.err.println("Error loading users: " + e.getMessage());
            showAlert("Error", "Failed to load users", e.getMessage());
        }
    }

    private void applyFilters() {
        String searchText = searchField.getText().toLowerCase();
        String roleFilterValue = roleFilterComboBox.getValue();
        String statusFilterValue = statusFilterComboBox.getValue();

        filteredUsers.setPredicate(item -> {
            User user = item.getUser();

            // Search filter
            boolean matchesSearch = searchText.isEmpty() ||
                user.getFirstName().toLowerCase().contains(searchText) ||
                user.getLastName().toLowerCase().contains(searchText) ||
                user.getEmail().toLowerCase().contains(searchText);

            // Role filter
            boolean matchesRole = "All Roles".equals(roleFilterValue) ||
                roleFilterValue.equals(user.getRole());

            // Status filter
            boolean matchesStatus = "All Status".equals(statusFilterValue);
            if (!matchesStatus) {
                switch (statusFilterValue) {
                    case "Active":
                        matchesStatus = user.isActive() && user.isVerifiedByAdmin();
                        break;
                    case "Inactive":
                        matchesStatus = !user.isActive();
                        break;
                    case "Pending Verification":
                        matchesStatus = user.isActive() && !user.isVerifiedByAdmin();
                        break;
                    case "Suspended":
                        matchesStatus = !user.isActive();
                        break;
                }
            }

            return matchesSearch && matchesRole && matchesStatus;
        });

        // Reset to first page when filters change
        currentPage = 1;
        updateTableView();
        updatePaginationControls();
        updateBulkActionButtons();
    }

    private void updateTableView() {
        int totalItems = filteredUsers.size();
        int startIndex = (currentPage - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalItems);

        if (startIndex < totalItems) {
            List<UserTableItem> pageItems = filteredUsers.stream()
                .skip(startIndex)
                .limit(pageSize)
                .collect(Collectors.toList());

            // Create sorted list for the current page
            SortedList<UserTableItem> sortedPageItems = new SortedList<>(
                FXCollections.observableArrayList(pageItems));
            sortedPageItems.comparatorProperty().bind(usersTable.comparatorProperty());

            usersTable.setItems(sortedPageItems);
        } else {
            usersTable.setItems(FXCollections.emptyObservableList());
        }
    }

    private void updatePaginationControls() {
        int totalItems = filteredUsers.size();
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);

        totalPagesLabel.setText(String.valueOf(totalPages));
        pageNumberField.setText(String.valueOf(currentPage));

        firstPageBtn.setDisable(currentPage == 1);
        prevPageBtn.setDisable(currentPage == 1);
        nextPageBtn.setDisable(currentPage == totalPages);
        lastPageBtn.setDisable(currentPage == totalPages);

        int startItem = (currentPage - 1) * pageSize + 1;
        int endItem = Math.min(currentPage * pageSize, totalItems);
        paginationInfoLabel.setText(String.format("Showing %d to %d of %d entries",
            startItem, endItem, totalItems));
    }

    private void updateBulkActionButtons() {
        long selectedCount = usersTable.getItems().stream()
            .filter(UserTableItem::isSelected)
            .count();

        boolean hasSelection = selectedCount > 0;
        activateSelectedBtn.setDisable(!hasSelection);
        deactivateSelectedBtn.setDisable(!hasSelection);
        deleteSelectedBtn.setDisable(!hasSelection);
        verifySelectedBtn.setDisable(!hasSelection);

        selectionCountLabel.setText(selectedCount + " users selected");
    }


    private void handleEditUser(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/admin-edit-user.fxml"));
            Parent root = loader.load();
            
            AdminEditUserController controller = loader.getController();
            controller.setUser(user, userService);
            
            Stage stage = new Stage();
            stage.setTitle("Edit User - " + user.getEmail());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            
            if (controller.isSaved()) {
                loadUsers(); // Refresh table
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Could not open edit dialog", e.getMessage());
        }
    }

    // ==================== EVENT HANDLERS ====================

    private void handleBackToDashboard() {
        SceneManager.getInstance().switchTo(SceneManager.ADMIN_DASHBOARD);
    }

    private void handleDeleteUser(User user) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Delete");
        confirmation.setHeaderText("Delete user: " + user.getEmail() + "?");
        confirmation.setContentText("This action cannot be undone.");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // TODO: Implement delete via UserService
                showAlert("Info", "Delete User", "Delete functionality not yet implemented");
            }
        });
    }

    private void handleViewDiploma() {
        User user = userDetailsPanel.getUserData() instanceof User ? (User) userDetailsPanel.getUserData() : null;
        // Re-fetching user because the data in panel might be stale or partial
        if (user == null && usersTable.getSelectionModel().getSelectedItem() != null) {
            user = usersTable.getSelectionModel().getSelectedItem().getUser();
        }

        if (user == null || user.getDiplomaUrl() == null || user.getDiplomaUrl().isEmpty()) {
            showAlert("Info", "No Diploma", "This user has no diploma on file.");
            return;
        }

        File file = userService.getDiplomaFile(user.getDiplomaUrl());
        if (file == null) {
            showAlert("Error", "File Not Found", "The diploma file could not be located on the server.");
            return;
        }

        try {
            Desktop.getDesktop().open(file);
        } catch (Exception e) {
            showAlert("Error", "Viewer Error", "Could not open the file viewer: " + e.getMessage());
        }
    }

    private void handleEditUserDetails() {
        User user = (User) userDetailsPanel.getUserData();
        if (user != null) {
            handleEditUser(user);
        }
    }

    private void handleResetPassword() {
        // TODO: Implement password reset
        showAlert("Info", "Reset Password", "Password reset not yet implemented");
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // ==================== PAGINATION METHODS ====================

    private void goToFirstPage() {
        currentPage = 1;
        updateTableView();
        updatePaginationControls();
    }

    private void goToPreviousPage() {
        if (currentPage > 1) {
            currentPage--;
            updateTableView();
            updatePaginationControls();
        }
    }

    private void goToNextPage() {
        int totalPages = (int) Math.ceil((double) filteredUsers.size() / pageSize);
        if (currentPage < totalPages) {
            currentPage++;
            updateTableView();
            updatePaginationControls();
        }
    }

    private void goToLastPage() {
        int totalPages = (int) Math.ceil((double) filteredUsers.size() / pageSize);
        currentPage = totalPages;
        updateTableView();
        updatePaginationControls();
    }

    private void goToPage() {
        try {
            int page = Integer.parseInt(pageNumberField.getText());
            int totalPages = (int) Math.ceil((double) filteredUsers.size() / pageSize);
            if (page >= 1 && page <= totalPages) {
                currentPage = page;
                updateTableView();
                updatePaginationControls();
            } else {
                pageNumberField.setText(String.valueOf(currentPage));
            }
        } catch (NumberFormatException e) {
            pageNumberField.setText(String.valueOf(currentPage));
        }
    }

    // ==================== BULK ACTION METHODS ====================

    private void handleBulkActivate() {
        List<User> selectedUsers = getSelectedUsers();
        if (selectedUsers.isEmpty()) return;

        // TODO: Implement bulk activation via UserService
        showAlert("Info", "Bulk Activate", "Bulk activation not yet implemented");
    }

    private void handleBulkDeactivate() {
        List<User> selectedUsers = getSelectedUsers();
        if (selectedUsers.isEmpty()) return;

        // TODO: Implement bulk deactivation via UserService
        showAlert("Info", "Bulk Deactivate", "Bulk deactivation not yet implemented");
    }

    private void handleBulkDelete() {
        List<User> selectedUsers = getSelectedUsers();
        if (selectedUsers.isEmpty()) return;

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Bulk Delete");
        confirmation.setHeaderText("Delete " + selectedUsers.size() + " users?");
        confirmation.setContentText("This action cannot be undone.");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // TODO: Implement bulk delete via UserService
                showAlert("Info", "Bulk Delete", "Bulk delete not yet implemented");
            }
        });
    }

    private void handleBulkVerify() {
        List<User> selectedUsers = getSelectedUsers();
        if (selectedUsers.isEmpty()) return;

        // TODO: Implement bulk verification via UserService
        showAlert("Info", "Bulk Verify", "Bulk verification not yet implemented");
    }

    private List<User> getSelectedUsers() {
        return usersTable.getItems().stream()
            .filter(UserTableItem::isSelected)
            .map(UserTableItem::getUser)
            .collect(Collectors.toList());
    }

    // ==================== USER DETAILS METHODS ====================

    private void showUserDetails(User user) {
        userDetailsPanel.setUserData(user);
        detailFullNameLabel.setText(user.getFirstName() + " " + user.getLastName());
        detailEmailLabel.setText(user.getEmail());
        detailRoleLabel.setText(getRoleDisplayName(user.getRole()));
        detailStatusLabel.setText(getStatusDisplayText(user));
        detailPhoneLabel.setText(user.getPhone() != null ? user.getPhone() : "Not provided");
        detailBirthDateLabel.setText(user.getBirthdate() != null ?
            user.getBirthdate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) : "Not provided");
        detailLicenseLabel.setText(user.getLicenseNumber() != null ? user.getLicenseNumber() : "Not provided");
        detailSpecialtyLabel.setText(user.getSpecialite() != null ? user.getSpecialite() : "Not provided");
        detailExperienceLabel.setText(user.getYearsOfExperience() > 0 ?
            user.getYearsOfExperience() + " years" : "Not specified");
        detailRegistrationDateLabel.setText(user.getCreatedAt() != null ?
            user.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")) : "Unknown");

        // Show diploma button only for professionals
        viewDiplomaBtn.setVisible(user.getDiplomaUrl() != null &&
            !user.getDiplomaUrl().isEmpty() &&
            !"ROLE_PATIENT".equals(user.getRole()));

        userDetailsPanel.setVisible(true);
        userDetailsPanel.setManaged(true);
    }

    private void hideUserDetails() {
        userDetailsPanel.setVisible(false);
        userDetailsPanel.setManaged(false);
    }

    private String getRoleDisplayName(String role) {
        switch (role) {
            case "ROLE_PATIENT": return "Patient";
            case "ROLE_MEDECIN": return "Physician";
            case "ROLE_COACH": return "Coach";
            case "ROLE_NUTRITIONIST": return "Nutritionist";
            case "ROLE_ADMIN": return "Administrator";
            default: return role;
        }
    }

    private String getStatusDisplayText(User user) {
        if (!user.isActive()) return "Inactive";
        if (!user.isVerifiedByAdmin()) return "Pending Verification";
        return "Active";
    }


    @FXML
    private void handleProfile() {
        // TODO: Navigate to profile
        showAlert("Info", "Profile", "Profile management coming soon!");
    }

    @FXML
    private void handleSettings() {
        // TODO: Navigate to settings
        showAlert("Info", "Settings", "Settings management coming soon!");
    }

    @FXML
    private void handleLogout() {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Logout Confirmation");
        confirmation.setHeaderText("Are you sure you want to logout?");
        confirmation.setContentText("You will be redirected to the login screen.");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                SceneManager.getInstance().logout();
            }
        });
    }

    // ==================== INTERFACE IMPLEMENTATIONS ====================

    @Override
    public void setUserService(UserService userService) {
        this.userService = userService;
        if (this.userService != null) {
            loadUsers(); // Load data once the service is available
        }
    }

    @Override
    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (userMenu != null) {
            userMenu.setText(currentUser.getFirstName() + " " + currentUser.getLastName());
        }
    }
}