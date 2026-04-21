package com.wellora.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class ApiService {
    private static final String QUOTE_API_URL = "https://zenquotes.io/api/random";
    private static final int TIMEOUT = 5000;

    // =========================
    // 🎯 PUBLIC METHODS
    // =========================

    // 🔹 Get motivational quote from API
    public String getQuote() {
        try {
            String response = sendGetRequest(QUOTE_API_URL);
            return parseQuote(response);
        } catch (Exception e) {
            return getFallbackQuote();
        }
    }

    // 🔹 Get risk level based on score
    public String getRiskLevel(int score) {
        if (score < 30) return "HIGH";
        if (score < 50) return "MODERATE";
        if (score < 70) return "LOW";
        return "MINIMAL";
    }

    // 🔹 Get risk description based on score
    public String getRiskDescription(int score) {
        if (score < 30) {
            return "Risque élevé: Consultez un médecin rapidement. Priorisez le repos et l'hydratation. Surveillez vos symptômes.";
        } else if (score < 50) {
            return "Risque modéré: Améliorez votre sommeil et surveillez votre glycémie. Maintenez une bonne hydratation.";
        } else if (score < 70) {
            return "Risque faible: Continuez vos bonnes habitudes. Surveillez régulièrement vos indicateurs de santé.";
        } else {
            return "Risque minimal: Excellent état de santé! Gardez votre routine saine et équilibrée.";
        }
    }

    // 🔹 Local health advice based on score
    public String getHealthAdvice(int score) {
        if (score < 40) {
            return "Vous devez vous reposer, vous hydrater et réduire le stress.";
        } else if (score < 70) {
            return "Maintenez un style de vie équilibré et surveillez votre santé.";
        } else {
            return "Excellent travail! Gardez votre routine saine.";
        }
    }

    // =========================
    // 🔧 PRIVATE HELPERS
    // =========================

    // 🔹 Send HTTP GET request
    private String sendGetRequest(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("GET");
        conn.setConnectTimeout(TIMEOUT);
        conn.setReadTimeout(TIMEOUT);

        int status = conn.getResponseCode();

        if (status != 200) {
            throw new RuntimeException("HTTP error code: " + status);
        }

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)
        );

        String response = reader.lines().collect(Collectors.joining());
        reader.close();

        return response;
    }

    // 🔹 Parse JSON manually (simple & fast)
    private String parseQuote(String json) {
        try {
            // Example response: [{"q":"Quote text","a":"Author"}]
            String quote = json.split("\"q\":\"")[1].split("\"")[0];
            String author = json.split("\"a\":\"")[1].split("\"")[0];

            return quote + " — " + author;

        } catch (Exception e) {
            return getFallbackQuote();
        }
    }

    // 🔹 Fallback quote (if API fails)
    private String getFallbackQuote() {
        return "Take care of your body. It's the only place you have to live.";
    }
}

