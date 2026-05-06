package com.wellcare.javafx.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.wellcare.javafx.service.DoctorAnalyticsService;

public class doc {

    // ========== FXML Components ==========
    @FXML private Label aiStatusLabel;
    @FXML private TextField patientSearchField;
    @FXML private ComboBox<String> alertFilterCombo;

    @FXML private Label totalPatientsValue;
    @FXML private Label criticalAlertsValue;
    @FXML private Label todayAppointmentsValue;
    @FXML private Label reportsGeneratedValue;

    @FXML private HBox predictionsContainer;
    @FXML private Label lastPredictionUpdateLabel;

    @FXML private TableView<DoctorAnalyticsService.Patient> patientsTable;
    @FXML private TableColumn<DoctorAnalyticsService.Patient, String> patientNameColumn;
    @FXML private TableColumn<DoctorAnalyticsService.Patient, Integer> healthScoreColumn;
    @FXML private TableColumn<DoctorAnalyticsService.Patient, String> trendColumn;
    @FXML private TableColumn<DoctorAnalyticsService.Patient, String> alertsColumn;
    @FXML private TableColumn<DoctorAnalyticsService.Patient, String> lastEntryColumn;
    @FXML private TableColumn<DoctorAnalyticsService.Patient, Void> actionsColumn;

    @FXML private ComboBox<String> treatmentMetricCombo;
    @FXML private LineChart<String, Number> treatmentEffectivenessChart;

    @FXML private VBox profitPredictionsContainer;
    @FXML private ListView<String> planningSuggestionsList;
    @FXML private ListView<String> recommendationsList;
    @FXML private ListView<String> recentAlertsList;
    @FXML private Label recentAlertsCount;

    @FXML private ComboBox<String> reportPatientCombo;
    @FXML private ComboBox<String> reportTypeCombo;
    @FXML private ComboBox<String> reportPeriodCombo;

    @FXML private StackPane patientModal;
    @FXML private VBox patientModalContent;

    // ========== Data and Services ==========
    private DoctorAnalyticsService analyticsService;
    private ObservableList<DoctorAnalyticsService.Patient> allPatients;
    private ObservableList<DoctorAnalyticsService.Patient> filteredPatients;

    private int currentDoctorId = 1; // replace with actual login

    @FXML
    public void initialize() {
        analyticsService = new DoctorAnalyticsService(currentDoctorId);
        setupTableColumns();
        setupComboBoxes();
        setupListeners();
        setupCharts();
        loadAllData();
    }

    private void setupTableColumns() {
        patientNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        healthScoreColumn.setCellValueFactory(new PropertyValueFactory<>("healthScore"));
        healthScoreColumn.setCellFactory(col -> new TableCell<DoctorAnalyticsService.Patient, Integer>() {
            @Override
            protected void updateItem(Integer score, boolean empty) {
                super.updateItem(score, empty);
                if (empty || score == null) setText(null);
                else {
                    HBox hbox = new HBox(8);
                    hbox.setAlignment(Pos.CENTER_LEFT);
                    ProgressBar pb = new ProgressBar(score / 100.0);
                    pb.setPrefWidth(60);
                    Label label = new Label(score + "%");
                    label.setStyle("-fx-font-size: 11px;");
                    hbox.getChildren().addAll(pb, label);
                    setGraphic(hbox);
                }
            }
        });
        trendColumn.setCellValueFactory(new PropertyValueFactory<>("trendLabel"));
        alertsColumn.setCellValueFactory(cellData -> {
            DoctorAnalyticsService.Patient p = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(formatAlerts(p.getAlerts()));
        });
        lastEntryColumn.setCellValueFactory(new PropertyValueFactory<>("lastEntryDateFormatted"));
        actionsColumn.setCellFactory(createActionButtons());
    }

    private void setupComboBoxes() {
        alertFilterCombo.getItems().addAll("Tous les patients", "Alertes critiques", "Alertes modÃ©rÃ©es", "Stables");
        alertFilterCombo.setValue("Tous les patients");
        treatmentMetricCombo.getItems().addAll("RÃ©duction symptÃ´mes", "AdhÃ©rence", "Satisfaction");
        treatmentMetricCombo.setValue("RÃ©duction symptÃ´mes");
        reportTypeCombo.getItems().addAll("RÃ©sumÃ© clinique", "Rapport dÃ©taillÃ©", "Analyse des tendances", "Suivi mÃ©dicaments");
        reportPeriodCombo.getItems().addAll("7 derniers jours", "30 derniers jours", "3 derniers mois", "PersonnalisÃ©");
    }

    private String formatAlerts(List<DoctorAnalyticsService.Patient.Alert> alerts) {
        if (alerts == null || alerts.isEmpty()) return "-";
        StringBuilder sb = new StringBuilder();
        for (DoctorAnalyticsService.Patient.Alert a : alerts) {
            sb.append("critical".equals(a.getSeverity()) ? "ðŸ”´" : "ðŸŸ¡");
        }
        return sb.toString();
    }

    private Callback<TableColumn<DoctorAnalyticsService.Patient, Void>, TableCell<DoctorAnalyticsService.Patient, Void>> createActionButtons() {
        return param -> new TableCell<>() {
            private final Button viewBtn = new Button("ðŸ‘ï¸");
            private final Button reportBtn = new Button("ðŸ“„");
            private final Button messageBtn = new Button("âœ‰ï¸");
            private final HBox buttons = new HBox(6, viewBtn, reportBtn, messageBtn);

            {
                viewBtn.getStyleClass().add("action-button");
                reportBtn.getStyleClass().add("action-button");
                messageBtn.getStyleClass().add("action-button");
                viewBtn.setOnAction(e -> viewPatientDetails(getTableView().getItems().get(getIndex())));
                reportBtn.setOnAction(e -> generateQuickReport(getTableView().getItems().get(getIndex())));
                messageBtn.setOnAction(e -> sendMessageToPatient(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
            }
        };
    }

    private void setupListeners() {
        patientSearchField.textProperty().addListener((obs, old, val) -> filterPatients());
        alertFilterCombo.valueProperty().addListener((obs, old, val) -> filterPatients());
        treatmentMetricCombo.valueProperty().addListener((obs, old, val) -> updateTreatmentChart());
    }

    private void filterPatients() {
        String search = patientSearchField.getText().toLowerCase();
        String filter = alertFilterCombo.getValue();
        if (allPatients == null) return;

        ObservableList<DoctorAnalyticsService.Patient> filtered = FXCollections.observableArrayList();
        for (DoctorAnalyticsService.Patient p : allPatients) {
            boolean matchesSearch = p.getName().toLowerCase().contains(search);
            boolean matchesFilter = true;
            if ("Alertes critiques".equals(filter)) {
                matchesFilter = p.getAlerts().stream().anyMatch(a -> "critical".equals(a.getSeverity()));
            } else if ("Alertes modÃ©rÃ©es".equals(filter)) {
                matchesFilter = p.getAlerts().stream().anyMatch(a -> "warning".equals(a.getSeverity()));
            } else if ("Stables".equals(filter)) {
                matchesFilter = p.getAlerts().isEmpty();
            }
            if (matchesSearch && matchesFilter) filtered.add(p);
        }
        filteredPatients = filtered;
        patientsTable.setItems(filteredPatients);
    }

    private void loadAllData() {
        // Statistics
        Task<Map<String, Object>> statsTask = analyticsService.getStatsTask();
        statsTask.setOnSucceeded(event -> updateStats(statsTask.getValue()));
        new Thread(statsTask).start();

        // AI predictions
        Task<List<Map<String, Object>>> predictionsTask = analyticsService.getAIPredictionsTask();
        predictionsTask.setOnSucceeded(event -> displayPredictions(predictionsTask.getValue()));
        new Thread(predictionsTask).start();

        // Patients
        Task<List<DoctorAnalyticsService.Patient>> patientsTask = analyticsService.getPatientsTask();
        patientsTask.setOnSucceeded(event -> {
            allPatients = FXCollections.observableArrayList(patientsTask.getValue());
            filterPatients();
            populateReportPatients(allPatients);
        });
        new Thread(patientsTask).start();

        // Profit predictions
        Task<Map<String, Object>> profitTask = analyticsService.getProfitPredictionsTask();
        profitTask.setOnSucceeded(event -> displayProfitPredictions(profitTask.getValue()));
        new Thread(profitTask).start();

        // Planning suggestions
        Task<List<String>> planningTask = analyticsService.getPlanningSuggestionsTask();
        planningTask.setOnSucceeded(event -> planningSuggestionsList.setItems(FXCollections.observableArrayList(planningTask.getValue())));
        new Thread(planningTask).start();

        // AI recommendations
        Task<List<String>> recommendationsTask = analyticsService.getAIRecommendationsTask();
        recommendationsTask.setOnSucceeded(event -> recommendationsList.setItems(FXCollections.observableArrayList(recommendationsTask.getValue())));
        new Thread(recommendationsTask).start();

        // Recent alerts
        Task<List<DoctorAnalyticsService.Patient.Alert>> alertsTask = analyticsService.getRecentAlertsTask();
        alertsTask.setOnSucceeded(event -> updateAlertList(alertsTask.getValue()));
        new Thread(alertsTask).start();

        // Treatment effectiveness
        String initialMetric = treatmentMetricCombo.getValue();
        Task<Map<String, Double>> treatmentTask = analyticsService.getTreatmentEffectivenessTask(initialMetric);
        treatmentTask.setOnSucceeded(event -> updateTreatmentChartData(treatmentTask.getValue()));
        new Thread(treatmentTask).start();
    }

    private void updateStats(Map<String, Object> stats) {
        totalPatientsValue.setText(String.valueOf(stats.get("totalPatients")));
        criticalAlertsValue.setText(String.valueOf(stats.get("criticalAlerts")));
        todayAppointmentsValue.setText(String.valueOf(stats.get("todayAppointments")));
        reportsGeneratedValue.setText(String.valueOf(stats.get("reportsGenerated")));
    }

    private void displayPredictions(List<Map<String, Object>> predictions) {
        predictionsContainer.getChildren().clear();
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("dd/MM");
        for (Map<String, Object> p : predictions) {
            VBox card = new VBox(4);
            card.setAlignment(Pos.CENTER);
            card.setStyle("-fx-background-color: white; -fx-padding: 12; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 2, 0, 0, 1);");
            Label dayName = new Label(((String) p.get("day_name")).toUpperCase());
            dayName.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #6b7280;");
            Label date = new Label(dayFormatter.format(LocalDate.parse((String) p.get("day"))));
            date.setStyle("-fx-font-size: 10px; -fx-text-fill: #9ca3af;");
            Label value = new Label(String.valueOf(p.get("predicted_consultations")));
            value.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #00A790;");
            Label unit = new Label("consultations");
            unit.setStyle("-fx-font-size: 10px; -fx-text-fill: #6b7280;");
            card.getChildren().addAll(dayName, date, value, unit);
            predictionsContainer.getChildren().add(card);
            HBox.setHgrow(card, Priority.ALWAYS);
        }
        lastPredictionUpdateLabel.setText("DerniÃ¨re mise Ã  jour: " + LocalDate.now().format(DateTimeFormatter.ofPattern("HH:mm")));
    }

    private void displayProfitPredictions(Map<String, Object> profit) {
        profitPredictionsContainer.getChildren().clear();
        if (profit == null || profit.isEmpty()) {
            Label empty = new Label("DonnÃ©es insuffisantes");
            empty.setStyle("-fx-text-fill: #9ca3af;");
            profitPredictionsContainer.getChildren().add(empty);
            return;
        }
        HBox row1 = createProfitRow("30 derniers jours", formatCurrency((Double) profit.get("last_30")));
        HBox row2 = createProfitRow("Prochains 30 jours", formatCurrency((Double) profit.get("next_30")));
        HBox row3 = createProfitRow("Honoraires moyens", formatCurrency((Double) profit.get("avg_fee")));
        String trendVal = profit.get("trend") + "%";
        HBox row4 = createProfitRow("Tendance", trendVal, ((Double) profit.get("trend") >= 0 ? "text-emerald-600" : "text-rose-600"));
        profitPredictionsContainer.getChildren().addAll(row1, row2, row3, row4);

        // monthly forecast
        List<Map<String, Object>> monthly = (List<Map<String, Object>>) profit.get("monthly_forecast");
        if (monthly != null && !monthly.isEmpty()) {
            Label title = new Label("Prochains mois");
            title.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #6b7280; -fx-padding: 8 0 0 0;");
            profitPredictionsContainer.getChildren().add(title);
            for (Map<String, Object> m : monthly) {
                HBox row = createProfitRow((String) m.get("month"), formatCurrency((Double) m.get("revenue")));
                profitPredictionsContainer.getChildren().add(row);
            }
        }
    }

    private HBox createProfitRow(String label, String value) {
        return createProfitRow(label, value, "");
    }

    private HBox createProfitRow(String label, String value, String extraStyleClass) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label val = new Label(value);
        val.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1f2937;");
        if (!extraStyleClass.isEmpty()) val.getStyleClass().add(extraStyleClass);
        row.getChildren().addAll(lbl, spacer, val);
        return row;
    }

    private String formatCurrency(Double amount) {
        if (amount == null) return "0 DT";
        return String.format(Locale.FRANCE, "%,.0f DT", amount);
    }

    private void updateAlertList(List<DoctorAnalyticsService.Patient.Alert> alerts) {
        ObservableList<String> items = FXCollections.observableArrayList();
        for (DoctorAnalyticsService.Patient.Alert a : alerts) {
            items.add(a.getPatientName() + " - " + a.getMessage() + " (" + a.getTime() + ")");
        }
        recentAlertsList.setItems(items);
        recentAlertsCount.setText(String.valueOf(alerts.size()));
    }

    private void updateTreatmentChart() {
        String metric = treatmentMetricCombo.getValue();
        Task<Map<String, Double>> task = analyticsService.getTreatmentEffectivenessTask(metric);
        task.setOnSucceeded(event -> updateTreatmentChartData(task.getValue()));
        new Thread(task).start();
    }

    private void updateTreatmentChartData(Map<String, Double> data) {
        treatmentEffectivenessChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(treatmentMetricCombo.getValue());
        for (Map.Entry<String, Double> entry : data.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        treatmentEffectivenessChart.getData().add(series);
    }

    private void setupCharts() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis(0, 100, 10);
        yAxis.setLabel("Score (%)");
        treatmentEffectivenessChart.setAnimated(false);
    }

    private void populateReportPatients(ObservableList<DoctorAnalyticsService.Patient> patients) {
        ObservableList<String> names = FXCollections.observableArrayList();
        names.add("");
        for (DoctorAnalyticsService.Patient p : patients) {
            names.add(p.getName() + " (" + p.getId() + ")");
        }
        reportPatientCombo.setItems(names);
        reportPatientCombo.getSelectionModel().select(0);
    }

    // ========== Event Handlers ==========
    @FXML
    private void refreshAIPredictions() {
        Task<List<Map<String, Object>>> task = analyticsService.getAIPredictionsTask();
        task.setOnSucceeded(event -> displayPredictions(task.getValue()));
        new Thread(task).start();
    }

    @FXML
    private void refreshPatientData() {
        loadAllData();
    }

    @FXML
    private void generateQuickReport() {
        String selected = reportPatientCombo.getValue();
        if (selected == null || selected.isEmpty()) {
            showAlert("Erreur", "Veuillez sÃ©lectionner un patient.");
            return;
        }
        // Extract patient ID (format: "Name (ID)")
        String patientId = selected.replaceAll(".+\\((.+)\\)", "$1");
        String reportType = reportTypeCombo.getValue();
        String period = reportPeriodCombo.getValue();
        showAlert("SuccÃ¨s", "Rapport " + reportType + " gÃ©nÃ©rÃ© pour le patient " + selected + " (" + period + ").");
    }

    private void generateQuickReport(DoctorAnalyticsService.Patient patient) {
        showAlert("Rapport", "GÃ©nÃ©ration du rapport pour " + patient.getName());
    }

    private void viewPatientDetails(DoctorAnalyticsService.Patient patient) {
        patientModalContent.getChildren().clear();
        VBox details = new VBox(8);
        details.getChildren().addAll(
                new Label("Nom: " + patient.getName()),
                new Label("Ã‚ge: " + patient.getAge()),
                new Label("Score santÃ©: " + patient.getHealthScore()),
                new Label("DerniÃ¨re consultation: " + patient.getLastEntryDateFormatted())
        );
        patientModalContent.getChildren().add(details);
        patientModal.setVisible(true);
    }

    @FXML
    private void closePatientModal() {
        patientModal.setVisible(false);
    }

    private void sendMessageToPatient(DoctorAnalyticsService.Patient patient) {
        showAlert("Message", "Envoi d'un message Ã  " + patient.getName());
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
