package com.wellora.services;

import com.wellora.models.Recipe;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class RecipeApiService {

    private final HttpClient client;

    public RecipeApiService() {
        this.client = HttpClient.newHttpClient();
    }

    public List<Recipe> getRecipesByCalories(int maxCalories) {
        List<Recipe> recipes = new ArrayList<>();
        try {
            // URL de l'API gratuite (pas de clé nécessaire)
            String apiUrl = "https://dummyjson.com/recipes?limit=50";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .GET()
                    .build();

            HttpResponse<String> response = this.client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JSONObject jsonResponse = new JSONObject(response.body());
                JSONArray results = jsonResponse.optJSONArray("recipes");

                if (results != null) {
                    int count = 0;
                    for (int i = 0; i < results.length(); i++) {
                        JSONObject recipeJson = results.getJSONObject(i);

                        // Récupération des calories par portion
                        int calories = recipeJson.optInt("caloriesPerServing", 0);

                        // Filtre : on ne garde que les recettes en dessous de l'objectif
                        if (calories > 0 && calories <= maxCalories) {
                            String title = recipeJson.optString("name", "Recette sans nom");
                            String imageUrl = recipeJson.optString("image", "");

                            // DummyJSON donne les instructions sous forme de tableau (ex: ["Etape 1", "Etape 2"])
                            JSONArray instrArray = recipeJson.optJSONArray("instructions");
                            StringBuilder instructionsBuilder = new StringBuilder();
                            if (instrArray != null) {
                                for (int j = 0; j < instrArray.length(); j++) {
                                    instructionsBuilder.append(j + 1).append(". ").append(instrArray.getString(j)).append("\n");
                                }
                            }
                            String instructions = instructionsBuilder.toString().trim();
                            if (instructions.isEmpty()) instructions = "Instructions non disponibles.";

                            recipes.add(new Recipe(title, calories, instructions, imageUrl));
                            count++;

                            // On s'arrête à 3 suggestions pour ne pas surcharger l'interface
                            if (count >= 3) break;
                        }
                    }
                }
            } else {
                System.err.println("Erreur de connexion à l'API. Code : " + response.statusCode());
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur RecipeApiService : " + e.getMessage());
            e.printStackTrace();
        }
        return recipes;
    }
}