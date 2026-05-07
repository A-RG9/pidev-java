package com.wellcare.javafx.controller;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import com.wellcare.javafx.dao.ExerciseDAO;
import com.wellcare.javafx.model.Exercise;
import com.wellcare.javafx.util.VideoUploadService;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;

public class ExerciseLibraryController {

    @FXML
    private FlowPane cardsContainer;
    @FXML
    private VBox formOverlay;
    @FXML
    private TextField searchField, txtTitle, txtVideoUrl, txtDuration;
    @FXML
    private TextArea txtDescription;
    @FXML
    private ComboBox<String> sortComboBox, comboCategory, comboDifficulty;
    @FXML
    private CheckBox checkBeginner, checkIntermediate, checkAdvanced;
    @FXML
    private Label lblResultCount;
    @FXML
    private Button btnResetFilters;
    @FXML
    private ScrollPane cardsScrollPane;
    @FXML
    private Label lblError;
    @FXML
    private StackPane mainStackPane;

    // Composants upload vidéo
    @FXML
    private RadioButton radioYouTube, radioUpload;
    @FXML
    private VBox uploadVideoContainer;
    @FXML
    private Button btnChooseVideo;
    @FXML
    private Label lblSelectedVideo;
    @FXML
    private ProgressBar uploadProgress;
    @FXML
    private Label lblUploadStatus;
    @FXML
    private MediaView videoPreview;

    // NOUVEAUX COMPOSANTS POUR LE FILTRE SOURCE
    @FXML
    private ToggleGroup sourceToggleGroup;
    @FXML
    private RadioButton radioAllExercises;
    @FXML
    private RadioButton radioLocalExercises;
    @FXML
    private RadioButton radioApiExercises;
    @FXML
    private Label lblLocalCount;
    @FXML
    private Label lblApiCount;

    private ExerciseDAO exerciseDAO = new ExerciseDAO();
    private ObservableList<Exercise> masterData = FXCollections.observableArrayList();
    private FilteredList<Exercise> filteredData;
    private Exercise selectedExercise = null;
    private boolean isEditMode = false;
    private String selectedCategory = "All";
    private File selectedVideoFile = null;
    private FadeTransition formFadeIn;
    private FadeTransition formFadeOut;

    // Pagination
    private int currentPage = 0;
    private static final int PAGE_SIZE = 20;
    private List<Exercise> currentSortedList = new java.util.ArrayList<>();

    @FXML
    public void initialize() {
        setupUIComponents();
        setupComboBoxes();
        setupListeners();
        setupVideoSourceToggle();
        setupAnimations();
        setupSourceFilters();  // NOUVEAU
        loadExercises();
    }

    // NOUVELLE MÉTHODE POUR LES FILTRES SOURCE
    private void setupSourceFilters() {
        if (radioAllExercises != null) {
            radioAllExercises.setSelected(true);

            radioAllExercises.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) updateFilter();
            });
            radioLocalExercises.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) updateFilter();
            });
            radioApiExercises.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) updateFilter();
            });
        }
    }

    // NOUVELLE MÉTHODE POUR DÉTECTER LES EXERCICES API
    private boolean isApiExercise(Exercise exercise) {
        // Détecter les exercices importés de WorkoutX API par leur vidéo GIF
        String videoUrl = exercise.getVideoUrl();
        if (videoUrl != null && (videoUrl.contains("/uploads/gifs/") || videoUrl.contains("/gifs/"))) {
            return true;
        }
        // Vérifier aussi par le nom du fichier vidéo
        if (videoUrl != null && videoUrl.endsWith(".gif")) {
            return true;
        }
        return false;
    }

    // MÉTHODE MODIFIÉE POUR METTRE À JOUR LES COMPTEURS
    private void updateSourceCounts() {
        if (lblLocalCount != null && lblApiCount != null) {
            long localCount = masterData.stream()
                    .filter(ex -> !isApiExercise(ex))
                    .count();

            long apiCount = masterData.stream()
                    .filter(this::isApiExercise)
                    .count();

            lblLocalCount.setText("(" + localCount + ")");
            lblApiCount.setText("(" + apiCount + ")");
        }
    }

    private void setupVideoSourceToggle() {
        if (radioYouTube != null && radioUpload != null) {
            radioYouTube.selectedProperty().addListener((obs, old, newVal) -> {
                txtVideoUrl.setVisible(newVal);
                txtVideoUrl.setManaged(newVal);
                uploadVideoContainer.setVisible(!newVal);
                uploadVideoContainer.setManaged(!newVal);
                if (newVal) {
                    selectedVideoFile = null;
                    lblSelectedVideo.setText("Aucun fichier sélectionné");
                    if (videoPreview != null) videoPreview.setVisible(false);
                }
            });

            radioUpload.selectedProperty().addListener((obs, old, newVal) -> {
                uploadVideoContainer.setVisible(newVal);
                uploadVideoContainer.setManaged(newVal);
                txtVideoUrl.setVisible(!newVal);
                txtVideoUrl.setManaged(!newVal);
            });
        }
    }

    @FXML
    private void chooseVideoFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une vidéo");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Vidéos", "*.mp4", "*.avi", "*.mov", "*.mkv", "*.webm"),
                new FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
        );

        selectedVideoFile = fileChooser.showOpenDialog(null);

        if (selectedVideoFile != null) {
            lblSelectedVideo.setText(selectedVideoFile.getName());
            try {
                Media media = new Media(selectedVideoFile.toURI().toString());
                MediaPlayer mediaPlayer = new MediaPlayer(media);
                videoPreview.setMediaPlayer(mediaPlayer);
                videoPreview.setVisible(true);
                mediaPlayer.setAutoPlay(false);
            } catch (Exception e) {
                System.out.println("Impossible de prévisualiser: " + e.getMessage());
                videoPreview.setVisible(false);
            }
        }
    }

    private void uploadVideoFile(Exercise exercise, Runnable onSuccess) {
        if (selectedVideoFile == null) {
            onSuccess.run();
            return;
        }

        uploadProgress.setVisible(true);
        lblUploadStatus.setText("Upload en cours...");

        Task<String> uploadTask = new Task<>() {
            @Override
            protected String call() throws Exception {
                for (int i = 0; i <= 100; i += 20) {
                    Thread.sleep(50);
                    updateProgress(i, 100);
                    updateMessage(i + "%");
                }
                return VideoUploadService.saveLocalVideo(selectedVideoFile, exercise.getName());
            }

            @Override
            protected void updateMessage(String message) {
                super.updateMessage(message);
                Platform.runLater(() -> lblUploadStatus.setText(message));
            }

            @Override
            protected void updateProgress(double workDone, double max) {
                super.updateProgress(workDone, max);
                Platform.runLater(() -> uploadProgress.setProgress(workDone / max));
            }
        };

        uploadTask.setOnSucceeded(event -> {
            String localVideoPath = uploadTask.getValue();
            exercise.setVideoUrl(localVideoPath);
            uploadProgress.setVisible(false);
            lblUploadStatus.setText("✅ Vidéo uploadée avec succès!");
            onSuccess.run();
        });

        uploadTask.setOnFailed(event -> {
            uploadProgress.setVisible(false);
            lblUploadStatus.setText("❌ Erreur lors de l'upload");
            onSuccess.run();
        });

        new Thread(uploadTask).start();
    }

    private void setupUIComponents() {
        cardsContainer.setHgap(25);
        cardsContainer.setVgap(25);
        cardsContainer.setPadding(new Insets(25));
        cardsContainer.setAlignment(Pos.TOP_CENTER);

        if (cardsScrollPane != null) {
            cardsScrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
            cardsScrollPane.setFitToWidth(true);
        }

        searchField.setStyle(
                "-fx-background-color: white; -fx-background-radius: 25; -fx-border-radius: 25; " +
                        "-fx-border-color: #E2E8F0; -fx-border-width: 1px; -fx-padding: 10 15; -fx-font-size: 13px;"
        );
        searchField.setPromptText("🔍 Rechercher un exercice...");

        if (btnResetFilters != null) {
            btnResetFilters.setStyle(
                    "-fx-background-color: #EF4444; -fx-text-fill: white; -fx-background-radius: 20; " +
                            "-fx-padding: 8 16; -fx-font-weight: bold; -fx-cursor: hand;"
            );
        }

        String fieldStyle = "-fx-background-color: #F8FAFC; -fx-padding: 12; -fx-background-radius: 10; " +
                "-fx-border-radius: 10; -fx-border-color: #E2E8F0; -fx-border-width: 1px; -fx-font-size: 13px;";

        txtTitle.setStyle(fieldStyle);
        txtDescription.setStyle(fieldStyle);
        txtVideoUrl.setStyle(fieldStyle);
        txtDuration.setStyle(fieldStyle);
    }

    private void setupComboBoxes() {
        sortComboBox.setItems(FXCollections.observableArrayList("🏷️ Nom (A-Z)", "⏱️ Durée (Croissant)", "⏱️ Durée (Décroissant)"));
        sortComboBox.setValue("🏷️ Nom (A-Z)");
        comboCategory.setItems(FXCollections.observableArrayList("🏃 Cardio", "💪 Musculation", "🧘 Yoga", "🤸 Étirements"));
        comboDifficulty.setItems(FXCollections.observableArrayList("🌱 Débutant", "⚡ Intermédiaire", "🔥 Avancé"));

        String comboStyle = "-fx-background-color: white; -fx-background-radius: 20; -fx-border-radius: 20; " +
                "-fx-border-color: #E2E8F0; -fx-border-width: 1px; -fx-padding: 5 10;";
        sortComboBox.setStyle(comboStyle);
        comboCategory.setStyle(comboStyle);
        comboDifficulty.setStyle(comboStyle);
    }

    private void setupListeners() {
        searchField.textProperty().addListener((obs, old, val) -> updateFilter());
        checkBeginner.selectedProperty().addListener((obs, old, val) -> updateFilter());
        checkIntermediate.selectedProperty().addListener((obs, old, val) -> updateFilter());
        checkAdvanced.selectedProperty().addListener((obs, old, val) -> updateFilter());
        sortComboBox.valueProperty().addListener((obs, old, val) -> applySort());
        if (btnResetFilters != null) btnResetFilters.setOnAction(e -> resetFilters());
    }

    private void setupAnimations() {
        formFadeIn = new FadeTransition(Duration.millis(300), formOverlay);
        formFadeIn.setFromValue(0);
        formFadeIn.setToValue(1);
        formFadeOut = new FadeTransition(Duration.millis(200), formOverlay);
        formFadeOut.setFromValue(1);
        formFadeOut.setToValue(0);
    }

    // MÉTHODE MODIFIÉE
    private void loadExercises() {
        masterData.setAll(exerciseDAO.getAllExercises());
        filteredData = new FilteredList<>(masterData, p -> true);
        updateSourceCounts();  // NOUVEAU
        updateFilter();
    }

    // MÉTHODE MODIFIÉE AVEC FILTRE SOURCE
    private void updateFilter() {
        String search = searchField.getText().toLowerCase().trim();

        filteredData.setPredicate(ex -> {
            // NOUVEAU FILTRE SOURCE
            if (radioLocalExercises != null && radioLocalExercises.isSelected() && isApiExercise(ex)) {
                return false;
            }
            if (radioApiExercises != null && radioApiExercises.isSelected() && !isApiExercise(ex)) {
                return false;
            }

            boolean matchesSearch = search.isEmpty() || ex.getName().toLowerCase().contains(search) ||
                    (ex.getDescription() != null && ex.getDescription().toLowerCase().contains(search));
            boolean matchesCat = selectedCategory.equals("All") || ex.getCategory().equalsIgnoreCase(selectedCategory);
            String diff = ex.getDifficultyLevel() != null ? ex.getDifficultyLevel() : "";
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
        if ("🏷️ Nom (A-Z)".equals(sortVal)) sorted.setComparator(Comparator.comparing(Exercise::getName));
        else if ("⏱️ Durée (Croissant)".equals(sortVal))
            sorted.setComparator(Comparator.comparingInt(Exercise::getDuration));
        else if ("⏱️ Durée (Décroissant)".equals(sortVal))
            sorted.setComparator((e1, e2) -> Integer.compare(e2.getDuration(), e1.getDuration()));
        renderCards(sorted);
        updateResultCount();
    }

    private void renderCards(List<Exercise> exercises) {
        currentSortedList = new java.util.ArrayList<>(exercises);
        currentPage = 0;
        renderCurrentPage();
        updateResultCount();
    }

    private void renderCurrentPage() {
        cardsContainer.getChildren().clear();

        if (currentSortedList.isEmpty()) {
            showEmptyState();
            return;
        }

        int start = currentPage * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, currentSortedList.size());
        int totalPages = (int) Math.ceil((double) currentSortedList.size() / PAGE_SIZE);

        for (int i = start; i < end; i++) {
            Exercise ex = currentSortedList.get(i);
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ExerciseCard.fxml"));
                Node card = loader.load();
                ExerciseCardController controller = loader.getController();
                controller.setExerciseData(ex, this);
                cardsContainer.getChildren().add(card);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Ajouter les controles de pagination
        javafx.scene.layout.HBox paginationBox = new javafx.scene.layout.HBox(10);
        paginationBox.setAlignment(Pos.CENTER);
        paginationBox.setPadding(new Insets(15, 0, 5, 0));

        Button btnPrev = new Button("◀ Précédent");
        btnPrev.setStyle("-fx-background-color: #F1F5F9; -fx-text-fill: #475569; -fx-background-radius: 8; -fx-padding: 8 16; -fx-cursor: hand;");
        btnPrev.setDisable(currentPage == 0);
        btnPrev.setOnAction(e -> { currentPage--; renderCurrentPage(); if (cardsScrollPane != null) cardsScrollPane.setVvalue(0); });

        Label pageLabel = new Label("Page " + (currentPage + 1) + " / " + totalPages + "  (" + currentSortedList.size() + " exercices)");
        pageLabel.setStyle("-fx-text-fill: #64748B; -fx-font-size: 13px;");

        Button btnNext = new Button("Suivant ▶");
        btnNext.setStyle("-fx-background-color: #F1F5F9; -fx-text-fill: #475569; -fx-background-radius: 8; -fx-padding: 8 16; -fx-cursor: hand;");
        btnNext.setDisable(currentPage >= totalPages - 1);
        btnNext.setOnAction(e -> { currentPage++; renderCurrentPage(); if (cardsScrollPane != null) cardsScrollPane.setVvalue(0); });

        paginationBox.getChildren().addAll(btnPrev, pageLabel, btnNext);
        cardsContainer.getChildren().add(paginationBox);
    }

    private void updateResultCount() {
        if (lblResultCount != null && filteredData != null) {
            lblResultCount.setText(filteredData.size() + " exercice(s) trouvé(s)");
        }
    }

    private void showEmptyState() {
        VBox emptyState = new VBox(15);
        emptyState.setAlignment(Pos.CENTER);
        emptyState.setPadding(new Insets(50));
        emptyState.getChildren().addAll(
                new Label("📭") {{
                    setStyle("-fx-font-size: 64px;");
                }},
                new Label("Aucun exercice trouvé") {{
                    setStyle("-fx-font-size: 18px; -fx-text-fill: #94A3B8; -fx-font-weight: bold;");
                }},
                new Label("Essayez de modifier vos filtres ou d'ajouter un nouvel exercice") {{
                    setStyle("-fx-font-size: 13px; -fx-text-fill: #CBD5E0;");
                }}
        );
        cardsContainer.getChildren().add(emptyState);
    }

    @FXML
    public void handleSave() {
        if (txtTitle.getText().trim().isEmpty()) {
            showError("Veuillez saisir un titre");
            return;
        }
        if (comboCategory.getValue() == null) {
            showError("Veuillez sélectionner une catégorie");
            return;
        }
        if (comboDifficulty.getValue() == null) {
            showError("Veuillez sélectionner une difficulté");
            return;
        }
        if (txtDuration.getText().trim().isEmpty()) {
            showError("Veuillez saisir une durée");
            return;
        }

        String videoUrl = null;
        if (radioYouTube.isSelected()) {
            if (txtVideoUrl.getText().trim().isEmpty()) {
                showError("Veuillez saisir une URL YouTube");
                return;
            }
            videoUrl = txtVideoUrl.getText().trim();
            if (!VideoUploadService.isValidYouTubeUrl(videoUrl)) {
                showError("URL YouTube invalide");
                return;
            }
        }

        try {
            int dur = Integer.parseInt(txtDuration.getText());

            Exercise exercise = new Exercise(
                    txtTitle.getText(),
                    txtDescription.getText(),
                    comboCategory.getValue().replaceAll("[🏃💪🧘🤸] ", ""),
                    comboDifficulty.getValue().replaceAll("[🌱⚡🔥] ", ""),
                    "min",
                    videoUrl,
                    dur,
                    0, 0, 0
            );

            if (isEditMode && selectedExercise != null) {
                if (selectedExercise.getVideoUrl() != null && selectedExercise.getVideoUrl().startsWith("uploads/")) {
                    VideoUploadService.deleteLocalVideo(selectedExercise.getVideoUrl());
                }

                if (radioYouTube.isSelected()) {
                    exercise.setVideoUrl(videoUrl);
                    exercise.setId(selectedExercise.getId());
                    exerciseDAO.updateExercise(exercise);
                    showSuccessMessage("Exercice modifié avec succès!");
                    loadExercises();
                    closeForm();
                } // Dans le cas upload vidéo
                else if (radioUpload.isSelected() && selectedVideoFile != null) {
                    uploadVideoFile(exercise, () -> {
                        // Vérifier que le chemin est relatif
                        System.out.println("🔍 Chemin vidéo sauvegardé: " + exercise.getVideoUrl());

                        if (isEditMode) {
                            exerciseDAO.updateExercise(exercise);
                        } else {
                            exerciseDAO.addExercise(exercise);
                        }
                        loadExercises();
                        closeForm();
                        showSuccessMessage("Exercice " + (isEditMode ? "modifié" : "ajouté") + " avec succès!");
                    });
                    return;
                } else if (radioUpload.isSelected() && selectedVideoFile == null) {
                    exercise.setVideoUrl(selectedExercise.getVideoUrl());
                    exercise.setId(selectedExercise.getId());
                    exerciseDAO.updateExercise(exercise);
                    showSuccessMessage("Exercice modifié avec succès!");
                    loadExercises();
                    closeForm();
                }
            } else {
                if (radioYouTube.isSelected()) {
                    exercise.setVideoUrl(videoUrl);
                    exerciseDAO.addExercise(exercise);
                    showSuccessMessage("Exercice ajouté avec succès!");
                    loadExercises();
                    closeForm();
                } else if (radioUpload.isSelected() && selectedVideoFile != null) {
                    uploadVideoFile(exercise, () -> {
                        exerciseDAO.addExercise(exercise);
                        loadExercises();
                        closeForm();
                        showSuccessMessage("Exercice ajouté avec succès!");
                    });
                    return;
                } else {
                    showError("Veuillez sélectionner une vidéo");
                    return;
                }
            }
            loadExercises();
            closeForm();
        } catch (NumberFormatException e) {
            showError("La durée doit être un nombre valide");
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur lors de l'enregistrement: " + e.getMessage());
        }
    }

    @FXML
    public void openAddForm() {
        isEditMode = false;
        selectedExercise = null;
        selectedVideoFile = null;
        radioYouTube.setSelected(true);
        txtVideoUrl.clear();
        txtVideoUrl.setVisible(true);
        uploadVideoContainer.setVisible(false);
        uploadVideoContainer.setManaged(false);
        lblSelectedVideo.setText("Aucun fichier sélectionné");
        if (videoPreview != null) {
            videoPreview.setVisible(false);
            videoPreview.setMediaPlayer(null);
        }
        clearForm();
        formOverlay.setVisible(true);
        formFadeIn.play();
    }

    public void openEditForm(Exercise ex) {
        isEditMode = true;
        selectedExercise = ex;
        txtTitle.setText(ex.getName());
        txtDescription.setText(ex.getDescription());
        txtDuration.setText(String.valueOf(ex.getDuration()));

        String videoUrl = ex.getVideoUrl();
        if (videoUrl != null && (videoUrl.contains("youtube.com") || videoUrl.contains("youtu.be"))) {
            radioYouTube.setSelected(true);
            txtVideoUrl.setText(videoUrl);
        } else if (videoUrl != null && !videoUrl.isEmpty()) {
            radioUpload.setSelected(true);
            selectedVideoFile = new File(videoUrl);
            lblSelectedVideo.setText(new File(videoUrl).getName());
            try {
                Media media = new Media(new File(videoUrl).toURI().toString());
                MediaPlayer mediaPlayer = new MediaPlayer(media);
                videoPreview.setMediaPlayer(mediaPlayer);
                videoPreview.setVisible(true);
                mediaPlayer.setAutoPlay(false);
            } catch (Exception e) {
                videoPreview.setVisible(false);
            }
        } else {
            radioYouTube.setSelected(true);
            txtVideoUrl.clear();
        }

        String category = ex.getCategory();
        if (category.equals("Cardio")) comboCategory.setValue("🏃 Cardio");
        else if (category.equals("Musculation")) comboCategory.setValue("💪 Musculation");
        else if (category.equals("Yoga")) comboCategory.setValue("🧘 Yoga");
        else comboCategory.setValue(category);

        String difficulty = ex.getDifficultyLevel();
        if (difficulty.equals("Beginner")) comboDifficulty.setValue("🌱 Débutant");
        else if (difficulty.equals("Intermediate")) comboDifficulty.setValue("⚡ Intermédiaire");
        else if (difficulty.equals("Advanced")) comboDifficulty.setValue("🔥 Avancé");
        else comboDifficulty.setValue(difficulty);

        formOverlay.setVisible(true);
        formFadeIn.play();
    }

    @FXML
    public void closeForm() {
        formFadeOut.setOnFinished(e -> {
            formOverlay.setVisible(false);
            clearForm();
        });
        formFadeOut.play();
    }

    private void clearForm() {
        txtTitle.clear();
        txtDescription.clear();
        txtVideoUrl.clear();
        txtDuration.clear();
        comboCategory.setValue(null);
        comboDifficulty.setValue(null);
        selectedVideoFile = null;
        lblSelectedVideo.setText("Aucun fichier sélectionné");
        lblError.setVisible(false);
        if (videoPreview != null) videoPreview.setVisible(false);
    }

    public void deleteExercise(Exercise ex) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer l'exercice");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cet exercice ?");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (ex.getVideoUrl() != null && ex.getVideoUrl().startsWith("uploads/")) {
                    VideoUploadService.deleteLocalVideo(ex.getVideoUrl());
                }
                exerciseDAO.deleteExercise(ex.getId());
                loadExercises();
                showSuccessMessage("Exercice supprimé avec succès!");
            }
        });
    }

    @FXML
    public void filterByStrength() {
        selectedCategory = "Musculation";
        updateFilter();
    }

    @FXML
    public void filterByCardio() {
        selectedCategory = "Cardio";
        updateFilter();
    }

    @FXML
    public void filterByYoga() {
        selectedCategory = "Yoga";
        updateFilter();
    }

    @FXML
    public void filterAll() {
        selectedCategory = "All";
        updateFilter();
    }

    @FXML
    public void resetFilters() {
        searchField.clear();
        checkBeginner.setSelected(false);
        checkIntermediate.setSelected(false);
        checkAdvanced.setSelected(false);
        selectedCategory = "All";
        sortComboBox.setValue("🏷️ Nom (A-Z)");
        if (radioAllExercises != null) radioAllExercises.setSelected(true);
        updateFilter();
        showSuccessMessage("Filtres réinitialisés");
    }

    @FXML
    private void openImportWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ExerciseImport.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Importer des exercices depuis ExerciseDB");
            stage.setScene(new Scene(loader.load()));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur: " + e.getMessage());
        }
    }

    private void showSuccessMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        lblError.setText(message);
        lblError.setVisible(true);
    }

    public void refreshAfterImport() {
        Platform.runLater(() -> {
            loadExercises();
            updateResultCount();
            showSuccessMessage("Bibliothèque d'exercices mise à jour !");
        });
    }

    @FXML
    private void openWorkoutXImport() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/WorkoutXImport.fxml"));
            Stage stage = new Stage();
            stage.setTitle("📥 Import WorkoutX");
            stage.setScene(new Scene(loader.load()));

            WorkoutXImportController controller = loader.getController();
            controller.setParentController(this);

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur: " + e.getMessage());
        }
    }
}