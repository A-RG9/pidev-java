package org.example.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.util.Callback;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;

public class DoctorAnalytics {

    @FXML private Label aiStatusLabel;
    @FXML private TextField patientSearchField;
    @FXML private ComboBox<String> alertFilterCombo;

    @FXML private Label totalPatientsValue;
    @FXML private Label criticalAlertsValue;
    @FXML private Label todayAppointmentsValue;
    @FXML private Label reportsGeneratedValue;

    @FXML private HBox predictionsContainer;
    @FXML private Label lastPredictionUpdateLabel;

    @FXML private TableView<Patient> patientsTable;
    @FXML private TableColumn<Patient, String> patientNameColumn;
    @FXML private TableColumn<Patient, Integer> healthScoreColumn;
    @FXML private TableColumn<Patient, String> trendColumn;
    @FXML private TableColumn<Patient, String> alertsColumn;
    @FXML private TableColumn<Patient, String> lastEntryColumn;
    @FXML private TableColumn<Patient, Void> actionsColumn;

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

    private ObservableList<Patient> allPatients;

    @FXML
    public void initialize() {
        setupTableColumns();
        setupComboBoxes();
        setupListeners();
        setupChart();
        loadMockData();
    }

    private void setupTableColumns() {
        patientNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        healthScoreColumn.setCellValueFactory(new PropertyValueFactory<>("healthScore"));
        healthScoreColumn.setCellFactory(col -> new TableCell<Patient, Integer>() {
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
            Patient p = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(formatAlerts(p.getAlerts()));
        });
        lastEntryColumn.setCellValueFactory(new PropertyValueFactory<>("lastEntryDateFormatted"));
        actionsColumn.setCellFactory(createActionButtons());
    }

    private void setupComboBoxes() {
        alertFilterCombo.getItems().addAll("Tous les patients", "Alertes critiques", "Alertes modérées", "Stables");
        alertFilterCombo.setValue("Tous les patients");
        treatmentMetricCombo.getItems().addAll("Réduction symptômes", "Adhérence", "Satisfaction");
        treatmentMetricCombo.setValue("Réduction symptômes");
        reportTypeCombo.getItems().addAll("Résumé clinique", "Rapport détaillé", "Analyse des tendances", "Suivi médicaments");
        reportPeriodCombo.getItems().addAll("7 derniers jours", "30 derniers jours", "3 derniers mois", "Personnalisé");
    }

    private void setupListeners() {
        patientSearchField.textProperty().addListener((obs, old, val) -> filterPatients());
        alertFilterCombo.valueProperty().addListener((obs, old, val) -> filterPatients());
        treatmentMetricCombo.valueProperty().addListener((obs, old, val) -> updateTreatmentChart());
    }

    private String formatAlerts(List<PatientAlert> alerts) {
        if (alerts == null || alerts.isEmpty()) return "-";
        StringBuilder sb = new StringBuilder();
        for (PatientAlert a : alerts) {
            sb.append("critical".equals(a.getSeverity()) ? "🔴" : "🟡");
        }
        return sb.toString();
    }

    private Callback<TableColumn<Patient, Void>, TableCell<Patient, Void>> createActionButtons() {
        return param -> new TableCell<>() {
            private final Button viewBtn = new Button("👁️");
            private final Button reportBtn = new Button("📄");
            private final Button messageBtn = new Button("✉️");
            private final HBox buttons = new HBox(6, viewBtn, reportBtn, messageBtn);

            {
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

    private void filterPatients() {
        String search = patientSearchField.getText().toLowerCase();
        String filter = alertFilterCombo.getValue();
        if (allPatients == null) return;

        ObservableList<Patient> filtered = FXCollections.observableArrayList();
        for (Patient p : allPatients) {
            boolean matchesSearch = p.getName().toLowerCase().contains(search);
            boolean matchesFilter = true;
            if ("Alertes critiques".equals(filter))
                matchesFilter = p.getAlerts().stream().anyMatch(a -> "critical".equals(a.getSeverity()));
            else if ("Alertes modérées".equals(filter))
                matchesFilter = p.getAlerts().stream().anyMatch(a -> "warning".equals(a.getSeverity()));
            else if ("Stables".equals(filter))
                matchesFilter = p.getAlerts().isEmpty();

            if (matchesSearch && matchesFilter) filtered.add(p);
        }
        patientsTable.setItems(filtered);
    }

    private void loadMockData() {
        totalPatientsValue.setText("1");
        criticalAlertsValue.setText("0");
        todayAppointmentsValue.setText("0");
        reportsGeneratedValue.setText("0");

        displayPredictions(generateMockPredictions());

        allPatients = FXCollections.observableArrayList(generateSinglePatient());
        filterPatients();
        populateReportPatients(allPatients);

        displayProfitPredictions(generateMockProfit());

        planningSuggestionsList.setItems(FXCollections.observableArrayList(
                "Réduire les creux\nVous avez des créneaux vides importants. Regroupez les consultations ou ouvrez des créneaux ciblés.",
                "Répartir la charge\nCertaines journées sont surchargées. Répartissez les rendez-vous sur la semaine.",
                "Ajouter des marges\nLes consultations sont très rapprochées. Ajoutez des marges pour éviter les retards."
        ));

        recommendationsList.setItems(FXCollections.observableArrayList(
                "Activez les rappels automatiques pour réduire les absences",
                "Augmentez le nombre de suivis pour améliorer l'observance",
                "Utilisez l'IA pour prioriser les patients à risque",
                "Proposez des forfaits de suivi pour fidéliser"
        ));

        recentAlertsList.setItems(FXCollections.observableArrayList());
        recentAlertsCount.setText("0");

        updateTreatmentChartData(generateMockChartData());
    }

    private List<Patient> generateSinglePatient() {
        List<Patient> patients = new ArrayList<>();
        Patient p = new Patient();
        p.setId("68a7fb3-c388-41f3-bb88-2a31c8fa85cf");
        p.setName("CHAHD MAALOUL");
        p.setHealthScore(75);
        p.setTrendLabel("Stable");
        p.setLastEntryDateFormatted("Il y a 1 min");
        p.setAge(30);
        p.setAlerts(new ArrayList<>());
        patients.add(p);
        return patients;
    }

    private List<Map<String, Object>> generateMockPredictions() {
        List<Map<String, Object>> predictions = new ArrayList<>();
        String[] days = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"};
        int[] values = {14, 16, 13, 18, 15, 10, 12};
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 7; i++) {
            Map<String, Object> p = new HashMap<>();
            p.put("day_name", days[i]);
            p.put("day", today.plusDays(i));
            p.put("predicted_consultations", values[i]);
            predictions.add(p);
        }
        return predictions;
    }

    private Map<String, Object> generateMockProfit() {
        Map<String, Object> profit = new HashMap<>();
        profit.put("last_30", 4200.0);
        profit.put("next_30", 4400.0);
        profit.put("avg_fee", 70.0);
        profit.put("trend", 8.0);
        profit.put("specialty_avg", 4100.0);
        profit.put("vs_specialty", 2.5);
        List<Map<String, Object>> monthly = new ArrayList<>();
        monthly.add(Map.of("month", "2026-05", "revenue", 4600.0));
        monthly.add(Map.of("month", "2026-06", "revenue", 4750.0));
        monthly.add(Map.of("month", "2026-07", "revenue", 4900.0));
        profit.put("monthly_forecast", monthly);
        profit.put("alert_message", "BAISSE DE REVENUS PRÉVUE\nLes revenus prévus sont inférieurs à 80% des 30 derniers jours.");
        return profit;
    }

    private Map<String, Double> generateMockChartData() {
        Map<String, Double> data = new LinkedHashMap<>();
        data.put("Jan", 78.0);
        data.put("Fév", 82.0);
        data.put("Mar", 79.0);
        data.put("Avr", 85.0);
        data.put("Mai", 88.0);
        return data;
    }

    private void displayPredictions(List<Map<String, Object>> predictions) {
        predictionsContainer.getChildren().clear();
        for (Map<String, Object> p : predictions) {
            VBox card = new VBox(4);
            card.setAlignment(Pos.CENTER);
            card.setStyle("-fx-background-color: white; -fx-padding: 12; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 2, 0, 0, 1);");

            Label dayName = new Label(((String) p.get("day_name")).toUpperCase());
            dayName.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #6b7280;");

            // Manual date formatting – NO DateTimeFormatter used
            Object dateObj = p.get("day");
            String dateStr = "";
            if (dateObj instanceof LocalDate) {
                LocalDate date = (LocalDate) dateObj;
                String day = String.format("%02d", date.getDayOfMonth());
                String month = date.getMonth().getDisplayName(TextStyle.SHORT, Locale.FRENCH);
                dateStr = day + " " + month;
            } else if (dateObj != null) {
                dateStr = dateObj.toString();
            }
            Label dateLabel = new Label(dateStr);
            dateLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #9ca3af;");

            Label value = new Label(String.valueOf(p.get("predicted_consultations")));
            value.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #00A790;");
            Label unit = new Label("consultations");
            unit.setStyle("-fx-font-size: 10px; -fx-text-fill: #6b7280;");

            card.getChildren().addAll(dayName, dateLabel, value, unit);
            predictionsContainer.getChildren().add(card);
            HBox.setHgrow(card, Priority.ALWAYS);
        }

    }

    private void displayProfitPredictions(Map<String, Object> profit) {
        profitPredictionsContainer.getChildren().clear();
        addProfitRow("30 derniers jours", String.format("%.0f DT", profit.get("last_30")));
        addProfitRow("Prochains 30 jours", String.format("%.0f DT", profit.get("next_30")));
        addProfitRow("Honoraires moyens", String.format("%.0f DT", profit.get("avg_fee")));
        String trendVal = profit.get("trend") + "%";
        addProfitRow("Tendance", trendVal, ((Double) profit.get("trend") >= 0 ? "text-emerald-600" : "text-rose-600"));
        Label title = new Label("PROCHAINS MOIS :");
        title.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #6b7280; -fx-padding: 8 0 0 0;");
        profitPredictionsContainer.getChildren().add(title);
        List<Map<String, Object>> monthly = (List<Map<String, Object>>) profit.get("monthly_forecast");
        for (Map<String, Object> m : monthly) {
            addProfitRow((String) m.get("month"), String.format("%.0f DT", m.get("revenue")));
        }
        String alertMsg = (String) profit.get("alert_message");
        if (alertMsg != null) {
            VBox alertBox = new VBox(4);
            alertBox.setStyle("-fx-background-color: #fee2e2; -fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #e11d48; -fx-border-width: 1; -fx-border-radius: 8;");
            Label alertTitle = new Label(alertMsg.split("\n")[0]);
            alertTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #e11d48;");
            Label alertDesc = new Label(alertMsg.split("\n")[1]);
            alertDesc.setStyle("-fx-font-size: 12px; -fx-text-fill: #b91c1c; -fx-wrap-text: true;");
            alertBox.getChildren().addAll(alertTitle, alertDesc);
            profitPredictionsContainer.getChildren().add(alertBox);
        }
    }

    private void addProfitRow(String label, String value) { addProfitRow(label, value, ""); }
    private void addProfitRow(String label, String value, String styleClass) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label val = new Label(value);
        val.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1f2937;");
        if (!styleClass.isEmpty()) val.getStyleClass().add(styleClass);
        row.getChildren().addAll(lbl, spacer, val);
        profitPredictionsContainer.getChildren().add(row);
    }

    private void updateTreatmentChart() {
        updateTreatmentChartData(generateMockChartData());
    }

    private void updateTreatmentChartData(Map<String, Double> data) {
        treatmentEffectivenessChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(treatmentMetricCombo.getValue() + " (%)");
        for (Map.Entry<String, Double> entry : data.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        treatmentEffectivenessChart.getData().add(series);
    }

    private void setupChart() {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Mois");
        NumberAxis yAxis = new NumberAxis(0, 100, 10);
        yAxis.setLabel("Taux (%)");
        treatmentEffectivenessChart.setAnimated(false);
    }

    private void populateReportPatients(ObservableList<Patient> patients) {
        ObservableList<String> names = FXCollections.observableArrayList();
        names.add("");
        for (Patient p : patients) {
            names.add(p.getName() + " (" + p.getId() + ")");
        }
        reportPatientCombo.setItems(names);
        reportPatientCombo.getSelectionModel().select(0);
    }

    @FXML private void refreshAIPredictions() { displayPredictions(generateMockPredictions()); }
    @FXML private void refreshPatientData() { loadMockData(); }
    @FXML private void generateQuickReport() { showAlert("Rapport", "Fonctionnalité à implémenter"); }
    private void generateQuickReport(Patient patient) { showAlert("Rapport", "Génération du rapport pour " + patient.getName()); }
    private void viewPatientDetails(Patient patient) { showAlert("Détails", "Patient: " + patient.getName() + "\nÂge: " + patient.getAge() + "\nScore santé: " + patient.getHealthScore()); }
    @FXML private void closePatientModal() { patientModal.setVisible(false); }
    private void sendMessageToPatient(Patient patient) { showAlert("Message", "Envoi d'un message à " + patient.getName()); }
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static class Patient {
        private String id, name, trendLabel, lastEntryDateFormatted;
        private int healthScore, age;
        private List<PatientAlert> alerts;
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getHealthScore() { return healthScore; }
        public void setHealthScore(int healthScore) { this.healthScore = healthScore; }
        public String getTrendLabel() { return trendLabel; }
        public void setTrendLabel(String trendLabel) { this.trendLabel = trendLabel; }
        public String getLastEntryDateFormatted() { return lastEntryDateFormatted; }
        public void setLastEntryDateFormatted(String lastEntryDateFormatted) { this.lastEntryDateFormatted = lastEntryDateFormatted; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
        public List<PatientAlert> getAlerts() { return alerts; }
        public void setAlerts(List<PatientAlert> alerts) { this.alerts = alerts; }
    }

    public static class PatientAlert {
        private String patientName, message, severity, time;
        public String getPatientName() { return patientName; }
        public void setPatientName(String patientName) { this.patientName = patientName; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
        public String getTime() { return time; }
        public void setTime(String time) { this.time = time; }
    }
}