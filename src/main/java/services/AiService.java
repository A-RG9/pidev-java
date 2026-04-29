package com.wellora.services;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

public class AiService {

    // Timeout augmenté car lire 7 jours de repas prend un peu plus de temps à l'IA
    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build();

    public String getNutritionalCoachAdvice(String mealData) {
        try {
            // 1. Le Prompt strict pour le comportement du Coach
            String prompt = "Tu es un Coach Nutritionnel expert, sérieux et bienveillant. " +
                    "Voici ce que ton patient a mangé sur les 7 derniers jours : " + mealData + ". " +
                    "Fais une courte analyse de ses habitudes (exemple: 'Tu as mangé beaucoup de glucides ces derniers jours') " +
                    "et donne-lui un conseil pratique et précis pour son prochain repas (exemple: 'Je te conseille un dîner léger riche en fibres comme du poisson et des brocolis'). " +
                    "Réponds obligatoirement en français, sois direct, n'utilise pas de listes, fais 3 ou 4 phrases maximum.";

            // 2. Encodage et Appel de l'API gratuite
            String encodedPrompt = URLEncoder.encode(prompt, StandardCharsets.UTF_8.toString());
            String url = "https://text.pollinations.ai/" + encodedPrompt;

            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();

            // 3. Exécution
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    return response.body().string();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "🧑‍⚕️ Impossible de joindre le coach pour le moment. Vérifie ta connexion internet !";
    }
}