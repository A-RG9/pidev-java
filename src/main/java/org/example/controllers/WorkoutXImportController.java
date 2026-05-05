package org.example.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.utils.WorkoutXService;
public class WorkoutXImportController {

    @FXML private TextField txtLimit;
    @FXML private ProgressBar importProgress;
    @FXML private Label lblStatus;
    @FXML private TextArea logArea;
    @FXML private Button btnImportAll;
    @FXML private Button btnImportLimit;

    private ExerciseLibraryController parentController;
    private WorkoutXService service = new WorkoutXService();

    @FXML
    public void initialize() {
        txtLimit.setText("50");
        importProgress.setVisible(false);

        // Vérifier l'API au démarrage
        checkAPIAvailability();
    }

    private void checkAPIAvailability() {
        new Thread(() -> {
            boolean available = service.isAPIAvailable();
            Platform.runLater(() -> {
                if (available) {
                    logArea.appendText("✅ API WorkoutX disponible\n");
                    logArea.appendText("📊 " + service.getLocalExerciseCount() + " exercices en base\n");
                } else {
                    logArea.appendText("⚠️ API WorkoutX temporairement indisponible\n");
                    logArea.appendText("📊 Utilisation des " + service.getLocalExerciseCount() + " exercices existants\n");
                    btnImportAll.setDisable(true);
                    btnImportLimit.setDisable(true);
                }
            });
        }).start();
    }

    @FXML
    private void importAllExercises() {
        confirmAndImport(() -> service.importNewExercises());
    }

    @FXML
    private void importWithLimit() {
        try {
            int limit = Integer.parseInt(txtLimit.getText());
            if (limit < 1) limit = 10;
            if (limit > 100) limit = 100;

            final int finalLimit = limit;
            confirmAndImport(() -> service.importExercises(finalLimit));

        } catch (NumberFormatException e) {
            logArea.appendText("❌ Nombre invalide\n");
        }
    }

    private void confirmAndImport(ImportOperation operation) {
        if (!service.isAPIAvailable()) {
            logArea.appendText("⚠️ API indisponible. Veuillez réessayer dans 24 heures.\n");
            logArea.appendText("💡 Utilisez les " + service.getLocalExerciseCount() + " exercices déjà disponibles.\n");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Import d'exercices");
        confirm.setContentText("Voulez-vous continuer ?\n"
                + "- L'API a des limites (max 50 requêtes/jour)\n"
                + "- L'import peut prendre plusieurs minutes\n"
                + "- Seuls les nouveaux exercices seront ajoutés");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        importProgress.setVisible(true);
        lblStatus.setText("Import en cours...");
        logArea.appendText("\n📥 DÉBUT DE L'IMPORT\n");

        new Thread(() -> {
            int count = operation.execute();
            int finalCount = count;

            Platform.runLater(() -> {
                importProgress.setVisible(false);

                if (finalCount > 0) {
                    lblStatus.setText("✅ " + finalCount + " exercices importés !");
                    logArea.appendText("\n✅ " + finalCount + " nouveaux exercices importés !\n");

                    Alert success = new Alert(Alert.AlertType.INFORMATION);
                    success.setTitle("Succès");
                    success.setHeaderText("Import terminé");
                    success.setContentText(finalCount + " nouveaux exercices ont été ajoutés.");
                    success.showAndWait();

                    if (parentController != null) {
                        parentController.refreshAfterImport();
                    }

                } else if (finalCount == 0) {
                    lblStatus.setText("📭 Aucun nouvel exercice");
                    logArea.appendText("\n📭 Aucun nouvel exercice à importer\n");
                } else {
                    lblStatus.setText("❌ Erreur lors de l'import");
                    logArea.appendText("\n❌ L'import a échoué\n");
                }
            });
        }).start();
    }

    @FXML
    private void closeWindow() {
        Stage stage = (Stage) importProgress.getScene().getWindow();
        stage.close();
    }

    public void setParentController(ExerciseLibraryController parent) {
        this.parentController = parent;
    }

    @FunctionalInterface
    private interface ImportOperation {
        int execute();
    }
}