package com.wellora.javafx.model;

import java.time.LocalDate;

public class Examens {

    private Integer id;
    private String typeExamen;
    private LocalDate dateExamen;
    private String resultat;
    private String status;
    private String notes;
    private String nomExamen;
    private LocalDate dateRealisation;
    private String resultFile;
    private String doctorAnalysis;
    private String doctorTreatment;
    private Consultation consultation;
    private int consultationId;

    public Examens() {
    }

    public Examens(Integer id, String typeExamen, LocalDate dateExamen, String resultat, String status, String notes, String nomExamen, LocalDate dateRealisation, String resultFile, String doctorAnalysis, String doctorTreatment, Consultation consultation) {
        this.id = id;
        this.typeExamen = typeExamen;
        this.dateExamen = dateExamen;
        this.resultat = resultat;
        this.status = status;
        this.notes = notes;
        this.nomExamen = nomExamen;
        this.dateRealisation = dateRealisation;
        this.resultFile = resultFile;
        this.doctorAnalysis = doctorAnalysis;
        this.doctorTreatment = doctorTreatment;
        this.consultation = consultation;
    }
    public Consultation getConsultation() {
        return consultation;
    }

    public void setConsultation(Consultation consultation) {
        this.consultation = consultation;
    }

    public int getConsultationId() {
        return consultationId;
    }

    public void setConsultationId(int consultationId) {
        this.consultationId = consultationId;
    }

    public String getDoctorTreatment() {
        return doctorTreatment;
    }

    public void setDoctorTreatment(String doctorTreatment) {
        this.doctorTreatment = doctorTreatment;
    }

    public String getDoctorAnalysis() {
        return doctorAnalysis;
    }

    public void setDoctorAnalysis(String doctorAnalysis) {
        this.doctorAnalysis = doctorAnalysis;
    }

    public String getResultFile() {
        return resultFile;
    }

    public void setResultFile(String resultFile) {
        this.resultFile = resultFile;
    }

    public LocalDate getDateRealisation() {
        return dateRealisation;
    }

    public void setDateRealisation(LocalDate dateRealisation) {
        this.dateRealisation = dateRealisation;
    }

    public String getNomExamen() {
        return nomExamen;
    }

    public void setNomExamen(String nomExamen) {
        this.nomExamen = nomExamen;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getResultat() {
        return resultat;
    }

    public void setResultat(String resultat) {
        this.resultat = resultat;
    }

    public LocalDate getDateExamen() {
        return dateExamen;
    }

    public void setDateExamen(LocalDate dateExamen) {
        this.dateExamen = dateExamen;
    }

    public String getTypeExamen() {
        return typeExamen;
    }

    public void setTypeExamen(String typeExamen) {
        this.typeExamen = typeExamen;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }


    @Override
    public String toString() {
        return "Examens{" +
                "id=" + id +
                ", typeExamen='" + typeExamen + '\'' +
                ", dateExamen=" + dateExamen +
                ", resultat='" + resultat + '\'' +
                ", status='" + status + '\'' +
                ", notes='" + notes + '\'' +
                ", nomExamen='" + nomExamen + '\'' +
                ", dateRealisation=" + dateRealisation +
                ", resultFile='" + resultFile + '\'' +
                ", doctorAnalysis='" + doctorAnalysis + '\'' +
                ", doctorTreatment='" + doctorTreatment + '\'' +
                ", consultation=" + consultation +
                '}';
    }


}
