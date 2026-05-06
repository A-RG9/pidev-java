package com.wellcare.javafx.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import com.wellcare.javafx.util.CountryChallengeService;

import java.net.URL;

public class MainController {

    @FXML private StackPane contentArea;
    @FXML private Button btnTheme;

    private boolean isDarkMode = true;

    @FXML
    public void initialize() {
        // Précharger les pays en arrière-plan (ne bloque pas l'UI)
        CountryChallengeService.preloadCountries();
        loadFitnessDashboard();
    }

    @FXML
    private void toggleTheme() {
        if (btnTheme.getScene() == null) return;
        Scene scene = btnTheme.getScene();

        scene.getStylesheets().clear();

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
        loadPage("/DailyPlanEditor.fxml");
    }

    @FXML
    private void loadFitnessDashboard() {
        loadPage("/FitnessDashboard.fxml");
    }

    @FXML
    private void loadAllPlans() {
        loadPage("/AllPlans.fxml");
    }

    @FXML
    private void loadworkoutplan() {
        loadPage("/WorkoutPlanner.fxml");
    }
    @FXML private void loadAICoach() {
        loadPage("/AICoachView.fxml");
    }
    @FXML
    private void loadcoach() {
        loadPage("/CoachDashboard.fxml");
    }

    @FXML
    private void loadCountryChallenge() {
        loadPage("/CountryChallengeView.fxml");
    }

    @FXML
    private void loadClients() {
        loadPage("/UserDashboard.fxml");
    }

    @FXML
    private void loadDailyPlanView() {
        loadPage("/DailyPlanView.fxml");
    }

    private void loadPage(String fxmlPath) {
        try {
            System.out.println("🔍 Chargement: " + fxmlPath);
            URL url = getClass().getResource(fxmlPath);

            if (url == null) {
                System.err.println("❌ Fichier non trouvé: " + fxmlPath);
                System.err.println("   Vérifiez que le fichier existe dans src/main/resources/");
                return;
            }

            System.out.println("✅ Fichier trouvé: " + url);
            FXMLLoader loader = new FXMLLoader(url);
            Node view = loader.load();
            contentArea.getChildren().setAll(view);
            System.out.println("✅ Page chargée avec succès!");

        } catch (Exception e) {
            System.err.println("❌ Erreur chargement: " + fxmlPath);
            e.printStackTrace();
        }
    }
}