package com.wellora.controllers;


import com.wellora.dao.MealPlanDAO;
import com.wellora.models.MealPlan;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

// --- LES IMPORTS MANQUANTS À AJOUTER SONT LÀ ---
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.time.LocalDate;
import java.util.function.UnaryOperator;
import com.wellora.utils.ThemeManager;

public class PlanificateurController {

    @FXML private DatePicker datePickerPlan;
    @FXML private TableView<MealPlan> tablePlan;
    @FXML private TableColumn<MealPlan, String> colType, colNom;
    @FXML private TableColumn<MealPlan, Integer> colCalories;
    @FXML private TableColumn<MealPlan, Boolean> colStatut;
    @FXML private Button btnAjouter, btnTerminer, btnSupprimer;
    @FXML private javafx.scene.layout.BorderPane rootPane;
    @FXML private ToggleButton btnThemeToggle;

    private final MealPlanDAO dao = new MealPlanDAO();

    // Vos identifiants de test basés sur la BDD
    private final String currentUserUuid = "c513e200-d605-4f61-9efc-d539f0e80914";
    private final int currentUserId = 1; // Un identifiant entier par défaut, car user_id est NOT NULL dans meal_plans

    @FXML public void navToAnalyse(ActionEvent event) { switchScene(event, "Analyse.fxml"); }
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
        // Liaison des colonnes
        colType.setCellValueFactory(new PropertyValueFactory<>("mealType"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("name")); // Correspond au getter getName()
        colCalories.setCellValueFactory(new PropertyValueFactory<>("calories"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("completed"));

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

    private void openAddDialog() {
        Dialog<MealPlan> dialog = new Dialog<>();
        dialog.setTitle("Planifier un repas");
        ButtonType btnValider = new ButtonType("Ajouter", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnValider, ButtonType.CANCEL);

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10); grid.setVgap(10);

        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll("Breakfast", "Lunch", "Dinner", "Snack");
        typeBox.setValue("Lunch");

        TextField nameField = new TextField();
        TextField calField = new TextField();

        // Filtre pour n'accepter que des chiffres dans les calories
        UnaryOperator<TextFormatter.Change> intFilter = change -> change.getControlNewText().matches("\\d*") ? change : null;
        calField.setTextFormatter(new TextFormatter<>(intFilter));

        grid.add(new Label("Type de repas :"), 0, 0); grid.add(typeBox, 1, 0);
        grid.add(new Label("Nom du plat :"), 0, 1); grid.add(nameField, 1, 1);
        grid.add(new Label("Calories :"), 0, 2); grid.add(calField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(button -> {
            if (button == btnValider && !nameField.getText().trim().isEmpty() && !calField.getText().trim().isEmpty()) {
                LocalDate selectedDate = datePickerPlan.getValue();
                String dayOfWeekName = selectedDate.getDayOfWeek().name(); // Ex: "MONDAY"

                return new MealPlan(
                        0, // ID autogénéré
                        currentUserId,
                        currentUserUuid,
                        selectedDate,
                        dayOfWeekName,
                        typeBox.getValue(),
                        nameField.getText().trim(),
                        Integer.parseInt(calField.getText().trim()),
                        false // isCompleted = false
                );
            }
            return null;
        });

        dialog.showAndWait().ifPresent(plan -> {
            if (dao.addPlan(plan)) {
                refreshData();
            } else {
                new Alert(Alert.AlertType.ERROR, "Erreur lors de l'ajout en base de données.").showAndWait();
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

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer ce plat planifié ?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES && dao.deletePlan(selected.getId())) {
                refreshData();
            }
        });
    }
    // -- NAVIGATION --
    @FXML public void navToDashboard(ActionEvent event) { switchScene(event, "Dashboard.fxml"); }
    @FXML public void navToObjectifs(ActionEvent event) { switchScene(event, "Objectif.fxml"); }
    @FXML public void navToJournal(ActionEvent event) { switchScene(event, "Journal.fxml"); }
    @FXML public void navToPlanificateur(ActionEvent event) { /* On est déjà sur la page */ }

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

