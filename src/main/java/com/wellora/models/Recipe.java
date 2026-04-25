package com.wellora.models;

public class Recipe {
    private String title;
    private int calories;
    private String instructions;
    private String imageUrl;

    public Recipe(String title, int calories, String instructions, String imageUrl) {
        this.title = title;
        this.calories = calories;
        this.instructions = instructions;
        this.imageUrl = imageUrl;
    }

    public String getTitle() { return title; }
    public int getCalories() { return calories; }
    public String getInstructions() { return instructions; }
    public String getImageUrl() { return imageUrl; }
}