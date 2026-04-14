package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // ⚠️ LE CHANGEMENT EST ICI : On charge Main.fxml (la Sidebar) en premier !
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Main.fxml"));

        Parent root = loader.load();

        primaryStage.setTitle("WellCare - Dashboard");
        // On agrandit un peu la fenêtre pour laisser la place à la Sidebar
        primaryStage.setScene(new Scene(root, 1280, 800));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}