package org.example.controllers;

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
import org.example.dao.DailyPlanDAO;
import org.example.dao.IGoalDao;
import org.example.dao.GoalDaoImpl;
import org.example.models.DailyPlan;
import org.example.models.Goal;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public class WorkoutPlannerController {
    @FXML private GridPane calendarGrid;
    @FXML private ComboBox<Goal> comboGoals;
    @FXML private Label lblMonthYear, lblSelectedDate, lblDuration, lblCalories;

    private DailyPlanDAO dailyPlanDAO = new DailyPlanDAO();
    private IGoalDao goalDao = new GoalDaoImpl();
    private YearMonth currentMonth = YearMonth.now();

    @FXML
    public void initialize() {
        // Style du calendrier (Thème Light)
        calendarGrid.setStyle("-fx-background-color: white;");

        loadGoals();
        setupGoalComboBox();
        refreshCalendar();

        // Filtrage : rafraîchir quand on sélectionne un Goal
        comboGoals.setOnAction(e -> refreshCalendar());
    }

    private void setupGoalComboBox() {
        comboGoals.setCellFactory(lv -> new ListCell<Goal>() {
            @Override
            protected void updateItem(Goal item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getTitle());
            }
        });
        comboGoals.setButtonCell(new ListCell<Goal>() {
            @Override
            protected void updateItem(Goal item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getTitle());
            }
        });
    }

    private void loadGoals() {
        List<Goal> goals = goalDao.getAllGoals();
        comboGoals.getItems().clear();
        comboGoals.getItems().addAll(goals);
    }

    private void refreshCalendar() {
        calendarGrid.getChildren().clear();
        lblMonthYear.setText(currentMonth.getMonth().toString() + " " + currentMonth.getYear());

        List<DailyPlan> plans = dailyPlanDAO.getDailyPlansByMonth(currentMonth.getMonthValue(), currentMonth.getYear());
        Goal selectedGoal = comboGoals.getValue();

        LocalDate firstOfMonth = currentMonth.atDay(1);
        int dayOffset = firstOfMonth.getDayOfWeek().getValue() % 7;

        for (int day = 1; day <= currentMonth.lengthOfMonth(); day++) {
            LocalDate date = currentMonth.atDay(day);
            VBox dayBox = createDayBox(day, date, plans, selectedGoal);

            int col = (day + dayOffset - 1) % 7;
            int row = (day + dayOffset - 1) / 7 + 1;
            calendarGrid.add(dayBox, col, row);
        }
    }

    private VBox createDayBox(int day, LocalDate date, List<DailyPlan> plans, Goal filter) {
        VBox box = new VBox(5);
        box.setAlignment(Pos.TOP_LEFT);
        box.setStyle("-fx-border-color: #F1F5F9; -fx-padding: 8; -fx-background-color: white; -fx-cursor: hand;");
        box.setPrefSize(120, 110);

        Label lbl = new Label(String.valueOf(day));
        lbl.setStyle("-fx-text-fill: #1E293B; -fx-font-weight: bold;");
        box.getChildren().add(lbl);

        for (DailyPlan p : plans) {
            if (p.getDate().toLocalDate().equals(date)) {
                // FILTRAGE : On ignore le plan s'il ne correspond pas au Goal sélectionné
                if (filter != null && p.getGoalId() != filter.getId()) {
                    continue;
                }

                Label tag = new Label(p.getTitre());
                tag.setStyle("-fx-background-color: #F0FDFA; -fx-text-fill: #0D9488; -fx-font-size: 10; -fx-padding: 2 5; -fx-background-radius: 4;");
                tag.setMaxWidth(Double.MAX_VALUE);
                box.getChildren().add(tag);
            }
        }

        box.setOnMouseClicked(e -> {
            lblSelectedDate.setText(date.toString());
            updateStats(date, plans, filter);
        });

        return box;
    }

    private void updateStats(LocalDate date, List<DailyPlan> plans, Goal filter) {
        DailyPlan selected = plans.stream()
                .filter(p -> p.getDate().toLocalDate().equals(date))
                .filter(p -> filter == null || p.getGoalId() == filter.getId())
                .findFirst().orElse(null);

        if (selected != null) {
            lblDuration.setText(selected.getDureeMin() + " min");
            lblCalories.setText(selected.getCalories() + " kcal");
        } else {
            lblDuration.setText("0 min");
            lblCalories.setText("0 kcal");
        }
    }

    // --- NAVIGATION ---

    @FXML
    private void loadDailyPlanEditor(ActionEvent event) {
        // Utilisation de ta méthode loadPage personnalisée
        loadPage(event, "/DailyPlanEditor.fxml");
    }

    private void loadPage(ActionEvent event, String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Impossible de charger la page : " + fxmlPath);
        }
    }

    @FXML private void nextMonth() { currentMonth = currentMonth.plusMonths(1); refreshCalendar(); }
    @FXML private void prevMonth() { currentMonth = currentMonth.minusMonths(1); refreshCalendar(); }
}