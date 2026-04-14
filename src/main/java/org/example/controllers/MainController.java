package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

public class MainController {

    @FXML private StackPane contentArea; // Pour charger les pages
    @FXML private Button btnTheme;       // Pour le toggle

    private boolean isDarkMode = true;

    @FXML
    public void initialize() {
        loadGoals(); // Charge le dashboard au début
    }

    @FXML
    private void toggleTheme() {
        // Sécurité : si le bouton n'est pas encore sur la scène
        if (btnTheme.getScene() == null) return;

        Scene scene = btnTheme.getScene();
        scene.getStylesheets().clear();

        // 1. Charger la structure de base
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        // 2. Charger le mode
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

    @FXML
    private void loadExerciseLibrary() {
        loadPage("/ExerciseLibrary.fxml");
    }

    @FXML
    private void loadGoals() {
        loadPage("/Dashboard.fxml");
    }

    // Méthode utilitaire pour éviter de répéter le code
    private void loadPage(String fxmlPath) {
        try {
            Node view = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentArea.getChildren().setAll(view);
        } catch (Exception e) {
            System.err.println("Erreur de chargement : " + fxmlPath);
            e.printStackTrace();
        }
    }
}