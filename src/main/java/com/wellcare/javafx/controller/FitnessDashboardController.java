package com.wellcare.javafx.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import com.wellcare.javafx.dao.DailyPlanDAO;
import com.wellcare.javafx.dao.ExerciseDAO;
import com.wellcare.javafx.dao.GoalDaoImpl;
import com.wellcare.javafx.dao.IGoalDao;
import com.wellcare.javafx.model.DailyPlan;
import com.wellcare.javafx.model.Exercise;
import com.wellcare.javafx.model.Goal;
import com.wellcare.javafx.util.ZenQuotesService;
import com.wellcare.javafx.util.ZenQuotesService.QuoteResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

public class FitnessDashboardController {

    // ==================== FXML COMPOSANTS ====================
    @FXML private Label lblWebinarInfo;
    @FXML private Label lblMotivationQuote;
    @FXML private Label lblQuoteAuthor;

    // Cartes de stats
    @FXML private Label lblActiveGoals;
    @FXML private Label lblAvgProgress;
    @FXML private Label lblTotalExercises;
    @FXML private Label lblTotalCalories;

    // Carte Objectif sélectionné
    @FXML private Label lblMainGoalTitle;
    @FXML private Label lblGoalStatus;
    @FXML private Text txtGoalDescription;
    @FXML private ProgressBar goalProgressBar;
    @FXML private Label lblCurrentProgress;
    @FXML private Label lblTargetProgress;
    @FXML private Label lblGoalPercentage;
    @FXML private Label lblStartDate;
    @FXML private Label lblEndDate;
    @FXML private Label lblDaysLeft;

    // Plan du jour
    @FXML private VBox todayPlanContainer;

    // Exercices récents
    @FXML private ListView<String> recentExercisesList;

    // Catégories
    @FXML private Label lblStrengthCount;
    @FXML private Label lblCardioCount;
    @FXML private Label lblMusculationCount;

    // Sélecteur d'objectif
    @FXML private ComboBox<Goal> goalSelector;

    // ==================== VARIABLES ====================
    private IGoalDao goalDao = new GoalDaoImpl();
    private ExerciseDAO exerciseDAO = new ExerciseDAO();
    private DailyPlanDAO dailyPlanDAO = new DailyPlanDAO();
    private Goal currentSelectedGoal;
    private List<Goal> allGoals;
    private List<DailyPlan> allPlans;

    // ==================== INITIALISATION ====================
    @FXML
    public void initialize() {
        loadSportQuoteOfTheDay();  // NOUVEAU : Charge une citation sportive
        loadAllData();
        setupGoalSelector();
        
        // Auto-load exercises and stats
        loadRecentExercises();
        loadCategoryStats();

        goalSelector.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                currentSelectedGoal = newVal;
                updateAllDashboardForGoal();
            }
        });
    }

    // ==================== NOUVEAU : Citation sportive ZenQuotes ====================

    /**
     * Charge une citation sportive depuis l'API ZenQuotes
     */
    private void loadSportQuoteOfTheDay() {
        // Créer un thread séparé pour ne pas bloquer l'interface
        new Thread(() -> {
            try {
                // Appeler l'API ZenQuotes
                QuoteResponse quote = ZenQuotesService.getRandomSportQuote();

                // Mettre à jour l'interface dans le thread JavaFX
                Platform.runLater(() -> {
                    lblMotivationQuote.setText("💪 " + quote.getText());
                    lblQuoteAuthor.setText("— " + quote.getAuthor());

                    // Animation subtile pour la nouvelle citation
                    animateQuoteAppearance();
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    lblMotivationQuote.setText("💪 " + getFallbackQuote());
                    lblQuoteAuthor.setText("— Wellora Coach");
                });
            }
        }).start();
    }

    /**
     * Animation lors du changement de citation
     */
    private void animateQuoteAppearance() {
        lblMotivationQuote.setOpacity(0);
        lblQuoteAuthor.setOpacity(0);

        javafx.animation.FadeTransition ft1 = new javafx.animation.FadeTransition(javafx.util.Duration.millis(500), lblMotivationQuote);
        ft1.setFromValue(0);
        ft1.setToValue(1);
        ft1.play();

        javafx.animation.FadeTransition ft2 = new javafx.animation.FadeTransition(javafx.util.Duration.millis(500), lblQuoteAuthor);
        ft2.setFromValue(0);
        ft2.setToValue(1);
        ft2.play();
    }

    /**
     * Citation de secours
     */
    private String getFallbackQuote() {
        String[] quotes = {
                "Le sport ne construit pas le caractère, il le révèle.",
                "La seule limite est celle que tu t'imposes.",
                "Chaque entraînement est une victoire sur toi-même.",
                "La douleur est temporaire, la fierté est éternelle.",
                "Un champion est quelqu'un qui se relève quand il ne le peut pas."
        };
        return quotes[(int)(Math.random() * quotes.length)];
    }

    /**
     * Rafraîchir la citation (bouton)
     */
    @FXML
    private void refreshSportQuote() {
        loadSportQuoteOfTheDay();
    }

    // ==================== CHARGEMENT DES DONNÉES ====================

    private void loadAllData() {
        com.wellcare.javafx.model.User currentUser = com.wellcare.javafx.util.SceneManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            if ("ROLE_PATIENT".equals(currentUser.getRole())) {
                allGoals = goalDao.getGoalsByPatient(currentUser.getUuid());
            } else if ("ROLE_COACH".equals(currentUser.getRole())) {
                allGoals = goalDao.getGoalsByCoach(currentUser.getUuid());
            } else {
                allGoals = goalDao.getAllGoals();
            }
        } else {
            allGoals = goalDao.getAllGoals();
        }

        List<DailyPlan> allPlansRaw = dailyPlanDAO.getAllPlans();
        List<Integer> userGoalIds = allGoals.stream().map(Goal::getId).collect(Collectors.toList());
        allPlans = allPlansRaw.stream().filter(p -> userGoalIds.contains(p.getGoalId())).collect(Collectors.toList());
    }

    private void setupGoalSelector() {
        if (allGoals.isEmpty()) return;

        goalSelector.setItems(FXCollections.observableArrayList(allGoals));
        goalSelector.setCellFactory(lv -> new ListCell<Goal>() {
            @Override
            protected void updateItem(Goal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    int planCount = (int) allPlans.stream()
                            .filter(p -> p.getGoalId() == item.getId())
                            .count();
                    setText(item.getTitle() + " (" + item.getProgress() + "%) - " + planCount + " plans");
                }
            }
        });
        goalSelector.setButtonCell(new ListCell<Goal>() {
            @Override
            protected void updateItem(Goal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Sélectionner un objectif");
                } else {
                    setText(item.getTitle() + " (" + item.getProgress() + "%)");
                }
            }
        });

        goalSelector.setValue(allGoals.get(0));
        currentSelectedGoal = allGoals.get(0);
        updateAllDashboardForGoal();
    }

    // ==================== MISE À JOUR COMPLÈTE ====================

    private void updateAllDashboardForGoal() {
        if (currentSelectedGoal == null) return;
        updateStatsCards();
        updateGoalCard();
        loadTodayPlan();
        updateWebinarInfo();
    }

    private void updateStatsCards() {
        List<DailyPlan> goalPlans = allPlans.stream()
                .filter(p -> p.getGoalId() == currentSelectedGoal.getId())
                .collect(Collectors.toList());

        boolean isActive = !"Terminé".equals(currentSelectedGoal.getStatus());
        lblActiveGoals.setText(isActive ? "1" : "0");

        int progress = currentSelectedGoal.getProgress();
        lblAvgProgress.setText(progress + "%");

        int totalExercisesInGoal = 0;
        for (DailyPlan plan : goalPlans) {
            totalExercisesInGoal += dailyPlanDAO.getExercisesForPlan(plan.getId()).size();
        }
        lblTotalExercises.setText(String.valueOf(totalExercisesInGoal));

        int totalCalories = goalPlans.stream().mapToInt(DailyPlan::getCalories).sum();
        lblTotalCalories.setText(String.valueOf(totalCalories));
    }

    private void updateGoalCard() {
        lblMainGoalTitle.setText(currentSelectedGoal.getTitle());

        String statusText = currentSelectedGoal.getProgress() + "% - " + currentSelectedGoal.getCategory();
        lblGoalStatus.setText(statusText);

        txtGoalDescription.setText(currentSelectedGoal.getDescription() != null ?
                currentSelectedGoal.getDescription() : "Aucune description");

        int progress = currentSelectedGoal.getProgress();
        goalProgressBar.setProgress(progress / 100.0);
        lblCurrentProgress.setText(String.valueOf(progress));
        lblTargetProgress.setText("100");
        lblGoalPercentage.setText(progress + "%");

        if (progress < 30) {
            goalProgressBar.setStyle("-fx-accent: #EF4444; -fx-background-radius: 10;");
            lblGoalPercentage.setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;");
        } else if (progress < 70) {
            goalProgressBar.setStyle("-fx-accent: #F59E0B; -fx-background-radius: 10;");
            lblGoalPercentage.setStyle("-fx-text-fill: #F59E0B; -fx-font-weight: bold;");
        } else {
            goalProgressBar.setStyle("-fx-accent: #10B981; -fx-background-radius: 10;");
            lblGoalPercentage.setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold;");
        }

        if (currentSelectedGoal.getStartDate() != null) {
            lblStartDate.setText(currentSelectedGoal.getStartDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
        } else {
            lblStartDate.setText("Non définie");
        }

        if (currentSelectedGoal.getEndDate() != null) {
            lblEndDate.setText(currentSelectedGoal.getEndDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
            long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), currentSelectedGoal.getEndDate());
            lblDaysLeft.setText(daysLeft + "d");
            if (daysLeft < 7) {
                lblDaysLeft.setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold; -fx-font-size: 18px;");
            } else if (daysLeft < 30) {
                lblDaysLeft.setStyle("-fx-text-fill: #F59E0B; -fx-font-weight: bold; -fx-font-size: 18px;");
            } else {
                lblDaysLeft.setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold; -fx-font-size: 18px;");
            }
        } else {
            lblEndDate.setText("Non définie");
            lblDaysLeft.setText("-");
        }
    }

    private void updateWebinarInfo() {
        long activeCount = allGoals.stream()
                .filter(g -> !"Terminé".equals(g.getStatus()))
                .count();

        String goalInfo = "";
        if (currentSelectedGoal != null) {
            int planCount = (int) allPlans.stream()
                    .filter(p -> p.getGoalId() == currentSelectedGoal.getId())
                    .count();
            goalInfo = " | Objectif: " + currentSelectedGoal.getTitle() + " (" + planCount + " plans)";
        }


    }

    private void loadTodayPlan() {
        if (todayPlanContainer == null || currentSelectedGoal == null) return;

        todayPlanContainer.getChildren().clear();
        LocalDate today = LocalDate.now();

        List<DailyPlan> goalPlans = allPlans.stream()
                .filter(p -> p.getGoalId() == currentSelectedGoal.getId())
                .collect(Collectors.toList());

        DailyPlan todayPlan = goalPlans.stream()
                .filter(p -> p.getDate().toLocalDate().equals(today))
                .findFirst()
                .orElse(null);

        if (todayPlan != null) {
            VBox planBox = new VBox(8);
            planBox.setStyle("-fx-background-color: #F0FDFA; -fx-background-radius: 12; -fx-padding: 12;");

            Label titleLabel = new Label(todayPlan.getTitre());
            titleLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #0D9488; -fx-font-size: 14px;");

            Label infoLabel = new Label("⏱️ " + todayPlan.getDureeMin() + " min  |  🔥 " + todayPlan.getCalories() + " kcal");
            infoLabel.setStyle("-fx-text-fill: #64748B; -fx-font-size: 12px;");

            List<Exercise> exercises = dailyPlanDAO.getExercisesForPlan(todayPlan.getId());
            Label exercisesLabel = new Label("🏋️ " + exercises.size() + " exercices");
            exercisesLabel.setStyle("-fx-text-fill: #64748B; -fx-font-size: 11px;");

            Button detailsBtn = new Button("Voir détails");
            detailsBtn.setStyle("-fx-background-color: #EFF6FF; -fx-text-fill: #3B82F6; -fx-background-radius: 15; -fx-padding: 4 12; -fx-cursor: hand; -fx-font-size: 11px;");
            detailsBtn.setOnAction(e -> showPlanDetails(todayPlan));

            planBox.getChildren().addAll(titleLabel, infoLabel, exercisesLabel, detailsBtn);
            todayPlanContainer.getChildren().add(planBox);
        } else {
            Label noPlanLabel = new Label("📅 Aucun plan pour aujourd'hui");
            noPlanLabel.setStyle("-fx-text-fill: #94A3B8; -fx-font-style: italic; -fx-alignment: center;");
            noPlanLabel.setWrapText(true);

            Button createPlanBtn = new Button("➕ Créer un plan pour aujourd'hui");
            createPlanBtn.setStyle("-fx-background-color: #14b8a6; -fx-text-fill: white; -fx-background-radius: 15; -fx-padding: 8; -fx-cursor: hand; -fx-font-size: 12px;");
            createPlanBtn.setOnAction(e -> createNewPlanForToday());

            DailyPlan nextPlan = goalPlans.stream()
                    .filter(p -> p.getDate().toLocalDate().isAfter(today))
                    .findFirst()
                    .orElse(null);

            if (nextPlan != null) {
                Label nextPlanLabel = new Label("📅 Prochain plan: " + nextPlan.getDate().toLocalDate() + " - " + nextPlan.getTitre());
                nextPlanLabel.setStyle("-fx-text-fill: #64748B; -fx-font-size: 11px;");
                todayPlanContainer.getChildren().addAll(noPlanLabel, nextPlanLabel, createPlanBtn);
            } else {
                todayPlanContainer.getChildren().addAll(noPlanLabel, createPlanBtn);
            }
        }
    }

    private void loadRecentExercises() {
        recentExercisesList.getItems().clear();
        List<Exercise> exercises = exerciseDAO.getAllExercises();

        exercises.stream()
                .limit(5)
                .forEach(ex -> {
                    String item = ex.getName() + " - " + ex.getCategory();
                    recentExercisesList.getItems().add(item);
                });
    }

    private void loadCategoryStats() {
        List<Exercise> exercises = exerciseDAO.getAllExercises();

        long strengthCount = exercises.stream()
                .filter(e -> "Musculation".equals(e.getCategory()))
                .count();
        long cardioCount = exercises.stream()
                .filter(e -> "Cardio".equals(e.getCategory()))
                .count();
        long musculationCount = exercises.stream()
                .filter(e -> "Yoga".equals(e.getCategory()) || "Étirements".equals(e.getCategory()))
                .count();

        lblStrengthCount.setText("💪 Strength (" + strengthCount + ")");
        lblCardioCount.setText("🏃 Cardio (" + cardioCount + ")");
        lblMusculationCount.setText("🧘 Wellness (" + musculationCount + ")");
    }

    // ==================== ACTIONS ====================

    @FXML
    private void refreshDashboard() {
        loadAllData();
        setupGoalSelector();
        loadRecentExercises();
        loadCategoryStats();
        loadSportQuoteOfTheDay();  // Rafraîchir aussi la citation

        if (currentSelectedGoal != null) {
            currentSelectedGoal = allGoals.stream()
                    .filter(g -> g.getId() == currentSelectedGoal.getId())
                    .findFirst()
                    .orElse(currentSelectedGoal);
            updateAllDashboardForGoal();
        }

        showSuccessMessage("Dashboard actualisé");
    }

    @FXML
    private void editSelectedGoal() {
        if (currentSelectedGoal != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/GoalView.fxml"));
                Node view = loader.load();
                GoalController controller = loader.getController();
                controller.openEditForm(currentSelectedGoal);
                refreshDashboard();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            showError("Aucun objectif sélectionné");
        }
    }

    @FXML
    private void updateSelectedGoalProgress() {
        if (currentSelectedGoal == null) {
            showError("Aucun objectif sélectionné");
            return;
        }

        TextInputDialog dialog = new TextInputDialog(String.valueOf(currentSelectedGoal.getProgress()));
        dialog.setTitle("Mise à jour progression");
        dialog.setHeaderText("Mettre à jour la progression pour: " + currentSelectedGoal.getTitle());
        dialog.setContentText("Progression (%):");

        dialog.showAndWait().ifPresent(value -> {
            try {
                int newProgress = Integer.parseInt(value);
                if (newProgress >= 0 && newProgress <= 100) {
                    currentSelectedGoal.setProgress(newProgress);
                    goalDao.updateGoal(currentSelectedGoal);
                    updateAllDashboardForGoal();

                    loadAllData();
                    goalSelector.setItems(FXCollections.observableArrayList(allGoals));
                    goalSelector.setValue(currentSelectedGoal);

                    showSuccessMessage("Progression mise à jour: " + newProgress + "%");
                } else {
                    showError("La progression doit être entre 0 et 100");
                }
            } catch (NumberFormatException e) {
                showError("Veuillez entrer un nombre valide");
            }
        });
    }

    @FXML
    private void createNewPlan() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DailyPlanView.fxml"));
            Node view = loader.load();
            DailyPlanController controller = loader.getController();
            controller.openAddForm();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createNewPlanForToday() {
        if (currentSelectedGoal == null) {
            showError("Aucun objectif sélectionné");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DailyPlanView.fxml"));
            Node view = loader.load();
            DailyPlanController controller = loader.getController();
            controller.openAddForm();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void searchExercise() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ExerciseLibrary.fxml"));
            loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showPlanDetails(DailyPlan plan) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails du plan");
        alert.setHeaderText(plan.getTitre());

        List<Exercise> exercises = dailyPlanDAO.getExercisesForPlan(plan.getId());
        StringBuilder content = new StringBuilder();
        content.append("📅 Date: ").append(plan.getDate()).append("\n");
        content.append("⏱️ Durée: ").append(plan.getDureeMin()).append(" minutes\n");
        content.append("🔥 Calories: ").append(plan.getCalories()).append(" kcal\n");
        content.append("📝 Notes: ").append(plan.getNotes() != null ? plan.getNotes() : "Aucune").append("\n\n");
        content.append("🏋️ Exercices:\n");
        for (Exercise ex : exercises) {
            content.append("  • ").append(ex.getName()).append("\n");
        }

        alert.setContentText(content.toString());
        alert.showAndWait();
    }

    private void showSuccessMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}