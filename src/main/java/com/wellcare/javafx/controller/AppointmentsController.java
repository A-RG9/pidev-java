package com.wellcare.javafx.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import com.wellcare.javafx.model.Consultation;
import com.wellcare.javafx.service.ConsulationServices;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class AppointmentsController {

    @FXML
    private Text upcomingCount;

    @FXML
    private Text completedCount;

    @FXML
    private Text pendingCount;

    @FXML
    private Text cancelledCount;

    @FXML
    private TabPane tabPane;

    @FXML
    private VBox submenuContainer;
    
    @FXML
    private VBox upcomingList;
    
    private boolean rendezvousSubmenuVisible = false;
    private boolean santeSubmenuVisible = false;

    @FXML
    private VBox pendingList;

    @FXML
    private VBox completedList;

    @FXML
    private VBox cancelledList;

    @FXML
    private StackPane loadingPane;

    private ConsulationServices consultationService;

    private List<Consultation> allConsultations;

    public AppointmentsController() {
        this.consultationService = new ConsulationServices();
    }

    @FXML
    private void initialize() {
        System.out.println("AppointmentsController initializing...");
        loadAppointments();
    }

    private void loadAppointments() {
        showLoading(true);
        try {
            allConsultations = consultationService.ShowConsultation();
            System.out.println("Loaded " + allConsultations.size() + " consultations from database");
            for (Consultation c : allConsultations) {
                System.out.println("  - ID: " + c.getId() + ", Date: " + c.getDateConsultation() + ", Status: " + c.getStatus() + ", Reason: " + c.getReasonForVisit());
            }
            updateStats();
            populateLists();
        } catch (SQLException e) {
            showError("Failed to load appointments: " + e.getMessage());
            e.printStackTrace();
        } finally {
            showLoading(false);
        }
    }

    private void updateStats() {
        int upcoming = 0;
        int completed = 0;
        int pending = 0;
        int cancelled = 0;

        LocalDate today = LocalDate.now();

        for (Consultation c : allConsultations) {
            String status = c.getStatus();
            if (status == null) continue;

            switch (status.toLowerCase()) {
                case "confirmed":
                case "upcoming":
                    upcoming++;
                    break;
                case "completed":
                    completed++;
                    break;
                case "pending":
                case "waiting":
                    pending++;
                    break;
                case "cancelled":
                    cancelled++;
                    break;
            }
        }

        upcomingCount.setText(String.valueOf(upcoming));
        completedCount.setText(String.valueOf(completed));
        pendingCount.setText(String.valueOf(pending));
        cancelledCount.setText(String.valueOf(cancelled));
    }

    private void populateLists() {
        upcomingList.getChildren().clear();
        pendingList.getChildren().clear();
        completedList.getChildren().clear();
        cancelledList.getChildren().clear();

        System.out.println("populateLists: Processing " + allConsultations.size() + " consultations");

        for (Consultation c : allConsultations) {
            VBox card = createAppointmentCard(c);
            String status = c.getStatus();

            System.out.println("  Processing consultation ID: " + c.getId() + ", status: '" + status + "'");

            if (status == null) {
                upcomingList.getChildren().add(card);
                System.out.println("    -> Added to upcoming (status null)");
                continue;
            }

            switch (status.toLowerCase()) {
                case "confirmed":
                case "upcoming":
                    upcomingList.getChildren().add(card);
                    System.out.println("    -> Added to upcoming");
                    break;
                case "completed":
                    completedList.getChildren().add(card);
                    System.out.println("    -> Added to completed");
                    break;
                case "pending":
                case "waiting":
                    pendingList.getChildren().add(card);
                    System.out.println("    -> Added to pending");
                    break;
                case "cancelled":
                    cancelledList.getChildren().add(card);
                    System.out.println("    -> Added to cancelled");
                    break;
                default:
                    upcomingList.getChildren().add(card);
                    System.out.println("    -> Added to upcoming (default)");
            }
        }

        // Show empty state if no appointments
        if (upcomingList.getChildren().isEmpty()) {
            upcomingList.getChildren().add(createEmptyState("No upcoming appointments", "You don't have any upcoming appointments scheduled."));
        }
        if (pendingList.getChildren().isEmpty()) {
            pendingList.getChildren().add(createEmptyState("No pending appointments", "You don't have any appointments waiting for confirmation."));
        }
        if (completedList.getChildren().isEmpty()) {
            completedList.getChildren().add(createEmptyState("No completed appointments", "Your completed appointments will appear here."));
        }
        if (cancelledList.getChildren().isEmpty()) {
            cancelledList.getChildren().add(createEmptyState("No cancelled appointments", "Good news! You don't have any cancelled appointments."));
        }
        
        // Auto-select Pending tab if there are pending appointments (e.g., after booking)
        boolean hasPending = allConsultations.stream()
            .anyMatch(c -> "pending".equalsIgnoreCase(c.getStatus()));
        if (hasPending) {
            System.out.println("Selecting Pending tab since there are pending appointments");
            tabPane.getSelectionModel().select(1); // Index 1 = Pending tab
        }
    }

    private VBox createAppointmentCard(Consultation consultation) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; "
                    + "-fx-border-color: #e5e7eb; -fx-border-radius: 12; "
                    + "-fx-effect: dropshadow(gaussian, #00000010, 2, 0, 0, 1);");

        // Date section
        HBox dateSection = new HBox(16);
        dateSection.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        VBox dateBox = new VBox(4);
        dateBox.setAlignment(javafx.geometry.Pos.CENTER);
        dateBox.setPadding(new Insets(12));
        dateBox.setStyle("-fx-background-color: #eff6ff; -fx-background-radius: 8;");

        Text monthText = new Text(consultation.getDateConsultation() != null ?
            consultation.getDateConsultation().format(DateTimeFormatter.ofPattern("MMM")) : "N/A");
        monthText.setFont(Font.font(12));
        monthText.setStyle("-fx-fill: #2563eb;");

        Text dayText = new Text(consultation.getDateConsultation() != null ?
            String.valueOf(consultation.getDateConsultation().getDayOfMonth()) : "-");
        dayText.setFont(Font.font("System", FontWeight.BOLD, 24));
        dayText.setStyle("-fx-fill: #1f2937;");

        Text weekdayText = new Text(consultation.getDateConsultation() != null ?
            consultation.getDateConsultation().format(DateTimeFormatter.ofPattern("EEE")) : "-");
        weekdayText.setStyle("-fx-fill: #6b7280;");
        weekdayText.setFont(Font.font(11));

        dateBox.getChildren().addAll(monthText, dayText, weekdayText);

        // Info section
        VBox infoBox = new VBox(8);
        infoBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        HBox.setHgrow(infoBox, javafx.scene.layout.Priority.ALWAYS);

        // Doctor name with status badge
        HBox headerBox = new HBox(8);
        headerBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Text doctorName = new Text("Dr. " + (consultation.getReasonForVisit() != null ? 
            consultation.getReasonForVisit() : "No Doctor"));
        doctorName.setFont(Font.font("System", FontWeight.BOLD, 16));

        Label statusBadge = new Label(consultation.getStatus() != null ? 
            consultation.getStatus() : "Unknown");
        statusBadge.setPadding(new Insets(2, 8, 2, 8));
        statusBadge.setStyle(getStatusStyle(consultation.getStatus()));

        headerBox.getChildren().addAll(doctorName, statusBadge);

        Text specialty = new Text(consultation.getConsultationType() != null ?
            consultation.getConsultationType() : "General");
        specialty.setStyle("-fx-fill: #2563eb;");
        specialty.setFont(Font.font(13));

        // Time and duration
        HBox timeBox = new HBox(16);
        Text timeText = new Text("🕐 " + (consultation.getTimeConsultation() != null ?
            consultation.getTimeConsultation().format(DateTimeFormatter.ofPattern("HH:mm")) : "N/A"));
        timeText.setStyle("-fx-fill: #6b7280;");

        Text durationText = new Text("⏱ " + (consultation.getDuration() != null ?
            consultation.getDuration() + " min" : "N/A"));
        durationText.setStyle("-fx-fill: #6b7280;");

        Text locationText = new Text("📍 " + (consultation.getLocation() != null ?
            consultation.getLocation() : "TBD"));
        locationText.setStyle("-fx-fill: #6b7280;");

        timeBox.getChildren().addAll(timeText, durationText, locationText);

        infoBox.getChildren().addAll(headerBox, specialty, timeBox);

        // Actions section
        HBox actionsBox = new HBox(8);

        Button detailsBtn = new Button("Details");
        detailsBtn.setStyle("-fx-background-color: transparent; -fx-border-color: #6b7280; -fx-border-radius: 6; -fx-background-radius: 6; -fx-text-fill: #6b7280;");
        detailsBtn.setOnAction(e -> showAppointmentDetails(consultation));

        Button rescheduleBtn = new Button("Reschedule");
        rescheduleBtn.setStyle("-fx-background-color: transparent; -fx-border-color: #6b7280; -fx-border-radius: 6; -fx-background-radius: 6; -fx-text-fill: #6b7280;");
        rescheduleBtn.setOnAction(e -> handleReschedule(consultation));

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-background-color: transparent; -fx-border-color: #dc2626; -fx-border-radius: 6; -fx-background-radius: 6; -fx-text-fill: #dc2626;");
        cancelBtn.setOnAction(e -> handleCancel(consultation));

        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle("-fx-background-color: transparent; -fx-border-color: #be185d; -fx-border-radius: 6; -fx-background-radius: 6; -fx-text-fill: #be185d;");
        deleteBtn.setOnAction(e -> handleDelete(consultation));

        actionsBox.getChildren().addAll(detailsBtn, rescheduleBtn, cancelBtn, deleteBtn);

        // Add all to info box
        VBox.setMargin(infoBox, new Insets(0, 0, 0, 0));
        infoBox.getChildren().add(actionsBox);

        dateSection.getChildren().addAll(dateBox, infoBox);
        card.getChildren().add(dateSection);

        return card;
    }

    private VBox createEmptyState(String title, String message) {
        VBox box = new VBox(8);
        box.setAlignment(javafx.geometry.Pos.CENTER);
        box.setPadding(new Insets(32));
        box.setStyle("-fx-background-color: #f9fafb; -fx-background-radius: 12;");

        Text titleText = new Text(title);
        titleText.setStyle("-fx-fill: #1f2937; -fx-font-size: 16px; -fx-font-weight: 600;");

        Text messageText = new Text(message);
        messageText.setStyle("-fx-fill: #6b7280;");

        box.getChildren().addAll(titleText, messageText);
        return box;
    }

    private String getStatusStyle(String status) {
        if (status == null) return "-fx-background-color: #f3f4f6; -fx-text-fill: #6b7280;";
        
        switch (status.toLowerCase()) {
            case "confirmed":
            case "upcoming":
                return "-fx-background-color: #d1fae5; -fx-text-fill: #059669; -fx-background-radius: 4;";
            case "pending":
            case "waiting":
                return "-fx-background-color: #fef3c7; -fx-text-fill: #d97706; -fx-background-radius: 4;";
            case "completed":
                return "-fx-background-color: #dbeafe; -fx-text-fill: #2563eb; -fx-background-radius: 4;";
            case "cancelled":
                return "-fx-background-color: #fee2e2; -fx-text-fill: #dc2626; -fx-background-radius: 4;";
            default:
                return "-fx-background-color: #f3f4f6; -fx-text-fill: #6b7280;";
        }
    }

    private void showAppointmentDetails(Consultation consultation) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Appointment Details");
        alert.setHeaderText("Dr. " + consultation.getReasonForVisit());
        
        String content = "Date: " + consultation.getDateConsultation() + "\n" +
                        "Time: " + consultation.getTimeConsultation() + "\n" +
                        "Type: " + consultation.getConsultationType() + "\n" +
                        "Location: " + consultation.getLocation() + "\n" +
                        "Status: " + consultation.getStatus() + "\n" +
                        "Notes: " + (consultation.getNotes() != null ? consultation.getNotes() : "N/A");
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleBookNewAppointment() {
        try {
            // Navigate to doctor-search to find a doctor first
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/doctor-search.fxml"));
            Parent doctorSearchRoot = loader.load();
            
            // Get the stage from the current window
            Stage stage = null;
            if (tabPane != null && tabPane.getScene() != null) {
                stage = (Stage) tabPane.getScene().getWindow();
            }
            
            if (stage == null) {
                showError("Cannot navigate - window not available");
                return;
            }
            
            // Create new scene with doctor-search
            Scene newScene = new Scene(doctorSearchRoot, 1200, 800);
            
            // Apply stylesheet to new scene
            String css = getClass().getResource("/css/styles.css").toExternalForm();
            if (css != null) {
                newScene.getStylesheets().add(css);
            }
            
            stage.setScene(newScene);
            stage.setTitle("WellCare Connect - Find a Doctor");
            stage.show();
        } catch (Exception e) {
            showError("Failed to navigate to doctor search: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleReschedule(Consultation consultation) {
        try {
            System.out.println("Rescheduling consultation ID: " + consultation.getId());
            
            // Load the booking FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/booking.fxml"));
            Parent bookingRoot = loader.load();
            
            // Get the BookingController and set the reschedule data
            BookingController bookingController = loader.getController();
            bookingController.setRescheduleData(consultation);
            
            // Navigate to booking page for reschedule
            Stage stage = null;
            if (tabPane != null && tabPane.getScene() != null) {
                stage = (Stage) tabPane.getScene().getWindow();
            }
            
            if (stage != null) {
                Scene newScene = new Scene(bookingRoot, 1200, 800);
                String css = getClass().getResource("/css/styles.css").toExternalForm();
                if (css != null) {
                    newScene.getStylesheets().add(css);
                }
                stage.setScene(newScene);
                stage.setTitle("WellCare Connect - Reschedule Appointment");
                stage.show();
            } else {
                showError("Could not navigate to booking page");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to open reschedule: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel(Consultation consultation) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Cancel");
        alert.setHeaderText("Cancel Appointment");
        alert.setContentText("Are you sure you want to cancel this appointment?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                consultation.setStatus("cancelled");
                consultationService.ModifyConsultation(consultation.getId(), consultation);
                showInfo("Appointment cancelled successfully!");
                loadAppointments();
            } catch (SQLException e) {
                showError("Failed to cancel: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleDelete(Consultation consultation) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete");
        alert.setHeaderText("Delete Appointment");
        alert.setContentText("Are you sure you want to delete this appointment? This cannot be undone.");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                consultationService.DeleteConsultation(consultation.getId());
                showInfo("Appointment deleted successfully!");
                loadAppointments();
            } catch (SQLException e) {
                showError("Failed to delete: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleFindDoctor() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Find a Doctor");
        alert.setContentText("This would navigate to the doctor search screen.");
        alert.showAndWait();
    }

    @FXML
    private void handleMedicalRecords() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Medical Records");
        alert.setContentText("This would navigate to medical records.");
        alert.showAndWait();
    }

    @FXML
    private void handleSupport() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Support");
        alert.setContentText("Contact support for assistance.");
        alert.showAndWait();
    }

    private void showLoading(boolean show) {
        loadingPane.setVisible(show);
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    @FXML
    private void handleMenuClick(javafx.event.ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String buttonText = clickedButton.getText();
        
        if (buttonText.contains("Tableau de bord")) {
            System.out.println("Navigate to dashboard");
        } else if (buttonText.contains("Mes Rendez-vous")) {
            System.out.println("Already on appointments");
        } else if (buttonText.contains("Trouver un Médecin")) {
            System.out.println("Navigate to doctor search");
        } else if (buttonText.contains("Profil Médecin")) {
            System.out.println("Navigate to doctor profile");
        } else if (buttonText.contains("Prendre RDV")) {
            System.out.println("Navigate to booking");
        } else if (buttonText.contains("Mon Profil")) {
            System.out.println("Navigate to profile");
        } else if (buttonText.contains("Paramètres")) {
            System.out.println("Navigate to settings");
        } else if (buttonText.contains("Déconnexion")) {
            System.out.println("Logout");
        }
    }
    
    @FXML
    private void toggleSubmenu(javafx.event.ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String buttonText = clickedButton.getText();
        
        if (buttonText.contains("Santé")) {
            // Toggle Santé submenu
            santeSubmenuVisible = !santeSubmenuVisible;
            // Note: No Santé submenu in appointments.fxml, but keep for consistency
            System.out.println("Santé button clicked");
        } else if (buttonText.contains("Rendez-vous")) {
            // Toggle Rendez-vous submenu
            rendezvousSubmenuVisible = !rendezvousSubmenuVisible;
            if (submenuContainer != null) {
                submenuContainer.setVisible(rendezvousSubmenuVisible);
                submenuContainer.setManaged(rendezvousSubmenuVisible);
            }
        }
        // Nutrition and Fitness buttons don't have submenus - do nothing
        System.out.println("Santé: " + santeSubmenuVisible + " | Rendez-vous: " + rendezvousSubmenuVisible);
    }
}