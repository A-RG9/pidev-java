package org.example.models;

import java.sql.Date;
import java.time.LocalDate;

public class DailyPlan {
    private int id;
    private int goalId;
    private String titre;
    private Date date;
    private String notes;
    private int dureeMin;
    private int calories;
    private String status;

    // 1. CONSTRUCTEUR VIDE (Nécessaire pour certaines manipulations)
    public DailyPlan() {}

    // 2. CONSTRUCTEUR COMPLET (Utilisé par le DAO pour charger les données)
    public DailyPlan(int id, String titre, Date date, int dureeMin, int calories) {
        this.id = id;
        this.titre = titre;
        this.date = date;
        this.dureeMin = dureeMin;
        this.calories = calories;
    }

    // 3. CONSTRUCTEUR AVEC LOCALDATE (Optionnel, utile pour le WorkoutPlannerController)
    public DailyPlan(int id, String titre, LocalDate localDate, int dureeMin, int calories) {
        this.id = id;
        this.titre = titre;
        this.date = Date.valueOf(localDate); // Convertit LocalDate en sql.Date
        this.dureeMin = dureeMin;
        this.calories = calories;
    }

    // --- Getters et Setters ---
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