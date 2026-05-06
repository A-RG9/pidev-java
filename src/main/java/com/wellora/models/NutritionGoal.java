package com.wellora.models;

import java.time.LocalDate;

public class NutritionGoal {
    private int id;
    private String userId; // Correspond à user_id dans votre table
    private String name;
    private String goalType;
    private int caloriesTarget;
    private double weightTarget;
    private LocalDate targetDate;

    public NutritionGoal(int id, String userId, String name, String goalType, int caloriesTarget, double weightTarget, LocalDate targetDate) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.goalType = goalType;
        this.caloriesTarget = caloriesTarget;
        this.weightTarget = weightTarget;
        this.targetDate = targetDate;
    }

    public int getId() { return id; }
    public String getUserId() { return userId; }
    public String getName() { return name; }
    public String getGoalType() { return goalType; }
    public int getCaloriesTarget() { return caloriesTarget; }
    public double getWeightTarget() { return weightTarget; }
    public LocalDate getTargetDate() { return targetDate; }
}