package com.wellcare.javafx.model;

import java.time.LocalDateTime;

public class Exercise {

    private int id;
    private String name;
    private String description;
    private String category;
    private String difficultyLevel;
    private String defaultUnit;
    private String videoUrl;
    private int duration;
    private int sets;
    private int reps;
    private int restTime;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String userId;

    // Constructeur par défaut (OBLIGATOIRE)
    public Exercise() {
    }

    // Constructeur avec paramètres (sans ID - pour création)
    public Exercise(String name, String description, String category,
                    String difficultyLevel, String defaultUnit, String videoUrl,
                    int duration, int sets, int reps, int restTime) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.difficultyLevel = difficultyLevel;
        this.defaultUnit = defaultUnit;
        this.videoUrl = videoUrl;
        this.duration = duration;
        this.sets = sets;
        this.reps = reps;
        this.restTime = restTime;
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
    }

    // Constructeur complet avec ID (pour modification)
    public Exercise(int id, String name, String description, String category,
                    String difficultyLevel, String defaultUnit, String videoUrl,
                    int duration, int sets, int reps, int restTime) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.difficultyLevel = difficultyLevel;
        this.defaultUnit = defaultUnit;
        this.videoUrl = videoUrl;
        this.duration = duration;
        this.sets = sets;
        this.reps = reps;
        this.restTime = restTime;
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDifficultyLevel() { return difficultyLevel; }
    public void setDifficultyLevel(String difficultyLevel) { this.difficultyLevel = difficultyLevel; }
    public String getDefaultUnit() { return defaultUnit; }
    public void setDefaultUnit(String defaultUnit) { this.defaultUnit = defaultUnit; }
    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }
    public int getSets() { return sets; }
    public void setSets(int sets) { this.sets = sets; }
    public int getReps() { return reps; }
    public void setReps(int reps) { this.reps = reps; }
    public int getRestTime() { return restTime; }
    public void setRestTime(int restTime) { this.restTime = restTime; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}