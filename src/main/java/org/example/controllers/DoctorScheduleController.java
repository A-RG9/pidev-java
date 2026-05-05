package org.example.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

public class DoctorScheduleController {

    @FXML private Label weekRangeLabel;
    @FXML private StackPane pendingBadgeContainer;
    @FXML private Label pendingBadge;
    @FXML private VBox timeLabelsContainer;
    @FXML private HBox daysContainer;
    @FXML private VBox todayAppointmentsList;
    @FXML private VBox upcomingList;
    @FXML private Label todayLabel;
    @FXML private StackPane appointmentModal;
    @FXML private VBox appointmentModalContent;
    @FXML private StackPane dayAppointmentsModal;
    @FXML private VBox dayAppointmentsContent;
    @FXML private Label selectedDateDisplay;

    private LocalDate currentWeekStart;
    private List<AppointmentDTO> allAppointments = new ArrayList<>();
    private Map<LocalDate, List<AppointmentDTO>> appointmentsByDay = new HashMap<>();
    private int pendingCount = 0;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final int START_HOUR = 8;
    private static final int END_HOUR = 18;
    private static final int ROW_HEIGHT = 60;

    @FXML
    public void initialize() {
        buildTimeColumn();
        currentWeekStart = getStartOfWeek(LocalDate.now());
        buildWeekView();
        loadAcceptedAppointments();
        loadPendingCount();
        startAutoRefresh();
    }

    private void buildTimeColumn() {
        timeLabelsContainer.getChildren().clear();
        for (int hour = START_HOUR; hour <= END_HOUR; hour++) {
            Label timeLabel = new Label(String.format("%02d:00", hour));
            timeLabel.setPrefHeight(ROW_HEIGHT);
            timeLabel.setAlignment(Pos.TOP_CENTER);
            timeLabel.getStyleClass().add("time-label");
            timeLabelsContainer.getChildren().add(timeLabel);
        }
    }

    private void buildWeekView() {
        daysContainer.getChildren().clear();
        LocalDate weekEnd = currentWeekStart.plusDays(6);
        weekRangeLabel.setText(formatWeekRange(currentWeekStart, weekEnd));

        for (int i = 0; i < 7; i++) {
            LocalDate date = currentWeekStart.plusDays(i);
            VBox dayColumn = createDayColumn(date);
            daysContainer.getChildren().add(dayColumn);
        }
        renderAppointments();
    }

    private VBox createDayColumn(LocalDate date) {
        VBox column = new VBox();
        column.setPrefWidth(160);
        column.setMinWidth(160);
        column.getStyleClass().add("day-column");
        column.setStyle("-fx-background-color: white; -fx-border-color: #e5e7eb; -fx-border-width: 0 1 0 0;");

        // Header
        VBox header = new VBox(4);
        header.setAlignment(Pos.CENTER);
        header.setPrefHeight(72);
        header.setStyle("-fx-border-color: #e5e7eb; -fx-border-width: 0 0 1 0;");
        Label dayName = new Label(date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.FRENCH).toUpperCase());
        dayName.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #6b7280;");
        Label dayNumber = new Label(String.valueOf(date.getDayOfMonth()));
        dayNumber.setStyle("-fx-font-size: 24px; -fx-font-weight: 600; -fx-text-fill: #1f2937;");
        if (date.equals(LocalDate.now())) {
            dayNumber.setStyle("-fx-font-size: 24px; -fx-font-weight: 600; -fx-text-fill: #00A790; -fx-background-color: #d1fae5; -fx-background-radius: 50%; -fx-padding: 6;");
            Rectangle todayIndicator = new Rectangle(24, 2);
            todayIndicator.setFill(Color.valueOf("#00A790"));
            header.getChildren().addAll(dayName, dayNumber, todayIndicator);
        } else {
            header.getChildren().addAll(dayName, dayNumber);
        }
        column.getChildren().add(header);

        // Appointments container (ScrollPane inside each day)
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-border-width: 0;");
        VBox appointmentsBox = new VBox(8);
        appointmentsBox.setPadding(new Insets(12));
        scrollPane.setContent(appointmentsBox);
        column.getChildren().add(scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        appointmentsBox.setUserData(date);
        return column;
    }

    private void renderAppointments() {
        for (Node node : daysContainer.getChildren()) {
            VBox column = (VBox) node;
            VBox appointmentsBox = (VBox) ((ScrollPane) column.getChildren().get(1)).getContent();
            appointmentsBox.getChildren().clear();
            LocalDate columnDate = (LocalDate) appointmentsBox.getUserData();
            List<AppointmentDTO> dayApps = appointmentsByDay.getOrDefault(columnDate, Collections.emptyList());
            if (dayApps.isEmpty()) {
                Label emptyLabel = new Label("Libre");
                emptyLabel.setStyle("-fx-text-fill: #10b981; -fx-font-size: 12px; -fx-font-weight: 500; -fx-alignment: center; -fx-padding: 20;");
                appointmentsBox.getChildren().add(emptyLabel);
            } else {
                for (AppointmentDTO apt : dayApps) {
                    VBox card = createAppointmentCard(apt);
                    appointmentsBox.getChildren().add(card);
                }
            }
        }
        updateSidebar();
    }

    private VBox createAppointmentCard(AppointmentDTO apt) {
        VBox card = new VBox(4);
        card.setPadding(new Insets(10));
        card.setStyle(getCardStyle(apt.getType()));
        card.setOnMouseClicked(e -> openAppointmentDetails(apt));

        Label timeLabel = new Label(apt.getTime().substring(0, 5));
        timeLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + getTypeColor(apt.getType()) + ";");
        Label nameLabel = new Label(apt.getPatientName());
        nameLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #1f2937;");
        Label reasonLabel = new Label(apt.getReason());
        reasonLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6b7280;");
        card.getChildren().addAll(timeLabel, nameLabel, reasonLabel);
        return card;
    }

    private String getCardStyle(String type) {
        String bgColor, borderColor;
        switch (type) {
            case "consultation":
                bgColor = "#ecfdf5"; borderColor = "#a7f3d0"; break;
            case "follow-up":
                bgColor = "#eff6ff"; borderColor = "#bfdbfe"; break;
            case "new-patient":
                bgColor = "#f5f3ff"; borderColor = "#e9d5ff"; break;
            default:
                bgColor = "#f3f4f6"; borderColor = "#e5e7eb";
        }
        return String.format("-fx-background-color: %s; -fx-border-color: %s; -fx-border-radius: 12; -fx-background-radius: 12; -fx-cursor: hand;", bgColor, borderColor);
    }

    private String getTypeColor(String type) {
        switch (type) {
            case "consultation": return "#00A790";
            case "follow-up": return "#3B82F6";
            case "new-patient": return "#A855F7";
            default: return "#6B7280";
        }
    }

    private void updateSidebar() {
        LocalDate today = LocalDate.now();
        todayLabel.setText(today.format(DateTimeFormatter.ofPattern("EEEE d MMMM", Locale.FRENCH)));
        todayAppointmentsList.getChildren().clear();
        List<AppointmentDTO> todayApps = appointmentsByDay.getOrDefault(today, Collections.emptyList());
        if (todayApps.isEmpty()) {
            Label empty = new Label("Aucune disponibilité");
            empty.setStyle("-fx-text-fill: #9ca3af; -fx-font-style: italic; -fx-padding: 12; -fx-alignment: center;");
            todayAppointmentsList.getChildren().add(empty);
        } else {
            for (AppointmentDTO apt : todayApps) {
                HBox item = new HBox(12);
                item.setAlignment(Pos.CENTER_LEFT);
                Label time = new Label(apt.getTime().substring(0, 5));
                time.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #00A790; -fx-min-width: 45;");
                Label name = new Label(apt.getPatientName());
                name.setStyle("-fx-font-size: 13px; -fx-text-fill: #1f2937;");
                item.getChildren().addAll(time, name);
                todayAppointmentsList.getChildren().add(item);
            }
        }

        upcomingList.getChildren().clear();
        List<AppointmentDTO> upcoming = allAppointments.stream()
                .filter(a -> !a.getDate().isBefore(today))
                .limit(10)
                .collect(Collectors.toList());
        if (upcoming.isEmpty()) {
            Label empty = new Label("Aucun rendez-vous à venir");
            empty.setStyle("-fx-text-fill: #9ca3af; -fx-font-style: italic; -fx-padding: 12; -fx-alignment: center;");
            upcomingList.getChildren().add(empty);
        } else {
            for (AppointmentDTO apt : upcoming) {
                HBox item = new HBox(12);
                item.setAlignment(Pos.CENTER_LEFT);
                item.setStyle("-fx-padding: 8 0; -fx-border-color: #e5e7eb; -fx-border-width: 0 0 1 0; -fx-cursor: hand;");
                item.setOnMouseClicked(e -> openAppointmentDetails(apt));
                Label dateTime = new Label(apt.getDate().format(DateTimeFormatter.ofPattern("dd/MM")) + " " + apt.getTime().substring(0, 5));
                dateTime.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #00A790; -fx-min-width: 65;");
                Label nameReason = new Label(apt.getPatientName() + " · " + apt.getReason());
                nameReason.setStyle("-fx-font-size: 12px; -fx-text-fill: #1f2937;");
                item.getChildren().addAll(dateTime, nameReason);
                upcomingList.getChildren().add(item);
            }
        }
    }

    private void loadAcceptedAppointments() {
        new Thread(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/appointment/api/doctor/accepted"))
                        .header("Accept", "application/json")
                        .GET().build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    JsonNode root = objectMapper.readTree(response.body());
                    List<AppointmentDTO> list = new ArrayList<>();
                    if (root.isArray()) {
                        for (JsonNode node : root) {
                            AppointmentDTO apt = new AppointmentDTO();
                            apt.id = node.path("id").asInt();
                            apt.patientName = node.path("patientName").asText("Patient");
                            apt.date = LocalDate.parse(node.path("dateConsultation").asText());
                            String timeStr = node.path("timeConsultation").asText("09:00:00");
                            apt.time = timeStr.length() > 5 ? timeStr.substring(0, 5) : timeStr;
                            apt.duration = node.path("duration").asInt(30);
                            apt.type = node.path("consultationType").asText("consultation");
                            apt.reason = node.path("reasonForVisit").asText("Consultation");
                            list.add(apt);
                        }
                    }
                    allAppointments = list;
                    appointmentsByDay = allAppointments.stream().collect(Collectors.groupingBy(AppointmentDTO::getDate));
                    Platform.runLater(this::renderAppointments);
                }
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    private void loadPendingCount() {
        new Thread(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/appointment/api/doctor/pending"))
                        .GET().build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    JsonNode root = objectMapper.readTree(response.body());
                    pendingCount = root.path("count").asInt(0);
                    Platform.runLater(() -> {
                        if (pendingCount > 0) {
                            pendingBadge.setText(String.valueOf(pendingCount));
                            pendingBadgeContainer.setVisible(true);
                        } else {
                            pendingBadgeContainer.setVisible(false);
                        }
                    });
                }
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    private void startAutoRefresh() {
        new Thread(() -> {
            while (true) {
                try { Thread.sleep(30000); } catch (InterruptedException e) { break; }
                Platform.runLater(() -> { loadAcceptedAppointments(); loadPendingCount(); });
            }
        }).start();
    }

    @FXML private void previousWeek() { currentWeekStart = currentWeekStart.minusWeeks(1); refresh(); }
    @FXML private void nextWeek() { currentWeekStart = currentWeekStart.plusWeeks(1); refresh(); }
    @FXML private void goToCurrentWeek() { currentWeekStart = getStartOfWeek(LocalDate.now()); refresh(); }
    private void refresh() { buildWeekView(); loadAcceptedAppointments(); }

    private LocalDate getStartOfWeek(LocalDate date) {
        return date.minusDays(date.getDayOfWeek().getValue() - 1);
    }

    private String formatWeekRange(LocalDate start, LocalDate end) {
        DateTimeFormatter df = DateTimeFormatter.ofPattern("d MMM", Locale.FRENCH);
        if (start.getMonth() == end.getMonth())
            return start.getDayOfMonth() + " - " + end.format(df) + " " + start.getYear();
        else
            return start.format(df) + " - " + end.format(df) + " " + end.getYear();
    }

    @FXML private void openPendingRequests() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/doctor-pending.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) daysContainer.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void openLeaveRequest() { showToast("Demande de congé envoyée", "info"); }
    @FXML private void openAppointmentDetails(AppointmentDTO apt) { /* build modal content */ appointmentModal.setVisible(true); }
    @FXML private void closeAppointmentModal() { appointmentModal.setVisible(false); }
    @FXML private void closeDayAppointmentsModal() { dayAppointmentsModal.setVisible(false); }

    private void showToast(String message, String type) {
        Label toast = new Label(message);
        toast.setStyle("-fx-background-color: #00A790; -fx-text-fill: white; -fx-padding: 10 20; -fx-background-radius: 8;");
        StackPane root = (StackPane) daysContainer.getScene().getRoot();
        root.getChildren().add(toast);
        StackPane.setAlignment(toast, Pos.TOP_RIGHT);
        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(e -> root.getChildren().remove(toast));
        delay.play();
    }

    // Inner DTO
    static class AppointmentDTO {
        int id; String patientName; LocalDate date; String time; int duration; String type; String reason;
        public LocalDate getDate() { return date; }
        public String getTime() { return time; }
        public String getPatientName() { return patientName; }
        public String getReason() { return reason; }
        public String getType() { return type; }
    }
}