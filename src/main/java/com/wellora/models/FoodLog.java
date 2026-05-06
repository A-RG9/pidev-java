package com.wellora.models;

import java.time.LocalDate;

// MODEL LAYER: Represents the data structure
public class FoodLog {
    private int id;
    private int userId;      // integer FK required by food_logs.user_id (NOT NULL)
    private String userUuid;
    private LocalDate date;
    private String mealType;
    private int totalCalories;
    private double totalProtein;
    private double totalCarbs;
    private double totalFats;

    /**
     * Full constructor including userId (required when inserting into DB).
     * userId should be derived from the logged-in user's integer PK.
     */
    public FoodLog(int id, int userId, String userUuid, LocalDate date, String mealType,
                   int totalCalories, double totalProtein, double totalCarbs, double totalFats) {
        this.id = id;
        this.userId = userId;
        this.userUuid = userUuid;
        this.date = date;
        this.mealType = mealType;
        this.totalCalories = totalCalories;
        this.totalProtein = totalProtein;
        this.totalCarbs = totalCarbs;
        this.totalFats = totalFats;
    }

    /**
     * Backwards-compatible constructor without userId.
     * Derives userId from the UUID hashcode (same strategy as PlanificateurController).
     */
    public FoodLog(int id, String userUuid, LocalDate date, String mealType,
                   int totalCalories, double totalProtein, double totalCarbs, double totalFats) {
        this(id,
                (userUuid != null && !userUuid.isEmpty()) ? Math.abs(userUuid.hashCode() % 100000) : 0,
                userUuid, date, mealType, totalCalories, totalProtein, totalCarbs, totalFats);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUserUuid() { return userUuid; }
    public LocalDate getDate() { return date; }
    public String getMealType() { return mealType; }
    public void setMealType(String mealType) { this.mealType = mealType; }

    public int getTotalCalories() { return totalCalories; }
    public void setTotalCalories(int totalCalories) { this.totalCalories = totalCalories; }

    public double getTotalProtein() { return totalProtein; }
    public void setTotalProtein(double totalProtein) { this.totalProtein = totalProtein; }

    public double getTotalCarbs() { return totalCarbs; }
    public void setTotalCarbs(double totalCarbs) { this.totalCarbs = totalCarbs; }

    public double getTotalFats() { return totalFats; }
    public void setTotalFats(double totalFats) { this.totalFats = totalFats; }
}