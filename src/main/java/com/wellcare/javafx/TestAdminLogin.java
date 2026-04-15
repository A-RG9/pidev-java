package com.wellcare.javafx;

import com.wellcare.javafx.model.User;
import com.wellcare.javafx.service.UserService;

public class TestAdminLogin {
    public static void main(String[] args) {
        try {
            UserService userService = new UserService();

            // Test admin login with a known password hash from the web app
            String email = "admin123@gmail.com";
            String password = "admin123"; // This should match what's in your database

            System.out.println("Testing admin login...");
            System.out.println("Email: " + email);
            System.out.println("Password: " + password);

            User authenticatedUser = userService.authenticate(email, password);

            if (authenticatedUser != null) {
                System.out.println("✅ SUCCESS: Admin login successful!");
                System.out.println("User: " + authenticatedUser.getFirstName() + " " + authenticatedUser.getLastName());
                System.out.println("Role: " + authenticatedUser.getRole());
            } else {
                System.out.println("❌ FAILED: Admin login failed!");
            }

        } catch (Exception e) {
            System.out.println("❌ ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }
}