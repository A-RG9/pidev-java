package com.wellora.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Random;
import java.util.stream.Collectors;

public class ApiService {
    private static final String QUOTE_API_URL = "https://zenquotes.io/api/random";
    private static final String WEATHER_API_URL = "https://api.open-meteo.com/v1/forecast";
    private static final int TIMEOUT = 5000;
    private static final Random random = new Random();

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

    // 🔹 Get weather data by date (using Open-Meteo API or fallback)
    public String getWeatherByDate(java.time.LocalDate date) {
        // Default location: Paris, France (can be customized)
        String latitude = "48.8566";
        String longitude = "2.3522";

        try {
            String url = WEATHER_API_URL + "?latitude=" + latitude
                    + "&longitude=" + longitude
                    + "&daily=temperature_2m_max,temperature_2m_min,weathercode"
                    + "&start_date=" + date
                    + "&end_date=" + date
                    + "&timezone=auto";

            String response = sendGetRequest(url);
            return parseWeatherResponse(response, date);
        } catch (Exception e) {
            return getFallbackWeather();
        }
    }

    // 🔹 Parse weather response
    private String parseWeatherResponse(String json, java.time.LocalDate date) {
        try {
            // Simple JSON parsing without library
            double tempMax = extractJsonValue(json, "temperature_2m_max");
            double tempMin = extractJsonValue(json, "temperature_2m_min");
            int weatherCode = (int) extractJsonValue(json, "weathercode");

            String temp = String.format("%.0f°C - %.0f°C", tempMin, tempMax);
            String condition = getWeatherCondition(weatherCode);
            String icon = getWeatherIcon(weatherCode);

            return temp + "|" + condition + "|" + icon;
        } catch (Exception e) {
            return getFallbackWeather();
        }
    }

    // 🔹 Extract numeric value from JSON
    private double extractJsonValue(String json, String key) {
        try {
            String searchKey = "\"" + key + "\":[";
            int idx = json.indexOf(searchKey);
            if (idx >= 0) {
                int start = idx + searchKey.length();
                int end = json.indexOf("]", start);
                String value = json.substring(start, end);
                return Double.parseDouble(value.trim());
            }
        } catch (Exception e) { /* ignore */ }
        return 0;
    }

    // 🔹 Get weather condition text
    private String getWeatherCondition(int code) {
        if (code == 0) return "Ciel dégagé";
        if (code <= 3) return "Partiellement nuageux";
        if (code <= 49) return "Brouillard";
        if (code <= 59) return "Bruine";
        if (code <= 69) return "Pluie";
        if (code <= 79) return "Neige";
        if (code <= 82) return "Averses";
        if (code <= 86) return "Neige fondue";
        return "Orage";
    }

    // 🔹 Get weather emoji
    private String getWeatherIcon(int code) {
        if (code == 0) return "☀️";
        if (code <= 3) return "⛅";
        if (code <= 49) return "🌫️";
        if (code <= 69) return "🌧️";
        if (code <= 79) return "❄️";
        if (code <= 86) return "🌨️";
        return "⛈️";
    }

    // 🔹 Fallback weather
    private String getFallbackWeather() {
        String[] conditions = {
            "20°C|Partiellement nuageux|⛅",
            "22°C|Ensoleillé|☀️",
            "18°C|Nuageux|☁️",
            "15°C|Pluie légère|🌧️"
        };
        return conditions[random.nextInt(conditions.length)];
    }
}

