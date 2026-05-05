package org.example.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class CountryChallengeService {

    private static final String API_URL = "https://restcountries.com/v3.1/all?fields=name,flags,population,region";
    private static final Random random = new Random();

    // CACHE LOCAL
    private static List<CachedCountry> cachedCountries = null;
    private static boolean isLoading = false;

    // Défis sportifs (locaux - ultra rapides)
    private static final String[][] CHALLENGES = {
            {"Europe", "🏃 30 minutes de course à pied", "🇪🇺", "Améliorez votre endurance cardiovasculaire."},
            {"Africa", "🏋️ 40 squats (4x10)", "🦁", "Renforcez vos jambes et votre sangle abdominale."},
            {"Asia", "🧘 15 minutes de méditation", "🌸", "Réduisez le stress et améliorez la concentration."},
            {"Americas", "💪 25 pompes (5x5)", "🌎", "Développez les pectoraux, épaules et triceps."},
            {"Oceania", "🪜 15 étages d'escaliers", "🏝️", "Excellent pour le cardio et les fessiers."},
            {"Antarctic", "🧊 Douche froide 30 secondes", "❄️", "Stimule la circulation sanguine."},
            {"Asia", "🥋 20 minutes d'arts martiaux", "🥋", "Améliore coordination et confiance."},
            {"Europe", "🚴 20 km de vélo", "🚴", "Excellent pour les articulations."},
            {"Americas", "🤸 15 minutes d'étirements", "🤸", "Améliore la souplesse."},
            {"Africa", "🏃‍♂️ 50 jumping jacks", "🦘", "Excellent échauffement cardio."},
            {"Europe", "🏊 30 minutes de natation", "🏊", "Sport complet sans impact."},
            {"Asia", "🥊 10 rounds de shadow boxing", "🥊", "Travaillez votre cardio et votre coordination."},
            {"Americas", "🏀 30 tirs au panier", "🏀", "Améliorez votre précision."},
            {"Oceania", "🏄 20 minutes de gainage", "🏄", "Renforcez votre sangle abdominale."},
            {"Africa", "🦵 50 fentes (25x2)", "🦵", "Renforcez vos jambes et votre équilibre."}
    };

    // Citations inspirantes (ultra rapides)
    private static final String[] FUN_FACTS = {
            "🏃 Le marathon le plus rapide: 1h59 par Eliud Kipchoge (Kenya).",
            "🧘 Le yoga existe depuis plus de 5000 ans en Inde.",
            "🏊 La nage la plus rapide est le crawl.",
            "🤸 La gym suédoise date du 19ème siècle.",
            "🚴 Le Tour de France existe depuis 1903.",
            "🥋 Le judo est devenu olympique en 1964 à Tokyo.",
            "🏆 Le Brésil a gagné 5 Coupes du Monde.",
            "⚽ Le football a 4 milliards de fans.",
            "🎾 Service le plus rapide: 263 km/h.",
            "🏀 La NBA a été créée en 1946.",
            "🏒 Le hockey sur glace existe depuis 1875.",
            "🥇 Les Jeux Olympiques modernes datent de 1896.",
            "🏋️ L'haltérophilie est un sport olympique depuis 1896.",
            "🤺 L'escrime est l'un des sports olympiques les plus anciens."
    };

    /**
     * Charge les pays en arrière-plan une seule fois
     */
    public static void preloadCountries() {
        if (cachedCountries == null && !isLoading) {
            isLoading = true;
            CompletableFuture.supplyAsync(() -> loadCountriesFromApi())
                    .thenAccept(countries -> {
                        cachedCountries = countries;
                        isLoading = false;
                    });
        }
    }

    private static List<CachedCountry> loadCountriesFromApi() {
        List<CachedCountry> countries = new ArrayList<>();
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(response.body());

            for (JsonNode country : json) {
                String name = country.path("name").path("common").asText();
                String flagUrl = country.path("flags").path("png").asText();
                String region = country.path("region").asText();
                long population = country.path("population").asLong();
                countries.add(new CachedCountry(name, flagUrl, region, population));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return countries;
    }

    /**
     * Récupère un défi instantanément (utilise le cache si disponible)
     */
    public static ChallengeResponse getRandomCountryChallenge() {
        // Utiliser le cache si disponible
        if (cachedCountries != null && !cachedCountries.isEmpty()) {
            CachedCountry country = cachedCountries.get(random.nextInt(cachedCountries.size()));
            String[] challenge = getChallengeForRegion(country.region);

            return new ChallengeResponse(
                    country.name, country.flagUrl, country.region, country.population,
                    challenge[1], challenge[2], challenge[3]
            );
        }

        // Sinon, retourner un défi sans API (ultra rapide)
        return getInstantChallenge();
    }

    /**
     * Défi instantané sans API (temps de réponse < 1ms)
     */
    private static ChallengeResponse getInstantChallenge() {
        String[] challenge = CHALLENGES[random.nextInt(CHALLENGES.length)];
        String[] fakeCountries = {"France", "Japon", "Brésil", "Australie", "Kenya", "Canada", "Inde", "Mexique"};
        String randomCountry = fakeCountries[random.nextInt(fakeCountries.length)];
        String flagEmoji = getFlagEmoji(randomCountry);

        return new ChallengeResponse(
                randomCountry,
                "",
                challenge[0],
                (long)(random.nextInt(1000) + 1) * 1000000,
                challenge[1],
                challenge[2] + " " + flagEmoji,
                challenge[3]
        );
    }

    private static String getFlagEmoji(String country) {
        switch(country) {
            case "France": return "🇫🇷";
            case "Japon": return "🇯🇵";
            case "Brésil": return "🇧🇷";
            case "Australie": return "🇦🇺";
            case "Kenya": return "🇰🇪";
            case "Canada": return "🇨🇦";
            case "Inde": return "🇮🇳";
            default: return "🌍";
        }
    }

    private static String[] getChallengeForRegion(String region) {
        List<String[]> regionChallenges = new ArrayList<>();
        for (String[] challenge : CHALLENGES) {
            if (challenge[0].equalsIgnoreCase(region)) {
                regionChallenges.add(challenge);
            }
        }
        if (!regionChallenges.isEmpty()) {
            return regionChallenges.get(random.nextInt(regionChallenges.size()));
        }
        return CHALLENGES[random.nextInt(CHALLENGES.length)];
    }

    public static String getRandomFunFact() {
        return FUN_FACTS[random.nextInt(FUN_FACTS.length)];
    }

    // Classes internes
    private static class CachedCountry {
        String name, flagUrl, region;
        long population;
        CachedCountry(String name, String flagUrl, String region, long population) {
            this.name = name; this.flagUrl = flagUrl; this.region = region; this.population = population;
        }
    }

    public static class ChallengeResponse {
        public String countryName, flagUrl, region, challenge, emoji, description;
        public long population;

        public ChallengeResponse(String countryName, String flagUrl, String region,
                                 long population, String challenge, String emoji, String description) {
            this.countryName = countryName; this.flagUrl = flagUrl; this.region = region;
            this.population = population; this.challenge = challenge; this.emoji = emoji; this.description = description;
        }
    }
}