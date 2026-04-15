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

    private boolean isDarkMode = true; // Default state

    @FXML
    public void initialize() {
        loadFitnessDashboard(); // Charge le dashboard par défaut
    }

    @FXML
    private void toggleTheme() {
        // Obtenir la scène principale
        if (btnTheme.getScene() == null) return;
        Scene scene = btnTheme.getScene();

        // Vider tous les styles actuels
        scene.getStylesheets().clear();

        if (isDarkMode) {
            // Passer au mode clair
            scene.getStylesheets().add(getClass().getResource("/light.css").toExternalForm());
            btnTheme.setText("Mode Sombre 🌙");
            isDarkMode = false;
        } else {
            // Passer au mode sombre
            scene.getStylesheets().add(getClass().getResource("/dark.css").toExternalForm());
            btnTheme.setText("Mode Clair ☀️");
            isDarkMode = true;
        }
    }

    // --- NAVIGATION ---
    @FXML private void loadExerciseLibrary() { loadPage("/ExerciseLibrary.fxml"); }
    @FXML private void loadGoals() { loadPage("/Dashboard.fxml"); }
    @FXML private void loadDailyPlanEditor() { loadPage("/DailyPlanEditor.fxml"); }
    @FXML private void loadFitnessDashboard() { loadPage("/FitnessDashboard.fxml"); }
    @FXML private void loadAllPlans() { loadPage("/AllPlans.fxml"); }
    @FXML private void loadworkoutplan() { loadPage("/WorkoutPlanner.fxml"); }

    private void loadPage(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node view = loader.load();
            contentArea.getChildren().setAll(view);
        } catch (Exception e) {
            System.err.println("Erreur chargement: " + fxmlPath);
            e.printStackTrace();
        }
    }
}