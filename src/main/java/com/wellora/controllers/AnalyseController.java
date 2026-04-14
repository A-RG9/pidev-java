package com.wellora.controllers;

import com.wellora.dao.FoodLogDAO;
import com.wellora.dao.NutritionGoalDAO;
import com.wellora.models.NutritionGoal;
import com.wellora.utils.ThemeManager;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import com.wellora.utils.ThemeManager;

public class AnalyseController {

    @FXML private BorderPane rootPane;
    @FXML private ToggleButton btnThemeToggle;
    @FXML private BarChart<String, Number> progressChart;

    private final FoodLogDAO foodLogDao = new FoodLogDAO();
    private final NutritionGoalDAO goalDao = new NutritionGoalDAO();
    private final String currentUserUuid = "c513e200-d605-4f61-9efc-d539f0e80914";

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
        // --- GESTION DU THEME GLOBAL ---
        if (ThemeManager.isDarkMode) {
            btnThemeToggle.setSelected(true);
            btnThemeToggle.setText("☀️ Mode Clair");
            // ON UTILISE "light-theme" COMME DANS LE RESTE DE VOTRE APP !
            if (!rootPane.getStyleClass().contains("light-theme")) {
                rootPane.getStyleClass().add("light-theme");
            }
        } else {
            btnThemeToggle.setSelected(false);
            btnThemeToggle.setText("🌙 Mode Sombre");
            rootPane.getStyleClass().remove("light-theme");
        }

        // --- CHARGEMENT DU GRAPHIQUE ---
        loadChartData();
    }

    private void loadChartData() {
        progressChart.getData().clear();

        XYChart.Series<String, Number> seriesConsomme = new XYChart.Series<>();
        seriesConsomme.setName("Calories Consommées");

        XYChart.Series<String, Number> seriesObjectif = new XYChart.Series<>();
        seriesObjectif.setName("Objectif Calories");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");

        // On boucle sur les 7 derniers jours (de J-6 à Aujourd'hui)
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            String dateString = date.format(formatter); // Ex: 14/04

            // 1. Récupérer les calories consommées pour CE jour précis
            double[] totals = foodLogDao.getDailyTotals(currentUserUuid, date);
            double caloriesConsommees = totals[0];

            // 2. Récupérer l'objectif pour CE jour précis
            // ATTENTION : Vous devez utiliser une méthode de votre DAO qui accepte la "date" en paramètre
            NutritionGoal dailyGoal = goalDao.getGoalByDate(currentUserUuid, date);

            // Si un objectif existe pour ce jour, on l'utilise. Sinon, on met 2000 par défaut.
            int targetCalories = (dailyGoal != null && dailyGoal.getCaloriesTarget() > 0) ? dailyGoal.getCaloriesTarget() : 2000;

            // 3. Ajouter les données au graphique
            seriesConsomme.getData().add(new XYChart.Data<>(dateString, caloriesConsommees));
            seriesObjectif.getData().add(new XYChart.Data<>(dateString, targetCalories));
        }

        // Ajouter les deux séries au graphique
        progressChart.getData().addAll(seriesObjectif, seriesConsomme);
    }

    // --- L'UNIQUE MÉTHODE toggleTheme() ---
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
    // --- NAVIGATION ---
    @FXML public void navToDashboard(ActionEvent event) { switchScene(event, "Dashboard.fxml"); }
    @FXML public void navToJournal(ActionEvent event) { switchScene(event, "Journal.fxml"); }
    @FXML public void navToPlanificateur(ActionEvent event) { switchScene(event, "Planificateur.fxml"); }
    @FXML public void navToObjectifs(ActionEvent event) { switchScene(event, "Objectif.fxml"); }
    @FXML public void navToAnalyse(ActionEvent event) { /* On est déjà sur Analyse */ }

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
}