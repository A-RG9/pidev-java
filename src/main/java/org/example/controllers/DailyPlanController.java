package org.example.controllers;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.example.dao.DailyPlanDAO;
import org.example.dao.ExerciseDAO;
import org.example.dao.GoalDaoImpl;
import org.example.dao.IGoalDao;
import org.example.models.DailyPlan;
import org.example.models.Exercise;
import org.example.models.Goal;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DailyPlanController {

    @FXML private VBox formOverlay, exerciseList, plansContainer;
    @FXML private TextField txtTitre, txtDuree, txtCalories, searchPlan;
    @FXML private TextArea txtNotes;
    @FXML private ComboBox<Goal> comboGoals;
    @FXML private DatePicker datePicker;

    private List<CheckBox> exerciseCheckBoxes = new ArrayList<>();
    private DailyPlanDAO dailyPlanDAO = new DailyPlanDAO(); // Instance utilisée pour éviter l'erreur statique
    private ExerciseDAO exerciseDAO = new ExerciseDAO();
    private IGoalDao goalDAO = new GoalDaoImpl();

    private int editingPlanId = -1;

    @FXML
    public void initialize() {
        loadData();
        loadPlansList();
        searchPlan.textProperty().addListener((obs, old, val) -> filterPlans(val));
    }

    private void loadData() {
        comboGoals.getItems().setAll(goalDAO.getAllGoals());
        comboGoals.setCellFactory(lv -> new ListCell<Goal>() {
            @Override protected void updateItem(Goal item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item.getTitle());
            }
        });
        comboGoals.setButtonCell(comboGoals.getCellFactory().call(null));
        refreshExerciseList();
    }

    private void refreshExerciseList() {
        List<Exercise> exercises = exerciseDAO.getAllExercises();
        exerciseList.getChildren().clear();
        exerciseCheckBoxes.clear();
        for (Exercise ex : exercises) {
            CheckBox cb = new CheckBox(ex.getName());
            cb.setUserData(ex);
            cb.setStyle("-fx-text-fill: white; -fx-padding: 5;");
            exerciseCheckBoxes.add(cb);
            exerciseList.getChildren().add(cb);
        }
    }

    @FXML
    public void handleSave() {
        if (txtTitre.getText().trim().isEmpty() || comboGoals.getValue() == null || datePicker.getValue() == null) {
            showAlert("Erreur", "Veuillez remplir les champs obligatoires.");
            return;
        }

        List<Exercise> selected = exerciseCheckBoxes.stream()
                .filter(CheckBox::isSelected)
                .map(cb -> (Exercise) cb.getUserData())
                .collect(Collectors.toList());

        if (selected.isEmpty()) {
            showAlert("Erreur", "Sélectionnez au moins un exercice");
            return;
        }

        try {
            DailyPlan p = new DailyPlan();
            p.setTitre(txtTitre.getText());
            p.setGoalId(comboGoals.getValue().getId());
            p.setDate(java.sql.Date.valueOf(datePicker.getValue()));
            p.setDureeMin(txtDuree.getText().isEmpty() ? 0 : Integer.parseInt(txtDuree.getText()));
            p.setCalories(txtCalories.getText().isEmpty() ? 0 : Integer.parseInt(txtCalories.getText()));
            p.setNotes(txtNotes.getText());
            p.setStatus("PENDING");

            boolean success;
            if (editingPlanId == -1) {
                // Mode AJOUT : on appelle save
                success = dailyPlanDAO.saveDailyPlan(p, selected);
            } else {
                // Mode MODIFICATION : on fixe l'ID d'abord, puis on appelle update
                p.setId(editingPlanId); // Ligne séparée car elle renvoie 'void'
                success = dailyPlanDAO.updateDailyPlan(p, selected);
            }

            if (success) {
                closeForm();
                loadPlansList();
            } else {
                showAlert("Erreur", "L'opération a échoué en base de données.");
            }
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Format numérique invalide pour la durée ou les calories.");
        }
    }

    private void loadPlansList() {
        plansContainer.getChildren().clear();
        List<DailyPlan> plans = dailyPlanDAO.getAllPlans();
        for (DailyPlan plan : plans) {
            plansContainer.getChildren().add(createPlanCard(plan));
        }
    }

    private HBox createPlanCard(DailyPlan plan) {
        HBox card = new HBox(20);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(15, 20, 15, 20));

        // STYLE : Bleu intermédiaire (ni trop clair, ni trop sombre)
        card.setStyle(
                "-fx-background-color: #334155; " + // Bleu Ardoise (Slate 700)
                        "-fx-background-radius: 12; " +
                        "-fx-border-color: #475569; " +     // Bordure subtile
                        "-fx-border-width: 1; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 4);"
        );

        // --- INFOS GAUCHE ---
        VBox info = new VBox(5);
        Label lblTitre = new Label(plan.getTitre().toUpperCase());
        lblTitre.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 15;");

        Label lblDate = new Label("📅 " + plan.getDate().toString());
        lblDate.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 12;");
        info.getChildren().addAll(lblTitre, lblDate);

        // --- EXERCICES CENTRE ---
        VBox exSection = new VBox(3);
        HBox.setHgrow(exSection, Priority.ALWAYS);
        Label exTitle = new Label("EXERCICES :");
        exTitle.setStyle("-fx-text-fill: #22d3ee; -fx-font-size: 10; -fx-font-weight: bold;"); // Cyan

        List<Exercise> exercises = dailyPlanDAO.getExercisesForPlan(plan.getId());
        String names = exercises.stream().map(Exercise::getName).collect(Collectors.joining(" • "));

        Label lblExList = new Label(names.isEmpty() ? "Aucun exercice" : names);
        lblExList.setStyle("-fx-text-fill: #e2e8f0; -fx-font-size: 12; -fx-font-style: italic;");
        exSection.getChildren().addAll(exTitle, lblExList);

        // --- 3. ACTIONS ET STATS ---
        VBox rightSection = new VBox(10);
        rightSection.setAlignment(Pos.CENTER_RIGHT);

        // Badges (Durée et Calories)
        HBox stats = new HBox(8);
        stats.getChildren().addAll(
                createBadge("⏳ " + plan.getDureeMin() + "m", "#0284c7"),
                createBadge("🔥 " + plan.getCalories() + "c", "#e11d48")
        );

        // Boutons avec texte
        HBox btns = new HBox(10);
        Button btnEdit = new Button("Modifier");
        btnEdit.setStyle("-fx-background-color: #14b8a6; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        btnEdit.setOnAction(e -> handleEdit(plan));

        Button btnDelete = new Button("Supprimer");
        btnDelete.setStyle("-fx-background-color: #f43f5e; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        btnDelete.setOnAction(e -> handleDelete(plan));

        btns.getChildren().addAll(btnEdit, btnDelete);
        rightSection.getChildren().addAll(stats, btns);

        card.getChildren().addAll(info, exSection, rightSection);
        return card;
    }

    private void handleEdit(DailyPlan plan) {
        editingPlanId = plan.getId();
        txtTitre.setText(plan.getTitre());
        txtDuree.setText(String.valueOf(plan.getDureeMin()));
        txtCalories.setText(String.valueOf(plan.getCalories()));
        txtNotes.setText(plan.getNotes());
        datePicker.setValue(plan.getDate().toLocalDate());
        formOverlay.setVisible(true);
    }

    private void handleDelete(DailyPlan plan) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer ce plan ?", ButtonType.YES, ButtonType.NO);
        if (a.showAndWait().get() == ButtonType.YES) {
            if (dailyPlanDAO.deleteDailyPlan(plan.getId())) loadPlansList();
        }
    }

    @FXML public void openAddForm() { editingPlanId = -1; formOverlay.setVisible(true); }
    @FXML public void closeForm() { formOverlay.setVisible(false); }

    private HBox createBadge(String text, String color) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold; -fx-font-size: 11;");

        HBox container = new HBox(lbl);
        container.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-padding: 3 8; " +
                        "-fx-background-radius: 15; " +
                        "-fx-border-color: " + color + "; " +
                        "-fx-border-width: 1;"
        );
        return container;
    }

    private void filterPlans(String query) { /* logique de filtrage */ }
    private void showAlert(String t, String c) { Alert a = new Alert(Alert.AlertType.ERROR); a.setTitle(t); a.setContentText(c); a.show(); }
}