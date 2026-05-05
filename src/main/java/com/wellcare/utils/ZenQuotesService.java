package com.wellcare.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class ZenQuotesService {

    // URL de l'API ZenQuotes - catégorie sports (gratuit, sans clé)
    private static final String API_URL = "https://zenquotes.io/api/quotes/sports";

    /**
     * Récupère une citation sportive aléatoire depuis l'API ZenQuotes
     * @return QuoteResponse contenant le texte et l'auteur
     */
    public static QuoteResponse getRandomSportQuote() {
        try {
            // 1. Créer un client HTTP
            HttpClient client = HttpClient.newHttpClient();

            // 2. Créer la requête HTTP GET
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .build();

            // 3. Envoyer la requête et recevoir la réponse
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // 4. Parser le JSON
            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(response.body());

            // 5. Extraire les données (ZenQuotes retourne un tableau)
            JsonNode firstQuote = json.get(0);
            String quoteText = firstQuote.path("q").asText();
            String quoteAuthor = firstQuote.path("a").asText();

            // 6. Nettoyer les guillemets si présents
            quoteText = quoteText.replaceAll("^\"|\"$", "");

            return new QuoteResponse(quoteText, quoteAuthor);

        } catch (Exception e) {
            e.printStackTrace();
            // En cas d'erreur, retourner une citation sportive par défaut
            return getDefaultSportQuote();
        }
    }

    /**
     * Récupère plusieurs citations sportives
     * @param count Nombre de citations à récupérer (max 50)
     */
    public static List<QuoteResponse> getMultipleSportQuotes(int count) {
        List<QuoteResponse> quotes = new ArrayList<>();
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(response.body());

            int limit = Math.min(count, json.size());
            for (int i = 0; i < limit; i++) {
                String quoteText = json.get(i).path("q").asText().replaceAll("^\"|\"$", "");
                String quoteAuthor = json.get(i).path("a").asText();
                quotes.add(new QuoteResponse(quoteText, quoteAuthor));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return quotes;
    }

    /**
     * Citations sportives par défaut en cas d'erreur réseau
     */
    private static QuoteResponse getDefaultSportQuote() {
        String[][] sportQuotes = {
                {"Le vrai succès, c'est de se relever plus fort à chaque chute.", "Wellora Sport"},
                {"La seule mauvaise séance est celle qui n'a pas eu lieu.", "Fitness Motivation"},
                {"Un champion n'est pas fait de victoires, mais de reprises après les défaites.", "Sport Spirit"},
                {"La douleur que tu ressens aujourd'hui sera ta force demain.", "Athlete Mindset"},
                {"L'impossible n'est qu'une opinion, pas un fait.", "Wellora Coach"},
                {"Petits progrès chaque jour = grands résultats.", "Fitness Daily"},
                {"Ton seul adversaire, c'est toi-même.", "Motivation Sport"},
                {"La discipline finit par battre la motivation.", "Sport Wisdom"},
                {"Ne compte pas les jours, fais que les jours comptent.", "Fitness Quote"},
                {"Le corps humain est la meilleure œuvre d'art.", "Sport Inspiration"}
        };
        int randomIndex = (int)(Math.random() * sportQuotes.length);
        return new QuoteResponse(sportQuotes[randomIndex][0], sportQuotes[randomIndex][1]);
    }

    /**
     * Classe interne pour encapsuler la réponse
     */
    public static class QuoteResponse {
        private String text;
        private String author;

        public QuoteResponse(String text, String author) {
            this.text = text;
            this.author = author;
        }

        public String getText() { return text; }
        public String getAuthor() { return author; }

        @Override
        public String toString() {
            return "\"" + text + "\" — " + author;
        }
    }
}