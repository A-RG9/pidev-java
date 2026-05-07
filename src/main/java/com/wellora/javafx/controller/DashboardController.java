package com.wellora.javafx.controller;

import javafx.fxml.FXML;
import javafx.scene.Parent;
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
import com.wellora.model.Healthentry;
import com.wellora.dao.HealthjournalDAO;
import com.wellora.dao.HealthentryDAO;
import com.wellora.dao.SymptomDAO;
import com.wellora.model.Symptom;
import com.wellora.services.ApiService;
import com.wellora.controllers.BaseController;
import com.wellcare.javafx.util.SceneManager;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DashboardController extends BaseController {

    private MainController mainController;

    @FXML private ComboBox<Healthjournal> journalComboBox;
    @FXML private Label healthScoreLabel;
    @FXML private Label quoteLabel;
    @FXML private Label riskLabel;

    @FXML private VBox alertContainer;
    @FXML private Label alertTitle;
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

    private Healthjournal selectedJournal;
    private final ApiService apiService = new ApiService();

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @Override
    protected Parent getRoot() {
        return null; // Not used in Dashboard context - theme is applied via mainController
    }

    @FXML
    public void initialize() {
        loadJournalSelector();
        refreshDashboardData();
    }

    @FXML
    public void refreshDashboard() {
        refreshDashboardData();
    }

    @FXML
    private void exportPdf() {
        if (selectedJournal != null) {
            try {
                javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
                fileChooser.setTitle("Export Health Report");
                fileChooser.getExtensionFilters().add(
                        new javafx.stage.FileChooser.ExtensionFilter("PDF Files", "*.pdf")
                );
                fileChooser.setInitialFileName("health_report_" +
                        selectedJournal.getName().replaceAll("\\s+", "_") + ".pdf");

                javafx.stage.Stage stage = (javafx.stage.Stage) alertContainer.getScene().getWindow();
                java.io.File file = fileChooser.showSaveDialog(stage);

                if (file != null) {
                    com.wellora.services.PdfExportService exportService = new com.wellora.services.PdfExportService();
                    exportService.exportHealthReport(selectedJournal, file.getAbsolutePath());
                    alertMessage.setText("PDF exported successfully to: " + file.getAbsolutePath());
                    alertMessage.setStyle("-fx-text-fill: green;");
                }
            } catch (Exception e) {
                alertMessage.setText("Error exporting PDF: " + e.getMessage());
                alertMessage.setStyle("-fx-text-fill: red;");
                e.printStackTrace();
            }
        } else {
            alertMessage.setText("Please select a journal first.");
            alertMessage.setStyle("-fx-text-fill: orange;");
        }
    }

    public void refreshDashboardData() {
        try {
            loadStatistics();
            loadCharts();
            loadAlertsAndRecommendations();
            loadApiData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadStatistics() {
        try {
            String userId = SceneManager.getInstance().getCurrentUser().getUuid();
            int entryCount;
            int symptomCount;
            double avgWeight;
            double avgSleep;

            if (selectedJournal != null) {
                entryCount = entryDAO.getCountByJournal(selectedJournal.getId());
                symptomCount = symptomDAO.getCountByJournal(selectedJournal.getId());
                avgWeight = entryDAO.getAverageWeightByJournal(selectedJournal.getId());
                avgSleep = entryDAO.getAverageSleepByJournal(selectedJournal.getId());
            } else {
                entryCount = entryDAO.getTotalCount(userId);
                symptomCount = symptomDAO.getCount(userId);
                avgWeight = entryDAO.getOverallAverageWeight(userId);
                avgSleep = entryDAO.getOverallAverageSleep(userId);
            }

            entryCountLabel.setText(String.valueOf(entryCount));
            symptomCountLabel.setText(String.valueOf(symptomCount));
            avgWeightLabel.setText(String.format("%.1f kg", avgWeight));
            avgSleepLabel.setText(String.format("%.1f h", avgSleep));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadCharts() {
        try {
            String userId = SceneManager.getInstance().getCurrentUser().getUuid();
            if (selectedJournal != null) {
                loadSleepChart(selectedJournal.getId(), userId);
                loadWeightChart(selectedJournal.getId(), userId);
                loadGlycemiaChart(selectedJournal.getId(), userId);
                loadSymptomChart(selectedJournal.getId(), userId);
            } else {
                loadSleepChart(null, userId);
                loadWeightChart(null, userId);
                loadGlycemiaChart(null, userId);
                loadSymptomChart(null, userId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadSleepChart(Integer journalId, String userId) throws SQLException {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Sleep Hours");

        List<Healthentry> entries = (journalId != null)
                ? entryDAO.findByJournalId(journalId)
                : entryDAO.findAll(userId);

        entries.sort((a, b) -> a.getDate().compareTo(b.getDate()));
        int start = Math.max(0, entries.size() - 30);

        for (int i = start; i < entries.size(); i++) {
            Healthentry entry = entries.get(i);
            String dateStr = entry.getDate().format(DateTimeFormatter.ofPattern("MM-dd"));
            series.getData().add(new XYChart.Data<>(dateStr, entry.getSommeil()));
        }

        sleepLineChart.getData().clear();
        sleepLineChart.getData().add(series);
    }

    private void loadWeightChart(Integer journalId, String userId) throws SQLException {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Weight (kg)");

        List<Healthentry> entries = (journalId != null)
                ? entryDAO.findByJournalId(journalId)
                : entryDAO.findAll(userId);

        entries.sort((a, b) -> a.getDate().compareTo(b.getDate()));
        int start = Math.max(0, entries.size() - 30);

        for (int i = start; i < entries.size(); i++) {
            Healthentry entry = entries.get(i);
            String dateStr = entry.getDate().format(DateTimeFormatter.ofPattern("MM-dd"));
            series.getData().add(new XYChart.Data<>(dateStr, entry.getPoids()));
        }

        weightLineChart.getData().clear();
        weightLineChart.getData().add(series);
    }

    private void loadGlycemiaChart(Integer journalId, String userId) throws SQLException {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Glycemia (g/L)");

        List<Healthentry> entries = (journalId != null)
                ? entryDAO.findByJournalId(journalId)
                : entryDAO.findAll(userId);

        entries.sort((a, b) -> a.getDate().compareTo(b.getDate()));
        int start = Math.max(0, entries.size() - 30);

        for (int i = start; i < entries.size(); i++) {
            Healthentry entry = entries.get(i);
            String dateStr = entry.getDate().format(DateTimeFormatter.ofPattern("MM-dd"));
            series.getData().add(new XYChart.Data<>(dateStr, entry.getGlycemie()));
        }

        glycemiaLineChart.getData().clear();
        glycemiaLineChart.getData().add(series);
    }

    private void loadSymptomChart(Integer journalId, String userId) throws SQLException {
        symptomPieChart.getData().clear();

        List<Object[]> counts = (journalId != null)
                ? symptomDAO.getCountByTypeByJournal(journalId)
                : symptomDAO.getCountByType(userId);

        for (Object[] item : counts) {
            String type = (String) item[0];
            Long count = (Long) item[1];
            PieChart.Data slice = new PieChart.Data(type, count.doubleValue());
            symptomPieChart.getData().add(slice);
        }
    }

    private void loadAlertsAndRecommendations() {
        try {
            String userId = SceneManager.getInstance().getCurrentUser().getUuid();
            List<Healthentry> recentEntries = entryDAO.findAll(userId);
            recentEntries.sort((a, b) -> b.getDate().compareTo(a.getDate()));

            boolean hasAlerts = false;
            StringBuilder alerts = new StringBuilder();

            for (int i = 0; i < Math.min(5, recentEntries.size()); i++) {
                Healthentry entry = recentEntries.get(i);

                if (entry.getGlycemie() > 1.2) {
                    hasAlerts = true;
                    alerts.append("High blood sugar: ").append(String.format("%.2f", entry.getGlycemie())).append(" g/L on ").append(entry.getDate()).append("\n");
                }
                if (entry.getPoids() > 100) {
                    hasAlerts = true;
                    alerts.append("High weight: ").append(String.format("%.1f", entry.getPoids())).append(" kg on ").append(entry.getDate()).append("\n");
                }
                if (entry.getSommeil() < 5) {
                    hasAlerts = true;
                    alerts.append("Low sleep: ").append(entry.getSommeil()).append("h on ").append(entry.getDate()).append("\n");
                }
            }

            if (hasAlerts) {
                alertContainer.setVisible(true);
                alertTitle.setText("Health Alerts");
                alertMessage.setText(alerts.toString());
                alertMessage.setStyle("-fx-text-fill: #d32f2f;");
            } else {
                alertContainer.setVisible(false);
            }

            recommendationsContainer.setVisible(true);
            recommendationsList.getChildren().clear();

            if (recentEntries.size() > 0) {
                Healthentry latest = recentEntries.get(0);

                if (latest.getSommeil() < 7) {
                    addRecommendation("Try to get at least 7-8 hours of sleep for better health.");
                }
                if (latest.getPoids() > 85) {
                    addRecommendation("Consider a balanced diet and regular exercise to maintain a healthy weight.");
                }
                if (latest.getGlycemie() > 1.0) {
                    addRecommendation("Monitor your blood sugar levels and reduce sugar intake.");
                }

                List<Symptom> recentSymptoms = symptomDAO.findByEntryId(latest.getId());
                if (!recentSymptoms.isEmpty()) {
                    addRecommendation("You have reported symptoms recently. Consider consulting a healthcare provider if they persist.");
                }
            }

            if (recommendationsList.getChildren().isEmpty()) {
                addRecommendation("Keep up the good work! Your health metrics look good.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addRecommendation(String text) {
        Label recLabel = new Label("• " + text);
        recLabel.setWrapText(true);
        recLabel.setStyle("-fx-text-fill: #2e7d32; -fx-font-size: 13px;");
        recommendationsList.getChildren().add(recLabel);
    }

    private void loadApiData() {
        try {
            int healthScore = calculateHealthScore();
            healthScoreLabel.setText(String.valueOf(healthScore));

            String quote = apiService.getQuote();
            if (quote != null && !quote.isEmpty()) {
                quoteLabel.setText(quote);
            } else {
                quoteLabel.setText("Stay healthy and keep tracking your wellness journey!");
            }

            String risk = apiService.getRiskAssessment(healthScore);
            if (risk != null && !risk.isEmpty()) {
                riskLabel.setText(risk);
            } else {
                riskLabel.setText("Risk Level: Low");
            }
        } catch (Exception e) {
            quoteLabel.setText("Stay healthy and keep tracking your wellness journey!");
            riskLabel.setText("Risk Level: Low");
        }
    }

    private int calculateHealthScore() {
        try {
            String userId = SceneManager.getInstance().getCurrentUser().getUuid();
            List<Healthentry> entries;
            if (selectedJournal != null) {
                entries = entryDAO.findByJournalId(selectedJournal.getId());
            } else {
                entries = entryDAO.findAll(userId);
            }

            if (entries == null || entries.isEmpty()) {
                return 0;
            }

            double totalScore = 0;
            int count = 0;

            for (Healthentry entry : entries) {
                int score = 100;

                if (entry.getPoids() > 0) {
                    if (entry.getPoids() < 40 || entry.getPoids() > 120) {
                        score -= 20;
                    } else if (entry.getPoids() < 50 || entry.getPoids() > 100) {
                        score -= 10;
                    }
                }

                if (entry.getSommeil() > 0) {
                    if (entry.getSommeil() < 5 || entry.getSommeil() > 10) {
                        score -= 15;
                    } else if (entry.getSommeil() < 6 || entry.getSommeil() > 9) {
                        score -= 10;
                    }
                }

                if (entry.getGlycemie() > 0) {
                    if (entry.getGlycemie() > 1.4) {
                        score -= 15;
                    } else if (entry.getGlycemie() > 1.1) {
                        score -= 10;
                    }
                }

                totalScore += Math.max(0, Math.min(100, score));
                count++;
            }

            return (int) Math.round(totalScore / count);
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private void loadJournalSelector() {
        try {
            String userId = SceneManager.getInstance().getCurrentUser().getUuid();
            ObservableList<Healthjournal> journals = FXCollections.observableArrayList(journalDAO.findAll(userId));
            journalComboBox.setItems(journals);
            journalComboBox.setConverter(new StringConverter<Healthjournal>() {
                @Override
                public String toString(Healthjournal journal) {
                    return journal != null ? journal.getName() : "";
                }

                @Override
                public Healthjournal fromString(String string) {
                    return null;
                }
            });

            if (!journals.isEmpty()) {
                journalComboBox.getSelectionModel().selectFirst();
                selectedJournal = journalComboBox.getSelectionModel().getSelectedItem();
            }

            journalComboBox.setOnAction(event -> {
                selectedJournal = journalComboBox.getSelectionModel().getSelectedItem();
                refreshDashboardData();
            });

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onJournalSelected() {
        refreshDashboardData();
    }

    @FXML
    private void goBackToMain() {
        if (mainController != null) {
            mainController.showHomepage();
        }
    }
}