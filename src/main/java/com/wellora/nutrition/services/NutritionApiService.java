package com.wellora.nutrition.services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.json.JSONArray;
import org.json.JSONObject;

public class NutritionApiService {

    private final HttpClient client;

    public NutritionApiService() {
        // --- NOUVEAUTÉ : On configure le client pour suivre les redirections automatiquement ---
        this.client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public int getCaloriesForFood(String foodName) {
        try {
            String encodedQuery = URLEncoder.encode(foodName, StandardCharsets.UTF_8);

            String apiUrl = "https://world.openfoodfacts.org/cgi/search.pl?search_terms="
                    + encodedQuery + "&search_simple=1&action=process&json=1&page_size=1";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("User-Agent", "WelloraApp/1.0 - Java Application")
                    // --- NOUVEAUTÉ : On force le serveur à répondre en JSON ---
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = this.client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                // On nettoie les espaces invisibles au début et à la fin
                String body = response.body().trim();

                // --- NOUVEAUTÉ : Détecteur de faux JSON ---
                if (!body.startsWith("{")) {
                    System.err.println("❌ L'API n'a pas renvoyé du JSON ! Voici ce qu'elle a renvoyé :");
                    // On affiche les 200 premiers caractères pour comprendre ce qui bloque
                    System.err.println(body.substring(0, Math.min(body.length(), 200)));
                    return 0;
                }

                JSONObject jsonResponse = new JSONObject(body);
                JSONArray products = jsonResponse.optJSONArray("products");

                if (products != null && products.length() > 0) {
                    JSONObject product = products.getJSONObject(0);
                    JSONObject nutriments = product.optJSONObject("nutriments");

                    if (nutriments != null && nutriments.has("energy-kcal_100g")) {
                        // optInt gère automatiquement les erreurs de format si c'est un double
                        return nutriments.optInt("energy-kcal_100g", 0);
                    }
                }
            } else {
                System.err.println("❌ Erreur API - Code HTTP : " + response.statusCode());
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur dans NutritionApiService : " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }
}