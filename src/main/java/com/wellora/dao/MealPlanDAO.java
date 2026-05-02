package com.wellora.dao;

import com.wellora.models.MealPlan;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MealPlanDAO {
    private final String URL = "jdbc:mysql://localhost:3306/wellora";
    private final String USER = "root";
    private final String PASS = "";

    public List<MealPlan> getPlansByDate(String userUuid, LocalDate date) {
        List<MealPlan> list = new ArrayList<>();
        String query = "SELECT * FROM meal_plans WHERE user_uuid = ? AND date = ? ORDER BY meal_type";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, userUuid);
            ps.setDate(2, Date.valueOf(date));
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(new MealPlan(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("user_uuid"),
                        rs.getDate("date").toLocalDate(),
                        rs.getString("day_of_week"),
                        rs.getString("meal_type"),
                        rs.getString("name"),
                        rs.getInt("calories"),
                        rs.getBoolean("is_completed")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean addPlan(MealPlan plan) {
        // created_at est requis (DATETIME NOT NULL) dans votre table meal_plans
        String query = "INSERT INTO meal_plans (user_id, user_uuid, date, day_of_week, meal_type, name, calories, is_completed, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW())";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, plan.getUserId());
            ps.setString(2, plan.getUserUuid());
            ps.setDate(3, Date.valueOf(plan.getDate()));
            ps.setString(4, plan.getDayOfWeek());
            ps.setString(5, plan.getMealType());
            ps.setString(6, plan.getName());
            ps.setInt(7, plan.getCalories());
            ps.setBoolean(8, plan.isCompleted());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean toggleCompletion(int id, boolean status) {
        String query = "UPDATE meal_plans SET is_completed = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = conn.prepareStatement(query)) {

            // is_completed est un TINYINT(4) dans votre BDD, on envoie 1 (true) ou 0 (false)
            ps.setInt(1, status ? 1 : 0);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deletePlan(int id) {
        String query = "DELETE FROM meal_plans WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}