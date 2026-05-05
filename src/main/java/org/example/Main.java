package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // 1. Load the main layout
        Parent root = FXMLLoader.load(getClass().getResource("/Main.fxml"));

        // 2. Create the Scene EXACTLY ONCE
        Scene scene = new Scene(root);

        // 3. Load the default dark theme CSS
        String cssPath = getClass().getResource("/dark.css").toExternalForm();
        scene.getStylesheets().add(cssPath);

        // 4. Show the window
        primaryStage.setTitle("WellCare");
        primaryStage.setScene(scene);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}