package org.example.controllers;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.dao.DailyPlanDAO;
import org.example.dao.IGoalDao;
import org.example.dao.GoalDaoImpl;
import org.example.models.DailyPlan;
import org.example.models.Goal;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class WorkoutPlannerController {

    @FXML private GridPane calendarGrid;
    @FXML private ComboBox<Goal> comboGoals;
    @FXML private Label lblMonthYear, lblSelectedDate, lblDuration, lblCalories;
    @FXML private Label lblPlanTitle, lblExercisesCount;

    private DailyPlanDAO dailyPlanDAO = new DailyPlanDAO();
    private IGoalDao goalDao = new GoalDaoImpl();
    private YearMonth currentMonth = YearMonth.now();
    private List<DailyPlan> currentPlans = null;
    private List<DailyPlan> allPlans = null;
    private Goal selectedGoal = null;

    @FXML
    public void initialize() {
        setupUI();
        loadGoals();
        setupGoalComboBox();
        setupDayHeaders();
        refreshCalendar();

        comboGoals.setOnAction(e -> {
            selectedGoal = comboGoals.getValue();
            System.out.println("=== FILTRAGE ===");
            System.out.println("Goal sélectionné: " + (selectedGoal != null ? selectedGoal.getTitle() + " (ID: " + selectedGoal.getId() + ")" : "null"));
            filterPlansByGoal();
            renderCalendar();
        });
    }

    private void setupUI() {
        calendarGrid.setStyle("-fx-background-color: white; -fx-background-radius: 20;");
        calendarGrid.setAlignment(Pos.CENTER);
    }

    private void setupDayHeaders() {
        String[] days = {"MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN"};
        for (int i = 0; i < days.length; i++) {
            Label dayLabel = new Label(days[i]);
            dayLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #64748B; -fx-padding: 10; -fx-font-size: 12px;");
            dayLabel.setAlignment(Pos.CENTER);
            dayLabel.setMaxWidth(Double.MAX_VALUE);
            GridPane.setHgrow(dayLabel, Priority.ALWAYS);
            calendarGrid.add(dayLabel, i, 0);
        }
    }

    private void setupGoalComboBox() {
        comboGoals.setCellFactory(lv -> new ListCell<Goal>() {
            @Override
            protected void updateItem(Goal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else if (item.getId() == -1) {
                    setText("📋 " + item.getTitle());
                } else {
                    setText("🎯 " + item.getTitle() + " (ID: " + item.getId() + ")");
                }
            }
        });

        comboGoals.setButtonCell(new ListCell<Goal>() {
            @Override
            protected void updateItem(Goal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("All Goals");
                } else if (item.getId() == -1) {
                    setText("📋 " + item.getTitle());
                } else {
                    setText("🎯 " + item.getTitle());
                }
            }
        });
    }

    private void loadGoals() {
        List<Goal> goalsCache = goalDao.getAllGoals();
        comboGoals.getItems().clear();

        // Ajouter une option "Tous les objectifs"
        Goal allGoals = new Goal();
        allGoals.setId(-1);
        allGoals.setTitle("All Goals");
        comboGoals.getItems().add(allGoals);

        comboGoals.getItems().addAll(goalsCache);
        comboGoals.setValue(allGoals);
        selectedGoal = allGoals;

        System.out.println("=== CHARGEMENT DES GOALS ===");
        System.out.println("Goals chargés: " + goalsCache.size() + " objectifs");
        for (Goal g : goalsCache) {
            System.out.println("  - ID: " + g.getId() + ", Titre: " + g.getTitle());
        }
    }

    @FXML
    public void resetGoalFilter() {
        Goal allGoals = comboGoals.getItems().stream()
                .filter(g -> g.getId() == -1)
                .findFirst()
                .orElse(null);
        comboGoals.setValue(allGoals);
        selectedGoal = allGoals;
        filterPlansByGoal();
        renderCalendar();
        showInfoMessage("Filtre réinitialisé - Tous les plans affichés", "#10B981");
    }

    private void filterPlansByGoal() {
        if (allPlans == null) {
            allPlans = dailyPlanDAO.getDailyPlansByMonth(currentMonth.getMonthValue(), currentMonth.getYear());
            System.out.println("\n=== PLANS CHARGÉS ===");
            System.out.println("Total plans: " + allPlans.size());
            for (DailyPlan p : allPlans) {
                System.out.println("  - ID: " + p.getId() + ", Titre: " + p.getTitre() + ", Goal ID: " + p.getGoalId() + ", Date: " + p.getDate());
            }
        }

        // Filtrer les plans par objectif sélectionné
        if (selectedGoal != null && selectedGoal.getId() != -1) {
            final int targetGoalId = selectedGoal.getId();
            currentPlans = allPlans.stream()
                    .filter(p -> p.getGoalId() == targetGoalId)
                    .collect(Collectors.toList());
            System.out.println("\n=== FILTRAGE ===");
            System.out.println("Goal sélectionné ID: " + targetGoalId + " (" + selectedGoal.getTitle() + ")");
            System.out.println("Plans correspondants: " + currentPlans.size());
            if (currentPlans.isEmpty()) {
                System.out.println("⚠️ Aucun plan avec goal_id = " + targetGoalId);
                System.out.println("Goal_id des plans existants: " + allPlans.stream().map(p -> String.valueOf(p.getGoalId())).collect(Collectors.joining(", ")));
            } else {
                for (DailyPlan p : currentPlans) {
                    System.out.println("  ✓ " + p.getTitre() + " (ID: " + p.getId() + ")");
                }
            }
        } else {
            currentPlans = allPlans;
            System.out.println("\n=== PAS DE FILTRE ===");
            System.out.println("Tous les plans: " + currentPlans.size());
        }
    }

    private void refreshCalendar() {
        // Recharger tous les plans pour le nouveau mois
        allPlans = dailyPlanDAO.getDailyPlansByMonth(currentMonth.getMonthValue(), currentMonth.getYear());
        System.out.println("\n=== MOIS CHANGÉ: " + currentMonth.getMonth() + " " + currentMonth.getYear() + " ===");

        filterPlansByGoal();
        renderCalendar();
    }

    private void renderCalendar() {
        calendarGrid.getChildren().removeIf(node -> {
            Integer row = GridPane.getRowIndex(node);
            return row != null && row > 0;
        });

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        lblMonthYear.setText(currentMonth.format(formatter));

        LocalDate firstOfMonth = currentMonth.atDay(1);
        int dayOffset = firstOfMonth.getDayOfWeek().getValue() % 7;

        int row = 1;
        for (int day = 1; day <= currentMonth.lengthOfMonth(); day++) {
            LocalDate date = currentMonth.atDay(day);
            VBox dayBox = createDayBox(day, date);

            int col = (day + dayOffset - 1) % 7;
            if (col == 0 && day > 1) row++;

            calendarGrid.add(dayBox, col, row);
        }

        // Ajouter des cellules vides pour compléter la grille
        int lastDayRow = row;
        int lastDayCol = (currentMonth.lengthOfMonth() + dayOffset - 1) % 7;
        while (lastDayCol < 6) {
            lastDayCol++;
            VBox emptyBox = new VBox();
            emptyBox.setPrefSize(120, 110);
            emptyBox.setStyle("-fx-border-color: #F1F5F9; -fx-background-color: #F8FAFC;");
            calendarGrid.add(emptyBox, lastDayCol, lastDayRow);
        }
    }

    private VBox createDayBox(int day, LocalDate date) {
        VBox box = new VBox(8);
        box.setAlignment(Pos.TOP_LEFT);
        box.setStyle("-fx-border-color: #E2E8F0; -fx-padding: 12; -fx-background-color: white; -fx-cursor: hand; -fx-background-radius: 8; -fx-border-radius: 8;");
        box.setPrefSize(120, 110);

        // Effet de survol
        box.setOnMouseEntered(e -> {
            box.setStyle("-fx-border-color: #667eea; -fx-padding: 12; -fx-background-color: #F8FAFC; -fx-cursor: hand; -fx-background-radius: 8; -fx-border-radius: 8;");
            ScaleTransition st = new ScaleTransition(Duration.millis(150), box);
            st.setToX(1.02);
            st.setToY(1.02);
            st.play();
        });

        box.setOnMouseExited(e -> {
            box.setStyle("-fx-border-color: #E2E8F0; -fx-padding: 12; -fx-background-color: white; -fx-cursor: hand; -fx-background-radius: 8; -fx-border-radius: 8;");
            ScaleTransition st = new ScaleTransition(Duration.millis(150), box);
            st.setToX(1);
            st.setToY(1);
            st.play();
        });

        Label lbl = new Label(String.valueOf(day));
        lbl.setStyle("-fx-text-fill: #1E293B; -fx-font-weight: bold; -fx-font-size: 14px;");
        box.getChildren().add(lbl);

        // Afficher les plans du jour
        List<DailyPlan> dayPlans = currentPlans.stream()
                .filter(p -> p.getDate().toLocalDate().equals(date))
                .collect(Collectors.toList());

        for (DailyPlan plan : dayPlans) {
            VBox planBox = new VBox(3);
            planBox.setStyle("-fx-background-color: #F0FDFA; -fx-background-radius: 6; -fx-padding: 6;");

            Label tag = new Label(plan.getTitre());
            tag.setStyle("-fx-text-fill: #0D9488; -fx-font-size: 10px; -fx-font-weight: bold;");
            tag.setMaxWidth(Double.MAX_VALUE);
            tag.setWrapText(true);

            Label timeLabel = new Label("⏱️ " + plan.getDureeMin() + " min");
            timeLabel.setStyle("-fx-text-fill: #64748B; -fx-font-size: 9px;");

            planBox.getChildren().addAll(tag, timeLabel);
            box.getChildren().add(planBox);
        }

        // Si c'est aujourd'hui, ajouter un indicateur
        if (date.equals(LocalDate.now())) {
            Label todayLabel = new Label("TODAY");
            todayLabel.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; -fx-font-size: 9px; -fx-padding: 2 6; -fx-background-radius: 10;");
            box.getChildren().add(todayLabel);
        }

        box.setOnMouseClicked(e -> {
            updateStats(date);
            animateBoxClick(box);
        });

        return box;
    }

    private void animateBoxClick(VBox box) {
        ScaleTransition st = new ScaleTransition(Duration.millis(100), box);
        st.setToX(0.98);
        st.setToY(0.98);
        st.setCycleCount(2);
        st.setAutoReverse(true);
        st.play();
    }

    private void updateStats(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy");
        lblSelectedDate.setText(date.format(formatter));

        DailyPlan selected = currentPlans.stream()
                .filter(p -> p.getDate().toLocalDate().equals(date))
                .findFirst()
                .orElse(null);

        if (selected != null) {
            lblDuration.setText(selected.getDureeMin() + " min");
            lblCalories.setText(selected.getCalories() + " kcal");
            lblPlanTitle.setText(selected.getTitre());

            int exercisesCount = dailyPlanDAO.getExercisesForPlan(selected.getId()).size();
            lblExercisesCount.setText(exercisesCount + " exercise" + (exercisesCount > 1 ? "s" : ""));

            animateStatLabels();
        } else {
            lblDuration.setText("0 min");
            lblCalories.setText("0 kcal");
            lblPlanTitle.setText("No plan scheduled");
            lblExercisesCount.setText("Select a date to view details");
        }
    }

    private void animateStatLabels() {
        ScaleTransition st1 = new ScaleTransition(Duration.millis(200), lblDuration);
        st1.setToX(1.1);
        st1.setToY(1.1);
        st1.setCycleCount(2);
        st1.setAutoReverse(true);
        st1.play();

        ScaleTransition st2 = new ScaleTransition(Duration.millis(200), lblCalories);
        st2.setToX(1.1);
        st2.setToY(1.1);
        st2.setCycleCount(2);
        st2.setAutoReverse(true);
        st2.play();
    }

    @FXML
    private void loadDailyPlanEditor(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DailyPlanView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            FadeTransition ft = new FadeTransition(Duration.millis(300), root);
            ft.setFromValue(0);
            ft.setToValue(1);

            Scene scene = new Scene(root);
            stage.setScene(scene);
            ft.play();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Impossible de charger l'éditeur de plans");
        }
    }

    @FXML
    private void viewAllPlans(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DailyPlanView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            FadeTransition ft = new FadeTransition(Duration.millis(300), root);
            ft.setFromValue(0);
            ft.setToValue(1);

            Scene scene = new Scene(root);
            stage.setScene(scene);
            ft.play();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Impossible de charger la liste des plans");
        }
    }

    private void showInfoMessage(String message, String color) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: white;");

        alert.showAndWait();
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: white;");

        alert.showAndWait();
    }

    @FXML
    private void nextMonth() {
        currentMonth = currentMonth.plusMonths(1);
        allPlans = null; // Forcer le rechargement
        refreshCalendar();
        animateMonthChange();
    }

    @FXML
    private void prevMonth() {
        currentMonth = currentMonth.minusMonths(1);
        allPlans = null; // Forcer le rechargement
        refreshCalendar();
        animateMonthChange();
    }

    private void animateMonthChange() {
        ScaleTransition st = new ScaleTransition(Duration.millis(200), calendarGrid);
        st.setToX(1.02);
        st.setToY(1.02);
        st.setCycleCount(2);
        st.setAutoReverse(true);
        st.play();
    }
}