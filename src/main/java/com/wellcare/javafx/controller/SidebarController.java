package com.wellcare.javafx.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.event.ActionEvent;
import javafx.stage.Stage;

public class SidebarController {

    @FXML
    private VBox submenuContainer;

    private boolean submenuVisible = false;

    @FXML
    public void initialize() {
        if (submenuContainer != null) {
            submenuContainer.setVisible(true);
            submenuContainer.setManaged(true);
        }
    }

    @FXML
    public void handleMenuClick(ActionEvent event) {
        if (event.getSource() instanceof Button) {
            Button clickedButton = (Button) event.getSource();
            String buttonText = clickedButton.getText().trim();

            System.out.println("Menu clicked: " + buttonText);

            switch (buttonText) {
                case "Tableau de bord":
                    navigateTo("appointments.fxml", "WellCare Connect - Tableau de bord");
                    break;
                case "Mes Rendez-vous":
                    navigateTo("appointments.fxml", "WellCare Connect - Mes Rendez-vous");
                    break;
                case "Trouver un MÃ©decin":
                    navigateTo("doctor-search.fxml", "WellCare Connect - Trouver un MÃ©decin");
                    break;
                case "Profil MÃ©decin":
                    navigateTo("doctor-profile.fxml", "WellCare Connect - Profil MÃ©decin");
                    break;
                case "Prendre RDV":
                    navigateTo("booking.fxml", "WellCare Connect - Prendre RDV");
                    break;
                case "Agenda":
                    navigateTo("doctor-schedule-week.fxml", "WellCare Connect - Agenda MÃ©decin");
                    break;
                case "RÃ©sultats de Laboratoire":
                    navigateTo("lab-results.fxml", "WellCare Connect - RÃ©sultats de Laboratoire");
                    break;
                case "Mes Ordonnances":
                    navigateTo("prescriptions.fxml", "WellCare Connect - Mes Ordonnances");
                    break;
                case "Notes Cliniques":
                    navigateTo("clinical-notes.fxml", "WellCare Connect - Notes Cliniques");
                    break;
                case "Assistant IA":
                    navigateTo("chatbot.fxml", "WellCare Connect - Assistant IA");
                    break;
                case "Doctor Dashboard":
                    navigateTo("DoctorDashboard.fxml", "WellCare Connect - Doctor Dashboard");
                    break;
                case "Mon Profil":
                    System.out.println("Navigate to profile");
                    break;
                case "ParamÃ¨tres":
                    System.out.println("Navigate to settings");
                    break;
                case "DÃ©connexion":
                    handleLogout();
                    break;
                default:
                    System.out.println("Unknown menu item: " + buttonText);
                    break;
            }
        }
    }

    @FXML
    public void toggleSubmenu(ActionEvent event) {
        if (submenuContainer != null) {
            submenuVisible = !submenuVisible;
            submenuContainer.setVisible(submenuVisible);
            submenuContainer.setManaged(submenuVisible);
        }
    }

    private void navigateTo(String fxmlFile, String title) {
        try {
            // Load the new FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/" + fxmlFile));
            Parent root = loader.load();

            // Get the stage from the current scene
            Stage stage = null;
            if (submenuContainer != null && submenuContainer.getScene() != null) {
                stage = (Stage) submenuContainer.getScene().getWindow();
            }

            if (stage == null) {
                System.err.println("Cannot get stage for navigation");
                return;
            }

            // Create new scene
            Scene newScene = new Scene(root, 1200, 800);

            // Apply stylesheet
            String css = getClass().getResource("/css/styles.css") != null ? getClass().getResource("/css/styles.css").toExternalForm() : null;
            if (css != null) {
                newScene.getStylesheets().add(css);
            }

            // Set the new scene
            stage.setScene(newScene);
            stage.setTitle(title);
            stage.show();

            System.out.println("Navigated to: " + fxmlFile);
        } catch (Exception e) {
            System.err.println("Error navigating to " + fxmlFile + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleLogout() {
        System.out.println("Logout triggered");
        // Add logout logic here - could close the application or navigate to login
        Stage stage = null;
        if (submenuContainer != null && submenuContainer.getScene() != null) {
            stage = (Stage) submenuContainer.getScene().getWindow();
        }
        if (stage != null) {
            stage.close();
        }
    }
}


