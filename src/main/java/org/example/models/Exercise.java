package org.example.models;

public class Exercise {
    private int id;
    private String name;
    private String description;
    private String category;
    private String difficultyLevel;
    private String videoUrl;
    private int duration;

    // Champs additionnels pour correspondre au constructeur utilisé dans le DAO
    private int calories;
    private int sets;
    private int reps;
    private String defaultUnit;

    // Constructeur complet
    public Exercise(int id, String name, String description, String category, String difficultyLevel,
                    String defaultUnit, String videoUrl, int duration, int calories, int sets, int reps) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.difficultyLevel = difficultyLevel;
        this.defaultUnit = defaultUnit;
        this.videoUrl = videoUrl;
        this.duration = duration;
        this.calories = calories;
        this.sets = sets;
        this.reps = reps;
    }

    // Getters et Setters indispensables
    public int getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDifficultyLevel() { return difficultyLevel; }
    public void setDifficultyLevel(String difficultyLevel) { this.difficultyLevel = difficultyLevel; }
    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }
}