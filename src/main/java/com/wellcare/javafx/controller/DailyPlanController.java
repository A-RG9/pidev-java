package com.wellcare.javafx.controller;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import com.wellcare.javafx.dao.DailyPlanDAO;
import com.wellcare.javafx.dao.ExerciseDAO;
import com.wellcare.javafx.dao.GoalDaoImpl;
import com.wellcare.javafx.dao.IGoalDao;
import com.wellcare.javafx.model.DailyPlan;
import com.wellcare.javafx.model.Exercise;
import com.wellcare.javafx.model.Goal;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DailyPlanController {

    @FXML private VBox formOverlay, exerciseList, plansContainer;
    @FXML private TextField txtTitre, txtDuree, txtCalories, searchPlan;
    @FXML private TextArea txtNotes;
    @FXML private ComboBox<Goal> comboGoals;
    @FXML private DatePicker datePicker;
    @FXML private Label lblResultCount;
    @FXML private Button btnResetSearch;
    @FXML private ScrollPane plansScrollPane;

    // Statistiques et filtres
    @FXML private Label lblTotalPlans;
    @FXML private Label lblTotalExercises;
    @FXML private Label lblTotalCalories;
    @FXML private ComboBox<String> filterStatusPlan;
    @FXML private DatePicker filterDate;
    @FXML private Button btnResetFilters;
    @FXML private ComboBox<Goal> filterGoal;
    @FXML private Label lblPageInfo;

    private List<CheckBox> exerciseCheckBoxes = new ArrayList<>();
    private DailyPlanDAO dailyPlanDAO = new DailyPlanDAO();
    private ExerciseDAO exerciseDAO = new ExerciseDAO();
    private IGoalDao goalDAO = new GoalDaoImpl();

    private int editingPlanId = -1;
    private FadeTransition formFadeIn;
    private FadeTransition formFadeOut;
    private List<Goal> goalsCache;

    // Pagination
    private int currentPage = 1;
    private int itemsPerPage = 10;
    private int totalPages = 1;
    private List<DailyPlan> currentFilteredPlans = new ArrayList<>();

    @FXML
    public void initialize() {
        setupUI();
        setupAnimations();
        loadData();
        setupSearchListener();
        setupFilters();
        setupGoalFilter();
        loadPlansList();
    }

    private void setupUI() {
        if (plansContainer != null) {
            plansContainer.setStyle(
                    "-fx-background-color: #F8FAFC;" +
                            "-fx-spacing: 16;" +
                            "-fx-padding: 20;"
            );
        }

        if (plansScrollPane != null) {
            plansScrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
            plansScrollPane.setFitToWidth(true);
        }

        if (searchPlan != null) {
            searchPlan.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-background-radius: 25;" +
                            "-fx-border-radius: 25;" +
                            "-fx-border-color: #E2E8F0;" +
                            "-fx-border-width: 1px;" +
                            "-fx-padding: 10 15 10 15;" +
                            "-fx-font-size: 13px;" +
                            "-fx-prompt-text-fill: #94A3B8;"
            );
            searchPlan.setPromptText("🔍 Rechercher un plan...");
        }

        if (btnResetSearch != null) {
            btnResetSearch.setStyle(
                    "-fx-background-color: #F1F5F9;" +
                            "-fx-text-fill: #64748B;" +
                            "-fx-background-radius: 20;" +
                            "-fx-padding: 8 16 8 16;" +
                            "-fx-cursor: hand;" +
                            "-fx-font-weight: bold;"
            );
            btnResetSearch.setOnMouseEntered(e -> {
                btnResetSearch.setStyle(
                        "-fx-background-color: #E2E8F0;" +
                                "-fx-text-fill: #475569;" +
                                "-fx-background-radius: 20;" +
                                "-fx-padding: 8 16 8 16;" +
                                "-fx-cursor: hand;" +
                                "-fx-font-weight: bold;"
                );
            });
            btnResetSearch.setOnMouseExited(e -> {
                btnResetSearch.setStyle(
                        "-fx-background-color: #F1F5F9;" +
                                "-fx-text-fill: #64748B;" +
                                "-fx-background-radius: 20;" +
                                "-fx-padding: 8 16 8 16;" +
                                "-fx-cursor: hand;" +
                                "-fx-font-weight: bold;"
                );
            });
        }

        if (datePicker != null) {
            datePicker.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-background-radius: 10;" +
                            "-fx-border-radius: 10;" +
                            "-fx-border-color: #E2E8F0;" +
                            "-fx-border-width: 1px;"
            );
        }

        String fieldStyle = "-fx-background-color: white;" +
                "-fx-background-radius: 10;" +
                "-fx-border-radius: 10;" +
                "-fx-border-color: #E2E8F0;" +
                "-fx-border-width: 1px;" +
                "-fx-padding: 10;" +
                "-fx-font-size: 13px;";

        if (txtTitre != null) txtTitre.setStyle(fieldStyle);
        if (txtDuree != null) txtDuree.setStyle(fieldStyle);
        if (txtCalories != null) txtCalories.setStyle(fieldStyle);
        if (txtNotes != null) txtNotes.setStyle(fieldStyle + " -fx-font-family: 'Segoe UI';");

        if (exerciseList != null) {
            exerciseList.setStyle(
                    "-fx-background-color: #F8FAFC;" +
                            "-fx-padding: 15;" +
                            "-fx-spacing: 8;" +
                            "-fx-background-radius: 10;"
            );
        }
    }

    private void setupFilters() {
        if (filterStatusPlan != null) {
            filterStatusPlan.setItems(FXCollections.observableArrayList(
                    "Tous", "PENDING", "IN_PROGRESS", "COMPLETED"
            ));
            filterStatusPlan.setValue("Tous");
            filterStatusPlan.valueProperty().addListener((obs, old, val) -> {
                currentPage = 1;
                loadPlansList();
            });
        }

        if (filterDate != null) {
            filterDate.valueProperty().addListener((obs, old, val) -> {
                currentPage = 1;
                loadPlansList();
            });
        }

        if (btnResetFilters != null) {
            btnResetFilters.setOnAction(e -> resetFilters());
        }
    }

    private void setupGoalFilter() {
        if (filterGoal != null) {
            List<Goal> goals = goalDAO.getAllGoals();
            Goal allGoalsItem = new Goal();
            allGoalsItem.setId(-1);
            allGoalsItem.setTitle("Tous les objectifs");
            goals.add(0, allGoalsItem);
            filterGoal.setItems(FXCollections.observableArrayList(goals));
            filterGoal.setValue(allGoalsItem);

            filterGoal.valueProperty().addListener((obs, old, val) -> {
                currentPage = 1;
                loadPlansList();
            });
        }
    }

    private void setupAnimations() {
        if (formOverlay != null) {
            formFadeIn = new FadeTransition(Duration.millis(300), formOverlay);
            formFadeIn.setFromValue(0);
            formFadeIn.setToValue(1);

            formFadeOut = new FadeTransition(Duration.millis(200), formOverlay);
            formFadeOut.setFromValue(1);
            formFadeOut.setToValue(0);
        }

        if (plansContainer != null) {
            plansContainer.setOpacity(0);
            TranslateTransition tt = new TranslateTransition(Duration.millis(400), plansContainer);
            tt.setFromY(20);
            tt.setToY(0);
            FadeTransition ft = new FadeTransition(Duration.millis(400), plansContainer);
            ft.setFromValue(0);
            ft.setToValue(1);
            tt.play();
            ft.play();
        }
    }

    private void setupSearchListener() {
        if (searchPlan != null) {
            searchPlan.textProperty().addListener((obs, old, val) -> {
                currentPage = 1;
                loadPlansList();
                animateSearch();
            });
        }
    }

    private void animateSearch() {
        if (searchPlan != null) {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), searchPlan);
            st.setToX(1.02);
            st.setToY(1.02);
            st.setCycleCount(2);
            st.setAutoReverse(true);
            st.play();
        }
    }

    private void updateResultCount(int count) {
        if (lblResultCount != null) {
            String text = count + " plan" + (count > 1 ? "s" : "") + " trouvé" + (count > 1 ? "s" : "");
            lblResultCount.setText(text);

            ScaleTransition st = new ScaleTransition(Duration.millis(300), lblResultCount);
            st.setToX(1.1);
            st.setToY(1.1);
            st.setCycleCount(2);
            st.setAutoReverse(true);
            st.play();
        }
    }

    private void updateStatistics() {
        List<DailyPlan> plans = dailyPlanDAO.getAllPlans();
        if (lblTotalPlans != null) {
            lblTotalPlans.setText(String.valueOf(plans.size()));
        }

        int totalExercises = 0;
        int totalCalories = 0;
        for (DailyPlan plan : plans) {
            totalExercises += dailyPlanDAO.getExercisesForPlan(plan.getId()).size();
            totalCalories += plan.getCalories();
        }

        if (lblTotalExercises != null) {
            lblTotalExercises.setText(String.valueOf(totalExercises));
        }
        if (lblTotalCalories != null) {
            lblTotalCalories.setText(String.valueOf(totalCalories));
        }
    }

    @FXML
    public void refreshPlansList() {
        loadPlansList();
        updateStatistics();
        showSuccessMessage("Liste rafraîchie");
    }

    @FXML
    public void resetFilters() {
        if (searchPlan != null) {
            searchPlan.clear();
        }
        if (filterStatusPlan != null) {
            filterStatusPlan.setValue("Tous");
        }
        if (filterDate != null) {
            filterDate.setValue(null);
        }
        if (filterGoal != null) {
            Goal allGoals = filterGoal.getItems().stream()
                    .filter(g -> g.getId() == -1)
                    .findFirst()
                    .orElse(null);
            filterGoal.setValue(allGoals);
        }
        currentPage = 1;
        loadPlansList();
        showSuccessMessage("Filtres réinitialisés");
    }

    private Goal getGoalById(int id) {
        if (goalsCache == null) {
            goalsCache = goalDAO.getAllGoals();
        }
        return goalsCache.stream()
                .filter(goal -> goal.getId() == id)
                .findFirst()
                .orElse(null);
    }

    private void loadData() {
        goalsCache = goalDAO.getAllGoals();
        if (goalsCache.isEmpty()) {
            showInfo("Information", "Aucun objectif trouvé. Veuillez d'abord créer des objectifs.");
        }
        if (comboGoals != null) {
            comboGoals.getItems().setAll(goalsCache);

            comboGoals.setCellFactory(lv -> new ListCell<Goal>() {
                @Override
                protected void updateItem(Goal item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText("🎯 " + item.getTitle());
                    }
                }
            });

            comboGoals.setButtonCell(new ListCell<Goal>() {
                @Override
                protected void updateItem(Goal item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText("Sélectionner un objectif");
                    } else {
                        setText("🎯 " + item.getTitle());
                    }
                }
            });

            comboGoals.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-background-radius: 10;" +
                            "-fx-border-radius: 10;" +
                            "-fx-border-color: #E2E8F0;" +
                            "-fx-border-width: 1px;"
            );
        }

        refreshExerciseList();
    }

    private void refreshExerciseList() {
        List<Exercise> exercises = exerciseDAO.getAllExercises();
        if (exerciseList != null) {
            exerciseList.getChildren().clear();
        }
        exerciseCheckBoxes.clear();

        if (exercises.isEmpty()) {
            Label emptyLabel = new Label("Aucun exercice disponible\nVeuillez d'abord créer des exercices");
            emptyLabel.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 12px; -fx-alignment: center;");
            emptyLabel.setAlignment(Pos.CENTER);
            if (exerciseList != null) {
                exerciseList.getChildren().add(emptyLabel);
            }
            return;
        }

        for (Exercise ex : exercises) {
            CheckBox cb = new CheckBox(ex.getName());
            cb.setUserData(ex);
            cb.setStyle(
                    "-fx-text-fill: #1E293B;" +
                            "-fx-padding: 8;" +
                            "-fx-font-size: 13px;" +
                            "-fx-cursor: hand;"
            );

            cb.setOnMouseEntered(e -> cb.setStyle(
                    "-fx-text-fill: #0F172A;" +
                            "-fx-padding: 8;" +
                            "-fx-font-size: 13px;" +
                            "-fx-cursor: hand;" +
                            "-fx-font-weight: bold;"
            ));

            cb.setOnMouseExited(e -> cb.setStyle(
                    "-fx-text-fill: #1E293B;" +
                            "-fx-padding: 8;" +
                            "-fx-font-size: 13px;" +
                            "-fx-cursor: hand;"
            ));

            exerciseCheckBoxes.add(cb);
            if (exerciseList != null) {
                exerciseList.getChildren().add(cb);
            }
        }
    }

    @FXML
    public void handleSave() {
        if (!validateForm()) return;

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
            p.setTitre(txtTitre.getText().trim());
            p.setGoalId(comboGoals.getValue().getId());
            p.setDate(java.sql.Date.valueOf(datePicker.getValue()));
            p.setDureeMin(txtDuree.getText().isEmpty() ? 0 : Integer.parseInt(txtDuree.getText()));
            p.setCalories(txtCalories.getText().isEmpty() ? 0 : Integer.parseInt(txtCalories.getText()));
            p.setNotes(txtNotes.getText());
            p.setStatus("PENDING");

            boolean success;
            if (editingPlanId == -1) {
                success = dailyPlanDAO.saveDailyPlan(p, selected);
                if (success) showSuccessMessage("Plan ajouté avec succès!");
            } else {
                p.setId(editingPlanId);
                success = dailyPlanDAO.updateDailyPlan(p, selected);
                if (success) showSuccessMessage("Plan modifié avec succès!");
            }

            if (success) {
                closeForm();
                loadPlansList();
                clearForm();
            } else {
                showAlert("Erreur", "L'opération a échoué. Veuillez réessayer.");
            }
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Format numérique invalide pour la durée ou les calories.");
        }
    }

    private boolean validateForm() {
        if (txtTitre.getText().trim().isEmpty()) {
            showAlert("Erreur", "Veuillez saisir un titre");
            return false;
        }
        if (comboGoals.getValue() == null) {
            showAlert("Erreur", "Veuillez sélectionner un objectif");
            return false;
        }
        if (datePicker.getValue() == null) {
            showAlert("Erreur", "Veuillez sélectionner une date");
            return false;
        }
        return true;
    }

    private void loadPlansList() {
        if (plansContainer == null) return;

        List<DailyPlan> allPlans = dailyPlanDAO.getAllPlans();

        // Application des filtres
        String searchText = searchPlan != null ? searchPlan.getText().toLowerCase() : "";
        String statusFilter = filterStatusPlan != null ? filterStatusPlan.getValue() : null;
        LocalDate dateFilter = filterDate != null ? filterDate.getValue() : null;
        Goal selectedGoal = filterGoal != null ? filterGoal.getValue() : null;

        currentFilteredPlans = allPlans.stream()
                .filter(p -> searchText.isEmpty() || p.getTitre().toLowerCase().contains(searchText))
                .filter(p -> statusFilter == null || statusFilter.equals("Tous") || p.getStatus().equals(statusFilter))
                .filter(p -> dateFilter == null || p.getDate().toLocalDate().equals(dateFilter))
                .filter(p -> selectedGoal == null || selectedGoal.getId() == -1 || p.getGoalId() == selectedGoal.getId())
                .collect(Collectors.toList());

        // Pagination
        totalPages = (int) Math.ceil((double) currentFilteredPlans.size() / itemsPerPage);
        if (totalPages == 0) totalPages = 1;
        if (currentPage > totalPages) currentPage = totalPages;

        int start = (currentPage - 1) * itemsPerPage;
        int end = Math.min(start + itemsPerPage, currentFilteredPlans.size());

        plansContainer.getChildren().clear();

        if (currentFilteredPlans.isEmpty()) {
            showEmptyState();
            if (lblPageInfo != null) {
                lblPageInfo.setText("Page 0 / 0");
            }
            updateStatistics();
            updateResultCount(0);
            return;
        }

        List<DailyPlan> pagePlans = currentFilteredPlans.subList(start, end);

        for (DailyPlan plan : pagePlans) {
            Node card = createPlanCard(plan);
            card.setOpacity(0);
            plansContainer.getChildren().add(card);
            FadeTransition ft = new FadeTransition(Duration.millis(300), card);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();
        }

        if (lblPageInfo != null) {
            lblPageInfo.setText("Page " + currentPage + " / " + totalPages);
        }

        updateStatistics();
        updateResultCount(currentFilteredPlans.size());
    }

    @FXML
    private void nextPage() {
        if (currentPage < totalPages) {
            currentPage++;
            loadPlansList();
        }
    }

    @FXML
    private void previousPage() {
        if (currentPage > 1) {
            currentPage--;
            loadPlansList();
        }
    }

    private void showEmptyState() {
        VBox emptyState = new VBox(15);
        emptyState.setAlignment(Pos.CENTER);
        emptyState.setPadding(new Insets(50));

        Label emojiLabel = new Label("📋");
        emojiLabel.setStyle("-fx-font-size: 64px;");

        Label messageLabel = new Label("Aucun plan d'entraînement");
        messageLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #94A3B8; -fx-font-weight: bold;");

        Label subMessageLabel = new Label("Créez votre premier plan en cliquant sur 'Nouveau Plan'");
        subMessageLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #CBD5E0;");

        emptyState.getChildren().addAll(emojiLabel, messageLabel, subMessageLabel);
        plansContainer.getChildren().add(emptyState);
    }

    private Node createPlanCard(DailyPlan plan) {
        HBox card = new HBox(20);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(15, 20, 15, 20));

        card.setStyle(
                "-fx-background-color: #FFFFFF; " +
                        "-fx-background-radius: 16; " +
                        "-fx-border-color: #E2E8F0; " +
                        "-fx-border-width: 1; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);"
        );

        card.setOnMouseEntered(e -> {
            card.setStyle(
                    "-fx-background-color: #FFFFFF; " +
                            "-fx-background-radius: 16; " +
                            "-fx-border-color: #CBD5E1; " +
                            "-fx-border-width: 1; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 12, 0, 0, 4);"
            );
            ScaleTransition st = new ScaleTransition(Duration.millis(200), card);
            st.setToX(1.02);
            st.setToY(1.02);
            st.play();
        });

        card.setOnMouseExited(e -> {
            card.setStyle(
                    "-fx-background-color: #FFFFFF; " +
                            "-fx-background-radius: 16; " +
                            "-fx-border-color: #E2E8F0; " +
                            "-fx-border-width: 1; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);"
            );
            ScaleTransition st = new ScaleTransition(Duration.millis(200), card);
            st.setToX(1);
            st.setToY(1);
            st.play();
        });

        VBox info = new VBox(5);
        Label lblTitre = new Label(plan.getTitre().toUpperCase());
        lblTitre.setStyle("-fx-text-fill: #0F172A; -fx-font-weight: bold; -fx-font-size: 15px;");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
        String formattedDate = plan.getDate().toLocalDate().format(formatter);
        Label lblDate = new Label("📅 " + formattedDate);
        lblDate.setStyle("-fx-text-fill: #64748B; -fx-font-size: 12px;");

        Goal goal = getGoalById(plan.getGoalId());
        if (goal != null) {
            Label lblGoal = new Label("🎯 " + goal.getTitle());
            lblGoal.setStyle("-fx-text-fill: #8B5CF6; -fx-font-size: 11px; -fx-font-weight: bold;");
            info.getChildren().addAll(lblTitre, lblDate, lblGoal);
        } else {
            info.getChildren().addAll(lblTitre, lblDate);
        }

        VBox exSection = new VBox(5);
        HBox.setHgrow(exSection, Priority.ALWAYS);

        Label exTitle = new Label("EXERCICES");
        exTitle.setStyle("-fx-text-fill: #0D9488; -fx-font-size: 10px; -fx-font-weight: bold; -fx-letter-spacing: 1px;");

        List<Exercise> exercises = dailyPlanDAO.getExercisesForPlan(plan.getId());
        String names = exercises.stream()
                .map(Exercise::getName)
                .limit(3)
                .collect(Collectors.joining(" • "));

        if (exercises.size() > 3) {
            names += " +" + (exercises.size() - 3) + " autres";
        }

        Label lblExList = new Label(names.isEmpty() ? "Aucun exercice" : names);
        lblExList.setStyle("-fx-text-fill: #475569; -fx-font-size: 12px; -fx-font-style: italic;");
        lblExList.setWrapText(true);

        exSection.getChildren().addAll(exTitle, lblExList);

        VBox rightSection = new VBox(12);
        rightSection.setAlignment(Pos.CENTER_RIGHT);

        HBox stats = new HBox(8);
        stats.getChildren().addAll(
                createBadge("⏱️ " + plan.getDureeMin() + " min", "#0284C7"),
                createBadge("🔥 " + plan.getCalories() + " cal", "#E11D48")
        );

        HBox btns = new HBox(8);

        Button btnEdit = createStyledButton("✏️ Modifier", "#F1F5F9", "#0F172A");
        btnEdit.setOnAction(e -> handleEdit(plan));

        Button btnDelete = createStyledButton("🗑️ Supprimer", "#FFF1F2", "#E11D48");
        btnDelete.setOnAction(e -> handleDelete(plan));

        btns.getChildren().addAll(btnEdit, btnDelete);
        rightSection.getChildren().addAll(stats, btns);

        card.getChildren().addAll(info, exSection, rightSection);
        return card;
    }

    private Button createStyledButton(String text, String bgColor, String textColor) {
        Button btn = new Button(text);
        btn.setStyle(
                "-fx-background-color: " + bgColor + ";" +
                        "-fx-text-fill: " + textColor + ";" +
                        "-fx-font-weight: bold;" +
                        "-fx-cursor: hand;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 6 12 6 12;" +
                        "-fx-font-size: 12px;"
        );

        btn.setOnMouseEntered(e -> {
            btn.setStyle(
                    "-fx-background-color: " + (bgColor.equals("#F1F5F9") ? "#E2E8F0" : "#FECDD3") + ";" +
                            "-fx-text-fill: " + textColor + ";" +
                            "-fx-font-weight: bold;" +
                            "-fx-cursor: hand;" +
                            "-fx-border-radius: 8;" +
                            "-fx-background-radius: 8;" +
                            "-fx-padding: 6 12 6 12;" +
                            "-fx-font-size: 12px;"
            );
            ScaleTransition st = new ScaleTransition(Duration.millis(150), btn);
            st.setToX(1.05);
            st.setToY(1.05);
            st.play();
        });

        btn.setOnMouseExited(e -> {
            btn.setStyle(
                    "-fx-background-color: " + bgColor + ";" +
                            "-fx-text-fill: " + textColor + ";" +
                            "-fx-font-weight: bold;" +
                            "-fx-cursor: hand;" +
                            "-fx-border-radius: 8;" +
                            "-fx-background-radius: 8;" +
                            "-fx-padding: 6 12 6 12;" +
                            "-fx-font-size: 12px;"
            );
            ScaleTransition st = new ScaleTransition(Duration.millis(150), btn);
            st.setToX(1);
            st.setToY(1);
            st.play();
        });

        return btn;
    }

    private void handleEdit(DailyPlan plan) {
        editingPlanId = plan.getId();
        txtTitre.setText(plan.getTitre());
        txtDuree.setText(String.valueOf(plan.getDureeMin()));
        txtCalories.setText(String.valueOf(plan.getCalories()));
        txtNotes.setText(plan.getNotes());
        datePicker.setValue(plan.getDate().toLocalDate());

        List<Integer> selectedExerciseIds = dailyPlanDAO.getExercisesForPlan(plan.getId())
                .stream()
                .map(Exercise::getId)
                .collect(Collectors.toList());

        for (CheckBox cb : exerciseCheckBoxes) {
            Exercise ex = (Exercise) cb.getUserData();
            cb.setSelected(selectedExerciseIds.contains(ex.getId()));
        }

        for (Goal g : comboGoals.getItems()) {
            if (g.getId() == plan.getGoalId()) {
                comboGoals.setValue(g);
                break;
            }
        }

        openForm();
    }

    private void handleDelete(DailyPlan plan) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer le plan");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer le plan \"" + plan.getTitre() + "\" ?");

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: white;");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                if (dailyPlanDAO.deleteDailyPlan(plan.getId())) {
                    loadPlansList();
                    showSuccessMessage("Plan supprimé avec succès!");
                }
            }
        });
    }

    @FXML
    public void openAddForm() {
        editingPlanId = -1;
        clearForm();
        openForm();
    }

    private void openForm() {
        if (formOverlay == null) return;
        formOverlay.setVisible(true);
        if (formFadeIn != null) formFadeIn.play();

        formOverlay.setScaleX(0.95);
        formOverlay.setScaleY(0.95);
        ScaleTransition st = new ScaleTransition(Duration.millis(200), formOverlay);
        st.setToX(1);
        st.setToY(1);
        st.play();
    }

    @FXML
    public void closeForm() {
        if (formOverlay == null || formFadeOut == null) return;
        formFadeOut.setOnFinished(e -> {
            formOverlay.setVisible(false);
            clearForm();
        });
        formFadeOut.play();
    }

    private void clearForm() {
        if (txtTitre != null) txtTitre.clear();
        if (txtDuree != null) txtDuree.clear();
        if (txtCalories != null) txtCalories.clear();
        if (txtNotes != null) txtNotes.clear();
        if (datePicker != null) datePicker.setValue(null);
        if (comboGoals != null) comboGoals.setValue(null);
        for (CheckBox cb : exerciseCheckBoxes) {
            cb.setSelected(false);
        }
    }

    private HBox createBadge(String text, String color) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold; -fx-font-size: 11px;");

        HBox container = new HBox(lbl);
        container.setStyle(
                "-fx-background-color: white; " +
                        "-fx-padding: 4 12 4 12; " +
                        "-fx-background-radius: 20; " +
                        "-fx-border-color: " + color + "66; " +
                        "-fx-border-width: 1.5;" +
                        "-fx-border-radius: 20;"
        );
        return container;
    }

    private void showSuccessMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void resetSearch() {
        if (searchPlan != null) {
            searchPlan.clear();
        }
        currentPage = 1;
        loadPlansList();
        showSuccessMessage("Recherche réinitialisée");
    }

    @FXML
    public void backToPlanner(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/wellcare/views/WorkoutPlanner.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);

            FadeTransition ft = new FadeTransition(Duration.millis(300), root);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de retourner au planner");
        }
    }
}