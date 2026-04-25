package com.wellora.services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.json.JSONArray;
import org.json.JSONObject;

public class NutritionApiService {

    // On crée un seul client HTTP pour toute l'application (meilleure performance)
    private final HttpClient client;

    public NutritionApiService() {
        this.client = HttpClient.newHttpClient();
    }

    /**
     * Cherche un aliment sur l'API OpenFoodFacts et retourne ses calories pour 100g.
     * @param foodName Le nom de l'aliment (ex: "banane", "pizza")
     * @return Les calories (kcal) ou 0 si non trouvé
     */
    public int getCaloriesForFood(String foodName) {
        try {
            // Encodage du texte pour l'URL (ex: "pomme de terre" -> "pomme+de+terre")
            String encodedQuery = URLEncoder.encode(foodName, StandardCharsets.UTF_8);

            // URL de l'API OpenFoodFacts
            String apiUrl = "https://world.openfoodfacts.org/cgi/search.pl?search_terms="
                    + encodedQuery + "&search_simple=1&action=process&json=1&page_size=1";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .GET()
                    .build();

            // Envoi de la requête
            HttpResponse<String> response = this.client.send(request, HttpResponse.BodyHandlers.ofString());

            // Analyse de la réponse JSON
            JSONObject jsonResponse = new JSONObject(response.body());
            JSONArray products = jsonResponse.optJSONArray("products");

            // Si on a trouvé un produit
            if (products != null && products.length() > 0) {
                JSONObject product = products.getJSONObject(0);
                JSONObject nutriments = product.optJSONObject("nutriments");

                // Récupération des calories
                if (nutriments != null && nutriments.has("energy-kcal_100g")) {
                    return nutriments.getInt("energy-kcal_100g");
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur dans NutritionApiService : " + e.getMessage());
        }
        return 0; // Retourne 0 en cas d'erreur ou si rien n'est trouvé
    }
}