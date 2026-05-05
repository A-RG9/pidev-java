package com.wellcare.javafx.util;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

/**
 * Reusable component for displaying password strength indicator.
 * Shows visual bars and text feedback for password strength.
 */
public class PasswordStrengthIndicator extends HBox {

    private Region strengthBar1;
    private Region strengthBar2;
    private Region strengthBar3;
    private Region strengthBar4;
    private Label strengthLabel;
    private Label requirementsLabel;

    public PasswordStrengthIndicator() {
        initializeComponents();
        setupLayout();
        setupStyles();
    }

    private void initializeComponents() {
        strengthBar1 = new Region();
        strengthBar2 = new Region();
        strengthBar3 = new Region();
        strengthBar4 = new Region();
        strengthLabel = new Label("Weak");
        requirementsLabel = new Label("Requirements: 8+ characters, uppercase, lowercase, number, special character");
    }

    private void setupLayout() {
        // Configure bars
        strengthBar1.setPrefWidth(60);
        strengthBar1.setPrefHeight(4);
        strengthBar2.setPrefWidth(60);
        strengthBar2.setPrefHeight(4);
        strengthBar3.setPrefWidth(60);
        strengthBar3.setPrefHeight(4);
        strengthBar4.setPrefWidth(60);
        strengthBar4.setPrefHeight(4);

        // Configure labels
        requirementsLabel.setWrapText(true);
        requirementsLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666666;");

        // Add components to layout
        HBox strengthBars = new HBox(5, strengthBar1, strengthBar2, strengthBar3, strengthBar4, strengthLabel);
        strengthBars.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        this.getChildren().addAll(strengthBars);
        this.setSpacing(5);
        this.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        this.setPadding(new Insets(5, 0, 5, 0));
    }

    private void setupStyles() {
        // Load CSS styles
        this.getStyleClass().add("password-strength-indicator");

        strengthBar1.getStyleClass().add("strength-bar");
        strengthBar2.getStyleClass().add("strength-bar");
        strengthBar3.getStyleClass().add("strength-bar");
        strengthBar4.getStyleClass().add("strength-bar");
        strengthLabel.getStyleClass().add("strength-label");

        // Initial state - weak
        updateStrength(0);
    }

    /**
     * Updates the strength indicator based on password strength score.
     * @param strength Score from 0 (very weak) to 4 (strong)
     */
    public void updateStrength(int strength) {
        // Reset all bars
        resetBarStyles();

        // Reset label style
        strengthLabel.getStyleClass().removeAll("strength-weak", "strength-fair", "strength-good", "strength-strong");

        switch (strength) {
            case 0: // Very Weak
                strengthBar1.getStyleClass().add("strength-weak");
                strengthLabel.setText("Very Weak");
                strengthLabel.getStyleClass().add("strength-weak");
                break;
            case 1: // Weak
                strengthBar1.getStyleClass().add("strength-weak");
                strengthBar2.getStyleClass().add("strength-weak");
                strengthLabel.setText("Weak");
                strengthLabel.getStyleClass().add("strength-weak");
                break;
            case 2: // Fair
                strengthBar1.getStyleClass().add("strength-fair");
                strengthBar2.getStyleClass().add("strength-fair");
                strengthBar3.getStyleClass().add("strength-fair");
                strengthLabel.setText("Fair");
                strengthLabel.getStyleClass().add("strength-fair");
                break;
            case 3: // Good
                strengthBar1.getStyleClass().add("strength-good");
                strengthBar2.getStyleClass().add("strength-good");
                strengthBar3.getStyleClass().add("strength-good");
                strengthBar4.getStyleClass().add("strength-good");
                strengthLabel.setText("Good");
                strengthLabel.getStyleClass().add("strength-good");
                break;
            case 4: // Strong
                strengthBar1.getStyleClass().add("strength-strong");
                strengthBar2.getStyleClass().add("strength-strong");
                strengthBar3.getStyleClass().add("strength-strong");
                strengthBar4.getStyleClass().add("strength-strong");
                strengthLabel.setText("Strong");
                strengthLabel.getStyleClass().add("strength-strong");
                break;
        }
    }

    private void resetBarStyles() {
        strengthBar1.getStyleClass().removeAll("strength-weak", "strength-fair", "strength-good", "strength-strong");
        strengthBar2.getStyleClass().removeAll("strength-weak", "strength-fair", "strength-good", "strength-strong");
        strengthBar3.getStyleClass().removeAll("strength-weak", "strength-fair", "strength-good", "strength-strong");
        strengthBar4.getStyleClass().removeAll("strength-weak", "strength-fair", "strength-good", "strength-strong");
    }

    /**
     * Gets the requirements label for external access.
     * @return The requirements label
     */
    public Label getRequirementsLabel() {
        return requirementsLabel;
    }
}