package com.wellcare.javafx.controller.auth;

import com.wellcare.javafx.util.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;

public class ProfessionalTypeChoiceController {

    @FXML private VBox medecinCard;
    @FXML private VBox coachCard;
    @FXML private VBox nutritionistCard;

    private DropShadow hoverShadow = new DropShadow(15, Color.web("#00A790"));
    private DropShadow clickShadow = new DropShadow(8, Color.web("#00DBB6"));

    @FXML
    public void initialize() {
        setupCardHoverEffects();
    }

    private void setupCardHoverEffects() {
        // Médecin card hover effects
        medecinCard.setOnMouseEntered(e -> {
            medecinCard.setEffect(hoverShadow);
            medecinCard.setStyle(medecinCard.getStyle() + "-fx-border-color: #00A790; -fx-border-width: 2;");
        });
        medecinCard.setOnMouseExited(e -> {
            medecinCard.setEffect(null);
            medecinCard.setStyle(medecinCard.getStyle().replace("-fx-border-color: #00A790; -fx-border-width: 2;", ""));
        });

        // Coach card hover effects
        coachCard.setOnMouseEntered(e -> {
            coachCard.setEffect(hoverShadow);
            coachCard.setStyle(coachCard.getStyle() + "-fx-border-color: #00A790; -fx-border-width: 2;");
        });
        coachCard.setOnMouseExited(e -> {
            coachCard.setEffect(null);
            coachCard.setStyle(coachCard.getStyle().replace("-fx-border-color: #00A790; -fx-border-width: 2;", ""));
        });

        // Nutritionist card hover effects
        nutritionistCard.setOnMouseEntered(e -> {
            nutritionistCard.setEffect(hoverShadow);
            nutritionistCard.setStyle(nutritionistCard.getStyle() + "-fx-border-color: #00A790; -fx-border-width: 2;");
        });
        nutritionistCard.setOnMouseExited(e -> {
            nutritionistCard.setEffect(null);
            nutritionistCard.setStyle(nutritionistCard.getStyle().replace("-fx-border-color: #00A790; -fx-border-width: 2;", ""));
        });
    }

    @FXML
    private void onMedecinSelected() {
        animateCardClick(medecinCard);
        navigateToRegistration("ROLE_MEDECIN");
    }

    @FXML
    private void onCoachSelected() {
        animateCardClick(coachCard);
        navigateToRegistration("ROLE_COACH");
    }

    @FXML
    private void onNutritionistSelected() {
        animateCardClick(nutritionistCard);
        navigateToRegistration("ROLE_NUTRITIONIST");
    }

    private void animateCardClick(VBox card) {
        card.setEffect(clickShadow);
        // Reset effect after animation
        new Thread(() -> {
            try {
                Thread.sleep(150);
                javafx.application.Platform.runLater(() -> card.setEffect(null));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private void navigateToRegistration(String professionalType) {
        try {
            // Pass the selected professional type to the registration controller
            SceneManager.getInstance().switchToProfessionalRegistration(professionalType);
        } catch (Exception e) {
            System.err.println("Error navigating to professional registration: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void onBackToLogin() {
        try {
            SceneManager.getInstance().switchToLogin();
        } catch (Exception e) {
            System.err.println("Error navigating back to login: " + e.getMessage());
            e.printStackTrace();
        }
    }
}