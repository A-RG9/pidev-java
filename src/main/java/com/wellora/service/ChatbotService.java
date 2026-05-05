package com.wellora.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

/**
 * Service for interacting with Mistral AI API for medical chatbot functionality
 * Implements ChatbotServiceInterface for loose coupling.
 * Uses manual JSON (no external libraries).
 */
public class ChatbotService implements ChatbotServiceInterface {

    private static final String API_KEY = "h4S2z91IjWqucgaphMxdMeFXdEXBYpgb";
    private static final String API_URL = "https://api.mistral.ai/v1/chat/completions";

    private final HttpClient httpClient;

    public ChatbotService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
    }

    public ChatbotServiceInterface.ChatResponse generateMedicalResponse(String userMessage, List<ChatbotServiceInterface.Message> conversationHistory) {
        try {
            String systemPrompt = buildMedicalSystemPrompt();

            // Build messages array as JSON string manually
            StringBuilder messagesJson = new StringBuilder();
            messagesJson.append("[");
            // System message
            messagesJson.append("{\"role\":\"system\",\"content\":").append(escapeJson(systemPrompt)).append("},");
            // Conversation history
            for (ChatbotServiceInterface.Message msg : conversationHistory) {
                messagesJson.append("{\"role\":\"").append(msg.getRole()).append("\",\"content\":")
                        .append(escapeJson(msg.getContent())).append("},");
            }
            // Current user message
            messagesJson.append("{\"role\":\"user\",\"content\":").append(escapeJson(userMessage)).append("}");
            messagesJson.append("]");

            String json = "{" +
                    "\"model\":\"mistral-large-latest\"," +
                    "\"messages\":" + messagesJson.toString() + "," +
                    "\"temperature\":0.7," +
                    "\"max_tokens\":800," +
                    "\"top_p\":0.95" +
                    "}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Authorization", "Bearer " + API_KEY)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .timeout(Duration.ofSeconds(30))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String aiMessage = extractContentFromJson(response.body());
                if (aiMessage != null && !aiMessage.trim().isEmpty()) {
                    String level = determineUrgencyLevel(aiMessage);
                    String specialist = extractSpecialist(aiMessage);
                    return ChatbotServiceInterface.ChatResponse.success(aiMessage, level, specialist);
                } else {
                    return ChatbotServiceInterface.ChatResponse.error("Aucune réponse reçue de l'IA.");
                }
            } else {
                System.err.println("Mistral API error " + response.statusCode() + ": " + response.body());
                return ChatbotServiceInterface.ChatResponse.error("Erreur du service IA. Veuillez réessayer plus tard.");
            }
        } catch (Exception e) {
            System.err.println("Error calling Mistral API: " + e.getMessage());
            return ChatbotServiceInterface.ChatResponse.error("Erreur de connexion au service IA: " + e.getMessage());
        }
    }

    public boolean detectEmergency(String message) {
        String lowerMessage = message.toLowerCase();
        String[] emergencyKeywords = {
                "poitrine", "étouffe", "respire pas", "inconscient", "évanoui",
                "saigne", "brûlure grave", "accident", "mal à la poitrine",
                "difficulté respiratoire", "ne respire plus"
        };
        for (String keyword : emergencyKeywords) {
            if (lowerMessage.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private String buildMedicalSystemPrompt() {
        return """
                Tu es un assistant médical WellCare, spécialisé en pré-consultation.
                
                RÈGLES ABSOLUES :
                1. Ne donne JAMAIS de diagnostic médical
                2. Recommande TOUJOURS de consulter un médecin
                3. Détecte les URGENCES et alerte immédiatement (⚠️ URGENCE ⚠️)
                4. Si patient a pris des médicaments, demande lesquels
                5. Propose des remèdes naturels simples (tisanes, repos, hydratation)
                6. Pose UNE seule question à la fois. Attends la réponse avant de poser la suivante.
                7. Ne liste pas plusieurs questions dans une même réponse.
                
                CLASSIFICATION :
                - ROUGE (urgence) : douleur thoracique, difficulté respiratoire, perte connaissance
                - ORANGE (rapide) : fièvre >39°C, douleur intense, vertiges persistants
                - VERT (normal) : symptômes légers, fatigue, toux légère
                
                CONSEILS NATURELS :
                - Toux → tisane thym-miel
                - Fièvre → infusion de sureau, compresses fraîches
                - Maux de tête → repos dans le noir, infusion de menthe
                - Nausées → gingembre, petites gorgées d'eau
                
                Réponds en français, de façon claire et bienveillante. N'utilise pas d'emoji.
                """;
    }

    private String determineUrgencyLevel(String response) {
        if (response.toUpperCase().contains("URGENCE")) return "rouge";
        if (response.toUpperCase().contains("CONSULTEZ") || response.toLowerCase().contains("consultez")) return "orange";
        return "vert";
    }

    private String extractSpecialist(String response) {
        String[] specialists = {
                "cardiologue", "généraliste", "pneumologue", "neurologue",
                "dermatologue", "rhumatologue", "pédiatre", "ophtalmologue",
                "gastro-entérologue", "urologue", "orthopédiste"
        };
        String lowerResponse = response.toLowerCase();
        for (String spec : specialists) {
            if (lowerResponse.contains(spec)) return spec;
        }
        return null;
    }

    // Helper: escape a string for JSON
    private String escapeJson(String s) {
        if (s == null) return "\"\"";
        StringBuilder sb = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            if (c == '"') sb.append("\\\"");
            else if (c == '\\') sb.append("\\\\");
            else if (c == '\n') sb.append("\\n");
            else if (c == '\r') sb.append("\\r");
            else if (c == '\t') sb.append("\\t");
            else sb.append(c);
        }
        sb.append("\"");
        return sb.toString();
    }

    // Helper: extract "content" field from JSON response
    private String extractContentFromJson(String json) {
        String target = "\"content\":\"";
        int start = json.indexOf(target);
        if (start == -1) return null;
        start += target.length();
        int end = start;
        while (end < json.length()) {
            char c = json.charAt(end);
            if (c == '"' && (end == 0 || json.charAt(end-1) != '\\')) break;
            end++;
        }
        if (end > start) {
            String content = json.substring(start, end);
            return content.replace("\\\"", "\"").replace("\\\\", "\\").replace("\\n", "\n").replace("\\r", "\r").replace("\\t", "\t");
        }
        return null;
    }
}