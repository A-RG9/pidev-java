package org.example.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

public class AIService {

    private static final String AI_URL = "http://localhost:5000";
    private final HttpClient httpClient;
    private final ObjectMapper mapper;

    public AIService() {
        this.httpClient = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
    }

    public boolean testConnection() {
        try {
            String url = AI_URL + "/health";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("🔌 Test IA: " + response.statusCode());
            return response.statusCode() == 200;
        } catch (Exception e) {
            System.err.println("❌ IA non disponible: " + e.getMessage());
            return false;
        }
    }

    public Map<String, Object> analyzeRequest(String userRequest) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);

        String lower = userRequest.toLowerCase();
        String category = "General";
        if (lower.contains("perdre") || lower.contains("poids")) category = "Weight Loss";
        else if (lower.contains("muscle")) category = "Muscle Gain";
        else if (lower.contains("endurance") || lower.contains("cardio")) category = "Endurance";
        else if (lower.contains("flexible") || lower.contains("yoga")) category = "Flexibility";

        String difficulty = "Intermediate";
        if (lower.contains("débutant") || lower.contains("beginner")) difficulty = "Beginner";
        else if (lower.contains("avancé") || lower.contains("advanced")) difficulty = "Advanced";

        int weeks = 8;
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d+)\\s*(semaine|week|mois)");
        java.util.regex.Matcher matcher = pattern.matcher(lower);
        if (matcher.find()) {
            int value = Integer.parseInt(matcher.group(1));
            if (lower.contains("mois")) {
                weeks = value * 4;
            } else {
                weeks = value;
            }
        }

        int sessionsPerWeek = 3;
        java.util.regex.Pattern patternSessions = java.util.regex.Pattern.compile("(\\d+)\\s*(séance|session|fois)");
        java.util.regex.Matcher matcherSessions = patternSessions.matcher(lower);
        if (matcherSessions.find()) {
            sessionsPerWeek = Integer.parseInt(matcherSessions.group(1));
        }

        Map<String, Object> analysis = new HashMap<>();
        analysis.put("title", category + " Program - " + weeks + " weeks");
        analysis.put("description", "Personalized " + category + " program for " + difficulty + " level");
        analysis.put("category", category);
        analysis.put("difficultyLevel", difficulty);
        analysis.put("durationWeeks", weeks);
        analysis.put("sessionsPerWeek", sessionsPerWeek);

        result.put("analysis", analysis);
        return result;
    }

    public Map<String, Object> generateProgram(String userRequest) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);

        // Analyser d'abord
        Map<String, Object> analysis = analyzeRequest(userRequest);
        Map<String, Object> analysisData = (Map<String, Object>) analysis.get("analysis");

        Map<String, Object> goal = new HashMap<>();
        goal.put("title", analysisData.get("title"));
        goal.put("description", analysisData.get("description"));
        goal.put("category", analysisData.get("category"));
        goal.put("difficultyLevel", analysisData.get("difficultyLevel"));
        goal.put("durationWeeks", analysisData.get("durationWeeks"));

        result.put("goal", goal);
        result.put("dailyPlans", new java.util.ArrayList<>());

        return result;
    }
}