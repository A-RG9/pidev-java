package org.example.dao;

import org.example.models.DailyPlan;
import org.example.models.Exercise;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DailyPlanDAO {
    private final String URL = "jdbc:mysql://localhost:3306/wellora";
    private final String USER = "root";
    private final String PASS = "";

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    // --- LIRE TOUS LES PLANS ---
    public List<DailyPlan> getAllPlans() {
        List<DailyPlan> list = new ArrayList<>();
        String query = "SELECT * FROM daily_plan ORDER BY date DESC";

        try (Connection cnx = getConnection();
             Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            while (rs.next()) {
                DailyPlan p = new DailyPlan();
                p.setId(rs.getInt("id"));
                p.setGoalId(rs.getInt("goal_id"));
                p.setTitre(rs.getString("titre"));
                p.setDate(rs.getDate("date"));
                p.setNotes(rs.getString("notes"));
                p.setDureeMin(rs.getInt("duree_min"));
                p.setCalories(rs.getInt("calories"));
                p.setStatus(rs.getString("status"));
                list.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // --- SAUVEGARDER (INSERT) ---
    public boolean saveDailyPlan(DailyPlan plan, List<Exercise> exercises) {
        String sqlPlan = "INSERT INTO daily_plan (goal_id, titre, date, notes, duree_min, calories, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String sqlJointure = "INSERT INTO daily_plan_exercises (daily_plan_id, exercises_id) VALUES (?, ?)";

        try (Connection c = getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement ps = c.prepareStatement(sqlPlan, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, plan.getGoalId());
                ps.setString(2, plan.getTitre());
                ps.setDate(3, plan.getDate());
                ps.setString(4, (plan.getNotes() == null || plan.getNotes().isEmpty()) ? "Pas de notes" : plan.getNotes());
                ps.setInt(5, plan.getDureeMin());
                ps.setInt(6, plan.getCalories());
                ps.setString(7, plan.getStatus());

                ps.executeUpdate();
                ResultSet rs = ps.getGeneratedKeys();

                if (rs.next()) {
                    int planId = rs.getInt(1);
                    try (PreparedStatement psEx = c.prepareStatement(sqlJointure)) {
                        for (Exercise ex : exercises) {
                            psEx.setInt(1, planId);
                            psEx.setInt(2, ex.getId());
                            psEx.addBatch();
                        }
                        psEx.executeBatch();
                    }
                }
                c.commit();
                return true;
            } catch (SQLException e) {
                c.rollback();
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // --- METTRE À JOUR (UPDATE) ---
    public boolean updateDailyPlan(DailyPlan plan, List<Exercise> exercises) {
        String sqlUpdatePlan = "UPDATE daily_plan SET goal_id=?, titre=?, date=?, notes=?, duree_min=?, calories=?, status=? WHERE id=?";
        String sqlDeleteJointure = "DELETE FROM daily_plan_exercises WHERE daily_plan_id=?";
        String sqlInsertJointure = "INSERT INTO daily_plan_exercises (daily_plan_id, exercises_id) VALUES (?, ?)";

        try (Connection c = getConnection()) {
            c.setAutoCommit(false);
            try {
                // 1. Update des infos de base
                try (PreparedStatement ps = c.prepareStatement(sqlUpdatePlan)) {
                    ps.setInt(1, plan.getGoalId());
                    ps.setString(2, plan.getTitre());
                    ps.setDate(3, plan.getDate());
                    ps.setString(4, plan.getNotes());
                    ps.setInt(5, plan.getDureeMin());
                    ps.setInt(6, plan.getCalories());
                    ps.setString(7, plan.getStatus());
                    ps.setInt(8, plan.getId());
                    ps.executeUpdate();
                }

                // 2. Refresh des exercices (Supprimer puis ré-insérer)
                try (PreparedStatement psDel = c.prepareStatement(sqlDeleteJointure)) {
                    psDel.setInt(1, plan.getId());
                    psDel.executeUpdate();
                }

                try (PreparedStatement psIns = c.prepareStatement(sqlInsertJointure)) {
                    for (Exercise ex : exercises) {
                        psIns.setInt(1, plan.getId());
                        psIns.setInt(2, ex.getId());
                        psIns.addBatch();
                    }
                    psIns.executeBatch();
                }

                c.commit();
                return true;
            } catch (SQLException e) {
                c.rollback();
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // --- SUPPRIMER (DELETE) ---
    public boolean deleteDailyPlan(int planId) {
        String sqlJointure = "DELETE FROM daily_plan_exercises WHERE daily_plan_id = ?";
        String sqlPlan = "DELETE FROM daily_plan WHERE id = ?";

        try (Connection c = getConnection()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement ps1 = c.prepareStatement(sqlJointure)) {
                    ps1.setInt(1, planId);
                    ps1.executeUpdate();
                }
                try (PreparedStatement ps2 = c.prepareStatement(sqlPlan)) {
                    ps2.setInt(1, planId);
                    int deleted = ps2.executeUpdate();
                    c.commit();
                    return deleted > 0;
                }
            } catch (SQLException e) {
                c.rollback();
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // --- LIRE LES EXERCICES D'UN PLAN ---
    public List<Exercise> getExercisesForPlan(int planId) {
        List<Exercise> list = new ArrayList<>();
        String query = "SELECT e.* FROM exercises e " +
                "JOIN daily_plan_exercises dpe ON e.id = dpe.exercises_id " +
                "WHERE dpe.daily_plan_id = ?";

        try (Connection cnx = getConnection();
             PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, planId);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                list.add(new Exercise(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("category"),
                        rs.getString("difficulty_level"),
                        "min",
                        rs.getString("video_url"),
                        rs.getInt("duration"),
                        0, 0, 0
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}