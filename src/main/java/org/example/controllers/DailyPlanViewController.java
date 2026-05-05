package org.example.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import org.example.dao.DailyPlanDAO;
import org.example.dao.GoalDaoImpl;
import org.example.dao.IGoalDao;
import org.example.models.DailyPlan;
import org.example.models.Exercise;
import org.example.models.Goal;
import org.example.utils.GiphyService;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DailyPlanViewController {

    @FXML private ComboBox<Goal> goalSelector;
    @FXML private Label lblTodayPlansCount;
    @FXML private Label lblCompletedCount;
    @FXML private Label lblProgressPercent;
    @FXML private ProgressBar progressBar;
    @FXML private ListView<DailyPlan> plansListView;
    @FXML private VBox videoSection;
    @FXML private WebView videoPlayer;
    @FXML private WebView gifPlayer;
    @FXML private VBox rewardSection;
    @FXML private Label videoTitleLabel;
    @FXML private HBox videoControlsBar;

    private DailyPlanDAO dailyPlanDAO = new DailyPlanDAO();
    private IGoalDao goalDao = new GoalDaoImpl();
    private List<DailyPlan> allGoalPlans = new ArrayList<>();
    private List<Integer> completedPlanIds = new ArrayList<>();
    private DailyPlan currentSelectedPlan;
    private Goal currentGoal;
    private boolean isRefreshing = false;
    private List<Exercise> currentExercises = new ArrayList<>();
    private int currentVideoIndex = 0;

    @FXML
    public void initialize() {
        loadGoals();
        setupModernPlanList();
        setupProgressBar();
        setupVideoControls();

        videoPlayer.setContextMenuEnabled(false);
        gifPlayer.setContextMenuEnabled(false);
    }

    private void setupVideoControls() {
        if (videoControlsBar != null) {
            Button prevButton = new Button("◀ Exercice précédent");
            prevButton.setStyle("-fx-background-color: #3B82F6; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 8 16; -fx-cursor: hand;");
            prevButton.setOnAction(e -> navigateVideo(-1));

            Button nextButton = new Button("Exercice suivant ▶");
            nextButton.setStyle("-fx-background-color: #3B82F6; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 8 16; -fx-cursor: hand;");
            nextButton.setOnAction(e -> navigateVideo(1));

            videoControlsBar.getChildren().addAll(prevButton, nextButton);
        }
    }

    private void navigateVideo(int direction) {
        if (currentExercises.isEmpty()) return;

        int newIndex = currentVideoIndex + direction;
        if (newIndex >= 0 && newIndex < currentExercises.size()) {
            currentVideoIndex = newIndex;
            displayExerciseVideo(currentExercises.get(currentVideoIndex));
        }
    }

    private void setupProgressBar() {
        progressBar.setStyle("-fx-accent: #8B5CF6; -fx-background-color: #E2E8F0; -fx-background-radius: 8;");
        progressBar.setMinHeight(15);
        progressBar.setMaxHeight(15);
    }

    private void loadGoals() {
        List<Goal> goals = goalDao.getAllGoals();

        goalSelector.setCellFactory(lv -> new ListCell<Goal>() {
            @Override
            protected void updateItem(Goal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    int planCount = (int) dailyPlanDAO.getAllPlans().stream()
                            .filter(p -> p.getGoalId() == item.getId())
                            .count();
                    int completedCount = (int) dailyPlanDAO.getAllPlans().stream()
                            .filter(p -> p.getGoalId() == item.getId() && "COMPLETED".equals(p.getStatus()))
                            .count();
                    setText(item.getTitle() + " (" + completedCount + "/" + planCount + " complétés)");
                }
            }
        });

        goalSelector.setButtonCell(new ListCell<Goal>() {
            @Override
            protected void updateItem(Goal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Sélectionnez un objectif");
                } else {
                    setText(item.getTitle());
                }
            }
        });

        goalSelector.setItems(FXCollections.observableArrayList(goals));

        if (!goals.isEmpty()) {
            goalSelector.setValue(goals.get(0));
            currentGoal = goals.get(0);
            loadPlansForGoal(goals.get(0));
        }

        goalSelector.setOnAction(e -> {
            if (!isRefreshing) {
                Goal selected = goalSelector.getValue();
                if (selected != null) {
                    currentGoal = selected;
                    loadPlansForGoal(selected);
                }
            }
        });
    }

    private void setupModernPlanList() {
        plansListView.setFixedCellSize(150);
        plansListView.setStyle("-fx-padding: 5;");

        plansListView.setCellFactory(param -> new ListCell<DailyPlan>() {
            @Override
            protected void updateItem(DailyPlan plan, boolean empty) {
                super.updateItem(plan, empty);

                if (empty || plan == null) {
                    setGraphic(null);
                    setStyle("");
                } else {
                    VBox card = createPlanCard(plan);
                    setGraphic(card);
                    setStyle("-fx-background-color: transparent; -fx-padding: 6 0 6 0;");
                }
            }
        });

        plansListView.setOnMouseClicked(event -> {
            DailyPlan selectedPlan = plansListView.getSelectionModel().getSelectedItem();
            if (selectedPlan != null && !completedPlanIds.contains(selectedPlan.getId())) {
                currentSelectedPlan = selectedPlan;
                currentExercises = dailyPlanDAO.getExercisesForPlan(selectedPlan.getId());
                currentVideoIndex = 0;
                showVideoForPlan(selectedPlan);
                plansListView.getSelectionModel().clearSelection();
            } else if (selectedPlan != null && completedPlanIds.contains(selectedPlan.getId())) {
                showAlert("✅ Ce plan a déjà été complété !");
            }
        });
    }

    private VBox createPlanCard(DailyPlan plan) {
        boolean isCompleted = completedPlanIds.contains(plan.getId());

        VBox card = new VBox();
        card.setSpacing(12);

        String cardStyle = String.format(
                "-fx-background-color: %s; " +
                        "-fx-background-radius: 16; " +
                        "-fx-padding: 16 20; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 3);" +
                        "-fx-cursor: hand; " +
                        "-fx-border-color: %s; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 16;",
                isCompleted ? "#F0FDF4" : "#FFFFFF",
                isCompleted ? "#D1FAE5" : "#E2E8F0"
        );
        card.setStyle(cardStyle);

        // Header avec titre et statut
        HBox headerRow = new HBox();
        headerRow.setAlignment(Pos.CENTER_LEFT);
        headerRow.setSpacing(12);
        HBox.setHgrow(headerRow, Priority.ALWAYS);

        Label iconLabel = new Label(isCompleted ? "✅" : "🏋️");
        iconLabel.setStyle("-fx-font-size: 26px;");

        Label titleLabel = new Label(plan.getTitre());
        titleLabel.setStyle(String.format(
                "-fx-font-size: 18px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: %s;",
                isCompleted ? "#059669" : "#1E293B"
        ));

        Label statusBadge = new Label(isCompleted ? " COMPLÉTÉ " : " EN COURS ");
        statusBadge.setStyle(String.format(
                "-fx-background-color: %s; " +
                        "-fx-text-fill: %s; " +
                        "-fx-font-size: 11px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 4 14; " +
                        "-fx-background-radius: 15;",
                isCompleted ? "#D1FAE5" : "#FEF3C7",
                isCompleted ? "#065F46" : "#92400E"
        ));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        headerRow.getChildren().addAll(iconLabel, titleLabel, spacer, statusBadge);

        // Détails du plan
        HBox detailsRow = new HBox();
        detailsRow.setSpacing(25);
        detailsRow.setAlignment(Pos.CENTER_LEFT);
        detailsRow.setStyle("-fx-padding: 8 0 0 38;");

        // Durée
        HBox durationBox = new HBox();
        durationBox.setAlignment(Pos.CENTER_LEFT);
        durationBox.setSpacing(6);
        Label durationIcon = new Label("⏱️");
        durationIcon.setStyle("-fx-font-size: 13px;");
        Label durationLabel = new Label(plan.getDureeMin() + " min");
        durationLabel.setStyle("-fx-text-fill: #64748B; -fx-font-size: 13px;");
        durationBox.getChildren().addAll(durationIcon, durationLabel);

        // Date
        HBox dateBox = new HBox();
        dateBox.setAlignment(Pos.CENTER_LEFT);
        dateBox.setSpacing(6);
        Label dateIcon = new Label("📅");
        dateIcon.setStyle("-fx-font-size: 13px;");
        Label dateLabel = new Label(plan.getDate().toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        dateLabel.setStyle("-fx-text-fill: #64748B; -fx-font-size: 13px;");
        dateBox.getChildren().addAll(dateIcon, dateLabel);

        // Calories
        HBox caloriesBox = new HBox();
        caloriesBox.setAlignment(Pos.CENTER_LEFT);
        caloriesBox.setSpacing(6);
        Label caloriesIcon = new Label("🔥");
        caloriesIcon.setStyle("-fx-font-size: 13px;");
        Label caloriesLabel = new Label(plan.getCalories() + " kcal");
        caloriesLabel.setStyle("-fx-text-fill: #64748B; -fx-font-size: 13px;");
        caloriesBox.getChildren().addAll(caloriesIcon, caloriesLabel);

        detailsRow.getChildren().addAll(durationBox, dateBox, caloriesBox);

        // Section des exercices
        VBox exercisesBox = new VBox();
        exercisesBox.setSpacing(8);
        exercisesBox.setStyle("-fx-padding: 12 0 0 38;");

        Label exercisesTitle = new Label("🏋️ EXERCICES");
        exercisesTitle.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #0D9488; -fx-letter-spacing: 0.5px;");
        exercisesBox.getChildren().add(exercisesTitle);

        List<Exercise> exercises = dailyPlanDAO.getExercisesForPlan(plan.getId());

        if (exercises.isEmpty()) {
            Label noExercisesLabel = new Label("Aucun exercice dans ce plan");
            noExercisesLabel.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 13px; -fx-font-style: italic;");
            exercisesBox.getChildren().add(noExercisesLabel);
        } else {
            for (int i = 0; i < exercises.size(); i++) {
                Exercise ex = exercises.get(i);
                HBox exerciseRow = new HBox();
                exerciseRow.setAlignment(Pos.CENTER_LEFT);
                exerciseRow.setSpacing(10);
                exerciseRow.setStyle("-fx-padding: 3 0;");

                Label numberLabel = new Label(String.format("%02d", i + 1));
                numberLabel.setStyle("-fx-text-fill: #0D9488; -fx-font-size: 13px; -fx-font-weight: bold; -fx-min-width: 30;");

                Label nameLabel = new Label(ex.getName());
                nameLabel.setStyle("-fx-text-fill: #1E293B; -fx-font-size: 14px;");
                nameLabel.setMaxWidth(Double.MAX_VALUE);
                HBox.setHgrow(nameLabel, Priority.ALWAYS);

                Label videoIcon = new Label("🎬");
                videoIcon.setStyle("-fx-text-fill: #3B82F6; -fx-font-size: 14px; -fx-cursor: hand;");
                videoIcon.setOnMouseClicked(e -> {
                    currentSelectedPlan = plan;
                    currentExercises = exercises;
                    currentVideoIndex = 1;
                    showVideoForPlan(plan);
                });

                exerciseRow.getChildren().addAll(numberLabel, nameLabel, videoIcon);
                exercisesBox.getChildren().add(exerciseRow);
            }
        }

        card.getChildren().addAll(headerRow, detailsRow, exercisesBox);

        // Effet de survol
        card.setOnMouseEntered(e -> {
            if (!isCompleted) {
                card.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 16; -fx-padding: 16 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 12, 0, 0, 5); -fx-cursor: hand; -fx-border-color: #CBD5E1; -fx-border-width: 1; -fx-border-radius: 16;");
            }
        });

        card.setOnMouseExited(e -> {
            if (!isCompleted) {
                card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 16; -fx-padding: 16 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 3); -fx-cursor: hand; -fx-border-color: #E2E8F0; -fx-border-width: 1; -fx-border-radius: 16;");
            }
        });

        return card;
    }

    private void loadPlansForGoal(Goal goal) {
        List<DailyPlan> allPlans = dailyPlanDAO.getAllPlans();

        allGoalPlans = allPlans.stream()
                .filter(p -> p.getGoalId() == goal.getId())
                .collect(Collectors.toList());

        completedPlanIds = allGoalPlans.stream()
                .filter(p -> "COMPLETED".equals(p.getStatus()))
                .map(DailyPlan::getId)
                .collect(Collectors.toList());

        plansListView.setItems(FXCollections.observableArrayList(allGoalPlans));
        plansListView.refresh();

        updateGoalSelectorDisplay();
        updateStatistics();
    }

    private void updateGoalSelectorDisplay() {
        isRefreshing = true;

        Goal selectedGoal = currentGoal;
        List<Goal> updatedGoals = goalDao.getAllGoals();
        goalSelector.setItems(FXCollections.observableArrayList(updatedGoals));

        if (selectedGoal != null) {
            Goal matchingGoal = updatedGoals.stream()
                    .filter(g -> g.getId() == selectedGoal.getId())
                    .findFirst()
                    .orElse(null);
            if (matchingGoal != null) {
                goalSelector.setValue(matchingGoal);
                currentGoal = matchingGoal;
            }
        }

        isRefreshing = false;
    }

    private void updateStatistics() {
        int total = allGoalPlans.size();
        int completed = completedPlanIds.size();

        lblTodayPlansCount.setText(String.valueOf(total));
        lblCompletedCount.setText(String.valueOf(completed));

        int percent = total > 0 ? (completed * 100 / total) : 0;
        lblProgressPercent.setText(percent + "%");

        double progressValue = total > 0 ? (double) completed / total : 0;
        progressBar.setProgress(progressValue);

        String progressColor;
        if (percent < 30) {
            progressColor = "#EF4444";
        } else if (percent < 70) {
            progressColor = "#F59E0B";
        } else {
            progressColor = "#10B981";
        }
        progressBar.setStyle(String.format("-fx-accent: %s; -fx-background-color: #E2E8F0; -fx-background-radius: 8;", progressColor));

        String color;
        if (percent < 30) {
            color = "#EF4444";
        } else if (percent < 70) {
            color = "#F59E0B";
        } else {
            color = "#10B981";
        }
        lblProgressPercent.setStyle(String.format("-fx-text-fill: %s; -fx-font-size: 42px; -fx-font-weight: bold;", color));
    }

    private void showVideoForPlan(DailyPlan plan) {
        if (plan == null) {
            showPlaceholder("Plan non trouvé");
            return;
        }

        List<Exercise> exercises = dailyPlanDAO.getExercisesForPlan(plan.getId());

        if (exercises.isEmpty()) {
            showPlaceholder("Aucun exercice dans ce plan");
            return;
        }

        currentExercises = exercises;

        // Afficher la section vidéo avec toutes les vidéos
        videoSection.setVisible(true);
        videoSection.setManaged(true);
        rewardSection.setVisible(false);
        rewardSection.setManaged(false);

        // Afficher le titre du plan
        if (videoTitleLabel != null) {
            videoTitleLabel.setText("Plan: " + plan.getTitre() + " - " + exercises.size() + " exercices");
        }

        // Afficher la première vidéo ou la vidéo sélectionnée
        if (currentVideoIndex >= 0 && currentVideoIndex < exercises.size()) {
            displayExerciseVideo(exercises.get(currentVideoIndex));
        }
    }

    private void displayExerciseVideo(Exercise exercise) {
        if (exercise == null) return;

        // Afficher le titre de l'exercice courant
        if (videoTitleLabel != null) {
            videoTitleLabel.setText(exercise.getName() + " (" + (currentVideoIndex + 1) + "/" + currentExercises.size() + ")");
        }

        String videoUrl = exercise.getVideoUrl();

        if (videoUrl != null && !videoUrl.isEmpty()) {
            if (videoUrl.endsWith(".gif") || videoUrl.contains("/gifs/") || videoUrl.contains("media.giphy.com")) {
                displayGif(videoUrl);
            } else if (isYouTubeUrl(videoUrl)) {
                displayYouTube(videoUrl);
            } else {
                videoPlayer.getEngine().load(videoUrl);
            }
        } else {
            // Afficher un message pour l'exercice sans vidéo
            String noVideoHtml = "<!DOCTYPE html><html><head><style>" +
                    "body{margin:0;padding:0;background:linear-gradient(135deg,#667eea 0%,#764ba2 100%);display:flex;justify-content:center;align-items:center;min-height:450px;font-family:Arial;}" +
                    ".container{text-align:center;color:white;}" +
                    ".icon{font-size:64px;margin-bottom:15px;}" +
                    ".exercise-name{font-size:24px;font-weight:bold;margin-bottom:10px;}" +
                    ".message{font-size:16px;}" +
                    "</style></head><body>" +
                    "<div class='container'>" +
                    "<div class='icon'>🏋️</div>" +
                    "<div class='exercise-name'>" + exercise.getName() + "</div>" +
                    "<div class='message'>Aucune vidéo de démonstration disponible</div>" +
                    "<p style='margin-top:20px;'>Complétez cet exercice ! 💪</p>" +
                    "</div></body></html>";
            videoPlayer.getEngine().loadContent(noVideoHtml);
        }
    }

    private void displayGif(String gifUrl) {
        String html = "<!DOCTYPE html><html><head><style>" +
                "body{margin:0;padding:0;background:#000;display:flex;justify-content:center;align-items:center;min-height:450px;}" +
                "img{max-width:100%;max-height:450px;object-fit:contain;}" +
                "</style></head><body><img src='" + gifUrl + "'/></body></html>";
        Platform.runLater(() -> videoPlayer.getEngine().loadContent(html));
    }

    private void displayYouTube(String url) {
        String videoId = extractYouTubeId(url);
        if (videoId != null) {
            String embedUrl = "https://www.youtube.com/embed/" + videoId;
            String html = "<!DOCTYPE html><html><head><style>" +
                    "body{margin:0;padding:0;background:#000;}" +
                    "iframe{width:100%;height:450px;border:none;}" +
                    "</style></head><body><iframe src='" + embedUrl + "' allowfullscreen></iframe></body></html>";
            Platform.runLater(() -> videoPlayer.getEngine().loadContent(html));
        } else {
            showPlaceholder("URL YouTube invalide");
        }
    }

    private String extractYouTubeId(String url) {
        if (url.contains("youtube.com/watch?v=")) {
            int start = url.indexOf("v=") + 2;
            int end = url.indexOf("&", start);
            if (end == -1) end = url.length();
            return url.substring(start, end);
        } else if (url.contains("youtu.be/")) {
            int start = url.lastIndexOf("/") + 1;
            int end = url.indexOf("?", start);
            if (end == -1) end = url.length();
            return url.substring(start, end);
        } else if (url.contains("youtube.com/embed/")) {
            int start = url.indexOf("embed/") + 6;
            int end = url.indexOf("?", start);
            if (end == -1) end = url.length();
            return url.substring(start, end);
        }
        return null;
    }

    private void showPlaceholder(String message) {
        String html = "<!DOCTYPE html><html><head><style>" +
                "body{margin:0;padding:0;background:linear-gradient(135deg,#667eea 0%,#764ba2 100%);display:flex;justify-content:center;align-items:center;height:450px;font-family:Arial;}" +
                ".container{text-align:center;color:white;}" +
                ".icon{font-size:64px;margin-bottom:15px;}" +
                ".message{font-size:16px;}" +
                "</style></head><body>" +
                "<div class='container'><div class='icon'>🎬</div><div class='message'>" + message + "</div></div>" +
                "</body></html>";
        Platform.runLater(() -> videoPlayer.getEngine().loadContent(html));
    }

    @FXML
    private void completePlan() {
        if (currentSelectedPlan == null) {
            showAlert("❌ Aucun plan sélectionné !");
            return;
        }

        if (completedPlanIds.contains(currentSelectedPlan.getId())) {
            showAlert("✅ Ce plan a déjà été complété !");
            return;
        }

        try {
            completedPlanIds.add(currentSelectedPlan.getId());
            currentSelectedPlan.setStatus("COMPLETED");

            boolean updated = dailyPlanDAO.updateDailyPlanStatus(currentSelectedPlan.getId(), "COMPLETED");

            if (updated) {
                if (currentGoal != null) {
                    loadPlansForGoal(currentGoal);
                }
                updateStatistics();
                closeVideo();
                showRewardGif();
                showAlert("🎉 Félicitations ! Plan complété !");
            } else {
                showAlert("❌ Erreur de sauvegarde");
                if (currentGoal != null) {
                    loadPlansForGoal(currentGoal);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("❌ Erreur : " + e.getMessage());
        }
    }

    private void showRewardGif() {
        rewardSection.setVisible(true);
        rewardSection.setManaged(true);

        new Thread(() -> {
            String gifUrl = GiphyService.getCelebrationGif("workout celebration victory");
            Platform.runLater(() -> {
                String html = "<!DOCTYPE html><html><head><style>" +
                        "body{margin:0;padding:0;background:linear-gradient(135deg,#667eea 0%,#764ba2 100%);display:flex;justify-content:center;align-items:center;height:350px;font-family:Arial;}" +
                        ".container{text-align:center;color:white;}" +
                        "img{max-width:80%;max-height:250px;border-radius:20px;margin-bottom:20px;}" +
                        "h2{font-size:24px;margin:0 0 10px 0;}" +
                        "p{font-size:14px;margin:0;}" +
                        "</style></head><body>" +
                        "<div class='container'><img src='" + gifUrl + "'/><h2>🎉 FÉLICITATIONS ! 🎉</h2>" +
                        "<p>Vous avez complété votre plan ! 💪</p></div></body></html>";
                gifPlayer.getEngine().loadContent(html);
            });
        }).start();
    }

    @FXML
    private void closeVideo() {
        videoSection.setVisible(false);
        videoSection.setManaged(false);
        videoPlayer.getEngine().load(null);
        currentSelectedPlan = null;
        currentExercises.clear();
        currentVideoIndex = 0;
    }

    @FXML
    private void closeReward() {
        rewardSection.setVisible(false);
        rewardSection.setManaged(false);
        gifPlayer.getEngine().load(null);
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean isYouTubeUrl(String url) {
        return url.contains("youtube.com/watch?v=") || url.contains("youtu.be/") || url.contains("youtube.com/embed/");
    }
}