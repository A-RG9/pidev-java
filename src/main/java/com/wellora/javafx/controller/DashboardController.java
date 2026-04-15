package com.wellora.javafx.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.StringConverter;

import com.wellora.javafx.WelloraApp;
import com.wellora.model.Healthjournal;

import com.wellora.dao.HealthjournalDAO;
import com.wellora.dao.HealthentryDAO;
import com.wellora.dao.SymptomDAO;
import com.wellora.model.Healthjournal;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DashboardController — handles the analytics dashboard view
 * 
 * Displays:
 * - Summary statistics (count of journals, entries, symptoms)
 * - Average weight and sleep data
 * - Bar chart: monthly entries (last 12 months)
 * - Pie chart: symptom type distribution
 * - Line charts: weight and glycemia trends
 */
public class DashboardController {

    private MainController mainController;

    // Teal color scheme
    private static final String TEAL_PRIMARY = "#00A790";
    private static final String TEAL_DARK = "#008B74";
    private static final String TEAL_LIGHT = "#E8F5F3";

    @FXML
    private ComboBox<Healthjournal> journalComboBox;

    @FXML
    private Label healthScoreLabel;

    @FXML
    private VBox alertContainer;

    @FXML
    private Label alertTitle;

    @FXML
    private Label alertMessage;

    @FXML
    private VBox recommendationsContainer;

    @FXML
    private VBox recommendationsList;

    @FXML
    private Label entryCountLabel;

    @FXML
    private Label symptomCountLabel;

    @FXML
    private Label avgWeightLabel;

    @FXML
    private Label avgSleepLabel;

    @FXML
    private LineChart<String, Number> sleepLineChart;

    @FXML
    private PieChart symptomPieChart;

    @FXML
    private LineChart<String, Number> weightLineChart;

    @FXML
    private LineChart<String, Number> glycemiaLineChart;

    private final HealthjournalDAO journalDAO = new HealthjournalDAO();
    private final HealthentryDAO entryDAO = new HealthentryDAO();
    private final SymptomDAO symptomDAO = new SymptomDAO();
    private Healthjournal selectedJournal;

    /**
     * Initialize the dashboard - called automatically via FXML
     * Loading styles and setting up charts happens when the view loads
     */
    @FXML
    public void initialize() {
        // Load journals into combo box
        loadJournalSelector();
        // Load data from database when FXML is loaded
        refreshDashboardData();
        applyStyles();
    }

    /**
     * Load journal selector with available journals
     */
    private void loadJournalSelector() {
        try {
            List<Healthjournal> journals = journalDAO.findAll();
            ObservableList<Healthjournal> items = FXCollections.observableArrayList(journals);
            journalComboBox.setItems(items);
            // Show journal name in dropdown
            journalComboBox.setConverter(new StringConverter<Healthjournal>() {
                @Override
                public String toString(Healthjournal j) {
                    return j != null ? j.getName() : "";
                }
                @Override
                public Healthjournal fromString(String s) {
                    return null;
                }
            });
            // Select first journal if available
            if (!journals.isEmpty()) {
                journalComboBox.getSelectionModel().selectFirst();
                selectedJournal = journals.get(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Handle journal selection from combo box
     */
    @FXML
    private void onJournalSelected() {
        selectedJournal = journalComboBox.getSelectionModel().getSelectedItem();
        if (selectedJournal != null) {
            refreshDashboardData();
        }
    }

    /**
     * Apply styling to match the Teal color scheme
     */
    private void applyStyles() {
        // Style will be applied when stage is configured
    }

    /**
     * Refresh all dashboard data - called on button click
     */
    @FXML
    private void refreshDashboard() {
        refreshDashboardData();
    }
    
    public void refreshDashboardData() {
        try {
            loadStatistics();
            loadSleepTrendChart();
            loadSymptomPieChart();
            loadWeightTrendChart();
            loadGlycemiaTrendChart();
            loadAlertsAndRecommendations();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur lors du chargement des données: " + e.getMessage());
        }
    }

    /**
     * Load alerts and recommendations based on health data
     */
    private void loadAlertsAndRecommendations() {
        try {
            // Get data
            double avgGlycemia = 0, avgSleep = 0, avgWeight = 0, avgTension = 0;
            int symptomCount = 0;
            int journalId = (selectedJournal != null) ? selectedJournal.getId() : 0;
            
            if (journalId > 0) {
                avgGlycemia = entryDAO.getAverageGlycemiaByJournal(journalId);
                avgSleep = entryDAO.getAverageSleepByJournal(journalId);
                avgWeight = entryDAO.getAverageWeightByJournal(journalId);
                avgTension = entryDAO.getAverageTensionByJournal(journalId);
                symptomCount = symptomDAO.getCountByJournal(journalId);
            } else {
                avgGlycemia = entryDAO.getAverageGlycemia();
                avgSleep = entryDAO.getAverageSleep();
                avgWeight = entryDAO.getAverageWeight();
                avgTension = entryDAO.getAverageTension();
                symptomCount = symptomDAO.getCount();
            }
            
            // Check alerts
            StringBuilder alerts = new StringBuilder();
            if (avgGlycemia > 1.2) alerts.append("Glycémie élevée! ");
            else if (avgGlycemia > 0 && avgGlycemia < 0.6) alerts.append("Glycémie basse! ");
            if (avgSleep < 6) alerts.append("Sommeil insuffisant! ");
            else if (avgSleep > 10) alerts.append("Sommeil excessif! ");
            if (avgWeight > 120) alerts.append("Poids élevé! ");
            else if (avgWeight > 0 && avgWeight < 40) alerts.append("Poids bas! ");
            if (symptomCount > 2) alerts.append("Plusieurs symptômes! ");
            
            if (alerts.length() > 0) {
                alertContainer.setVisible(true);
                alertMessage.setText(alerts.toString().trim());
            } else {
                alertContainer.setVisible(false);
            }
            
            // Build recommendations
            recommendationsList.getChildren().clear();
            java.util.List<String> tips = new java.util.ArrayList<>();
            if (avgGlycemia == 0 || avgGlycemia > 1.0) tips.add("Surveillez votre glycémie régulièrement");
            if (avgSleep < 7) tips.add("Visez 7-9h de sommeil par nuit");
            if (avgWeight > 100) tips.add("Envisagez une perte de poids");
            if (symptomCount > 0) tips.add("Notez vos symptômes quotidiennement");
            if (tips.isEmpty()) tips.add("Continuez vos bonnes habitudes!");
            
            for (String tip : tips) {
                Label tipLabel = new Label("• " + tip);
                tipLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #475569;");
                recommendationsList.getChildren().add(tipLabel);
            }
            
            recommendationsContainer.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Load summary statistics
     */
    private void loadStatistics() {
        try {
            int entryCount;
            int symptomCount;
            double avgWeight;
            double avgSleep;
            double healthScore = 0;
            
            if (selectedJournal != null) {
                // Filter by selected journal
                entryCount = entryDAO.getCountByJournal(selectedJournal.getId());
                symptomCount = symptomDAO.getCountByJournal(selectedJournal.getId());
                avgWeight = entryDAO.getAverageWeightByJournal(selectedJournal.getId());
                avgSleep = entryDAO.getAverageSleepByJournal(selectedJournal.getId());
                // Calculate health score
                healthScore = calculateHealthScore(selectedJournal.getId(), symptomCount);
            } else {
                // All journals
                entryCount = entryDAO.getCount();
                symptomCount = symptomDAO.getCount();
                avgWeight = entryDAO.getAverageWeight();
                avgSleep = entryDAO.getAverageSleep();
                // Calculate health score
                healthScore = calculateHealthScore(0, symptomCount);
            }

            healthScoreLabel.setText(String.valueOf((int) healthScore));
            entryCountLabel.setText(String.valueOf(entryCount));
            symptomCountLabel.setText(String.valueOf(symptomCount));
            avgWeightLabel.setText(String.format("%.1f kg", avgWeight));
            avgSleepLabel.setText(String.format("%.1f h", avgSleep));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Calculate health score based on metrics
     * Score from 0-100: Based on glycemia, sleep, weight, tension, symptoms
     */
    private double calculateHealthScore(int journalId, int symptomCount) {
        double score = 0;
        
        try {
            // Get average values
            double avgGlycemia;
            double avgSleep;
            double avgWeight;
            double avgTension;
            
            if (journalId > 0) {
                avgGlycemia = entryDAO.getAverageGlycemiaByJournal(journalId);
                avgSleep = entryDAO.getAverageSleepByJournal(journalId);
                avgWeight = entryDAO.getAverageWeightByJournal(journalId);
                avgTension = entryDAO.getAverageTensionByJournal(journalId);
            } else {
                avgGlycemia = entryDAO.getAverageGlycemia();
                avgSleep = entryDAO.getAverageSleep();
                avgWeight = entryDAO.getAverageWeight();
                avgTension = entryDAO.getAverageTension();
            }
            
            // Glycemia score (25 max): Ideal 0.7-1.0 g/L
            if (avgGlycemia > 0) {
                if (avgGlycemia >= 0.7 && avgGlycemia <= 1.0) {
                    score += 25;
                } else if (avgGlycemia >= 0.6 && avgGlycemia < 0.7) {
                    score += 15;
                } else if (avgGlycemia > 1.0 && avgGlycemia <= 1.2) {
                    score += 15;
                } else if (avgGlycemia < 0.5 || avgGlycemia > 1.3) {
                    score += 0;
                }
            }
            
            // Sleep score (25 max): Ideal 7-9 hours
            if (avgSleep > 0) {
                if (avgSleep >= 7 && avgSleep <= 9) {
                    score += 25;
                } else if (avgSleep >= 6 && avgSleep < 7) {
                    score += 15;
                } else if (avgSleep > 9 && avgSleep <= 10) {
                    score += 15;
                } else if (avgSleep < 5 || avgSleep > 11) {
                    score += 0;
                }
            }
            
            // Weight score (20 max): Ideal between 40-100 kg
            if (avgWeight > 0) {
                if (avgWeight >= 40 && avgWeight <= 100) {
                    score += 20;
                } else if (avgWeight > 100 && avgWeight <= 120) {
                    score += 10;
                } else if (avgWeight < 40) {
                    score += 10;
                }
            }
            
            // Tension score (20 max): Ideal 80-120
            if (avgTension > 0) {
                if (avgTension >= 80 && avgTension <= 120) {
                    score += 20;
                } else if (avgTension < 80) {
                    score += 10;
                }
            }
            
            // Symptoms penalty (-5 per symptom, max -10)
            score -= Math.min(symptomCount * 5, 10);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return Math.max(0, Math.min(100, score));
    }

    /**
     * Load sleep trend line chart
     */
    private void loadSleepTrendChart() {
        try {
            sleepLineChart.getData().clear();
            
            List<double[]> data;
            if (selectedJournal != null) {
                data = entryDAO.getSleepTrendByJournal(30, selectedJournal.getId());
            } else {
                data = entryDAO.getSleepTrend(30);
            }
            
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Sommeil (heures)");
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
            for (double[] row : data) {
                LocalDate date = LocalDate.ofEpochDay((long) row[0]);
                String dateStr = date.format(formatter);
                series.getData().add(new XYChart.Data<>(dateStr, row[1]));
            }
            
            sleepLineChart.getData().add(series);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Load symptom type distribution pie chart
     */
    private void loadSymptomPieChart() {
        try {
            symptomPieChart.getData().clear();
            
            List<Object[]> data;
            if (selectedJournal != null) {
                data = symptomDAO.getCountByTypeByJournal(selectedJournal.getId());
            } else {
                data = symptomDAO.getCountByType();
            }
            
            for (Object[] row : data) {
                String type = (String) row[0];
                long count = (Long) row[1];
                PieChart.Data slice = new PieChart.Data(type + " (" + count + ")", count);
                symptomPieChart.getData().add(slice);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    
    /**
     * Load weight trend line chart
     */
    private void loadWeightTrendChart() {
        try {
            weightLineChart.getData().clear();
            
            List<double[]> data;
            if (selectedJournal != null) {
                data = entryDAO.getWeightTrendByJournal(30, selectedJournal.getId());
            } else {
                data = entryDAO.getWeightTrend(30);
            }
            
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Poids (kg)");
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
            for (double[] row : data) {
                LocalDate date = LocalDate.ofEpochDay((long) row[0]);
                String dateStr = date.format(formatter);
                series.getData().add(new XYChart.Data<>(dateStr, row[1]));
            }
            
            weightLineChart.getData().add(series);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Load glycemia trend line chart
     */
    private void loadGlycemiaTrendChart() {
        try {
            glycemiaLineChart.getData().clear();
            
            List<double[]> data;
            if (selectedJournal != null) {
                data = entryDAO.getGlycemiaTrendByJournal(30, selectedJournal.getId());
            } else {
                data = entryDAO.getGlycemiaTrend(30);
            }
            
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Glycémie (g/L)");
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
            for (double[] row : data) {
                LocalDate date = LocalDate.ofEpochDay((long) row[0]);
                String dateStr = date.format(formatter);
                series.getData().add(new XYChart.Data<>(dateStr, row[1]));
            }
            
            glycemiaLineChart.getData().add(series);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Navigate back to main menu (using single window navigation)
     */
    @FXML
    private void goBackToMain() {
        if (mainController != null) {
            mainController.showHomepage();
        }
    }

    /**
     * Set reference to main controller for navigation
     */
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    /**
     * Apply main view styles
     */
    private void applyMainStyles(Scene scene) {
        scene.getStylesheets().add(
                WelloraApp.class.getResource("/fxml/dashboard.css").toExternalForm());
    }

    /**
     * Show error message
     */
    private void showError(String message) {
        try {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Une erreur s'est produite");
            alert.setContentText(message);
            alert.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }}