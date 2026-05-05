package com.wellcare.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Random;

public class GiphyService {

    private static final String API_KEY = "ZbF5lXVZJLOtEmvNJVbckCxXzLvtSwzX";
    private static final String BASE_URL = "https://api.giphy.com/v1/gifs/random";

    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private static final Random random = new Random();

    // 🔥 GIFS DE SUCCÈS ET CÉLÉBRATION (Fallback - ultra fiables)
    private static final String[] SUCCESS_GIFS = {
            "https://media.giphy.com/media/3o7abB06u9bNzA8LC8/giphy.gif",  // Yes!
            "https://media.giphy.com/media/26ufdipQqU2lhNA4g/giphy.gif",  // Celebration
            "https://media.giphy.com/media/xT9IgzoKnwFNmISR8I/giphy.gif", // Victory
            "https://media.giphy.com/media/l0HlNQ3JY7RmqMvbK/giphy.gif",  // Success
            "https://media.giphy.com/media/3o6Zt481isNVuQI1l6/giphy.gif",  // Good job
            "https://media.giphy.com/media/l4FGyGx8iTn4OJEoE/giphy.gif",   // Amazing
            "https://media.giphy.com/media/26BGGx6wXK6pZfV8c/giphy.gif",   // Champion
            "https://media.giphy.com/media/3o7abB06u9bNzA8LC8/giphy.gif",   // You did it
            "https://media.giphy.com/media/26gsc0p6F9qMOhC6I/giphy.gif",    // Perfect
            "https://media.giphy.com/media/l0MYEqEzwMWFCg8Ji/giphy.gif"      // Great work
    };

    // Tags de succès (pour l'API)
    private static final String[] SUCCESS_TAGS = {
            "workout success",
            "celebration victory",
            "fitness motivation",
            "good job",
            "congratulations workout",
            "you did it",
            "success champion",
            "victory dance"
    };

    public static String getCelebrationGif(String tag) {
        // Essayer avec un tag de succès aléatoire
        String successTag = SUCCESS_TAGS[random.nextInt(SUCCESS_TAGS.length)];

        try {
            String encodedTag = URLEncoder.encode(successTag, StandardCharsets.UTF_8.toString());
            String url = BASE_URL + "?api_key=" + API_KEY + "&tag=" + encodedTag + "&rating=g";

            System.out.println("🔍 Recherche GIF: " + successTag);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode json = mapper.readTree(response.body());
                String gifUrl = json.path("data").path("images").path("original").path("url").asText();

                if (gifUrl != null && !gifUrl.isEmpty()) {
                    System.out.println("✅ GIF de succès trouvé !");
                    return gifUrl;
                }
            }

            System.out.println("🎁 Utilisation d'un GIF de succès local");
            return SUCCESS_GIFS[random.nextInt(SUCCESS_GIFS.length)];

        } catch (Exception e) {
            System.out.println("⚠️ Erreur API, utilisation GIF local");
            return SUCCESS_GIFS[random.nextInt(SUCCESS_GIFS.length)];
        }
    }

    /**
     * Récupère un GIF spécifiquement pour la réussite d'un objectif
     */
    public static String getGoalCompletedGif() {
        return getCelebrationGif("goal completed success celebration");
    }

    /**
     * Récupère un GIF pour la réussite d'un plan
     */
    public static String getPlanCompletedGif() {
        return getCelebrationGif("workout plan completed victory");
    }

    /**
     * Récupère un GIF pour un exercice complété
     */
    public static String getExerciseCompletedGif() {
        return getCelebrationGif("exercise done good job");
    }

    /**
     * Récupère un GIF de récompense aléatoire (toujours un succès)
     */
    public static String getRandomRewardGif() {
        return SUCCESS_GIFS[random.nextInt(SUCCESS_GIFS.length)];
    }
}