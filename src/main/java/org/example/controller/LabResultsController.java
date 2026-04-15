package org.example.controller;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.event.ActionEvent;
import javafx.stage.FileChooser;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.example.entities.Examens;
import org.example.service.ExamensServices;

public class LabResultsController {

    // Sidebar components
    @FXML
    private VBox sidebar;
    @FXML
    private VBox submenuContainer;
    @FXML
    private VBox labResultsContainer;
    @FXML
    private VBox labResultsCardsContainer;
    @FXML
    private VBox prescriptionSubmenuContainer;
    @FXML
    private VBox santeSubmenuContainer;

    private boolean submenuVisible = true;
    private boolean labResultsSubmenuVisible = true;
    private boolean santeSubmenuVisible = true;
    private ExamensServices examensServices = new ExamensServices();
    private List<Examens> labResults = new ArrayList<>();

    @FXML
    private void initialize() {
        // Load lab results from database
        loadLabResults();
        // Display the loaded lab results
        displayLabResults();
    }
    
    private void displayLabResults() {
        if (labResultsCardsContainer == null) {
            System.out.println("labResultsCardsContainer is NULL!");
            return;
        }
        
        labResultsCardsContainer.getChildren().clear();
        
        if (labResults.isEmpty()) {
            labResultsCardsContainer.getChildren().add(
                createEmptyState("Aucun résultat", "Aucun résultat de laboratoire disponible."));
            return;
        }
        
        for (Examens result : labResults) {
            labResultsCardsContainer.getChildren().add(createLabResultCard(result));
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
    
    private javafx.scene.Node createLabResultCard(Examens result) {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: white; -fx-padding: 16; -fx-background-radius: 8; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0, 0, 1);");
        
        HBox header = new HBox();
        header.setStyle("-fx-alignment: center-left; -fx-spacing: 12;");
        
        StackPane icon = new StackPane();
        icon.setStyle("-fx-min-width: 40px; -fx-min-height: 40px; -fx-background-color: #ecfdf5; -fx-background-radius: 8;");
        Label iconLabel = new Label("🧪");
        iconLabel.setStyle("-fx-font-size: 18px;");
        icon.getChildren().add(iconLabel);
        
        VBox info = new VBox(4);
        Label nameLabel = new Label(result.getNomExamen() != null ? result.getNomExamen() : result.getTypeExamen());
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #111827;");
        Label dateLabel = new Label(result.getDateExamen() != null ? result.getDateExamen().toString() : "");
        dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");
        info.getChildren().addAll(nameLabel, dateLabel);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        
        Label statusLabel = new Label(result.getStatus() != null ? result.getStatus().toUpperCase() : "COMPLETED");
        statusLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 4 8 4 8; " +
                           "-fx-background-color: #d1fae5; -fx-text-fill: #059669; -fx-background-radius: 4;");
        
        header.getChildren().addAll(icon, info, spacer, statusLabel);
        
        Label resultLabel = new Label(result.getResultat() != null ? result.getResultat() : "");
        resultLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #374151; -fx-wrap-text: true;");
        
        Label notesLabel = new Label(result.getNotes() != null ? "Notes: " + result.getNotes() : "");
        notesLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");
        
        // Add PDF view button if resultFile exists
        HBox buttonRow = new HBox();
        buttonRow.setStyle("-fx-spacing: 8;");
        
        // Always show Upload PDF button
        Button uploadPdfButton = new Button("📤 Upload PDF");
        uploadPdfButton.setStyle("-fx-background-color: #059669; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 6 12 6 12; -fx-background-radius: 4; -fx-cursor: hand;");
        uploadPdfButton.setOnAction(e -> uploadPdfFile(result));
        buttonRow.getChildren().add(uploadPdfButton);
        
        if (result.getResultFile() != null && !result.getResultFile().isEmpty()) {
            Button viewPdfButton = new Button("📄 Voir PDF");
            viewPdfButton.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 6 12 6 12; -fx-background-radius: 4; -fx-cursor: hand;");
            viewPdfButton.setOnAction(e -> openPdfFile(result.getResultFile()));
            buttonRow.getChildren().add(viewPdfButton);
        }
        
        Button viewDetailsButton = new Button("👁 Voir Détails");
        viewDetailsButton.setStyle("-fx-background-color: #6b7280; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 6 12 6 12; -fx-background-radius: 4; -fx-cursor: hand;");
        viewDetailsButton.setOnAction(e -> showExamDetails(result));
        buttonRow.getChildren().add(viewDetailsButton);
        
        card.getChildren().addAll(header, resultLabel, notesLabel, buttonRow);
        return card;
    }
    
    private void openPdfFile(String filePath) {
        try {
            java.io.File file = new java.io.File(filePath);
            if (file.exists()) {
                java.awt.Desktop.getDesktop().open(file);
            } else {
                showAlert("Fichier non trouvé", "Le fichier PDF n'existe pas: " + filePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le fichier: " + e.getMessage());
        }
    }
    
    private void uploadPdfFile(Examens result) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner un fichier PDF");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("PDF Files", "*.pdf"),
            new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        
        java.io.File selectedFile = fileChooser.showOpenDialog(null);
        
        if (selectedFile != null) {
            try {
                // Copy the file to a storage location
                String uploadDir = "uploads/lab-results/";
                java.io.File dir = new java.io.File(uploadDir);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                
                // Generate unique filename
                String newFileName = result.getId() + "_" + System.currentTimeMillis() + ".pdf";
                java.io.File destFile = new java.io.File(uploadDir + newFileName);
                
                // Copy file
                java.nio.file.Files.copy(selectedFile.toPath(), destFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                
                String filePath = destFile.getAbsolutePath();
                
                // Update the exam with the file path
                result.setResultFile(filePath);
                examensServices.ModifyExamens(result.getId(), result);
                
                showAlert("Succès", "Fichier PDF uploadé avec succès!");
                
                // Refresh the display
                loadLabResults();
                displayLabResults();
                
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Erreur", "Erreur lors de l'upload: " + e.getMessage());
            }
        }
    }
    
    private void showExamDetails(Examens result) {
        String details = "Type: " + (result.getTypeExamen() != null ? result.getTypeExamen() : "N/A") + "\n" +
                       "Nom: " + (result.getNomExamen() != null ? result.getNomExamen() : "N/A") + "\n" +
                       "Date: " + (result.getDateExamen() != null ? result.getDateExamen().toString() : "N/A") + "\n" +
                       "Statut: " + (result.getStatus() != null ? result.getStatus() : "N/A") + "\n\n" +
                       "Résultat: " + (result.getResultat() != null ? result.getResultat() : "N/A") + "\n\n" +
                       "Analyse Médecin: " + (result.getDoctorAnalysis() != null ? result.getDoctorAnalysis() : "N/A") + "\n\n" +
                       "Traitement: " + (result.getDoctorTreatment() != null ? result.getDoctorTreatment() : "N/A") + "\n\n" +
                       "Notes: " + (result.getNotes() != null ? result.getNotes() : "N/A");
        
        showAlert("Détails de l'Examen", details);
    }

    private void loadLabResults() {
        try {
            labResults = examensServices.ShowExamens();
            System.out.println("=== DEBUG: Loaded " + labResults.size() + " lab results from database ===");
            
            // If no results, add sample data for demo purposes
            if (labResults.isEmpty()) {
                System.out.println("=== DEBUG: No data in database, adding sample data ===");
                addSampleData();
                labResults = examensServices.ShowExamens();
            }
            
            // Debug: Print each result
            for (Examens e : labResults) {
                System.out.println("  - " + e.getNomExamen() + " (" + e.getStatus() + ")");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("=== DEBUG: Database error - " + e.getMessage() + " ===");
            showAlert("Erreur de connexion", "Impossible de charger les résultats: " + e.getMessage());
            labResults = new ArrayList<>();
        }
    }
    
    private void addSampleData() {
        try {
            // Add sample lab results for demo
            Examens e1 = new Examens();
            e1.setId(1);
            e1.setTypeExamen("Blood Test");
            e1.setNomExamen("Complete Blood Count (CBC)");
            e1.setDateExamen(java.time.LocalDate.now().minusDays(5));
            e1.setResultat(" hemoglobin: 14.5 g/dL\n WBC: 7,500 cells/μL\n Platelets: 250,000/μL");
            e1.setStatus("COMPLETED");
            e1.setNotes("All values within normal range");
            examensServices.AddExamens(e1);
            
            Examens e2 = new Examens();
            e2.setId(2);
            e2.setTypeExamen("Lipid Panel");
            e2.setNomExamen("Cholesterol Test");
            e2.setDateExamen(java.time.LocalDate.now().minusDays(10));
            e2.setResultat("Total Cholesterol: 195 mg/dL\n LDL: 110 mg/dL\n HDL: 55 mg/dL");
            e2.setStatus("COMPLETED");
            e2.setNotes("Borderline LDL level");
            examensServices.AddExamens(e2);
            
            Examens e3 = new Examens();
            e3.setId(3);
            e3.setTypeExamen("Urinalysis");
            e3.setNomExamen("Urine Test");
            e3.setDateExamen(java.time.LocalDate.now().minusDays(3));
            e3.setResultat("pH: 6.0\n Specific Gravity: 1.020\n No abnormalities");
            e3.setStatus("COMPLETED");
            e3.setNotes("Kidney function normal");
            examensServices.AddExamens(e3);
            
            System.out.println("=== DEBUG: Sample data added successfully ===");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("=== DEBUG: Error adding sample data: " + e.getMessage() + " ===");
        }
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

            switch (buttonText) {
                case "Tableau de bord":
                    loader = new FXMLLoader(getClass().getResource("/fxml/appointments.fxml"));
                    root = loader.load();
                    newScene = new Scene(root, 1200, 800);
                    newScene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
                    stage.setScene(newScene);
                    stage.setTitle("WellCare Connect - Tableau de bord");
                    break;
                    
                case "Mes Rendez-vous":
                    loader = new FXMLLoader(getClass().getResource("/fxml/appointments.fxml"));
                    root = loader.load();
                    newScene = new Scene(root, 1200, 800);
                    newScene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
                    stage.setScene(newScene);
                    stage.setTitle("WellCare Connect - Mes Rendez-vous");
                    break;
                    
                case "Trouver un Médecin":
                    loader = new FXMLLoader(getClass().getResource("/fxml/doctor-search.fxml"));
                    root = loader.load();
                    newScene = new Scene(root, 1200, 800);
                    newScene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
                    stage.setScene(newScene);
                    stage.setTitle("WellCare Connect - Trouver un Médecin");
                    break;
                    
                case "Profil Médecin":
                    loader = new FXMLLoader(getClass().getResource("/fxml/doctor-profile.fxml"));
                    root = loader.load();
                    newScene = new Scene(root, 1200, 800);
                    newScene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
                    stage.setScene(newScene);
                    stage.setTitle("WellCare Connect - Profil Médecin");
                    break;
                    
                case "Résultats de Laboratoire":
                case "Résultats Labo":
                    loader = new FXMLLoader(getClass().getResource("/fxml/lab-results.fxml"));
                    root = loader.load();
                    newScene = new Scene(root, 1200, 800);
                    newScene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
                    stage.setScene(newScene);
                    stage.setTitle("WellCare Connect - Résultats de Laboratoire");
                    break;
                    
                case "Mes Ordonnances":
                    loader = new FXMLLoader(getClass().getResource("/fxml/prescriptions.fxml"));
                    root = loader.load();
                    newScene = new Scene(root, 1200, 800);
                    newScene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
                    stage.setScene(newScene);
                    stage.setTitle("WellCare Connect - Mes Ordonnances");
                    break;
                    
                case "Prescription":
                    loader = new FXMLLoader(getClass().getResource("/fxml/prescriptions.fxml"));
                    root = loader.load();
                    newScene = new Scene(root, 1200, 800);
                    newScene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
                    stage.setScene(newScene);
                    stage.setTitle("WellCare Connect - Prescription");
                    break;
                    
                case "Prendre RDV":
                    loader = new FXMLLoader(getClass().getResource("/fxml/booking.fxml"));
                    root = loader.load();
                    newScene = new Scene(root, 1200, 800);
                    newScene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
                    stage.setScene(newScene);
                    stage.setTitle("WellCare Connect - Prendre RDV");
                    break;
                    
                case "Mon Profil":
                    showAlert("Mon Profil", "Page de profil utilisateur à implémenter");
                    break;
                    
                case "Paramètres":
                    showAlert("Paramètres", "Page de paramètres à implémenter");
                    break;
                    
                case "Déconnexion":
                    showAlert("Déconnexion", "Voulez-vous vraiment vous déconnecter?");
                    break;
            }
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de la navigation: " + e.getMessage());
        }
    }

    @FXML
    private void toggleSubmenu(ActionEvent event) {
        submenuVisible = !submenuVisible;
        if (submenuContainer != null) {
            submenuContainer.setVisible(submenuVisible);
            submenuContainer.setManaged(submenuVisible);
        }
    }
    
    @FXML
    private void toggleLabResultsSubmenu(ActionEvent event) {
        labResultsSubmenuVisible = !labResultsSubmenuVisible;
        if (prescriptionSubmenuContainer != null) {
            prescriptionSubmenuContainer.setVisible(labResultsSubmenuVisible);
            prescriptionSubmenuContainer.setManaged(labResultsSubmenuVisible);
        }
    }
    
    @FXML
    private void toggleSanteSubmenu(ActionEvent event) {
        santeSubmenuVisible = !santeSubmenuVisible;
        if (santeSubmenuContainer != null) {
            santeSubmenuContainer.setVisible(santeSubmenuVisible);
            santeSubmenuContainer.setManaged(santeSubmenuVisible);
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Getters for FXML binding
    public List<Examens> getLabResults() {
        return labResults;
    }
}
