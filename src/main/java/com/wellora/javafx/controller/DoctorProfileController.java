package com.wellora.javafx.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import com.wellora.javafx.controller.DoctorSearchController.Doctor;

public class DoctorProfileController {

    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    // Static field to pass doctor data between controllers
    public static DoctorSearchController.Doctor selectedDoctor;

    // Sidebar components
    @FXML
    private VBox sidebar;
    @FXML
    private VBox submenuContainer;
    @FXML
    private VBox santeSubmenu;
    private boolean santeSubmenuVisible = false;
    private boolean rendezvousSubmenuVisible = false;

    // Doctor info
    @FXML
    private Text doctorName;
    @FXML
    private Text doctorSpecialty;
    @FXML
    private Text experienceText;
    @FXML
    private Text locationText;
    @FXML
    private Text priceText;
    @FXML
    private Text aboutText;
    @FXML
    private Text educationText;
    @FXML
    private Text certificationsText;
    @FXML
    private Text hospitalText;
    @FXML
    private Text awardsText;
    @FXML
    private Text addressText;
    @FXML
    private Text phoneText;

    // Buttons
    @FXML
    private Button favoriteBtn;
    @FXML
    private Button inPersonBtn;
    @FXML
    private Button phoneBtn;

    // Price
    @FXML
    private Text consultationFee;
    @FXML
    private Text totalFee;

    // FlowPanes
    @FXML
    private FlowPane specializationsFlow;
    @FXML
    private FlowPane datesFlow;
    @FXML
    private FlowPane timesFlow;

    // State
    private String selectedType = "in-person";
    private String selectedDate = "";
    private String selectedTime = "";
    private boolean isFavorite = false;
    private int basePrice = 120;

    @FXML
    private void initialize() {
        // Initialize submenu visibility - start HIDDEN
        santeSubmenuVisible = false;
        rendezvousSubmenuVisible = false;
        if (santeSubmenu != null) {
            santeSubmenu.setVisible(false);
            santeSubmenu.setManaged(false);
        }
        
        // Also hide rendez-vous submenu initially
        if (submenuContainer != null) {
            submenuContainer.setVisible(false);
            submenuContainer.setManaged(false);
        }
        
        loadDoctorInfo();
        loadSpecializations();
        loadAvailableDates();
        loadTimeSlots();
    }

    private void loadDoctorInfo() {
        // Check if we have a selected doctor from doctor search
        Doctor doctor = DoctorProfileController.selectedDoctor;
        
        if (doctor != null) {
            // Use data from the selected doctor
            doctorName.setText(doctor.name);
            doctorSpecialty.setText(doctor.specialty);
            experienceText.setText(doctor.experience + " years experience");
            locationText.setText(doctor.location);
            priceText.setText(doctor.price + " TND");
            
            aboutText.setText("Dr. " + doctor.name + " is a dedicated " + doctor.specialty + " with " + 
                            doctor.experience + " years of experience in diagnosing and treating diseases. " +
                            "Committed to providing personalized care and utilizing the latest medical advancements.");
            
            educationText.setText("Medical Degree - University of Tunis");
            certificationsText.setText("Board Certified in " + doctor.specialty);
            hospitalText.setText(doctor.location + " Medical Center");
            awardsText.setText("Excellence in Healthcare");
            
            addressText.setText(doctor.location + ", Tunisia");
            phoneText.setText("+216 71 123 456");
        } else {
            // Load default data
            doctorName.setText("Dr. Mohamed Ben Ali");
            doctorSpecialty.setText("Cardiology");
            experienceText.setText("15 years experience");
            locationText.setText("Tunis");
            priceText.setText("120 TND");
            
            aboutText.setText("Dr. Mohamed Ben Ali is a dedicated cardiologist with 15 years of experience in diagnosing and treating cardiovascular diseases. Committed to providing personalized care and utilizing the latest medical advancements to ensure optimal patient outcomes.");
            
            educationText.setText("Medical Degree - University of Tunis");
            certificationsText.setText("Board Certified in Cardiology");
            hospitalText.setText("Local Hospital");
            awardsText.setText("Excellence in Healthcare");
            
            addressText.setText("Tunis, Tunisia");
            phoneText.setText("+216 71 123 456");
        }

        updatePrice();
    }

    private void loadSpecializations() {
        String[] specs = {"Cardiology", "Heart Disease", "Hypertension", "Arrhythmia", "Heart Failure"};
        
        for (String spec : specs) {
            Label specLabel = new Label(spec);
            specLabel.setStyle("-fx-background-color: #eff6ff; -fx-text-fill: #2563eb; "
                           + "-fx-padding: 8 16; -fx-background-radius: 8;");
            specializationsFlow.getChildren().add(specLabel);
        }
    }

    private void loadAvailableDates() {
        String[] days = {"Mon 03", "Tue 04", "Wed 05", "Thu 06"};
        
        for (String day : days) {
            Button dayBtn = new Button(day);
            dayBtn.setStyle("-fx-background-color: white; -fx-border-color: #d1d5db; "
                         + "-fx-background-radius: 8; -fx-padding: 8 12;");
            final String date = day;
            dayBtn.setOnAction(e -> selectDate(date));
            datesFlow.getChildren().add(dayBtn);
        }
        
        // Select first by default
        selectedDate = days[0];
    }

    private void loadTimeSlots() {
        String[] times = {"09:00", "10:00", "11:00", "14:00", "15:00", "16:00"};
        
        for (String time : times) {
            Button timeBtn = new Button(time);
            timeBtn.setStyle("-fx-background-color: white; -fx-border-color: #d1d5db; "
                         + "-fx-background-radius: 8; -fx-padding: 8 12;");
            final String t = time;
            timeBtn.setOnAction(e -> selectTime(t));
            timesFlow.getChildren().add(timeBtn);
        }
        
        // Select first by default
        selectedTime = times[0];
    }

    private void selectDate(String date) {
        selectedDate = date;
        
        // Update button styles
        for (javafx.scene.Node node : datesFlow.getChildren()) {
            if (node instanceof Button) {
                Button btn = (Button) node;
                if (btn.getText().equals(date)) {
                    btn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; "
                               + "-fx-background-radius: 8; -fx-padding: 8 12;");
                } else {
                    btn.setStyle("-fx-background-color: white; -fx-border-color: #d1d5db; "
                             + "-fx-background-radius: 8; -fx-padding: 8 12;");
                }
            }
        }
    }

    private void selectTime(String time) {
        selectedTime = time;
        
        // Update button styles
        for (javafx.scene.Node node : timesFlow.getChildren()) {
            if (node instanceof Button) {
                Button btn = (Button) node;
                if (btn.getText().equals(time)) {
                    btn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; "
                               + "-fx-background-radius: 8; -fx-padding: 8 12;");
                } else {
                    btn.setStyle("-fx-background-color: white; -fx-border-color: #d1d5db; "
                             + "-fx-background-radius: 8; -fx-padding: 8 12;");
                }
            }
        }
    }

    @FXML
    private void selectInPerson() {
        selectedType = "in-person";
        basePrice = 120;
        updateTypeButtons();
        updatePrice();
    }

    @FXML
    private void selectPhone() {
        selectedType = "phone";
        basePrice = 70;
        updateTypeButtons();
        updatePrice();
    }

    private void updateTypeButtons() {
        if ("in-person".equals(selectedType)) {
            inPersonBtn.setStyle("-fx-background-color: #eff6ff; -fx-border-color: #2563eb; "
                            + "-fx-text-fill: #2563eb; -fx-background-radius: 8; -fx-padding: 12 16;");
            phoneBtn.setStyle("-fx-background-color: white; -fx-border-color: #d1d5db; "
                          + "-fx-text-fill: #374151; -fx-background-radius: 8; -fx-padding: 12 16;");
        } else {
            phoneBtn.setStyle("-fx-background-color: #eff6ff; -fx-border-color: #2563eb; "
                          + "-fx-text-fill: #2563eb; -fx-background-radius: 8; -fx-padding: 12 16;");
            inPersonBtn.setStyle("-fx-background-color: white; -fx-border-color: #d1d5db; "
                              + "-fx-text-fill: #374151; -fx-background-radius: 8; -fx-padding: 12 16;");
        }
    }

    private void updatePrice() {
        consultationFee.setText(basePrice + " TND");
        int total = basePrice + 5;
        totalFee.setText(total + " TND");
    }

    @FXML
    private void toggleFavorite() {
        isFavorite = !isFavorite;
        
        if (isFavorite) {
            favoriteBtn.setStyle("-fx-font-family: 'Font Awesome 5 Free'; -fx-font-size: 20; "
                           + "-fx-text-fill: #ef4444; -fx-background-color: transparent;");
        } else {
            favoriteBtn.setStyle("-fx-font-family: 'Font Awesome 5 Free'; -fx-font-size: 20; "
                           + "-fx-text-fill: #374151; -fx-background-color: transparent;");
        }
    }

    @FXML
    private void bookAppointment() {
        if (selectedDate.isEmpty() || selectedTime.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Incomplete Selection");
            alert.setHeaderText("Please select a date and time");
            alert.setContentText("You need to select both a date and time to proceed with booking.");
            alert.showAndWait();
            return;
        }

        // Navigate to booking page
        if (mainController != null) {
            mainController.loadView("/fxml/booking.fxml");
        } else {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/booking.fxml"));
                Parent bookingRoot = loader.load();
                Scene scene = doctorName.getScene();
                Stage stage = (Stage) scene.getWindow();
                Scene newScene = new Scene(bookingRoot, 1200, 800);
                newScene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
                stage.setScene(newScene);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    @FXML
    private void handleMenuClick(javafx.event.ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String buttonText = clickedButton.getText();
        
        if (buttonText.contains("Tableau de bord")) {
            navigateToDoctorSearch();
        } else if (buttonText.contains("Mes Rendez-vous")) {
            navigateToAppointments();
        } else if (buttonText.contains("Trouver un Médecin")) {
            navigateToDoctorSearch();
        } else if (buttonText.contains("Profil Médecin")) {
            navigateToDoctorProfile();
        } else if (buttonText.contains("Prendre RDV")) {
            navigateToBooking();
        } else if (buttonText.contains("Notes Cliniques")) {
            navigateToClinicalNotes();
        } else if (buttonText.contains("Mes Ordonnances")) {
            navigateToPrescriptions();
        } else if (buttonText.contains("Résultats de Laboratoire")) {
            navigateToLabResults();
        } else if (buttonText.contains("Mon Profil")) {
            System.out.println("Navigate to profile");
        } else if (buttonText.contains("Paramètres")) {
            System.out.println("Navigate to settings");
        } else if (buttonText.contains("Déconnexion")) {
            System.out.println("Logout");
        }
    }
    
    @FXML
    public void navigateToDoctorSearch() {
        if (mainController != null) {
            mainController.goBack();
        } else {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/doctor-search.fxml"));
                Parent root = loader.load();
                Scene scene = getSceneFromNode(doctorName);
                if (scene != null) {
                    scene.setRoot(root);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    @FXML
    public void navigateToDoctorProfile() {
        if (mainController != null) {
            mainController.loadView("/fxml/doctor-profile.fxml");
        } else {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/doctor-profile.fxml"));
                Parent root = loader.load();
                Scene scene = getSceneFromNode(doctorName);
                if (scene != null) {
                    scene.setRoot(root);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    @FXML
    public void navigateToBooking() {
        if (mainController != null) {
            mainController.loadView("/fxml/booking.fxml");
        } else {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/booking.fxml"));
                Parent root = loader.load();
                Scene scene = getSceneFromNode(doctorName);
                if (scene != null) {
                    scene.setRoot(root);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    @FXML
    public void navigateToAppointments() {
        if (mainController != null) {
            mainController.loadView("/fxml/appointments.fxml");
        } else {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/appointments.fxml"));
                Parent root = loader.load();
                Scene scene = getSceneFromNode(doctorName);
                if (scene != null) {
                    scene.setRoot(root);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    private void navigateToClinicalNotes() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/clinical-notes.fxml"));
            Parent root = loader.load();
            
            Scene scene = getSceneFromNode(doctorName);
            if (scene != null) {
                scene.setRoot(root);
                System.out.println("Navigated to clinical notes");
            } else {
                showError("Cannot navigate - scene not available");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to navigate: " + e.getMessage());
        }
    }
    
    private void navigateToPrescriptions() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/prescriptions.fxml"));
            Parent root = loader.load();
            
            Scene scene = getSceneFromNode(doctorName);
            if (scene != null) {
                scene.setRoot(root);
                System.out.println("Navigated to prescriptions");
            } else {
                showError("Cannot navigate - scene not available");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to navigate: " + e.getMessage());
        }
    }
    
    private void navigateToLabResults() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/lab-results.fxml"));
            Parent root = loader.load();
            
            Scene scene = getSceneFromNode(doctorName);
            if (scene != null) {
                scene.setRoot(root);
                System.out.println("Navigated to lab results");
            } else {
                showError("Cannot navigate - scene not available");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to navigate: " + e.getMessage());
        }
    }
    
    private Scene getSceneFromNode(javafx.scene.Node node) {
        if (node == null) return null;
        return node.getScene();
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.showAndWait();
    }
    
    @FXML
    private void toggleSubmenu(javafx.event.ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String buttonText = clickedButton.getText();
        
        if (buttonText.contains("Santé")) {
            // Toggle Santé submenu
            santeSubmenuVisible = !santeSubmenuVisible;
            if (santeSubmenu != null) {
                santeSubmenu.setVisible(santeSubmenuVisible);
                santeSubmenu.setManaged(santeSubmenuVisible);
            }
        } else if (buttonText.contains("Rendez-vous")) {
            // Toggle Rendez-vous submenu
            rendezvousSubmenuVisible = !rendezvousSubmenuVisible;
            if (submenuContainer != null) {
                submenuContainer.setVisible(rendezvousSubmenuVisible);
                submenuContainer.setManaged(rendezvousSubmenuVisible);
            }
        }
        // Nutrition and Fitness buttons don't have submenus - do nothing
        System.out.println("Santé submenu: " + santeSubmenuVisible + " | Rendez-vous submenu: " + rendezvousSubmenuVisible);
    }
}