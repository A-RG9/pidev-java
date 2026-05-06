package com.wellora.controllers;

import com.wellora.dao.MealPlanDAO;
import com.wellora.models.MealPlan;
import com.wellora.models.Recipe;
import com.wellora.nutrition.services.RecipeApiService;
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

import com.wellcare.javafx.util.SceneManager;

public class RecettesController extends BaseController {

    @FXML private BorderPane rootPane;
    @FXML private ToggleButton btnThemeToggle;
    @FXML private TextField fieldCalories;
    @FXML private Button btnChercher;
    @FXML private VBox containerRecettes;
    @FXML private Label labelStatut;

    private final RecipeApiService recipeApi = new RecipeApiService();
    private final MealPlanDAO dao = new MealPlanDAO();

    private String currentUserUuid = "";
    private int currentUserId = 0;

    @FXML
    public void initialize() {
        // Load logged-in user from SceneManager
        com.wellcare.javafx.model.User loggedUser = SceneManager.getInstance().getCurrentUser();
        if (loggedUser != null) {
            currentUserUuid = loggedUser.getUuid();
            // currentUserId fallback
            currentUserId = Math.abs(currentUserUuid.hashCode() % 100000);
        } else {
            System.err.println("⚠️ RecettesController: No logged-in user found in SceneManager!");
        }

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
        // Appliquer le mode CLAIR au dialogue si actif sur la page principale
        if (btnThemeToggle.isSelected()) {
            dialogPane.getStyleClass().add("light-theme");
        }

        // --- AJOUTEZ CES DEUX LIGNES POUR AGRANDIR LA FENÊTRE ---
        dialogPane.setPrefWidth(450);  // Largeur de la fenêtre
        dialogPane.setPrefHeight(320); // Hauteur de la fenêtre

        ButtonType btnValider = new ButtonType("Ajouter", ButtonBar.ButtonData.OK_DONE);
        dialogPane.getButtonTypes().addAll(btnValider, ButtonType.CANCEL);

        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.getStyleClass().add("date-picker-custom");
        datePicker.setPrefWidth(250); // Agrandir aussi le champ de date

        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll("Breakfast", "Lunch", "Dinner", "Snack");
        typeBox.setValue("Lunch");
        typeBox.setPrefWidth(250); // Agrandir aussi le menu déroulant

        // On augmente l'espacement entre les éléments (15 au lieu de 10)
        VBox box = new VBox(15);
        // On ajoute des marges tout autour (Haut, Droite, Bas, Gauche) pour aérer le contenu
        box.setPadding(new Insets(20, 20, 20, 20));

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

    // --- NAVIGATION AMÉLIORÉE ET SÉCURISÉE ---
    
    
    
    
    
    


    @Override
    protected javafx.scene.Parent getRoot() { return rootPane; }

}