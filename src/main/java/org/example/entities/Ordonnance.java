package org.example.entities;
import java.time.LocalDate;
public class Ordonnance {
    private Integer id;
    private LocalDate dateOrdonnance;
    private String medicament;
    private String dosage;
    private String forme;
    private String dureeTraitement;
    private String instructions;
    private String frequency;
    private String diagnosisCode;
    private String status;
    private Consultation consultation;
    private int consultationId;

    public Ordonnance() {
    }

    public Ordonnance(Integer id, LocalDate dateOrdonnance, String medicament, String dosage, String forme, String dureeTraitement, String instructions, String frequency, String diagnosisCode, Consultation consultation) {
        this.id = id;
        this.dateOrdonnance = dateOrdonnance;
        this.medicament = medicament;
        this.dosage = dosage;
        this.forme = forme;
        this.dureeTraitement = dureeTraitement;
        this.instructions = instructions;
        this.frequency = frequency;
        this.diagnosisCode = diagnosisCode;
        this.consultation = consultation;
    }
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDate getDateOrdonnance() {
        return dateOrdonnance;
    }

    public void setDateOrdonnance(LocalDate dateOrdonnance) {
        this.dateOrdonnance = dateOrdonnance;
    }

    public String getMedicament() {
        return medicament;
    }

    public void setMedicament(String medicament) {
        this.medicament = medicament;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public String getForme() {
        return forme;
    }

    public void setForme(String forme) {
        this.forme = forme;
    }

    public String getDureeTraitement() {
        return dureeTraitement;
    }

    public void setDureeTraitement(String dureeTraitement) {
        this.dureeTraitement = dureeTraitement;
    }

    public int getConsultationId() {
        return consultationId;
    }

    public void setConsultationId(int consultationId) {
        this.consultationId = consultationId;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getDiagnosisCode() {
        return diagnosisCode;
    }

    public void setDiagnosisCode(String diagnosisCode) {
        this.diagnosisCode = diagnosisCode;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }

    public Consultation getConsultation() {
        return consultation;
    }

    public void setConsultation(Consultation consultation) {
        this.consultation = consultation;
    }
    @Override
    public String toString() {
        return "Ordonnance{" +
                "id=" + id +
                ", dateOrdonnance=" + dateOrdonnance +
                ", medicament='" + medicament + '\'' +
                ", dosage='" + dosage + '\'' +
                ", forme='" + forme + '\'' +
                ", dureeTraitement='" + dureeTraitement + '\'' +
                ", instructions='" + instructions + '\'' +
                ", frequency='" + frequency + '\'' +
                ", diagnosisCode='" + diagnosisCode + '\'' +
                ", consultation=" + consultation +
                '}';
    }


}
