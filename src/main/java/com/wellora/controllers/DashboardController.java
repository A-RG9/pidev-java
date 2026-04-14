package com.wellora.controllers;

import javafx.scene.control.TextFormatter;
import java.util.function.UnaryOperator;
import com.wellora.dao.FoodLogDAO;
import com.wellora.models.FoodLog;
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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

// CONTROLLER LAYER: Manages user interaction, UI updates, and talks to the DAO
public class DashboardController {

    @FXML private BorderPane rootPane;
    @FXML private ToggleButton btnThemeToggle;
    @FXML private Label lblCalories, lblProteines, lblGlucides, lblLipides;
    @FXML private Button btnAjoutRapide, btnModifier, btnSupprimer;
    @FXML private TableView<FoodLog> tableRepas;
    @FXML private TableColumn<FoodLog, String> colRepas;
    @FXML private TableColumn<FoodLog, Integer> colCalories;
    @FXML private TableColumn<FoodLog, Double> colProteines, colGlucides, colLipides;

    private final FoodLogDAO dao = new FoodLogDAO();
    private final String currentUserUuid = "c513e200-d605-4f61-9efc-d539f0e80914";
    private ObservableList<FoodLog> foodLogsList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
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

        refreshData();
    }

    private void refreshData() {
        double[] totals = dao.getDailyTotals(currentUserUuid, LocalDate.now());
        lblCalories.setText(String.format("%.0f / 2000", totals[0]));
        lblProteines.setText(String.format("%.0fg / 120g", totals[1]));
        lblGlucides.setText(String.format("%.0fg / 200g", totals[2]));
        lblLipides.setText(String.format("%.0fg / 65g", totals[3]));

        List<FoodLog> logs = dao.getFoodLogsByDate(currentUserUuid, LocalDate.now());
        foodLogsList.setAll(logs);
    }

    private void openFoodDialog(FoodLog existingLog) {
        boolean isEdit = (existingLog != null);
        Dialog<FoodLog> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Modifier Repas" : "Ajout Rapide");
        dialog.setHeaderText("Entrez les valeurs nutritionnelles");

        ButtonType btnValider = new ButtonType(isEdit ? "Enregistrer" : "Ajouter", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnValider, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);

        TextField calField = new TextField();
        TextField protField = new TextField();
        TextField glucField = new TextField();
        TextField lipField = new TextField();

        // --- DEBUT CONTROLE DE SAISIE ---

        // Filtre pour n'accepter que des nombres entiers (pour les calories)
        UnaryOperator<TextFormatter.Change> intFilter = change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d*")) { // Autorise uniquement les chiffres (0-9)
                return change;
            }
            return null; // Rejette la modification si ce n'est pas un chiffre
        };

        // Filtre pour n'accepter que des nombres décimaux (pour les macros : protéines, etc.)
        UnaryOperator<TextFormatter.Change> doubleFilter = change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d*\\.?\\d*")) { // Autorise les chiffres et un seul point (.)
                return change;
            }
            return null;
        };

        // Appliquer les filtres aux champs de texte
        calField.setTextFormatter(new TextFormatter<>(intFilter));
        protField.setTextFormatter(new TextFormatter<>(doubleFilter));
        glucField.setTextFormatter(new TextFormatter<>(doubleFilter));
        lipField.setTextFormatter(new TextFormatter<>(doubleFilter));

        // --- FIN CONTROLE DE SAISIE ---

        ComboBox<String> mealBox = new ComboBox<>();
        mealBox.getItems().addAll("BREAKFAST", "LUNCH", "DINNER", "SNACK");
        mealBox.setValue("SNACK");

        if (isEdit) {
            mealBox.setValue(existingLog.getMealType());
            calField.setText(String.valueOf(existingLog.getTotalCalories()));
            protField.setText(String.valueOf(existingLog.getTotalProtein()));
            glucField.setText(String.valueOf(existingLog.getTotalCarbs()));
            lipField.setText(String.valueOf(existingLog.getTotalFats()));
        }

        grid.add(new Label("Type:"), 0, 0); grid.add(mealBox, 1, 0);
        grid.add(new Label("Calories:"), 0, 1); grid.add(calField, 1, 1);
        grid.add(new Label("Protéines (g):"), 0, 2); grid.add(protField, 1, 2);
        grid.add(new Label("Glucides (g):"), 0, 3); grid.add(glucField, 1, 3);
        grid.add(new Label("Lipides (g):"), 0, 4); grid.add(lipField, 1, 4);

        dialog.getDialogPane().setContent(grid);

        final Button btOk = (Button) dialog.getDialogPane().lookupButton(btnValider);
        btOk.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            try {
                // Vérifier si les champs sont vides avant de parser
                if (calField.getText().isEmpty() || protField.getText().isEmpty() ||
                        glucField.getText().isEmpty() || lipField.getText().isEmpty()) {
                    new Alert(Alert.AlertType.ERROR, "Veuillez remplir tous les champs.").showAndWait();
                    event.consume();
                    return;
                }

                int cal = Integer.parseInt(calField.getText().trim());
                double prot = Double.parseDouble(protField.getText().trim());
                double gluc = Double.parseDouble(glucField.getText().trim());
                double lip = Double.parseDouble(lipField.getText().trim());

                if (cal < 0 || prot < 0 || gluc < 0 || lip < 0) {
                    new Alert(Alert.AlertType.ERROR, "Valeurs négatives interdites.").showAndWait();
                    event.consume();
                }
            } catch (NumberFormatException e) {
                new Alert(Alert.AlertType.ERROR, "Veuillez entrer des nombres valides.").showAndWait();
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
                new Alert(Alert.AlertType.ERROR, "Erreur BDD.").showAndWait();
            }
        });
    }

    private void deleteSelectedLog() {
        FoodLog selected = tableRepas.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer ce repas ?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                if (dao.deleteFoodLog(selected.getId())) {
                    refreshData();
                } else {
                    new Alert(Alert.AlertType.ERROR, "Erreur lors de la suppression.").showAndWait();
                }
            }
        });
    }

    @FXML public void navToDashboard(ActionEvent event) { System.out.println("Nav Dashboard..."); }
    @FXML public void navToJournal(ActionEvent event) { switchScene(event, "Journal.fxml"); }

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

    @FXML public void navToAppointments() { System.out.println("Nav Appointments..."); }

    @FXML
    public void toggleTheme() {
        if (btnThemeToggle.isSelected()) {
            rootPane.getStyleClass().add("light-theme");
            btnThemeToggle.setText("☀️ Mode Clair");
        } else {
            rootPane.getStyleClass().remove("light-theme");
            btnThemeToggle.setText("🌙 Mode Sombre");
        }
    }
}