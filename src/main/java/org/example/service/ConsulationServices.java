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

        if (conn == null) {
            throw new SQLException("Database connection not available. Please check your database.");
        }

        if (!conn.isValid(5)) {
            throw new SQLException("Database connection is not valid. Please restart the application.");
        }

        // INSERT includes patient_id column
        String query = "INSERT INTO consultation (consultation_type, reason_for_visit, symptoms_description, " +
                "date_consultation, time_consultation, duration, location, fee, status, notes, created_at, updated_at, " +
                "appointment_mode, subjective, objective, assessment, plan, patient_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW(), ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
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
            pstmt.setString(12, consultation.getSubjective() != null ? consultation.getSubjective() : "");
            pstmt.setString(13, consultation.getObjective() != null ? consultation.getObjective() : "");
            pstmt.setString(14, consultation.getAssessment() != null ? consultation.getAssessment() : "");
            pstmt.setString(15, consultation.getPlan() != null ? consultation.getPlan() : "");
            pstmt.setString(16, consultation.getPatientId());   // patient_id column, can be null

            pstmt.executeUpdate();

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
        // Added patient_id to SELECT
        String query = "SELECT id, consultation_type, reason_for_visit, symptoms_description, " +
                "date_consultation, time_consultation, duration, location, fee, status, " +
                "notes, created_at, updated_at, appointment_mode, patient_id " +
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
                consultation.setPatientId(rs.getString("patient_id"));   // <-- ADD THIS LINE

                // The rest of your existing code for optional fields remains unchanged
                try { consultation.setChiefComplaint(rs.getString("chief_complaint")); } catch (SQLException e) {}
                try { consultation.setSubjective(rs.getString("subjective")); } catch (SQLException e) {}
                try { consultation.setObjective(rs.getString("objective")); } catch (SQLException e) {}
                try { consultation.setAssessment(rs.getString("assessment")); } catch (SQLException e) {}
                try { consultation.setPlan(rs.getString("plan")); } catch (SQLException e) {}

                try {
                    String diagnosesStr = rs.getString("diagnoses");
                    if (diagnosesStr != null && !diagnosesStr.isEmpty()) {
                        List<String> diagnosesList = new ArrayList<>();
                        for (String d : diagnosesStr.split(",")) {
                            diagnosesList.add(d.trim());
                        }
                        consultation.setDiagnoses(diagnosesList);
                    }
                } catch (SQLException e) {}

                // Vitals
                try { int bpSys = rs.getInt("bp_systolic"); if (!rs.wasNull()) consultation.setBpSystolic(bpSys); } catch (SQLException e) {}
                try { int bpDia = rs.getInt("bp_diastolic"); if (!rs.wasNull()) consultation.setBpDiastolic(bpDia); } catch (SQLException e) {}
                try { int pulseVal = rs.getInt("pulse"); if (!rs.wasNull()) consultation.setPulse(pulseVal); } catch (SQLException e) {}
                try { double temp = rs.getDouble("temperature"); if (!rs.wasNull()) consultation.setTemperature(temp); } catch (SQLException e) {}
                try { int spo2Val = rs.getInt("spo2"); if (!rs.wasNull()) consultation.setSpo2(spo2Val); } catch (SQLException e) {}

                try { Date followUp = rs.getDate("follow_up_date"); if (followUp != null) consultation.setFollowUpDate(followUp.toLocalDate()); } catch (SQLException e) {}
                try { consultation.setFollowUpType(rs.getString("follow_up_type")); } catch (SQLException e) {}
                try { consultation.setFollowUpPriority(rs.getString("follow_up_priority")); } catch (SQLException e) {}

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

        String deleteOrdonnances = "DELETE FROM ordonnance WHERE consultation_id = ?";
        try (PreparedStatement pstmtOrd = conn.prepareStatement(deleteOrdonnances)) {
            pstmtOrd.setInt(1, id);
            pstmtOrd.executeUpdate();
        }

        String deleteExamens = "DELETE FROM examen WHERE consultation_id = ?";
        try (PreparedStatement pstmtExam = conn.prepareStatement(deleteExamens)) {
            pstmtExam.setInt(1, id);
            pstmtExam.executeUpdate();
        }

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

    /**
     * Retrieves patient details (email, first name, last name) from the user table.
     * @param patientId the patient UUID (varchar 36) from consultation.patient_id
     * @return String array: [email, firstName, lastName] or null if not found
     * @throws SQLException if database error occurs
     */
    public String[] getUserDetailsByPatientId(String patientId) throws SQLException {
        if (patientId == null || patientId.isEmpty()) return null;
        String query = "SELECT email, first_name, last_name FROM users WHERE uuid = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, patientId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new String[]{
                            rs.getString("email"),
                            rs.getString("first_name"),
                            rs.getString("last_name")
                    };
                }
            }
        }
        return null;
    }
}