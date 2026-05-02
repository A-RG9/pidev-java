package com.wellora.models;

public class DailyPlan {
    public String dateString;
    public String breakfast = "-";
    public String lunch = "-";
    public String dinner = "-";
    public String snack = "-";
    public int totalCalories = 0;

    public DailyPlan(String dateString) {
        this.dateString = dateString;
    }
}