package com.wellcare.javafx.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.concurrent.Task;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

public class DoctorAnalyticsService {

    private static final String API_BASE = "http://localhost:5000/api/doctor";
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final int doctorId;

    public DoctorAnalyticsService(int doctorId) {
        this.doctorId = doctorId;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public Task<Map<String, Object>> getStatsTask() {
        return new Task<>() {
            @Override
            protected Map<String, Object> call() throws Exception {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(API_BASE + "/" + doctorId + "/stats"))
                        .header("Accept", "application/json")
                        .GET()
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    JsonNode root = objectMapper.readTree(response.body());
                    Map<String, Object> stats = new HashMap<>();
                    stats.put("totalPatients", root.path("totalPatients").asInt());
                    stats.put("criticalAlerts", root.path("criticalAlerts").asInt());
                    stats.put("todayAppointments", root.path("todayAppointments").asInt());
                    stats.put("reportsGenerated", root.path("reportsGenerated").asInt());
                    return stats;
                } else {
                    throw new RuntimeException("API error: " + response.statusCode());
                }
            }
        };
    }

    public Task<List<Map<String, Object>>> getAIPredictionsTask() {
        return new Task<>() {
            @Override
            protected List<Map<String, Object>> call() throws Exception {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(API_BASE.replace("/api/doctor", "/api/predict/doctor") + "/" + doctorId))
                        .GET()
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    JsonNode root = objectMapper.readTree(response.body());
                    JsonNode predictionsArray = root.path("predictions");
                    List<Map<String, Object>> predictions = new ArrayList<>();
                    for (JsonNode node : predictionsArray) {
                        Map<String, Object> p = new HashMap<>();
                        p.put("day_name", node.path("day_name").asText());
                        p.put("day", node.path("day").asText());
                        p.put("predicted_consultations", node.path("predicted_consultations").asInt());
                        predictions.add(p);
                    }
                    return predictions;
                } else {
                    return Collections.emptyList();
                }
            }
        };
    }

    public Task<List<Patient>> getPatientsTask() {
        return new Task<>() {
            @Override
            protected List<Patient> call() throws Exception {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(API_BASE + "/" + doctorId + "/patients"))
                        .GET()
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    JsonNode arr = objectMapper.readTree(response.body());
                    List<Patient> patients = new ArrayList<>();
                    for (JsonNode node : arr) {
                        Patient p = new Patient();
                        p.setId(node.path("id").asText());
                        p.setName(node.path("name").asText());
                        p.setHealthScore(node.path("healthScore").asInt());
                        p.setTrendLabel(node.path("trendLabel").asText());
                        p.setLastEntryDateFormatted(node.path("lastEntryDateFormatted").asText());
                        p.setAge(node.path("age").asInt());
                        List<Patient.Alert> alerts = new ArrayList<>();
                        JsonNode alertsNode = node.path("alerts");
                        for (JsonNode a : alertsNode) {
                            Patient.Alert alert = new Patient.Alert();
                            alert.setSeverity(a.asText());
                            alerts.add(alert);
                        }
                        p.setAlerts(alerts);
                        patients.add(p);
                    }
                    return patients;
                }
                return Collections.emptyList();
            }
        };
    }

    public Task<Map<String, Double>> getTreatmentEffectivenessTask(String metric) {
        return new Task<>() {
            @Override
            protected Map<String, Double> call() throws Exception {
                String url = API_BASE + "/" + doctorId + "/treatmentEffectiveness?metric=" + metric;
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    JsonNode root = objectMapper.readTree(response.body());
                    Map<String, Double> data = new LinkedHashMap<>();
                    root.fields().forEachRemaining(entry -> data.put(entry.getKey(), entry.getValue().asDouble()));
                    return data;
                }
                return Collections.emptyMap();
            }
        };
    }

    public Task<Map<String, Object>> getProfitPredictionsTask() {
        return new Task<>() {
            @Override
            protected Map<String, Object> call() throws Exception {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(API_BASE + "/" + doctorId + "/profit"))
                        .GET()
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    JsonNode root = objectMapper.readTree(response.body());
                    Map<String, Object> profit = new HashMap<>();
                    profit.put("current_monthly", root.path("current_monthly").asDouble());
                    profit.put("next_month", root.path("next_month").asDouble());
                    profit.put("trend", root.path("growth").asDouble());
                    profit.put("avg_fee", root.path("avg_fee").asDouble());
                    profit.put("last_30", root.path("last_30").asDouble());
                    profit.put("next_30", root.path("next_30").asDouble());
                    profit.put("vs_specialty", root.path("vs_specialty").asDouble());
                    profit.put("specialty_avg", root.path("specialty_avg").asDouble());

                    List<Map<String, Object>> monthly = new ArrayList<>();
                    JsonNode forecast = root.path("monthly_forecast");
                    for (JsonNode f : forecast) {
                        Map<String, Object> m = new HashMap<>();
                        m.put("month", f.path("month").asText());
                        m.put("revenue", f.path("revenue").asDouble());
                        monthly.add(m);
                    }
                    profit.put("monthly_forecast", monthly);
                    profit.put("recommendations", root.path("recommendations"));
                    return profit;
                }
                return Collections.emptyMap();
            }
        };
    }

    public Task<List<String>> getPlanningSuggestionsTask() {
        return new Task<>() {
            @Override
            protected List<String> call() throws Exception {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(API_BASE + "/" + doctorId + "/planningSuggestions"))
                        .GET()
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    JsonNode arr = objectMapper.readTree(response.body());
                    List<String> suggestions = new ArrayList<>();
                    for (JsonNode node : arr) suggestions.add(node.asText());
                    return suggestions;
                }
                return Collections.emptyList();
            }
        };
    }

    public Task<List<String>> getAIRecommendationsTask() {
        return new Task<>() {
            @Override
            protected List<String> call() throws Exception {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(API_BASE + "/" + doctorId + "/recommendations"))
                        .GET()
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    JsonNode arr = objectMapper.readTree(response.body());
                    List<String> recs = new ArrayList<>();
                    for (JsonNode node : arr) recs.add(node.asText());
                    return recs;
                }
                return Collections.emptyList();
            }
        };
    }

    public Task<List<Patient.Alert>> getRecentAlertsTask() {
        return new Task<>() {
            @Override
            protected List<Patient.Alert> call() throws Exception {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(API_BASE + "/" + doctorId + "/recentAlerts"))
                        .GET()
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    JsonNode arr = objectMapper.readTree(response.body());
                    List<Patient.Alert> alerts = new ArrayList<>();
                    for (JsonNode node : arr) {
                        Patient.Alert alert = new Patient.Alert();
                        alert.setPatientName(node.path("patientName").asText());
                        alert.setMessage(node.path("message").asText());
                        alert.setSeverity(node.path("severity").asText());
                        alert.setTime(node.path("time").asText());
                        alerts.add(alert);
                    }
                    return alerts;
                }
                return Collections.emptyList();
            }
        };
    }

    public static class Patient {
        private String id, name, trendLabel, lastEntryDateFormatted;
        private int healthScore, age;
        private List<Alert> alerts;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getTrendLabel() { return trendLabel; }
        public void setTrendLabel(String trendLabel) { this.trendLabel = trendLabel; }
        public String getLastEntryDateFormatted() { return lastEntryDateFormatted; }
        public void setLastEntryDateFormatted(String lastEntryDateFormatted) { this.lastEntryDateFormatted = lastEntryDateFormatted; }
        public int getHealthScore() { return healthScore; }
        public void setHealthScore(int healthScore) { this.healthScore = healthScore; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
        public List<Alert> getAlerts() { return alerts; }
        public void setAlerts(List<Alert> alerts) { this.alerts = alerts; }

        public boolean hasCriticalAlert() {
            if (alerts == null) return false;
            return alerts.stream()
                    .anyMatch(a -> "critical".equals(a.getSeverity()));
        }

        public boolean hasWarningAlert() {
            if (alerts == null) return false;
            return alerts.stream()
                    .anyMatch(a -> "warning".equals(a.getSeverity()) || "moderate".equals(a.getSeverity()));
        }

        public static class Alert {
            private String patientName, message, severity, time, icon;
            public String getPatientName() { return patientName; }
            public void setPatientName(String patientName) { this.patientName = patientName; }
            public String getMessage() { return message; }
            public void setMessage(String message) { this.message = message; }
            public String getSeverity() { return severity; }
            public void setSeverity(String severity) { this.severity = severity; }
            public String getTime() { return time; }
            public void setTime(String time) { this.time = time; }
            public String getIcon() { return icon; }
            public void setIcon(String icon) { this.icon = icon; }
        }
    }
}
