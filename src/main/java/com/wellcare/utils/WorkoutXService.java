package com.wellcare.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.wellcare.dao.ExerciseDAO;
import com.wellcare.models.Exercise;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class WorkoutXService {

    private static final String API_KEY = "wx_7e037b3f7649e593a01eae63ca3a617d12a8f4d10c64f8dec0514fe0";
    private static final String BASE_URL = "https://api.workoutxapp.com/v1/exercises";
    private static final String CACHE_DIR = "cache";
    private static final String EXERCISES_CACHE_FILE = "cache/exercises_cache.json";
    private static final String GIF_CACHE_DIR = "uploads/gifs/";

    private ExerciseDAO exerciseDAO = new ExerciseDAO();
    private Set<String> existingExerciseNames = null;
    private Map<String, String> gifCache = new HashMap<>();
    private ObjectMapper mapper = new ObjectMapper();

    /**
     * Vérifie si l'API est disponible
     */
    public boolean isAPIAvailable() {
        try {
            String url = BASE_URL + "?offset=0&limit=1";
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("X-WorkoutX-Key", API_KEY)
                    .timeout(java.time.Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Récupère tous les exercices depuis l'API ou le cache
     */
    public List<Exercise> fetchAllExercises() {
        // 1. Essayer le cache d'abord
        List<Exercise> cachedExercises = loadFromCache();
        if (cachedExercises != null && !cachedExercises.isEmpty()) {
            System.out.println("📦 Utilisation du cache: " + cachedExercises.size() + " exercices");
            return cachedExercises;
        }

        // 2. Vérifier si l'API est disponible
        if (!isAPIAvailable()) {
            System.err.println("⚠️ API indisponible, utilisation des exercices déjà importés");
            return exerciseDAO.getAllExercises();
        }

        // 3. Importer depuis l'API
        List<Exercise> exercises = fetchAllExercisesWithSmartRetry();

        // 4. Sauvegarder dans le cache
        if (exercises != null && !exercises.isEmpty()) {
            saveToCache(exercises);
        }

        return exercises != null ? exercises : new ArrayList<>();
    }

    /**
     * Version intelligente avec backoff exponentiel et retry limité
     */
    private List<Exercise> fetchAllExercisesWithSmartRetry() {
        List<Exercise> allExercises = new ArrayList<>();
        int offset = 0;
        int maxPages = 30;
        int retryCount = 0;
        int waitTimeSeconds = 2;
        int consecutiveErrors = 0;

        System.out.println("\n📥 RÉCUPÉRATION DES EXERCICES DEPUIS L'API");

        for (int page = 0; page < maxPages; page++) {
            try {
                // Attendre avant chaque requête
                if (page > 0 || retryCount > 0) {
                    System.out.println("⏳ Attente " + waitTimeSeconds + " secondes...");
                    Thread.sleep(waitTimeSeconds * 1000L);
                }

                String url = BASE_URL + "?offset=" + offset;
                System.out.println("📄 Page " + (page + 1) + " (offset: " + offset + ")");

                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("X-WorkoutX-Key", API_KEY)
                        .header("Accept", "application/json")
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    // Succès - réinitialiser les compteurs
                    retryCount = 0;
                    waitTimeSeconds = 2;
                    consecutiveErrors = 0;

                    JsonNode jsonResponse = mapper.readTree(response.body());
                    JsonNode dataArray = jsonResponse.path("data");
                    int received = dataArray.size();

                    System.out.println("   ✅ Reçu: " + received + " exercices");

                    if (received == 0) {
                        consecutiveErrors++;
                        if (consecutiveErrors >= 2) {
                            System.out.println("   🏁 Fin de la pagination");
                            break;
                        }
                    } else {
                        for (JsonNode node : dataArray) {
                            Exercise exercise = convertToExercise(node);
                            if (exercise != null) {
                                allExercises.add(exercise);
                            }
                        }
                        System.out.println("   📊 Total: " + allExercises.size() + " exercices");
                    }

                    offset += 10;

                } else if (response.statusCode() == 429) {
                    // Rate limit - backoff exponentiel
                    retryCount++;
                    waitTimeSeconds = Math.min(waitTimeSeconds * 2, 60);

                    System.err.println("   ⚠️ Rate limit! Tentative " + retryCount + "/5 - Attente " + waitTimeSeconds + "s");

                    if (retryCount >= 5) {
                        System.err.println("   ❌ Trop de tentatives, arrêt de l'import");
                        break;
                    }

                    page--; // Réessayer cette page
                    continue;

                } else {
                    System.err.println("   ❌ Erreur HTTP: " + response.statusCode());
                    break;
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("   ⚠️ Interruption");
                break;
            } catch (Exception e) {
                System.err.println("   ❌ Exception: " + e.getMessage());
                break;
            }
        }

        System.out.println("\n✅ TOTAL: " + allExercises.size() + " exercices récupérés");
        return allExercises;
    }

    /**
     * Import intelligent - seulement les nouveaux exercices
     */
    public int importNewExercises() {
        loadExistingExerciseNames();
        System.out.println("\n📥 RECHERCHE DES NOUVEAUX EXERCICES");

        List<Exercise> allExercises = fetchAllExercises();

        if (allExercises.isEmpty()) {
            System.out.println("⚠️ Aucun exercice disponible");
            return 0;
        }

        // Filtrer les exercices existants
        List<Exercise> newExercises = allExercises.stream()
                .filter(ex -> !existingExerciseNames.contains(ex.getName().toLowerCase().trim()))
                .collect(Collectors.toList());

        System.out.println("📊 " + newExercises.size() + " nouveaux exercices sur " + allExercises.size());

        if (newExercises.isEmpty()) {
            System.out.println("✅ Base de données déjà à jour!");
            return 0;
        }

        // Importer avec pause entre chaque
        int count = 0;
        for (int i = 0; i < newExercises.size(); i++) {
            Exercise ex = newExercises.get(i);
            try {
                exerciseDAO.addExercise(ex);
                count++;
                existingExerciseNames.add(ex.getName().toLowerCase().trim());

                if ((i + 1) % 10 == 0) {
                    System.out.println("📊 Importé: " + (i + 1) + "/" + newExercises.size());
                    Thread.sleep(1000); // Pause toutes les 10 insertions
                }
            } catch (Exception e) {
                System.err.println("❌ Erreur import " + ex.getName() + ": " + e.getMessage());
            }
        }

        System.out.println("✅ " + count + " nouveaux exercices importés!");
        return count;
    }

    /**
     * Import avec limite (pour test)
     */
    public int importExercises(int limit) {
        loadExistingExerciseNames();

        List<Exercise> allExercises = fetchAllExercises();

        List<Exercise> newExercises = allExercises.stream()
                .filter(ex -> !existingExerciseNames.contains(ex.getName().toLowerCase().trim()))
                .limit(limit)
                .collect(Collectors.toList());

        int count = 0;
        for (Exercise ex : newExercises) {
            try {
                exerciseDAO.addExercise(ex);
                count++;
                existingExerciseNames.add(ex.getName().toLowerCase().trim());
            } catch (Exception e) {
                System.err.println("❌ Erreur: " + ex.getName());
            }
        }

        System.out.println("✅ " + count + " exercices importés");
        return count;
    }

    /**
     * Télécharge un GIF avec nom unique et cache
     */
    private String downloadGifLocally(String gifUrl, String exerciseName) {
        if (gifUrl == null || gifUrl.isEmpty()) return null;

        // Vérifier le cache mémoire
        if (gifCache.containsKey(gifUrl)) {
            return gifCache.get(gifUrl);
        }

        try {
            // Créer un nom unique basé sur l'exercice et l'URL
            String safeName = exerciseName.toLowerCase()
                    .replaceAll("[^a-z0-9]", "_")
                    .replaceAll("_+", "_")
                    .substring(0, Math.min(50, exerciseName.length()));

            String urlHash = Integer.toHexString(gifUrl.hashCode());
            String fileName = safeName + "_" + urlHash + ".gif";

            Path gifDir = Paths.get(GIF_CACHE_DIR);
            Path localPath = gifDir.resolve(fileName);

            // Vérifier si déjà téléchargé
            if (Files.exists(localPath)) {
                String path = localPath.toUri().toString();
                gifCache.put(gifUrl, path);
                return path;
            }

            // Créer le dossier
            if (!Files.exists(gifDir)) {
                Files.createDirectories(gifDir);
            }

            // Télécharger avec retry
            for (int attempt = 1; attempt <= 3; attempt++) {
                try {
                    Thread.sleep(100 * attempt);

                    HttpClient client = HttpClient.newHttpClient();
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(gifUrl))
                            .header("X-WorkoutX-Key", API_KEY)
                            .build();

                    HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

                    if (response.statusCode() == 200) {
                        Files.copy(response.body(), localPath, StandardCopyOption.REPLACE_EXISTING);
                        String path = localPath.toUri().toString();
                        gifCache.put(gifUrl, path);
                        return path;
                    } else if (response.statusCode() == 429) {
                        Thread.sleep(2000 * attempt);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            return null;

        } catch (Exception e) {
            System.err.println("❌ Erreur GIF pour " + exerciseName);
            return null;
        }
    }

    /**
     * Convertit un noeud JSON en objet Exercise
     */
    private Exercise convertToExercise(JsonNode node) {
        try {
            String name = node.path("name").asText();
            if (name == null || name.isEmpty()) return null;

            String bodyPart = node.path("bodyPart").asText("general");
            String gifUrl = node.path("gifUrl").asText(null);

            String localGifPath = null;
            if (gifUrl != null && !gifUrl.isEmpty()) {
                localGifPath = downloadGifLocally(gifUrl, name);
            }

            double caloriesPerMinute = node.path("caloriesPerMinute").asDouble(4.0);
            String difficulty = node.path("difficulty").asText("intermediate");
            String description = buildDescription(node, bodyPart);
            String category = mapToWelloraCategory(bodyPart);

            Exercise exercise = new Exercise();
            exercise.setName(name);
            exercise.setDescription(description);
            exercise.setCategory(category);
            exercise.setDifficultyLevel(mapDifficulty(difficulty));
            exercise.setDefaultUnit("minutes");
            exercise.setVideoUrl(localGifPath != null ? localGifPath : "");
            exercise.setDuration(15);
            exercise.setSets(3);
            exercise.setReps(10);
            exercise.setActive(true);
            exercise.setCreatedAt(LocalDateTime.now());

            return exercise;

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Sauvegarde les exercices dans le cache
     */
    private void saveToCache(List<Exercise> exercises) {
        try {
            Path cacheDir = Paths.get(CACHE_DIR);
            if (!Files.exists(cacheDir)) {
                Files.createDirectories(cacheDir);
            }

            mapper.writeValue(Paths.get(EXERCISES_CACHE_FILE).toFile(), exercises);
            System.out.println("💾 Cache sauvegardé: " + exercises.size() + " exercices");
        } catch (Exception e) {
            System.err.println("⚠️ Impossible de sauvegarder le cache: " + e.getMessage());
        }
    }

    /**
     * Charge les exercices depuis le cache
     */
    private List<Exercise> loadFromCache() {
        try {
            Path cacheFile = Paths.get(EXERCISES_CACHE_FILE);
            if (Files.exists(cacheFile)) {
                List<Exercise> exercises = mapper.readValue(cacheFile.toFile(),
                        new TypeReference<List<Exercise>>() {});
                System.out.println("📀 Cache chargé: " + exercises.size() + " exercices");
                return exercises;
            }
        } catch (Exception e) {
            System.err.println("⚠️ Erreur lecture cache: " + e.getMessage());
        }
        return null;
    }

    /**
     * Charge les noms des exercices existants en base
     */
    private void loadExistingExerciseNames() {
        if (existingExerciseNames == null) {
            existingExerciseNames = new HashSet<>();
            List<Exercise> exercises = exerciseDAO.getAllExercises();
            for (Exercise ex : exercises) {
                if (ex.getName() != null) {
                    existingExerciseNames.add(ex.getName().toLowerCase().trim());
                }
            }
            System.out.println("📊 Base de données: " + existingExerciseNames.size() + " exercices");
        }
    }

    /**
     * Construit la description de l'exercice
     */
    private String buildDescription(JsonNode node, String bodyPart) {
        StringBuilder desc = new StringBuilder();
        JsonNode instrArray = node.path("instructions");

        if (instrArray.isArray() && instrArray.size() > 0) {
            int count = 0;
            for (JsonNode instr : instrArray) {
                if (count >= 2) break;
                String line = instr.asText();
                if (line.length() > 100) line = line.substring(0, 97) + "...";
                desc.append("• ").append(line).append("\n");
                count++;
            }
        } else {
            desc.append("• Exercice pour ").append(bodyPart).append("\n");
        }

        String result = desc.toString();
        if (result.length() > 200) result = result.substring(0, 197) + "...";
        return result;
    }

    /**
     * Mappe la partie du corps vers la catégorie Wellora
     */
    private String mapToWelloraCategory(String bodyPart) {
        if (bodyPart == null) return "Cardio";
        switch (bodyPart.toLowerCase()) {
            case "chest":
            case "back":
            case "shoulders":
            case "biceps":
            case "triceps":
            case "upper legs":
            case "lower legs":
            case "quadriceps":
            case "hamstrings":
            case "glutes":
                return "Musculation";
            default:
                return "Cardio";
        }
    }

    /**
     * Mappe la difficulté
     */
    private String mapDifficulty(String difficulty) {
        if (difficulty == null) return "Intermediate";
        switch (difficulty.toLowerCase()) {
            case "beginner":
                return "Beginner";
            case "intermediate":
                return "Intermediate";
            case "advanced":
                return "Advanced";
            default:
                return "Intermediate";
        }
    }

    /**
     * Retourne le nombre d'exercices en base
     */
    public int getLocalExerciseCount() {
        return exerciseDAO.getAllExercises().size();
    }
}