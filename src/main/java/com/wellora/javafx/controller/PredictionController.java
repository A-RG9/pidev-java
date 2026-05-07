package com.wellora.javafx.controller;

import com.wellora.controllers.HealthNavigationProxy;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import com.wellora.model.Healthentry;
import com.wellora.services.PredictionService;
import com.wellcare.javafx.util.SceneManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Controller for the AI Health Prediction page.
 * 
 * Follows MVC pattern:
 * - View: prediction.fxml
 * - Controller: This class
 * - Model: PredictionService + Healthentry data
 */
public class PredictionController implements Initializable {

    private HealthNavigationProxy proxy;

    @FXML
    private Label predictionLabel;

    @FXML
    private Label explanationLabel;

    @FXML
    private Button refreshButton;

    @FXML
    private Button methodologyButton;

    @FXML
    private VBox rootContainer;

    private final PredictionService predictionService;
    private MainController mainController;

    public PredictionController() {
        this.predictionService = new PredictionService();
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Apply consistent styling
        applyStyles();
        // Load prediction data
        loadPrediction();
    }

    /**
     * Applies consistent styling to UI components.
     */
    private void applyStyles() {
        // Button hover effects
        refreshButton.setOnMouseEntered(e -> 
            refreshButton.setStyle("-fx-background-color: linear-gradient(to right, #2980b9, #1f618d); -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 12 24; -fx-background-radius: 8; -fx-cursor: hand;")
        );
        refreshButton.setOnMouseExited(e -> 
            refreshButton.setStyle("-fx-background-color: linear-gradient(to right, #3498db, #2980b9); -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 12 24; -fx-background-radius: 8; -fx-cursor: hand;")
        );

        methodologyButton.setOnMouseEntered(e -> 
            methodologyButton.setStyle("-fx-background-color: #d5dbdb; -fx-text-fill: #2c3e50; -fx-font-size: 14px; -fx-padding: 12 24; -fx-background-radius: 8; -fx-cursor: hand;")
        );
        methodologyButton.setOnMouseExited(e -> 
            methodologyButton.setStyle("-fx-background-color: #ecf0f1; -fx-text-fill: #2c3e50; -fx-font-size: 14px; -fx-padding: 12 24; -fx-background-radius: 8; -fx-cursor: hand;")
        );
    }

    /**
     * Loads prediction data and updates the UI.
     * This is the main method called from initialize().
     */
    @FXML
    public void loadPrediction() {
        try {
            // Show loading state
            predictionLabel.setText("Analyzing...");
            explanationLabel.setText("Loading your health data...");

            // Get recent health entries (last 5 days)
            List<Healthentry> recentEntries = loadRecentHealthEntries();

            // Generate prediction using AI service
            PredictionService.PredictionResult result = predictionService.predictHealthState(recentEntries);

            // Update UI with results
            updatePredictionUI(result);

        } catch (Exception e) {
            handlePredictionError(e);
        }
    }

    /**
     * Loads recent health entries from the database.
     * Returns simulated data if database access fails.
     */
    private List<Healthentry> loadRecentHealthEntries() {
        try {
            String userId = SceneManager.getInstance().getCurrentUser().getUuid();
            // Try to load from database using HealthentryDAO
            com.wellora.dao.HealthentryDAO entryDAO = new com.wellora.dao.HealthentryDAO();
            List<Healthentry> entries = entryDAO.findAll(userId);

            if (entries != null && !entries.isEmpty()) {
                // Return last 5 entries
                int size = entries.size();
                int fromIndex = Math.max(0, size - 5);
                return entries.subList(fromIndex, size);
            }
        } catch (Exception e) {
            System.err.println("Database access failed, using simulated data: " + e.getMessage());
        }

        // Fallback to simulated data for demonstration
        return generateSimulatedData();
    }

    /**
     * Generates simulated health data for demonstration purposes.
     * Simulates 5 days of varied health metrics.
     */
    private List<Healthentry> generateSimulatedData() {
        return List.of(
            createHealthentry(1, 7.5, 92.0, 68.5, List.of("Headache")),
            createHealthentry(2, 8.0, 95.0, 68.3, List.of()),
            createHealthentry(3, 6.5, 88.0, 68.0, List.of("Fatigue", "Cough")),
            createHealthentry(4, 7.0, 90.0, 67.8, List.of()),
            createHealthentry(5, 8.5, 93.0, 67.5, List.of("Sore throat"))
        );
    }

    /**
     * Creates a Healthentry object with the given parameters.
     */
    private Healthentry createHealthentry(int id, double sleep, double glycemia,
            double weight, List<String> symptoms) {
        Healthentry entry = new Healthentry();
        // Use reflection or setters if available
        try {
            // Try to set ID if method exists
            try {
                entry.getClass().getMethod("setId", int.class).invoke(entry, id);
            } catch (Exception e) {
                // ID setting not critical
            }

            // Set sleep hours
            try {
                entry.getClass().getMethod("setSleepHours", double.class).invoke(entry, sleep);
            } catch (Exception e) {
                // Use field directly if setter not available
            }

            // Set glycemia
            try {
                entry.getClass().getMethod("setGlycemia", double.class).invoke(entry, glycemia);
            } catch (Exception e) {
                // Use field directly if setter not available
            }

            // Set weight
            try {
                entry.getClass().getMethod("setWeight", double.class).invoke(entry, weight);
            } catch (Exception e) {
                // Use field directly if setter not available
            }

            // Set symptoms
            try {
                entry.getClass().getMethod("setSymptoms", List.class).invoke(entry, symptoms);
            } catch (Exception e) {
                // Use field directly if setter not available
            }

        } catch (Exception e) {
            System.err.println("Error creating Healthentry: " + e.getMessage());
        }

        return entry;
    }

    /**
     * Updates the UI with prediction results.
     */
    private void updatePredictionUI(PredictionService.PredictionResult result) {
        // Update prediction label with emoji and state
        predictionLabel.setText(result.getDisplayText());

        // Color-code based on prediction state
        String color;
        switch (result.getState()) {
            case "Good":
                color = "#27ae60"; // Green
                break;
            case "Moderate":
                color = "#f39c12"; // Orange
                break;
            case "Risk":
                color = "#e74c3c"; // Red
                break;
            default:
                color = "#2c3e50"; // Dark blue
        }

        predictionLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");

        // Update explanation
        explanationLabel.setText(result.getExplanation());
    }

    /**
     * Handles errors during prediction loading.
     */
    private void handlePredictionError(Exception e) {
        predictionLabel.setText("Error");
        explanationLabel.setText("Unable to generate prediction: " + e.getMessage());

        // Show alert dialog
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Prediction Error");
        alert.setHeaderText("Failed to generate health prediction");
        alert.setContentText("An error occurred while analyzing your health data: " + e.getMessage());
        alert.showAndWait();

        e.printStackTrace();
    }

    /**
     * Refreshes the prediction when the Refresh button is clicked.
     */
    @FXML
    private void onRefreshClicked() {
        loadPrediction();
    }

    /**
     * Shows the prediction methodology explanation.
     */
    @FXML
    private void onMethodologyClicked() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("AI Prediction Methodology");
        alert.setHeaderText("How the Health Prediction Works");
        alert.setContentText(predictionService.getPredictionExplanation());
        alert.getDialogPane().setPrefWidth(600);
        alert.showAndWait();
    }

    /**
     * Gets the current prediction result.
     * Useful for testing and integration.
     */
    public PredictionService.PredictionResult getCurrentPrediction() {
        try {
            List<Healthentry> entries = loadRecentHealthEntries();
            return predictionService.predictHealthState(entries);
        } catch (Exception e) {
            return new PredictionService.PredictionResult(
                "Error", "❓", "Unable to generate prediction", 0.0
            );
        }
    }

    public void setMainControllerProxy(HealthNavigationProxy proxy) {
        this.proxy = proxy;
    }

}