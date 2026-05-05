package org.example.dao;

import org.example.models.Goal;
import org.example.utils.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GoalDaoImpl implements IGoalDao {

    @Override
    public void addGoal(Goal goal) {
        String query = "INSERT INTO goal (title, description, patient_id, coach_id, category, status, difficulty_level, progress, start_date, date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, goal.getTitle());
            stmt.setString(2, goal.getDescription());
            stmt.setString(3, goal.getPatientId());
            stmt.setString(4, goal.getCoachId());
            stmt.setString(5, goal.getCategory());
            stmt.setString(6, goal.getStatus());
            stmt.setString(7, goal.getDifficultyLevel());
            stmt.setInt(8, goal.getProgress());
            stmt.setDate(9, goal.getStartDate() != null ? Date.valueOf(goal.getStartDate()) : null);
            stmt.setDate(10, goal.getEndDate() != null ? Date.valueOf(goal.getEndDate()) : null);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateGoal(Goal goal) {
        String query = "UPDATE goal SET title=?, description=?, patient_id=?, coach_id=?, category=?, status=?, difficulty_level=?, progress=?, start_date=?, date=? WHERE id=?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, goal.getTitle());
            stmt.setString(2, goal.getDescription());
            stmt.setString(3, goal.getPatientId());
            stmt.setString(4, goal.getCoachId());
            stmt.setString(5, goal.getCategory());
            stmt.setString(6, goal.getStatus());
            stmt.setString(7, goal.getDifficultyLevel());
            stmt.setInt(8, goal.getProgress());
            stmt.setDate(9, goal.getStartDate() != null ? Date.valueOf(goal.getStartDate()) : null);
            stmt.setDate(10, goal.getEndDate() != null ? Date.valueOf(goal.getEndDate()) : null);
            stmt.setInt(11, goal.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteGoal(int id) {
        String query = "DELETE FROM goal WHERE id = ?";
        try (Connection conn = Database.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public List<Goal> getAllGoals() {
        List<Goal> goals = new ArrayList<>();
        String query = "SELECT * FROM goal";
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                goals.add(extractGoalFromResultSet(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return goals;
    }

    @Override
    public List<Goal> getGoalsByPatient(String patientId) {
        List<Goal> goals = new ArrayList<>();
        String query = "SELECT * FROM goal WHERE patient_id = ?";
        try (Connection conn = Database.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, patientId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) { goals.add(extractGoalFromResultSet(rs)); }
        } catch (SQLException e) { e.printStackTrace(); }
        return goals;
    }

    private Goal extractGoalFromResultSet(ResultSet rs) throws SQLException {
        return new Goal(
                rs.getInt("id"), rs.getString("title"), rs.getString("description"),
                rs.getString("patient_id"), rs.getString("coach_id"), rs.getString("category"),
                rs.getString("status"), rs.getString("difficulty_level"), rs.getInt("progress"),
                rs.getDate("start_date") != null ? rs.getDate("start_date").toLocalDate() : null,
                rs.getDate("date") != null ? rs.getDate("date").toLocalDate() : null
        );
    }
}