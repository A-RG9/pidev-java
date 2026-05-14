package com.wellora.javafx.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.util.Callback;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.geometry.Insets;
import javafx.collections.transformation.FilteredList;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import javafx.concurrent.Task;

import com.wellcare.javafx.model.User;
import com.wellcare.javafx.service.UserService;
import com.wellcare.javafx.util.SceneManager;
import com.wellora.javafx.model.Consultation;
import com.wellora.javafx.service.ConsulationServices;

public class DoctorAnalytics implements DashboardInjectedController {

    @FXML private Label aiStatusLabel;
    @FXML private TextField patientSearchField;
    @FXML private ComboBox<String> alertFilterCombo;

    @FXML private Label totalPatientsValue;
    @FXML private Label criticalAlertsValue;
    @FXML private Label todayAppointmentsValue;
    @FXML private Label reportsGeneratedValue;
    @FXML private Label showingConsultationsLabel;

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
    @FXML private VBox planningSuggestionsContainer;
    @FXML private VBox recommendationsContainer;
    @FXML private ListView<String> recentAlertsList;
    @FXML private Label recentAlertsCount;

    @FXML private ComboBox<String> reportPatientCombo;
    @FXML private ComboBox<String> reportTypeCombo;
    @FXML private ComboBox<String> reportPeriodCombo;

    @FXML private StackPane patientModal;
    @FXML private VBox patientModalContent;

    private com.wellcare.javafx.controller.dashboard.DoctorDashboardController dashboardController;

    @Override
    public void setDashboardController(com.wellcare.javafx.controller.dashboard.DoctorDashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }

    private ObservableList<Patient> allPatients;

    @FXML
    public void initialize() {
        setupTableColumns();
        setupComboBoxes();
        setupListeners();
        setupChart();
        setupListCellFactories();
        loadMockData();
    }

    private void setupListCellFactories() {
        // no-op: using VBox with Labels now
    }

    private void setupTableColumns() {
        // 1. CONSULTATION / PATIENT
        patientNameColumn.setText("CONSULTATION / PATIENT");
        patientNameColumn.setCellFactory(col -> new TableCell<Patient, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableView().getItems().get(getIndex()) == null) {
                    setGraphic(null);
                } else {
                    Patient p = getTableView().getItems().get(getIndex());
                    HBox container = new HBox(12);
                    container.setAlignment(Pos.CENTER_LEFT);
                    container.setPadding(new Insets(8, 0, 8, 0));

                    // Initials Circle
                    String initials = "";
                    if (p.getName() != null && !p.getName().isEmpty()) {
                        String[] parts = p.getName().split(" ");
                        if (parts.length > 0) initials += parts[0].substring(0, 1);
                        if (parts.length > 1) initials += parts[1].substring(0, 1);
                    }
                    Label initialsCircle = new Label(initials.toUpperCase());
                    initialsCircle.setMinWidth(36);
                    initialsCircle.setMinHeight(36);
                    initialsCircle.setStyle("-fx-background-color: #0d9488; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 18; -fx-alignment: center; -fx-font-size: 13px;");

                    VBox info = new VBox(2);
                    Label nameLabel = new Label(p.getName().toUpperCase());
                    nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #111827; -fx-font-size: 13px;");
                    Label detailsLabel = new Label(p.getAge() + " yrs • " + p.getGender());
                    detailsLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 11px;");
                    Label consLabel = new Label(p.getConsId());
                    consLabel.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 10px;");

                    info.getChildren().addAll(nameLabel, detailsLabel, consLabel);
                    container.getChildren().addAll(initialsCircle, info);
                    setGraphic(container);
                }
            }
        });

        // 2. STATUS
        trendColumn.setText("STATUS");
        trendColumn.setCellValueFactory(new PropertyValueFactory<>("trendLabel"));
        trendColumn.setCellFactory(col -> new TableCell<Patient, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setGraphic(null);
                else {
                    HBox badge = new HBox(6);
                    badge.setAlignment(Pos.CENTER_LEFT);
                    badge.setPadding(new Insets(4, 10, 4, 10));
                    badge.setMaxWidth(Region.USE_PREF_SIZE);
                    badge.setStyle("-fx-background-color: #eff6ff; -fx-background-radius: 12;");
                    
                    Circle dot = new Circle(3, Color.web("#3b82f6"));
                    Label label = new Label(item);
                    label.setStyle("-fx-text-fill: #3b82f6; -fx-font-size: 11px; -fx-font-weight: bold;");
                    
                    badge.getChildren().addAll(dot, label);
                    setGraphic(badge);
                }
            }
        });

        // 3. HEALTH SCORE
        healthScoreColumn.setText("HEALTH SCORE");
        healthScoreColumn.setCellValueFactory(new PropertyValueFactory<>("healthScore"));
        healthScoreColumn.setCellFactory(col -> new TableCell<Patient, Integer>() {
            @Override
            protected void updateItem(Integer score, boolean empty) {
                super.updateItem(score, empty);
                if (empty || score == null) setGraphic(null);
                else {
                    HBox container = new HBox(8);
                    container.setAlignment(Pos.CENTER_LEFT);
                    
                    ProgressBar pb = new ProgressBar(score / 100.0);
                    pb.setPrefWidth(120);
                    pb.setPrefHeight(6);
                    pb.setStyle("-fx-accent: #10b981; -fx-control-inner-background: #f3f4f6; -fx-background-color: transparent; -fx-padding: 0;");
                    
                    Label label = new Label(score.toString());
                    label.setStyle("-fx-font-size: 12px; -fx-text-fill: #374151; -fx-font-weight: bold;");
                    
                    container.getChildren().addAll(pb, label);
                    setGraphic(container);
                }
            }
        });

        // 4. DIAGNOSES
        alertsColumn.setText("DIAGNOSES");
        alertsColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(""));

        // 5. CONSULTATION DATE
        lastEntryColumn.setText("CONSULTATION DATE");
        lastEntryColumn.setCellFactory(col -> new TableCell<Patient, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableView().getItems().get(getIndex()) == null) {
                    setGraphic(null);
                } else {
                    Patient p = getTableView().getItems().get(getIndex());
                    VBox vbox = new VBox(2);
                    vbox.setAlignment(Pos.CENTER_LEFT);
                    Label dateLabel = new Label(p.getLastEntryDateFormatted());
                    dateLabel.setStyle("-fx-text-fill: #374151; -fx-font-size: 12px; -fx-font-weight: bold;");
                    Label timeLabel = new Label(p.getConsultationTime());
                    timeLabel.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 11px;");
                    vbox.getChildren().addAll(dateLabel, timeLabel);
                    setGraphic(vbox);
                }
            }
        });

        // 6. ACTIONS
        actionsColumn.setText("ACTIONS");
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
            private final Button noteBtn = new Button("📑");
            private final Button reportBtn = new Button("📋");
            private final Button messageBtn = new Button("💬");
            private final Button moreBtn = new Button("⋮");
            private final HBox buttons = new HBox(12, noteBtn, reportBtn, messageBtn, moreBtn);

            {
                buttons.setAlignment(Pos.CENTER_LEFT);
                String baseStyle = "-fx-background-color: transparent; -fx-text-fill: #6b7280; -fx-font-size: 16px; -fx-cursor: hand; -fx-padding: 4;";
                
                noteBtn.setStyle(baseStyle);
                reportBtn.setStyle(baseStyle);
                messageBtn.setStyle(baseStyle);
                moreBtn.setStyle(baseStyle);

                // Hover effects
                noteBtn.setOnMouseEntered(e -> noteBtn.setStyle(baseStyle + "-fx-text-fill: #111827;"));
                noteBtn.setOnMouseExited(e -> noteBtn.setStyle(baseStyle));
                reportBtn.setOnMouseEntered(e -> reportBtn.setStyle(baseStyle + "-fx-text-fill: #111827;"));
                reportBtn.setOnMouseExited(e -> reportBtn.setStyle(baseStyle));
                messageBtn.setOnMouseEntered(e -> messageBtn.setStyle(baseStyle + "-fx-text-fill: #111827;"));
                messageBtn.setOnMouseExited(e -> messageBtn.setStyle(baseStyle));
                moreBtn.setOnMouseEntered(e -> moreBtn.setStyle(baseStyle + "-fx-text-fill: #111827;"));
                moreBtn.setOnMouseExited(e -> moreBtn.setStyle(baseStyle));

                noteBtn.setOnAction(e -> {
                    Patient p = getTableView().getItems().get(getIndex());
                    if (p != null) openClinicalNotes(p);
                });
                
                reportBtn.setOnAction(e -> {
                    Patient p = getTableView().getItems().get(getIndex());
                    if (p != null) generateQuickReportForPatient(p);
                });

                messageBtn.setOnAction(e -> {
                    Patient p = getTableView().getItems().get(getIndex());
                    if (p != null) sendMessageToPatient(p);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else setGraphic(buttons);
            }
        };
    }

    private void generateQuickReportForPatient(Patient patient) {
        if (patient == null) return;
        if (reportPatientCombo != null) {
            reportPatientCombo.setValue(patient.getName());
            generateQuickReport();
        }
    }
    
    private void openClinicalNotes(Patient patient) {
        try {
            // Retrieve the actual User object from the DB using patient ID
            UserService userService = new UserService();
            User realUser = userService.getUserByUuid(patient.getId());
            ClinicalNotesController.selectedPatient = realUser;
            
            if (dashboardController != null) {
                dashboardController.handleClinicalNotes();
            } else {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/clinical-notes.fxml"));
                Parent root = loader.load();
                Scene scene = patientsTable.getScene();
                if (scene != null) {
                    scene.setRoot(root);
                }
            }
        } catch (Exception ex) {
            showAlert("Erreur", "Impossible d'ouvrir les notes cliniques: " + ex.getMessage());
            ex.printStackTrace();
        }
    }



    private void loadMockData() {
        // Load Real Patients
        List<Patient> realPatients = new ArrayList<>();
        int consultationsCount = 0;
        try {
            User currentDoctor = SceneManager.getInstance().getCurrentUser();
            if (currentDoctor != null) {
                ConsulationServices consultationService = new ConsulationServices();
                UserService userService = new UserService();
                
                List<Consultation> allCons = consultationService.ShowConsultation();
                Set<String> patientIds = new HashSet<>();
                
                System.out.println("🔍 DoctorAnalytics: Found " + allCons.size() + " total consultations in DB");
                
                for (Consultation c : allCons) {
                    boolean matchesDoctor = false;
                    
                    if (c.getMedecinId() != null && c.getMedecinId().equals(currentDoctor.getUuid())) {
                        matchesDoctor = true;
                    } else if (currentDoctor.getLastName() != null) {
                        if (c.getReasonForVisit() != null && c.getReasonForVisit().contains(currentDoctor.getLastName())) {
                            matchesDoctor = true;
                        } else if (c.getNotes() != null && c.getNotes().contains(currentDoctor.getLastName())) {
                            matchesDoctor = true;
                        }
                    }
                    
                    if (matchesDoctor && c.getStatus() != null && c.getStatus().equalsIgnoreCase("confirmed")) {
                        String pid = c.getPatientId();
                        if (pid != null && !pid.isEmpty()) {
                            User u = userService.getUserByUuid(pid);
                            if (u != null) {
                                Patient p = new Patient();
                                p.setId(u.getUuid());
                                p.setName(u.getFirstName() + " " + u.getLastName());
                                p.setConsId("#CONS-" + String.format("%04d", c.getId()));
                                p.setConsultationTime(c.getTimeConsultation() != null ? c.getTimeConsultation().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")) : "00:00");
                                p.setGender("Male"); // Default since not in model
                                
                                int age = 30;
                                if (u.getBirthdate() != null) {
                                    age = java.time.Period.between(u.getBirthdate(), LocalDate.now()).getYears();
                                }
                                p.setAge(age);
                                
                                p.setHealthScore(85); // Matches screenshot
                                p.setTrendLabel("Stable");
                                p.setLastEntryDateFormatted(c.getDateConsultation() != null ? c.getDateConsultation().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) : LocalDate.now().toString());
                                p.setAlerts(new ArrayList<>());
                                realPatients.add(p);
                                
                                if (c.getDateConsultation() != null && c.getDateConsultation().equals(LocalDate.now())) {
                                    consultationsCount++;
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading real data: " + e.getMessage());
        }

        totalPatientsValue.setText(String.valueOf(realPatients.size()));
        criticalAlertsValue.setText("0");
        todayAppointmentsValue.setText(String.valueOf(consultationsCount));
        reportsGeneratedValue.setText("0");
        if (showingConsultationsLabel != null) {
            showingConsultationsLabel.setText("Showing " + realPatients.size() + " of " + realPatients.size() + " consultations");
        }

        displayPredictions(generateMockPredictions());

        allPatients = FXCollections.observableArrayList(realPatients);
        filterPatients();
        populateReportPatients(allPatients);

        displayProfitPredictions(generateRealProfit());

        // Show loading placeholder in VBoxes
        Label loadingLabel = new Label("⏳ Génération des suggestions IA en cours...");
        loadingLabel.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 13px; -fx-font-style: italic;");
        planningSuggestionsContainer.getChildren().setAll(loadingLabel);

        Label loadingLabel2 = new Label("⏳ Analyse des données...");
        loadingLabel2.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 13px; -fx-font-style: italic;");
        recommendationsContainer.getChildren().setAll(loadingLabel2);

        generateAISuggestions();

        recentAlertsList.setItems(FXCollections.observableArrayList());
        recentAlertsCount.setText("0");

        updateTreatmentChartData(generateMockChartData());
    }

    private void filterPatients() {
        if (allPatients == null) return;
        String search = (patientSearchField != null) ? patientSearchField.getText().toLowerCase() : "";
        String filter = (alertFilterCombo != null) ? alertFilterCombo.getValue() : "Tous les patients";

        FilteredList<Patient> filtered = new FilteredList<>(allPatients, p -> {
            boolean matchesSearch = search.isEmpty() || 
                                   (p.getName() != null && p.getName().toLowerCase().contains(search)) || 
                                   (p.getConsId() != null && p.getConsId().toLowerCase().contains(search));
            boolean matchesFilter = filter == null || filter.equals("Tous les patients") || 
                                   (filter.equals("Stables") && "Stable".equals(p.getTrendLabel()));
            return matchesSearch && matchesFilter;
        });
        
        patientsTable.setItems(filtered);
        if (showingConsultationsLabel != null) {
            showingConsultationsLabel.setText("Showing " + filtered.size() + " of " + allPatients.size() + " consultations");
        }
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

    private void generateAISuggestions() {
        Task<String[]> task = new Task<>() {
            @Override
            protected String[] call() throws Exception {
                String prompt = "Tu es une IA conseillère médicale. Fournis exactement 2 suggestions courtes (moins de 10 mots chacune) d'amélioration de planning pour un médecin, séparées par un tiret (-). Par exemple: Regroupez vos rendez-vous - Ajoutez des pauses.";
                String apiKey = "h4S2z91IjWqucgaphMxdMeFXdEXBYpgb";
                String json = "{\n" +
                        "    \"model\": \"mistral-small-latest\",\n" +
                        "    \"messages\": [\n" +
                        "        {\"role\": \"user\", \"content\": \"" + prompt + "\"}\n" +
                        "    ],\n" +
                        "    \"temperature\": 0.6,\n" +
                        "    \"max_tokens\": 200\n" +
                        "}";
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://api.mistral.ai/v1/chat/completions"))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + apiKey)
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                System.out.println("[AI Suggestions] Status: " + response.statusCode());
                System.out.println("[AI Suggestions] Body: " + response.body().substring(0, Math.min(300, response.body().length())));
                if (response.statusCode() == 200) {
                    String body = response.body();
                    String target = "\"content\":\"";
                    int start = body.indexOf(target);
                    if (start == -1) return new String[]{"Erreur d'analyse API"};
                    start += target.length();
                    int end = start;
                    while (end < body.length()) {
                        if (body.charAt(end) == '"' && body.charAt(end - 1) != '\\') break;
                        end++;
                    }
                    String content = body.substring(start, end).replace("\\n", "\n").replace("\\\"", "\"").trim();
                    System.out.println("[AI Suggestions] Content: " + content);
                    List<String> res = new ArrayList<>();
                    // Try splitting by newline first, then by " - "
                    String[] lines = content.split("\n");
                    if (lines.length > 1) {
                        for (String l : lines) {
                            String clean = l.replaceAll("^[-*0-9.]+\\s*", "").trim();
                            if (!clean.isEmpty()) res.add(clean);
                        }
                    } else {
                        // Single line - split by " - " or ". "
                        String[] parts = content.split(" - | \\. ");
                        for (String p : parts) {
                            String clean = p.replaceAll("^[-*0-9.]+\\s*", "").trim();
                            if (!clean.isEmpty()) res.add(clean);
                        }
                    }
                    if (res.isEmpty()) res.add(content);
                    return res.toArray(new String[0]);
                }
                return new String[]{"Erreur API " + response.statusCode()};
            }
        };
        task.setOnSucceeded(e -> {
            String[] res = task.getValue();
            planningSuggestionsContainer.getChildren().clear();
            for (String s : res) {
                if (!s.trim().isEmpty()) {
                    Label lbl = new Label("✅ " + s.trim());
                    lbl.setStyle("-fx-text-fill: #374151; -fx-font-size: 13px; -fx-wrap-text: true;");
                    lbl.setMaxWidth(Double.MAX_VALUE);
                    planningSuggestionsContainer.getChildren().add(lbl);
                }
            }
            recommendationsContainer.getChildren().clear();
            for (String rec : new String[]{"Utilisez l'IA pour prioriser les patients à risque", "Activez les rappels automatiques pour réduire les absences"}) {
                Label lbl = new Label("💡 " + rec);
                lbl.setStyle("-fx-text-fill: #374151; -fx-font-size: 13px; -fx-wrap-text: true;");
                lbl.setMaxWidth(Double.MAX_VALUE);
                recommendationsContainer.getChildren().add(lbl);
            }
        });
        task.setOnFailed(e -> {
            System.err.println("[AI Suggestions] Failed: " + task.getException().getMessage());
            Label fallback = new Label("⚠️ Suggestions indisponibles");
            fallback.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 13px;");
            planningSuggestionsContainer.getChildren().setAll(fallback);
        });
        new Thread(task).start();
    }

    private Map<String, Object> generateRealProfit() {
        Map<String, Object> profit = new HashMap<>();
        double last30 = 0.0;
        int countLast30 = 0;
        try {
            ConsulationServices cs = new ConsulationServices();
            List<Consultation> all = cs.ShowConsultation();
            LocalDate today = LocalDate.now();
            LocalDate thirtyDaysAgo = today.minusDays(30);
            for(Consultation c : all) {
                if (c.getDateConsultation() != null && !c.getDateConsultation().isBefore(thirtyDaysAgo) && !c.getDateConsultation().isAfter(today)) {
                    if (c.getFee() != null) {
                        last30 += c.getFee();
                    } else {
                        last30 += 50.0;
                    }
                    countLast30++;
                }
            }
        } catch(Exception e) { e.printStackTrace(); }
        
        double avgFee = countLast30 > 0 ? last30 / countLast30 : 50.0;
        double next30 = last30 * 1.05;
        double trend = countLast30 > 0 ? 5.0 : 0.0;
        
        profit.put("last_30", last30);
        profit.put("next_30", next30);
        profit.put("avg_fee", avgFee);
        profit.put("trend", trend);
        profit.put("specialty_avg", last30 > 0 ? last30 * 0.9 : 4000.0);
        profit.put("vs_specialty", 2.5);
        List<Map<String, Object>> monthly = new ArrayList<>();
        LocalDate nextMonth = LocalDate.now().plusMonths(1);
        monthly.add(Map.of("month", nextMonth.toString().substring(0,7), "revenue", next30));
        monthly.add(Map.of("month", nextMonth.plusMonths(1).toString().substring(0,7), "revenue", next30 * 1.02));
        monthly.add(Map.of("month", nextMonth.plusMonths(2).toString().substring(0,7), "revenue", next30 * 1.05));
        profit.put("monthly_forecast", monthly);
        profit.put("alert_message", trend <= 0 ? "STAGNATION DE REVENUS\nVos revenus sont stables ou en légère baisse." : "CROISSANCE STABLE\nVos revenus sont en hausse.");
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

    @FXML private void refreshAIPredictions() {
        Task<List<Map<String, Object>>> task = new Task<>() {
            @Override
            protected List<Map<String, Object>> call() throws Exception {
                ConsulationServices cs = new ConsulationServices();
                List<Consultation> all = cs.ShowConsultation();
                int last7Days = 0;
                LocalDate aWeekAgo = LocalDate.now().minusDays(7);
                for(Consultation c : all) {
                    if (c.getDateConsultation() != null && c.getDateConsultation().isAfter(aWeekAgo)) {
                        last7Days++;
                    }
                }
                
                String prompt = "Tu es une IA de prévision médicale. Le médecin a eu " + last7Days + " consultations ces 7 derniers jours. " +
                                "Génère de façon réaliste le nombre de consultations prévues pour les 7 prochains jours. " +
                                "Renvoie UNIQUEMENT une liste de 7 nombres entiers séparés par des virgules. Aucun texte supplémentaire.";
                                
                String apiKey = "h4S2z91IjWqucgaphMxdMeFXdEXBYpgb";
                String json = "{\n" +
                        "    \"model\": \"mistral-small-latest\",\n" +
                        "    \"messages\": [\n" +
                        "        {\"role\": \"system\", \"content\": \"You are a helpful assistant.\"},\n" +
                        "        {\"role\": \"user\", \"content\": \"" + prompt + "\"}\n" +
                        "    ],\n" +
                        "    \"temperature\": 0.6,\n" +
                        "    \"max_tokens\": 20\n" +
                        "}";

                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://api.mistral.ai/v1/chat/completions"))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + apiKey)
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() == 200) {
                    String body = response.body();
                    String target = "\"content\":\"";
                    int start = body.indexOf(target);
                    if (start == -1) throw new Exception("Erreur d'analyse API");
                    start += target.length();
                    int end = start;
                    while (end < body.length()) {
                        char c = body.charAt(end);
                        if (c == '"' && (end == 0 || body.charAt(end - 1) != '\\')) break;
                        end++;
                    }
                    String content = body.substring(start, end).replace("\\n", "").trim();
                    String[] parts = content.split(",");
                    List<Map<String, Object>> predictions = new ArrayList<>();
                    LocalDate today = LocalDate.now();
                    
                    int[] values = {10, 10, 10, 10, 10, 10, 10};
                    for(int i = 0; i < 7 && i < parts.length; i++) {
                        try {
                            values[i] = Integer.parseInt(parts[i].trim());
                        } catch(Exception ignored) {}
                    }
                    
                    for (int i = 0; i < 7; i++) {
                        Map<String, Object> p = new HashMap<>();
                        p.put("day_name", today.plusDays(i).getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.FRENCH));
                        p.put("day", today.plusDays(i));
                        p.put("predicted_consultations", values[i]);
                        predictions.add(p);
                    }
                    return predictions;
                } else {
                    throw new RuntimeException("API error " + response.statusCode());
                }
            }
        };

        task.setOnSucceeded(e -> {
            displayPredictions(task.getValue());
            if (lastPredictionUpdateLabel != null) {
                lastPredictionUpdateLabel.setText("Dernière mise à jour: à l'instant");
            }
        });
        
        task.setOnFailed(e -> {
            System.err.println("AI Prediction failed: " + task.getException().getMessage());
            displayPredictions(generateMockPredictions());
        });

        new Thread(task).start();
    }
    @FXML private void refreshPatientData() { loadMockData(); }
    @FXML private void generateQuickReport() {
        String selectedPatientName = reportPatientCombo.getValue();
        if (selectedPatientName == null || selectedPatientName.isEmpty() || selectedPatientName.equals("Tous les patients")) {
            showAlert("Erreur", "Veuillez sélectionner un patient valide.");
            return;
        }
        Patient selectedPatient = null;
        for(Patient p : allPatients) {
            if(p.getName().equals(selectedPatientName)) {
                selectedPatient = p;
                break;
            }
        }
        if (selectedPatient != null) {
            generateQuickReport(selectedPatient);
        } else {
            showAlert("Erreur", "Patient introuvable.");
        }
    }
    private void generateQuickReport(Patient patient) {
        try {
            ConsulationServices consultationService = new ConsulationServices();
            List<Consultation> all = consultationService.ShowConsultation();
            Consultation latestNote = null;
            for (Consultation c : all) {
                if (patient.getId().equals(c.getPatientId())) {
                    if (latestNote == null || c.getDateConsultation().isAfter(latestNote.getDateConsultation())) {
                        latestNote = c;
                    }
                }
            }
            if (latestNote == null) {
                showAlert("Erreur", "Aucune note clinique trouvée pour ce patient.");
                return;
            }

            StringBuilder sb = new StringBuilder();
            sb.append("SUBJECTIVE:\n").append(latestNote.getSubjective() != null ? latestNote.getSubjective() : "").append("\n\n");
            sb.append("OBJECTIVE:\n").append(latestNote.getObjective() != null ? latestNote.getObjective() : "").append("\n\n");
            sb.append("ASSESSMENT:\n").append(latestNote.getAssessment() != null ? latestNote.getAssessment() : "").append("\n\n");
            sb.append("PLAN:\n").append(latestNote.getPlan() != null ? latestNote.getPlan() : "").append("\n");
            
            String rawNote = sb.toString();
            String prompt = """
            # Objective
            You are an expert medical report writer. Transform the following raw clinical SOAP notes into a professional, well‑structured medical report in French.

            # Instructions
            - Write in past tense, third person.
            - Do not add information that is not present in the notes.
            - Organise the final report under these headings:
                **Histoire de la maladie**
                **Examen Physique**
                **Évaluation**
                **Plan de traitement**
            - Use clear medical language. Omit any extra comments.

            # Raw Notes
            %s
            """.formatted(rawNote);

            Task<String> aiTask = new Task<>() {
                @Override
                protected String call() throws Exception {
                    String apiKey = "h4S2z91IjWqucgaphMxdMeFXdEXBYpgb";
                    String escapedPrompt = prompt.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
                    String json = "{\n" +
                            "    \"model\": \"mistral-small-latest\",\n" +
                            "    \"messages\": [\n" +
                            "        {\"role\": \"system\", \"content\": \"You are a professional medical report writer.\"},\n" +
                            "        {\"role\": \"user\", \"content\": \"" + escapedPrompt + "\"}\n" +
                            "    ],\n" +
                            "    \"temperature\": 0.3,\n" +
                            "    \"max_tokens\": 1500\n" +
                            "}";

                    HttpClient client = HttpClient.newHttpClient();
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create("https://api.mistral.ai/v1/chat/completions"))
                            .header("Content-Type", "application/json")
                            .header("Authorization", "Bearer " + apiKey)
                            .POST(HttpRequest.BodyPublishers.ofString(json))
                            .build();

                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                    if (response.statusCode() == 200) {
                        String body = response.body();
                        String target = "\"content\":\"";
                        int start = body.indexOf(target);
                        if (start == -1) return "Erreur d'analyse API";
                        start += target.length();
                        int end = start;
                        while (end < body.length()) {
                            char c = body.charAt(end);
                            if (c == '"' && (end == 0 || body.charAt(end - 1) != '\\')) break;
                            end++;
                        }
                        if (end > start) {
                            String content = body.substring(start, end);
                            return content.replace("\\\"", "\"").replace("\\\\", "\\").replace("\\n", "\n");
                        }
                        return "Erreur d'analyse de la réponse";
                    } else {
                        throw new RuntimeException("API error " + response.statusCode());
                    }
                }
            };

            aiTask.setOnSucceeded(event -> {
                Dialog<Void> dialog = new Dialog<>();
                dialog.setTitle("Rapport IA pour " + patient.getName());
                TextArea textArea = new TextArea(aiTask.getValue());
                textArea.setEditable(true);
                textArea.setWrapText(true);
                textArea.setPrefHeight(400);
                textArea.setPrefWidth(500);
                dialog.getDialogPane().setContent(textArea);
                dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);
                dialog.showAndWait();
            });

            aiTask.setOnFailed(event -> {
                showAlert("Erreur IA", "Échec : " + aiTask.getException().getMessage());
            });

            new Thread(aiTask).start();

        } catch (Exception e) {
            showAlert("Erreur", "Génération du rapport échouée : " + e.getMessage());
        }
    }
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
        private String gender, consId, consultationTime;
        private int healthScore, age;
        private List<PatientAlert> alerts;

        public String getGender() { return gender; }
        public void setGender(String gender) { this.gender = gender; }
        public String getConsId() { return consId; }
        public void setConsId(String consId) { this.consId = consId; }
        public String getConsultationTime() { return consultationTime; }
        public void setConsultationTime(String consultationTime) { this.consultationTime = consultationTime; }

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