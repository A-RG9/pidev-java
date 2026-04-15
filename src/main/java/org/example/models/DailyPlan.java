package org.example.models;
import java.sql.Date;

public class DailyPlan {
    private int id;
    private int goalId;
    private String titre;
    private Date date;
    private String notes;
    private int dureeMin;
    private int calories; // Champ manquant précédemment
    private String status;

    // Constructeurs, Getters et Setters
    public DailyPlan() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getGoalId() { return goalId; }
    public void setGoalId(int goalId) { this.goalId = goalId; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public int getDureeMin() { return dureeMin; }
    public void setDureeMin(int dureeMin) { this.dureeMin = dureeMin; }
    public int getCalories() { return calories; }
    public void setCalories(int calories) { this.calories = calories; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}