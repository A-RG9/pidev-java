package org.example.controllers;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.example.dao.GoalDaoImpl;
import org.example.dao.IGoalDao;
import org.example.models.Goal;
import org.example.utils.Database;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class GoalController {

    private IGoalDao goalDao = new GoalDaoImpl();

    @FXML private FlowPane cardsContainer;
    @FXML private VBox formOverlay;
    @FXML private Label lblError;

    // Éléments optionnels (peuvent être null si non définis dans FXML)
    @FXML private Label lblResultCount;
    @FXML private TextField searchField;
    @FXML private Button btnResetSearch;
    @FXML private ScrollPane cardsScrollPane;
    @FXML private Label lblTotalGoals;
    @FXML private Label lblCompletedGoals;
    @FXML private Label lblInProgressGoals;
    @FXML private ProgressBar globalProgressBar;
    @FXML private Label lblProgressValue;
    @FXML private Button btnSave;
    @FXML private Button btnCancel;
    @FXML private ComboBox<String> filterCategory;
    @FXML private ComboBox<String> filterStatus;
    @FXML private ComboBox<String> filterDifficulty;
    @FXML private ComboBox<String> filterProgress;
    @FXML private Button btnResetFilters;

    // --- Champs du formulaire (obligatoires) ---
    @FXML private TextField txtTitle;
    @FXML private TextArea txtDescription;
    @FXML private ComboBox<String> comboCategory;
    @FXML private ComboBox<String> comboStatus;
    @FXML private ComboBox<String> comboDifficulty;
    @FXML private ComboBox<UserItem> comboPatient;
    @FXML private ComboBox<UserItem> comboCoach;
    @FXML private DatePicker dateStart;
    @FXML private DatePicker dateEnd;
    @FXML private Slider sliderProgress;
    @FXML private Label lblSliderProgressValue;

    // Variables
    private boolean isEditMode = false;
    private Goal selectedGoal = null;
    private FadeTransition formFadeIn;
    private FadeTransition formFadeOut;
    private ObservableList<Goal> masterData = FXCollections.observableArrayList();
    private FilteredList<Goal> filteredData;

    @FXML
    public void initialize() {
        setupUI();
        setupAnimations();
        setupComboBoxes();
        setupSlider();
        setupFilters();
        loadUsersIntoCombos();
        loadGoals();
        setupStatsPanel();
    }

    private void setupUI() {
        if (cardsContainer != null) {
            cardsContainer.setHgap(25);
            cardsContainer.setVgap(25);
            cardsContainer.setPadding(new Insets(25));
            cardsContainer.setAlignment(Pos.TOP_CENTER);
        }

        if (cardsScrollPane != null) {
            cardsScrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
            cardsScrollPane.setFitToWidth(true);
        }

        if (searchField != null) {
            searchField.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-background-radius: 25;" +
                            "-fx-border-radius: 25;" +
                            "-fx-border-color: #E2E8F0;" +
                            "-fx-border-width: 1px;" +
                            "-fx-padding: 10 15 10 15;" +
                            "-fx-font-size: 13px;" +
                            "-fx-prompt-text-fill: #94A3B8;"
            );
            searchField.setPromptText("🔍 Rechercher un objectif...");
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
        }

        String fieldStyle =
                "-fx-background-color: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-radius: 10;" +
                        "-fx-border-color: #E2E8F0;" +
                        "-fx-border-width: 1px;" +
                        "-fx-padding: 10;" +
                        "-fx-font-size: 13px;";

        if (txtTitle != null) txtTitle.setStyle(fieldStyle);
        if (txtDescription != null) txtDescription.setStyle(fieldStyle + " -fx-font-family: 'Segoe UI';");

        if (btnSave != null) {
            btnSave.setStyle(
                    "-fx-background-color: #14b8a6;" +
                            "-fx-text-fill: white;" +
                            "-fx-background-radius: 25;" +
                            "-fx-padding: 10 20 10 20;" +
                            "-fx-font-weight: bold;" +
                            "-fx-cursor: hand;" +
                            "-fx-font-size: 14px;"
            );
        }

        if (btnCancel != null) {
            btnCancel.setStyle(
                    "-fx-background-color: #F1F5F9;" +
                            "-fx-text-fill: #64748B;" +
                            "-fx-background-radius: 25;" +
                            "-fx-padding: 10 20 10 20;" +
                            "-fx-font-weight: bold;" +
                            "-fx-cursor: hand;" +
                            "-fx-font-size: 14px;"
            );
        }

        if (lblError != null) {
            lblError.setStyle("-fx-text-fill: #E11D48; -fx-font-size: 12px;");
            lblError.setVisible(false);
        }
    }

    private void setupFilters() {
        if (filterCategory != null) {
            filterCategory.setItems(FXCollections.observableArrayList(
                    "Toutes les catégories", "🍎 Nutrition", "🏃 Sport", "🧠 Mental", "📋 Général"
            ));
            filterCategory.setValue("Toutes les catégories");
            filterCategory.valueProperty().addListener((obs, old, val) -> applyFilters());
        }

        if (filterStatus != null) {
            filterStatus.setItems(FXCollections.observableArrayList(
                    "Tous les statuts", "🟢 En cours", "✅ Terminé", "⏳ En attente"
            ));
            filterStatus.setValue("Tous les statuts");
            filterStatus.valueProperty().addListener((obs, old, val) -> applyFilters());
        }

        if (filterDifficulty != null) {
            filterDifficulty.setItems(FXCollections.observableArrayList(
                    "Toutes les difficultés", "🌱 Facile", "⚡ Moyen", "🔥 Difficile"
            ));
            filterDifficulty.setValue("Toutes les difficultés");
            filterDifficulty.valueProperty().addListener((obs, old, val) -> applyFilters());
        }

        if (filterProgress != null) {
            filterProgress.setItems(FXCollections.observableArrayList(
                    "Tous", "0-25%", "26-50%", "51-75%", "76-100%"
            ));
            filterProgress.setValue("Tous");
            filterProgress.valueProperty().addListener((obs, old, val) -> applyFilters());
        }

        if (searchField != null) {
            searchField.textProperty().addListener((obs, old, val) -> applyFilters());
        }

        if (btnResetFilters != null) {
            btnResetFilters.setOnAction(e -> resetFilters());
        }
    }

    private void applyFilters() {
        if (filteredData == null) return;

        String searchText = searchField != null ? searchField.getText().toLowerCase() : "";
        String category = filterCategory != null ? filterCategory.getValue() : "Toutes les catégories";
        String status = filterStatus != null ? filterStatus.getValue() : "Tous les statuts";
        String difficulty = filterDifficulty != null ? filterDifficulty.getValue() : "Toutes les difficultés";
        String progressRange = filterProgress != null ? filterProgress.getValue() : "Tous";

        filteredData.setPredicate(goal -> {
            if (!searchText.isEmpty()) {
                boolean matchesSearch = goal.getTitle().toLowerCase().contains(searchText) ||
                        (goal.getDescription() != null && goal.getDescription().toLowerCase().contains(searchText));
                if (!matchesSearch) return false;
            }

            if (!category.equals("Toutes les catégories")) {
                String cleanCategory = category.replaceAll("[🍎🏃🧠📋] ", "");
                if (!goal.getCategory().equals(cleanCategory)) return false;
            }

            if (!status.equals("Tous les statuts")) {
                String cleanStatus = status.replaceAll("[🟢✅⏳] ", "");
                if (!goal.getStatus().equals(cleanStatus)) return false;
            }

            if (!difficulty.equals("Toutes les difficultés")) {
                String cleanDifficulty = difficulty.replaceAll("[🌱⚡🔥] ", "");
                if (!goal.getDifficultyLevel().equals(cleanDifficulty)) return false;
            }

            if (!progressRange.equals("Tous")) {
                int progress = goal.getProgress();
                switch (progressRange) {
                    case "0-25%":
                        if (progress < 0 || progress > 25) return false;
                        break;
                    case "26-50%":
                        if (progress < 26 || progress > 50) return false;
                        break;
                    case "51-75%":
                        if (progress < 51 || progress > 75) return false;
                        break;
                    case "76-100%":
                        if (progress < 76 || progress > 100) return false;
                        break;
                }
            }

            return true;
        });

        renderCards();
        updateResultCount();
    }

    @FXML
    public void resetFilters() {
        if (searchField != null) searchField.clear();
        if (filterCategory != null) filterCategory.setValue("Toutes les catégories");
        if (filterStatus != null) filterStatus.setValue("Tous les statuts");
        if (filterDifficulty != null) filterDifficulty.setValue("Toutes les difficultés");
        if (filterProgress != null) filterProgress.setValue("Tous");
        applyFilters();
        showSuccessMessage("Filtres réinitialisés");
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

        if (cardsContainer != null) {
            cardsContainer.setOpacity(0);
            TranslateTransition tt = new TranslateTransition(Duration.millis(400), cardsContainer);
            tt.setFromY(20);
            tt.setToY(0);
            FadeTransition ft = new FadeTransition(Duration.millis(400), cardsContainer);
            ft.setFromValue(0);
            ft.setToValue(1);
            tt.play();
            ft.play();
        }
    }

    private void setupComboBoxes() {
        if (comboCategory != null) {
            comboCategory.setItems(FXCollections.observableArrayList(
                    "🍎 Nutrition", "🏃 Sport", "🧠 Mental", "📋 Général"
            ));
            comboCategory.setValue("📋 Général");
        }

        if (comboStatus != null) {
            comboStatus.setItems(FXCollections.observableArrayList(
                    "🟢 En cours", "✅ Terminé", "⏳ En attente"
            ));
            comboStatus.setValue("🟢 En cours");
        }

        if (comboDifficulty != null) {
            comboDifficulty.setItems(FXCollections.observableArrayList(
                    "🌱 Facile", "⚡ Moyen", "🔥 Difficile"
            ));
            comboDifficulty.setValue("⚡ Moyen");
        }

        String comboStyle =
                "-fx-background-color: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-radius: 10;" +
                        "-fx-border-color: #E2E8F0;" +
                        "-fx-border-width: 1px;" +
                        "-fx-padding: 5 10 5 10;";

        if (comboCategory != null) comboCategory.setStyle(comboStyle);
        if (comboStatus != null) comboStatus.setStyle(comboStyle);
        if (comboDifficulty != null) comboDifficulty.setStyle(comboStyle);
        if (comboPatient != null) comboPatient.setStyle(comboStyle);
        if (comboCoach != null) comboCoach.setStyle(comboStyle);

        if (dateStart != null) dateStart.setStyle(comboStyle);
        if (dateEnd != null) dateEnd.setStyle(comboStyle);
    }

    private void setupSlider() {
        if (sliderProgress != null && lblSliderProgressValue != null) {
            sliderProgress.valueProperty().addListener((obs, old, newVal) -> {
                int progress = newVal.intValue();
                lblSliderProgressValue.setText(progress + "%");

                if (progress < 30) {
                    lblSliderProgressValue.setStyle("-fx-text-fill: #E11D48; -fx-font-weight: bold;");
                } else if (progress < 70) {
                    lblSliderProgressValue.setStyle("-fx-text-fill: #F59E0B; -fx-font-weight: bold;");
                } else {
                    lblSliderProgressValue.setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold;");
                }
            });
        }
    }

    private void setupStatsPanel() {
        updateGlobalStats();

        if (globalProgressBar != null) {
            globalProgressBar.setStyle("-fx-accent: #667eea;");
        }
    }

    private void loadUsersIntoCombos() {
        ObservableList<UserItem> users = FXCollections.observableArrayList();
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT uuid, email FROM users")) {
            while (rs.next()) {
                users.add(new UserItem(rs.getString("uuid"), rs.getString("email")));
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur lors du chargement des utilisateurs");
        }

        if (comboPatient != null) {
            comboPatient.setItems(users);
            comboPatient.setCellFactory(lv -> new ListCell<UserItem>() {
                @Override
                protected void updateItem(UserItem item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText("👤 " + item.toString());
                    }
                }
            });

            comboPatient.setButtonCell(new ListCell<UserItem>() {
                @Override
                protected void updateItem(UserItem item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText("Sélectionner un patient");
                    } else {
                        setText("👤 " + item.toString());
                    }
                }
            });
        }

        if (comboCoach != null) {
            comboCoach.setItems(users);
            comboCoach.setCellFactory(lv -> new ListCell<UserItem>() {
                @Override
                protected void updateItem(UserItem item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText("👨‍🏫 " + item.toString());
                    }
                }
            });

            comboCoach.setButtonCell(new ListCell<UserItem>() {
                @Override
                protected void updateItem(UserItem item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText("Sélectionner un coach");
                    } else {
                        setText("👨‍🏫 " + item.toString());
                    }
                }
            });
        }
    }

    private void loadGoals() {
        masterData.clear();
        masterData.addAll(goalDao.getAllGoals());
        filteredData = new FilteredList<>(masterData, p -> true);

        renderCards();
        updateGlobalStats();
        updateResultCount();
    }

    private void renderCards() {
        if (cardsContainer == null) return;

        cardsContainer.getChildren().clear();

        if (filteredData.isEmpty()) {
            showEmptyState();
            return;
        }

        for (Goal goal : filteredData) {
            addGoalCard(goal);
        }
    }

    private void addGoalCard(Goal goal) {
        if (cardsContainer == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GoalCard.fxml"));
            Node card = loader.load();

            GoalCardController controller = loader.getController();
            controller.setGoalData(goal, this);

            card.setOpacity(0);
            cardsContainer.getChildren().add(card);

            FadeTransition ft = new FadeTransition(Duration.millis(300), card);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur lors du chargement de la carte");
        }
    }

    private void updateResultCount() {
        if (lblResultCount != null) {
            int count = filteredData.size();
            String text = count + " objectif" + (count > 1 ? "s" : "") + " trouvé" + (count > 1 ? "s" : "");
            lblResultCount.setText(text);

            ScaleTransition st = new ScaleTransition(Duration.millis(300), lblResultCount);
            st.setToX(1.1);
            st.setToY(1.1);
            st.setCycleCount(2);
            st.setAutoReverse(true);
            st.play();
        }
    }

    private void showEmptyState() {
        if (cardsContainer == null) return;

        VBox emptyState = new VBox(15);
        emptyState.setAlignment(Pos.CENTER);
        emptyState.setPadding(new Insets(50));

        Label emojiLabel = new Label("🎯");
        emojiLabel.setStyle("-fx-font-size: 64px;");

        Label messageLabel = new Label("Aucun objectif");
        messageLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #94A3B8; -fx-font-weight: bold;");

        Label subMessageLabel = new Label("Créez votre premier objectif en cliquant sur 'Nouvel Objectif'");
        subMessageLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #CBD5E0;");

        emptyState.getChildren().addAll(emojiLabel, messageLabel, subMessageLabel);
        cardsContainer.getChildren().add(emptyState);
    }

    @FXML
    private void openAddForm() {
        isEditMode = false;
        selectedGoal = null;
        clearForm();
        openForm();
    }

    public void openEditForm(Goal goal) {
        this.selectedGoal = goal;
        this.isEditMode = true;

        if (txtTitle != null) txtTitle.setText(goal.getTitle());
        if (txtDescription != null) txtDescription.setText(goal.getDescription());

        if (comboCategory != null) {
            String category = goal.getCategory();
            switch(category) {
                case "Nutrition": comboCategory.setValue("🍎 Nutrition"); break;
                case "Sport": comboCategory.setValue("🏃 Sport"); break;
                case "Mental": comboCategory.setValue("🧠 Mental"); break;
                default: comboCategory.setValue("📋 Général");
            }
        }

        if (comboStatus != null) {
            String status = goal.getStatus();
            switch(status) {
                case "En cours": comboStatus.setValue("🟢 En cours"); break;
                case "Terminé": comboStatus.setValue("✅ Terminé"); break;
                default: comboStatus.setValue("⏳ En attente");
            }
        }

        if (comboDifficulty != null) {
            String difficulty = goal.getDifficultyLevel();
            switch(difficulty) {
                case "Facile": comboDifficulty.setValue("🌱 Facile"); break;
                case "Moyen": comboDifficulty.setValue("⚡ Moyen"); break;
                default: comboDifficulty.setValue("🔥 Difficile");
            }
        }

        if (sliderProgress != null) sliderProgress.setValue(goal.getProgress());
        if (dateStart != null) dateStart.setValue(goal.getStartDate());
        if (dateEnd != null) dateEnd.setValue(goal.getEndDate());

        if (comboPatient != null && goal.getPatientId() != null) {
            comboPatient.getItems().stream()
                    .filter(u -> u.getUuid().equals(goal.getPatientId()))
                    .findFirst()
                    .ifPresent(u -> comboPatient.setValue(u));
        }

        if (comboCoach != null && goal.getCoachId() != null) {
            comboCoach.getItems().stream()
                    .filter(u -> u.getUuid().equals(goal.getCoachId()))
                    .findFirst()
                    .ifPresent(u -> comboCoach.setValue(u));
        }

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

    public void handleDelete(Goal goal) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer l'objectif");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer l'objectif \"" + goal.getTitle() + "\" ?");

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: white;");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            goalDao.deleteGoal(goal.getId());
            loadGoals();
            showSuccessMessage("Objectif supprimé avec succès!");
        }
    }

    @FXML
    private void closeForm() {
        if (formOverlay == null) return;

        if (formFadeOut != null) {
            formFadeOut.setOnFinished(e -> {
                formOverlay.setVisible(false);
                clearForm();
            });
            formFadeOut.play();
        } else {
            formOverlay.setVisible(false);
            clearForm();
        }
    }

    private void clearForm() {
        if (txtTitle != null) txtTitle.clear();
        if (txtDescription != null) txtDescription.clear();
        if (dateStart != null) dateStart.setValue(null);
        if (dateEnd != null) dateEnd.setValue(null);
        if (sliderProgress != null) sliderProgress.setValue(0);
        if (comboCategory != null) comboCategory.setValue("📋 Général");
        if (comboStatus != null) comboStatus.setValue("🟢 En cours");
        if (comboDifficulty != null) comboDifficulty.setValue("⚡ Moyen");
        if (comboPatient != null) comboPatient.getSelectionModel().clearSelection();
        if (comboCoach != null) comboCoach.getSelectionModel().clearSelection();
        if (lblError != null) lblError.setVisible(false);
    }

    @FXML
    private void handleSave() {
        if (txtTitle == null || comboPatient == null || comboCoach == null) return;

        if (txtTitle.getText().trim().isEmpty()) {
            showFieldError("Le titre est obligatoire");
            return;
        }
        if (comboPatient.getValue() == null) {
            showFieldError("Veuillez sélectionner un patient");
            return;
        }
        if (comboCoach.getValue() == null) {
            showFieldError("Veuillez sélectionner un coach");
            return;
        }

        try {
            String title = txtTitle.getText().trim();
            String description = txtDescription != null ? txtDescription.getText() : "";
            String patientId = comboPatient.getValue().getUuid();
            String coachId = comboCoach.getValue().getUuid();

            String category = comboCategory != null ? comboCategory.getValue().replaceAll("[🍎🏃🧠📋] ", "") : "Général";
            String status = comboStatus != null ? comboStatus.getValue().replaceAll("[🟢✅⏳] ", "") : "En cours";
            String difficulty = comboDifficulty != null ? comboDifficulty.getValue().replaceAll("[🌱⚡🔥] ", "") : "Moyen";

            int progress = sliderProgress != null ? (int) sliderProgress.getValue() : 0;
            LocalDate startDate = dateStart != null ? dateStart.getValue() : null;
            LocalDate endDate = dateEnd != null ? dateEnd.getValue() : null;

            if (isEditMode && selectedGoal != null) {
                selectedGoal.setTitle(title);
                selectedGoal.setDescription(description);
                selectedGoal.setPatientId(patientId);
                selectedGoal.setCoachId(coachId);
                selectedGoal.setCategory(category);
                selectedGoal.setStatus(status);
                selectedGoal.setDifficultyLevel(difficulty);
                selectedGoal.setProgress(progress);
                selectedGoal.setStartDate(startDate);
                selectedGoal.setEndDate(endDate);

                goalDao.updateGoal(selectedGoal);
                showSuccessMessage("Objectif modifié avec succès!");
                closeForm();
                loadGoals();
            } else {
                Goal newGoal = new Goal(
                        0, title, description, patientId, coachId,
                        category, status, difficulty, progress,
                        startDate, endDate
                );

                goalDao.addGoal(newGoal);
                showSuccessMessage("Objectif ajouté avec succès!");
                closeForm();
                loadGoals();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Une erreur est survenue: " + e.getMessage());
        }
    }

    private void showFieldError(String message) {
        if (lblError == null) return;

        lblError.setText("❌ " + message);
        lblError.setVisible(true);
        lblError.setManaged(true);

        ScaleTransition st = new ScaleTransition(Duration.millis(200), lblError);
        st.setToX(1.05);
        st.setToY(1.05);
        st.setCycleCount(2);
        st.setAutoReverse(true);
        st.play();

        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(Duration.seconds(3));
        pause.setOnFinished(e -> lblError.setVisible(false));
        pause.play();
    }

    private void showSuccessMessage(String message) {
        // Utiliser une alerte simple au lieu d'un toast problématique
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: white;");

        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: white;");

        alert.showAndWait();
    }

    private void updateGlobalStats() {
        List<Goal> goals = goalDao.getAllGoals();
        long total = goals.size();
        long completed = goals.stream()
                .filter(g -> "Terminé".equals(g.getStatus()))
                .count();
        long inProgress = goals.stream()
                .filter(g -> "En cours".equals(g.getStatus()))
                .count();

        if (lblTotalGoals != null) {
            lblTotalGoals.setText(String.valueOf(total));
        }
        if (lblCompletedGoals != null) {
            lblCompletedGoals.setText(String.valueOf(completed));
        }
        if (lblInProgressGoals != null) {
            lblInProgressGoals.setText(String.valueOf(inProgress));
        }
        if (globalProgressBar != null && total > 0) {
            double progress = (double) completed / total;
            globalProgressBar.setProgress(progress);
            if (lblProgressValue != null) {
                lblProgressValue.setText(Math.round(progress * 100) + "%");
            }
        }
    }

    @FXML
    private void refreshGoals() {
        loadGoals();
        showSuccessMessage("Liste rafraîchie");
    }

    // Classe interne pour gérer les ComboBox d'utilisateurs
    public static class UserItem {
        private String uuid;
        private String email;

        public UserItem(String uuid, String email) {
            this.uuid = uuid;
            this.email = email;
        }

        public String getUuid() {
            return uuid;
        }

        @Override
        public String toString() {
            return email;
        }
    }
}