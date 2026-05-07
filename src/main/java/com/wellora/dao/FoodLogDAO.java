package com.wellora.dao;

import com.wellora.models.FoodLog;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// DATA ACCESS OBJECT: Handles database operations
public class FoodLogDAO {
    private static final String URL = "jdbc:mysql://localhost:3306/wellora";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * Résout le vrai id entier (PK) d'un utilisateur depuis la table users,
     * en cherchant par son uuid. Cette valeur est utilisée comme user_id (INT NOT NULL)
     * lors des insertions dans food_logs.
     *
     * @param uuid  le UUID string de l'utilisateur connecté
     * @return      l'id entier réel, ou 0 si non trouvé
     */
    private int getRealUserIdByUuid(String uuid) {
        if (uuid == null || uuid.isEmpty()) return 0;
        String query = "SELECT id FROM users WHERE uuid = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, uuid);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            System.err.println("[FoodLogDAO] Impossible de résoudre user_id pour uuid=" + uuid);
            e.printStackTrace();
        }
        return 0;
    }

    public boolean addFoodLog(FoodLog log) {
        String query = "INSERT INTO food_logs (user_id, user_uuid, date, meal_type, total_calories, total_protein, total_carbs, total_fats) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            // Résolution du vrai user_id entier depuis la table users via uuid (jointure)
            int realUserId = getRealUserIdByUuid(log.getUserUuid());
            if (realUserId == 0) {
                // Fallback sur la valeur portée par le modèle (hashcode) si uuid inconnu
                realUserId = log.getUserId();
                System.err.println("[FoodLogDAO] ⚠️ user_id réel introuvable pour uuid='" + log.getUserUuid() + "', fallback=" + realUserId);
            }

            pstmt.setInt(1, realUserId);
            pstmt.setString(2, log.getUserUuid());
            pstmt.setDate(3, Date.valueOf(log.getDate()));
            pstmt.setString(4, log.getMealType());
            pstmt.setInt(5, log.getTotalCalories());
            pstmt.setDouble(6, log.getTotalProtein());
            pstmt.setDouble(7, log.getTotalCarbs());
            pstmt.setDouble(8, log.getTotalFats());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateFoodLog(FoodLog log) {
        String query = "UPDATE food_logs SET meal_type = ?, total_calories = ?, total_protein = ?, total_carbs = ?, total_fats = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, log.getMealType());
            pstmt.setInt(2, log.getTotalCalories());
            pstmt.setDouble(3, log.getTotalProtein());
            pstmt.setDouble(4, log.getTotalCarbs());
            pstmt.setDouble(5, log.getTotalFats());
            pstmt.setInt(6, log.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteFoodLog(int id) {
        String query = "DELETE FROM food_logs WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<FoodLog> getFoodLogsByDate(String userUuid, LocalDate date) {
        List<FoodLog> list = new ArrayList<>();
        String query = "SELECT * FROM food_logs WHERE user_uuid = ? AND date = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, userUuid);
            pstmt.setDate(2, Date.valueOf(date));
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(new FoodLog(
                        rs.getInt("id"),
                        rs.getString("user_uuid"),
                        rs.getDate("date").toLocalDate(),
                        rs.getString("meal_type"),
                        rs.getInt("total_calories"),
                        rs.getDouble("total_protein"),
                        rs.getDouble("total_carbs"),
                        rs.getDouble("total_fats")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public double[] getDailyTotals(String userUuid, LocalDate date) {
        String query = "SELECT SUM(total_calories), SUM(total_protein), SUM(total_carbs), SUM(total_fats) FROM food_logs WHERE user_uuid = ? AND date = ?";
        double[] totals = new double[4];

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, userUuid);
            pstmt.setDate(2, Date.valueOf(date));

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                totals[0] = rs.getDouble(1);
                totals[1] = rs.getDouble(2);
                totals[2] = rs.getDouble(3);
                totals[3] = rs.getDouble(4);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return totals;
    }
}
