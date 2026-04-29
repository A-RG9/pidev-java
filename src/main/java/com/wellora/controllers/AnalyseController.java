package com.wellora.controllers;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Label; // <-- NOUVEL IMPORT
import com.wellora.dao.FoodLogDAO;
import com.wellora.dao.NutritionGoalDAO;
import com.wellora.dao.MealPlanDAO;
import com.wellora.models.NutritionGoal;
import com.wellora.models.MealPlan;
import com.wellora.models.DailyPlan;
import com.wellora.services.PdfExportService;
import com.wellora.services.AiService;
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

    // NOUVEAU : Le composant qui va afficher le texte de l'IA
    @FXML private Label aiResponseLabel;

    private final FoodLogDAO foodLogDao = new FoodLogDAO();
    private final NutritionGoalDAO goalDao = new NutritionGoalDAO();
    private final MealPlanDAO mealPlanDao = new MealPlanDAO();

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

        loadChartData();
    }

    private void loadChartData() {
        progressChart.getData().clear();

        XYChart.Series<String, Number> seriesConsomme = new XYChart.Series<>();
        seriesConsomme.setName("Calories Consommées");

        XYChart.Series<String, Number> seriesObjectif = new XYChart.Series<>();
        seriesObjectif.setName("Objectif Calories");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");

        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            String dateString = date.format(formatter);

            double[] totals = foodLogDao.getDailyTotals(currentUserUuid, date);
            double caloriesConsommees = totals[0];

            NutritionGoal dailyGoal = goalDao.getGoalByDate(currentUserUuid, date);
            int targetCalories = (dailyGoal != null && dailyGoal.getCaloriesTarget() > 0) ? dailyGoal.getCaloriesTarget() : 2000;

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
    // EXPORT PDF
    // ==========================================
    @FXML
    public void downloadPdf(ActionEvent event) {
        List<DailyPlan> weeklyPlan = new ArrayList<>();
        DateTimeFormatter displayFormat = DateTimeFormatter.ofPattern("EEEE dd MMM");

        for (int i = 6; i >= 0; i--) {
            LocalDate pastDate = LocalDate.now().minusDays(i);
            DailyPlan dayPlan = new DailyPlan(pastDate.format(displayFormat));

            List<MealPlan> mealsOfTheDay = mealPlanDao.getPlansByDate(currentUserUuid, pastDate);

            for (MealPlan meal : mealsOfTheDay) {
                String type = meal.getMealType();
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

    // ==========================================
    // IA - COACH NUTRITIONNEL (Basé sur le Journal)
    // ==========================================
    @FXML
    public void askAiForAdvice(ActionEvent event) {
        StringBuilder nutritionData = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");

        boolean hasData = false;

        for (int i = 6; i >= 0; i--) {
            LocalDate pastDate = LocalDate.now().minusDays(i);

            // 1. On lit depuis le Journal Alimentaire (FoodLogDAO) au lieu du Planificateur
            double[] totals = foodLogDao.getDailyTotals(currentUserUuid, pastDate);
            double caloriesConsommees = totals[0];

            // 2. On récupère l'objectif de la journée pour que l'IA puisse comparer
            NutritionGoal dailyGoal = goalDao.getGoalByDate(currentUserUuid, pastDate);
            int targetCalories = (dailyGoal != null && dailyGoal.getCaloriesTarget() > 0) ? dailyGoal.getCaloriesTarget() : 2000;

            // S'il y a eu des calories consommées ce jour-là, on les ajoute au prompt
            if (caloriesConsommees > 0) {
                hasData = true;
                nutritionData.append("Jour ").append(pastDate.format(formatter)).append(" : ");
                nutritionData.append(caloriesConsommees).append(" kcal consommées ");
                nutritionData.append("(Objectif : ").append(targetCalories).append(" kcal). ");

                // Si ta méthode getDailyTotals renvoie aussi les macros aux index 1, 2, 3 (Protéines, Glucides, Lipides), on les donne à l'IA !
                if (totals.length >= 4) {
                    nutritionData.append("Macros -> Protéines: ").append(totals[1]).append("g, ")
                            .append("Glucides: ").append(totals[2]).append("g, ")
                            .append("Lipides: ").append(totals[3]).append("g.");
                }
                nutritionData.append("\n");
            }
        }

        if (!hasData) {
            aiResponseLabel.setText("❌ Le coach a besoin de données ! Remplis ton Journal Alimentaire des 7 derniers jours.");
            return;
        }

        // On affiche un texte d'attente sur l'interface
        aiResponseLabel.setText("⏳ Le coach analyse ton journal alimentaire, un instant...");

        new Thread(() -> {
            AiService aiService = new AiService();

            // On envoie un prompt clair avec les données réelles
            String prompt = "Agis comme un coach en nutrition. Voici mon journal alimentaire des derniers jours. " +
                    "Fais-moi une analyse courte et donne-moi un conseil pertinent basé sur ces données :\n" +
                    nutritionData.toString();

            String aiResponse = aiService.getNutritionalCoachAdvice(prompt);

            // On met à jour l'interface avec la réponse
            Platform.runLater(() -> {
                aiResponseLabel.setText("🧑‍⚕️ Coach : " + aiResponse);
            });
        }).start();
    }
}