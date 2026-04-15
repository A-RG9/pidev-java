package org.example.service;
import org.example.entities.Consultation;
import org.example.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
public class ConsulationServices implements CRUDconsultation<Consultation> {

    @Override
    public void AddConsultation(Consultation consultation) throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        
        // Check if connection is valid
        if (conn == null) {
            throw new SQLException("Database connection not available. Please check your database.");
        }
        
        if (!conn.isValid(5)) {
            throw new SQLException("Database connection is not valid. Please restart the application.");
        }
        
        // Insert only essential columns that exist in the database (16 parameters)
        String query = "INSERT INTO consultation (consultation_type, reason_for_visit, symptoms_description, " +
                       "date_consultation, time_consultation, duration, location, fee, status, notes, created_at, updated_at, " +
                       "appointment_mode, subjective, objective, assessment, plan) " +
                       "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW(), ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            // Set all parameters with default values for required fields
            pstmt.setString(1, consultation.getConsultationType() != null ? consultation.getConsultationType() : "Clinical Note");
            pstmt.setString(2, consultation.getReasonForVisit() != null ? consultation.getReasonForVisit() : "Consultation médicale");
            pstmt.setString(3, consultation.getSymptomsDescription() != null ? consultation.getSymptomsDescription() : "");
            pstmt.setDate(4, consultation.getDateConsultation() != null ? 
                         java.sql.Date.valueOf(consultation.getDateConsultation()) : java.sql.Date.valueOf(java.time.LocalDate.now()));
            pstmt.setTime(5, consultation.getTimeConsultation() != null ? 
                         java.sql.Time.valueOf(consultation.getTimeConsultation()) : java.sql.Time.valueOf(java.time.LocalTime.now()));
            pstmt.setObject(6, consultation.getDuration() != null ? consultation.getDuration() : 30);
            pstmt.setString(7, consultation.getLocation() != null ? consultation.getLocation() : "Tunis, Tunisia");
            pstmt.setObject(8, consultation.getFee() != null ? consultation.getFee() : 120);
            pstmt.setString(9, consultation.getStatus() != null ? consultation.getStatus() : "pending");
            pstmt.setString(10, consultation.getNotes() != null ? consultation.getNotes() : null);
            pstmt.setString(11, consultation.getAppointmentMode() != null ? consultation.getAppointmentMode() : "in-person");
            
            // Clinical notes fields (stored in notes field if columns don't exist)
            pstmt.setString(12, consultation.getSubjective() != null ? consultation.getSubjective() : "");
            pstmt.setString(13, consultation.getObjective() != null ? consultation.getObjective() : "");
            pstmt.setString(14, consultation.getAssessment() != null ? consultation.getAssessment() : "");
            pstmt.setString(15, consultation.getPlan() != null ? consultation.getPlan() : "");
            
            pstmt.executeUpdate();
            
            // Get generated ID
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    consultation.setId(rs.getInt(1));
                    System.out.println("Consultation added with ID: " + consultation.getId());
                }
            }
            
            System.out.println("Consultation added successfully!");
        }
    }

    @Override
    public List<Consultation> ShowConsultation() throws SQLException {
        List<Consultation> consultations = new ArrayList<>();
        // Only select columns that exist in the database
        String query = "SELECT id, consultation_type, reason_for_visit, symptoms_description, " +
                      "date_consultation, time_consultation, duration, location, fee, status, " +
                      "notes, created_at, updated_at, appointment_mode " +
                      "FROM consultation ORDER BY date_consultation DESC, time_consultation DESC";
        
        System.out.println("Executing query: " + query);
        
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            int count = 0;
            while (rs.next()) {
                count++;
                Consultation consultation = new Consultation();
                consultation.setId(rs.getInt("id"));
                consultation.setConsultationType(rs.getString("consultation_type"));
                consultation.setReasonForVisit(rs.getString("reason_for_visit"));
                consultation.setSymptomsDescription(rs.getString("symptoms_description"));
                
                Date date = rs.getDate("date_consultation");
                consultation.setDateConsultation(date != null ? date.toLocalDate() : null);
                
                Time time = rs.getTime("time_consultation");
                consultation.setTimeConsultation(time != null ? time.toLocalTime() : null);
                
                consultation.setDuration(rs.getInt("duration"));
                consultation.setLocation(rs.getString("location"));
                consultation.setFee(rs.getInt("fee"));
                consultation.setStatus(rs.getString("status"));
                consultation.setNotes(rs.getString("notes"));
                consultation.setAppointmentMode(rs.getString("appointment_mode"));
                
                consultations.add(consultation);
            }
            System.out.println("ShowConsultation found " + count + " consultations");
        } catch (SQLException e) {
            System.err.println("Error in ShowConsultation: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
        return consultations;
    }

    @Override
    public void ModifyConsultation(int id, Consultation consultation) throws SQLException {
        String query = "UPDATE consultation SET consultation_type = ?, reason_for_visit = ?, " +
                       "symptoms_description = ?, date_consultation = ?, time_consultation = ?, duration = ?, " +
                       "location = ?, fee = ?, status = ?, notes = ?, subjective = ?, " +
                       "objective = ?, assessment = ?, plan = ?, diagnoses = ?, updated_at = NOW() " +
                       "WHERE id = ?";
        
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, consultation.getConsultationType());
            pstmt.setString(2, consultation.getReasonForVisit());
            pstmt.setString(3, consultation.getSymptomsDescription());
            pstmt.setDate(4, consultation.getDateConsultation() != null ? 
                         java.sql.Date.valueOf(consultation.getDateConsultation()) : null);
            pstmt.setTime(5, consultation.getTimeConsultation() != null ? 
                         java.sql.Time.valueOf(consultation.getTimeConsultation()) : null);
            pstmt.setInt(6, consultation.getDuration() != null ? consultation.getDuration() : 0);
            pstmt.setString(7, consultation.getLocation());
            pstmt.setInt(8, consultation.getFee() != null ? consultation.getFee() : 0);
            pstmt.setString(9, consultation.getStatus());
            pstmt.setString(10, consultation.getNotes());
            pstmt.setString(11, consultation.getSubjective());
            pstmt.setString(12, consultation.getObjective());
            pstmt.setString(13, consultation.getAssessment());
            pstmt.setString(14, consultation.getPlan());
            // Handle diagnoses - use null if empty to satisfy foreign key constraint
            String diagnosesStr = listToString(consultation.getDiagnoses());
            if (diagnosesStr == null || diagnosesStr.isEmpty()) {
                pstmt.setNull(15, Types.VARCHAR);
            } else {
                pstmt.setString(15, diagnosesStr);
            }
            pstmt.setInt(16, id);
            
            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Consultation updated successfully!");
            } else {
                System.out.println("No consultation found with id: " + id);
            }
        }
    }

    @Override
    public void DeleteConsultation(int id) throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        
        // First delete related ordonnances
        String deleteOrdonnances = "DELETE FROM ordonnance WHERE consultation_id = ?";
        try (PreparedStatement pstmtOrd = conn.prepareStatement(deleteOrdonnances)) {
            pstmtOrd.setInt(1, id);
            pstmtOrd.executeUpdate();
        }
        
        // Then delete related examens
        String deleteExamens = "DELETE FROM examen WHERE consultation_id = ?";
        try (PreparedStatement pstmtExam = conn.prepareStatement(deleteExamens)) {
            pstmtExam.setInt(1, id);
            pstmtExam.executeUpdate();
        }
        
        // Finally delete the consultation
        String query = "DELETE FROM consultation WHERE id = ?";
        System.out.println("Deleting consultation with ID: " + id);
        
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);
            
            int rowsDeleted = pstmt.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("Consultation deleted successfully! Rows affected: " + rowsDeleted);
            } else {
                System.out.println("No consultation found with id: " + id);
            }
        }
    }

    // Helper method to convert List<String> to comma-separated String
    private String listToString(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "";
        }
        return String.join(",", list);
    }

    // Helper method to convert comma-separated String to List<String>
    private List<String> stringToList(String str) {
        List<String> list = new ArrayList<>();
        if (str != null && !str.isEmpty()) {
            for (String item : str.split(",")) {
                list.add(item.trim());
            }
        }
        return list;
    }
}
