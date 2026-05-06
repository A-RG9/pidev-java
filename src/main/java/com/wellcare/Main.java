package com.wellcare;

import com.wellcare.javafx.model.User;
import com.wellcare.javafx.service.UserService;
import com.wellcare.javafx.util.SceneManager;
import com.wellcare.ui.LandingController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Classe principale de l'application WellCare Connect
 * Lance la page d'accueil (landing page) avec intégration SceneManager
 */
public class Main extends Application {

    private static final int WINDOW_WIDTH = 1200;
    private static final int WINDOW_HEIGHT = 800;
    private static final String APP_TITLE = "WellCare Connect - Votre santé, connectée";

    private UserService userService;
    private SceneManager sceneManager;

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            // Initialize services
            userService = new UserService();

            // Create admin user for testing if it doesn't exist
            createAdminUserIfNotExists();

            // Initialize SceneManager
            sceneManager = SceneManager.getInstance();
            sceneManager.initialize(primaryStage, userService);

            // Charger le FXML de la page d'accueil
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/landing.fxml"));
            Parent root = loader.load();

            // Obtenir le contrôleur et injecter les services
            LandingController controller = loader.getController();
            if (controller instanceof SceneManager.ServiceAware) {
                ((SceneManager.ServiceAware) controller).setUserService(userService);
            }

            // Créer la scène avec taille responsive
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/landing.css").toExternalForm());
            scene.getStylesheets().add(getClass().getResource("/css/wellcare.css").toExternalForm());

            // Passer la scène au contrôleur pour la gestion du thème
            controller.setScene(scene);

            // Configurer la fenêtre principale de manière responsive
            primaryStage.setTitle(APP_TITLE);
            primaryStage.setScene(scene);
            primaryStage.setWidth(WINDOW_WIDTH);
            primaryStage.setHeight(WINDOW_HEIGHT);
            primaryStage.setMinWidth(1000);
            primaryStage.setMinHeight(700);

            // Centrer la fenêtre
            primaryStage.centerOnScreen();

            // Gestionnaire de fermeture
            primaryStage.setOnCloseRequest(e -> {
                if (controller != null) {
                    controller.onClose();
                }
            });

            // Afficher la fenêtre
            primaryStage.show();

        } catch (Exception e) {
            System.err.println("Erreur lors du démarrage de l'application:");
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Creates an admin user for testing if one doesn't already exist
     */
    private void createAdminUserIfNotExists() {
        try {
            // Try to find existing admin user
            User existingAdmin = userService.authenticate("admin@wellcare.com", "Admin123!");
            if (existingAdmin != null) {
                System.out.println("Admin user already exists and can login.");
            }
        } catch (Exception e) {
            // Admin doesn't exist or authentication failed
            System.out.println("Admin user not found, proceeding...");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}