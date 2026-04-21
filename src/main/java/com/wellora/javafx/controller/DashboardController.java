package com.wellora.javafx.controller;

import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.StringConverter;

import com.wellora.model.Healthjournal;
import com.wellora.dao.HealthjournalDAO;
import com.wellora.dao.HealthentryDAO;
import com.wellora.dao.SymptomDAO;
import com.wellora.services.ApiService; // 🔥 NEW

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DashboardController {

    private MainController mainController;

    @FXML private ComboBox<Healthjournal> journalComboBox;
    @FXML private Label healthScoreLabel;
    @FXML private Label quoteLabel;
    @FXML private Label apiAdviceLabel;
    @FXML private Label riskLabel;             // 🔥 NEW - risk indicator

    @FXML private VBox alertContainer;
    @FXML private Label alertMessage;

    @FXML private VBox recommendationsContainer;
    @FXML private VBox recommendationsList;

    @FXML private Label entryCountLabel;
    @FXML private Label symptomCountLabel;
    @FXML private Label avgWeightLabel;
    @FXML private Label avgSleepLabel;

    @FXML private LineChart<String, Number> sleepLineChart;
    @FXML private PieChart symptomPieChart;
    @FXML private LineChart<String, Number> weightLineChart;
    @FXML private LineChart<String, Number> glycemiaLineChart;

    private final HealthjournalDAO journalDAO = new HealthjournalDAO();
    private final HealthentryDAO entryDAO = new HealthentryDAO();
    private final SymptomDAO symptomDAO = new SymptomDAO();

    private final ApiService apiService = new ApiService(); // 🔥 NEW

    private Healthjournal selectedJournal;
    private int currentHealthScore = 0; // 🔥 IMPORTANT

    // =========================
    // 🚀 SET MAIN CONTROLLER
    // =========================

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    // =========================
    // 🚀 INITIALIZE
    // =========================
    @FXML
    public void initialize() {
        loadJournalSelector();
        refreshDashboardData();
    }

    // =========================
    // 🔄 REFRESH (FXML)
    // =========================
    @FXML
    public void refreshDashboard() {
        refreshDashboardData();
    }

    @FXML
    private void goBackToMain() {
        if (mainController != null) {
            mainController.showHomepage();
        }
    }

    // =========================
    // 🔄 MAIN REFRESH
    // =========================
    public void refreshDashboardData() {
        try {
            loadStatistics();
            loadCharts();
            loadAlertsAndRecommendations();
            loadApiData(); // 🔥 NEW

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =========================
    // 📊 STATISTICS + SCORE
    // =========================
    private void loadStatistics() {
        try {
            int entryCount;
            int symptomCount;
            double avgWeight;
            double avgSleep;

            if (selectedJournal != null) {
                entryCount = entryDAO.getCountByJournal(selectedJournal.getId());
                symptomCount = symptomDAO.getCountByJournal(selectedJournal.getId());
                avgWeight = entryDAO.getAverageWeightByJournal(selectedJournal.getId());
                avgSleep = entryDAO.getAverageSleepByJournal(selectedJournal.getId());
                currentHealthScore = (int) calculateHealthScore(selectedJournal.getId(), symptomCount);
            } else {
                entryCount = entryDAO.getCount();
                symptomCount = symptomDAO.getCount();
                avgWeight = entryDAO.getAverageWeight();
                avgSleep = entryDAO.getAverageSleep();
                currentHealthScore = (int) calculateHealthScore(0, symptomCount);
            }

            healthScoreLabel.setText(String.valueOf(currentHealthScore));

            // Color-code health score
            if (currentHealthScore < 40) {
                healthScoreLabel.setStyle("-fx-text-fill: #e74c3c;");
            } else if (currentHealthScore < 70) {
                healthScoreLabel.setStyle("-fx-text-fill: #f39c12;");
            } else {
                healthScoreLabel.setStyle("-fx-text-fill: #27ae60;");
            }
            entryCountLabel.setText(String.valueOf(entryCount));
            symptomCountLabel.setText(String.valueOf(symptomCount));
            avgWeightLabel.setText(String.format("%.1f kg", avgWeight));
            avgSleepLabel.setText(String.format("%.1f h", avgSleep));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =========================
    // 🌐 API INTEGRATION
    // =========================
    private void loadApiData() {
        try {
            String quote = apiService.getQuote();
            String advice = apiService.getHealthAdvice(currentHealthScore);
            String riskLevel = apiService.getRiskLevel(currentHealthScore);
            String riskDesc = apiService.getRiskDescription(currentHealthScore);

            quoteLabel.setText(quote);
            apiAdviceLabel.setText(advice);
            riskLabel.setText(riskLevel);

            // Set risk color based on level
            if ("HIGH".equals(riskLevel)) {
                riskLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            } else if ("MODERATE".equals(riskLevel)) {
                riskLabel.setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
            } else if ("LOW".equals(riskLevel)) {
                riskLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
            } else {
                riskLabel.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
            }

        } catch (Exception e) {
            quoteLabel.setText("Restez positif et prenez soin de vous!");
            apiAdviceLabel.setText("Maintenez un style de vie sain.");
            riskLabel.setText("N/A");
        }
    }

    // =========================
    // ⚠️ ALERTS + RECOMMENDATIONS
    // =========================
    private void loadAlertsAndRecommendations() {
        try {
            int symptomCount = (selectedJournal != null)
                    ? symptomDAO.getCountByJournal(selectedJournal.getId())
                    : symptomDAO.getCount();

            if (symptomCount > 2) {
                alertContainer.setVisible(true);
                alertMessage.setText("Plusieurs symptômes détectés !");
            } else {
                alertContainer.setVisible(false);
            }

            recommendationsList.getChildren().clear();

            if (currentHealthScore < 50) {
                addRecommendation("Reposez-vous davantage");
                addRecommendation("Hydratez-vous");
            } else {
                addRecommendation("Continuez vos bonnes habitudes");
            }

            recommendationsContainer.setVisible(true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addRecommendation(String text) {
        Label label = new Label("• " + text);
        label.setStyle("-fx-font-size: 13px;");
        recommendationsList.getChildren().add(label);
    }

    // =========================
    // 📊 CHARTS
    // =========================
    private void loadCharts() {
        loadSleepTrendChart();
        loadSymptomPieChart();
        loadWeightTrendChart();
        loadGlycemiaTrendChart();
    }

    private void loadSleepTrendChart() {
        sleepLineChart.getData().clear();
        try {
            List<double[]> data = (selectedJournal != null)
                    ? entryDAO.getSleepTrendByJournal(30, selectedJournal.getId())
                    : entryDAO.getSleepTrend(30);

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");

            for (double[] row : data) {
                LocalDate date = LocalDate.ofEpochDay((long) row[0]);
                series.getData().add(new XYChart.Data<>(date.format(formatter), row[1]));
            }

            sleepLineChart.getData().add(series);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadSymptomPieChart() {
        symptomPieChart.getData().clear();
        try {
            List<Object[]> data = (selectedJournal != null)
                    ? symptomDAO.getCountByTypeByJournal(selectedJournal.getId())
                    : symptomDAO.getCountByType();

            for (Object[] row : data) {
                symptomPieChart.getData().add(
                        new PieChart.Data((String) row[0], (Long) row[1])
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadWeightTrendChart() {
        weightLineChart.getData().clear();
        try {
            List<double[]> data = (selectedJournal != null)
                    ? entryDAO.getWeightTrendByJournal(30, selectedJournal.getId())
                    : entryDAO.getWeightTrend(30);

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");

            for (double[] row : data) {
                LocalDate date = LocalDate.ofEpochDay((long) row[0]);
                series.getData().add(new XYChart.Data<>(date.format(formatter), row[1]));
            }

            weightLineChart.getData().add(series);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadGlycemiaTrendChart() {
        glycemiaLineChart.getData().clear();
        try {
            List<double[]> data = (selectedJournal != null)
                    ? entryDAO.getGlycemiaTrendByJournal(30, selectedJournal.getId())
                    : entryDAO.getGlycemiaTrend(30);

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");

            for (double[] row : data) {
                LocalDate date = LocalDate.ofEpochDay((long) row[0]);
                series.getData().add(new XYChart.Data<>(date.format(formatter), row[1]));
            }

            glycemiaLineChart.getData().add(series);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // =========================
    // 🧠 SCORE CALCULATION
    // =========================
    private double calculateHealthScore(int journalId, int symptomCount) {
        double score = 100;

        // Factor 1: Symptom count (max 30 points)
        score -= Math.min(symptomCount * 5, 30);

        // Factor 2: Weight evaluation (max 20 points)
        try {
            double avgWeight = (selectedJournal != null)
                    ? entryDAO.getAverageWeightByJournal(selectedJournal.getId())
                    : entryDAO.getAverageWeight();
            if (avgWeight > 0) {
                // Consider healthy weight range: 18.5-25 BMI equivalent
                if (avgWeight < 40 || avgWeight > 120) score -= 20; // Abnormal weight
                else if (avgWeight < 50 || avgWeight > 100) score -= 10; // Slightly off
            }
        } catch (Exception e) { /* ignore */ }

        // Factor 3: Sleep evaluation (max 15 points)
        try {
            double avgSleep = (selectedJournal != null)
                    ? entryDAO.getAverageSleepByJournal(selectedJournal.getId())
                    : entryDAO.getAverageSleep();
            if (avgSleep > 0) {
                if (avgSleep < 5 || avgSleep > 10) score -= 15; // Too little or too much
                else if (avgSleep < 6 || avgSleep > 9) score -= 10; // Insufficient
            }
        } catch (Exception e) { /* ignore */ }

        // Factor 4: Glycemia evaluation (max 15 points)
        try {
            double avgGlycemia = (selectedJournal != null)
                    ? entryDAO.getAverageGlycemiaByJournal(selectedJournal.getId())
                    : entryDAO.getAverageGlycemia();
            if (avgGlycemia > 0) {
                if (avgGlycemia > 1.4) score -= 15; // High glycemia
                else if (avgGlycemia > 1.1) score -= 10; // Pre-diabetic range
            }
        } catch (Exception e) { /* ignore */ }

        return Math.max(0, Math.min(100, score));
    }

    // =========================
    // 📋 JOURNAL SELECTOR
    // =========================
    private void loadJournalSelector() {
        try {
            List<Healthjournal> journals = journalDAO.findAll();
            ObservableList<Healthjournal> items = FXCollections.observableArrayList(journals);

            journalComboBox.setItems(items);

            journalComboBox.setConverter(new StringConverter<>() {
                @Override
                public String toString(Healthjournal j) {
                    return j != null ? j.getName() : "";
                }

                @Override
                public Healthjournal fromString(String s) {
                    return null;
                }
            });

            if (!journals.isEmpty()) {
                journalComboBox.getSelectionModel().selectFirst();
                selectedJournal = journals.get(0);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onJournalSelected() {
        selectedJournal = journalComboBox.getSelectionModel().getSelectedItem();
        refreshDashboardData();
    }
}