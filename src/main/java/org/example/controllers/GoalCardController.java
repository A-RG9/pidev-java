package org.example.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.text.Text;
import org.example.models.Goal;

public class GoalCardController {
    @FXML private Label lblStatus;
    @FXML private Label lblCategory;
    @FXML private Text txtTitle;
    @FXML private Text txtDescription;
    @FXML private Label lblProgress;
    @FXML private ProgressBar progressBar;
    @FXML private Label lblPatient;
    @FXML private Label lblEndDate;

    // Nouveaux boutons
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;

    // On ajoute un paramètre parentController pour pouvoir lui parler
    public void setGoalData(Goal goal, GoalController parentController) {
        txtTitle.setText(goal.getTitle());
        txtDescription.setText(goal.getDescription());
        lblCategory.setText(goal.getCategory());
        lblStatus.setText(goal.getStatus());
        lblPatient.setText(goal.getPatientId());
        lblEndDate.setText(goal.getEndDate() != null ? goal.getEndDate().toString() : "Non définie");

        lblProgress.setText(goal.getProgress() + "%");
        progressBar.setProgress(goal.getProgress() / 100.0);

        if ("Terminé".equals(goal.getStatus())) {
            lblStatus.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-padding: 3 10; -fx-background-radius: 10;");
        } else if ("En attente".equals(goal.getStatus())) {
            lblStatus.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white; -fx-padding: 3 10; -fx-background-radius: 10;");
        }

        // Actions des boutons : on demande au parent de faire le travail !
        btnEdit.setOnAction(event -> parentController.openEditForm(goal));
        btnDelete.setOnAction(event -> parentController.handleDelete(goal));
    }
}