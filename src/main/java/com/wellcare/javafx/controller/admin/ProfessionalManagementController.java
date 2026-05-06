package com.wellcare.javafx.controller.admin;

import com.wellcare.javafx.model.User;
import com.wellcare.javafx.service.UserService;
import com.wellcare.javafx.util.SceneManager;
import com.wellcare.javafx.util.SceneManager.ServiceAware;
import com.wellcare.javafx.util.SceneManager.UserAware;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.beans.property.SimpleBooleanProperty;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.awt.Desktop;
import java.io.File;

public class ProfessionalManagementController implements Initializable, ServiceAware, UserAware {

    private UserService userService;
    private User currentUser;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> roleFilter;
    @FXML private ComboBox<String> statusFilter;
    @FXML private ComboBox<String> specialtyFilter;

    @FXML private Label totalProfessionalsLabel;
    @FXML private Label verifiedProfessionalsLabel;
    @FXML private Label pendingVerificationLabel;
    @FXML private Label activeProfessionalsLabel;

    @FXML private TableView<ProfessionalTableItem> professionalsTable;
    @FXML private TableColumn<ProfessionalTableItem, Boolean> checkboxColumn;
    @FXML private TableColumn<ProfessionalTableItem, String> nameColumn;
    @FXML private TableColumn<ProfessionalTableItem, String> emailColumn;
    @FXML private TableColumn<ProfessionalTableItem, String> roleSpecialtyColumn;
    @FXML private TableColumn<ProfessionalTableItem, String> licenseColumn;
    @FXML private TableColumn<ProfessionalTableItem, Integer> experienceColumn;
    @FXML private TableColumn<ProfessionalTableItem, String> statusColumn;
    @FXML private TableColumn<ProfessionalTableItem, Void> actionsColumn;

    @FXML private Button verifySelectedBtn;
    @FXML private Button rejectSelectedBtn;
    @FXML private Button backToDashboardBtn;

    private ObservableList<ProfessionalTableItem> professionalsData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Force the table to automatically resize columns to occupy empty grey space
        professionalsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);


        setupTableColumns();
        setupFilters();
        setupEventHandlers();
    }

    private void setupTableColumns() {
        checkboxColumn.setCellValueFactory(cellData -> cellData.getValue().selectedProperty());
        checkboxColumn.setCellFactory(javafx.scene.control.cell.CheckBoxTableCell.forTableColumn(checkboxColumn));
        checkboxColumn.setStyle("-fx-alignment: CENTER;");

        createTextColumn(nameColumn, ProfessionalTableItem::getFullName, javafx.geometry.Pos.CENTER_LEFT);
        createTextColumn(emailColumn, ProfessionalTableItem::getEmail, javafx.geometry.Pos.CENTER_LEFT);
        createTextColumn(roleSpecialtyColumn, item -> item.getRoleDisplay() + " (" + item.getFormattedSpecialty() + ")", javafx.geometry.Pos.CENTER_LEFT);
        createTextColumn(licenseColumn, ProfessionalTableItem::getLicenseNumber, javafx.geometry.Pos.CENTER);
        createTextColumn(experienceColumn, item -> item.getYearsOfExperience() != null ? item.getYearsOfExperience() : 0, javafx.geometry.Pos.CENTER);
        
        statusColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getUnifiedStatus()));
        statusColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                } else {
                    Label label = new Label(status);
                    label.setStyle("-fx-font-weight: bold; -fx-padding: 3 8 3 8; -fx-background-radius: 12;");
                    if (status.equals("Verified")) {
                        label.setStyle(label.getStyle() + "-fx-background-color: #E8F5E9; -fx-text-fill: #2E7D32;");
                    } else if (status.equals("Pending")) {
                        label.setStyle(label.getStyle() + "-fx-background-color: #FFF3E0; -fx-text-fill: #E65100;");
                    } else if (status.equals("Inactive")) {
                        label.setStyle(label.getStyle() + "-fx-background-color: #FFEBEE; -fx-text-fill: #C62828;");
                    }
                    HBox box = new HBox(label);
                    box.setAlignment(javafx.geometry.Pos.CENTER);
                    setGraphic(box);
                }
            }
        });

        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button viewBtn = new Button("View");
            private final Button editBtn = new Button("Edit");
            private final Button verifyBtn = new Button("Verify");
            private final Button rejectRowBtn = new Button("Reject");
            private final HBox buttons = new HBox(3, viewBtn, editBtn, verifyBtn, rejectRowBtn);

            {
                viewBtn.getStyleClass().addAll("table-action-btn", "view-btn");
                editBtn.getStyleClass().addAll("table-action-btn", "edit-btn");
                verifyBtn.getStyleClass().addAll("table-action-btn", "verify-btn");
                rejectRowBtn.getStyleClass().addAll("table-action-btn", "reject-btn");

                viewBtn.setOnAction(event -> {
                    ProfessionalTableItem item = getTableView().getItems().get(getIndex());
                    viewProfessionalDetails(item);
                });

                editBtn.setOnAction(event -> {
                    ProfessionalTableItem item = getTableView().getItems().get(getIndex());
                    editProfessional(item);
                });

                verifyBtn.setOnAction(event -> {
                    ProfessionalTableItem item = getTableView().getItems().get(getIndex());
                    verifyProfessional(item);
                });

                rejectRowBtn.setOnAction(event -> {
                    ProfessionalTableItem item = getTableView().getItems().get(getIndex());
                    rejectProfessional(item);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    ProfessionalTableItem professional = getTableView().getItems().get(getIndex());
                    buttons.setAlignment(javafx.geometry.Pos.CENTER);
                    if (!professional.isVerified() && !"Inactive".equals(professional.getUnifiedStatus())) {
                        buttons.getChildren().setAll(viewBtn, editBtn, verifyBtn, rejectRowBtn);
                    } else {
                        buttons.getChildren().setAll(viewBtn, editBtn);
                    }
                    setGraphic(buttons);
                    setAlignment(javafx.geometry.Pos.CENTER);
                }
            }
        });

        // Bind table to data
        professionalsTable.setItems(professionalsData);
    }

    private <T> void createTextColumn(TableColumn<ProfessionalTableItem, T> column, java.util.function.Function<ProfessionalTableItem, T> extractor, javafx.geometry.Pos alignment) {
        column.setCellValueFactory(cellData -> {
            T value = extractor.apply(cellData.getValue());
            if (value instanceof String) {
                return (javafx.beans.value.ObservableValue) new javafx.beans.property.SimpleStringProperty((String) value);
            } else if (value instanceof Integer) {
                return (javafx.beans.value.ObservableValue) new javafx.beans.property.SimpleObjectProperty<>(value);
            }
            return new javafx.beans.property.SimpleObjectProperty<>(value);
        });

        column.setCellFactory(col -> new TableCell<ProfessionalTableItem, T>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label label = new Label(String.valueOf(item));
                    label.setStyle("-fx-text-fill: #1F2937 !important; -fx-font-size: 13px !important;");
                    label.setMaxWidth(Double.MAX_VALUE);
                    label.setAlignment(alignment);
                    setGraphic(label);
                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    setText(null);
                }
            }
        });
    }

    private void setupFilters() {
        roleFilter.setItems(FXCollections.observableArrayList(
            "All Professionals", "Doctor", "Coach", "Nutritionist"));
        statusFilter.setItems(FXCollections.observableArrayList(
            "All Status", "Verified", "Pending", "Rejected"));
        specialtyFilter.setItems(FXCollections.observableArrayList(
            "All Specialties", "Cardiology", "Dermatology", "Fitness Coaching",
            "Clinical Nutrition", "Other"));

        roleFilter.setValue("All Professionals");
        statusFilter.setValue("All Status");
        specialtyFilter.setValue("All Specialties");
    }

    private void setupEventHandlers() {
        // Live search filter
        searchField.textProperty().addListener((obs, oldV, newV) -> applyFilters());

        // Dropdown filters
        roleFilter.setOnAction(e -> applyFilters());
        statusFilter.setOnAction(e -> applyFilters());
        specialtyFilter.setOnAction(e -> applyFilters());

        backToDashboardBtn.setOnAction(e -> SceneManager.getInstance().switchTo(SceneManager.ADMIN_DASHBOARD));

        verifySelectedBtn.setOnAction(e -> verifySelectedProfessionals());
        rejectSelectedBtn.setOnAction(e -> rejectSelectedProfessionals());
    }

    private void applyFilters() {
        String searchText = searchField.getText() == null ? "" : searchField.getText().toLowerCase();
        String roleVal = roleFilter.getValue();
        String statusVal = statusFilter.getValue();

        ObservableList<ProfessionalTableItem> filtered = FXCollections.observableArrayList();
        for (ProfessionalTableItem item : professionalsData) {
            // Search filter
            boolean matchesSearch = searchText.isEmpty() ||
                    item.getFullName().toLowerCase().contains(searchText) ||
                    item.getEmail().toLowerCase().contains(searchText);

            // Role filter
            boolean matchesRole = roleVal == null || "All Professionals".equals(roleVal) ||
                    item.getRoleDisplay().equals(roleVal);

            // Status filter
            boolean matchesStatus = statusVal == null || "All Status".equals(statusVal) ||
                    item.getUnifiedStatus().equals(statusVal);

            if (matchesSearch && matchesRole && matchesStatus) {
                filtered.add(item);
            }
        }
        professionalsTable.setItems(filtered);
    }

    private void updateBulkActionButtons() {
        boolean anySelected = professionalsData.stream().anyMatch(ProfessionalTableItem::isSelected);
        verifySelectedBtn.setDisable(!anySelected);
        rejectSelectedBtn.setDisable(!anySelected);
    }

    private void verifySelectedProfessionals() {
        long count = professionalsData.stream().filter(ProfessionalTableItem::isSelected).count();
        if (count == 0) return;

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Bulk Verify");
        confirmation.setHeaderText("Verify " + count + " professional(s)?");
        confirmation.setContentText("This will mark selected professionals as verified.");
        confirmation.showAndWait().ifPresent(res -> {
           if (res == ButtonType.OK) {
               try {
                   for (ProfessionalTableItem item : professionalsData) {
                       if (item.isSelected() && !item.isVerified()) {
                           userService.verifyProfessional(item.getUuid());
                       }
                   }
                   showAlert("Success", "Bulk Verification Complete", count + " professional(s) verified.");
                   loadProfessionalsData();
               } catch (Exception e) {
                   showAlert("Error", "Bulk action failed", e.getMessage());
               }
           }
        });
    }

    private void rejectSelectedProfessionals() {
        long count = professionalsData.stream().filter(ProfessionalTableItem::isSelected).count();
        if (count == 0) return;
        
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Bulk Reject");
        dialog.setHeaderText("Reject " + count + " professional(s)");
        dialog.setContentText("Reason for rejection:");
        dialog.showAndWait().ifPresent(reason -> {
            if (!reason.trim().isEmpty()) {
                try {
                    for (ProfessionalTableItem item : professionalsData) {
                        if (item.isSelected() && !item.isVerified()) {
                            userService.unverifyProfessional(item.getUuid());
                        }
                    }
                    showAlert("Success", "Bulk Rejection Complete", count + " professional(s) rejected.");
                    loadProfessionalsData();
                } catch (Exception e) {
                    showAlert("Error", "Bulk action failed", e.getMessage());
                }
            }
        });
    }

    private void loadProfessionalsData() {
        if (userService == null) return;

        try {
            List<User> allUsers = userService.getAllUsers();

            // All UI updates must be on the FX thread
            Platform.runLater(() -> {
                professionalsData.clear();

                int totalProfessionals = 0;
                int verifiedCount = 0;
                int pendingCount = 0;
                int activeCount = 0;

                for (User user : allUsers) {
                    if (isProfessionalRole(user.getRole())) {
                        totalProfessionals++;

                        if (user.isVerifiedByAdmin()) {
                            verifiedCount++;
                        } else {
                            pendingCount++;
                        }

                        if (user.isActive()) {
                            activeCount++;
                        }

                        ProfessionalTableItem item = new ProfessionalTableItem(user);
                        item.selectedProperty().addListener((obs, oldV, newV) -> updateBulkActionButtons());
                        System.out.println("Adding professional: " + item.getFullName());
                        professionalsData.add(item);
                    }
                }

                updateBulkActionButtons();

                totalProfessionalsLabel.setText(String.valueOf(totalProfessionals));
                verifiedProfessionalsLabel.setText(String.valueOf(verifiedCount));
                pendingVerificationLabel.setText(String.valueOf(pendingCount));
                activeProfessionalsLabel.setText(String.valueOf(activeCount));

                professionalsTable.refresh();

                System.out.println("Loaded " + totalProfessionals + " professionals into table");
            });

        } catch (Exception e) {
            Platform.runLater(() -> showAlert("Error", "Failed to load professionals", "Failed to load data: " + e.getMessage()));
        }
    }

    private boolean isProfessionalRole(String role) {
        return "ROLE_MEDECIN".equals(role) ||
               "ROLE_COACH".equals(role) ||
               "ROLE_NUTRITIONIST".equals(role);
    }

    private void viewProfessionalDetails(ProfessionalTableItem item) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Professional Details");
        alert.setHeaderText(item.getFullName() + " - " + item.getRoleDisplay());
        
        StringBuilder details = new StringBuilder();
        details.append("Email: ").append(item.getEmail()).append("\n");
        details.append("Specialty: ").append(item.getFormattedSpecialty()).append("\n");
        details.append("License: ").append(item.getLicenseNumber()).append("\n");
        details.append("Experience: ").append(item.getYearsOfExperience()).append(" years\n");
        details.append("Status: ").append(item.getUnifiedStatus()).append("\n");
        
        alert.setContentText(details.toString());

        // Add View Diploma button to the alert result if it exists
        ButtonType viewDiplomaBtn = new ButtonType("📜 View Diploma");
        ButtonType closeBtn = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        
        if (item.getUser().getDiplomaUrl() != null && !item.getUser().getDiplomaUrl().isEmpty()) {
            alert.getButtonTypes().setAll(viewDiplomaBtn, closeBtn);
        }

        alert.showAndWait().ifPresent(res -> {
            if (res == viewDiplomaBtn) {
                handleViewDiploma(item.getUser());
            }
        });
    }

    private void handleViewDiploma(User user) {
        if (user.getDiplomaUrl() == null || user.getDiplomaUrl().isEmpty()) {
            showAlert("Info", "No Diploma", "This professional has no diploma on file.");
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

    private void editProfessional(ProfessionalTableItem item) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/admin-edit-user.fxml"));
            Parent root = loader.load();
            
            AdminEditUserController controller = loader.getController();
            controller.setUser(item.getUser(), userService);
            
            Stage stage = new Stage();
            stage.setTitle("Edit Professional - " + item.getFullName());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            
            if (controller.isSaved()) {
                loadProfessionalsData(); // Refresh table
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Could not open edit dialog", e.getMessage());
        }
    }

    private void verifyProfessional(ProfessionalTableItem item) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Verify Professional");
        confirmation.setHeaderText("Verify " + item.getFullName() + "?");
        confirmation.setContentText("This will mark the professional as verified and allow them to access the platform.");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    userService.verifyProfessional(item.getUuid());
                    showAlert("Success", "Professional Verified", item.getFullName() + " has been verified successfully!");
                    loadProfessionalsData();
                } catch (Exception e) {
                    showAlert("Error", "Verification Failed", e.getMessage());
                }
            }
        });
    }

    private void rejectProfessional(ProfessionalTableItem item) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Reject Professional");
        dialog.setHeaderText("Reject " + item.getFullName());
        dialog.setContentText("Reason for rejection:");

        dialog.showAndWait().ifPresent(reason -> {
            if (!reason.trim().isEmpty()) {
                try {
                    userService.unverifyProfessional(item.getUuid());
                    showAlert("Success", "Professional Rejected", item.getFullName() + " has been rejected.\nReason: " + reason);
                    loadProfessionalsData();
                } catch (Exception e) {
                    showAlert("Error", "Rejection Failed", e.getMessage());
                }
            }
        });
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
        if (this.userService != null) {
            loadProfessionalsData();
        }
    }

    @Override
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public static class ProfessionalTableItem {
        private final User user;
        private final SimpleBooleanProperty selected = new SimpleBooleanProperty(false);

        public ProfessionalTableItem(User user) {
            this.user = user;
        }

        public User getUser() { return user; }
        public SimpleBooleanProperty selectedProperty() { return selected; }
        public boolean isSelected() { return selected.get(); }
        public void setSelected(boolean sel) { selected.set(sel); }

        public String getUuid() { return user.getUuid(); }
        public String getFullName() { return user.getFirstName() + " " + user.getLastName(); }
        public String getEmail() { return user.getEmail(); }
        public String getRole() { return user.getRole(); }
        public String getRoleDisplay() {
            return switch (user.getRole()) {
                case "ROLE_MEDECIN" -> "Doctor";
                case "ROLE_COACH" -> "Coach";
                case "ROLE_NUTRITIONIST" -> "Nutritionist";
                default -> user.getRole();
            };
        }
        public String getSpecialty() { return user.getSpecialty(); }
        public String getFormattedSpecialty() {
            String raw = user.getSpecialty();
            if (raw == null || raw.isEmpty()) return "General";
            // Convert WEIGHT_LOSS_SPECIALIST -> Weight Loss Specialist
            String[] parts = raw.replace("_", " ").toLowerCase().split(" ");
            StringBuilder sb = new StringBuilder();
            for (String part : parts) {
                if (!part.isEmpty()) {
                    sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1)).append(" ");
                }
            }
            return sb.toString().trim();
        }
        public String getLicenseNumber() { return user.getLicenseNumber(); }
        public Integer getYearsOfExperience() { return user.getYearsOfExperience(); }
        public String getStatus() { return user.isActive() ? "Active" : "Inactive"; }
        public String getUnifiedStatus() {
            if (!user.isActive()) return "Inactive";
            return user.isVerifiedByAdmin() ? "Verified" : "Pending";
        }
        public boolean isVerified() { return user.isVerifiedByAdmin(); }
    }
}