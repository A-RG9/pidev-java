package com.wellora.javafx.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class Consultation {
    private Integer id;
    private String consultationType;
    private String reasonForVisit;
    private String symptomsDescription;
    private LocalDate dateConsultation;
    private LocalTime timeConsultation;
    private Integer duration;
    private String location;
    private Integer fee;
    private String status;
    private String notes;
    private String subjective;
    private String objective;
    private String assessment;
    private String plan;
    private List<String> diagnoses;
    private String appointmentMode;
    private String patientId;          // foreign key to user table (varchar 36)
    private String medecinId;          // foreign key to user table (doctor) (varchar 36)
    private String patientEmail;
    private String patientFirstName;
    private String patientLastName;
    private String patientPhone;
    private String medecinName;      // full name of the doctor

    // Clinical notes fields (optional)
    private String chiefComplaint;
    private Integer bpSystolic;
    private Integer bpDiastolic;
    private Integer pulse;
    private Double temperature;
    private Integer spo2;
    private LocalDate followUpDate;
    private String followUpType;
    private String followUpPriority;

    // Constructors
    public Consultation() {}

    public Consultation(Integer id, String consultationType, String reasonForVisit, String symptomsDescription,
                        LocalDate dateConsultation, LocalTime timeConsultation, Integer duration,
                        String location, Integer fee, String status, String notes, String subjective,
                        String objective, String assessment, String plan, List<String> diagnoses,
                        String patientId) {
        this.id = id;
        this.consultationType = consultationType;
        this.reasonForVisit = reasonForVisit;
        this.symptomsDescription = symptomsDescription;
        this.dateConsultation = dateConsultation;
        this.timeConsultation = timeConsultation;
        this.duration = duration;
        this.location = location;
        this.fee = fee;
        this.status = status;
        this.notes = notes;
        this.subjective = subjective;
        this.objective = objective;
        this.assessment = assessment;
        this.plan = plan;
        this.diagnoses = diagnoses;
        this.patientId = patientId;
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getConsultationType() { return consultationType; }
    public void setConsultationType(String consultationType) { this.consultationType = consultationType; }

    public String getReasonForVisit() { return reasonForVisit; }
    public void setReasonForVisit(String reasonForVisit) { this.reasonForVisit = reasonForVisit; }

    public String getSymptomsDescription() { return symptomsDescription; }
    public void setSymptomsDescription(String symptomsDescription) { this.symptomsDescription = symptomsDescription; }

    public LocalDate getDateConsultation() { return dateConsultation; }
    public void setDateConsultation(LocalDate dateConsultation) { this.dateConsultation = dateConsultation; }

    public LocalTime getTimeConsultation() { return timeConsultation; }
    public void setTimeConsultation(LocalTime timeConsultation) { this.timeConsultation = timeConsultation; }

    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Integer getFee() { return fee; }
    public void setFee(Integer fee) { this.fee = fee; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getSubjective() { return subjective; }
    public void setSubjective(String subjective) { this.subjective = subjective; }

    public String getObjective() { return objective; }
    public void setObjective(String objective) { this.objective = objective; }

    public String getAssessment() { return assessment; }
    public void setAssessment(String assessment) { this.assessment = assessment; }

    public String getPlan() { return plan; }
    public void setPlan(String plan) { this.plan = plan; }

    public List<String> getDiagnoses() { return diagnoses; }
    public void setDiagnoses(List<String> diagnoses) { this.diagnoses = diagnoses; }

    public String getAppointmentMode() { return appointmentMode; }
    public void setAppointmentMode(String appointmentMode) { this.appointmentMode = appointmentMode; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getMedecinId() { return medecinId; }
    public void setMedecinId(String medecinId) { this.medecinId = medecinId; }

    public String getPatientEmail() { return patientEmail; }
    public void setPatientEmail(String patientEmail) { this.patientEmail = patientEmail; }

    public String getPatientFirstName() { return patientFirstName; }
    public void setPatientFirstName(String patientFirstName) { this.patientFirstName = patientFirstName; }

    public String getPatientLastName() { return patientLastName; }
    public void setPatientLastName(String patientLastName) { this.patientLastName = patientLastName; }

    public String getPatientPhone() { return patientPhone; }
    public void setPatientPhone(String patientPhone) { this.patientPhone = patientPhone; }

    public String getMedecinName() { return medecinName; }
    public void setMedecinName(String medecinName) { this.medecinName = medecinName; }

    // Optional clinical fields
    public String getChiefComplaint() { return chiefComplaint; }
    public void setChiefComplaint(String chiefComplaint) { this.chiefComplaint = chiefComplaint; }

    public Integer getBpSystolic() { return bpSystolic; }
    public void setBpSystolic(Integer bpSystolic) { this.bpSystolic = bpSystolic; }

    public Integer getBpDiastolic() { return bpDiastolic; }
    public void setBpDiastolic(Integer bpDiastolic) { this.bpDiastolic = bpDiastolic; }

    public Integer getPulse() { return pulse; }
    public void setPulse(Integer pulse) { this.pulse = pulse; }

    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }

    public Integer getSpo2() { return spo2; }
    public void setSpo2(Integer spo2) { this.spo2 = spo2; }

    public LocalDate getFollowUpDate() { return followUpDate; }
    public void setFollowUpDate(LocalDate followUpDate) { this.followUpDate = followUpDate; }

    public String getFollowUpType() { return followUpType; }
    public void setFollowUpType(String followUpType) { this.followUpType = followUpType; }

    public String getFollowUpPriority() { return followUpPriority; }
    public void setFollowUpPriority(String followUpPriority) { this.followUpPriority = followUpPriority; }

    @Override
    public String toString() {
        return "Consultation{" +
                "id=" + id +
                ", consultationType='" + consultationType + '\'' +
                ", reasonForVisit='" + reasonForVisit + '\'' +
                ", symptomsDescription='" + symptomsDescription + '\'' +
                ", dateConsultation=" + dateConsultation +
                ", timeConsultation=" + timeConsultation +
                ", duration=" + duration +
                ", location='" + location + '\'' +
                ", fee=" + fee +
                ", status='" + status + '\'' +
                ", notes='" + notes + '\'' +
                ", patientId='" + patientId + '\'' +
                '}';
    }
}