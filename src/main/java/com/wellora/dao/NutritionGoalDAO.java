package com.wellora.dao;

import com.wellora.models.NutritionGoal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NutritionGoalDAO {

    // Assurez-vous que l'URL correspond à votre BDD (mysql ou mariadb)
    private final String URL = "jdbc:mysql://localhost:3306/wellora";
    private final String USER = "root";
    private final String PASS = "";

    public List<NutritionGoal> getGoalsByUser(String userId) {
        List<NutritionGoal> list = new ArrayList<>();
        String query = "SELECT * FROM nutrition_goals WHERE user_id = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                java.sql.Date sqlDate = rs.getDate("target_date");
                java.time.LocalDate targetDate = (sqlDate != null) ? sqlDate.toLocalDate() : null;

                list.add(new NutritionGoal(
                        rs.getInt("id"),
                        rs.getString("user_id"),
                        rs.getString("name"),
                        rs.getString("goal_type"),
                        rs.getInt("calories_target"),
                        rs.getDouble("weight_target"),
                        targetDate
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean addGoal(NutritionGoal goal) {
        String query = "INSERT INTO nutrition_goals (user_id, name, goal_type, calories_target, weight_target, target_date, created_at, status) VALUES (?, ?, ?, ?, ?, ?, NOW(), 'ACTIVE')";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, goal.getUserId());
            ps.setString(2, goal.getName());
            ps.setString(3, goal.getGoalType());
            ps.setInt(4, goal.getCaloriesTarget());
            ps.setDouble(5, goal.getWeightTarget());

            if (goal.getTargetDate() != null) {
                ps.setDate(6, Date.valueOf(goal.getTargetDate()));
            } else {
                ps.setNull(6, Types.DATE);
            }

            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean updateGoal(NutritionGoal goal) {
        String query = "UPDATE nutrition_goals SET name=?, goal_type=?, calories_target=?, weight_target=?, target_date=?, updated_at=NOW() WHERE id=?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, goal.getName());
            ps.setString(2, goal.getGoalType());
            ps.setInt(3, goal.getCaloriesTarget());
            ps.setDouble(4, goal.getWeightTarget());

            if (goal.getTargetDate() != null) {
                ps.setDate(5, Date.valueOf(goal.getTargetDate()));
            } else {
                ps.setNull(5, Types.DATE);
            }

            ps.setInt(6, goal.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean deleteGoal(int id) {
        String query = "DELETE FROM nutrition_goals WHERE id=?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // --- NOUVELLE METHODE (BIEN PLACEE A L'INTERIEUR DE LA CLASSE) ---
    public NutritionGoal getDailyGoalByUser(String userId) {
        String query = "SELECT * FROM nutrition_goals WHERE user_id = ? AND (DATE(target_date) = CURRENT_DATE OR goal_type = 'Daily Challenge') ORDER BY id DESC LIMIT 1";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                java.sql.Date sqlDate = rs.getDate("target_date");
                java.time.LocalDate targetDate = (sqlDate != null) ? sqlDate.toLocalDate() : null;

                String name = rs.getString("name");
                if (name == null || name.trim().isEmpty()) {
                    name = "Objectif sans nom";
                }

                String type = rs.getString("goal_type");
                if (type == null) {
                    type = "Général";
                }

                return new NutritionGoal(
                        rs.getInt("id"),
                        rs.getString("user_id"),
                        name,
                        type,
                        rs.getInt("calories_target"),
                        rs.getDouble("weight_target"),
                        targetDate
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
} // <-- FIN DE LA CLASSE ICI