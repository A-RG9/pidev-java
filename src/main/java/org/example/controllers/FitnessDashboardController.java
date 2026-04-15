package org.example.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.example.dao.*;
import org.example.models.*;
import java.util.List;
import java.util.stream.Collectors;

public class FitnessDashboardController {

    @FXML private Label lblGoalTitle, lblStartDate, lblEndDate, lblPlanCount;
    @FXML private ProgressBar progressGoal;
    @FXML private VBox statProgress, statPlans, statMinutes, statTimeLeft;
    @FXML private TextField searchDashboard;

    private IGoalDao goalDAO = new GoalDaoImpl();
    private DailyPlanDAO planDAO = new DailyPlanDAO();
    private List<Goal> allGoals;

    @FXML
    public void initialize() {
        // Chargement initial
        allGoals = goalDAO.getAllGoals();
        updateDisplay(allGoals);

        // Logique de recherche
        searchDashboard.textProperty().addListener((obs, old, query) -> {
            List<Goal> filtered = allGoals.stream()
                    .filter(g -> g.getTitle().toLowerCase().contains(query.toLowerCase()))
                    .collect(Collectors.toList());
            updateDisplay(filtered);
        });
    }

    private void updateDisplay(List<Goal> goals) {
        if (goals.isEmpty()) {
            lblGoalTitle.setText("Aucun objectif trouvé");
            return;
        }

        // On affiche le premier objectif de la liste
        Goal g = goals.get(0);
        lblGoalTitle.setText(g.getTitle());
        lblStartDate.setText("25 Fév, 2026"); // À remplacer par g.getStartDate() si dispo
        lblEndDate.setText("20 Mai, 2026");

        // Calculer les plans via la DB
        List<DailyPlan> plans = planDAO.getAllPlans();
        lblPlanCount.setText(plans.size() + " plans");

        // Mise à jour des cartes de stats
        refreshStats(plans.size());
    }

    private void refreshStats(int planSize) {
        statProgress.getChildren().clear();
        statPlans.getChildren().clear();
        statMinutes.getChildren().clear();
        statTimeLeft.getChildren().clear();

        setupStatCard(statProgress, "PROGRESS", "45%", "#14b8a6");
        setupStatCard(statPlans, "PLANS", String.valueOf(planSize), "#3b82f6");
        setupStatCard(statMinutes, "MINUTES", "120", "#f59e0b");
        setupStatCard(statTimeLeft, "TIME LEFT", "35d", "#ef4444");
    }

    private void setupStatCard(VBox card, String title, String value, String color) {
        card.setStyle("-fx-background-color: #1e293b; -fx-background-radius: 12; -fx-padding: 15; -fx-border-color: #334155;");
        Label lblT = new Label(title);
        lblT.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 10; -fx-font-weight: bold;");
        Label lblV = new Label(value);
        lblV.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 22; -fx-font-weight: bold;");
        card.getChildren().addAll(lblT, lblV);
    }
}