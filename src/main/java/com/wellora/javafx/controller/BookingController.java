package com.wellora.javafx.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.Parent;
import javafx.scene.Scene;
import com.wellora.javafx.model.Consultation;
import com.wellora.javafx.service.ConsulationServices;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class BookingController {

    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    // Step panes
    @FXML private VBox step1Pane, step2Pane, step3Pane, step4Pane;
    @FXML private HBox rootPane;

    // Step circles & labels
    @FXML private StackPane step1Circle, step2Circle, step3Circle, step4Circle;
    @FXML private Label step1Indicator, step2Indicator, step3Indicator, step4Indicator;
    @FXML private Label step1Label, step2Label, step3Label, step4Label;
    @FXML private Region connector1, connector2, connector3;

    // Consultation type & mode
    @FXML private VBox firstVisitBtn, followUpBtn, emergencyBtn;
    @FXML private VBox inPersonBtn, phoneBtn;

    @FXML private TextArea reasonField;
    @FXML private FlowPane symptomsFlow;

    // Duration buttons
    @FXML private Button duration15Btn, duration30Btn, duration45Btn, duration60Btn;

    // Calendar
    @FXML private Label monthYearText;
    @FXML private GridPane calendarGrid;
    @FXML private FlowPane timeSlotsFlow;
    @FXML private Label selectedDateLabel, selectedTimeLabel;

    // Patient info
    @FXML private TextField firstNameField, lastNameField, emailField, phoneField;
    @FXML private DatePicker dobPicker;
    @FXML private ChoiceBox<String> genderChoice;
    @FXML private TextField emergencyNameField, emergencyPhoneField;
    @FXML private ChoiceBox<String> relationChoice;
    @FXML private ChoiceBox<String> insuranceChoice;
    @FXML private TextField policyNumberField;
    @FXML private CheckBox wheelchairCheck, interpreterCheck;

    // Summary
    @FXML private Label summaryDoctor, summarySpecialty, summaryType, summaryMode;
    @FXML private Label summaryDateTime, summaryDuration, summaryPatient;
    @FXML private Label summaryFee, summaryTotal;
    @FXML private Label doctorNameLabel, doctorSpecialtyLabel;
    @FXML private Label inPersonPriceLabel, phonePriceLabel;

    // Payment & terms
    @FXML private ToggleGroup paymentGroup;
    @FXML private RadioButton cardPayment, insurancePayment, cashPayment;
    @FXML private CheckBox termsCheck;

    // Navigation
    @FXML private Button backBtn, nextBtn, submitBtn;
    @FXML private StackPane loadingPane;

    // Sidebar components
    @FXML private VBox sidebar, submenuContainer;
    @FXML private Button menuDashboard, menuRendezVous, menuMesRDV, menuTrouverMedecin, menuProfilMedecin, menuPrendreRDV, menuSettings;

    private boolean submenuVisible = false;

    private int currentStep = 1;
    private boolean[] stepCompleted = new boolean[5]; // indices 1..4

    private String consultationType = "first-visit";
    private String appointmentMode = "in-person";
    private int duration = 30;

    // Reschedule mode
    private boolean isRescheduleMode = false;
    private int rescheduleConsultationId = -1;
    private Consultation rescheduleConsultation;
    private LocalDate selectedDate = null;
    private String selectedTime = null;

    private ConsulationServices consultationService;

    // Calendar state
    private YearMonth currentMonth = YearMonth.now();
    private List<Button> dateButtons = new ArrayList<>();
    private List<Button> timeSlotButtons = new ArrayList<>();
    private Button selectedDateButton = null;

    public BookingController() {
        this.consultationService = new ConsulationServices();
    }

    /**
     * Sets the pre-selected date and time for the booking form.
     * This method is called from the DoctorScheduleController when a user
     * clicks on an appointment slot or day.
     *
     * @param date The LocalDate to pre-select
     * @param time The LocalTime to pre-select
     */
    public void setPreSelectedDateTime(LocalDate date, LocalTime time) {
        if (date == null) return;
        
        // Set the calendar to show the correct month
        currentMonth = YearMonth.from(date);
        generateCalendar();
        
        // Find and select the date button
        for (Button btn : dateButtons) {
            int day = Integer.parseInt(btn.getText());
            LocalDate buttonDate = currentMonth.atDay(day);
            if (buttonDate.equals(date) && !btn.isDisabled()) {
                // Simulate date selection
                selectedDate = date;
                if (selectedDateButton != null) {
                    selectedDateButton.setStyle("-fx-background-color: white; -fx-border-color: #d1d5db; -fx-background-radius: 8; -fx-font-size: 14;");
                }
                btn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-border-radius: 8; -fx-font-size: 14;");
                selectedDateButton = btn;
                selectedDateLabel.setText(date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));
                break;
            }
        }
        
        // Generate time slots for the selected date
        generateTimeSlots();
        
        // Select the time if provided
        if (time != null) {
            String timeStr = time.format(DateTimeFormatter.ofPattern("HH:mm"));
            selectedTime = timeStr;
            selectedTimeLabel.setText(timeStr);
            
            // Find and style the corresponding time button
            for (Button btn : timeSlotButtons) {
                if (btn.getText().equals(timeStr) && !btn.isDisabled()) {
                    for (Button b : timeSlotButtons) {
                        b.setStyle("-fx-background-color: white; -fx-border-color: #d1d5db; -fx-background-radius: 8; -fx-font-size: 14;");
                    }
                    btn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-border-radius: 8; -fx-font-size: 14;");
                    break;
                }
            }
        }
    }

    @FXML
    private void initialize() {
        // Pre-fill patient info as per the photo
        firstNameField.setText("CHAHAD");
        lastNameField.setText("MAALOUL");
        emailField.setText("chahad.maaloul@esprit.tn");
        phoneField.setText("");  // leave empty as in photo
        emergencyNameField.setText("Mariem Fakhfakh");
        emergencyPhoneField.setText("+21622132766");
        policyNumberField.setText("55");

        genderChoice.getItems().addAll("Male", "Female", "Other");
        genderChoice.setValue("Female");
        relationChoice.getItems().addAll("Friend", "Family", "Spouse", "Other");
        relationChoice.setValue("Friend");
        insuranceChoice.getItems().addAll("CNAM", "CNSS", "Private", "None");
        insuranceChoice.setValue("CNAM");

        // Symptom quick-tags
        String[] symptoms = {"Chest Pain", "Shortness of Breath", "Fatigue", "Palpitations",
                "Dizziness", "High BP", "Diabetes", "Headache"};
        for (String sym : symptoms) {
            Button tag = new Button(sym);
            tag.setStyle("-fx-background-color: #f3f4f6; -fx-text-fill: #374151; " +
                    "-fx-font-family: 'Segoe UI'; -fx-font-size: 12px; " +
                    "-fx-background-radius: 16; -fx-padding: 6 12; -fx-cursor: hand;");
            tag.setOnAction(e -> {
                String current = reasonField.getText();
                if (current.isBlank()) reasonField.setText(sym);
                else reasonField.setText(current + ", " + sym);
            });
            symptomsFlow.getChildren().add(tag);
        }

        updateDurationButtons();
        updateStepUI();
        generateCalendar();
        generateTimeSlots();
    }

    private void updateStepUI() {
        // Show/hide panes
        step1Pane.setVisible(currentStep == 1);
        step1Pane.setManaged(currentStep == 1);
        step2Pane.setVisible(currentStep == 2);
        step2Pane.setManaged(currentStep == 2);
        step3Pane.setVisible(currentStep == 3);
        step3Pane.setManaged(currentStep == 3);
        step4Pane.setVisible(currentStep == 4);
        step4Pane.setManaged(currentStep == 4);

        // Update step circles and connectors
        for (int i = 1; i <= 4; i++) {
            StackPane circle = getCircle(i);
            Label indicator = getIndicator(i);
            Label label = getStepLabel(i);
            boolean active = (i == currentStep);
            boolean completed = stepCompleted[i];

            String bgColor, textColor, indicatorText = String.valueOf(i);
            if (completed) {
                bgColor = "#10b981"; // green
                textColor = "white";
                indicatorText = "✓";
            } else if (active) {
                bgColor = "#2563eb"; // blue
                textColor = "white";
            } else {
                bgColor = "#e5e7eb"; // gray
                textColor = "#6b7280";
            }
            circle.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 20;");
            indicator.setText(indicatorText);
            indicator.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + textColor + ";");
            label.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + (active || completed ? "#111827" : "#6b7280") + ";");
        }

        connector1.setStyle("-fx-background-color: " + (stepCompleted[1] ? "#2563eb" : "#e5e7eb") + "; -fx-background-radius: 2;");
        connector2.setStyle("-fx-background-color: " + (stepCompleted[2] ? "#2563eb" : "#e5e7eb") + "; -fx-background-radius: 2;");
        connector3.setStyle("-fx-background-color: " + (stepCompleted[3] ? "#2563eb" : "#e5e7eb") + "; -fx-background-radius: 2;");

        // Navigation buttons
        backBtn.setVisible(currentStep > 1);
        nextBtn.setVisible(currentStep < 4);
        // submitBtn is hidden (we use the button inside step4Pane)
        submitBtn.setVisible(false);

        if (currentStep == 4) updateSummary();
    }

    private StackPane getCircle(int step) {
        switch (step) {
            case 1: return step1Circle;
            case 2: return step2Circle;
            case 3: return step3Circle;
            default: return step4Circle;
        }
    }

    private Label getIndicator(int step) {
        switch (step) {
            case 1: return step1Indicator;
            case 2: return step2Indicator;
            case 3: return step3Indicator;
            default: return step4Indicator;
        }
    }

    private Label getStepLabel(int step) {
        switch (step) {
            case 1: return step1Label;
            case 2: return step2Label;
            case 3: return step3Label;
            default: return step4Label;
        }
    }

    // ==================== Step 1 Actions ====================
    @FXML private void selectFirstVisit() { consultationType = "first-visit"; updateTypeButtons(); }
    @FXML private void selectFollowUp() { consultationType = "follow-up"; updateTypeButtons(); }
    @FXML private void selectEmergency() { consultationType = "emergency"; updateTypeButtons(); }

    private void updateTypeButtons() {
        String base = "-fx-background-color: white; -fx-border-color: #e5e7eb; -fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 16;";
        String selected = "-fx-background-color: #eff6ff; -fx-border-color: #2563eb; -fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 16;";
        String emergencySelected = "-fx-background-color: #fef2f2; -fx-border-color: #dc2626; -fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 16;";
        firstVisitBtn.setStyle(consultationType.equals("first-visit") ? selected : base);
        followUpBtn.setStyle(consultationType.equals("follow-up") ? selected : base);
        emergencyBtn.setStyle(consultationType.equals("emergency") ? emergencySelected : base);
    }

    @FXML private void selectInPerson() { appointmentMode = "in-person"; updateModeButtons(); }
    @FXML private void selectPhone() { appointmentMode = "phone"; updateModeButtons(); }

    private void updateModeButtons() {
        String selected = "-fx-background-color: #eff6ff; -fx-border-color: #2563eb; -fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 16;";
        String base = "-fx-background-color: white; -fx-border-color: #e5e7eb; -fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 16;";
        inPersonBtn.setStyle(appointmentMode.equals("in-person") ? selected : base);
        phoneBtn.setStyle(appointmentMode.equals("phone") ? selected : base);
    }

    @FXML private void selectDuration15() { duration = 15; updateDurationButtons(); }
    @FXML private void selectDuration30() { duration = 30; updateDurationButtons(); }
    @FXML private void selectDuration45() { duration = 45; updateDurationButtons(); }
    @FXML private void selectDuration60() { duration = 60; updateDurationButtons(); }

    private void updateDurationButtons() {
        String selectedStyle = "-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-family: 'Segoe UI'; -fx-font-size: 13px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-border-width: 0; -fx-pref-height: 36; -fx-cursor: hand;";
        String defaultStyle = "-fx-background-color: #f3f4f6; -fx-text-fill: #374151; -fx-font-family: 'Segoe UI'; -fx-font-size: 13px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-border-width: 0; -fx-pref-height: 36; -fx-cursor: hand;";
        duration15Btn.setStyle(duration == 15 ? selectedStyle : defaultStyle);
        duration30Btn.setStyle(duration == 30 ? selectedStyle : defaultStyle);
        duration45Btn.setStyle(duration == 45 ? selectedStyle : defaultStyle);
        duration60Btn.setStyle(duration == 60 ? selectedStyle : defaultStyle);
    }

    // ==================== Step 2: Calendar ====================
    @FXML private void previousMonth() { currentMonth = currentMonth.minusMonths(1); generateCalendar(); }
    @FXML private void nextMonth() { currentMonth = currentMonth.plusMonths(1); generateCalendar(); }

    private void generateCalendar() {
        monthYearText.setText(currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        calendarGrid.getChildren().clear();
        dateButtons.clear();

        String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (int i = 0; i < 7; i++) {
            Label dayLabel = new Label(days[i]);
            dayLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #6b7280;");
            dayLabel.setAlignment(Pos.CENTER);
            calendarGrid.add(dayLabel, i, 0);
        }

        int firstDayOfMonth = currentMonth.atDay(1).getDayOfWeek().getValue() % 7;
        int daysInMonth = currentMonth.lengthOfMonth();
        int row = 1, col = firstDayOfMonth;

        for (int i = 0; i < firstDayOfMonth; i++) {
            Label empty = new Label();
            empty.setMinSize(40, 40);
            calendarGrid.add(empty, i, 1);
        }

        LocalDate today = LocalDate.now();
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = currentMonth.atDay(day);
            boolean isPast = date.isBefore(today);
            boolean isAvailable = !isPast && (date.getDayOfWeek().getValue() <= 5) && (Math.random() > 0.3);

            Button dayBtn = new Button(String.valueOf(day));
            dayBtn.setMinSize(40, 40);
            dayBtn.setMaxSize(40, 40);

            if (isPast) {
                dayBtn.setStyle("-fx-background-color: #f3f4f6; -fx-text-fill: #9ca3af; -fx-background-radius: 8; -fx-font-size: 14;");
                dayBtn.setDisable(true);
            } else if (isAvailable) {
                dayBtn.setStyle("-fx-background-color: white; -fx-border-color: #d1d5db; -fx-border-radius: 8; -fx-font-size: 14;");
                dayBtn.setOnAction(e -> selectDate(date, dayBtn));
            } else {
                dayBtn.setStyle("-fx-background-color: #f3f4f6; -fx-text-fill: #9ca3af; -fx-background-radius: 8; -fx-font-size: 14;");
                dayBtn.setDisable(true);
            }
            dateButtons.add(dayBtn);
            calendarGrid.add(dayBtn, col, row);
            col++;
            if (col > 6) { col = 0; row++; }
        }
    }

    private void selectDate(LocalDate date, Button button) {
        selectedDate = date;
        if (selectedDateButton != null) {
            selectedDateButton.setStyle("-fx-background-color: white; -fx-border-color: #d1d5db; -fx-border-radius: 8; -fx-font-size: 14;");
        }
        button.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-border-radius: 8; -fx-font-size: 14;");
        selectedDateButton = button;
        selectedDateLabel.setText(date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));
        generateTimeSlots();
    }

    private void generateTimeSlots() {
        timeSlotsFlow.getChildren().clear();
        timeSlotButtons.clear();

        String[] times = {"09:00", "09:30", "10:00", "10:30", "11:00", "11:30",
                "14:00", "14:30", "15:00", "15:30", "16:00", "16:30"};
        for (String time : times) {
            boolean available = selectedDate != null && Math.random() > 0.4;
            Button slotBtn = new Button(time);
            slotBtn.setPadding(new Insets(8, 16, 8, 16));
            if (available) {
                slotBtn.setStyle("-fx-background-color: white; -fx-border-color: #d1d5db; -fx-border-radius: 8; -fx-font-size: 14;");
                slotBtn.setOnAction(e -> selectTimeSlot(time, slotBtn));
            } else {
                slotBtn.setStyle("-fx-background-color: #f3f4f6; -fx-text-fill: #9ca3af; -fx-background-radius: 8; -fx-font-size: 14;");
                slotBtn.setDisable(true);
            }
            timeSlotButtons.add(slotBtn);
            timeSlotsFlow.getChildren().add(slotBtn);
        }
    }

    private void selectTimeSlot(String time, Button button) {
        selectedTime = time;
        for (Button btn : timeSlotButtons) {
            btn.setStyle("-fx-background-color: white; -fx-border-color: #d1d5db; -fx-border-radius: 8; -fx-font-size: 14;");
        }
        button.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-border-radius: 8; -fx-font-size: 14;");
        selectedTimeLabel.setText(time);
    }

    // ==================== Navigation ====================
    @FXML
    private void previousStep() {
        if (currentStep > 1) {
            currentStep--;
            updateStepUI();
        }
    }

    @FXML
    private void nextStep() {
        if (validateCurrentStep()) {
            stepCompleted[currentStep] = true;
            currentStep++;
            updateStepUI();
        }
    }

    private boolean validateCurrentStep() {
        switch (currentStep) {
            case 1:
                if (consultationType == null || appointmentMode == null) {
                    showError("Please select consultation type and mode");
                    return false;
                }
                if (reasonField.getText().trim().isEmpty()) {
                    showError("Please provide a reason for visit");
                    return false;
                }
                return true;
            case 2:
                if (selectedDate == null || selectedTime == null) {
                    showError("Please select a date and time");
                    return false;
                }
                return true;
            case 3:
                if (firstNameField.getText().trim().isEmpty() || lastNameField.getText().trim().isEmpty() ||
                        emailField.getText().trim().isEmpty() || phoneField.getText().trim().isEmpty()) {
                    showError("Please fill in all required fields (First Name, Last Name, Email, Phone)");
                    return false;
                }
                return true;
            default:
                return true;
        }
    }

    @FXML
    private void submitBooking() {
        if (!termsCheck.isSelected()) {
            showError("Please agree to the terms and conditions");
            return;
        }
        showLoading(true);
        try {
            Consultation consultation = new Consultation();
            consultation.setConsultationType(consultationType);
            consultation.setReasonForVisit(reasonField.getText());
            consultation.setSymptomsDescription(reasonField.getText());
            consultation.setDateConsultation(selectedDate);
            consultation.setTimeConsultation(selectedTime != null ? LocalTime.parse(selectedTime) : LocalTime.now());
            consultation.setDuration(duration);
            consultation.setLocation("Tunis, Tunisia");
            consultation.setFee(appointmentMode.equals("in-person") ? 120 : 70);
            consultation.setStatus("pending");
            consultation.setAppointmentMode(appointmentMode);
            consultation.setNotes("Mode: " + appointmentMode + ", Patient: " + firstNameField.getText() + " " + lastNameField.getText());
            
            // Set current user as patient
            com.wellcare.javafx.model.User currentUser = com.wellcare.javafx.util.SceneManager.getInstance().getCurrentUser();
            if (currentUser != null) {
                consultation.setPatientId(currentUser.getUuid());
            }
            
            // Set patient contact information
            consultation.setPatientEmail(emailField.getText());
            consultation.setPatientFirstName(firstNameField.getText());
            consultation.setPatientLastName(lastNameField.getText());
            consultation.setPatientPhone(phoneField.getText());
            
            // Assign doctor if available
            if (DoctorProfileController.selectedDoctor != null) {
                consultation.setMedecinId(DoctorProfileController.selectedDoctor.uuid);
                // If you store doctor UUID in selectedDoctor.id (if it was string)
                // Since it's an int hash or something in DoctorSearchController, we just append the name
                consultation.setNotes("Mode: " + appointmentMode + ", Patient: " + firstNameField.getText() + " " + lastNameField.getText() + ", Doctor: " + DoctorProfileController.selectedDoctor.name);
                // Also append the doctor's name to reasonForVisit as a fallback for old matching mechanism
                if (consultation.getReasonForVisit() != null && !consultation.getReasonForVisit().contains(DoctorProfileController.selectedDoctor.name)) {
                    consultation.setReasonForVisit(consultation.getReasonForVisit() + " - " + DoctorProfileController.selectedDoctor.name);
                }
            }
            
            // Check if this is a reschedule or new booking
            if (isRescheduleMode && rescheduleConsultationId > 0) {
                // Update existing consultation
                System.out.println("Updating consultation ID: " + rescheduleConsultationId);
                consultationService.ModifyConsultation(rescheduleConsultationId, consultation);
                showInfo("Appointment rescheduled successfully!");
            } else {
                // Create new consultation
                consultationService.AddConsultation(consultation);
                showInfo("Appointment booked successfully!");
            }
            navigateToAppointments();
            // Optionally close or reset
        } catch (SQLException e) {
            showError("Failed to book appointment: " + e.getMessage());
        } finally {
            showLoading(false);
        }
    }

    private void updateSummary() {
        summaryDoctor.setText("Dr. Mariem Fakhfakh");
        summarySpecialty.setText("General Medicine");
        summaryType.setText(consultationType.equals("first-visit") ? "First Visit" :
                consultationType.equals("follow-up") ? "Follow-up" : "Emergency");
        summaryMode.setText(appointmentMode.equals("in-person") ? "In-Person" : "Phone Call");
        if (selectedDate != null && selectedTime != null) {
            summaryDateTime.setText(selectedDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) + " at " + selectedTime);
        } else {
            summaryDateTime.setText("—");
        }
        summaryDuration.setText(duration + " minutes");
        summaryPatient.setText(firstNameField.getText() + " " + lastNameField.getText());

        int fee = appointmentMode.equals("in-person") ? 120 : 70;
        summaryFee.setText(fee + " TND");
        summaryTotal.setText((fee + 5) + " TND");
    }

    // ==================== Calendar Helpers ====================
    @FXML private void addToGoogleCalendar() { showInfo("Google Calendar integration would open here."); }
    @FXML private void addToAppleCalendar() { showInfo("Apple Calendar (.ics) would be generated."); }
    @FXML private void addToOutlookCalendar() { showInfo("Outlook Calendar link would be generated."); }

    private void showLoading(boolean show) { loadingPane.setVisible(show); }
    private void showError(String msg) { new Alert(Alert.AlertType.ERROR, msg).showAndWait(); }
    private void showInfo(String msg) { new Alert(Alert.AlertType.INFORMATION, msg).showAndWait(); }

    private void navigateToAppointments() {
        if (mainController != null) {
            mainController.loadView("/fxml/appointments.fxml");
        } else {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/appointments.fxml"));
                Parent appointmentsRoot = loader.load();
                Scene scene = rootPane != null ? rootPane.getScene() : null;
                if (scene != null) {
                    scene.setRoot(appointmentsRoot);
                    System.out.println("Navigated to appointments successfully!");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    // Method to set reschedule data - called from AppointmentsController
    public void setRescheduleData(Consultation consultation) {
        this.isRescheduleMode = true;
        this.rescheduleConsultationId = consultation.getId();
        this.rescheduleConsultation = consultation;
        
        System.out.println("Reschedule mode set for consultation ID: " + rescheduleConsultationId);
        
        // Pre-fill form with existing data (but user can change date)
        // Step 1: Consultation type
        if (consultation.getConsultationType() != null) {
            if ("first-visit".equals(consultation.getConsultationType())) {
                selectConsultationType("first");
            } else if ("follow-up".equals(consultation.getConsultationType())) {
                selectConsultationType("follow");
            }
        }
        
        // Step 2: Reason and symptoms
        if (consultation.getReasonForVisit() != null) {
            reasonField.setText(consultation.getReasonForVisit());
        }
        
        // Step 3: Date and time - pre-fill so user can see current appointment
        if (consultation.getDateConsultation() != null) {
            selectedDate = consultation.getDateConsultation();
            System.out.println("Pre-filled date: " + selectedDate);
        }
        if (consultation.getTimeConsultation() != null) {
            selectedTime = consultation.getTimeConsultation().toString();
            System.out.println("Pre-filled time: " + selectedTime);
        }
        if (consultation.getDuration() != null) {
            duration = consultation.getDuration();
            selectDuration(duration);
        }
        
        // Pre-fill patient information from existing consultation
        if (consultation.getPatientFirstName() != null) {
            firstNameField.setText(consultation.getPatientFirstName());
        }
        if (consultation.getPatientLastName() != null) {
            lastNameField.setText(consultation.getPatientLastName());
        }
        if (consultation.getPatientEmail() != null) {
            emailField.setText(consultation.getPatientEmail());
        }
        if (consultation.getPatientPhone() != null) {
            phoneField.setText(consultation.getPatientPhone());
        }
        
        // Start from step 1 to show the entire booking form
        currentStep = 1;
        updateStepUI();
    }
    
    private void selectConsultationType(String type) {
        // Helper to select consultation type buttons
    }
    
    private void selectDuration(int minutes) {
        // Helper to select duration buttons
    }
    
    // ==================== Sidebar Event Handlers ====================
    
    @FXML
    private void handleMenuClick(javafx.event.ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String buttonId = clickedButton.getId();
        
        System.out.println("Menu clicked: " + buttonId);
        
        // Navigate based on which menu item was clicked
        if ("menuDashboard".equals(buttonId)) {
            navigateToDashboard();
        } else if ("menuMesRDV".equals(buttonId)) {
            navigateToAppointments();
        } else if ("menuTrouverMedecin".equals(buttonId)) {
            navigateToDoctorSearch();
        } else if ("menuProfilMedecin".equals(buttonId)) {
            navigateToDoctorProfile();
        } else if ("menuPrendreRDV".equals(buttonId)) {
            // Already on booking page
            showInfo("You are already on the booking page");
        } else if ("menuSettings".equals(buttonId)) {
            showInfo("Settings would open here");
        }
    }
    
    @FXML
    private void toggleSubmenu(javafx.event.ActionEvent event) {
        submenuVisible = !submenuVisible;
        submenuContainer.setVisible(submenuVisible);
        submenuContainer.setManaged(submenuVisible);
        System.out.println("Submenu toggled: " + submenuVisible);
    }
    
    private void navigateToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/doctor-search.fxml"));
            Parent root = loader.load();
            
            Scene scene = rootPane != null ? rootPane.getScene() : null;
            if (scene != null) {
                scene.setRoot(root);
                System.out.println("Navigated to dashboard");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to navigate: " + e.getMessage());
        }
    }
    
    @FXML
    private void navigateToDoctorSearch() {
        if (mainController != null) {
            mainController.goBack();
        } else {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/doctor-search.fxml"));
                Parent root = loader.load();
                Scene scene = rootPane != null ? rootPane.getScene() : null;
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
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/doctor-profile.fxml"));
            Parent root = loader.load();
            
            Scene scene = rootPane != null ? rootPane.getScene() : null;
            if (scene != null) {
                scene.setRoot(root);
                System.out.println("Navigated to doctor profile");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to navigate: " + e.getMessage());
        }
    }
}