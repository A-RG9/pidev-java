# Wellora Merged — Setup Guide

## Database Setup

This project uses **two databases** (or you can merge them into one):

### Option A — Two separate databases (recommended)
Run these two SQL scripts in MySQL Workbench or phpMyAdmin:

1. **Nutrition module** (`wellora` database):
   - Tables: `food_logs`, `meal_plans`, `nutrition_goals`
   - Create the DB manually: `CREATE DATABASE wellora;`

2. **Health/Journal module** (`wellora_health` database):
   - Run `src/main/resources/schema.sql`
   - This creates: `healthjournal`, `healthentry`, `symptom` tables

### Option B — Single database
1. Run `schema.sql` into your `wellora` database
2. Edit `src/main/resources/db-health.properties`:
   ```
   db.url=jdbc:mysql://localhost:3306/wellora
   ```

### Configuration Files
- `src/main/resources/db.properties` — Nutrition module DB (wellora)
- `src/main/resources/db-health.properties` — Health module DB (wellora_health)

Update **username** and **password** in both files if needed.
Default port is `3306`. Change to `3307` if using XAMPP.

## Running the Project
- Entry point: `com.wellora.Main`
- Maven: `mvn javafx:run`
- Or run `Main.java` directly from IntelliJ

## Navigation
- **Nutrition section**: Dashboard, Journal Alimentaire, Planificateur, Recettes, Objectifs, Analyse
- **Santé section**: Home, Dashboard Santé, Journaux, Entrées, Calendrier, Prédiction IA
