package org.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.event.ActionEvent;
import org.example.entities.Consultation;
import org.example.entities.Ordonnance;
import org.example.entities.Examens;
import org.example.service.ConsulationServices;
import org.example.service.OrdonnanceServices;
import org.example.service.ExamensServices;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ClinicalNotesController {

    @FXML
    private Label patientInitials;
    @FXML
    private Label patientName;
    @FXML
    private Label patientId;
    @FXML
    private TextField chiefComplaint;

    @FXML
    private VBox notesHistoryList;

    @FXML
    private Text editorTitle;
    @FXML
    private Label editorMeta;

    @FXML
    private TextArea subjective;
    @FXML
    private TextArea objective;
    @FXML
    private TextArea assessment;
    @FXML
    private TextArea plan;

    @FXML
    private TextField bpSystolic;
    @FXML
    private TextField bpDiastolic;
    @FXML
    private TextField pulse;
    @FXML
    private TextField temperature;
    @FXML
    private TextField spo2;

    @FXML
    private HBox diagnosesTags;
    @FXML
    private TextField newDiagnosis;

    @FXML
    private VBox medicationsContainer;
    @FXML
    private VBox examsContainer;

    @FXML
    private DatePicker followUpDate;
    @FXML
    private ComboBox<String> followUpType;
    @FXML
    private ComboBox<String> followUpPriority;

    @FXML
    private Button saveBtnText;

    // Sidebar elements
    @FXML
    private VBox sidebar;
    @FXML
    private VBox submenuContainer;

    private int medicationCounter = 0;
    private int examCounter = 0;
    private List<String> diagnoses = new ArrayList<>();
    private ConsulationServices consultationService = new ConsulationServices();
    private OrdonnanceServices ordonnanceService = new OrdonnanceServices();
    private ExamensServices examensService = new ExamensServices();
    private int currentConsultationId = -1;

    @FXML
    private void initialize() {
        try {
            // Set default values safely
            if (bpSystolic != null) bpSystolic.setText("120");
            if (bpDiastolic != null) bpDiastolic.setText("80");
            if (pulse != null) pulse.setText("72");
            if (temperature != null) temperature.setText("37");
            if (spo2 != null) spo2.setText("99");

            // Set patient info (default values)
            if (patientInitials != null) patientInitials.setText("JD");
            if (patientName != null) patientName.setText("Patient Name");
            if (patientId != null) patientId.setText("ID: --");

            // Set editor meta
            if (editorMeta != null) editorMeta.setText("Date: " + LocalDate.now() + " • Dr. Name");

            // Set follow-up defaults with null checks
            if (followUpType != null && followUpType.getItems() != null) {
                if (followUpType.getItems().isEmpty()) {
                    followUpType.getItems().addAll("Consultation", "Contrôle");
                    followUpType.setValue("Contrôle");
                }
            }
            if (followUpPriority != null && followUpPriority.getItems() != null) {
                if (followUpPriority.getItems().isEmpty()) {
                    followUpPriority.getItems().addAll("Routine", "Urgent");
                    followUpPriority.setValue("Routine");
                }
            }
        } catch (Exception e) {
            System.out.println("initialize() warning: " + e.getMessage());
        }

        loadNotesHistory();
    }

    private void loadNotesHistory() {
        if (notesHistoryList != null && notesHistoryList.getChildren() != null) {
            notesHistoryList.getChildren().clear();
            
            try {
                // Load consultations from database
                List<Consultation> consultations = consultationService.ShowConsultation();
                
                if (consultations.isEmpty()) {
                    Label emptyLabel = new Label("Aucune note");
                    emptyLabel.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 12px;");
                    notesHistoryList.getChildren().add(emptyLabel);
                } else {
                    // Show last 10 consultations
                    int count = 0;
                    for (Consultation consultation : consultations) {
                        if (count >= 10) break;
                        
                        VBox noteCard = createNoteCard(consultation);
                        if (noteCard != null) {
                            notesHistoryList.getChildren().add(noteCard);
                        }
                        count++;
                    }
                }
            } catch (Exception e) {
                System.out.println("Error loading notes history: " + e.getMessage());
                Label errorLabel = new Label("Erreur de chargement");
                errorLabel.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 12px;");
                notesHistoryList.getChildren().add(errorLabel);
            }
        }
    }

    private VBox createNoteCard(Consultation consultation) {
        VBox card = new VBox(4);
        card.setStyle("-fx-background-color: #f3f4f6; -fx-background-radius: 8; -fx-padding: 12;");
        
        String dateStr = consultation.getDateConsultation() != null ? 
            consultation.getDateConsultation().toString() : "Date unknown";
        String typeStr = consultation.getConsultationType() != null ? 
            consultation.getConsultationType() : "Consultation";
        
        Label dateLabel = new Label(dateStr);
        dateLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #374151;");
        
        Label typeLabel = new Label(typeStr);
        typeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6b7280;");
        
        String reason = consultation.getReasonForVisit();
        if (reason == null || reason.isEmpty()) {
            reason = consultation.getChiefComplaint() != null ? 
                consultation.getChiefComplaint() : "No details";
        }
        Label reasonLabel = new Label(reason);
        reasonLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #9ca3af;");
        
        card.getChildren().addAll(dateLabel, typeLabel, reasonLabel);
        
        // Click to load this consultation
        card.setOnMouseClicked(e -> loadConsultation(consultation));
        card.setStyle(card.getStyle() + "; -fx-cursor: hand;");
        
        return card;
    }

    private void loadConsultation(Consultation consultation) {
        currentConsultationId = consultation.getId();
        
        // Load basic info
        if (consultation.getChiefComplaint() != null) {
            chiefComplaint.setText(consultation.getChiefComplaint());
        }
        
        // Load SOAP notes
        if (consultation.getSubjective() != null) {
            subjective.setText(consultation.getSubjective());
        }
        if (consultation.getObjective() != null) {
            objective.setText(consultation.getObjective());
        }
        if (consultation.getAssessment() != null) {
            assessment.setText(consultation.getAssessment());
        }
        if (consultation.getPlan() != null) {
            plan.setText(consultation.getPlan());
        }
        
        // Load vitals
        if (consultation.getBpSystolic() != null) {
            bpSystolic.setText(consultation.getBpSystolic().toString());
        }
        if (consultation.getBpDiastolic() != null) {
            bpDiastolic.setText(consultation.getBpDiastolic().toString());
        }
        if (consultation.getPulse() != null) {
            pulse.setText(consultation.getPulse().toString());
        }
        if (consultation.getTemperature() != null) {
            temperature.setText(consultation.getTemperature().toString());
        }
        if (consultation.getSpo2() != null) {
            spo2.setText(consultation.getSpo2().toString());
        }
        
        // Load follow-up
        if (consultation.getFollowUpDate() != null) {
            followUpDate.setValue(consultation.getFollowUpDate());
        }
        if (consultation.getFollowUpType() != null) {
            followUpType.setValue(consultation.getFollowUpType());
        }
        if (consultation.getFollowUpPriority() != null) {
            followUpPriority.setValue(consultation.getFollowUpPriority());
        }
        
        // Load diagnoses
        diagnoses.clear();
        if (consultation.getDiagnoses() != null) {
            diagnoses.addAll(consultation.getDiagnoses());
        }
        updateDiagnosesTags();
        
        // Update title
        editorTitle.setText("Modifier Note #" + currentConsultationId);
        saveBtnText.setText("Mettre à jour");
    }

    @FXML
    private void addDiagnosis() {
        String diagnosis = newDiagnosis.getText().trim();
        if (diagnosis.isEmpty()) return;

        diagnoses.add(diagnosis);
        updateDiagnosesTags();
        newDiagnosis.clear();
    }

    private void updateDiagnosesTags() {
        if (diagnosesTags == null || diagnosesTags.getChildren() == null) {
            return;
        }
        
        diagnosesTags.getChildren().clear();
        for (String dx : diagnoses) {
            HBox tag = new HBox();
            tag.setStyle("-fx-background-color: #fffbeb; -fx-border-color: rgba(245, 158, 11, 0.2); -fx-border-radius: 20; -fx-padding: 8 14 8 14; -fx-alignment: CENTER; -fx-spacing: 8;");

            Label label = new Label(dx);
            label.setStyle("-fx-text-fill: #f59e0b; -fx-font-size: 13px; -fx-font-weight: bold;");

            Button removeBtn = new Button("×");
            removeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #dc2626; -fx-font-size: 16px; -fx-padding: 0; -fx-cursor: hand;");
            String diagnosisToRemove = dx;
            removeBtn.setOnAction(e -> {
                diagnoses.remove(diagnosisToRemove);
                updateDiagnosesTags();
            });

            tag.getChildren().addAll(label, removeBtn);
            diagnosesTags.getChildren().add(tag);
        }
    }

    @FXML
    private void addMedicationCard() {
        if (medicationsContainer == null || medicationsContainer.getChildren() == null) {
            return;
        }
        medicationCounter++;
        VBox card = createMedicationCard();
        if (card != null) {
            medicationsContainer.getChildren().add(card);
        }
    }

    private VBox createMedicationCard() {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: #fef2f2; -fx-border-color: rgba(239, 68, 68, 0.2); -fx-border-radius: 16; -fx-padding: 24;");

        HBox header = new HBox();
        header.setStyle("-fx-alignment: CENTER_LEFT; -fx-spacing: 12;");
        Label title = new Label("Médicament");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ef4444;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button deleteBtn = new Button("✕ Supprimer");
        deleteBtn.setStyle("-fx-background-color: #dc2626; -fx-text-fill: white; -fx-padding: 8 16 8 16; -fx-background-radius: 8; -fx-font-size: 12px;");
        deleteBtn.setOnAction(e -> medicationsContainer.getChildren().remove(card));
        header.getChildren().addAll(title, spacer, deleteBtn);

        GridPane form = new GridPane();
        form.setHgap(20);
        form.setVgap(12);
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        form.getColumnConstraints().addAll(col1, col2);

        TextField nameField = new TextField();
            nameField.setId("medName");
        nameField.setPromptText("Nom du médicament");
        nameField.setStyle("-fx-padding: 10 12 10 12; -fx-background-radius: 8; -fx-border-color: #d1d5db;");

        TextField dosageField = new TextField();
        dosageField.setId("medDosage");
        dosageField.setPromptText("Dosage");
        dosageField.setStyle("-fx-padding: 10 12 10 12; -fx-background-radius: 8; -fx-border-color: #d1d5db;");

        TextField frequencyField = new TextField();
        frequencyField.setId("medFrequency");
        frequencyField.setPromptText("Fréquence");
        frequencyField.setStyle("-fx-padding: 10 12 10 12; -fx-background-radius: 8; -fx-border-color: #d1d5db;");

        TextField durationField = new TextField();
        durationField.setId("medDuration");
        durationField.setPromptText("Durée");
        durationField.setStyle("-fx-padding: 10 12 10 12; -fx-background-radius: 8; -fx-border-color: #d1d5db;");

        form.add(new Label("Nom"), 0, 0);
        form.add(nameField, 1, 0);
        form.add(new Label("Dosage"), 0, 1);
        form.add(dosageField, 1, 1);
        form.add(new Label("Fréquence"), 0, 2);
        form.add(frequencyField, 1, 2);
        form.add(new Label("Durée"), 0, 3);
        form.add(durationField, 1, 3);

        card.getChildren().addAll(header, form);
        return card;
    }

    @FXML
    private void addExamCard() {
        if (examsContainer == null || examsContainer.getChildren() == null) {
            return;
        }
        examCounter++;
        VBox card = createExamCard();
        if (card != null) {
            examsContainer.getChildren().add(card);
        }
    }

    private VBox createExamCard() {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: #fef2f2; -fx-border-color: rgba(239, 68, 68, 0.2); -fx-border-radius: 16; -fx-padding: 24;");

        HBox header = new HBox();
        header.setStyle("-fx-alignment: CENTER_LEFT; -fx-spacing: 12;");
        Label title = new Label("Examen");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ef4444;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button deleteBtn = new Button("✕ Supprimer");
        deleteBtn.setStyle("-fx-background-color: #dc2626; -fx-text-fill: white; -fx-padding: 8 16 8 16; -fx-background-radius: 8; -fx-font-size: 12px;");
        deleteBtn.setOnAction(e -> examsContainer.getChildren().remove(card));
        header.getChildren().addAll(title, spacer, deleteBtn);

        GridPane form = new GridPane();
        form.setHgap(20);
        form.setVgap(12);
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        form.getColumnConstraints().addAll(col1, col2);

        ComboBox<String> typeField = new ComboBox<>();
        typeField.setId("examType");
        typeField.getItems().addAll("NFS", "Bilan lipidique", "Bilan hépatique", "Bilan rénal", "IRM", "Scanner", "Radiographie", "ECG");
        typeField.setStyle("-fx-padding: 10 12 10 12; -fx-background-radius: 8; -fx-border-color: #d1d5db;");

        TextField nameField = new TextField();
        nameField.setId("examName");
        nameField.setPromptText("Détails");
        nameField.setStyle("-fx-padding: 10 12 10 12; -fx-background-radius: 8; -fx-border-color: #d1d5db;");

        DatePicker dateField = new DatePicker();
        dateField.setId("examDate");
        dateField.setStyle("-fx-padding: 8 12 8 12; -fx-background-radius: 8; -fx-border-color: #d1d5db;");

        ComboBox<String> statusField = new ComboBox<>();
        statusField.setId("examStatus");
        statusField.getItems().addAll("Prescrit", "Réalisé");
        statusField.setValue("Prescrit");
        statusField.setStyle("-fx-padding: 10 12 10 12; -fx-background-radius: 8; -fx-border-color: #d1d5db;");

        form.add(new Label("Type"), 0, 0);
        form.add(typeField, 1, 0);
        form.add(new Label("Détails"), 0, 1);
        form.add(nameField, 1, 1);
        form.add(new Label("Date"), 0, 2);
        form.add(dateField, 1, 2);
        form.add(new Label("Statut"), 0, 3);
        form.add(statusField, 1, 3);

        card.getChildren().addAll(header, form);
        return card;
    }

    @FXML
    private void scrollToMedications() {
        // Disabled - was causing FX collection error
    }

    @FXML
    private void resetForm() {
        editorTitle.setText("Nouvelle Note SOAP");
        saveBtnText.setText("Enregistrer");
        if (chiefComplaint != null) chiefComplaint.clear();
        if (subjective != null) subjective.clear();
        if (objective != null) objective.clear();
        if (assessment != null) assessment.clear();
        if (plan != null) plan.clear();
        if (bpSystolic != null) bpSystolic.setText("120");
        if (bpDiastolic != null) bpDiastolic.setText("80");
        if (pulse != null) pulse.setText("72");
        if (temperature != null) temperature.setText("37");
        if (spo2 != null) spo2.setText("99");
        if (diagnoses != null) diagnoses.clear();
        if (diagnosesTags != null) diagnosesTags.getChildren().clear();
        if (medicationsContainer != null) medicationsContainer.getChildren().clear();
        if (examsContainer != null) examsContainer.getChildren().clear();
        if (newDiagnosis != null) newDiagnosis.clear();
    }

    @FXML
    private void saveNoteToServer() {
        // Check if FXML fields are initialized
        if (subjective == null || objective == null || assessment == null || plan == null) {
            showError("Erreur", "Les champs du formulaire ne sont pas initialisés. Veuillez redémarrer l'application.");
            return;
        }
        
        try {
            Consultation consultation = new Consultation();
            
            // Set SOAP notes - use empty string if null
            String chiefComplaintText = chiefComplaint != null ? chiefComplaint.getText() : "";
            consultation.setChiefComplaint(chiefComplaintText);
            
            String subjectiveText = subjective != null ? subjective.getText() : "";
            consultation.setSubjective(subjectiveText);
            
            String objectiveText = objective != null ? objective.getText() : "";
            consultation.setObjective(objectiveText);
            
            String assessmentText = assessment != null ? assessment.getText() : "";
            consultation.setAssessment(assessmentText);
            
            String planText = plan != null ? plan.getText() : "";
            consultation.setPlan(planText);
            
            // Set vitals with null checks
            if (bpSystolic != null && bpSystolic.getText() != null && !bpSystolic.getText().isEmpty()) {
                try {
                    consultation.setBpSystolic(Integer.parseInt(bpSystolic.getText()));
                } catch (NumberFormatException e) {
                    showError("Erreur de format", "La pression systolique doit être un nombre");
                    return;
                }
            }
            if (bpDiastolic != null && bpDiastolic.getText() != null && !bpDiastolic.getText().isEmpty()) {
                try {
                    consultation.setBpDiastolic(Integer.parseInt(bpDiastolic.getText()));
                } catch (NumberFormatException e) {
                    showError("Erreur de format", "La pression diastolique doit être un nombre");
                    return;
                }
            }
            if (pulse != null && pulse.getText() != null && !pulse.getText().isEmpty()) {
                try {
                    consultation.setPulse(Integer.parseInt(pulse.getText()));
                } catch (NumberFormatException e) {
                    showError("Erreur de format", "Le pouls doit être un nombre");
                    return;
                }
            }
            if (temperature != null && temperature.getText() != null && !temperature.getText().isEmpty()) {
                try {
                    consultation.setTemperature(Double.parseDouble(temperature.getText()));
                } catch (NumberFormatException e) {
                    showError("Erreur de format", "La température doit être un nombre");
                    return;
                }
            }
            if (spo2 != null && spo2.getText() != null && !spo2.getText().isEmpty()) {
                try {
                    consultation.setSpo2(Integer.parseInt(spo2.getText()));
                } catch (NumberFormatException e) {
                    showError("Erreur de format", "La saturation en oxygène doit être un nombre");
                    return;
                }
            }
            
            // Set follow-up with null checks
            if (followUpDate != null && followUpDate.getValue() != null) {
                consultation.setFollowUpDate(followUpDate.getValue());
            }
            if (followUpType != null && followUpType.getValue() != null) {
                consultation.setFollowUpType(followUpType.getValue());
            }
            if (followUpPriority != null && followUpPriority.getValue() != null) {
                consultation.setFollowUpPriority(followUpPriority.getValue());
            }
            
            // Set diagnoses
            consultation.setDiagnoses(diagnoses);
            
            // Set default values
            consultation.setConsultationType("Clinical Note");
            consultation.setDateConsultation(java.time.LocalDate.now());
            consultation.setStatus("completed");
            
            if (currentConsultationId > 0) {
                // Update existing consultation
                consultation.setId(currentConsultationId);
                consultationService.ModifyConsultation(currentConsultationId, consultation);
                
                // Also save medications as prescriptions
                saveMedicationsAsPrescriptions(currentConsultationId);
                saveExamsAsExamens(currentConsultationId);
                
                showInfo("Succès", "Note mise à jour avec succès!");
            } else {
                // Create new consultation
                consultationService.AddConsultation(consultation);
                System.out.println("DEBUG: Consultation created");
                System.out.println("DEBUG: Consultation ID after AddConsultation = " + consultation.getId());
                
                // Get the new consultation ID and save medications
                int newId = consultation.getId();
                System.out.println("DEBUG: newId = " + newId);
                
                if (newId > 0) {
                    System.out.println("DEBUG: Saving medications and exams with consultationId = " + newId);
                    saveMedicationsAsPrescriptions(newId);
                    saveExamsAsExamens(newId);
                    
                    showInfo("Succès", "Note enregistrée avec succès!");
                } else {
                    System.out.println("DEBUG: ERROR - newId is not greater than 0, newId = " + newId);
                    System.out.println("DEBUG: Trying to save without consultation ID...");
                    
                    // Try to save anyway, will use consultationId = 0
                    saveMedicationsAsPrescriptions(0);
                    saveExamsAsExamens(0);
                    
                    showError("Avertissement", "Note enregistrée mais consultation ID invalid. Vérifiez les médicaments et examens.");
                    return;
                }
                
                // Refresh the notes list
                loadNotesHistory();
                resetForm();
            }
            
        } catch (SQLException e) {
            showError("Erreur de base de données", "Impossible de sauvegarder la note:\n" + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de sauvegarder la note: " + e.getMessage());
        }
    }
    
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // Save medications as prescriptions to the database
    private void saveMedicationsAsPrescriptions(int consultationId) {
        System.out.println("DEBUG: saveMedicationsAsPrescriptions called with consultationId = " + consultationId);
        
        if (medicationsContainer == null) {
            System.out.println("DEBUG: medicationsContainer is null");
            return;
        }
        
        System.out.println("DEBUG: medicationsContainer has " + medicationsContainer.getChildren().size() + " children");
        
        if (medicationsContainer.getChildren().isEmpty()) {
            System.out.println("DEBUG: medicationsContainer is empty");
            return;
        }
        
        int count = 0;
        for (javafx.scene.Node node : medicationsContainer.getChildren()) {
            System.out.println("DEBUG: Processing node type: " + node.getClass().getName());
            
            if (node instanceof VBox) {
                VBox card = (VBox) node;
                String medicament = null;
                String dosage = null;
                String frequency = null;
                String duree = null;
                
                // Search for GridPane inside the card
                for (javafx.scene.Node cardChild : card.getChildren()) {
                    if (cardChild instanceof GridPane) {
                        GridPane gridPane = (GridPane) cardChild;
                        
                        // Get all TextFields from GridPane
                        for (javafx.scene.Node gridChild : gridPane.getChildren()) {
                            if (gridChild instanceof TextField) {
                                TextField tf = (TextField) gridChild;
                                String id = tf.getId();
                                String text = tf.getText();
                                
                                System.out.println("DEBUG: Found TextField with id=" + id + ", value=" + text);
                                
                                if ("medName".equals(id)) medicament = text;
                                else if ("medDosage".equals(id)) dosage = text;
                                else if ("medFrequency".equals(id)) frequency = text;
                                else if ("medDuration".equals(id)) duree = text;
                            }
                        }
                    }
                }
                
                if (medicament != null && !medicament.isEmpty()) {
                    try {
                        Ordonnance ordonnance = new Ordonnance();
                        ordonnance.setDateOrdonnance(java.time.LocalDate.now());
                        ordonnance.setMedicament(medicament);
                        ordonnance.setDosage(dosage != null ? dosage : "");
                        ordonnance.setForme("");
                        ordonnance.setDureeTraitement(duree != null ? duree : "");
                        ordonnance.setInstructions("");
                        ordonnance.setFrequency(frequency != null ? frequency : "");
                        ordonnance.setConsultationId(consultationId);
                        // Don't set status field - it may not exist in the database
                        
                        ordonnanceService.AddOrdonnance(ordonnance);
                        count++;
                        System.out.println("✓ Prescription saved: " + medicament);
                    } catch (Exception e) {
                        System.err.println("Error saving prescription: " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("DEBUG: Skipping card - medicament is empty");
                }
            }
        }
        System.out.println("DEBUG: Total prescriptions saved: " + count);
    }
    
    // Save exams as examens to the database
    private void saveExamsAsExamens(int consultationId) {
        System.out.println("DEBUG: saveExamsAsExamens called with consultationId = " + consultationId);
        
        if (examsContainer == null) {
            System.out.println("DEBUG: examsContainer is null");
            return;
        }
        
        System.out.println("DEBUG: examsContainer has " + examsContainer.getChildren().size() + " children");
        
        if (examsContainer.getChildren().isEmpty()) {
            System.out.println("DEBUG: examsContainer is empty");
            return;
        }
        
        int count = 0;
        for (javafx.scene.Node node : examsContainer.getChildren()) {
            System.out.println("DEBUG: Processing exam node type: " + node.getClass().getName());
            
            if (node instanceof VBox) {
                VBox card = (VBox) node;
                String examName = null;
                String examType = null;
                LocalDate examDate = null;
                
                System.out.println("DEBUG: Exam card has " + card.getChildren().size() + " children");
                
                // Search for GridPane inside the card
                for (javafx.scene.Node cardChild : card.getChildren()) {
                    System.out.println("DEBUG: Exam card child type: " + cardChild.getClass().getName());
                    
                    if (cardChild instanceof GridPane) {
                        GridPane gridPane = (GridPane) cardChild;
                        System.out.println("DEBUG: Found GridPane with " + gridPane.getChildren().size() + " children");
                        
                        // Get all controls from GridPane
                        for (javafx.scene.Node gridChild : gridPane.getChildren()) {
                            if (gridChild instanceof TextField) {
                                TextField tf = (TextField) gridChild;
                                String id = tf.getId();
                                String text = tf.getText();
                                
                                System.out.println("DEBUG: Found TextField with id=" + id + ", value=" + text);
                                
                                if ("examName".equals(id)) examName = text;
                            } else if (gridChild instanceof ComboBox) {
                                ComboBox<?> cb = (ComboBox<?>) gridChild;
                                String id = cb.getId();
                                Object value = cb.getValue();
                                
                                System.out.println("DEBUG: Found ComboBox with id=" + id + ", value=" + value);
                                
                                if ("examType".equals(id) && cb.getValue() != null) {
                                    examType = cb.getValue().toString();
                                }
                            } else if (gridChild instanceof DatePicker) {
                                DatePicker dp = (DatePicker) gridChild;
                                String id = dp.getId();
                                LocalDate value = dp.getValue();
                                
                                System.out.println("DEBUG: Found DatePicker with id=" + id + ", value=" + value);
                                
                                if ("examDate".equals(id)) {
                                    examDate = dp.getValue();
                                }
                            }
                        }
                    }
                }
                
                System.out.println("DEBUG: Exam extraction result - name:" + examName + ", type:" + examType + ", date:" + examDate);
                
                if (examName != null && !examName.isEmpty()) {
                    try {
                        Examens examens = new Examens();
                        examens.setNomExamen(examName);
                        examens.setTypeExamen(examType != null ? examType : "Laboratory");
                        examens.setDateExamen(examDate != null ? examDate : java.time.LocalDate.now());
                        examens.setNotes("");
                        examens.setConsultationId(consultationId);
                        
                        System.out.println("DEBUG: Saving exam - nom:" + examens.getNomExamen() + ", type:" + examens.getTypeExamen() + ", consultationId:" + consultationId);
                        
                        examensService.AddExamens(examens);
                        count++;
                        System.out.println("✓ Exam saved: " + examName);
                    } catch (Exception e) {
                        System.err.println("Error saving exam: " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("DEBUG: Skipping exam - examName is empty or null");
                }
            } else {
                System.out.println("DEBUG: Skipping non-VBox node in examsContainer");
            }
        }
        System.out.println("DEBUG: Total exams saved: " + count);
    }

    // Menu click handler for sidebar navigation
    @FXML
    private void handleMenuClick(ActionEvent event) {
        if (event != null && event.getSource() instanceof Button) {
            Button clickedButton = (Button) event.getSource();
            String buttonText = clickedButton.getText().trim();
            
            System.out.println("Menu clicked: " + buttonText);
            // Navigation is handled by the main app
        }
    }

    // Toggle submenu visibility
    @FXML
    private void toggleSubmenu() {
        if (submenuContainer != null) {
            boolean isVisible = submenuContainer.isVisible();
            submenuContainer.setVisible(!isVisible);
        }
    }
}

