package org.example.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;
import org.example.models.Exercise;

public class ExerciseCardController {

    @FXML private WebView videoPlayer;
    @FXML private Text txtTitle; // Gardez le nom du fx:id de votre FXML ici
    @FXML private Text txtDescription;
    @FXML private Label lblDuration ;
    @FXML private Label lblCategory;
    @FXML private Label lblDifficulty;




    private Exercise exercise;
    private ExerciseLibraryController parentController;

    public void setExerciseData(Exercise exercise, ExerciseLibraryController parentController) {
        this.exercise = exercise;
        this.parentController = parentController;

        // ATTENTION : On utilise .getName() car votre table utilise la colonne 'name'
        txtTitle.setText(exercise.getName());
        txtDescription.setText(exercise.getDescription());
        lblDuration.setText(exercise.getDuration() + " min");
        lblCategory.setText(exercise.getCategory());

        // Configuration du WebView pour éviter les bordures blanches et gérer l'URL
        videoPlayer.setContextMenuEnabled(false); // Désactive le clic droit sur la vidéo

        String url = exercise.getVideoUrl();
        if (url != null && !url.isEmpty()) {
            if (url.contains("youtube.com/watch?v=")) {
                // Conversion propre de l'URL YouTube en version Embed (Intégrée)
                String videoId = "";
                try {
                    videoId = url.substring(url.indexOf("v=") + 2);
                    int ampersandPos = videoId.indexOf('&');
                    if (ampersandPos != -1) {
                        videoId = videoId.substring(0, ampersandPos);
                    }
                    String embedUrl = "https://www.youtube.com/embed/" + videoId;
                    videoPlayer.getEngine().load(embedUrl);
                } catch (Exception e) {
                    System.out.println("Erreur format URL YouTube");
                    videoPlayer.setVisible(false);
                }
            } else if (url.startsWith("http") || url.startsWith("file:")) {
                videoPlayer.getEngine().load(url);
            } else {
                // Si c'est un chemin local brut, on rajoute file:///
                videoPlayer.getEngine().load("file:///" + url.replace("\\", "/"));
            }
        } else {
            videoPlayer.setVisible(false); // Cache le lecteur si pas de vidéo
        }
        if(lblDifficulty != null) {
            lblDifficulty.setText(exercise.getDifficultyLevel());}

    }

    @FXML
    private void handleEdit() {
        if (parentController != null) {
            parentController.openEditForm(exercise);
        }
    }

    @FXML
    private void handleDelete() {
        if (parentController != null) {
            parentController.deleteExercise(exercise);
        }
    }
}