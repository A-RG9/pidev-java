package org.example.controllers;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;
import javafx.util.Duration;
import org.example.models.Exercise;

public class ExerciseCardController {

    @FXML private WebView videoPlayer;
    @FXML private Text txtTitle;
    @FXML private Text txtDescription;
    @FXML private Label lblDuration;
    @FXML private Label lblCategory;
    @FXML private Label lblDifficulty;
    @FXML private VBox cardContainer;
    @FXML private HBox actionButtons;

    private Exercise exercise;
    private ExerciseLibraryController parentController;

    public void setExerciseData(Exercise exercise, ExerciseLibraryController parentController) {
        this.exercise = exercise;
        this.parentController = parentController;

        setupTitle(exercise.getName());
        setupDescription(exercise.getDescription());
        setupDurationLabel(exercise.getDuration());
        setupCategoryLabel(exercise.getCategory());
        setupDifficultyLabel(exercise.getDifficultyLevel());
        setupVideoPlayer(exercise.getVideoUrl());
        setupCardHoverEffect();
        animateCardEntry();
    }

    private void setupTitle(String title) {
        txtTitle.setText(title);
        txtTitle.setStyle(
                "-fx-font-size: 16px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-fill: #1E293B;" +
                        "-fx-font-family: 'Segoe UI';"
        );
    }

    private void setupDescription(String description) {
        if (description != null && !description.isEmpty()) {
            txtDescription.setText(description);
            txtDescription.setStyle(
                    "-fx-font-size: 12px;" +
                            "-fx-fill: #64748B;" +
                            "-fx-font-family: 'Segoe UI';"
            );
        } else {
            txtDescription.setText("Aucune description");
            txtDescription.setStyle(
                    "-fx-font-size: 12px;" +
                            "-fx-fill: #94A3B8;" +
                            "-fx-font-style: italic;" +
                            "-fx-font-family: 'Segoe UI';"
            );
        }
    }

    private void setupDurationLabel(int duration) {
        lblDuration.setText("⏱️ " + duration + " min");
        lblDuration.setStyle(
                "-fx-text-fill: #10B981;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 12px;"
        );
    }

    private void setupCategoryLabel(String category) {
        String displayCategory;
        String backgroundColor;
        String textColor;
        String icon;

        switch (category) {
            case "Cardio":
                icon = "🏃";
                backgroundColor = "#FEF3C7";
                textColor = "#D97706";
                displayCategory = "Cardio";
                break;
            case "Musculation":
                icon = "💪";
                backgroundColor = "#D1FAE5";
                textColor = "#059669";
                displayCategory = "Musculation";
                break;
            case "Yoga":
                icon = "🧘";
                backgroundColor = "#E0E7FF";
                textColor = "#4F46E5";
                displayCategory = "Yoga";
                break;
            case "Étirements":
                icon = "🤸";
                backgroundColor = "#FCE7F3";
                textColor = "#DB2777";
                displayCategory = "Étirements";
                break;
            default:
                icon = "📋";
                backgroundColor = "#F1F5F9";
                textColor = "#64748B";
                displayCategory = category;
        }

        lblCategory.setText(icon + " " + displayCategory);
        lblCategory.setStyle(
                "-fx-background-color: " + backgroundColor + ";" +
                        "-fx-text-fill: " + textColor + ";" +
                        "-fx-padding: 4 12 4 12;" +
                        "-fx-border-radius: 12;" +
                        "-fx-background-radius: 12;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 11px;"
        );
    }

    private void setupDifficultyLabel(String difficultyLevel) {
        if (difficultyLevel != null) {
            String backgroundColor;
            String textColor;
            String emoji = "";

            switch (difficultyLevel.toLowerCase()) {
                case "beginner":
                    backgroundColor = "#E8F5E9";
                    textColor = "#43A047";
                    emoji = "🌱 ";
                    break;
                case "intermediate":
                    backgroundColor = "#FFF3E0";
                    textColor = "#FB8C00";
                    emoji = "⚡ ";
                    break;
                case "advanced":
                    backgroundColor = "#FFEBEE";
                    textColor = "#E53935";
                    emoji = "🔥 ";
                    break;
                default:
                    backgroundColor = "#ECEFF1";
                    textColor = "#546E7A";
                    emoji = "⭐ ";
            }

            lblDifficulty.setText(emoji + difficultyLevel);
            lblDifficulty.setStyle(
                    "-fx-background-color: " + backgroundColor + ";" +
                            "-fx-text-fill: " + textColor + ";" +
                            "-fx-padding: 4 12 4 12;" +
                            "-fx-border-radius: 12;" +
                            "-fx-background-radius: 12;" +
                            "-fx-font-weight: bold;" +
                            "-fx-font-size: 11px;"
            );
        } else {
            lblDifficulty.setVisible(false);
        }
    }

    private void setupVideoPlayer(String url) {
        videoPlayer.setContextMenuEnabled(false);
        videoPlayer.setStyle("-fx-border-radius: 12; -fx-background-radius: 12;");

        if (url != null && !url.isEmpty()) {
            String embedUrl = convertToEmbedUrl(url);
            if (embedUrl != null) {
                videoPlayer.getEngine().load(embedUrl);
                videoPlayer.getEngine().setOnError(event -> showVideoPlaceholder());
            } else {
                showVideoPlaceholder();
            }
        } else {
            showVideoPlaceholder();
        }
    }

    private String convertToEmbedUrl(String url) {
        try {
            if (url.contains("youtube.com/watch?v=")) {
                String videoId = url.substring(url.indexOf("v=") + 2);
                int ampersandPos = videoId.indexOf('&');
                if (ampersandPos != -1) {
                    videoId = videoId.substring(0, ampersandPos);
                }
                return "https://www.youtube.com/embed/" + videoId +
                        "?autoplay=0&modestbranding=1&rel=0&showinfo=0";
            } else if (url.contains("youtu.be/")) {
                String videoId = url.substring(url.lastIndexOf("/") + 1);
                return "https://www.youtube.com/embed/" + videoId;
            } else if (url.startsWith("http") || url.startsWith("file:")) {
                return url;
            } else if (!url.isEmpty()) {
                return "file:///" + url.replace("\\", "/");
            }
        } catch (Exception e) {
            System.out.println("Erreur conversion URL: " + e.getMessage());
        }
        return null;
    }

    private void showVideoPlaceholder() {
        String placeholderHtml =
                "<html><body style='margin:0; padding:0; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); display:flex; align-items:center; justify-content:center; height:100%;'>" +
                        "<div style='text-align:center; color:white; font-family:Arial;'>" +
                        "<div style='font-size:48px; margin-bottom:16px;'>🎬</div>" +
                        "<div style='font-size:14px;'>Aperçu vidéo non disponible</div>" +
                        "<div style='font-size:12px; margin-top:8px; opacity:0.8'>" + exercise.getName() + "</div>" +
                        "</div></body></html>";
        videoPlayer.getEngine().loadContent(placeholderHtml);
    }

    private void setupCardHoverEffect() {
        if (cardContainer != null) {
            cardContainer.setOnMouseEntered(e -> {
                cardContainer.setStyle(
                        "-fx-background-color: white;" +
                                "-fx-background-radius: 16;" +
                                "-fx-border-radius: 16;" +
                                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 15, 0, 0, 4);" +
                                "-fx-scale-x: 1.02;" +
                                "-fx-scale-y: 1.02;" +
                                "-fx-transition: all 0.3s ease;"
                );
            });

            cardContainer.setOnMouseExited(e -> {
                cardContainer.setStyle(
                        "-fx-background-color: white;" +
                                "-fx-background-radius: 16;" +
                                "-fx-border-radius: 16;" +
                                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);" +
                                "-fx-scale-x: 1;" +
                                "-fx-scale-y: 1;"
                );
            });
        }
    }

    private void animateCardEntry() {
        if (cardContainer != null) {
            cardContainer.setOpacity(0);
            cardContainer.setTranslateY(20);

            TranslateTransition tt = new TranslateTransition(Duration.millis(300), cardContainer);
            tt.setFromY(20);
            tt.setToY(0);

            FadeTransition ft = new FadeTransition(Duration.millis(300), cardContainer);
            ft.setFromValue(0);
            ft.setToValue(1);

            tt.play();
            ft.play();
        }
    }

    @FXML
    private void handleEdit() {
        if (cardContainer != null) {
            cardContainer.setScaleX(0.98);
            cardContainer.setScaleY(0.98);
            ScaleTransition st = new ScaleTransition(Duration.millis(100), cardContainer);
            st.setToX(1);
            st.setToY(1);
            st.play();
        }
        if (parentController != null) {
            parentController.openEditForm(exercise);
        }
    }

    @FXML
    private void handleDelete() {
        if (cardContainer != null) {
            FadeTransition ft = new FadeTransition(Duration.millis(200), cardContainer);
            ft.setToValue(0);
            ft.setOnFinished(e -> {
                if (parentController != null) {
                    parentController.deleteExercise(exercise);
                }
            });
            ft.play();
        } else if (parentController != null) {
            parentController.deleteExercise(exercise);
        }
    }
}