package org.example.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import org.example.dao.ExerciseDAO;
import org.example.models.Exercise;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;

public class ExerciseLibraryController {

    @FXML private FlowPane cardsContainer;
    @FXML private VBox formOverlay;
    @FXML private TextField searchField, txtTitle, txtVideoUrl, txtDuration;
    @FXML private TextArea txtDescription;
    @FXML private ComboBox<String> sortComboBox, comboCategory, comboDifficulty;

    // --- LIAISON FILTRES DIFFICULTÉ ---
    @FXML private CheckBox checkBeginner;
    @FXML private CheckBox checkIntermediate;
    @FXML private CheckBox checkAdvanced;

    private ExerciseDAO exerciseDAO = new ExerciseDAO();
    private ObservableList<Exercise> masterData = FXCollections.observableArrayList();
    private FilteredList<Exercise> filteredData;
    private Exercise selectedExercise = null;
    private boolean isEditMode = false;
    private String selectedCategory = "All";

    @FXML
    public void initialize() {
        sortComboBox.setItems(FXCollections.observableArrayList("Nom (A-Z)", "Durée (Croissant)", "Durée (Décroissant)"));
        comboCategory.setItems(FXCollections.observableArrayList("Cardio", "Musculation", "Yoga", "Étirements"));
        comboDifficulty.setItems(FXCollections.observableArrayList("Beginner", "Intermediate", "Advanced"));

        loadExercises();

        // Listeners Recherche et CheckBoxes
        searchField.textProperty().addListener((obs, old, newVal) -> updateFilter());
        checkBeginner.selectedProperty().addListener((obs, old, newVal) -> updateFilter());
        checkIntermediate.selectedProperty().addListener((obs, old, newVal) -> updateFilter());
        checkAdvanced.selectedProperty().addListener((obs, old, newVal) -> updateFilter());

        sortComboBox.valueProperty().addListener((obs, old, newVal) -> applySort());
    }

    private void loadExercises() {
        masterData.setAll(exerciseDAO.getAllExercises());
        filteredData = new FilteredList<>(masterData, p -> true);
        updateFilter();
    }

    private void renderCards(List<Exercise> exercises) {
        cardsContainer.getChildren().clear();
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

    private void updateFilter() {
        String search = searchField.getText().toLowerCase();
        filteredData.setPredicate(ex -> {
            // 1. Filtre Recherche
            boolean matchesSearch = ex.getName().toLowerCase().contains(search);

            // 2. Filtre Catégorie
            boolean matchesCat = selectedCategory.equals("All") || ex.getCategory().equalsIgnoreCase(selectedCategory);

            // 3. Filtre Difficulté
            String diff = (ex.getDifficultyLevel() != null) ? ex.getDifficultyLevel() : "";
            boolean noneChecked = !checkBeginner.isSelected() && !checkIntermediate.isSelected() && !checkAdvanced.isSelected();

            boolean matchesDiff = noneChecked ||
                    (checkBeginner.isSelected() && diff.equalsIgnoreCase("Beginner")) ||
                    (checkIntermediate.isSelected() && diff.equalsIgnoreCase("Intermediate")) ||
                    (checkAdvanced.isSelected() && diff.equalsIgnoreCase("Advanced"));

            return matchesSearch && matchesCat && matchesDiff;
        });
        applySort();
    }

    private void applySort() {
        SortedList<Exercise> sorted = new SortedList<>(filteredData);
        String sortVal = sortComboBox.getValue();
        if ("Nom (A-Z)".equals(sortVal)) sorted.setComparator(Comparator.comparing(Exercise::getName));
        else if ("Durée (Croissant)".equals(sortVal)) sorted.setComparator(Comparator.comparingInt(Exercise::getDuration));
        else if ("Durée (Décroissant)".equals(sortVal)) sorted.setComparator((e1, e2) -> Integer.compare(e2.getDuration(), e1.getDuration()));
        renderCards(sorted);
    }

    @FXML
    public void handleSave() {
        try {
            if (txtTitle.getText().isEmpty() || comboCategory.getValue() == null || comboDifficulty.getValue() == null) {
                return;
            }
            int dur = Integer.parseInt(txtDuration.getText());
            if (isEditMode && selectedExercise != null) {
                selectedExercise.setName(txtTitle.getText());
                selectedExercise.setDescription(txtDescription.getText());
                selectedExercise.setVideoUrl(txtVideoUrl.getText());
                selectedExercise.setCategory(comboCategory.getValue());
                selectedExercise.setDifficultyLevel(comboDifficulty.getValue());
                selectedExercise.setDuration(dur);
                exerciseDAO.updateExercise(selectedExercise);
            } else {
                Exercise newEx = new Exercise(0, txtTitle.getText(), txtDescription.getText(),
                        comboCategory.getValue(), comboDifficulty.getValue(), "min", txtVideoUrl.getText(), dur, 0, 0, 0);
                exerciseDAO.addExercise(newEx);
            }
            loadExercises();
            closeForm();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML public void openAddForm() {
        isEditMode = false; selectedExercise = null;
        txtTitle.clear(); txtDescription.clear(); txtVideoUrl.clear(); txtDuration.clear();
        formOverlay.setVisible(true);
    }

    public void openEditForm(Exercise ex) {
        isEditMode = true; selectedExercise = ex;
        txtTitle.setText(ex.getName()); txtDescription.setText(ex.getDescription());
        txtVideoUrl.setText(ex.getVideoUrl()); txtDuration.setText(String.valueOf(ex.getDuration()));
        comboCategory.setValue(ex.getCategory()); comboDifficulty.setValue(ex.getDifficultyLevel());
        formOverlay.setVisible(true);
    }

    @FXML public void closeForm() { formOverlay.setVisible(false); }

    public void deleteExercise(Exercise ex) {
        exerciseDAO.deleteExercise(ex.getId());
        loadExercises();
    }

    @FXML public void filterByStrength() { selectedCategory = "Musculation"; updateFilter(); }
    @FXML public void filterByCardio() { selectedCategory = "Cardio"; updateFilter(); }
    @FXML public void filterAll() { selectedCategory = "All"; updateFilter(); }
}