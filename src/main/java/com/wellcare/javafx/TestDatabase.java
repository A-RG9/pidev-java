package com.wellcare.javafx;

import com.wellcare.javafx.dao.UserDAO;
import com.wellcare.javafx.model.User;
import java.sql.SQLException;
import java.util.List;

public class TestDatabase {
    public static void main(String[] args) {
        try {
            UserDAO userDAO = new UserDAO();

            System.out.println("=== Checking Database Contents ===");

            // Get all users
            List<User> users = userDAO.getAllUsers();
            System.out.println("Total users in database: " + users.size());

            for (User user : users) {
                System.out.println("\n--- User Details ---");
                System.out.println("UUID: " + user.getUuid());
                System.out.println("Email: " + user.getEmail());
                System.out.println("First Name: " + user.getFirstName());
                System.out.println("Last Name: " + user.getLastName());
                System.out.println("Role: " + user.getRole());
                System.out.println("Active: " + user.isActive());
                System.out.println("Password Hash: " + user.getPassword());
                System.out.println("Created At: " + user.getCreatedAt());
            }

            // Check for admin user specifically
            User adminUser = userDAO.getUserByEmail("admin@wellcare.com");
            if (adminUser != null) {
                System.out.println("\n=== Admin User Found ===");
                System.out.println("Email: " + adminUser.getEmail());
                System.out.println("Role: " + adminUser.getRole());
                System.out.println("Active: " + adminUser.isActive());
                System.out.println("Password Hash: " + adminUser.getPassword());
            } else {
                System.out.println("\n=== Admin User NOT Found ===");
                System.out.println("No user with email 'admin@wellcare.com' exists in the database.");
            }

            // Check for the specific user that failed login
            User testUser = userDAO.getUserByEmail("emailnn@gmail.com");
            if (testUser != null) {
                System.out.println("\n=== Test User (emailnn@gmail.com) Found ===");
                System.out.println("UUID: " + testUser.getUuid());
                System.out.println("Email: " + testUser.getEmail());
                System.out.println("First Name: " + testUser.getFirstName());
                System.out.println("Last Name: " + testUser.getLastName());
                System.out.println("Role: " + testUser.getRole());
                System.out.println("Active: " + testUser.isActive());
                System.out.println("Email Verified: " + testUser.isEmailVerified());
                System.out.println("Verified by Admin: " + testUser.isVerifiedByAdmin());
                System.out.println("Password Hash: " + testUser.getPassword());
                System.out.println("Created At: " + testUser.getCreatedAt());
            } else {
                System.out.println("\n=== Test User NOT Found ===");
                System.out.println("No user with email 'emailnn@gmail.com' exists in the database.");
            }

        } catch (Exception e) {
            System.out.println("❌ ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }
}