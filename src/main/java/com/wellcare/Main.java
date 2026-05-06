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
import java.io.IOException;
import java.time.LocalDateTime;

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
                controller.onClose();
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
            // Check if admin user already exists by email
            User existingAdmin = userService.getUserByEmail("admin@wellcare.com");
            if (existingAdmin != null) {
                System.out.println("Admin user already exists: " + existingAdmin.getEmail());
                return;
            }
        } catch (Exception e) {
            // User doesn't exist, will create new one
            System.out.println("Admin user not found, creating new admin user...");
        }

        try {
            // Create admin user
            System.out.println("Creating admin user for testing...");

            User adminUser = new User();
            adminUser.setUuid(java.util.UUID.randomUUID().toString());
            adminUser.setEmail("admin@wellcare.com");
            adminUser.setFirstName("WellCare");
            adminUser.setLastName("Admin");
            adminUser.setPassword("Admin@123"); // Will be hashed by service
            adminUser.setRole("ROLE_ADMIN");
            adminUser.setLicenseNumber("ADMIN-001"); // Required for professionals
            adminUser.setActive(true);
            adminUser.setVerifiedByAdmin(true);
            adminUser.setEmailVerified(true);
            adminUser.setCreatedAt(LocalDateTime.now());
            adminUser.setUpdatedAt(LocalDateTime.now());

            // Use registerProfessional to ensure proper password hashing
            userService.registerProfessional(adminUser, "Admin@123");
            System.out.println("✅ Admin user created successfully!");
            System.out.println("Email: admin@wellcare.com");
            System.out.println("Password: Admin@123");

        } catch (Exception e) {
            System.err.println("❌ Error creating admin user: " + e.getMessage());
            e.printStackTrace();
            // Don't fail the application if admin creation fails
        }
    }

    /**
     * Point d'entrée principal
     */
    public static void main(String[] args) {
        launch(args);
    }
}