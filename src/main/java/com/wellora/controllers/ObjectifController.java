package com.wellora.controllers;

import com.wellora.dao.NutritionGoalDAO;
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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.util.function.UnaryOperator;
import com.wellora.utils.ThemeManager;

public class ObjectifController {

    @FXML private BorderPane rootPane;
    @FXML private ToggleButton btnThemeToggle;
    @FXML private TableView<NutritionGoal> tableObjectifs;
    @FXML private TableColumn<NutritionGoal, String> colNom, colType;
    @FXML private TableColumn<NutritionGoal, Integer> colCalories;
    @FXML private TableColumn<NutritionGoal, Double> colPoids;
    @FXML private TableColumn<NutritionGoal, LocalDate> colDate;
    @FXML private Button btnAjouter, btnModifier, btnSupprimer, btnAjoutJournalier;

    private final NutritionGoalDAO dao = new NutritionGoalDAO();
    private final String currentUserUuid = "c513e200-d605-4f61-9efc-d539f0e80914";
    private ObservableList<NutritionGoal> objectifsList = FXCollections.observableArrayList();

    @FXML public void navToAnalyse(ActionEvent event) { switchScene(event, "Analyse.fxml"); }
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
        colNom.setCellValueFactory(new PropertyValueFactory<>("name"));
        colType.setCellValueFactory(new PropertyValueFactory<>("goalType"));
        colCalories.setCellValueFactory(new PropertyValueFactory<>("caloriesTarget"));
        colPoids.setCellValueFactory(new PropertyValueFactory<>("weightTarget"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("targetDate"));

        tableObjectifs.setItems(objectifsList);

        tableObjectifs.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean hasSelection = newSelection != null;
            btnModifier.setDisable(!hasSelection);
            btnSupprimer.setDisable(!hasSelection);
        });

        if (btnAjoutJournalier != null) {
            btnAjoutJournalier.setOnAction(e -> openObjectifDialog(null, true));
        }

        btnAjouter.setOnAction(e -> openObjectifDialog(null, false));

        btnModifier.setOnAction(e -> {
            NutritionGoal selected = tableObjectifs.getSelectionModel().getSelectedItem();
            if (selected != null) openObjectifDialog(selected, false);
        });

        btnSupprimer.setOnAction(e -> deleteSelectedObjectif());

        refreshData();
    }

    private void refreshData() {
        objectifsList.setAll(dao.getGoalsByUser(currentUserUuid));
    }

    private void openObjectifDialog(NutritionGoal existingObj, boolean isDaily) {
        boolean isEdit = (existingObj != null);
        Dialog<NutritionGoal> dialog = new Dialog<>();

        if (isDaily) {
            dialog.setTitle("Objectif du Jour");
            dialog.setHeaderText("Définissez votre objectif pour aujourd'hui !");
        } else {
            dialog.setTitle(isEdit ? "Modifier l'Objectif" : "Ajouter un Objectif");
            dialog.setHeaderText("Définissez vos objectifs nutritionnels");
        }

        ButtonType btnValider = new ButtonType(isEdit ? "Enregistrer" : "Ajouter", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnValider, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);

        TextField nameField = new TextField();
        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll("Weight Loss", "Muscle Gain", "Maintenance", "General Health", "Daily Challenge");
        typeBox.setValue(isDaily ? "Daily Challenge" : "Weight Loss");

        TextField caloriesField = new TextField();
        TextField weightField = new TextField();
        DatePicker datePicker = new DatePicker();

        if (isDaily) {
            datePicker.setValue(LocalDate.now());
            datePicker.setDisable(true);
        }

        UnaryOperator<TextFormatter.Change> intFilter = change -> {
            if (change.getControlNewText().matches("\\d*")) return change;
            return null;
        };
        UnaryOperator<TextFormatter.Change> doubleFilter = change -> {
            if (change.getControlNewText().matches("\\d*\\.?\\d*")) return change;
            return null;
        };
        caloriesField.setTextFormatter(new TextFormatter<>(intFilter));
        weightField.setTextFormatter(new TextFormatter<>(doubleFilter));

        if (isEdit) {
            nameField.setText(existingObj.getName());
            typeBox.setValue(existingObj.getGoalType());
            caloriesField.setText(String.valueOf(existingObj.getCaloriesTarget()));
            weightField.setText(String.valueOf(existingObj.getWeightTarget()));
            datePicker.setValue(existingObj.getTargetDate());
        }

        grid.add(new Label("Nom :"), 0, 0); grid.add(nameField, 1, 0);
        grid.add(new Label("Type :"), 0, 1); grid.add(typeBox, 1, 1);
        grid.add(new Label("Objectif Calories (kcal):"), 0, 2); grid.add(caloriesField, 1, 2);
        grid.add(new Label("Objectif Poids (kg):"), 0, 3); grid.add(weightField, 1, 3);
        grid.add(new Label(isDaily ? "Date (Aujourd'hui) :" : "Date Limite :"), 0, 4); grid.add(datePicker, 1, 4);

        dialog.getDialogPane().setContent(grid);

        final Button btOk = (Button) dialog.getDialogPane().lookupButton(btnValider);
        btOk.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (nameField.getText().trim().isEmpty() || caloriesField.getText().trim().isEmpty() ||
                    weightField.getText().trim().isEmpty() || datePicker.getValue() == null) {
                new Alert(Alert.AlertType.ERROR, "Veuillez remplir tous les champs.").showAndWait();
                event.consume();
                return;
            }

            if (datePicker.getValue().isBefore(LocalDate.now()) && !isEdit && !isDaily) {
                new Alert(Alert.AlertType.WARNING, "La date limite ne peut pas être dans le passé.").showAndWait();
                event.consume();
                return;
            }

            try {
                int calories = Integer.parseInt(caloriesField.getText().trim());
                double weight = Double.parseDouble(weightField.getText().trim());

                if (calories < 1000 || calories > 7000) {
                    new Alert(Alert.AlertType.WARNING, "L'objectif calorique doit être compris entre 1000 et 7000 kcal.").showAndWait();
                    event.consume();
                    return;
                }

                if (weight < 40 || weight > 120) {
                    new Alert(Alert.AlertType.WARNING, "L'objectif de poids doit être compris entre 40 et 120 kg.").showAndWait();
                    event.consume();
                    return;
                }

            } catch (NumberFormatException ex) {
                new Alert(Alert.AlertType.ERROR, "Les valeurs saisies ne sont pas valides.").showAndWait();
                event.consume();
                return;
            }
        });

        dialog.setResultConverter(button -> {
            if (button == btnValider) {
                return new NutritionGoal(
                        isEdit ? existingObj.getId() : 0,
                        currentUserUuid,
                        nameField.getText().trim(),
                        typeBox.getValue(),
                        Integer.parseInt(caloriesField.getText().trim()),
                        Double.parseDouble(weightField.getText().trim()),
                        datePicker.getValue()
                );
            }
            return null;
        });

        dialog.showAndWait().ifPresent(obj -> {
            boolean success = isEdit ? dao.updateGoal(obj) : dao.addGoal(obj);
            if (success) {
                refreshData();
            } else {
                new Alert(Alert.AlertType.ERROR, "Erreur BDD.").showAndWait();
            }
        });
    }

    private void deleteSelectedObjectif() {
        NutritionGoal selected = tableObjectifs.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer cet objectif ?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                if (dao.deleteGoal(selected.getId())) {
                    refreshData();
                } else {
                    new Alert(Alert.AlertType.ERROR, "Erreur lors de la suppression.").showAndWait();
                }
            }
        });
    }

    @FXML public void navToDashboard(ActionEvent event) { switchScene(event, "Dashboard.fxml"); }
    @FXML public void navToJournal(ActionEvent event) { switchScene(event, "Journal.fxml"); }
    @FXML public void navToObjectifs(ActionEvent event) { }

    private void switchScene(ActionEvent event, String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/wellora/views/" + fxmlFile));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root, 1200, 800);
            scene.getStylesheets().add(getClass().getResource("/com/wellora/css/style.css").toExternalForm());
            stage.setScene(scene);
        } catch (IOException e) { e.printStackTrace(); }
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

    @FXML
    public void navToPlanificateur(ActionEvent event) {
        switchScene(event, "Planificateur.fxml");
    }
}