package com.wellora.controllers;

import com.wellora.dao.MealPlanDAO;
import com.wellora.models.MealPlan;
import com.wellora.models.Recipe;
import com.wellora.services.RecipeApiService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class RecettesController {

    @FXML private BorderPane rootPane;
    @FXML private ToggleButton btnThemeToggle;
    @FXML private TextField fieldCalories;
    @FXML private Button btnChercher;
    @FXML private VBox containerRecettes;
    @FXML private Label labelStatut;

    private final RecipeApiService recipeApi = new RecipeApiService();
    private final MealPlanDAO dao = new MealPlanDAO();

    private final String currentUserUuid = "c513e200-d605-4f61-9efc-d539f0e80914";
    private final int currentUserId = 1;

    @FXML
    public void initialize() {
        btnChercher.setOnAction(e -> fetchRecipes());
    }

    private void fetchRecipes() {
        String calText = fieldCalories.getText().trim();
        if (calText.isEmpty()) {
            labelStatut.setText("Veuillez entrer un nombre de calories.");
            labelStatut.setStyle("-fx-text-fill: #EF4444;");
            return;
        }

        try {
            int targetCalories = Integer.parseInt(calText);
            labelStatut.setText("Recherche de recettes en cours... ⏳");
            labelStatut.setStyle("-fx-text-fill: #6B7280;");
            containerRecettes.getChildren().clear();
            btnChercher.setDisable(true);

            new Thread(() -> {
                List<Recipe> recipes = recipeApi.getRecipesByCalories(targetCalories);

                Platform.runLater(() -> {
                    btnChercher.setDisable(false);
                    if (recipes.isEmpty()) {
                        labelStatut.setText("Aucune recette trouvée.");
                        labelStatut.setStyle("-fx-text-fill: #EF4444;");
                    } else {
                        labelStatut.setText("Voici des suggestions pour moins de " + targetCalories + " kcal !");
                        labelStatut.setStyle("-fx-text-fill: #00E6A4;");
                        for (Recipe r : recipes) {
                            containerRecettes.getChildren().add(createRecipeCard(r));
                        }
                    }
                });
            }).start();

        } catch (NumberFormatException ex) {
            labelStatut.setText("Format de calories invalide.");
            labelStatut.setStyle("-fx-text-fill: #EF4444;");
        }
    }

    // --- CRÉATION DE LA CARTE LIÉE AU CSS ---
    private VBox createRecipeCard(Recipe recipe) {
        VBox card = new VBox(10);
        card.getStyleClass().add("recipe-card");

        Label title = new Label("🍽️ " + recipe.getTitle());
        title.getStyleClass().add("recipe-title");

        Label calories = new Label("🔥 " + recipe.getCalories() + " kcal");
        calories.getStyleClass().add("recipe-calories");

        Label instructions = new Label(recipe.getInstructions());
        instructions.setWrapText(true);
        instructions.getStyleClass().add("recipe-instructions");

        Button btnAdd = new Button("➕ Ajouter au Planificateur");
        btnAdd.setStyle("-fx-background-color: #00E6A4; -fx-text-fill: #121418; -fx-background-radius: 5; -fx-cursor: hand; -fx-padding: 8 15; -fx-font-weight: bold;");
        btnAdd.setOnAction(e -> openAddToPlanDialog(recipe));

        if (recipe.getImageUrl() != null && !recipe.getImageUrl().isEmpty()) {
            try {
                ImageView imgView = new ImageView(new Image(recipe.getImageUrl(), true));
                imgView.setFitWidth(200);
                imgView.setPreserveRatio(true);
                card.getChildren().addAll(title, imgView, calories, instructions, btnAdd);
                return card;
            } catch (Exception e) {
                // Ignore l'image
            }
        }

        card.getChildren().addAll(title, calories, instructions, btnAdd);
        return card;
    }

    // --- FORMULAIRE ADAPTÉ AU THÈME ---
    private void openAddToPlanDialog(Recipe recipe) {
        Dialog<MealPlan> dialog = new Dialog<>();
        dialog.setTitle("Ajouter au Planificateur");
        dialog.setHeaderText("Planifier la recette : \n" + recipe.getTitle());

        // Appliquer le CSS au Dialog
        DialogPane dialogPane = dialog.getDialogPane();
        try {
            dialogPane.getStylesheets().add(getClass().getResource("/com/wellora/css/style.css").toExternalForm());
        } catch (NullPointerException e) {
            System.err.println("Fichier style.css non trouvé pour le Dialog.");
        }

        // Appliquer le mode CLAIR au dialogue si actif sur la page principale
        if (btnThemeToggle.isSelected()) {
            dialogPane.getStyleClass().add("light-theme");
        }

        ButtonType btnValider = new ButtonType("Ajouter", ButtonBar.ButtonData.OK_DONE);
        dialogPane.getButtonTypes().addAll(btnValider, ButtonType.CANCEL);

        DatePicker datePicker = new DatePicker(LocalDate.now());

        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll("Breakfast", "Lunch", "Dinner", "Snack");
        typeBox.setValue("Lunch");

        VBox box = new VBox(10);
        box.setPadding(new Insets(20, 0, 0, 0));

        Label lblDate = new Label("Choisissez la date :");
        Label lblRepas = new Label("Moment de la journée :");

        box.getChildren().addAll(lblDate, datePicker, lblRepas, typeBox);
        dialogPane.setContent(box);

        dialog.setResultConverter(button -> {
            if (button == btnValider) {
                LocalDate date = datePicker.getValue();
                return new MealPlan(
                        0, currentUserId, currentUserUuid, date, date.getDayOfWeek().name(),
                        typeBox.getValue(), recipe.getTitle(), recipe.getCalories(), false
                );
            }
            return null;
        });

        dialog.showAndWait().ifPresent(plan -> {
            if (dao.addPlan(plan)) {
                labelStatut.setText("✅ " + recipe.getTitle() + " ajouté au planificateur !");
                labelStatut.setStyle("-fx-text-fill: #00E6A4; -fx-font-weight: bold;");
            } else {
                labelStatut.setText("❌ Erreur lors de l'enregistrement.");
                labelStatut.setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;");
            }
        });
    }

    // --- GESTION DU THÈME ---
    @FXML
    public void toggleTheme() {
        com.wellora.utils.ThemeManager.isDarkMode = btnThemeToggle.isSelected();
        if (com.wellora.utils.ThemeManager.isDarkMode) {
            btnThemeToggle.setText("☀️ Mode Clair");
            if (!rootPane.getStyleClass().contains("light-theme")) {
                rootPane.getStyleClass().add("light-theme");
            }
        } else {
            btnThemeToggle.setText("🌙 Mode Sombre");
            rootPane.getStyleClass().remove("light-theme");
        }
    }

    // --- NAVIGATION AMÉLIORÉE ET SÉCURISÉE ---
    @FXML public void navToDashboard(ActionEvent event) { switchScene(event, "Dashboard.fxml"); }
    @FXML public void navToJournal(ActionEvent event) { switchScene(event, "Journal.fxml"); }
    @FXML public void navToPlanificateur(ActionEvent event) { switchScene(event, "Planificateur.fxml"); }
    @FXML public void navToRecettes(ActionEvent event) {}
    @FXML public void navToObjectifs(ActionEvent event) { switchScene(event, "Objectif.fxml"); }
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

            // 2. Appliquer le thème actuel à la nouvelle page (Logique light-theme)
            boolean isLightMode = btnThemeToggle.isSelected();
            if (isLightMode) {
                if (!root.getStyleClass().contains("light-theme")) {
                    root.getStyleClass().add("light-theme");
                }
            } else {
                root.getStyleClass().remove("light-theme");
            }

            // 3. Mettre à jour le bouton de la nouvelle page
            ToggleButton nextBtnTheme = (ToggleButton) root.lookup("#btnThemeToggle");
            if (nextBtnTheme != null) {
                nextBtnTheme.setSelected(isLightMode);
                nextBtnTheme.setText(isLightMode ? "🌙 Mode Sombre" : "☀️ Mode Clair");
            }

            // 4. CHANGER UNIQUEMENT LE CONTENU
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);

        } catch (IOException e) {
            System.err.println("❌ Impossible de charger la page : " + fxmlFile);
            e.printStackTrace();
        } catch (NullPointerException e) {
            System.err.println("❌ Erreur de chemin CSS ou FXML lors de la navigation vers " + fxmlFile);
            e.printStackTrace();
        }
    }
}