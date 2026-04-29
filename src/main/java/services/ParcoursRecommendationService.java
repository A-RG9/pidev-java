package services;

import entities.parcours_de_sante;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class ParcoursRecommendationService {

    private static final String WEATHER_ANY = "any";
    private static final int MAX_WEATHER_EVALUATIONS = 6;

    private final WeatherServiceInterface weatherService;

    public ParcoursRecommendationService(WeatherServiceInterface weatherService) {
        this.weatherService = weatherService;
    }


    public List<RecommendationResult> getNearestTrails(List<parcours_de_sante> parcoursList, double userLatitude, double userLongitude, int limit) {
        List<Candidate> candidates = new ArrayList<>();

        for (parcours_de_sante trail : parcoursList) {
            if (trail == null) continue;
            double distance = calculateDistanceKm(userLatitude, userLongitude, trail.getLatitude_parcours(), trail.getLongitude_parcours());
            candidates.add(new Candidate(trail, distance));
        }

        candidates.sort(Comparator.comparingDouble(c -> c.distanceKm));

        List<RecommendationResult> results = new ArrayList<>();
        int actualLimit = Math.min(candidates.size(), limit);
        for(int i = 0; i < actualLimit; i++) {
            Candidate c = candidates.get(i);

            results.add(new RecommendationResult(c.parcours, c.distanceKm, "any", false, null));
        }
        return results;
    }

    public WeatherInfo getWeatherForTrail(parcours_de_sante trail) {
        return weatherService.getCurrentWeatherForCoordinates(
                trail.getLatitude_parcours(),
                trail.getLongitude_parcours(),
                trail.getLocalisation_parcours()
        );
    }


    public RecommendationResult recommendNearestByWeather(
            List<parcours_de_sante> parcoursList, double userLatitude, double userLongitude, String preferredWeather) {

        String normalizedPreference = normalizePreferredWeather(preferredWeather);
        List<Candidate> candidates = new ArrayList<>();

        for (parcours_de_sante trail : parcoursList) {
            if (trail == null) continue;
            double distance = calculateDistanceKm(userLatitude, userLongitude, trail.getLatitude_parcours(), trail.getLongitude_parcours());
            candidates.add(new Candidate(trail, distance));
        }

        if (candidates.isEmpty()) return null;
        candidates.sort(Comparator.comparingDouble(c -> c.distanceKm));

        if (WEATHER_ANY.equals(normalizedPreference)) {
            Candidate nearest = candidates.get(0);
            WeatherInfo weather = weatherService.getCurrentWeatherForCoordinates(
                    nearest.parcours.getLatitude_parcours(), nearest.parcours.getLongitude_parcours(), nearest.parcours.getLocalisation_parcours());
            return new RecommendationResult(nearest.parcours, nearest.distanceKm, normalizedPreference, true, weather);
        }

        Candidate bestCandidate = null;
        int limit = Math.min(candidates.size(), MAX_WEATHER_EVALUATIONS);
        List<Candidate> nearestCandidates = candidates.subList(0, limit);

        for (Candidate candidate : nearestCandidates) {
            WeatherInfo weather = weatherService.getCurrentWeatherForCoordinates(
                    candidate.parcours.getLatitude_parcours(), candidate.parcours.getLongitude_parcours(), candidate.parcours.getLocalisation_parcours());

            int matchScore = getWeatherMatchScore(normalizedPreference, weather);
            double rankingScore = (matchScore * 1000.0) - candidate.distanceKm;

            if (bestCandidate == null || rankingScore > bestCandidate.rankingScore) {
                candidate.weather = weather;
                candidate.matchScore = matchScore;
                candidate.rankingScore = rankingScore;
                bestCandidate = candidate;
            }
        }

        if (bestCandidate == null) return null;
        return new RecommendationResult(bestCandidate.parcours, bestCandidate.distanceKm, normalizedPreference, bestCandidate.matchScore > 0, bestCandidate.weather);
    }

    private String normalizePreferredWeather(String preferredWeather) {
        if (preferredWeather == null) return WEATHER_ANY;
        String normalized = preferredWeather.trim().toLowerCase();
        List<String> allowed = Arrays.asList("any", "clear", "cloudy", "rain", "snow", "windy");
        return allowed.contains(normalized) ? normalized : WEATHER_ANY;
    }

    private int getWeatherMatchScore(String preferredWeather, WeatherInfo weather) {
        if (weather == null) return 0;
        String condition = weather.condition != null ? weather.condition.toLowerCase() : "";
        double windSpeed = weather.windSpeed;

        switch (preferredWeather) {
            case "clear": return condition.contains("clear") ? 2 : 0;
            case "cloudy": return (condition.contains("cloud") || condition.contains("fog")) ? 2 : 0;
            case "rain": return (condition.contains("rain") || condition.contains("drizzle") || condition.contains("thunderstorm")) ? 2 : 0;
            case "snow": return condition.contains("snow") ? 2 : 0;
            case "windy":
                if (windSpeed >= 25.0) return 2;
                if (windSpeed >= 18.0) return 1;
                return 0;
            default: return 0;
        }
    }

    private double calculateDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        final double earthRadiusKm = 6371.0;
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double deltaLat = Math.toRadians(lat2 - lat1);
        double deltaLng = Math.toRadians(lon2 - lon1);
        double a = Math.pow(Math.sin(deltaLat / 2), 2) + Math.cos(lat1Rad) * Math.cos(lat2Rad) * Math.pow(Math.sin(deltaLng / 2), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadiusKm * c;
    }



    public interface WeatherServiceInterface {
        WeatherInfo getCurrentWeatherForCoordinates(double lat, double lon, String locationName);
    }

    public static class WeatherInfo {
        public String condition;
        public double windSpeed;

        public WeatherInfo(String condition, double windSpeed) {
            this.condition = condition;
            this.windSpeed = windSpeed;
        }
    }

    private static class Candidate {
        parcours_de_sante parcours;
        double distanceKm;
        WeatherInfo weather;
        int matchScore;
        double rankingScore;

        Candidate(parcours_de_sante parcours, double distanceKm) {
            this.parcours = parcours;
            this.distanceKm = distanceKm;
            this.rankingScore = Double.NEGATIVE_INFINITY;
        }
    }

    public static class RecommendationResult {
        public parcours_de_sante parcours;
        public double distanceKm;
        public String preferredWeather;
        public boolean weatherMatch;
        public WeatherInfo weather;

        public RecommendationResult(parcours_de_sante parcours, double distanceKm,
                                    String preferredWeather, boolean weatherMatch, WeatherInfo weather) {
            this.parcours = parcours;
            this.distanceKm = distanceKm;
            this.preferredWeather = preferredWeather;
            this.weatherMatch = weatherMatch;
            this.weather = weather;
        }
    }
}