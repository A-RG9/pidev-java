package org.example.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
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
        searchField.textProperty().addListener((obs, old, newVal) -> updateFilter());
        sortComboBox.valueProperty().addListener((obs, old, newVal) -> applySort());
    }

    private void loadExercises() {
        masterData.setAll(exerciseDAO.getAllExercises());
        filteredData = new FilteredList<>(masterData, p -> true);
        renderCards(filteredData);
    }

    // CORRECTION MAVEN : Utilise java.util.List pour la compatibilité
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
            boolean matchesSearch = ex.getName().toLowerCase().contains(search);
            boolean matchesCat = selectedCategory.equals("All") || ex.getCategory().equalsIgnoreCase(selectedCategory);
            return matchesSearch && matchesCat;
        });
        applySort();
    }

    private void applySort() {
        SortedList<Exercise> sorted = new SortedList<>(filteredData);
        String sortVal = sortComboBox.getValue();
        if ("Nom (A-Z)".equals(sortVal)) sorted.setComparator(Comparator.comparing(Exercise::getName));
        else if ("Durée (Croissant)".equals(sortVal)) sorted.setComparator(Comparator.comparingInt(Exercise::getDuration));
        renderCards(sorted);
    }

    @FXML
    public void handleSave() {
        try {
            // Validation simple : Vérifier si les champs obligatoires sont remplis
            if (txtTitle.getText().isEmpty() || comboCategory.getValue() == null || comboDifficulty.getValue() == null) {
                System.err.println("Veuillez remplir tous les champs obligatoires");
                return;
            }

            int dur = Integer.parseInt(txtDuration.getText());

            if (isEditMode && selectedExercise != null) {
                // MISE À JOUR DE L'OBJET EXISTANT
                selectedExercise.setName(txtTitle.getText());
                selectedExercise.setDescription(txtDescription.getText());
                selectedExercise.setVideoUrl(txtVideoUrl.getText());
                selectedExercise.setCategory(comboCategory.getValue());
                selectedExercise.setDifficultyLevel(comboDifficulty.getValue()); // RÉCUPÉRATION COMBO
                selectedExercise.setDuration(dur);

                exerciseDAO.updateExercise(selectedExercise);
            } else {
                // CRÉATION NOUVEL OBJET
                Exercise newEx = new Exercise(
                        0,
                        txtTitle.getText(),
                        txtDescription.getText(),
                        comboCategory.getValue(),
                        comboDifficulty.getValue(), // RÉCUPÉRATION COMBO
                        "min",
                        txtVideoUrl.getText(),
                        dur,
                        0, 0, 0
                );
                exerciseDAO.addExercise(newEx);
            }

            loadExercises(); // Rafraîchir la liste
            closeForm();     // Fermer le formulaire
        } catch (NumberFormatException e) {
            System.err.println("La durée doit être un nombre");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML public void openAddForm() {
        isEditMode = false; selectedExercise = null;
        txtTitle.clear(); txtDescription.clear(); txtVideoUrl.clear(); txtDuration.clear();
        formOverlay.setVisible(true);
    }

    public void openEditForm(Exercise ex) {
        isEditMode = true; selectedExercise = ex;
        txtTitle.setText(ex.getName()); txtDescription.setText(ex.getDescription());
        comboCategory.setValue(ex.getCategory()); comboDifficulty.setValue(ex.getDifficultyLevel());
        formOverlay.setVisible(true);
    }

    @FXML public void closeForm() { formOverlay.setVisible(false); }

    // CORRECTION MAVEN : Doit être PUBLIC pour ExerciseCardController
    public void deleteExercise(Exercise ex) {
        exerciseDAO.deleteExercise(ex.getId());
        loadExercises();
    }

    @FXML public void filterByStrength() { selectedCategory = "Musculation"; updateFilter(); }
    @FXML public void filterByCardio() { selectedCategory = "Cardio"; updateFilter(); }
    @FXML public void filterAll() { selectedCategory = "All"; updateFilter(); }
}