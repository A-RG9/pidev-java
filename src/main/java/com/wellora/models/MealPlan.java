package com.wellora.models;

import java.time.LocalDate;

public class MealPlan {
    private int id;
    private int userId; // Obligatoire selon votre BDD (INT NOT NULL)
    private String userUuid; // La clé étrangère vers votre table users
    private LocalDate date;
    private String dayOfWeek;
    private String mealType;
    private String name;
    private int calories;
    private boolean isCompleted;

    public MealPlan(int id, int userId, String userUuid, LocalDate date, String dayOfWeek,
                    String mealType, String name, int calories, boolean isCompleted) {
        this.id = id;
        this.userId = userId;
        this.userUuid = userUuid;
        this.date = date;
        this.dayOfWeek = dayOfWeek;
        this.mealType = mealType;
        this.name = name;
        this.calories = calories;
        this.isCompleted = isCompleted;
    }

    // --- Getters ---
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getUserUuid() { return userUuid; }
    public LocalDate getDate() { return date; }
    public String getDayOfWeek() { return dayOfWeek; }
    public String getMealType() { return mealType; }
    public String getName() { return name; }
    public int getCalories() { return calories; }
    public boolean isCompleted() { return isCompleted; }

    // --- Setters ---
    public void setCompleted(boolean completed) { isCompleted = completed; }
}