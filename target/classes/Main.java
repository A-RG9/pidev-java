package org.example;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.server.SimpleWebSocketServer;

public class Main extends Application {

    private SimpleWebSocketServer webSocketServer;

    @Override
    public void start(Stage primaryStage) throws Exception {
        // 1. Démarrer le serveur WebSocket
        startWebSocketServer();

        // 2. Load the main layout
        Parent root = FXMLLoader.load(getClass().getResource("/Main.fxml"));

        // 3. Create the Scene EXACTLY ONCE
        Scene scene = new Scene(root);

        // 4. Load the default dark theme CSS
        String cssPath = getClass().getResource("/dark.css").toExternalForm();
        scene.getStylesheets().add(cssPath);

        // 5. Show the window
        primaryStage.setTitle("Wellora - Coaching Platform");
        primaryStage.setScene(scene);
        primaryStage.show();

        // 6. Afficher dans la console que tout est prêt
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║              ✅ WELLORA DÉMARRÉ AVEC SUCCÈS ✅             ║");
        System.out.println("╠════════════════════════════════════════════════════════════╣");
        System.out.println("║  🖥️  Interface JavaFX: PRÊTE                               ║");
        System.out.println("║  🌐 Serveur WebSocket: ws://localhost:8887                ║");
        System.out.println("║  💬 Chat en temps réel: ACTIF                             ║");
        System.out.println("║  🎥 Visioconférence: PRÊTE (Jitsi Meet)                   ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
    }

    /**
     * Démarre le serveur WebSocket sur le port 8887
     */
    private void startWebSocketServer() {
        try {
            webSocketServer = new SimpleWebSocketServer(8887);
            webSocketServer.start();
            System.out.println("✅ Serveur WebSocket démarré sur ws://localhost:8887");
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du démarrage du serveur WebSocket: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Arrête le serveur WebSocket proprement à la fermeture de l'application
     */
    @Override
    public void stop() {
        System.out.println("\n🛑 Arrêt de l'application Wellora...");

        // Arrêter le serveur WebSocket
        if (webSocketServer != null) {
            try {
                webSocketServer.stop();
                System.out.println("✅ Serveur WebSocket arrêté");
            } catch (Exception e) {
                System.err.println("❌ Erreur lors de l'arrêt du serveur WebSocket: " + e.getMessage());
            }
        }

        // Quitter l'application
        Platform.exit();
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}