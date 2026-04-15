package org.example.controllers;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;
import org.example.dao.ExerciseDAO;
import org.example.models.Exercise;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class ExerciseLibraryController {

    @FXML private FlowPane cardsContainer;
    @FXML private VBox formOverlay;
    @FXML private TextField searchField, txtTitle, txtVideoUrl, txtDuration;
    @FXML private TextArea txtDescription;
    @FXML private ComboBox<String> sortComboBox, comboCategory, comboDifficulty;
    @FXML private CheckBox checkBeginner, checkIntermediate, checkAdvanced;
    @FXML private Label lblResultCount;
    @FXML private Button btnResetFilters;
    @FXML private ScrollPane cardsScrollPane;
    @FXML private Label lblError;

    private ExerciseDAO exerciseDAO = new ExerciseDAO();
    private ObservableList<Exercise> masterData = FXCollections.observableArrayList();
    private FilteredList<Exercise> filteredData;
    private Exercise selectedExercise = null;
    private boolean isEditMode = false;
    private String selectedCategory = "All";

    @FXML
    public void initialize() {
        setupUI();
        setupComboBoxes();
        setupListeners();
        loadExercises();
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
                    "-fx-background-color: white; -fx-background-radius: 25; -fx-border-radius: 25; " +
                            "-fx-border-color: #E2E8F0; -fx-border-width: 1px; -fx-padding: 10 15; -fx-font-size: 13px;"
            );
            searchField.setPromptText("🔍 Rechercher un exercice...");
        }

        String fieldStyle = "-fx-background-color: #F8FAFC; -fx-padding: 12; -fx-background-radius: 10; " +
                "-fx-border-radius: 10; -fx-border-color: #E2E8F0; -fx-border-width: 1px; -fx-font-size: 13px;";

        if (txtTitle != null) txtTitle.setStyle(fieldStyle);
        if (txtDescription != null) txtDescription.setStyle(fieldStyle);
        if (txtVideoUrl != null) txtVideoUrl.setStyle(fieldStyle);
        if (txtDuration != null) txtDuration.setStyle(fieldStyle);

        if (lblError != null) lblError.setStyle("-fx-text-fill: #EF4444; -fx-font-size: 12px;");
    }

    private void setupComboBoxes() {
        if (sortComboBox != null) {
            sortComboBox.setItems(FXCollections.observableArrayList("🏷️ Nom (A-Z)", "⏱️ Durée (Croissant)", "⏱️ Durée (Décroissant)"));
            sortComboBox.setValue("🏷️ Nom (A-Z)");
        }
        if (comboCategory != null) comboCategory.setItems(FXCollections.observableArrayList("🏃 Cardio", "💪 Musculation", "🧘 Yoga", "🤸 Étirements"));
        if (comboDifficulty != null) comboDifficulty.setItems(FXCollections.observableArrayList("🌱 Débutant", "⚡ Intermédiaire", "🔥 Avancé"));
    }

    private void setupListeners() {
        if (searchField != null) searchField.textProperty().addListener((obs, old, val) -> updateFilter());
        if (checkBeginner != null) checkBeginner.selectedProperty().addListener((obs, old, val) -> updateFilter());
        if (checkIntermediate != null) checkIntermediate.selectedProperty().addListener((obs, old, val) -> updateFilter());
        if (checkAdvanced != null) checkAdvanced.selectedProperty().addListener((obs, old, val) -> updateFilter());
        if (sortComboBox != null) sortComboBox.valueProperty().addListener((obs, old, val) -> applySort());
        if (btnResetFilters != null) btnResetFilters.setOnAction(e -> resetFilters());
    }

    private void loadExercises() {
        masterData.setAll(exerciseDAO.getAllExercises());
        filteredData = new FilteredList<>(masterData, p -> true);
        updateFilter();
    }

    private void updateFilter() {
        String search = searchField != null ? searchField.getText().toLowerCase().trim() : "";

        filteredData.setPredicate(ex -> {
            boolean matchesSearch = search.isEmpty() || ex.getName().toLowerCase().contains(search) ||
                    (ex.getDescription() != null && ex.getDescription().toLowerCase().contains(search));
            boolean matchesCat = selectedCategory.equals("All") || ex.getCategory().equalsIgnoreCase(selectedCategory);

            String diff = ex.getDifficultyLevel() != null ? ex.getDifficultyLevel() : "";
            boolean noneChecked = (checkBeginner == null || !checkBeginner.isSelected()) &&
                    (checkIntermediate == null || !checkIntermediate.isSelected()) &&
                    (checkAdvanced == null || !checkAdvanced.isSelected());
            boolean matchesDiff = noneChecked ||
                    (checkBeginner != null && checkBeginner.isSelected() && diff.equalsIgnoreCase("Beginner")) ||
                    (checkIntermediate != null && checkIntermediate.isSelected() && diff.equalsIgnoreCase("Intermediate")) ||
                    (checkAdvanced != null && checkAdvanced.isSelected() && diff.equalsIgnoreCase("Advanced"));

            return matchesSearch && matchesCat && matchesDiff;
        });
        applySort();
    }

    private void applySort() {
        SortedList<Exercise> sorted = new SortedList<>(filteredData);
        String sortVal = sortComboBox != null ? sortComboBox.getValue() : "🏷️ Nom (A-Z)";

        if ("🏷️ Nom (A-Z)".equals(sortVal)) sorted.setComparator(Comparator.comparing(Exercise::getName));
        else if ("⏱️ Durée (Croissant)".equals(sortVal)) sorted.setComparator(Comparator.comparingInt(Exercise::getDuration));
        else if ("⏱️ Durée (Décroissant)".equals(sortVal)) sorted.setComparator((e1, e2) -> Integer.compare(e2.getDuration(), e1.getDuration()));

        renderCards(sorted);
        updateResultCount();
    }

    private void renderCards(List<Exercise> exercises) {
        if (cardsContainer == null) return;
        cardsContainer.getChildren().clear();

        if (exercises.isEmpty()) { showEmptyState(); return; }

        for (Exercise ex : exercises) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ExerciseCard.fxml"));
                Node card = loader.load();
                ExerciseCardController controller = loader.getController();
                controller.setExerciseData(ex, this);
                cardsContainer.getChildren().add(card);
            } catch (IOException e) { e.printStackTrace(); }
        }
    }

    private void updateResultCount() {
        if (lblResultCount != null && filteredData != null) {
            lblResultCount.setText(filteredData.size() + " exercice(s) trouvé(s)");
        }
    }

    private void showEmptyState() {
        if (cardsContainer == null) return;
        VBox emptyState = new VBox(15);
        emptyState.setAlignment(Pos.CENTER);
        emptyState.setPadding(new Insets(50));
        emptyState.getChildren().addAll(
                new Label("📭") {{ setStyle("-fx-font-size: 64px;"); }},
                new Label("Aucun exercice trouvé") {{ setStyle("-fx-font-size: 18px; -fx-text-fill: #94A3B8; -fx-font-weight: bold;"); }},
                new Label("Essayez de modifier vos filtres ou d'ajouter un nouvel exercice") {{ setStyle("-fx-font-size: 13px; -fx-text-fill: #CBD5E0;"); }}
        );
        cardsContainer.getChildren().add(emptyState);
    }

    @FXML
    public void handleSave() {
        if (txtTitle == null || comboCategory == null || comboDifficulty == null || txtDuration == null) return;

        if (txtTitle.getText().trim().isEmpty()) { showError("Veuillez saisir un titre"); return; }
        if (comboCategory.getValue() == null) { showError("Veuillez sélectionner une catégorie"); return; }
        if (comboDifficulty.getValue() == null) { showError("Veuillez sélectionner une difficulté"); return; }
        if (txtDuration.getText().trim().isEmpty()) { showError("Veuillez saisir une durée"); return; }

        try {
            int dur = Integer.parseInt(txtDuration.getText());

            if (isEditMode && selectedExercise != null) {
                selectedExercise.setName(txtTitle.getText());
                selectedExercise.setDescription(txtDescription != null ? txtDescription.getText() : "");
                selectedExercise.setVideoUrl(txtVideoUrl != null ? txtVideoUrl.getText() : "");
                selectedExercise.setCategory(comboCategory.getValue().replaceAll("[🏃💪🧘🤸] ", ""));
                selectedExercise.setDifficultyLevel(comboDifficulty.getValue().replaceAll("[🌱⚡🔥] ", ""));
                selectedExercise.setDuration(dur);
                exerciseDAO.updateExercise(selectedExercise);
                showSuccessMessage("Exercice modifié avec succès!");
            } else {
                Exercise newEx = new Exercise(0, txtTitle.getText(), txtDescription != null ? txtDescription.getText() : "",
                        comboCategory.getValue().replaceAll("[🏃💪🧘🤸] ", ""),
                        comboDifficulty.getValue().replaceAll("[🌱⚡🔥] ", ""), "min",
                        txtVideoUrl != null ? txtVideoUrl.getText() : "", dur, 0, 0, 0);
                exerciseDAO.addExercise(newEx);
                showSuccessMessage("Exercice ajouté avec succès!");
            }
            loadExercises();
            closeForm();
        } catch (NumberFormatException e) { showError("La durée doit être un nombre valide"); }
        catch (Exception e) { showError("Erreur lors de l'enregistrement"); }
    }

    private void showSuccessMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        if (lblError != null) {
            lblError.setText(message);
            lblError.setVisible(true);
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText(message);
            alert.showAndWait();
        }
    }

    @FXML public void openAddForm() {
        isEditMode = false;
        selectedExercise = null;
        clearForm();
        if (formOverlay != null) formOverlay.setVisible(true);
    }

    public void openEditForm(Exercise ex) {
        isEditMode = true;
        selectedExercise = ex;
        if (txtTitle != null) txtTitle.setText(ex.getName());
        if (txtDescription != null) txtDescription.setText(ex.getDescription());
        if (txtVideoUrl != null) txtVideoUrl.setText(ex.getVideoUrl());
        if (txtDuration != null) txtDuration.setText(String.valueOf(ex.getDuration()));
        if (comboCategory != null) {
            String cat = ex.getCategory();
            if ("Cardio".equals(cat)) comboCategory.setValue("🏃 Cardio");
            else if ("Musculation".equals(cat)) comboCategory.setValue("💪 Musculation");
            else if ("Yoga".equals(cat)) comboCategory.setValue("🧘 Yoga");
            else if ("Étirements".equals(cat)) comboCategory.setValue("🤸 Étirements");
            else comboCategory.setValue(cat);
        }
        if (comboDifficulty != null) {
            String diff = ex.getDifficultyLevel();
            if ("Beginner".equals(diff)) comboDifficulty.setValue("🌱 Débutant");
            else if ("Intermediate".equals(diff)) comboDifficulty.setValue("⚡ Intermédiaire");
            else if ("Advanced".equals(diff)) comboDifficulty.setValue("🔥 Avancé");
            else comboDifficulty.setValue(diff);
        }
        if (formOverlay != null) formOverlay.setVisible(true);
    }

    @FXML public void closeForm() { if (formOverlay != null) formOverlay.setVisible(false); }

    private void clearForm() {
        if (txtTitle != null) txtTitle.clear();
        if (txtDescription != null) txtDescription.clear();
        if (txtVideoUrl != null) txtVideoUrl.clear();
        if (txtDuration != null) txtDuration.clear();
        if (comboCategory != null) comboCategory.setValue(null);
        if (comboDifficulty != null) comboDifficulty.setValue(null);
        if (lblError != null) lblError.setVisible(false);
    }

    public void deleteExercise(Exercise ex) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer l'exercice");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cet exercice ?");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                exerciseDAO.deleteExercise(ex.getId());
                loadExercises();
                showSuccessMessage("Exercice supprimé avec succès!");
            }
        });
    }

    @FXML public void filterByStrength() { selectedCategory = "Musculation"; updateFilter(); }
    @FXML public void filterByCardio() { selectedCategory = "Cardio"; updateFilter(); }
    @FXML public void filterByYoga() { selectedCategory = "Yoga"; updateFilter(); }
    @FXML public void filterAll() { selectedCategory = "All"; updateFilter(); }
    @FXML public void resetFilters() {
        if (searchField != null) searchField.clear();
        if (checkBeginner != null) checkBeginner.setSelected(false);
        if (checkIntermediate != null) checkIntermediate.setSelected(false);
        if (checkAdvanced != null) checkAdvanced.setSelected(false);
        selectedCategory = "All";
        if (sortComboBox != null) sortComboBox.setValue("🏷️ Nom (A-Z)");
        updateFilter();
        showSuccessMessage("Filtres réinitialisés");
    }
}