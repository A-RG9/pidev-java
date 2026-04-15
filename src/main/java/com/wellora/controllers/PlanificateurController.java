package com.wellora.controllers;

import com.wellora.dao.MealPlanDAO;
import com.wellora.models.MealPlan;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.function.UnaryOperator;
import com.wellora.utils.ThemeManager;

public class PlanificateurController {

    @FXML private BorderPane rootPane;
    @FXML private ToggleButton btnThemeToggle;
    @FXML private DatePicker datePickerPlan;
    @FXML private TableView<MealPlan> tablePlan;
    @FXML private TableColumn<MealPlan, String> colType, colNom;
    @FXML private TableColumn<MealPlan, Integer> colCalories;
    @FXML private TableColumn<MealPlan, Boolean> colStatut;
    @FXML private Button btnAjouter, btnTerminer, btnSupprimer;

    private final MealPlanDAO dao = new MealPlanDAO();

    // Identifiants
    private final String currentUserUuid = "c513e200-d605-4f61-9efc-d539f0e80914";
    private final int currentUserId = 1;

    private ObservableList<MealPlan> planList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        if (ThemeManager.isDarkMode) {
            btnThemeToggle.setSelected(true);
            btnThemeToggle.setText("☀️ Mode Clair");
            if (!rootPane.getStyleClass().contains("light-theme")) {
                rootPane.getStyleClass().add("light-theme");
            }
        } else {
            btnThemeToggle.setSelected(false);
            btnThemeToggle.setText("🌙 Mode Sombre");
            rootPane.getStyleClass().remove("light-theme");
        }

        // --- LIAISON DES COLONNES ---
        colType.setCellValueFactory(new PropertyValueFactory<>("mealType"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCalories.setCellValueFactory(new PropertyValueFactory<>("calories"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("completed"));

        // --- NOUVEAU : PERSONNALISATION DE LA COLONNE STATUT ---
        colStatut.setCellFactory(column -> new TableCell<MealPlan, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    if (item) {
                        setText("✅ Terminé");
                        setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold;"); // Vert
                    } else {
                        setText("⏳ En cours");
                        setStyle("-fx-text-fill: #F59E0B; -fx-font-weight: bold;"); // Orange
                    }
                }
            }
        });

        tablePlan.setItems(planList);

        // Date d'aujourd'hui par défaut
        datePickerPlan.setValue(LocalDate.now());
        datePickerPlan.setOnAction(e -> refreshData());

        // Gestion de la sélection du tableau
        tablePlan.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean hasSelection = newSelection != null;
            btnTerminer.setDisable(!hasSelection);
            btnSupprimer.setDisable(!hasSelection);
            if (hasSelection) {
                btnTerminer.setText(newSelection.isCompleted() ? "Marquer Non Terminé" : "Marquer Terminé");
            }
        });

        // Actions des boutons
        btnAjouter.setOnAction(e -> openAddDialog());
        btnTerminer.setOnAction(e -> toggleStatus());
        btnSupprimer.setOnAction(e -> deleteSelectedPlan());

        refreshData();
    }

    private void refreshData() {
        LocalDate selectedDate = datePickerPlan.getValue();
        if (selectedDate != null) {
            planList.setAll(dao.getPlansByDate(currentUserUuid, selectedDate));
        }
    }

    // --- NOUVEAU FORMULAIRE MODERNISÉ ---
    private void openAddDialog() {
        Dialog<MealPlan> dialog = new Dialog<>();
        dialog.setHeaderText(null);

        DialogPane dialogPane = dialog.getDialogPane();
        try {
            dialogPane.getStylesheets().add(getClass().getResource("/com/wellora/css/style.css").toExternalForm());
            dialogPane.getStyleClass().add("card");

            if (ThemeManager.isDarkMode) {
                dialogPane.getStyleClass().add("light-theme");
                dialogPane.setStyle("-fx-background-color: #FFFFFF; -fx-font-family: 'Segoe UI', sans-serif;");
            } else {
                dialogPane.setStyle("-fx-background-color: #1F2937; -fx-font-family: 'Segoe UI', sans-serif;");
            }
        } catch (Exception e) {
            System.out.println("CSS non trouvé pour le dialogue.");
        }

        ButtonType btnValider = new ButtonType("Ajouter", ButtonBar.ButtonData.OK_DONE);
        dialogPane.getButtonTypes().addAll(btnValider, ButtonType.CANCEL);

        Button btOk = (Button) dialogPane.lookupButton(btnValider);
        btOk.getStyleClass().add("primary-button");
        Button btCancel = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
        btCancel.getStyleClass().add("secondary-button");

        // --- CHAMPS DU FORMULAIRE ---
        String fieldStyle = "-fx-padding: 8; -fx-background-radius: 5;";

        TextField nameField = new TextField();
        nameField.setPromptText("Ex: Salade César, Poulet rôti...");
        nameField.setStyle(fieldStyle);

        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll("Breakfast", "Lunch", "Dinner", "Snack");
        typeBox.setValue("Lunch");
        typeBox.setMaxWidth(Double.MAX_VALUE);
        typeBox.setStyle(fieldStyle);

        TextField calField = new TextField();
        calField.setPromptText("Ex: 450");
        calField.setStyle(fieldStyle);

        // Filtre pour chiffres uniquement
        UnaryOperator<TextFormatter.Change> intFilter = change -> change.getControlNewText().matches("\\d*") ? change : null;
        calField.setTextFormatter(new TextFormatter<>(intFilter));

        // --- STRUCTURE DU LAYOUT ---
        Label titleLabel = new Label("🍽️ Planifier un repas");
        titleLabel.getStyleClass().add("main-title");
        titleLabel.setStyle("-fx-font-size: 18px;");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #EF4444; -fx-font-size: 13px; -fx-font-weight: bold;");
        errorLabel.setWrapText(true);

        VBox nameGroup = new VBox(5, new Label("📝 Nom du plat"), nameField);
        VBox typeGroup = new VBox(5, new Label("📌 Moment de la journée"), typeBox);
        VBox calGroup = new VBox(5, new Label("🔥 Calories estimées"), calField);

        HBox splitBox = new HBox(15, typeGroup, calGroup);
        HBox.setHgrow(typeGroup, Priority.ALWAYS);
        HBox.setHgrow(calGroup, Priority.ALWAYS);

        VBox contentContainer = new VBox(15);
        contentContainer.setStyle("-fx-background-color: transparent;");
        contentContainer.setPadding(new Insets(20, 30, 10, 30));
        contentContainer.getChildren().addAll(titleLabel, errorLabel, nameGroup, splitBox);

        dialogPane.setContent(contentContainer);

        // --- VALIDATION ET ERREURS IN-LINE ---
        btOk.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            errorLabel.setText("");

            if (nameField.getText().trim().isEmpty() || calField.getText().trim().isEmpty()) {
                errorLabel.setText("❌ Veuillez remplir le nom et les calories.");
                event.consume();
                return;
            }

            try {
                int calories = Integer.parseInt(calField.getText().trim());
                if (calories <= 0 || calories > 3000) {
                    errorLabel.setText("❌ Les calories doivent être entre 1 et 3000.");
                    event.consume();
                }
            } catch (NumberFormatException ex) {
                errorLabel.setText("❌ Format de calories invalide.");
                event.consume();
            }
        });

        dialog.setResultConverter(button -> {
            if (button == btnValider) {
                LocalDate selectedDate = datePickerPlan.getValue();
                return new MealPlan(
                        0,
                        currentUserId,
                        currentUserUuid,
                        selectedDate,
                        selectedDate.getDayOfWeek().name(),
                        typeBox.getValue(),
                        nameField.getText().trim(),
                        Integer.parseInt(calField.getText().trim()),
                        false
                );
            }
            return null;
        });

        dialog.showAndWait().ifPresent(plan -> {
            if (dao.addPlan(plan)) {
                refreshData();
            } else {
                showCustomAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ajout en base de données.");
            }
        });
    }

    private void toggleStatus() {
        MealPlan selected = tablePlan.getSelectionModel().getSelectedItem();
        if (selected != null) {
            boolean newStatus = !selected.isCompleted();
            if (dao.toggleCompletion(selected.getId(), newStatus)) {
                refreshData();
            }
        }
    }

    private void deleteSelectedPlan() {
        MealPlan selected = tablePlan.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        showCustomAlert(Alert.AlertType.CONFIRMATION, "Confirmation", "Supprimer ce plat planifié ?", ButtonType.YES, ButtonType.NO)
                .ifPresent(response -> {
                    if (response == ButtonType.YES && dao.deletePlan(selected.getId())) {
                        refreshData();
                    }
                });
    }

    // --- BOÎTES DE DIALOGUE MODERNES ---
    private Optional<ButtonType> showCustomAlert(Alert.AlertType type, String title, String message, ButtonType... buttonTypes) {
        Alert alert;
        if (buttonTypes.length > 0) {
            alert = new Alert(type, message, buttonTypes);
        } else {
            alert = new Alert(type, message);
        }

        alert.setTitle(title);
        alert.setHeaderText(null);

        DialogPane dialogPane = alert.getDialogPane();
        try {
            dialogPane.getStylesheets().add(getClass().getResource("/com/wellora/css/style.css").toExternalForm());
            dialogPane.getStyleClass().add("card");

            if (ThemeManager.isDarkMode) {
                dialogPane.getStyleClass().add("light-theme");
                dialogPane.setStyle("-fx-background-color: #FFFFFF; -fx-font-family: 'Segoe UI', sans-serif;");
            } else {
                dialogPane.setStyle("-fx-background-color: #1F2937; -fx-font-family: 'Segoe UI', sans-serif;");
                javafx.scene.Node content = dialogPane.lookup(".content.label");
                if (content != null) content.setStyle("-fx-text-fill: white;");
            }
        } catch (Exception e) {
            System.out.println("CSS non trouvé pour l'alerte.");
        }

        for (ButtonType btnType : dialogPane.getButtonTypes()) {
            Button btn = (Button) dialogPane.lookupButton(btnType);
            if (btn != null) {
                if (btnType == ButtonType.OK || btnType == ButtonType.YES) {
                    btn.getStyleClass().add("primary-button");
                } else if (btnType == ButtonType.NO || btnType == ButtonType.CANCEL) {
                    btn.getStyleClass().add("secondary-button");
                }
            }
        }

        return alert.showAndWait();
    }

    // -- NAVIGATION --
    @FXML public void navToDashboard(ActionEvent event) { switchScene(event, "Dashboard.fxml"); }
    @FXML public void navToObjectifs(ActionEvent event) { switchScene(event, "Objectif.fxml"); }
    @FXML public void navToJournal(ActionEvent event) { switchScene(event, "Journal.fxml"); }
    @FXML public void navToPlanificateur(ActionEvent event) { /* On est déjà sur la page */ }
    @FXML public void navToAnalyse(ActionEvent event) { switchScene(event, "Analyse.fxml"); }

    private void switchScene(ActionEvent event, String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/wellora/views/" + fxmlFile));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root, 1200, 800);
            scene.getStylesheets().add(getClass().getResource("/com/wellora/css/style.css").toExternalForm());
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void toggleTheme() {
        ThemeManager.isDarkMode = btnThemeToggle.isSelected();
        if (ThemeManager.isDarkMode) {
            btnThemeToggle.setText("☀️ Mode Clair");
            if (!rootPane.getStyleClass().contains("light-theme")) {
                rootPane.getStyleClass().add("light-theme");
            }
        } else {
            btnThemeToggle.setText("🌙 Mode Sombre");
            rootPane.getStyleClass().remove("light-theme");
        }
    }
}