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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;
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

        // Suppression de l'en-tête par défaut
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

        // Styliser les boutons
        Button btOk = (Button) dialogPane.lookupButton(btnValider);
        btOk.getStyleClass().add("primary-button");
        Button btCancel = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
        btCancel.getStyleClass().add("secondary-button");

        // --- CRÉATION DES CHAMPS ---
        String fieldStyle = "-fx-padding: 8; -fx-background-radius: 5;";

        TextField nameField = new TextField();
        nameField.setPromptText("Ex: Perte de poids été");
        nameField.setStyle(fieldStyle);

        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll("Weight Loss", "Muscle Gain", "Maintenance", "General Health", "Daily Challenge");
        typeBox.setValue(isDaily ? "Daily Challenge" : "Weight Loss");
        typeBox.setMaxWidth(Double.MAX_VALUE);
        typeBox.setStyle(fieldStyle);

        TextField caloriesField = new TextField();
        caloriesField.setPromptText("Ex: 2000");
        caloriesField.setStyle(fieldStyle);

        TextField weightField = new TextField();
        weightField.setPromptText("Ex: 75.5");
        weightField.setStyle(fieldStyle);

        DatePicker datePicker = new DatePicker();
        datePicker.setMaxWidth(Double.MAX_VALUE);
        datePicker.setStyle(fieldStyle);

        if (isDaily) {
            datePicker.setValue(LocalDate.now());
            datePicker.setDisable(true);
        }

        // Filtres pour n'accepter que les chiffres
        UnaryOperator<TextFormatter.Change> intFilter = change -> change.getControlNewText().matches("\\d*") ? change : null;
        UnaryOperator<TextFormatter.Change> doubleFilter = change -> change.getControlNewText().matches("\\d*\\.?\\d*") ? change : null;
        caloriesField.setTextFormatter(new TextFormatter<>(intFilter));
        weightField.setTextFormatter(new TextFormatter<>(doubleFilter));

        if (isEdit) {
            nameField.setText(existingObj.getName());
            typeBox.setValue(existingObj.getGoalType());
            caloriesField.setText(String.valueOf(existingObj.getCaloriesTarget()));
            weightField.setText(String.valueOf(existingObj.getWeightTarget()));
            datePicker.setValue(existingObj.getTargetDate());
        }

        // --- STRUCTURE DE LAYOUT ---
        String titreTexte = isDaily ? "🎯 Objectif du Jour" : (isEdit ? "✏️ Modifier l'Objectif" : "🎯 Nouvel Objectif");
        Label titleLabel = new Label(titreTexte);
        titleLabel.getStyleClass().add("main-title");
        titleLabel.setStyle("-fx-font-size: 18px;");

        // Label pour les erreurs de saisie
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #EF4444; -fx-font-size: 13px; -fx-font-weight: bold;");
        errorLabel.setWrapText(true);

        VBox nameGroup = new VBox(5, new Label("📝 Nom de l'objectif"), nameField);
        VBox typeGroup = new VBox(5, new Label("📌 Type"), typeBox);
        VBox dateGroup = new VBox(5, new Label(isDaily ? "📅 Date (Aujourd'hui)" : "📅 Date Limite"), datePicker);

        VBox calGroup = new VBox(5, new Label("🔥 Calories (kcal)"), caloriesField);
        VBox weightGroup = new VBox(5, new Label("⚖️ Poids (kg)"), weightField);

        HBox targetsBox = new HBox(15, calGroup, weightGroup);
        HBox.setHgrow(calGroup, Priority.ALWAYS);
        HBox.setHgrow(weightGroup, Priority.ALWAYS);

        VBox contentContainer = new VBox(15);
        contentContainer.setStyle("-fx-background-color: transparent;");
        contentContainer.setPadding(new Insets(20, 30, 10, 30));
        contentContainer.getChildren().addAll(titleLabel, errorLabel, nameGroup, typeGroup, targetsBox, dateGroup);

        dialogPane.setContent(contentContainer);

        // --- VALIDATION AU CLIC ---
        btOk.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            errorLabel.setText(""); // Réinitialise l'erreur

            if (nameField.getText().trim().isEmpty() || caloriesField.getText().trim().isEmpty() ||
                    weightField.getText().trim().isEmpty() || datePicker.getValue() == null) {
                errorLabel.setText("❌ Veuillez remplir tous les champs.");
                event.consume();
                return;
            }

            if (datePicker.getValue().isBefore(LocalDate.now()) && !isEdit && !isDaily) {
                errorLabel.setText("❌ La date limite ne peut pas être dans le passé.");
                event.consume();
                return;
            }

            try {
                int calories = Integer.parseInt(caloriesField.getText().trim());
                double weight = Double.parseDouble(weightField.getText().trim());

                if (calories < 1000 || calories > 7000) {
                    errorLabel.setText("❌ L'objectif calorique doit être entre 1000 et 7000 kcal.");
                    event.consume();
                    return;
                }

                if (weight < 40 || weight > 120) {
                    errorLabel.setText("❌ L'objectif de poids doit être entre 40 et 120 kg.");
                    event.consume();
                }

            } catch (NumberFormatException ex) {
                errorLabel.setText("❌ Les valeurs saisies ne sont pas valides.");
                event.consume();
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
                showCustomAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de base de données.");
            }
        });
    }

    private void deleteSelectedObjectif() {
        NutritionGoal selected = tableObjectifs.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        showCustomAlert(Alert.AlertType.CONFIRMATION, "Confirmation", "Voulez-vous vraiment supprimer cet objectif ?", ButtonType.YES, ButtonType.NO)
                .ifPresent(response -> {
                    if (response == ButtonType.YES) {
                        if (dao.deleteGoal(selected.getId())) {
                            refreshData();
                        } else {
                            showCustomAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la suppression.");
                        }
                    }
                });
    }

    // --- ALERTES MODERNES ---
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

    // --- NAVIGATION ---
    @FXML public void navToDashboard(ActionEvent event) { switchScene(event, "Dashboard.fxml"); }
    @FXML public void navToJournal(ActionEvent event) { switchScene(event, "Journal.fxml"); }
    @FXML public void navToObjectifs(ActionEvent event) { }
    @FXML public void navToPlanificateur(ActionEvent event) { switchScene(event, "Planificateur.fxml"); }
    @FXML public void navToRecettes(ActionEvent event) { switchScene(event, "Recettes.fxml"); }
    @FXML public void navToAnalyse(ActionEvent event) { switchScene(event, "Analyse.fxml"); }

    private void switchScene(ActionEvent event, String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/wellora/views/" + fxmlFile));
            Parent root = loader.load();

            // 1. Forcer le chargement du fichier CSS
            String cssPath = getClass().getResource("/com/wellora/css/style.css").toExternalForm();
            if (!root.getStylesheets().contains(cssPath)) {
                root.getStylesheets().add(cssPath);
            }

            // 2. TRANSMETTRE LE THÈME À LA PAGE SUIVANTE
            boolean isLightMode = btnThemeToggle.isSelected();
            if (isLightMode) {
                if (!root.getStyleClass().contains("light-theme")) {
                    root.getStyleClass().add("light-theme");
                }
            } else {
                root.getStyleClass().remove("light-theme");
            }

            // 3. Mettre à jour le bouton de la nouvelle page pour qu'il affiche le bon texte/état
            ToggleButton nextBtnTheme = (ToggleButton) root.lookup("#btnThemeToggle");
            if (nextBtnTheme != null) {
                nextBtnTheme.setSelected(isLightMode);
                nextBtnTheme.setText(isLightMode ? "🌙 Mode Sombre" : "☀️ Mode Clair");
            }

            // 4. Changer uniquement le contenu
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);

        } catch (IOException e) {
            System.err.println("❌ Impossible de charger la page : " + fxmlFile);
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