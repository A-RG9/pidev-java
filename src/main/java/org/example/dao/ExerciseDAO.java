package org.example.dao;

import org.example.models.Exercise;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExerciseDAO {
    private final String URL = "jdbc:mysql://localhost:3306/wellora";
    private final String USER = "root";
    private final String PASSWORD = "";

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public void addExercise(Exercise ex) {
        // Correction de la requête pour inclure dynamiquement la difficulté
        String query = "INSERT INTO exercises (name, description, category, difficulty_level, default_unit, video_url, is_active, created_at, duration, calories, sets, reps) VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), ?, ?, ?, ?)";

        try (Connection cnx = getConnection(); PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setString(1, ex.getName());
            pst.setString(2, ex.getDescription());
            pst.setString(3, ex.getCategory());
            pst.setString(4, ex.getDifficultyLevel()); // RÉCUPÈRE LA DIFFICULTÉ DU FORMULAIRE
            pst.setString(5, "min");
            pst.setString(6, ex.getVideoUrl());
            pst.setInt(7, 1);
            pst.setInt(8, ex.getDuration());
            pst.setInt(9, 100);
            pst.setInt(10, 3);
            pst.setInt(11, 12);
            pst.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public List<Exercise> getAllExercises() {
        List<Exercise> list = new ArrayList<>();
        String query = "SELECT * FROM exercises";
        try (Connection cnx = getConnection();
             Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            while (rs.next()) {
                // L'ordre des arguments ici doit être EXACTEMENT celui du constructeur ci-dessus
                list.add(new Exercise(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("category"),
                        rs.getString("difficulty_level"),
                        rs.getString("default_unit"),
                        rs.getString("video_url"),
                        rs.getInt("duration"),
                        rs.getInt("calories"),
                        rs.getInt("sets"),
                        rs.getInt("reps")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // --- MODIFIER UN EXERCICE ---
    public void updateExercise(Exercise ex) {
        // Ajout de difficulty_level dans le SET de la requête UPDATE
        String query = "UPDATE exercises SET name=?, description=?, category=?, difficulty_level=?, video_url=?, duration=? WHERE id=?";

        try (Connection cnx = getConnection();
             PreparedStatement pst = cnx.prepareStatement(query)) {

            pst.setString(1, ex.getName());
            pst.setString(2, ex.getDescription());
            pst.setString(3, ex.getCategory());
            pst.setString(4, ex.getDifficultyLevel()); // RÉCUPÈRE LA DIFFICULTÉ DU FORMULAIRE
            pst.setString(5, ex.getVideoUrl());
            pst.setInt(6, ex.getDuration());
            pst.setInt(7, ex.getId());

            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    // --- SUPPRIMER UN EXERCICE ---
    public void deleteExercise(int id) {
        // Option A : Suppression totale
        String query = "DELETE FROM exercises WHERE id = ?";

        // Option B : Si vous préférez utiliser votre colonne 'is_active'
        // String query = "UPDATE exercises SET is_active = 0 WHERE id = ?";

        try (Connection cnx = getConnection();
             PreparedStatement pst = cnx.prepareStatement(query)) {

            pst.setInt(1, id);
            pst.executeUpdate();
            System.out.println("Exercice supprimé !");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}