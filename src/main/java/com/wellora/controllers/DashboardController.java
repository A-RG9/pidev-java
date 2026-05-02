package com.wellora.controllers;

import javafx.scene.control.TextFormatter;
import java.util.function.UnaryOperator;
import java.util.Optional; // Ajouté pour les alertes
import com.wellora.dao.FoodLogDAO;
import com.wellora.dao.NutritionGoalDAO;
import com.wellora.models.FoodLog;
import com.wellora.models.NutritionGoal;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import com.wellora.utils.ThemeManager;

// CONTROLLER LAYER: Manages user interaction, UI updates, and talks to the DAO
public class DashboardController extends BaseController {

    @FXML private BorderPane rootPane;
    @FXML private ToggleButton btnThemeToggle;
    @FXML private Label lblCalories, lblProteines, lblGlucides, lblLipides;
    @FXML private Button btnAjoutRapide, btnModifier, btnSupprimer;
    @FXML private TableView<FoodLog> tableRepas;
    @FXML private TableColumn<FoodLog, String> colRepas;
    @FXML private TableColumn<FoodLog, Integer> colCalories;
    @FXML private TableColumn<FoodLog, Double> colProteines, colGlucides, colLipides;

    @FXML private Label lblDailyGoalName;
    @FXML private Label lblDailyGoalCalories;
    @FXML private Label lblDailyGoalWeight;

    private final FoodLogDAO dao = new FoodLogDAO();
    private final NutritionGoalDAO goalDao = new NutritionGoalDAO();

    // Une seule déclaration pour l'ID utilisateur
    private final String currentUserUuid = "c513e200-d605-4f61-9efc-d539f0e80914";
    private ObservableList<FoodLog> foodLogsList = FXCollections.observableArrayList();

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
        colRepas.setCellValueFactory(new PropertyValueFactory<>("mealType"));
        colCalories.setCellValueFactory(new PropertyValueFactory<>("totalCalories"));
        colProteines.setCellValueFactory(new PropertyValueFactory<>("totalProtein"));
        colGlucides.setCellValueFactory(new PropertyValueFactory<>("totalCarbs"));
        colLipides.setCellValueFactory(new PropertyValueFactory<>("totalFats"));

        tableRepas.setItems(foodLogsList);

        tableRepas.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean hasSelection = newSelection != null;
            btnModifier.setDisable(!hasSelection);
            btnSupprimer.setDisable(!hasSelection);
        });

        btnAjoutRapide.setOnAction(e -> openFoodDialog(null));
        btnModifier.setOnAction(e -> {
            FoodLog selected = tableRepas.getSelectionModel().getSelectedItem();
            if (selected != null) openFoodDialog(selected);
        });
        btnSupprimer.setOnAction(e -> deleteSelectedLog());

        loadDailyGoal();
        refreshData();
    }

    private void refreshData() {
        double[] totals = dao.getDailyTotals(currentUserUuid, LocalDate.now());
        NutritionGoal dailyGoal = goalDao.getDailyGoalByUser(currentUserUuid);
        int targetCalories = 2000;

        if (dailyGoal != null && dailyGoal.getCaloriesTarget() > 0) {
            targetCalories = dailyGoal.getCaloriesTarget();
        }

        lblCalories.setText(String.format("%.0f / %d", totals[0], targetCalories));
        lblProteines.setText(String.format("%.0fg / 120g", totals[1]));
        lblGlucides.setText(String.format("%.0fg / 200g", totals[2]));
        lblLipides.setText(String.format("%.0fg / 65g", totals[3]));

        List<FoodLog> logs = dao.getFoodLogsByDate(currentUserUuid, LocalDate.now());
        foodLogsList.setAll(logs);
    }

    private void loadDailyGoal() {
        NutritionGoal dailyGoal = goalDao.getDailyGoalByUser(currentUserUuid);

        if (dailyGoal != null) {
            lblDailyGoalName.setText("🎯 " + dailyGoal.getName());
            lblDailyGoalCalories.setText("Calories : " + dailyGoal.getCaloriesTarget() + " kcal");
            lblDailyGoalWeight.setText("Poids cible : " + dailyGoal.getWeightTarget() + " kg");
        } else {
            lblDailyGoalName.setText("Aucun objectif pour aujourd'hui");
            lblDailyGoalCalories.setText("Calories : -");
            lblDailyGoalWeight.setText("Poids cible : -");
        }
    }

    private void openFoodDialog(FoodLog existingLog) {
        boolean isEdit = (existingLog != null);
        Dialog<FoodLog> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Modifier Repas" : "Ajout Rapide");

        // Supprime l'en-tête par défaut
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

        ButtonType btnValider = new ButtonType(isEdit ? "Enregistrer" : "Ajouter", ButtonBar.ButtonData.OK_DONE);
        dialogPane.getButtonTypes().addAll(btnValider, ButtonType.CANCEL);

        // Styliser les boutons du dialogue
        Button btOk = (Button) dialogPane.lookupButton(btnValider);
        btOk.getStyleClass().add("primary-button");

        Button btCancel = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
        btCancel.getStyleClass().add("secondary-button");

        // --- CRÉATION DES CHAMPS ---
        TextField calField = new TextField(); calField.setPromptText("Ex: 450");
        TextField protField = new TextField(); protField.setPromptText("Ex: 30.5");
        TextField glucField = new TextField(); glucField.setPromptText("Ex: 45.0");
        TextField lipField = new TextField(); lipField.setPromptText("Ex: 15.2");

        String fieldStyle = "-fx-padding: 8; -fx-background-radius: 5;";
        calField.setStyle(fieldStyle); protField.setStyle(fieldStyle);
        glucField.setStyle(fieldStyle); lipField.setStyle(fieldStyle);

        UnaryOperator<TextFormatter.Change> intFilter = change -> change.getControlNewText().matches("\\d*") ? change : null;
        UnaryOperator<TextFormatter.Change> doubleFilter = change -> change.getControlNewText().matches("\\d*\\.?\\d*") ? change : null;

        calField.setTextFormatter(new TextFormatter<>(intFilter));
        protField.setTextFormatter(new TextFormatter<>(doubleFilter));
        glucField.setTextFormatter(new TextFormatter<>(doubleFilter));
        lipField.setTextFormatter(new TextFormatter<>(doubleFilter));

        ComboBox<String> mealBox = new ComboBox<>();
        mealBox.getItems().addAll("BREAKFAST", "LUNCH", "DINNER", "SNACK");
        mealBox.setValue("SNACK");
        mealBox.setMaxWidth(Double.MAX_VALUE);
        mealBox.setStyle(fieldStyle);

        if (isEdit) {
            mealBox.setValue(existingLog.getMealType());
            calField.setText(String.valueOf(existingLog.getTotalCalories()));
            protField.setText(String.valueOf(existingLog.getTotalProtein()));
            glucField.setText(String.valueOf(existingLog.getTotalCarbs()));
            lipField.setText(String.valueOf(existingLog.getTotalFats()));
        }

        // --- STRUCTURE DE LAYOUT ---
        Label titleLabel = new Label(isEdit ? "✏️ Modifier le repas" : "🥗 Ajouter un nouveau repas");
        titleLabel.getStyleClass().add("main-title");
        titleLabel.setStyle("-fx-font-size: 18px;");

        // Label pour les erreurs de saisie
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #EF4444; -fx-font-size: 13px; -fx-font-weight: bold;");
        errorLabel.setWrapText(true);

        VBox mealGroup = new VBox(5, new Label("🍽️ Type de Repas"), mealBox);
        VBox calGroup = new VBox(5, new Label("🔥 Calories (kcal)"), calField);
        VBox protGroup = new VBox(5, new Label("🥩 Protéines (g)"), protField);
        VBox glucGroup = new VBox(5, new Label("🍞 Glucides (g)"), glucField);
        VBox lipGroup = new VBox(5, new Label("🥑 Lipides (g)"), lipField);

        HBox macrosBox = new HBox(15, protGroup, glucGroup);
        HBox.setHgrow(protGroup, Priority.ALWAYS);
        HBox.setHgrow(glucGroup, Priority.ALWAYS);

        VBox contentContainer = new VBox(15);
        contentContainer.setStyle("-fx-background-color: transparent;");
        contentContainer.setPadding(new Insets(20, 30, 10, 30));
        contentContainer.getChildren().addAll(titleLabel, errorLabel, mealGroup, calGroup, macrosBox, lipGroup);

        dialogPane.setContent(contentContainer);

        // --- VALIDATION AU CLIC ---
        btOk.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            errorLabel.setText(""); // Réinitialise l'erreur

            try {
                if (calField.getText().isEmpty() || protField.getText().isEmpty() ||
                        glucField.getText().isEmpty() || lipField.getText().isEmpty()) {
                    errorLabel.setText("❌ Veuillez remplir tous les champs.");
                    event.consume();
                    return;
                }

                int cal = Integer.parseInt(calField.getText().trim());
                double prot = Double.parseDouble(protField.getText().trim());
                double gluc = Double.parseDouble(glucField.getText().trim());
                double lip = Double.parseDouble(lipField.getText().trim());

                if (cal < 0 || prot < 0 || gluc < 0 || lip < 0) {
                    errorLabel.setText("❌ Les valeurs ne peuvent pas être négatives.");
                    event.consume();
                }
            } catch (NumberFormatException e) {
                errorLabel.setText("❌ Veuillez entrer des nombres valides.");
                event.consume();
            }
        });

        dialog.setResultConverter(button -> {
            if (button == btnValider) {
                return new FoodLog(
                        isEdit ? existingLog.getId() : 0,
                        currentUserUuid,
                        LocalDate.now(),
                        mealBox.getValue(),
                        Integer.parseInt(calField.getText().trim()),
                        Double.parseDouble(protField.getText().trim()),
                        Double.parseDouble(glucField.getText().trim()),
                        Double.parseDouble(lipField.getText().trim())
                );
            }
            return null;
        });

        dialog.showAndWait().ifPresent(log -> {
            boolean success = isEdit ? dao.updateFoodLog(log) : dao.addFoodLog(log);
            if (success) {
                refreshData();
            } else {
                showCustomAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de base de données.");
            }
        });
    }

    private void deleteSelectedLog() {
        FoodLog selected = tableRepas.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        showCustomAlert(Alert.AlertType.CONFIRMATION, "Confirmation", "Voulez-vous vraiment supprimer ce repas ?", ButtonType.YES, ButtonType.NO)
                .ifPresent(response -> {
                    if (response == ButtonType.YES) {
                        if (dao.deleteFoodLog(selected.getId())) {
                            refreshData();
                        } else {
                            showCustomAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la suppression.");
                        }
                    }
                });
    }

    // --- MÉTHODE POUR CRÉER DES ALERTES MODERNES ---
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

    @FXML public void navToDashboard(ActionEvent event) { System.out.println("Nav Dashboard..."); }
    
    
    
    
    


    


    @Override
    protected javafx.scene.Parent getRoot() { return rootPane; }

}