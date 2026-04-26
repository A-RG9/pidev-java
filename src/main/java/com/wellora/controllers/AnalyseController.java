package com.wellora.controllers;

import com.wellora.dao.FoodLogDAO;
import com.wellora.dao.NutritionGoalDAO;
import com.wellora.dao.MealPlanDAO;
import com.wellora.models.NutritionGoal;
import com.wellora.models.MealPlan;
import com.wellora.models.DailyPlan;
import com.wellora.services.PdfExportService;
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
import java.util.ArrayList;
import java.util.List;

public class AnalyseController {

    @FXML private BorderPane rootPane;
    @FXML private ToggleButton btnThemeToggle;
    @FXML private BarChart<String, Number> progressChart;

    private final FoodLogDAO foodLogDao = new FoodLogDAO();
    private final NutritionGoalDAO goalDao = new NutritionGoalDAO();
    private final MealPlanDAO mealPlanDao = new MealPlanDAO(); // Ajout du DAO pour le PDF

    private final String currentUserUuid = "c513e200-d605-4f61-9efc-d539f0e80914";

    @FXML
    public void initialize() {
        // --- GESTION DU THEME GLOBAL ---
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
            String dateString = date.format(formatter);

            // 1. Récupérer les calories consommées
            double[] totals = foodLogDao.getDailyTotals(currentUserUuid, date);
            double caloriesConsommees = totals[0];

            // 2. Récupérer l'objectif
            NutritionGoal dailyGoal = goalDao.getGoalByDate(currentUserUuid, date);
            int targetCalories = (dailyGoal != null && dailyGoal.getCaloriesTarget() > 0) ? dailyGoal.getCaloriesTarget() : 2000;

            // 3. Ajouter les données au graphique
            seriesConsomme.getData().add(new XYChart.Data<>(dateString, caloriesConsommees));
            seriesObjectif.getData().add(new XYChart.Data<>(dateString, targetCalories));
        }

        progressChart.getData().addAll(seriesObjectif, seriesConsomme);
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

    // ==========================================
    // EXPORT PDF : GRAPHIQUE + 7 DERNIERS JOURS
    // ==========================================
    @FXML
    public void downloadPdf(ActionEvent event) {
        List<DailyPlan> weeklyPlan = new ArrayList<>();
        DateTimeFormatter displayFormat = DateTimeFormatter.ofPattern("EEEE dd MMM");

        // Boucle sur les 7 derniers jours
        for (int i = 6; i >= 0; i--) {
            LocalDate pastDate = LocalDate.now().minusDays(i);
            DailyPlan dayPlan = new DailyPlan(pastDate.format(displayFormat));

            // On récupère les plats de ce jour depuis la BDD
            List<MealPlan> mealsOfTheDay = mealPlanDao.getPlansByDate(currentUserUuid, pastDate);

            // On trie chaque plat dans le bon moment de la journée
            for (MealPlan meal : mealsOfTheDay) {
                String type = meal.getMealType();

                // CRÉATION DU TEXTE AVEC LE STATUT
                String status = meal.isCompleted() ? "\n✅ Terminé" : "\n⏳ En cours";
                String mealText = meal.getName() + status;

                if (type != null) {
                    switch (type.toLowerCase()) {
                        case "breakfast": dayPlan.breakfast = mealText; break;
                        case "lunch": dayPlan.lunch = mealText; break;
                        case "dinner": dayPlan.dinner = mealText; break;
                        case "snack": dayPlan.snack = mealText; break;
                    }
                }
                dayPlan.totalCalories += meal.getCalories();
            }
            weeklyPlan.add(dayPlan);
        }

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        PdfExportService pdfService = new PdfExportService();

        pdfService.exportPlanToPdf(stage, progressChart, weeklyPlan);
    }

    // --- NAVIGATION ---
    @FXML public void navToDashboard(ActionEvent event) { switchScene(event, "Dashboard.fxml"); }
    @FXML public void navToJournal(ActionEvent event) { switchScene(event, "Journal.fxml"); }
    @FXML public void navToPlanificateur(ActionEvent event) { switchScene(event, "Planificateur.fxml"); }
    @FXML public void navToRecettes(ActionEvent event) { switchScene(event, "Recettes.fxml"); }
    @FXML public void navToObjectifs(ActionEvent event) { switchScene(event, "Objectif.fxml"); }
    @FXML public void navToAnalyse(ActionEvent event) { /* On est déjà sur Analyse */ }

    private void switchScene(ActionEvent event, String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/wellora/views/" + fxmlFile));
            Parent root = loader.load();

            String cssPath = getClass().getResource("/com/wellora/css/style.css").toExternalForm();
            if (!root.getStylesheets().contains(cssPath)) {
                root.getStylesheets().add(cssPath);
            }

            boolean isLightMode = btnThemeToggle.isSelected();
            if (isLightMode) {
                if (!root.getStyleClass().contains("light-theme")) {
                    root.getStyleClass().add("light-theme");
                }
            } else {
                root.getStyleClass().remove("light-theme");
            }

            ToggleButton nextBtnTheme = (ToggleButton) root.lookup("#btnThemeToggle");
            if (nextBtnTheme != null) {
                nextBtnTheme.setSelected(isLightMode);
                nextBtnTheme.setText(isLightMode ? "🌙 Mode Sombre" : "☀️ Mode Clair");
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);

        } catch (IOException e) {
            System.err.println("❌ Impossible de charger la page : " + fxmlFile);
            e.printStackTrace();
        }
    }


}