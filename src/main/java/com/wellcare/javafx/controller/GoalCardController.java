package com.wellcare.javafx.controller;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;
import com.wellcare.javafx.model.Goal;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class GoalCardController {

    @FXML private VBox cardContainer;
    @FXML private Label lblStatus;
    @FXML private Label lblCategory;
    @FXML private Text txtTitle;
    @FXML private Text txtDescription;
    @FXML private Label lblProgress;
    @FXML private ProgressBar progressBar;
    @FXML private Label lblPatient;
    @FXML private Label lblEndDate;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;
    @FXML private HBox actionButtons;
    @FXML private Label lblDaysLeft;
    @FXML private Label lblPatientIcon;
    @FXML private Label lblDateIcon;

    private Goal currentGoal;
    private GoalController parentController;

    public void setGoalData(Goal goal, GoalController parentController) {
        this.currentGoal = goal;
        this.parentController = parentController;

        // Configuration de la carte plus large
        setupCardSize();

        // Configuration du texte principal
        setupTitle(goal.getTitle());
        setupDescription(goal.getDescription());

        // Configuration des labels
        setupCategoryLabel(goal.getCategory());
        setupStatusLabel(goal.getStatus());
        setupProgress(goal.getProgress());
        setupPatientInfo(goal.getPatientId());
        setupEndDate(goal.getEndDate());
        setupDaysLeft(goal.getEndDate());

        // Configuration des boutons
        setupButtons();

        // Configuration de la carte
        setupCardStyle();

        // Animation d'entrée
        animateCardEntry();
    }

    private void setupCardSize() {
        if (cardContainer != null) {
            // Définir une largeur plus grande pour la carte
            cardContainer.setMinWidth(500);
            cardContainer.setMaxWidth(600);
            cardContainer.setPrefWidth(550);

            // Hauteur automatique - sans utiliser Region
            cardContainer.setMinHeight(Region.USE_COMPUTED_SIZE);
            cardContainer.setMaxHeight(Region.USE_COMPUTED_SIZE);

            // Espacement interne
            cardContainer.setSpacing(12);
            cardContainer.setPadding(new javafx.geometry.Insets(20, 24, 20, 24));
        }
    }

    private void setupTitle(String title) {
        txtTitle.setText(title);
        txtTitle.setStyle(
                "-fx-font-size: 18px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-fill: #1E293B;" +
                        "-fx-font-family: 'Segoe UI';"
        );
        txtTitle.setWrappingWidth(450); // Permettre le retour à la ligne sur grande largeur
    }

    private void setupDescription(String description) {
        if (description != null && !description.isEmpty()) {
            txtDescription.setText(description);
            txtDescription.setStyle(
                    "-fx-font-size: 13px;" +
                            "-fx-fill: #64748B;" +
                            "-fx-font-family: 'Segoe UI';" +
                            "-fx-line-spacing: 1.4;"
            );
            txtDescription.setWrappingWidth(450);
        } else {
            txtDescription.setText("Aucune description");
            txtDescription.setStyle(
                    "-fx-font-size: 13px;" +
                            "-fx-fill: #94A3B8;" +
                            "-fx-font-style: italic;" +
                            "-fx-font-family: 'Segoe UI';"
            );
            txtDescription.setWrappingWidth(450);
        }
    }

    private void setupCategoryLabel(String category) {
        String displayCategory;
        String backgroundColor;
        String textColor;
        String icon;

        switch (category) {
            case "Nutrition":
                icon = "🍎";
                backgroundColor = "#FEF3C7";
                textColor = "#D97706";
                displayCategory = "Nutrition";
                break;
            case "Sport":
                icon = "🏃";
                backgroundColor = "#D1FAE5";
                textColor = "#059669";
                displayCategory = "Sport";
                break;
            case "Mental":
                icon = "🧠";
                backgroundColor = "#E0E7FF";
                textColor = "#4F46E5";
                displayCategory = "Mental";
                break;
            default:
                icon = "📋";
                backgroundColor = "#F1F5F9";
                textColor = "#64748B";
                displayCategory = "Général";
        }

        lblCategory.setText(icon + " " + displayCategory);
        lblCategory.setStyle(
                "-fx-background-color: " + backgroundColor + ";" +
                        "-fx-text-fill: " + textColor + ";" +
                        "-fx-padding: 5 14 5 14;" +
                        "-fx-border-radius: 15;" +
                        "-fx-background-radius: 15;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 12px;"
        );
    }

    private void setupStatusLabel(String status) {
        String backgroundColor;
        String textColor;
        String icon;

        switch (status) {
            case "Terminé":
                icon = "✅";
                backgroundColor = "#D1FAE5";
                textColor = "#059669";
                break;
            case "En cours":
                icon = "🟢";
                backgroundColor = "#DBEAFE";
                textColor = "#2563EB";
                break;
            default:
                icon = "⏳";
                backgroundColor = "#FEF3C7";
                textColor = "#D97706";
        }

        lblStatus.setText(icon + " " + status);
        lblStatus.setStyle(
                "-fx-background-color: " + backgroundColor + ";" +
                        "-fx-text-fill: " + textColor + ";" +
                        "-fx-padding: 5 14 5 14;" +
                        "-fx-border-radius: 15;" +
                        "-fx-background-radius: 15;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 12px;"
        );
    }

    private void setupProgress(int progress) {
        lblProgress.setText(progress + "%");
        progressBar.setProgress(progress / 100.0);
        progressBar.setPrefHeight(8);

        // Style de la barre de progression selon le pourcentage
        String progressColor;
        if (progress < 30) {
            progressColor = "#EF4444";
        } else if (progress < 70) {
            progressColor = "#F59E0B";
        } else {
            progressColor = "#10B981";
        }

        progressBar.setStyle(
                "-fx-accent: " + progressColor + ";" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 2;"
        );

        // Style du label de progression
        lblProgress.setStyle(
                "-fx-text-fill: " + progressColor + ";" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 14px;"
        );

        // Tooltip pour la progression
        Tooltip progressTooltip = new Tooltip(progress + "% complété");
        progressBar.setTooltip(progressTooltip);
    }

    private void setupPatientInfo(String patientId) {
        if (patientId != null && !patientId.isEmpty()) {
            // Tronquer l'UUID pour l'affichage
            String shortId = patientId.length() > 8 ?
                    patientId.substring(0, 8) + "..." : patientId;
            lblPatient.setText("👤 Patient: " + shortId);
        } else {
            lblPatient.setText("👤 Patient: Non assigné");
        }

        lblPatient.setStyle(
                "-fx-text-fill: #64748B;" +
                        "-fx-font-size: 12px;" +
                        "-fx-padding: 3 0 3 0;"
        );
    }

    private void setupEndDate(LocalDate endDate) {
        if (endDate != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
            String formattedDate = endDate.format(formatter);
            lblEndDate.setText("📅 " + formattedDate);
        } else {
            lblEndDate.setText("📅 Date non définie");
        }

        lblEndDate.setStyle(
                "-fx-text-fill: #64748B;" +
                        "-fx-font-size: 12px;" +
                        "-fx-padding: 3 0 3 0;"
        );
    }

    private void setupDaysLeft(LocalDate endDate) {
        if (lblDaysLeft != null && endDate != null) {
            LocalDate today = LocalDate.now();
            long daysLeft = ChronoUnit.DAYS.between(today, endDate);

            String daysText;
            String color;

            if (daysLeft < 0) {
                daysText = "⏰ Dépassé de " + Math.abs(daysLeft) + " jours";
                color = "#EF4444";
            } else if (daysLeft == 0) {
                daysText = "⚠️ Dernier jour!";
                color = "#F59E0B";
            } else if (daysLeft <= 7) {
                daysText = "⏰ " + daysLeft + " jours restants";
                color = "#F59E0B";
            } else {
                daysText = "📆 " + daysLeft + " jours restants";
                color = "#10B981";
            }

            lblDaysLeft.setText(daysText);
            lblDaysLeft.setStyle(
                    "-fx-text-fill: " + color + ";" +
                            "-fx-font-size: 12px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-padding: 3 0 3 0;"
            );
        }
    }

    private void setupButtons() {
        // Style du bouton Modifier
        btnEdit.setStyle(
                "-fx-background-color: #EFF6FF;" +
                        "-fx-text-fill: #3B82F6;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 7 14 7 14;" +
                        "-fx-font-weight: bold;" +
                        "-fx-cursor: hand;" +
                        "-fx-font-size: 12px;"
        );

        btnEdit.setOnMouseEntered(e -> {
            btnEdit.setStyle(
                    "-fx-background-color: #DBEAFE;" +
                            "-fx-text-fill: #2563EB;" +
                            "-fx-background-radius: 8;" +
                            "-fx-padding: 7 14 7 14;" +
                            "-fx-font-weight: bold;" +
                            "-fx-cursor: hand;" +
                            "-fx-font-size: 12px;"
            );
            animateButton(btnEdit);
        });

        btnEdit.setOnMouseExited(e -> {
            btnEdit.setStyle(
                    "-fx-background-color: #EFF6FF;" +
                            "-fx-text-fill: #3B82F6;" +
                            "-fx-background-radius: 8;" +
                            "-fx-padding: 7 14 7 14;" +
                            "-fx-font-weight: bold;" +
                            "-fx-font-size: 12px;"
            );
        });

        // Style du bouton Supprimer
        btnDelete.setStyle(
                "-fx-background-color: #FEF2F2;" +
                        "-fx-text-fill: #EF4444;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 7 14 7 14;" +
                        "-fx-font-weight: bold;" +
                        "-fx-cursor: hand;" +
                        "-fx-font-size: 12px;"
        );

        btnDelete.setOnMouseEntered(e -> {
            btnDelete.setStyle(
                    "-fx-background-color: #FEE2E2;" +
                            "-fx-text-fill: #DC2626;" +
                            "-fx-background-radius: 8;" +
                            "-fx-padding: 7 14 7 14;" +
                            "-fx-font-weight: bold;" +
                            "-fx-cursor: hand;" +
                            "-fx-font-size: 12px;"
            );
            animateButton(btnDelete);
        });

        btnDelete.setOnMouseExited(e -> {
            btnDelete.setStyle(
                    "-fx-background-color: #FEF2F2;" +
                            "-fx-text-fill: #EF4444;" +
                            "-fx-background-radius: 8;" +
                            "-fx-padding: 7 14 7 14;" +
                            "-fx-font-weight: bold;" +
                            "-fx-font-size: 12px;"
            );
        });

        // Tooltips pour les boutons
        Tooltip editTooltip = new Tooltip("Modifier cet objectif");
        Tooltip deleteTooltip = new Tooltip("Supprimer cet objectif");
        btnEdit.setTooltip(editTooltip);
        btnDelete.setTooltip(deleteTooltip);

        // Actions
        btnEdit.setOnAction(event -> {
            animateCardClick();
            if (parentController != null) {
                parentController.openEditForm(currentGoal);
            }
        });

        btnDelete.setOnAction(event -> {
            animateCardClick();
            if (parentController != null) {
                parentController.handleDelete(currentGoal);
            }
        });
    }

    private void setupCardStyle() {
        if (cardContainer != null) {
            // Style de base de la carte avec largeur augmentée
            cardContainer.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-background-radius: 16;" +
                            "-fx-border-radius: 16;" +
                            "-fx-border-color: #E2E8F0;" +
                            "-fx-border-width: 1px;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);"
            );

            // Effet de survol
            cardContainer.setOnMouseEntered(e -> {
                cardContainer.setStyle(
                        "-fx-background-color: white;" +
                                "-fx-background-radius: 16;" +
                                "-fx-border-radius: 16;" +
                                "-fx-border-color: #CBD5E1;" +
                                "-fx-border-width: 1px;" +
                                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 12, 0, 0, 4);"
                );
                ScaleTransition st = new ScaleTransition(Duration.millis(200), cardContainer);
                st.setToX(1.02);
                st.setToY(1.02);
                st.play();
            });

            cardContainer.setOnMouseExited(e -> {
                cardContainer.setStyle(
                        "-fx-background-color: white;" +
                                "-fx-background-radius: 16;" +
                                "-fx-border-radius: 16;" +
                                "-fx-border-color: #E2E8F0;" +
                                "-fx-border-width: 1px;" +
                                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);"
                );
                ScaleTransition st = new ScaleTransition(Duration.millis(200), cardContainer);
                st.setToX(1);
                st.setToY(1);
                st.play();
            });
        }
    }

    private void animateCardEntry() {
        if (cardContainer != null) {
            cardContainer.setOpacity(0);
            cardContainer.setTranslateY(20);

            TranslateTransition tt = new TranslateTransition(Duration.millis(300), cardContainer);
            tt.setFromY(20);
            tt.setToY(0);

            FadeTransition ft = new FadeTransition(Duration.millis(300), cardContainer);
            ft.setFromValue(0);
            ft.setToValue(1);

            tt.play();
            ft.play();
        }
    }

    private void animateButton(Button button) {
        ScaleTransition st = new ScaleTransition(Duration.millis(150), button);
        st.setToX(1.05);
        st.setToY(1.05);
        st.setCycleCount(2);
        st.setAutoReverse(true);
        st.play();
    }

    private void animateCardClick() {
        if (cardContainer != null) {
            ScaleTransition st = new ScaleTransition(Duration.millis(100), cardContainer);
            st.setToX(0.98);
            st.setToY(0.98);
            st.setCycleCount(2);
            st.setAutoReverse(true);
            st.play();
        }
    }
}