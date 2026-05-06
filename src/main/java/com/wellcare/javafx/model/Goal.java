package com.wellcare.javafx.model;

import java.time.LocalDate;

public class Goal {
    private int id;
    private String title;
    private String description;
    private String patientId;
    private String coachId;
    private String category;
    private String status;
    private String difficultyLevel;
    private int progress;
    private LocalDate startDate;
    private LocalDate endDate;

    public Goal() {}

    public Goal(int id, String title, String description, String patientId, String coachId,
                String category, String status, String difficultyLevel, int progress,
                LocalDate startDate, LocalDate endDate) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.patientId = patientId;
        this.coachId = coachId;
        this.category = category;
        this.status = status;
        this.difficultyLevel = difficultyLevel;
        this.progress = progress;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // --- GETTERS ET SETTERS ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
    public String getCoachId() { return coachId; }
    public void setCoachId(String coachId) { this.coachId = coachId; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDifficultyLevel() { return difficultyLevel; }
    public void setDifficultyLevel(String difficultyLevel) { this.difficultyLevel = difficultyLevel; }
    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
}