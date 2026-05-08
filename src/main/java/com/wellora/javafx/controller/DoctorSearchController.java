package com.wellora.javafx.controller;

import com.wellora.javafx.util.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DoctorSearchController {

    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    // Root and sidebar
    @FXML
    private StackPane rootPane;
    @FXML
    private VBox sidebar;
    @FXML
    private VBox submenuContainer;
    @FXML
    private VBox santeSubmenu;
    
    // Search fields
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> locationChoice;
    @FXML
    private ComboBox<String> availabilityChoice;
    
    // Filters
    @FXML
    private VBox specialtyFilters;
    @FXML
    private CheckBox inPersonCheck;
    @FXML
    private CheckBox phoneCheck;
    @FXML
    private Slider priceSlider;
    @FXML
    private Label priceLabel;
    @FXML
    private CheckBox arabicCheck;
    @FXML
    private CheckBox frenchCheck;
    @FXML
    private CheckBox englishCheck;
    @FXML
    private ToggleGroup genderGroup;
    @FXML
    private RadioButton rating4;
    @FXML
    private RadioButton rating3;
    @FXML
    private RadioButton rating2;
    
    // Results
    @FXML
    private Label resultsCount;
    @FXML
    private StackPane loadingPane;
    @FXML
    private VBox noResultsPane;
    @FXML
    private VBox doctorList;
    @FXML
    private HBox paginationPane;
    @FXML
    private Label pageInfo;
    
    // Sort
    @FXML
    private ComboBox<String> sortChoice;

    // Data
    private List<Doctor> allDoctors = new ArrayList<>();
    private List<Doctor> filteredDoctors = new ArrayList<>();
    
    // Pagination
    private int currentPage = 1;
    private int itemsPerPage = 10;

    // Doctor model class
    public static class Doctor {
        public int id;
        public String name;
        public String specialty;
        public String location;
        public int experience;
        public double rating;
        public int reviewCount;
        public int price;
        public boolean isVerified;
        public String nextAvailable;
        public List<String> languages;
        public List<String> hospitals;
        public List<String> availableSlots;
        public String uuid;
        
        public Doctor(int id, String name, String specialty, String location, 
                  int experience, double rating, int reviewCount, int price, String uuid) {
            this.id = id;
            this.name = name;
            this.specialty = specialty;
            this.location = location;
            this.experience = experience;
            this.rating = rating;
            this.reviewCount = reviewCount;
            this.price = price;
            this.uuid = uuid;
            this.isVerified = true;
            this.nextAvailable = "Today";
            this.languages = new ArrayList<>();
            this.hospitals = new ArrayList<>();
            this.availableSlots = new ArrayList<>();
        }
    }

    @FXML
    private void initialize() {
        // Setup ToggleGroup for gender radio buttons
        genderGroup = new ToggleGroup();
        rating4.setToggleGroup(genderGroup);
        rating3.setToggleGroup(genderGroup);
        rating2.setToggleGroup(genderGroup);
        
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
        
        loadSpecialtyFilters();
        loadDoctors();
        displayDoctors();
    }

    private void loadSpecialtyFilters() {
        String[] specialties = {
            "Cardiology", "Dermatology", "Pediatrics", "General Medicine",
            "Orthopedics", "Neurology", "Gynecology", "Psychiatry",
            "Ophthalmology", "ENT", "Urology", "Gastroenterology",
            "Endocrinology", "Oncology", "Radiology", "Pathology"
        };
        
        for (String specialty : specialties) {
            CheckBox checkBox = new CheckBox(specialty);
            checkBox.setOnAction(e -> applyFilters());
            specialtyFilters.getChildren().add(checkBox);
        }
    }

    private void loadDoctors() {
        // Load doctors from database where role = ROLE_MEDECIN
        try {
            java.sql.Connection conn = DatabaseConnection.getInstance().getConnection();
            java.sql.PreparedStatement stmt = conn.prepareStatement(
                "SELECT uuid, first_name, last_name, address, years_of_experience, specialite, consultation_price, rating FROM users WHERE role = ?");
            stmt.setString(1, "ROLE_MEDECIN");
            java.sql.ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String doctorUuid = rs.getString("uuid");
                allDoctors.add(new Doctor(
                    doctorUuid != null ? doctorUuid.hashCode() : 0,
                    rs.getString("first_name") + " " + rs.getString("last_name"),
                    rs.getString("specialite"),
                    rs.getString("address"),
                    rs.getInt("years_of_experience"),
                    rs.getDouble("rating"),
                    0,  // reviewCount - default
                    rs.getInt("consultation_price"),
                    doctorUuid
                ));
            }
            rs.close();
            stmt.close();
        } catch (Exception e) {
            System.out.println("Error loading doctors: " + e.getMessage());
        }
        
        filteredDoctors = new ArrayList<>(allDoctors);
        updateResultsCount();
    }

    private void displayDoctors() {
        doctorList.getChildren().clear();
        
        int start = (currentPage - 1) * itemsPerPage;
        int end = Math.min(start + itemsPerPage, filteredDoctors.size());
        
        if (start >= filteredDoctors.size()) {
            currentPage = 1;
            start = 0;
            end = Math.min(itemsPerPage, filteredDoctors.size());
        }
        
        for (int i = start; i < end; i++) {
            Doctor doctor = filteredDoctors.get(i);
            VBox card = createDoctorCard(doctor);
            doctorList.getChildren().add(card);
        }
        
        updatePagination();
        
        // Show/hide states
        boolean hasResults = !filteredDoctors.isEmpty();
        doctorList.setVisible(hasResults);
        noResultsPane.setVisible(!hasResults);
        paginationPane.setVisible(hasResults && filteredDoctors.size() > itemsPerPage);
    }

    // Helper method to format time with AM/PM
    private String formatTime(String time) {
        try {
            String[] parts = time.split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
            
            String ampm = hour >= 12 ? "PM" : "AM";
            int hour12 = hour % 12;
            if (hour12 == 0) hour12 = 12;
            
            return String.format("%02d:%02d %s", hour12, minute, ampm);
        } catch (Exception e) {
            return time;
        }
    }

    private VBox createDoctorCard(Doctor doctor) {
        VBox card = new VBox();
        card.setPadding(new Insets(24));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16; "
                  + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 4);");
        
        // Main HBox to hold everything
        HBox content = new HBox(24);
        content.setAlignment(javafx.geometry.Pos.TOP_LEFT);
        
        // Left Column: Photo and Rating
        VBox leftCol = new VBox(12);
        leftCol.setAlignment(javafx.geometry.Pos.TOP_CENTER);
        leftCol.setMinWidth(120);
        
        StackPane photoFrame = new StackPane();
        photoFrame.setPrefSize(100, 100);
        photoFrame.setStyle("-fx-background-color: #f3f4f6; -fx-background-radius: 12;");
        Label photoIcon = new Label("👨‍⚕️");
        photoIcon.setStyle("-fx-font-size: 48;");
        photoFrame.getChildren().add(photoIcon);
        
        VBox ratingBox = new VBox(4);
        ratingBox.setAlignment(javafx.geometry.Pos.CENTER);
        HBox stars = new HBox(2);
        stars.setAlignment(javafx.geometry.Pos.CENTER);
        for (int i = 0; i < 5; i++) {
            Label star = new Label(i < Math.floor(doctor.rating) ? "\u2605" : "\u2606");
            star.setStyle("-fx-font-size: 18; -fx-text-fill: #fbbf24;");
            stars.getChildren().add(star);
        }
        Label ratingLabel = new Label("(" + doctor.reviewCount + ")");
        ratingLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #6b7280;");
        ratingBox.getChildren().addAll(stars, ratingLabel);
        
        leftCol.getChildren().addAll(photoFrame, ratingBox);
        
        // Right Column: Information
        VBox rightCol = new VBox(16);
        rightCol.setAlignment(javafx.geometry.Pos.TOP_LEFT);
        HBox.setHgrow(rightCol, Priority.ALWAYS);
        
        // Top Row of Info: Name and Price
        HBox topRow = new HBox(12);
        topRow.setAlignment(javafx.geometry.Pos.TOP_LEFT);
        
        VBox nameSpecialty = new VBox(4);
        HBox nameLine = new HBox(8);
        nameLine.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Text name = new Text("Dr. " + doctor.name);
        name.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-fill: #111827;");
        nameLine.getChildren().add(name);
        if (doctor.isVerified) {
            Label verified = new Label("✓");
            verified.setStyle("-fx-text-fill: #00A790; -fx-font-weight: bold; -fx-font-size: 16;");
            nameLine.getChildren().add(verified);
        }
        
        Text specialty = new Text(doctor.specialty);
        specialty.setStyle("-fx-font-size: 18px; -fx-fill: #4b5563;");
        nameSpecialty.getChildren().addAll(nameLine, specialty);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        VBox priceBox = new VBox(0);
        priceBox.setAlignment(javafx.geometry.Pos.TOP_RIGHT);
        Text price = new Text(doctor.price + " TND");
        price.setStyle("-fx-font-size: 32px; -fx-font-weight: 800; -fx-fill: #111827;");
        Text perConsult = new Text("per consultation");
        perConsult.setStyle("-fx-font-size: 14px; -fx-fill: #6b7280;");
        Text nextAvail = new Text("Next: " + doctor.nextAvailable);
        nextAvail.setStyle("-fx-font-size: 14px; -fx-fill: #374151; -fx-font-weight: 500;");
        priceBox.getChildren().addAll(price, perConsult, nextAvail);
        
        topRow.getChildren().addAll(nameSpecialty, spacer, priceBox);
        
        // Details Row: Exp and Location
        HBox detailsRow = new HBox(20);
        Label expLabel = new Label("💼 " + doctor.experience + " years");
        expLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #374151;");
        Label locLabel = new Label("📍 " + (doctor.location != null ? doctor.location : "null"));
        locLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #374151;");
        detailsRow.getChildren().addAll(expLabel, locLabel);
        
        // Slots Section
        HBox slotsSection = new HBox(12);
        slotsSection.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label slotsTitle = new Label("Available Slots:");
        slotsTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: 600; -fx-text-fill: #111827;");
        
        FlowPane slotsFlow = new FlowPane(12, 12);
        String[] sampleSlots = {"09:00 AM", "10:30 AM", "02:00 PM", "03:30 PM", "04:00 PM"};
        for (String slot : sampleSlots) {
            Button slotBtn = new Button(slot);
            slotBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #00A790; "
                          + "-fx-font-size: 14px; -fx-font-weight: 500; -fx-padding: 0;");
            slotBtn.setCursor(javafx.scene.Cursor.HAND);
            slotBtn.setOnAction(e -> bookSlot(doctor));
            slotsFlow.getChildren().add(slotBtn);
        }
        Button moreLink = new Button("More >");
        moreLink.setStyle("-fx-background-color: transparent; -fx-text-fill: #6b7280; "
                        + "-fx-font-size: 14px; -fx-padding: 0;");
        moreLink.setCursor(javafx.scene.Cursor.HAND);
        moreLink.setOnAction(e -> showMoreSlots(doctor));
        slotsFlow.getChildren().add(moreLink);
        
        slotsSection.getChildren().addAll(slotsTitle, slotsFlow);
        
        // Action Buttons Row
        HBox actionsRow = new HBox(16);
        actionsRow.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        actionsRow.setPadding(new Insets(10, 0, 0, 0));
        
        Button viewProfile = new Button("View Profile");
        viewProfile.setStyle("-fx-background-color: white; -fx-text-fill: #374151; "
                           + "-fx-border-color: #d1d5db; -fx-border-radius: 8; "
                           + "-fx-background-radius: 8; -fx-padding: 10 24; -fx-font-weight: 600;");
        viewProfile.setOnAction(e -> viewDoctorProfile(doctor));
        
        Button bookBtn = new Button("Book Appointment");
        bookBtn.setStyle("-fx-background-color: #00A790; -fx-text-fill: white; "
                        + "-fx-background-radius: 8; -fx-padding: 10 32; -fx-font-weight: 600;");
        bookBtn.setOnAction(e -> bookSlot(doctor));
        
        actionsRow.getChildren().addAll(viewProfile, bookBtn);
        
        rightCol.getChildren().addAll(topRow, detailsRow, slotsSection, actionsRow);
        
        content.getChildren().addAll(leftCol, rightCol);
        card.getChildren().add(content);
        
        return card;
    }

    @FXML
    private void searchDoctors() {
        applyFilters();
    }
    @FXML
    private void filterGynecology() { applyFilters(); }
    @FXML
    private void filterPsychiatry() { applyFilters(); }
    @FXML
    private void startChatbot() { /* TODO: Implement chatbot */ }
    @FXML
    private void filterGenderAny(javafx.event.ActionEvent event) { applyFilters(); }
    @FXML
    private void filterGenderMale(javafx.event.ActionEvent event) { applyFilters(); }
    @FXML
    private void filterGenderFemale(javafx.event.ActionEvent event) { applyFilters(); }
    @FXML
    private void switchToList() { /* TODO: Implement list view */ }
    @FXML
    private void switchToMap() { /* TODO: Implement map view */ }

    @FXML
    private void showRecommended() {
        // Filter to show only recommended doctors (highest rated)
        filteredDoctors = allDoctors.stream()
            .sorted((d1, d2) -> Double.compare(d2.rating, d1.rating))
            .collect(Collectors.toList());
        
        currentPage = 1;
        updateResultsCount();
        displayDoctors();
    }

    @FXML
    private void applyFilters() {
        loadingPane.setVisible(true);
        
        String query = searchField.getText() != null ? searchField.getText().toLowerCase() : "";
        String location = locationChoice.getValue() != null ? locationChoice.getValue().toString() : "";
        
        filteredDoctors = allDoctors.stream()
            .filter(d -> {
                if (!query.isEmpty()) {
                    return d.name.toLowerCase().contains(query) || 
                           d.specialty.toLowerCase().contains(query);
                }
                return true;
            })
            .filter(d -> {
                if (!location.isEmpty() && !location.equals("All Locations")) {
                    return d.location.equalsIgnoreCase(location);
                }
                return true;
            })
            .filter(d -> d.price <= priceSlider.getValue())
            .collect(Collectors.toList());
        
        currentPage = 1;
        updateResultsCount();
        displayDoctors();
        
        loadingPane.setVisible(false);
    }

    @FXML
    private void resetFilters() {
        searchField.setText("");
        locationChoice.setValue("All Locations");
        availabilityChoice.setValue("Any Time");
        
        // Reset specialty filters
        for (javafx.scene.Node node : specialtyFilters.getChildren()) {
            if (node instanceof CheckBox) {
                ((CheckBox) node).setSelected(false);
            }
        }
        
        priceSlider.setValue(500);
        
        filteredDoctors = new ArrayList<>(allDoctors);
        currentPage = 1;
        updateResultsCount();
        displayDoctors();
    }

    // Quick specialty filters
    @FXML
    private void filterCardiology() { searchField.setText("Cardiology"); applyFilters(); }
    @FXML
    private void filterDermatology() { searchField.setText("Dermatology"); applyFilters(); }
    @FXML
    private void filterPediatrics() { searchField.setText("Pediatrics"); applyFilters(); }
    @FXML
    private void filterGeneral() { searchField.setText("General Medicine"); applyFilters(); }
    @FXML
    private void filterOrthopedics() { searchField.setText("Orthopedics"); applyFilters(); }
    @FXML
    private void filterNeurology() { searchField.setText("Neurology"); applyFilters(); }

    // Pagination
    @FXML
    private void previousPage() {
        if (currentPage > 1) {
            currentPage--;
            displayDoctors();
        }
    }

    @FXML
    private void nextPage() {
        int totalPages = (int) Math.ceil((double) filteredDoctors.size() / itemsPerPage);
        if (currentPage < totalPages) {
            currentPage++;
            displayDoctors();
        }
    }

    private void updateResultsCount() {
        resultsCount.setText(filteredDoctors.size() + " Doctors Available");
    }

    private void updatePagination() {
        int totalPages = Math.max(1, (int) Math.ceil((double) filteredDoctors.size() / itemsPerPage));
        pageInfo.setText("Page " + currentPage + " of " + totalPages);
    }

    @FXML
    private void viewDoctorProfile(Doctor doctor) {
        // Store the selected doctor for the profile controller
        DoctorProfileController.selectedDoctor = doctor;
        
        if (mainController != null) {
            mainController.loadView("/fxml/doctor-profile.fxml");
        } else {
            // Fallback if mainController not injected
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/doctor-profile.fxml"));
                Parent profileRoot = loader.load();
                Scene scene = doctorList.getScene();
                Stage stage = (Stage) scene.getWindow();
                Scene newScene = new Scene(profileRoot, 1200, 800);
                newScene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
                stage.setScene(newScene);
                stage.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    @FXML
    private void toggleFavorite(Doctor doctor) {
        // Toggle favorite status - could be implemented to save to database
        System.out.println("Toggle favorite for doctor: " + doctor.name);
    }

    @FXML
    private void bookSlot(Doctor doctor) {
        // Open booking screen with selected slot
        System.out.println("Booking slot for doctor: " + doctor.name);
        
        if (mainController != null) {
            mainController.loadView("/fxml/booking.fxml");
        } else {
            // Fallback
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/booking.fxml"));
                Parent bookingRoot = loader.load();
                Scene scene = doctorList.getScene();
                Stage stage = (Stage) scene.getWindow();
                Scene newScene = new Scene(bookingRoot, 1200, 800);
                newScene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
                stage.setScene(newScene);
                stage.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void showMoreSlots(Doctor doctor) {
        // Show more available slots - could open a dialog
        System.out.println("Show more slots for doctor: " + doctor.name);
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
    public void navigateToDashboard() {
        if (mainController != null) {
            mainController.goBack();
        } else {
            // Fallback
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Dashboard.fxml"));
                Parent root = loader.load();
                Scene scene = getSceneFromNode(searchField);
                if (scene != null) {
                    scene.setRoot(root);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void navigateToDoctorSearch() {
        if (mainController != null) {
            mainController.loadView("/fxml/doctor-search.fxml");
        } else {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/doctor-search.fxml"));
                Parent root = loader.load();
                Scene scene = getSceneFromNode(searchField);
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
                Scene scene = getSceneFromNode(searchField);
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
                Scene scene = getSceneFromNode(searchField);
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
                Scene scene = getSceneFromNode(searchField);
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
            
            Scene scene = getSceneFromNode(searchField);
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
    
    private void navigateToLabResults() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/lab-results.fxml"));
            Parent root = loader.load();
            
            Scene scene = getSceneFromNode(searchField);
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
        Scene scene = node.getScene();
        if (scene == null && rootPane != null) {
            scene = rootPane.getScene();
        }
        return scene;
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.showAndWait();
    }
    
    private boolean santeSubmenuVisible = false;
    private boolean rendezvousSubmenuVisible = false;
    
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
    @FXML
    private void navigateToPrescriptions() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/prescriptions.fxml"));
            Parent root = loader.load();
            
            Scene scene = getSceneFromNode(searchField);
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
} 
