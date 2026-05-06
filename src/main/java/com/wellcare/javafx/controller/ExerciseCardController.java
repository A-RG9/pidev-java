package com.wellcare.javafx.controller;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;
import javafx.util.Duration;
import com.wellcare.javafx.model.Exercise;
import com.wellcare.javafx.util.VideoUploadService;

import java.io.File;

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
        txtTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-fill: #1E293B;");
    }

    private void setupDescription(String description) {
        if (description != null && !description.isEmpty()) {
            txtDescription.setText(description);
            txtDescription.setStyle("-fx-font-size: 12px; -fx-fill: #64748B;");
        } else {
            txtDescription.setText("Aucune description");
            txtDescription.setStyle("-fx-font-size: 12px; -fx-fill: #94A3B8; -fx-font-style: italic;");
        }
    }

    private void setupDurationLabel(int duration) {
        lblDuration.setText("⏱️ " + duration + " min");
        lblDuration.setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold; -fx-font-size: 12px;");
    }

    private void setupCategoryLabel(String category) {
        String displayCategory, backgroundColor, textColor, icon;
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
            default:
                icon = "📋";
                backgroundColor = "#F1F5F9";
                textColor = "#64748B";
                displayCategory = category;
        }
        lblCategory.setText(icon + " " + displayCategory);
        lblCategory.setStyle("-fx-background-color: " + backgroundColor + "; -fx-text-fill: " + textColor +
                "; -fx-padding: 4 12; -fx-border-radius: 12; -fx-background-radius: 12; -fx-font-weight: bold; -fx-font-size: 11px;");
    }

    private void setupDifficultyLabel(String difficultyLevel) {
        if (difficultyLevel != null) {
            String backgroundColor, textColor, emoji = "";
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
            lblDifficulty.setStyle("-fx-background-color: " + backgroundColor + "; -fx-text-fill: " + textColor +
                    "; -fx-padding: 4 12; -fx-border-radius: 12; -fx-background-radius: 12; -fx-font-weight: bold; -fx-font-size: 11px;");
        } else {
            lblDifficulty.setVisible(false);
        }
    }
    /**
     * Affiche le GIF ou la vidéo selon l'URL
     */
    private void setupVideoPlayer(String url) {
        videoPlayer.setContextMenuEnabled(false);

        if (url != null && !url.isEmpty()) {
            System.out.println("🎬 Chargement: " + url);

            // 🔥 CAS 1: Fichier GIF local
            if (url.endsWith(".gif")) {
                displayMediaFile(url, "gif");
            }
            // 📹 CAS 2: Vidéo locale
            else if (url.contains(".mp4") || url.contains(".avi") || url.contains(".mov") || url.contains(".webm")) {
                displayMediaFile(url, "video");
            }
            // 🎬 CAS 3: YouTube
            else if (VideoUploadService.isValidYouTubeUrl(url)) {
                displayYouTubeVideo(url);
            }
            // 🌐 CAS 4: Autre URL
            else {
                videoPlayer.getEngine().load(url);
            }
        } else {
            showPlaceholder();
        }
    }

    /**
     * Affiche un fichier média local (GIF ou vidéo)
     */
    private void displayMediaFile(String path, String type) {
        try {
            File mediaFile = findMediaFile(path);

            if (mediaFile != null && mediaFile.exists()) {
                String localUrl = mediaFile.toURI().toString();
                System.out.println("📁 " + type.toUpperCase() + " trouvé: " + localUrl);

                if (type.equals("gif")) {
                    displayGifContent(localUrl);
                } else {
                    displayVideoContent(localUrl);
                }
            } else {
                System.err.println("❌ " + type.toUpperCase() + " non trouvé: " + path);
                showPlaceholder();
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur affichage " + type + ": " + e.getMessage());
            showPlaceholder();
        }
    }

    /**
     * Cherche le fichier média dans différents emplacements
     */
    private File findMediaFile(String path) {
        // 1. Essayer le chemin direct
        File directFile = new File(path);
        if (directFile.exists()) {
            return directFile;
        }

        // 2. Essayer depuis le dossier du projet
        File projectFile = new File(System.getProperty("user.dir"), path);
        if (projectFile.exists()) {
            return projectFile;
        }

        // 3. Essayer dans uploads/videos/ ou uploads/gifs/
        String fileName = new File(path).getName();
        if (path.contains(".mp4") || path.contains(".avi")) {
            File uploadsVideo = new File("uploads/videos/" + fileName);
            if (uploadsVideo.exists()) {
                return uploadsVideo;
            }
        } else if (path.contains(".gif")) {
            File uploadsGif = new File("uploads/gifs/" + fileName);
            if (uploadsGif.exists()) {
                return uploadsGif;
            }
        }

        // 4. Essayer avec le chemin absolu
        File absoluteFile = new File(System.getProperty("user.dir") + "/" + path);
        if (absoluteFile.exists()) {
            return absoluteFile;
        }

        return null;
    }

    /**
     * Affiche un GIF dans le WebView
     */
    private void displayGifContent(String gifUrl) {
        String html = "<!DOCTYPE html>"
                + "<html>"
                + "<head>"
                + "<style>"
                + "body { margin: 0; background: #1a1a2e; display: flex; justify-content: center; align-items: center; height: 100vh; }"
                + "img { max-width: 100%; max-height: 100%; object-fit: contain; }"
                + "</style>"
                + "</head>"
                + "<body>"
                + "<img src=\"" + gifUrl + "\" alt=\"Exercise demonstration\"/>"
                + "</body>"
                + "</html>";

        videoPlayer.getEngine().loadContent(html);
        Tooltip.install(videoPlayer, new Tooltip("Animation GIF de l'exercice"));
    }

    /**
     * Affiche une vidéo dans le WebView
     */
    private void displayVideoContent(String videoUrl) {
        try {
            // 🔥 Méthode directe sans HTML complexe
            String html = "<video width='100%' height='100%' controls autoplay><source src='" + videoUrl + "' type='video/mp4'></video>";
            videoPlayer.getEngine().loadContent(html);

        } catch (Exception e) {
            System.err.println("❌ Erreur affichage vidéo: " + e.getMessage());
            showPlaceholder();
        }
    }

    /**
     * Affiche une vidéo YouTube
     */
    private void displayYouTubeVideo(String url) {
        String embedUrl = VideoUploadService.convertToEmbedUrl(url);
        videoPlayer.getEngine().load(embedUrl);
    }

    /**
     * Affiche un placeholder quand aucun média n'est disponible
     */
    private void showPlaceholder() {
        String html = """
        <html>
        <body style='margin:0;padding:0;background:linear-gradient(135deg,#667eea 0%,#764ba2 100%);display:flex;align-items:center;justify-content:center;height:100%;'>
            <div style='text-align:center;color:white;font-family:Arial;'>
                <div style='font-size:48px;margin-bottom:16px;'>🎬</div>
                <div style='font-size:14px;'>Aperçu non disponible</div>
                <div style='font-size:12px;margin-top:8px;opacity:0.8'>Exercice</div>
            </div>
        </body>
        </html>
        """;
        videoPlayer.getEngine().loadContent(html);
    }

    private void setupCardHoverEffect() {
        if (cardContainer != null) {
            cardContainer.setOnMouseEntered(e -> {
                cardContainer.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-border-radius: 16; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 15, 0, 0, 4); -fx-scale-x: 1.02; -fx-scale-y: 1.02;");
            });
            cardContainer.setOnMouseExited(e -> {
                cardContainer.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-border-radius: 16; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2); -fx-scale-x: 1; -fx-scale-y: 1;");
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
        if (parentController != null) parentController.openEditForm(exercise);
    }

    @FXML
    private void handleDelete() {
        if (cardContainer != null) {
            FadeTransition ft = new FadeTransition(Duration.millis(200), cardContainer);
            ft.setToValue(0);
            ft.setOnFinished(e -> {
                if (parentController != null) parentController.deleteExercise(exercise);
            });
            ft.play();
        } else if (parentController != null) parentController.deleteExercise(exercise);
    }
}