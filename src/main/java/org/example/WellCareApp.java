package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * WellCare Connect - Main JavaFX Application
 * This launches the main application window
 */
public class WellCareApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Load the booking FXML (with sidebar)
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/fxml/booking.fxml"));
        
        // Load the root element
        Parent root = loader.load();
        
        // Create the scene
        Scene scene = new Scene(root, 1200, 800);
        
        // Apply the stylesheet
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        
        // Set up the stage
        stage.setTitle("WellCare Connect - Prendre RDV");
        stage.setScene(scene);
        stage.setMinWidth(1000);
        stage.setMinHeight(700);
        stage.centerOnScreen();
        stage.show();
    }

    public static void main(String[] args) {
        // Launch the JavaFX application
        Application.launch(args);
    }
}