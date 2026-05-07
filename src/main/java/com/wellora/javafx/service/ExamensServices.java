package com.wellora.javafx.service;
import com.wellora.javafx.model.Examens;
import com.wellora.javafx.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExamensServices implements CRUDexamens {

    @Override
    public void AddExamens(Examens examens) throws SQLException {
        String query = "INSERT INTO examens (type_examen, date_examen, resultat, status, notes, nom_examen, " +
                       "date_realisation, result_file, doctor_analysis, doctor_treatment, consultation_id, medecin_id, updated_at) " +
                       "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())";
        
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, examens.getTypeExamen() != null ? examens.getTypeExamen() : "");
            pstmt.setDate(2, examens.getDateExamen() != null ? 
                         java.sql.Date.valueOf(examens.getDateExamen()) : java.sql.Date.valueOf(java.time.LocalDate.now()));
            pstmt.setString(3, examens.getResultat() != null ? examens.getResultat() : "");
            pstmt.setString(4, examens.getStatus() != null ? examens.getStatus() : "pending");
            pstmt.setString(5, examens.getNotes() != null ? examens.getNotes() : "");
            pstmt.setString(6, examens.getNomExamen() != null ? examens.getNomExamen() : "");
            pstmt.setDate(7, examens.getDateRealisation() != null ?
                         java.sql.Date.valueOf(examens.getDateRealisation()) : java.sql.Date.valueOf(java.time.LocalDate.now()));
            pstmt.setString(8, examens.getResultFile() != null ? examens.getResultFile() : "");
            pstmt.setString(9, examens.getDoctorAnalysis() != null ? examens.getDoctorAnalysis() : "");
            pstmt.setString(10, examens.getDoctorTreatment() != null ? examens.getDoctorTreatment() : "");
            pstmt.setInt(11, getConsultationId(examens));
            pstmt.setInt(12, 0); // medecin_id default
            
            pstmt.executeUpdate();
            System.out.println("Examens added successfully!");
        }
    }
    
    private int getConsultationId(Examens examens) {
        // Try to get consultationId from the entity
        try {
            return examens.getConsultationId();
        } catch (Exception e) {
            // Fall back to getting it from the Consultation object
            if (examens.getConsultation() != null) {
                return examens.getConsultation().getId();
            }
            return 0;
        }
    }

    @Override
    public List<Examens> ShowExamens() throws SQLException {
        List<Examens> examenList = new ArrayList<>();
        String query = "SELECT id, type_examen, date_examen, resultat, status, notes, " +
                     "nom_examen, date_realisation, result_file, doctor_analysis, doctor_treatment, consultation_id " +
                     "FROM examens";
        
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Examens examens = new Examens();
                examens.setId(rs.getInt("id"));
                examens.setTypeExamen(rs.getString("type_examen"));
                
                Date dateEx = rs.getDate("date_examen");
                examens.setDateExamen(dateEx != null ? dateEx.toLocalDate() : null);
                
                examens.setResultat(rs.getString("resultat"));
                examens.setStatus(rs.getString("status"));
                examens.setNotes(rs.getString("notes"));
                examens.setNomExamen(rs.getString("nom_examen"));
                
                Date dateReal = rs.getDate("date_realisation");
                examens.setDateRealisation(dateReal != null ? dateReal.toLocalDate() : null);
                
                examens.setResultFile(rs.getString("result_file"));
                examens.setDoctorAnalysis(rs.getString("doctor_analysis"));
                examens.setDoctorTreatment(rs.getString("doctor_treatment"));
                
                try {
                    examens.setConsultationId(rs.getInt("consultation_id"));
                } catch (SQLException e) {
                    // Column might not exist or be null
                }
                
                examenList.add(examens);
            }
        }
        return examenList;
    }

    @Override
    public void ModifyExamens(int id, Examens examens) throws SQLException {
        String query = "UPDATE examens SET type_examen = ?, date_examen = ?, resultat = ?, status = ?, " +
                       "notes = ?, nom_examen = ?, date_realisation = ?, result_file = ?, " +
                       "doctor_analysis = ?, doctor_treatment = ?, updated_at = NOW() WHERE id = ?";
        
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, examens.getTypeExamen());
            pstmt.setDate(2, examens.getDateExamen() != null ? 
                         java.sql.Date.valueOf(examens.getDateExamen()) : null);
            pstmt.setString(3, examens.getResultat());
            pstmt.setString(4, examens.getStatus());
            pstmt.setString(5, examens.getNotes());
            pstmt.setString(6, examens.getNomExamen());
            pstmt.setDate(7, examens.getDateRealisation() != null ?
                         java.sql.Date.valueOf(examens.getDateRealisation()) : null);
            pstmt.setString(8, examens.getResultFile());
            pstmt.setString(9, examens.getDoctorAnalysis());
            pstmt.setString(10, examens.getDoctorTreatment());
            pstmt.setInt(11, id);
            
            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Examens updated successfully!");
            } else {
                System.out.println("No examens found with id: " + id);
            }
        }
    }

    @Override
    public void DeleteExamens(int id) throws SQLException {
        String query = "DELETE FROM examens WHERE id = ?";
        
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);
            
            int rowsDeleted = pstmt.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("Examens deleted successfully!");
            } else {
                System.out.println("No examens found with id: " + id);
            }
        }
    }
}
