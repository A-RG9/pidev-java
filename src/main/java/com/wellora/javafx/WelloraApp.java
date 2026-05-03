package com.wellora.javafx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class WelloraApp extends Application {

    // Teal color scheme - #00A790
    public static final String TEAL_PRIMARY = "#00A790";
    public static final String TEAL_DARK = "#008B74";
    public static final String TEAL_LIGHT = "#E8F5F3";

    @Override
    public void start(Stage stage) throws Exception {
        // Load main.fxml as the entry point with sidebar + dynamic content
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
        Parent root = loader.load();
        
        stage.setTitle("Wellora Health - Your Personal Health Tracker");
        stage.setWidth(1200);
        stage.setHeight(800);
        stage.setMinWidth(1000);
        stage.setMinHeight(700);
        
        Scene scene = new Scene(root);
        // Apply Wellora theme CSS - load in order: style.css (base), dashboard.css (light), dashboard-dark.css (dark override)
        scene.getStylesheets().add(
            WelloraApp.class.getResource("/com/wellora/css/style.css").toExternalForm()
        );
        scene.getStylesheets().add(
            WelloraApp.class.getResource("/fxml/dashboard.css").toExternalForm()
        );
        
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}