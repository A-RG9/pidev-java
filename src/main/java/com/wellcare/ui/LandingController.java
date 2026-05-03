package com.wellcare.ui;

import com.wellcare.javafx.service.UserService;
import com.wellcare.javafx.util.SceneManager;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Contrôleur pour la page d'accueil (landing page) de WellCare Connect
 * Implémente des compteurs animés et le mode sombre
 */
public class LandingController implements Initializable, SceneManager.ServiceAware {

    // Root container
    @FXML private BorderPane rootPane;

    // Navigation buttons
    @FXML private Button homeBtn;
    @FXML private Button loginBtn;
    @FXML private Button registerBtn;
    @FXML private Button professionalBtn;

    // Hero section buttons
    @FXML private Button startPatientBtn;
    @FXML private Button startProfessionalBtn;
    @FXML private Button ctaPatientBtn;
    @FXML private Button ctaProBtn;
    @FXML private Hyperlink loginLink;

    // Footer links
    @FXML private Hyperlink footerProfessionalLink;

    // Theme toggle
    @FXML private ToggleButton themeToggle;
    @FXML private Label themeIcon;

    // Animated counters
    @FXML private Label stat1Label; // Patients actifs
    @FXML private Label stat2Label; // Médecins partenaires
    @FXML private Label stat3Label; // Satisfaction client
    @FXML private Label stat4Label; // Support disponible

    // Scroll pane for animation trigger
    @FXML private ScrollPane mainScrollPane;

    // Scene reference for theme switching
    private Scene scene;

    private UserService userService;
    private SceneManager sceneManager;
    private boolean countersAnimated = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Get SceneManager instance
        sceneManager = SceneManager.getInstance();

        // Setup theme toggle
        setupThemeToggle();

        // Setup scroll listener for counter animation
        setupScrollListener();

        // Set initial theme icon
        updateThemeIcon();
    }

    @Override
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    /**
     * Configure le bouton de basculement de thème
     */
    private void setupThemeToggle() {
        themeToggle.setSelected(false);
        updateThemeIcon();
    }

    /**
     * Met à jour l'icône du thème
     */
    private void updateThemeIcon() {
        if (themeIcon != null) {
            themeIcon.setText(themeToggle.isSelected() ? "☀️" : "🌙");
        }
    }

    /**
     * Configure l'écouteur de scroll pour déclencher l'animation des compteurs
     */
    private void setupScrollListener() {
        if (mainScrollPane != null) {
            mainScrollPane.vvalueProperty().addListener((obs, oldVal, newVal) -> {
                // Trigger animation when stats section becomes visible (around 60% scroll)
                if (!countersAnimated && newVal.doubleValue() > 0.6) {
                    animateCounters();
                    countersAnimated = true;
                }
            });
        } else {
            // Fallback: animate counters after a short delay
            Platform.runLater(() -> {
                try {
                    Thread.sleep(1000);
                    animateCounters();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
    }

    /**
     * Anime les compteurs statistiques
     */
    private void animateCounters() {
        animateCounter(stat1Label, 0, 10000, 2000); // 10,000+ patients
        animateCounter(stat2Label, 0, 500, 2000);    // 500+ médecins
        animateCounter(stat3Label, 0, 98, 2000);     // 98% satisfaction
        animateCounter(stat4Label, 0, 24, 2000);     // 24/7 support
    }

    /**
     * Anime un compteur individuel
     */
    private void animateCounter(Label label, int startValue, int endValue, int durationMs) {
        Timeline timeline = new Timeline();

        // Pour les compteurs avec "+", ajouter le symbole
        String suffix = endValue >= 98 ? "%" : (endValue >= 24 ? "/7" : "+");

        for (int i = startValue; i <= endValue; i++) {
            final int value = i;
            double progress = (double) (i - startValue) / (endValue - startValue);
            long time = (long) (progress * durationMs);

            timeline.getKeyFrames().add(
                new KeyFrame(Duration.millis(time),
                    new KeyValue(label.textProperty(),
                        value + (value == endValue ? suffix : "")))
            );
        }

        timeline.play();
    }

    /**
     * Définit la référence à la scène pour la gestion du thème
     */
    public void setScene(Scene scene) {
        this.scene = scene;
    }

    /**
     * Gestionnaire pour le bouton Accueil
     */
    @FXML
    private void handleHome() {
        // Already on home page, maybe scroll to top
        if (mainScrollPane != null) {
            mainScrollPane.setVvalue(0);
        }
    }

    /**
     * Gestionnaire pour le bouton Connexion
     */
    @FXML
    private void handleLogin() {
        if (sceneManager != null) {
            sceneManager.switchTo(SceneManager.LOGIN);
        }
    }

    /**
     * Gestionnaire pour le bouton Inscription
     */
    @FXML
    private void handleRegister() {
        if (sceneManager != null) {
            sceneManager.switchTo(SceneManager.REGISTER_PATIENT);
        }
    }

    /**
     * Gestionnaire pour le bouton Professionnel
     */
    @FXML
    private void handleProfessional() {
        if (sceneManager != null) {
            sceneManager.switchToProfessionalTypeChoice();
        }
    }

    /**
     * Gestionnaire pour le bouton Commencer (Patient)
     */
    @FXML
    private void handleStartPatient() {
        if (sceneManager != null) {
            sceneManager.switchTo(SceneManager.REGISTER_PATIENT);
        }
    }

    /**
     * Gestionnaire pour le bouton Je suis un professionnel
     */
    @FXML
    private void handleStartProfessional() {
        if (sceneManager != null) {
            sceneManager.switchToProfessionalTypeChoice();
        }
    }

    /**
     * Gestionnaire pour le lien "Se connecter"
     */
    @FXML
    private void handleLoginLink() {
        if (sceneManager != null) {
            sceneManager.switchTo(SceneManager.LOGIN);
        }
    }

    /**
     * Gestionnaire pour le basculement de thème
     */
    @FXML
    private void toggleTheme() {
        updateThemeIcon();

        if (scene != null) {
            if (themeToggle.isSelected()) {
                // Mode sombre
                scene.getStylesheets().add(getClass().getResource("/css/landing-dark.css").toExternalForm());
                rootPane.getStyleClass().add("dark");
            } else {
                // Mode clair
                scene.getStylesheets().remove(getClass().getResource("/css/landing-dark.css").toExternalForm());
                rootPane.getStyleClass().remove("dark");
            }
        }
    }

    /**
     * Gestionnaire générique pour les liens "À venir"
     */
    @FXML
    private void showComingSoon() {
        showComingSoonAlert("Fonctionnalité à venir", "Cette fonctionnalité sera bientôt disponible.");
    }

    /**
     * Affiche une alerte "Fonctionnalité à venir"
     */
    private void showComingSoonAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Fonctionnalité à venir");
        alert.setHeaderText(title);
        alert.setContentText(message);

        // Style de l'alerte
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/css/wellcare.css").toExternalForm());
        dialogPane.getStyleClass().add("wellcare-alert");

        // Centrer l'alerte sur la fenêtre principale
        if (rootPane != null && rootPane.getScene() != null) {
            Stage stage = (Stage) rootPane.getScene().getWindow();
            alert.initOwner(stage);
        }

        alert.showAndWait();
    }

    /**
     * Méthode appelée lors de la fermeture de la fenêtre
     */
    public void onClose() {
        // Cleanup resources if needed
        if (sceneManager != null) {
            // Any cleanup for scene manager would go here
        }
    }
}