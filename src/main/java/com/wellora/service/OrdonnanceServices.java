package com.wellora.service;
import com.wellora.model.Ordonnance;
import com.wellora.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrdonnanceServices implements CRUDordonnance {

    @Override
    public void AddOrdonnance(Ordonnance ordonnance) throws SQLException {
        String query = "INSERT INTO ordonnance (date_ordonnance, medicament, dosage, forme, " +
                       "duree_traitement, instructions, frequency, diagnosis_code, status, consultation_id, updated_at) " +
                       "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())";
        
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setDate(1, ordonnance.getDateOrdonnance() != null ?
                         java.sql.Date.valueOf(ordonnance.getDateOrdonnance()) : java.sql.Date.valueOf(java.time.LocalDate.now()));
            pstmt.setString(2, ordonnance.getMedicament() != null ? ordonnance.getMedicament() : "");
            pstmt.setString(3, ordonnance.getDosage() != null ? ordonnance.getDosage() : "");
            pstmt.setString(4, ordonnance.getForme() != null ? ordonnance.getForme() : "");
            pstmt.setString(5, ordonnance.getDureeTraitement() != null ? ordonnance.getDureeTraitement() : "");
            pstmt.setString(6, ordonnance.getInstructions() != null ? ordonnance.getInstructions() : "");
            pstmt.setString(7, ordonnance.getFrequency() != null ? ordonnance.getFrequency() : "");
            pstmt.setString(8, ordonnance.getDiagnosisCode() != null ? ordonnance.getDiagnosisCode() : "");
            pstmt.setString(9, ordonnance.getStatus() != null ? ordonnance.getStatus() : "active");
            pstmt.setInt(10, ordonnance.getConsultationId());
            
            pstmt.executeUpdate();
            System.out.println("Ordonnance added successfully!");
        }
    }

    @Override
    public List<Ordonnance> ShowOrdonnance() throws SQLException {
        List<Ordonnance> ordonnances = new ArrayList<>();
        String query = "SELECT * FROM ordonnance";
        
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Ordonnance ordonnance = new Ordonnance();
                ordonnance.setId(rs.getInt("id"));
                
                Date date = rs.getDate("date_ordonnance");
                ordonnance.setDateOrdonnance(date != null ? date.toLocalDate() : null);
                
                ordonnance.setMedicament(rs.getString("medicament"));
                ordonnance.setDosage(rs.getString("dosage"));
                ordonnance.setForme(rs.getString("forme"));
                ordonnance.setDureeTraitement(rs.getString("duree_traitement"));
                ordonnance.setInstructions(rs.getString("instructions"));
                ordonnance.setFrequency(rs.getString("frequency"));
                ordonnance.setDiagnosisCode(rs.getString("diagnosis_code"));
                
                // Try to get status, but handle if column doesn't exist
                try {
                    ordonnance.setStatus(rs.getString("status"));
                } catch (SQLException e) {
                    // Column doesn't exist, set default
                    ordonnance.setStatus("active");
                }
                
                ordonnances.add(ordonnance);
            }
        }
        return ordonnances;
    }

    @Override
    public void ModifyOrdonnance(int id, Ordonnance ordonnance) throws SQLException {
        String query = "UPDATE ordonnance SET date_ordonnance = ?, medicament = ?, dosage = ?, forme = ?, " +
                       "duree_traitement = ?, instructions = ?, frequency = ?, diagnosis_code = ?, status = ?, updated_at = NOW() " +
                       "WHERE id = ?";
        
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setDate(1, ordonnance.getDateOrdonnance() != null ?
                         java.sql.Date.valueOf(ordonnance.getDateOrdonnance()) : null);
            pstmt.setString(2, ordonnance.getMedicament());
            pstmt.setString(3, ordonnance.getDosage());
            pstmt.setString(4, ordonnance.getForme());
            pstmt.setString(5, ordonnance.getDureeTraitement());
            pstmt.setString(6, ordonnance.getInstructions());
            pstmt.setString(7, ordonnance.getFrequency());
            pstmt.setString(8, ordonnance.getDiagnosisCode());
            pstmt.setString(9, ordonnance.getStatus());
            pstmt.setInt(10, id);
            
            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Ordonnance updated successfully!");
            } else {
                System.out.println("No ordonnance found with id: " + id);
            }
        }
    }

    @Override
    public void DeleteOrdonnance(int id) throws SQLException {
        String query = "DELETE FROM ordonnance WHERE id = ?";
        
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);
            
            int rowsDeleted = pstmt.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("Ordonnance deleted successfully!");
            } else {
                System.out.println("No ordonnance found with id: " + id);
            }
        }
    }
}
