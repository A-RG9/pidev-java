package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

public class MainController {

    @FXML private StackPane contentArea;
    @FXML private Button btnTheme;

    private boolean isDarkMode = true;

    @FXML
    public void initialize() {
        loadGoals(); // Charge le dashboard par défaut
    }

    @FXML
    private void toggleTheme() {
        if (btnTheme.getScene() == null) return;

        Scene scene = btnTheme.getScene();
        scene.getStylesheets().clear();
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        if (isDarkMode) {
            scene.getStylesheets().add(getClass().getResource("/light.css").toExternalForm());
            btnTheme.setText("Mode Sombre 🌙");
            isDarkMode = false;
        } else {
            scene.getStylesheets().add(getClass().getResource("/dark.css").toExternalForm());
            btnTheme.setText("Mode Clair ☀️");
            isDarkMode = true;
        }
    }

    // --- NAVIGATION ---

    @FXML
    private void loadExerciseLibrary() {
        loadPage("/ExerciseLibrary.fxml");
    }

    @FXML
    private void loadGoals() {
        loadPage("/Dashboard.fxml");
    }

    @FXML
    private void loadDailyPlanEditor() {
        // Cette méthode doit charger le fichier FXML que nous avons créé précédemment
        loadPage("/DailyPlanEditor.fxml");
    }
    @FXML
    private void loadFitnessDashboard() {
        // Cette méthode charge la nouvelle page du Dashboard Fitness
        loadPage("/FitnessDashboard.fxml");
    }

    @FXML
    private void loadAllPlans() {
        // Si vous avez une page spécifique pour la liste de tous les plans
        loadPage("/AllPlans.fxml");
    }

    // Méthode utilitaire robuste
    private void loadPage(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node view = loader.load();
            contentArea.getChildren().setAll(view);
        } catch (Exception e) {
            System.err.println("Erreur de chargement du fichier FXML : " + fxmlPath);
            e.printStackTrace();
        }
    }

}