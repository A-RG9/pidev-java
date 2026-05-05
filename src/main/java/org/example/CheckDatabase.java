package org.example;

import org.example.service.ConsulationServices;
import java.sql.*;

public class CheckDatabase {
    public static void main(String[] args) {
        try {
            ConsulationServices cs = new ConsulationServices();
            
            // Check users
            System.out.println("=== USERS ===");
            Connection conn = java.sql.DriverManager.getConnection("jdbc:mysql://localhost:3306/wellora", "root", "");
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT id, email, first_name, last_name FROM user");
            while (rs.next()) {
                System.out.println("User: id=" + rs.getString("id") + 
                    ", email=" + rs.getString("email") + 
                    ", name=" + rs.getString("first_name") + " " + rs.getString("last_name"));
            }
            
            // Check consultations
            System.out.println("\n=== CONSULTATIONS ===");
            rs = stmt.executeQuery("SELECT id, patient_id, status, date_consultation, time_consultation FROM consultation");
            while (rs.next()) {
                System.out.println("Consultation: id=" + rs.getInt("id") + 
                    ", patient_id=" + rs.getString("patient_id") + 
                    ", status=" + rs.getString("status") +
                    ", date=" + rs.getDate("date_consultation") +
                    ", time=" + rs.getTime("time_consultation"));
            }
            
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
