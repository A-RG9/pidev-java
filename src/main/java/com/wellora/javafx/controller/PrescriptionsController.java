package com.wellora.javafx.controller;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.wellora.javafx.model.Ordonnance;
import com.wellora.javafx.service.OrdonnanceServices;
import com.wellcare.javafx.model.User;
import com.wellcare.javafx.util.SceneManager;
import com.wellora.javafx.model.Consultation;
import com.wellora.javafx.service.ConsulationServices;

public class PrescriptionsController {

    // Sidebar components
    @FXML
    private VBox santeSubmenuContainer;
    @FXML
    private VBox submenuContainer;
    @FXML
    private VBox prescriptionsCardsContainer;
    
    private final OrdonnanceServices ordonnanceServices = new OrdonnanceServices();
    private List<Ordonnance> prescriptions = new ArrayList<>();

    @FXML
    private void initialize() {
        // Load prescriptions from database
        loadPrescriptions();
        // Display the loaded prescriptions
        displayPrescriptions();
    }
    
    private void displayPrescriptions() {
        if (prescriptionsCardsContainer == null) {
            System.out.println("prescriptionsCardsContainer is NULL!");
            return;
        }
        
        prescriptionsCardsContainer.getChildren().clear();
        
        if (prescriptions.isEmpty()) {
            prescriptionsCardsContainer.getChildren().add(
                createEmptyState("Aucune ordonnance", "Aucune ordonnance trouvée"));
            return;
        }
        
        for (Ordonnance prescription : prescriptions) {
            prescriptionsCardsContainer.getChildren().add(createPrescriptionCard(prescription));
        }
    }
    
    private javafx.scene.Node createEmptyState(String title, String message) {
        VBox box = new VBox(8);
        box.setStyle("-fx-alignment: center; -fx-padding: 24;");
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #6b7280;");
        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #9ca3af;");
        box.getChildren().addAll(titleLabel, messageLabel);
        return box;
    }
    
    private javafx.scene.Node createPrescriptionCard(Ordonnance prescription) {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: white; -fx-padding: 16; -fx-background-radius: 8; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0, 0, 1);");
        
        // Header with medication and doctor
        HBox header = new HBox();
        header.setStyle("-fx-alignment: center-left; -fx-spacing: 12;");
        
        StackPane icon = new StackPane();
        icon.setStyle("-fx-min-width: 40px; -fx-min-height: 40px; -fx-background-color: #dbeafe; -fx-background-radius: 8;");
        Label iconLabel = new Label("\uf5a3"); // fa-pills
        iconLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #3b82f6;");
        icon.getChildren().add(iconLabel);
        
        VBox info = new VBox(2);
        Label nameLabel = new Label(prescription.getMedicament() != null ? prescription.getMedicament() : "Médicament");
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #111827;");
        Label doctorLabel = new Label("Dr. Mabrouk"); // Default doctor name
        doctorLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");
        info.getChildren().addAll(nameLabel, doctorLabel);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        
        // Status badge - use actual status from prescription
        String statusText = prescription.getStatus();
        if (statusText == null || statusText.trim().isEmpty()) {
            statusText = "active";
        }
        String statusColor = statusText.equalsIgnoreCase("active") ? "#dcfce7" : "#fee2e2";
        String statusTextColor = statusText.equalsIgnoreCase("active") ? "#16a34a" : "#dc2626";
        
        Label statusLabel = new Label(statusText.substring(0, 1).toUpperCase() + statusText.substring(1).toLowerCase());
        statusLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 4 8 4 8; " +
                           "-fx-background-radius: 12; -fx-background-color: " + statusColor + "; -fx-text-fill: " + statusTextColor + ";");
        
        header.getChildren().addAll(icon, info, spacer, statusLabel);
        
        // Details section
        VBox details = new VBox(8);
        details.setStyle("-fx-padding: 0 0 0 0;");
        
        // Dosage
        HBox dosageRow = new HBox();
        dosageRow.setStyle("-fx-alignment: center-left; -fx-spacing: 8;");
        Label dosageIcon = new Label("\uf4b8"); // fa-capsules
        dosageIcon.setStyle("-fx-font-size: 14px; -fx-text-fill: #6b7280; -fx-min-width: 20;");
        Label dosageLabel = new Label(prescription.getDosage() != null ? prescription.getDosage() : "À définir");
        dosageLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #4b5563;");
        dosageRow.getChildren().addAll(dosageIcon, dosageLabel);
        
        // Duration
        HBox durationRow = new HBox();
        durationRow.setStyle("-fx-alignment: center-left; -fx-spacing: 8;");
        Label durationIcon = new Label("\uf017"); // fa-clock
        durationIcon.setStyle("-fx-font-size: 14px; -fx-text-fill: #6b7280; -fx-min-width: 20;");
        Label durationLabel = new Label(prescription.getDureeTraitement() != null ? prescription.getDureeTraitement() : "À définir");
        durationLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #4b5563;");
        durationRow.getChildren().addAll(durationIcon, durationLabel);
        
        // Date
        HBox dateRow = new HBox();
        dateRow.setStyle("-fx-alignment: center-left; -fx-spacing: 8;");
        Label dateIcon = new Label("\uf133"); // fa-calendar
        dateIcon.setStyle("-fx-font-size: 14px; -fx-text-fill: #6b7280; -fx-min-width: 20;");
        Label dateLabel = new Label(prescription.getDateOrdonnance() != null ? 
                                   "Du " + prescription.getDateOrdonnance().toString() : "Date à définir");
        dateLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #4b5563;");
        dateRow.getChildren().addAll(dateIcon, dateLabel);
        
        details.getChildren().addAll(dosageRow, durationRow, dateRow);
        
        // Action buttons
        HBox actions = new HBox();
        actions.setStyle("-fx-alignment: center-left; -fx-spacing: 16; -fx-padding: 12 0 0 0; -fx-border-color: #f3f4f6; -fx-border-width: 1 0 0 0;");
        
        Button renewBtn = new Button("\uf01e Renouveler");
        renewBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #00A790; -fx-font-size: 12px; -fx-cursor: hand;");
        
        Button downloadBtn = new Button("\uf019 Télécharger");
        downloadBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #00A790; -fx-font-size: 12px; -fx-cursor: hand;");
        
        actions.getChildren().addAll(renewBtn, downloadBtn);
        
        card.getChildren().addAll(header, details, actions);
        
        // Add a left border for active prescriptions
        card.setStyle(card.getStyle() + "; -fx-border-color: #00A790; -fx-border-width: 0 0 0 4;");
        
        return card;
    }
    
    private void loadPrescriptions() {
        try {
            prescriptions = ordonnanceServices.ShowOrdonnance();
            System.out.println("=== DEBUG: Loaded " + prescriptions.size() + " prescriptions from database ===");
            
            // If no results, add sample data for demo purposes
            if (prescriptions.isEmpty()) {
                System.out.println("=== DEBUG: No data in database, adding sample data ===");
                addSampleData();
                prescriptions = ordonnanceServices.ShowOrdonnance();
            }
            
            // Filter by logged-in patient
            User currentUser = SceneManager.getInstance().getCurrentUser();
            if (currentUser != null && currentUser.getUuid() != null) {
                ConsulationServices consultationService = new ConsulationServices();
                List<Consultation> allConsultations = consultationService.ShowConsultation();
                Set<Integer> patientConsultationIds = allConsultations.stream()
                        .filter(c -> currentUser.getUuid().equals(c.getPatientId()))
                        .map(Consultation::getId)
                        .collect(Collectors.toSet());
                
                prescriptions = prescriptions.stream()
                        .filter(p -> patientConsultationIds.contains(p.getConsultationId()))
                        .collect(Collectors.toList());
            }

            // Debug: Print each result
            for (Ordonnance o : prescriptions) {
                System.out.println("  - " + o.getMedicament() + " (" + o.getStatus() + ")");
            }
        } catch (Exception e) {
            System.out.println("=== DEBUG: Database error - " + e.getMessage() + " ===");
            showAlert("Erreur de connexion", "Impossible de charger les ordonnances: " + e.getMessage());
            prescriptions = new ArrayList<>();
        }
    }
    
    private void addSampleData() {
        try {
            // Add sample prescriptions for demo
            Ordonnance o1 = new Ordonnance();
            o1.setId(1);
            o1.setMedicament("Doliprane 1000mg");
            o1.setDosage("1 comprimé 3 fois par jour");
            o1.setDureeTraitement("5 jours");
            o1.setDateOrdonnance(java.time.LocalDate.now().minusDays(5));
            o1.setInstructions("À prendre après les repas");
            o1.setForme("Comprimés");
            o1.setFrequency("3 fois par jour");
            o1.setDiagnosisCode("M79.1");
            o1.setStatus("active");
            ordonnanceServices.AddOrdonnance(o1);
            
            Ordonnance o2 = new Ordonnance();
            o2.setId(2);
            o2.setMedicament("Amoxicilline 500mg");
            o2.setDosage("1 gélule 2 fois par jour");
            o2.setDureeTraitement("7 jours");
            o2.setDateOrdonnance(java.time.LocalDate.now().minusDays(10));
            o2.setInstructions("À prendre pendant les repas");
            o2.setForme("Gélules");
            o2.setFrequency("2 fois par jour");
            o2.setDiagnosisCode("J01.9");
            o2.setStatus("active");
            ordonnanceServices.AddOrdonnance(o2);
            
            Ordonnance o3 = new Ordonnance();
            o3.setId(3);
            o3.setMedicament("Smecta");
            o3.setDosage("1 sachet 3 fois par jour");
            o3.setDureeTraitement("3 jours");
            o3.setDateOrdonnance(java.time.LocalDate.now().minusDays(15));
            o3.setInstructions("À prendre entre les repas");
            o3.setForme("Sachets");
            o3.setFrequency("3 fois par jour");
            o3.setDiagnosisCode("K59.1");
            o3.setStatus("completed");
            ordonnanceServices.AddOrdonnance(o3);
            
            System.out.println("=== DEBUG: Sample data added successfully ===");
        } catch (Exception e) {
            System.out.println("=== DEBUG: Error adding sample data: " + e.getMessage() + " ===");
        }
    }
    
    private boolean submenuVisible = true;
    
    @FXML
    private void toggleSubmenu(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String buttonText = clickedButton.getText();
        
        if (buttonText.contains("Santé")) {
            submenuVisible = !submenuVisible;
            if (santeSubmenuContainer != null) {
                santeSubmenuContainer.setVisible(submenuVisible);
                santeSubmenuContainer.setManaged(submenuVisible);
            }
        } else {
            // Toggle rendez-vous submenu
            submenuVisible = !submenuVisible;
            if (submenuContainer != null) {
                submenuContainer.setVisible(submenuVisible);
                submenuContainer.setManaged(submenuVisible);
            }
        }
        System.out.println("Submenu toggled: " + submenuVisible + " for " + buttonText);
    }
    
    @FXML
    private void handleMenuClick(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String buttonText = clickedButton.getText().trim();
        
        System.out.println("Menu clicked: " + buttonText);

        try {
            FXMLLoader loader;
            Parent root;
            Scene newScene;
            Stage stage = (Stage) clickedButton.getScene().getWindow();

            if (buttonText.contains("Tableau de bord")) {
                loader = new FXMLLoader(getClass().getResource("/fxml/appointments.fxml"));
                root = loader.load();
                newScene = new Scene(root, 1200, 800);
                if (getClass().getResource("/css/styles.css") != null) {
                    newScene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
                }
                stage.setScene(newScene);
                stage.setTitle("WellCare Connect - Tableau de bord");
            } else if (buttonText.contains("Mes Rendez-vous")) {
                loader = new FXMLLoader(getClass().getResource("/fxml/appointments.fxml"));
                root = loader.load();
                newScene = new Scene(root, 1200, 800);
                if (getClass().getResource("/css/styles.css") != null) {
                    newScene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
                }
                stage.setScene(newScene);
                stage.setTitle("WellCare Connect - Mes Rendez-vous");
            } else if (buttonText.contains("Trouver un Médecin")) {
                loader = new FXMLLoader(getClass().getResource("/fxml/doctor-search.fxml"));
                root = loader.load();
                newScene = new Scene(root, 1200, 800);
                if (getClass().getResource("/css/styles.css") != null) {
                    newScene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
                }
                stage.setScene(newScene);
                stage.setTitle("WellCare Connect - Trouver un Médecin");
            } else if (buttonText.contains("Profil Médecin")) {
                loader = new FXMLLoader(getClass().getResource("/fxml/doctor-profile.fxml"));
                root = loader.load();
                newScene = new Scene(root, 1200, 800);
                if (getClass().getResource("/css/styles.css") != null) {
                    newScene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
                }
                stage.setScene(newScene);
                stage.setTitle("WellCare Connect - Profil Médecin");
            } else if (buttonText.contains("Prendre RDV")) {
                loader = new FXMLLoader(getClass().getResource("/fxml/booking.fxml"));
                root = loader.load();
                newScene = new Scene(root, 1200, 800);
                if (getClass().getResource("/css/styles.css") != null) {
                    newScene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
                }
                stage.setScene(newScene);
                stage.setTitle("WellCare Connect - Prendre RDV");
            } else if (buttonText.contains("Notes Cliniques")) {
                loader = new FXMLLoader(getClass().getResource("/fxml/clinical-notes.fxml"));
                root = loader.load();
                newScene = new Scene(root, 1200, 800);
                if (getClass().getResource("/css/styles.css") != null) {
                    newScene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
                }
                stage.setScene(newScene);
                stage.setTitle("WellCare Connect - Notes Cliniques");
            } else if (buttonText.contains("Mes Ordonnances")) {
                loader = new FXMLLoader(getClass().getResource("/fxml/prescriptions.fxml"));
                root = loader.load();
                newScene = new Scene(root, 1200, 800);
                if (getClass().getResource("/css/styles.css") != null) {
                    newScene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
                }
                stage.setScene(newScene);
                stage.setTitle("WellCare Connect - Ordonnances");
            } else if (buttonText.contains("Résultats de Laboratoire")) {
                loader = new FXMLLoader(getClass().getResource("/fxml/lab-results.fxml"));
                root = loader.load();
                newScene = new Scene(root, 1200, 800);
                if (getClass().getResource("/css/styles.css") != null) {
                    newScene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
                }
                stage.setScene(newScene);
                stage.setTitle("WellCare Connect - Résultats de Laboratoire");
            } else if (buttonText.contains("Mon Profil")) {
                System.out.println("Navigate to profile");
            } else if (buttonText.contains("Paramètres")) {
                System.out.println("Navigate to settings");
            } else if (buttonText.contains("Déconnexion")) {
                System.out.println("Logout");
            }
            stage.show();
        } catch (Exception e) {
            System.out.println("Navigation error: " + e.getMessage());
            showAlert("Erreur", "Une erreur s'est produite lors de la navigation: " + e.getMessage());
        }
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
