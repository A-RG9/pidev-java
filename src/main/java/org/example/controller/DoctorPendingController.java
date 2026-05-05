package org.example.controller;

import org.example.service.EmailService;
import java.time.format.DateTimeFormatter;
import java.time.LocalTime;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.entities.Consultation;
import org.example.service.ConsulationServices;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DoctorPendingController {

    @FXML private VBox appointmentsContainer;
    @FXML private VBox emptyState;
    @FXML private Label pendingCountLabel;
    @FXML private Text legendCountText;

    private ConsulationServices consultationService;
    private List<Consultation> pendingAppointments = new ArrayList<>();

    @FXML
    public void initialize() {
        consultationService = new ConsulationServices();
        loadPendingAppointments();
    }

    private void loadPendingAppointments() {
        showLoading(true);
        new Thread(() -> {
            try {
                List<Consultation> all = consultationService.ShowConsultation();
                pendingAppointments = all.stream()
                        .filter(c -> c.getStatus() != null && c.getStatus().equalsIgnoreCase("pending"))
                        .collect(Collectors.toList());
                Platform.runLater(() -> {
                    updateUI();
                    showLoading(false);
                });
            } catch (SQLException e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showError("Impossible de charger les demandes : " + e.getMessage());
                    showLoading(false);
                });
            }
        }).start();
    }

    private void updateUI() {
        appointmentsContainer.getChildren().clear();
        if (pendingAppointments.isEmpty()) {
            emptyState.setVisible(true);
            pendingCountLabel.setText("0 en attente");
            legendCountText.setText("0 demande(s) en attente");
            return;
        }
        emptyState.setVisible(false);
        pendingCountLabel.setText(pendingAppointments.size() + " en attente");
        legendCountText.setText(pendingAppointments.size() + " demande(s) en attente");

        for (Consultation apt : pendingAppointments) {
            VBox card = createAppointmentCard(apt);
            appointmentsContainer.getChildren().add(card);
        }
    }

    private VBox createAppointmentCard(Consultation apt) {
        VBox card = new VBox(12);
        card.setId("appointment-" + apt.getId());
        card.setStyle("-fx-background-color: white; -fx-border-color: #e5e7eb; -fx-border-width: 0 0 0 4; -fx-border-radius: 8; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 4, 0, 0, 1);");
        card.setPadding(new Insets(16));

        String patientName = "Patient #" + apt.getId();
        HBox headerBox = new HBox(12);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        Label nameLabel = new Label(patientName);
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1f2937;");
        Label statusBadge = new Label(" En attente ");
        statusBadge.setStyle("-fx-background-color: #fef3c7; -fx-text-fill: #b45309; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 12;");
        headerBox.getChildren().addAll(nameLabel, statusBadge);

        GridPane detailsGrid = new GridPane();
        detailsGrid.setHgap(20);
        detailsGrid.setVgap(10);
        detailsGrid.setPadding(new Insets(8, 0, 8, 0));

        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");

        addDetailRow(detailsGrid, 0, "📅 Date:", apt.getDateConsultation() != null ? apt.getDateConsultation().format(dateFmt) : "N/A");
        addDetailRow(detailsGrid, 1, "⏱️ Heure:", apt.getTimeConsultation() instanceof LocalTime ? apt.getTimeConsultation().format(timeFmt) : "N/A");
        addDetailRow(detailsGrid, 2, "⏳ Durée:", apt.getDuration() != null ? apt.getDuration() + " min" : "N/A");
        addDetailRow(detailsGrid, 3, "🏷️ Type:", apt.getConsultationType() != null ? apt.getConsultationType() : "Non spécifié");
        addDetailRow(detailsGrid, 4, "🎥 Mode:", apt.getAppointmentMode() != null ? apt.getAppointmentMode() : "Présentiel");
        addDetailRow(detailsGrid, 5, "📍 Lieu:", apt.getLocation() != null ? apt.getLocation() : "Cabinet");

        VBox reasonBox = new VBox(6);
        reasonBox.setStyle("-fx-background-color: #f9fafb; -fx-padding: 12; -fx-background-radius: 8;");
        Label reasonTitle = new Label("📝 Motif:");
        reasonTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        Label reasonText = new Label(apt.getReasonForVisit() != null ? apt.getReasonForVisit() : "Non spécifié");
        reasonText.setWrapText(true);
        reasonBox.getChildren().addAll(reasonTitle, reasonText);

        if (apt.getSymptomsDescription() != null && !apt.getSymptomsDescription().isBlank()) {
            Label symptomsTitle = new Label("🌡️ Symptômes:");
            symptomsTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
            Label symptomsText = new Label(apt.getSymptomsDescription());
            symptomsText.setWrapText(true);
            reasonBox.getChildren().addAll(symptomsTitle, symptomsText);
        }

        HBox buttonsBox = new HBox(12);
        buttonsBox.setAlignment(Pos.CENTER_RIGHT);
        Button acceptBtn = new Button("✅ Accepter");
        acceptBtn.setStyle("-fx-background-color: #22c55e; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 8;");
        acceptBtn.setOnAction(e -> acceptAppointment(apt, acceptBtn));
        Button rejectBtn = new Button("❌ Refuser");
        rejectBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 8;");
        rejectBtn.setOnAction(e -> rejectAppointment(apt, rejectBtn));
        buttonsBox.getChildren().addAll(acceptBtn, rejectBtn);

        HBox footerBox = new HBox();
        footerBox.setAlignment(Pos.CENTER_LEFT);
        String createdAt = (apt.getDateConsultation() != null && apt.getTimeConsultation() instanceof LocalTime) ?
                apt.getDateConsultation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " " +
                        apt.getTimeConsultation().format(DateTimeFormatter.ofPattern("HH:mm")) : "N/A";
        Label requestedLabel = new Label("Demandé le " + createdAt);
        requestedLabel.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 11px;");
        footerBox.getChildren().add(requestedLabel);

        card.getChildren().addAll(headerBox, detailsGrid, reasonBox, buttonsBox, footerBox);
        return card;
    }

    private void addDetailRow(GridPane grid, int row, String label, String value) {
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #6b7280; -fx-font-size: 13px;");
        Label val = new Label(value);
        val.setStyle("-fx-text-fill: #1f2937; -fx-font-size: 13px;");
        grid.add(lbl, 0, row);
        grid.add(val, 1, row);
    }

    private void acceptAppointment(Consultation apt, Button button) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Accepter le rendez-vous");
        confirm.setHeaderText("Accepter ce rendez-vous ?");
        confirm.setContentText("Il sera ajouté à votre planning et le patient sera notifié.");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        button.setText("⏳ Acceptation...");
        button.setDisable(true);

        new Thread(() -> {
            try {
                apt.setStatus("confirmed");
                consultationService.ModifyConsultation(apt.getId(), apt);

                // ✨ Send email notification ✨
                sendEmailNotification(apt);

                Platform.runLater(() -> {
                    removeCardWithAnimation(apt.getId());
                    showToast("Rendez-vous accepté !", Color.GREEN);
                    if (pendingAppointments.isEmpty()) updateUI();
                });
            } catch (SQLException e) {
                Platform.runLater(() -> {
                    button.setText("✅ Accepter");
                    button.setDisable(false);
                    showError("Erreur lors de l'acceptation : " + e.getMessage());
                });
            }
        }).start();
    }

    private void rejectAppointment(Consultation apt, Button button) {
        TextInputDialog reasonDialog = new TextInputDialog();
        reasonDialog.setTitle("Refuser le rendez-vous");
        reasonDialog.setHeaderText("Raison du refus");
        reasonDialog.setContentText("Veuillez indiquer la raison :");
        String reason = reasonDialog.showAndWait().orElse(null);
        if (reason == null) return;

        button.setText("⏳ Refus...");
        button.setDisable(true);

        new Thread(() -> {
            try {
                apt.setStatus("cancelled");
                consultationService.ModifyConsultation(apt.getId(), apt);
                Platform.runLater(() -> {
                    removeCardWithAnimation(apt.getId());
                    showToast("Rendez-vous refusé", Color.RED);
                    if (pendingAppointments.isEmpty()) updateUI();
                });
            } catch (SQLException e) {
                Platform.runLater(() -> {
                    button.setText("❌ Refuser");
                    button.setDisable(false);
                    showError("Erreur lors du refus : " + e.getMessage());
                });
            }
        }).start();
    }

    private void removeCardWithAnimation(int id) {
        Node card = appointmentsContainer.lookup("#appointment-" + id);
        if (card == null) return;
        pendingAppointments.removeIf(a -> a.getId() == id);

        FadeTransition fade = new FadeTransition(Duration.millis(200), card);
        fade.setFromValue(1);
        fade.setToValue(0);
        TranslateTransition slide = new TranslateTransition(Duration.millis(200), card);
        slide.setFromX(0);
        slide.setToX(400);
        ParallelTransition parallel = new ParallelTransition(fade, slide);
        parallel.setOnFinished(e -> {
            appointmentsContainer.getChildren().remove(card);
            pendingCountLabel.setText(pendingAppointments.size() + " en attente");
            legendCountText.setText(pendingAppointments.size() + " demande(s) en attente");
            if (pendingAppointments.isEmpty()) emptyState.setVisible(true);
        });
        parallel.play();
    }

    private void showToast(String message, Color color) {
        Stage stage = (Stage) appointmentsContainer.getScene().getWindow();
        Label toast = new Label(message);
        toast.setStyle("-fx-background-color: " + toRgb(color) + "; -fx-text-fill: white; -fx-padding: 10 20; -fx-background-radius: 8; -fx-font-size: 14px;");
        toast.setOpacity(0);

        Popup popup = new Popup();
        popup.getContent().add(toast);
        popup.setAutoFix(true);
        popup.setHideOnEscape(true);

        // Position near top-right of the stage
        popup.setX(stage.getX() + stage.getWidth() - 200);
        popup.setY(stage.getY() + 20);

        // Animate
        TranslateTransition moveIn = new TranslateTransition(Duration.millis(300), toast);
        moveIn.setFromY(-50);
        moveIn.setToY(0);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), toast);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        ParallelTransition showAnim = new ParallelTransition(moveIn, fadeIn);
        showAnim.setOnFinished(e -> {
            PauseTransition pause = new PauseTransition(Duration.seconds(3));
            pause.setOnFinished(ev -> {
                FadeTransition fadeOut = new FadeTransition(Duration.millis(500), toast);
                fadeOut.setToValue(0);
                fadeOut.setOnFinished(ev2 -> popup.hide());
                fadeOut.play();
            });
            pause.play();
        });
        showAnim.play();
    }

    private String toRgb(Color c) {
        return String.format("#%02X%02X%02X", (int)(c.getRed()*255), (int)(c.getGreen()*255), (int)(c.getBlue()*255));
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showLoading(boolean show) {
        // Optional: add a progress indicator
    }

    @FXML
    private void goBackToSchedule() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/doctor-schedule-week.fxml"));
            Stage stage = (Stage) appointmentsContainer.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendEmailNotification(Consultation apt) {
        try {
            // Static email for testing – bypass database lookup
            String patientEmail = "chahd.maaloul@esprit.tn";
            String patientName = "Chahd Maaloul";   // static name
            String doctorName = "WellCare Doctor";
            String appointmentDate = apt.getDateConsultation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            String appointmentTime = apt.getTimeConsultation() != null
                    ? apt.getTimeConsultation().format(DateTimeFormatter.ofPattern("HH:mm"))
                    : "N/A";

            System.out.println("Sending email to static address: " + patientEmail);
            EmailService.sendAppointmentAcceptedEmail(
                    patientEmail, patientName, doctorName, appointmentDate, appointmentTime
            );
            System.out.println("Email sending completed.");
        } catch (Exception e) {
            System.err.println("Email error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}