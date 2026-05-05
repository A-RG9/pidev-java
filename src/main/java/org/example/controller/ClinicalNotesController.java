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
import javafx.concurrent.Task;
import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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

    // ======================= INITIALIZATION =======================

    @FXML
    private void initialize() {
        try {
            if (bpSystolic != null) bpSystolic.setText("120");
            if (bpDiastolic != null) bpDiastolic.setText("80");
            if (pulse != null) pulse.setText("72");
            if (temperature != null) temperature.setText("37");
            if (spo2 != null) spo2.setText("99");

            if (patientInitials != null) patientInitials.setText("JD");
            if (patientName != null) patientName.setText("Patient Name");
            if (patientId != null) patientId.setText("ID: --");

            if (editorMeta != null) editorMeta.setText("Date: " + LocalDate.now() + " • Dr. Name");

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
                List<Consultation> consultations = consultationService.ShowConsultation();
                if (consultations.isEmpty()) {
                    Label emptyLabel = new Label("Aucune note");
                    emptyLabel.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 12px;");
                    notesHistoryList.getChildren().add(emptyLabel);
                } else {
                    int count = 0;
                    for (Consultation consultation : consultations) {
                        if (count >= 10) break;
                        VBox noteCard = createNoteCard(consultation);
                        if (noteCard != null) notesHistoryList.getChildren().add(noteCard);
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
        String dateStr = consultation.getDateConsultation() != null ? consultation.getDateConsultation().toString() : "Date unknown";
        String typeStr = consultation.getConsultationType() != null ? consultation.getConsultationType() : "Consultation";
        Label dateLabel = new Label(dateStr);
        dateLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #374151;");
        Label typeLabel = new Label(typeStr);
        typeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6b7280;");
        String reason = consultation.getReasonForVisit();
        if (reason == null || reason.isEmpty()) {
            reason = consultation.getChiefComplaint() != null ? consultation.getChiefComplaint() : "No details";
        }
        Label reasonLabel = new Label(reason);
        reasonLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #9ca3af;");
        card.getChildren().addAll(dateLabel, typeLabel, reasonLabel);
        card.setOnMouseClicked(e -> loadConsultation(consultation));
        card.setStyle(card.getStyle() + "; -fx-cursor: hand;");
        return card;
    }

    private void loadConsultation(Consultation consultation) {
        currentConsultationId = consultation.getId();
        if (consultation.getChiefComplaint() != null) chiefComplaint.setText(consultation.getChiefComplaint());
        if (consultation.getSubjective() != null) subjective.setText(consultation.getSubjective());
        if (consultation.getObjective() != null) objective.setText(consultation.getObjective());
        if (consultation.getAssessment() != null) assessment.setText(consultation.getAssessment());
        if (consultation.getPlan() != null) plan.setText(consultation.getPlan());
        if (consultation.getBpSystolic() != null) bpSystolic.setText(consultation.getBpSystolic().toString());
        if (consultation.getBpDiastolic() != null) bpDiastolic.setText(consultation.getBpDiastolic().toString());
        if (consultation.getPulse() != null) pulse.setText(consultation.getPulse().toString());
        if (consultation.getTemperature() != null) temperature.setText(consultation.getTemperature().toString());
        if (consultation.getSpo2() != null) spo2.setText(consultation.getSpo2().toString());
        if (consultation.getFollowUpDate() != null) followUpDate.setValue(consultation.getFollowUpDate());
        if (consultation.getFollowUpType() != null) followUpType.setValue(consultation.getFollowUpType());
        if (consultation.getFollowUpPriority() != null) followUpPriority.setValue(consultation.getFollowUpPriority());
        diagnoses.clear();
        if (consultation.getDiagnoses() != null) diagnoses.addAll(consultation.getDiagnoses());
        updateDiagnosesTags();
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
        if (diagnosesTags == null || diagnosesTags.getChildren() == null) return;
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
        if (medicationsContainer == null) return;
        medicationCounter++;
        VBox card = createMedicationCard();
        if (card != null) medicationsContainer.getChildren().add(card);
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
        nameField.setPromptText("Nom du médicament");
        nameField.setStyle("-fx-padding: 10 12 10 12; -fx-background-radius: 8; -fx-border-color: #d1d5db;");
        TextField dosageField = new TextField();
        dosageField.setPromptText("Dosage");
        dosageField.setStyle("-fx-padding: 10 12 10 12; -fx-background-radius: 8; -fx-border-color: #d1d5db;");
        TextField frequencyField = new TextField();
        frequencyField.setPromptText("Fréquence");
        frequencyField.setStyle("-fx-padding: 10 12 10 12; -fx-background-radius: 8; -fx-border-color: #d1d5db;");
        TextField durationField = new TextField();
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
        if (examsContainer == null) return;
        examCounter++;
        VBox card = createExamCard();
        if (card != null) examsContainer.getChildren().add(card);
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
        typeField.getItems().addAll("NFS", "Bilan lipidique", "Bilan hépatique", "Bilan rénal", "IRM", "Scanner", "Radiographie", "ECG");
        typeField.setStyle("-fx-padding: 10 12 10 12; -fx-background-radius: 8; -fx-border-color: #d1d5db;");
        TextField nameField = new TextField();
        nameField.setPromptText("Détails");
        nameField.setStyle("-fx-padding: 10 12 10 12; -fx-background-radius: 8; -fx-border-color: #d1d5db;");
        DatePicker dateField = new DatePicker();
        dateField.setStyle("-fx-padding: 8 12 8 12; -fx-background-radius: 8; -fx-border-color: #d1d5db;");
        ComboBox<String> statusField = new ComboBox<>();
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
    private void scrollToMedications() {}

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
        if (subjective == null || objective == null || assessment == null || plan == null) {
            showError("Erreur", "Les champs du formulaire ne sont pas initialisés.");
            return;
        }
        try {
            Consultation consultation = new Consultation();
            consultation.setChiefComplaint(chiefComplaint != null ? chiefComplaint.getText() : "");
            consultation.setSubjective(subjective.getText());
            consultation.setObjective(objective.getText());
            consultation.setAssessment(assessment.getText());
            consultation.setPlan(plan.getText());

            if (bpSystolic != null && !bpSystolic.getText().isEmpty()) consultation.setBpSystolic(Integer.parseInt(bpSystolic.getText()));
            if (bpDiastolic != null && !bpDiastolic.getText().isEmpty()) consultation.setBpDiastolic(Integer.parseInt(bpDiastolic.getText()));
            if (pulse != null && !pulse.getText().isEmpty()) consultation.setPulse(Integer.parseInt(pulse.getText()));
            if (temperature != null && !temperature.getText().isEmpty()) consultation.setTemperature(Double.parseDouble(temperature.getText()));
            if (spo2 != null && !spo2.getText().isEmpty()) consultation.setSpo2(Integer.parseInt(spo2.getText()));

            if (followUpDate != null) consultation.setFollowUpDate(followUpDate.getValue());
            if (followUpType != null) consultation.setFollowUpType(followUpType.getValue());
            if (followUpPriority != null) consultation.setFollowUpPriority(followUpPriority.getValue());

            consultation.setDiagnoses(diagnoses);
            consultation.setConsultationType("Clinical Note");
            consultation.setDateConsultation(LocalDate.now());
            consultation.setStatus("completed");

            if (currentConsultationId > 0) {
                consultation.setId(currentConsultationId);
                consultationService.ModifyConsultation(currentConsultationId, consultation);
                saveMedicationsAsPrescriptions(currentConsultationId);
                showInfo("Succès", "Note mise à jour avec succès!");
            } else {
                consultationService.AddConsultation(consultation);
                int newId = consultation.getId();
                if (newId > 0) {
                    saveMedicationsAsPrescriptions(newId);
                    saveExamsAsExamens(newId);
                }
                showInfo("Succès", "Note enregistrée avec succès!");
                loadNotesHistory();
                resetForm();
            }
        } catch (SQLException e) {
            showError("Erreur de base de données", e.getMessage());
        } catch (NumberFormatException e) {
            showError("Erreur de format", "Vérifiez les valeurs des constantes.");
        } catch (Exception e) {
            showError("Erreur", e.getMessage());
        }
    }

    private void saveMedicationsAsPrescriptions(int consultationId) {
        if (medicationsContainer == null || medicationsContainer.getChildren().isEmpty()) return;
        try {
            for (javafx.scene.Node node : medicationsContainer.getChildren()) {
                if (node instanceof VBox) {
                    VBox card = (VBox) node;
                    for (javafx.scene.Node child : card.getChildren()) {
                        if (child instanceof GridPane) {
                            GridPane grid = (GridPane) child;
                            TextField nameField = null;
                            TextField dosageField = null;
                            TextField durationField = null;
                            for (javafx.scene.Node gridChild : grid.getChildren()) {
                                if (gridChild instanceof TextField) {
                                    Integer col = GridPane.getColumnIndex(gridChild);
                                    Integer row = GridPane.getRowIndex(gridChild);
                                    if (col != null && row != null) {
                                        if (row == 0 && col == 1) nameField = (TextField) gridChild;
                                        else if (row == 1 && col == 1) dosageField = (TextField) gridChild;
                                        else if (row == 3 && col == 1) durationField = (TextField) gridChild;
                                    }
                                }
                            }
                            if (nameField != null && !nameField.getText().isEmpty()) {
                                Ordonnance ordonnance = new Ordonnance();
                                ordonnance.setDateOrdonnance(LocalDate.now());
                                ordonnance.setMedicament(nameField.getText());
                                ordonnance.setDosage(dosageField != null ? dosageField.getText() : "");
                                ordonnance.setDureeTraitement(durationField != null ? durationField.getText() : "");
                                ordonnance.setForme("");
                                ordonnance.setInstructions("");
                                ordonnance.setFrequency("");
                                ordonnance.setStatus("active");
                                ordonnance.setConsultationId(consultationId);
                                ordonnanceService.AddOrdonnance(ordonnance);
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error saving prescriptions: " + e.getMessage());
        }
    }

    private void saveExamsAsExamens(int consultationId) {
        if (examsContainer == null || examsContainer.getChildren().isEmpty()) return;
        try {
            for (javafx.scene.Node node : examsContainer.getChildren()) {
                if (node instanceof VBox) {
                    VBox card = (VBox) node;
                    for (javafx.scene.Node child : card.getChildren()) {
                        if (child instanceof GridPane) {
                            GridPane grid = (GridPane) child;
                            ComboBox<?> typeField = null;
                            TextField nameField = null;
                            for (javafx.scene.Node gridChild : grid.getChildren()) {
                                if (gridChild instanceof ComboBox && GridPane.getColumnIndex(gridChild) == 1 && GridPane.getRowIndex(gridChild) == 0) {
                                    typeField = (ComboBox<?>) gridChild;
                                }
                                if (gridChild instanceof TextField && GridPane.getColumnIndex(gridChild) == 1 && GridPane.getRowIndex(gridChild) == 1) {
                                    nameField = (TextField) gridChild;
                                }
                            }
                            if (nameField != null && !nameField.getText().isEmpty()) {
                                Examens exam = new Examens();
                                exam.setNomExamen(nameField.getText());
                                exam.setTypeExamen(typeField != null && typeField.getValue() != null ? typeField.getValue().toString() : "Laboratory");
                                exam.setDateExamen(LocalDate.now());
                                exam.setStatus("ordered");
                                exam.setNotes("");
                                exam.setConsultationId(consultationId);
                                examensService.AddExamens(exam);
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error saving exams: " + e.getMessage());
        }
    }

    // ======================= AI REPORT GENERATION (NO JACKSON) =======================

    @FXML
    private void generateAIReport() {
        String rawNote = buildClinicalNoteText();
        String prompt = buildAIPrompt(rawNote);

        ProgressIndicator progress = new ProgressIndicator();
        progress.setVisible(true);
        HBox root = (HBox) subjective.getScene().getRoot();
        root.getChildren().add(progress);
        progress.setTranslateX(subjective.getScene().getWidth() - 60);
        progress.setTranslateY(20);

        Task<String> aiTask = new Task<>() {
            @Override
            protected String call() throws Exception {
                return callMistralAPI(prompt);
            }
        };

        aiTask.setOnSucceeded(event -> {
            root.getChildren().remove(progress);
            String report = aiTask.getValue();
            showReportDialog(report);
        });

        aiTask.setOnFailed(event -> {
            root.getChildren().remove(progress);
            showError("Erreur IA", "Échec de la génération : " + aiTask.getException().getMessage());
        });

        new Thread(aiTask).start();
    }

    private String buildClinicalNoteText() {
        StringBuilder sb = new StringBuilder();
        sb.append("SUBJECTIVE:\n").append(subjective != null ? subjective.getText() : "").append("\n\n");
        sb.append("OBJECTIVE:\n");
        sb.append("Vitals: BP ").append(bpSystolic != null ? bpSystolic.getText() : "").append("/")
                .append(bpDiastolic != null ? bpDiastolic.getText() : "").append(" mmHg, Pulse ")
                .append(pulse != null ? pulse.getText() : "").append(" bpm, Temp ")
                .append(temperature != null ? temperature.getText() : "").append(" °C, SpO2 ")
                .append(spo2 != null ? spo2.getText() : "").append("%\n");
        sb.append("Physical exam: ").append(objective != null ? objective.getText() : "").append("\n\n");
        sb.append("ASSESSMENT:\n").append(assessment != null ? assessment.getText() : "").append("\n");
        sb.append("Diagnoses: ").append(String.join(", ", diagnoses)).append("\n\n");
        sb.append("PLAN:\n");
        sb.append("Medications:\n");
        if (medicationsContainer != null) {
            for (javafx.scene.Node card : medicationsContainer.getChildren()) {
                sb.append("  • ").append(extractMedicationText(card)).append("\n");
            }
        }
        sb.append("Exams:\n");
        if (examsContainer != null) {
            for (javafx.scene.Node card : examsContainer.getChildren()) {
                sb.append("  • ").append(extractExamText(card)).append("\n");
            }
        }
        sb.append("General plan: ").append(plan != null ? plan.getText() : "").append("\n");
        if (followUpDate != null && followUpDate.getValue() != null) {
            sb.append("Follow-up: ").append(followUpDate.getValue())
                    .append(" (").append(followUpType != null ? followUpType.getValue() : "")
                    .append(", ").append(followUpPriority != null ? followUpPriority.getValue() : "").append(")");
        }
        return sb.toString();
    }

    private String extractMedicationText(javafx.scene.Node card) {
        if (card instanceof VBox) {
            return findFirstTextField((VBox) card);
        }
        return "Médicament non renseigné";
    }

    private String extractExamText(javafx.scene.Node card) {
        if (card instanceof VBox) {
            VBox vbox = (VBox) card;
            for (javafx.scene.Node child : vbox.getChildren()) {
                if (child instanceof GridPane) {
                    GridPane grid = (GridPane) child;
                    for (javafx.scene.Node node : grid.getChildren()) {
                        if (node instanceof TextField && GridPane.getColumnIndex(node) == 1 && GridPane.getRowIndex(node) == 1) {
                            return ((TextField) node).getText();
                        }
                    }
                }
            }
        }
        return "Examen non renseigné";
    }

    private String findFirstTextField(VBox parent) {
        for (javafx.scene.Node node : parent.getChildren()) {
            if (node instanceof TextField) {
                String text = ((TextField) node).getText();
                return text.isEmpty() ? "Non spécifié" : text;
            } else if (node instanceof VBox) {
                String found = findFirstTextField((VBox) node);
                if (!found.equals("Non spécifié")) return found;
            } else if (node instanceof HBox) {
                HBox hbox = (HBox) node;
                for (javafx.scene.Node inner : hbox.getChildren()) {
                    if (inner instanceof TextField) {
                        String text = ((TextField) inner).getText();
                        if (!text.isEmpty()) return text;
                    } else if (inner instanceof VBox) {
                        String found2 = findFirstTextField((VBox) inner);
                        if (!found2.equals("Non spécifié")) return found2;
                    }
                }
            } else if (node instanceof GridPane) {
                GridPane grid = (GridPane) node;
                for (javafx.scene.Node inner : grid.getChildren()) {
                    if (inner instanceof TextField) {
                        String text = ((TextField) inner).getText();
                        if (!text.isEmpty()) return text;
                    }
                }
            }
        }
        return "Non spécifié";
    }

    private String buildAIPrompt(String rawNote) {
        return """
            # Objective
            You are an expert medical report writer. Transform the following raw clinical SOAP notes into a professional, well‑structured medical report.

            # Instructions
            - Write in past tense, third person.
            - Do not add information that is not present in the notes.
            - Organise the final report under these headings:
                **History**   (from SUBJECTIVE)
                **Physical Examination** (from OBJECTIVE, include vitals)
                **Assessment** (from ASSESSMENT and diagnoses)
                **Plan** (from PLAN, including medications, exams, follow‑up)
            - Use clear medical language. Omit any extra comments.

            # Raw Notes
            %s
            """.formatted(rawNote);
    }

    private String callMistralAPI(String prompt) throws Exception {
        String apiKey = "h4S2z91IjWqucgaphMxdMeFXdEXBYpgb";

        // Manual JSON building – no external libraries
        String escapedPrompt = escapeJson(prompt);
        String json = String.format("""
            {
                "model": "mistral-small-latest",
                "messages": [
                    {"role": "system", "content": "You are a professional medical report writer."},
                    {"role": "user", "content": "%s"}
                ],
                "temperature": 0.3,
                "max_tokens": 1500
            }
            """, escapedPrompt);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.mistral.ai/v1/chat/completions"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            // Manual JSON parsing – extract content after "content":"
            String body = response.body();
            return extractContentFromJson(body);
        } else {
            throw new RuntimeException("Mistral API error " + response.statusCode() + ": " + response.body());
        }
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (c == '"') sb.append("\\\"");
            else if (c == '\\') sb.append("\\\\");
            else if (c == '\n') sb.append("\\n");
            else if (c == '\r') sb.append("\\r");
            else if (c == '\t') sb.append("\\t");
            else sb.append(c);
        }
        return sb.toString();
    }

    private String extractContentFromJson(String json) {
        String target = "\"content\":\"";
        int start = json.indexOf(target);
        if (start == -1) return null;
        start += target.length();
        int end = start;
        while (end < json.length()) {
            char c = json.charAt(end);
            if (c == '"' && (end == 0 || json.charAt(end - 1) != '\\')) break;
            end++;
        }
        if (end > start) {
            String content = json.substring(start, end);
            return content.replace("\\\"", "\"").replace("\\\\", "\\").replace("\\n", "\n").replace("\\r", "\r").replace("\\t", "\t");
        }
        return null;
    }

    private void showReportDialog(String reportText) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Rapport Médical Généré par IA");
        dialog.setHeaderText("Rapport professionnel (vous pouvez le modifier)");

        TextArea textArea = new TextArea(reportText);
        textArea.setEditable(true);
        textArea.setWrapText(true);
        textArea.setPrefHeight(500);
        textArea.setPrefWidth(600);

        ButtonType saveButton = new ButtonType("Enregistrer PDF", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);
        dialog.getDialogPane().setContent(textArea);

        dialog.setResultConverter(button -> {
            if (button == saveButton) {
                saveReportAsPDF(textArea.getText());
            }
            return null;
        });
        dialog.showAndWait();
    }

    private void saveReportAsPDF(String reportText) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le rapport");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        fileChooser.setInitialFileName("rapport_clinique_" + LocalDate.now() + ".pdf");
        java.io.File file = fileChooser.showSaveDialog(subjective.getScene().getWindow());
        if (file != null) {
            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(file)) {
                com.lowagie.text.Document document = new com.lowagie.text.Document();
                com.lowagie.text.pdf.PdfWriter.getInstance(document, fos);
                document.open();
                document.add(new com.lowagie.text.Paragraph(reportText));
                document.close();
                showInfo("Succès", "Rapport PDF enregistré !");
            } catch (Exception ex) {
                showError("Erreur", "Impossible d'enregistrer le PDF : " + ex.getMessage());
            }
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

    // ======================= SIDEBAR NAVIGATION =======================

    @FXML
    private void handleMenuClick(ActionEvent event) {
        if (event != null && event.getSource() instanceof Button) {
            Button clickedButton = (Button) event.getSource();
            System.out.println("Menu clicked: " + clickedButton.getText());
        }
    }

    @FXML
    private void toggleSubmenu() {
        if (submenuContainer != null) {
            submenuContainer.setVisible(!submenuContainer.isVisible());
        }
    }
}