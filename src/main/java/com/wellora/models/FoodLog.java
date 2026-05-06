package com.wellora.models;

import java.time.LocalDate;

// MODEL LAYER: Represents the data structure
public class FoodLog {
    private int id;
    private String userUuid;
    private LocalDate date;
    private String mealType;
    private int totalCalories;
    private double totalProtein;
    private double totalCarbs;
    private double totalFats;

    public FoodLog(int id, String userUuid, LocalDate date, String mealType, int totalCalories, double totalProtein, double totalCarbs, double totalFats) {
        this.id = id;
        this.userUuid = userUuid;
        this.date = date;
        this.mealType = mealType;
        this.totalCalories = totalCalories;
        this.totalProtein = totalProtein;
        this.totalCarbs = totalCarbs;
        this.totalFats = totalFats;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
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