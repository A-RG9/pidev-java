package com.wellcare.javafx;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class TestPasswordHash {
    public static void main(String[] args) {
        // Test the admin password hash from the database for admin123@gmail.com
        String storedHash = "$2a$12$UQoEjxwrdbhwfA3eaGMyU.gDjv3KtMV9kROGujJpQerfWsMjk3eSq";
        String password = "admin123";
        System.out.println("Testing hash for admin123@gmail.com");

        System.out.println("Testing password verification...");
        System.out.println("Stored Hash: " + storedHash);
        System.out.println("Password: " + password);

        BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), storedHash);

        if (result.verified) {
            System.out.println("✅ SUCCESS: Password matches the hash!");
        } else {
            System.out.println("❌ FAILED: Password does NOT match the hash!");
        }

        // Also test a few common variations
        String[] testPasswords = {"Admin123!", "admin123!", "Admin123", "admin123", "admin", "password", "123456"};

        System.out.println("\nTesting other common passwords:");
        for (String testPwd : testPasswords) {
            BCrypt.Result testResult = BCrypt.verifyer().verify(testPwd.toCharArray(), storedHash);
            System.out.println("Password '" + testPwd + "': " + (testResult.verified ? "✅ MATCH" : "❌ NO MATCH"));
        }
    }
}