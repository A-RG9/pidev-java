package org.example.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import org.example.dao.DailyPlanDAO;
import org.example.dao.ExerciseDAO;
import org.example.dao.GoalDaoImpl;
import org.example.models.DailyPlan;
import org.example.models.Exercise;
import org.example.models.Goal;
import org.example.utils.AIService;
import org.example.utils.Database;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class AICoachController {

    @FXML private TextArea txtUserRequest;
    @FXML private TextArea txtResponse;
    @FXML private ProgressBar progressBar;
    @FXML private Button btnAnalyze;
    @FXML private Button btnGenerateProgram;
    @FXML private Label lblStatus;
    @FXML private FlowPane suggestionsContainer;

    private AIService aiService;
    private GoalDaoImpl goalDao;
    private DailyPlanDAO dailyPlanDAO;
    private ExerciseDAO exerciseDAO;
    private Map<String, Object> lastAnalysis;

    // IDs
    private String currentPatientId = null;
    private static final String AI_COACH_ID = "ai";

    @FXML
    public void initialize() {
        aiService = new AIService();
        goalDao = new GoalDaoImpl();
        dailyPlanDAO = new DailyPlanDAO();
        exerciseDAO = new ExerciseDAO();

        // Récupérer l'ID du patient depuis la base
        loadCurrentPatientId();

        txtUserRequest.setPromptText("Exemple: Je veux perdre 5 kilos en 2 mois, je suis débutant");

        addExampleSuggestions();

        // Test connexion IA
        new Thread(() -> {
            boolean connected = aiService.testConnection();
            Platform.runLater(() -> {
                if (connected) {
                    lblStatus.setText("✅ IA Coach connectée");
                    lblStatus.setStyle("-fx-text-fill: #10B981;");
                } else {
                    lblStatus.setText("⚠️ Mode hors ligne (analyse locale)");
                    lblStatus.setStyle("-fx-text-fill: #F59E0B;");
                }
            });
        }).start();
    }

    private void loadCurrentPatientId() {
        try (Connection conn = Database.getConnection()) {
            String sql = "SELECT uuid FROM users WHERE role = 'ROLE_PATIENT' LIMIT 1";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                currentPatientId = rs.getString("uuid");
                System.out.println("✅ Patient ID chargé: " + currentPatientId);
            } else {
                currentPatientId = "c513e200-d605-4f61-9efc-d539f0e80914";
                System.out.println("⚠️ Utilisation du patient par défaut: " + currentPatientId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            currentPatientId = "c513e200-d605-4f61-9efc-d539f0e80914";
        }
    }

    private void addExampleSuggestions() {
        String[] examples = {
                "Je veux perdre du poids, je suis débutant, 3 mois",
                "Objectif prise de muscle, niveau intermédiaire, 8 semaines",
                "Améliorer mon endurance pour courir un 10km",
                "Je veux devenir plus flexible, faire du yoga"
        };

        for (String example : examples) {
            Button btn = new Button(example);
            btn.setStyle("-fx-background-color: #F1F5F9; -fx-text-fill: #475569; -fx-padding: 8 15; -fx-background-radius: 20; -fx-cursor: hand;");
            btn.setOnAction(e -> txtUserRequest.setText(example));
            suggestionsContainer.getChildren().add(btn);
        }
    }

    @FXML
    private void analyzeRequest() {
        if (currentPatientId == null) {
            loadCurrentPatientId();
        }

        String userRequest = txtUserRequest.getText().trim();
        if (userRequest.isEmpty()) {
            showError("Veuillez décrire votre objectif");
            return;
        }

        progressBar.setVisible(true);
        btnAnalyze.setDisable(true);
        txtResponse.setText("🤔 Analyse de votre demande...\n");

        new Thread(() -> {
            Map<String, Object> result = aiService.analyzeRequest(userRequest);
            Platform.runLater(() -> {
                progressBar.setVisible(false);
                btnAnalyze.setDisable(false);

                if (result != null && Boolean.TRUE.equals(result.get("success"))) {
                    Object analysisObj = result.get("analysis");
                    Map<String, Object> analysis = new HashMap<>();

                    if (analysisObj instanceof Map) {
                        analysis = (Map<String, Object>) analysisObj;
                    } else {
                        try {
                            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                            String json = mapper.writeValueAsString(analysisObj);
                            analysis = mapper.readValue(json, Map.class);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    lastAnalysis = analysis;
                    displayAnalysis(analysis);
                    btnGenerateProgram.setDisable(false);
                } else {
                    txtResponse.setText("❌ Erreur lors de l'analyse. Veuillez réessayer.");
                }
            });
        }).start();
    }

    private void displayAnalysis(Map<String, Object> analysis) {
        StringBuilder sb = new StringBuilder();
        sb.append("📊 ANALYSE DE VOTRE DEMANDE\n\n");
        sb.append("🎯 Objectif: ").append(analysis.get("title")).append("\n");
        sb.append("📝 Description: ").append(analysis.get("description")).append("\n");
        sb.append("🏷️ Catégorie: ").append(analysis.get("category")).append("\n");
        sb.append("⭐ Niveau: ").append(analysis.get("difficultyLevel")).append("\n");
        sb.append("📅 Durée: ").append(analysis.get("durationWeeks")).append(" semaines\n");
        sb.append("🏋️ Séances/semaine: ").append(analysis.get("sessionsPerWeek")).append("\n\n");
        sb.append("✅ L'analyse est correcte ? Cliquez sur 'Générer le programme'.");

        txtResponse.setText(sb.toString());
    }

    @FXML
    private void generateProgram() {
        if (lastAnalysis == null) {
            showError("Veuillez d'abord analyser votre demande");
            return;
        }

        if (currentPatientId == null) {
            showError("Aucun patient trouvé.");
            return;
        }

        progressBar.setVisible(true);
        btnGenerateProgram.setDisable(true);
        txtResponse.appendText("\n\n🔄 Génération du programme en cours...\n");

        new Thread(() -> {
            String userRequest = txtUserRequest.getText().trim();
            Map<String, Object> result = aiService.generateProgram(userRequest);

            Platform.runLater(() -> {
                progressBar.setVisible(false);
                btnGenerateProgram.setDisable(false);

                if (result != null && Boolean.TRUE.equals(result.get("success"))) {
                    saveProgramToDatabase(result);
                } else {
                    txtResponse.appendText("\n❌ Erreur lors de la génération du programme.");
                    showError("Erreur lors de la génération du programme");
                }
            });
        }).start();
    }

    private void saveProgramToDatabase(Map<String, Object> result) {
        try {
            // 1. Récupérer les exercices existants
            List<Exercise> existingExercises = exerciseDAO.getAllExercises();

            if (existingExercises.isEmpty()) {
                txtResponse.appendText("\n⚠️ Aucun exercice trouvé dans la base !\n");
                txtResponse.appendText("💡 Veuillez d'abord importer des exercices via 'Import WorkoutX'\n");
                return;
            }

            txtResponse.appendText("\n📦 " + existingExercises.size() + " exercices disponibles\n");

            // 2. Créer l'objectif
            Map<String, Object> goalData = new HashMap<>();

            if (lastAnalysis != null) {
                goalData.put("title", lastAnalysis.get("title"));
                goalData.put("description", lastAnalysis.get("description"));
                goalData.put("category", lastAnalysis.get("category"));
                goalData.put("difficultyLevel", lastAnalysis.get("difficultyLevel"));
                goalData.put("durationWeeks", lastAnalysis.get("durationWeeks"));
            } else {
                goalData.put("title", "Programme personnalisé");
                goalData.put("description", "Programme adapté à votre objectif");
                goalData.put("category", "General");
                goalData.put("difficultyLevel", "Intermediate");
                goalData.put("durationWeeks", 8);
            }

            Goal goal = new Goal();
            goal.setTitle((String) goalData.getOrDefault("title", "Programme personnalisé"));
            goal.setDescription((String) goalData.getOrDefault("description", "Programme adapté à votre objectif"));
            goal.setCategory((String) goalData.getOrDefault("category", "General"));
            goal.setStatus("PENDING");
            goal.setStartDate(LocalDate.now());
            goal.setPatientId(currentPatientId);
            goal.setCoachId(AI_COACH_ID);

            int weeks = 8;
            Object weeksObj = goalData.get("durationWeeks");
            if (weeksObj instanceof Integer) {
                weeks = (Integer) weeksObj;
            }

            goal.setEndDate(LocalDate.now().plusWeeks(weeks));
            goal.setDifficultyLevel((String) goalData.getOrDefault("difficultyLevel", "Intermediate"));
            goal.setProgress(0);

            goalDao.addGoal(goal);
            txtResponse.appendText("✅ Objectif créé: " + goal.getTitle() + "\n");

            // Récupérer l'ID du goal
            List<Goal> goals = goalDao.getAllGoals();
            int goalId = goals.get(goals.size() - 1).getId();

            // 3. Paramètres du programme
            int sessionsPerWeek = 3;
            if (lastAnalysis != null && lastAnalysis.get("sessionsPerWeek") instanceof Integer) {
                sessionsPerWeek = (Integer) lastAnalysis.get("sessionsPerWeek");
            }

            String category = (String) goalData.getOrDefault("category", "General");
            String difficulty = (String) goalData.getOrDefault("difficultyLevel", "Intermediate");

            // 4. Filtrer les exercices par catégorie
            List<Exercise> filteredExercises = existingExercises.stream()
                    .filter(e -> e.getCategory() != null && !e.getCategory().isEmpty())
                    .collect(Collectors.toList());

            // Filtrer par catégorie si possible
            List<Exercise> categoryFiltered = filteredExercises.stream()
                    .filter(e -> e.getCategory().equalsIgnoreCase(category))
                    .collect(Collectors.toList());

            if (!categoryFiltered.isEmpty()) {
                filteredExercises = categoryFiltered;
            }

            // Filtrer par difficulté
            List<Exercise> difficultyFiltered = filteredExercises.stream()
                    .filter(e -> e.getDifficultyLevel() != null && e.getDifficultyLevel().equalsIgnoreCase(difficulty))
                    .collect(Collectors.toList());

            if (!difficultyFiltered.isEmpty()) {
                filteredExercises = difficultyFiltered;
            }

            txtResponse.appendText("📊 " + filteredExercises.size() + " exercices adaptés (" + category + "/" + difficulty + ")\n");

            // 5. Générer les plans
            int plansSaved = 0;
            Random random = new Random();

            for (int week = 1; week <= weeks; week++) {
                for (int session = 1; session <= sessionsPerWeek; session++) {
                    DailyPlan plan = new DailyPlan();
                    plan.setTitre("Semaine " + week + " - Séance " + session);
                    plan.setDate(Date.valueOf(LocalDate.now().plusWeeks(week - 1).plusDays(session * 2)));
                    plan.setStatus("PENDING");
                    plan.setDureeMin(30);
                    plan.setCalories(200);
                    plan.setNotes("Généré par IA Coach - " + category + " - Niveau " + difficulty);
                    plan.setGoalId(goalId);

                    // Sélectionner 3-5 exercices aléatoires
                    List<Exercise> selectedExercises = new ArrayList<>();
                    int exCount = Math.min(4, filteredExercises.size());

                    // Créer une copie pour ne pas modifier l'original
                    List<Exercise> tempList = new ArrayList<>(filteredExercises);
                    for (int i = 0; i < exCount && !tempList.isEmpty(); i++) {
                        int index = random.nextInt(tempList.size());
                        selectedExercises.add(tempList.get(index));
                        tempList.remove(index);
                    }

                    dailyPlanDAO.saveDailyPlan(plan, selectedExercises);
                    plansSaved++;
                }
            }

            txtResponse.appendText("\n\n✅ PROGRAMME GÉNÉRÉ AVEC SUCCÈS !\n");
            txtResponse.appendText("📌 Objectif: " + goal.getTitle() + "\n");
            txtResponse.appendText("📋 " + plansSaved + " séances programmées\n");
            txtResponse.appendText("🏋️ Chaque séance contient des exercices adaptés\n");
            txtResponse.appendText("\n👉 Retrouvez votre programme dans 'Mes Objectifs' et 'Mes Plans'");

            showSuccessMessage("Programme généré avec succès ! " + plansSaved + " séances ont été créées.");

        } catch (Exception e) {
            txtResponse.appendText("\n❌ Erreur: " + e.getMessage());
            e.printStackTrace();
            showError("Erreur: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccessMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}