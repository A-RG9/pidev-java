package com.wellcare.javafx.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class MyDataBase {
    final String USERNAME = "root";
    final String URL = "jdbc:mysql://localhost:3306/wellora";
    final String PASSWORD = "";
    Connection connection;
    static MyDataBase instance;

    private MyDataBase() {
        try {
            this.connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/wellora", "root", "");
            System.out.println("Connection established");

            // Initialize database schema
            initializeDatabaseSchema();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Creates the database tables if they don't exist.
     */
    private void initializeDatabaseSchema() {
        System.out.println("Initializing database schema...");

        try (Statement stmt = connection.createStatement()) {
            // --- 1. CORE TABLES ---
            try {
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS users (
                        uuid VARCHAR(36) PRIMARY KEY,
                        email VARCHAR(255) UNIQUE NOT NULL,
                        password VARCHAR(255) NOT NULL,
                        first_name VARCHAR(100) NOT NULL,
                        last_name VARCHAR(100) NOT NULL,
                        birthdate DATE,
                        phone VARCHAR(20),
                        avatar_url VARCHAR(500),
                        address VARCHAR(255),
                        role VARCHAR(50) NOT NULL,
                        license_number VARCHAR(50),
                        specialite VARCHAR(100),
                        diploma_url VARCHAR(500),
                        is_active BOOLEAN DEFAULT FALSE,
                        is_email_verified BOOLEAN DEFAULT FALSE,
                        is_verified_by_admin BOOLEAN DEFAULT FALSE,
                        verification_date TIMESTAMP,
                        email_verification_token VARCHAR(255),
                        email_verification_expires_at TIMESTAMP,
                        reset_token VARCHAR(255),
                        reset_token_expires_at TIMESTAMP,
                        login_attempts INT DEFAULT 0,
                        locked_until TIMESTAMP,
                        last_login_at TIMESTAMP,
                        last_session_id VARCHAR(255),
                        google_id VARCHAR(255),
                        is_two_factor_enabled BOOLEAN DEFAULT FALSE,
                        totp_secret VARCHAR(255),
                        years_of_experience INT DEFAULT 0,
                        about TEXT,
                        education TEXT,
                        certifications TEXT,
                        hospital_affiliations TEXT,
                        awards TEXT,
                        consultation_price INT,
                        lot VARCHAR(100),
                        token VARCHAR(255),
                        rating DECIMAL(3,2) DEFAULT 0.00,
                        backup_codes TEXT,
                        verification_score INT,
                        verification_description TEXT,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                    """);
            } catch (Exception e) {
                System.err.println("Error creating users table: " + e.getMessage());
            }

            // --- 2. NUTRITION MODULE FIXES (PRIORITY) ---
            try {
                // Create nutrition_goals table
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS nutrition_goals (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        user_id VARCHAR(36) NOT NULL,
                        name VARCHAR(255),
                        goal_type VARCHAR(100),
                        calories_target INT,
                        weight_target DOUBLE,
                        target_date DATE,
                        status VARCHAR(50) DEFAULT 'ACTIVE',
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        FOREIGN KEY (user_id) REFERENCES users(uuid) ON DELETE CASCADE
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                    """);

                // Migration: Fix user_id type in nutrition_goals if it was created as INT
                try {
                    stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
                    stmt.execute("ALTER TABLE nutrition_goals MODIFY COLUMN user_id VARCHAR(36)");
                    stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
                    System.out.println("Nutrition goals table migrated successfully.");
                } catch (Exception e) {
                    System.err.println("Migration failed in MyDataBase (might be already correct): " + e.getMessage());
                }
            } catch (Exception e) {
                System.err.println("Error fixing nutrition_goals: " + e.getMessage());
            }

            // --- 3. OTHER TABLES (NON-FATAL) ---
            try {
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS user_sessions (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        user_uuid VARCHAR(36) NOT NULL,
                        token VARCHAR(255) UNIQUE NOT NULL,
                        expires_at TIMESTAMP NOT NULL,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (user_uuid) REFERENCES users(uuid) ON DELETE CASCADE
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                    """);
            } catch (Exception e) {
                System.out.println("Note: user_sessions table skipped: " + e.getMessage());
            }

            try {
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS password_reset_tokens (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        user_uuid VARCHAR(36) NOT NULL,
                        token VARCHAR(255) UNIQUE NOT NULL,
                        expires_at TIMESTAMP NOT NULL,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (user_uuid) REFERENCES users(uuid) ON DELETE CASCADE
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                    """);
            } catch (Exception e) {
                System.out.println("Note: password_reset_tokens table skipped: " + e.getMessage());
            }

            try {
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS user_logs (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        user_uuid VARCHAR(36),
                        action VARCHAR(100) NOT NULL,
                        details TEXT,
                        ip_address VARCHAR(45),
                        user_agent TEXT,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (user_uuid) REFERENCES users(uuid) ON DELETE SET NULL
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                    """);
            } catch (Exception e) {
                System.out.println("Note: user_logs table skipped: " + e.getMessage());
            }

            try {
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS professional_verifications (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        professional_uuid VARCHAR(36) NOT NULL,
                        professional_email VARCHAR(255),
                        license_number VARCHAR(255),
                        specialty VARCHAR(255),
                        diploma_path VARCHAR(500),
                        diploma_filename VARCHAR(255),
                        extracted_data JSON,
                        confidence_score INT,
                        status ENUM('pending', 'processing', 'verified', 'rejected', 'manual_review') DEFAULT 'pending',
                        validation_details JSON,
                        forgery_indicators JSON,
                        rejection_reason TEXT,
                        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                        verified_at DATETIME,
                        reviewed_by VARCHAR(255),
                        FOREIGN KEY (professional_uuid) REFERENCES users(uuid) ON DELETE CASCADE
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                    """);
            } catch (Exception e) {
                System.out.println("Note: professional_verifications table skipped: " + e.getMessage());
            }

            try {
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS food_logs (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        user_id INT,
                        user_uuid VARCHAR(36) NOT NULL,
                        date DATE NOT NULL,
                        meal_type VARCHAR(50),
                        total_calories INT,
                        total_protein DOUBLE,
                        total_carbs DOUBLE,
                        total_fats DOUBLE,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (user_uuid) REFERENCES users(uuid) ON DELETE CASCADE
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                    """);
            } catch (Exception e) {
                System.out.println("Note: food_logs table skipped: " + e.getMessage());
            }

            try {
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS meal_plans (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        user_id INT,
                        user_uuid VARCHAR(36) NOT NULL,
                        date DATE NOT NULL,
                        day_of_week VARCHAR(20),
                        meal_type VARCHAR(50),
                        name VARCHAR(255),
                        calories INT,
                        is_completed BOOLEAN DEFAULT FALSE,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (user_uuid) REFERENCES users(uuid) ON DELETE CASCADE
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                    """);
            } catch (Exception e) {
                System.out.println("Note: meal_plans table skipped: " + e.getMessage());
            }

            System.out.println("Database schema initialization completed.");

        } catch (Exception ex) {
            System.err.println("Fatal error in initializeDatabaseSchema: " + ex.getMessage());
        }
    }

    public static MyDataBase getInstance() {
        if (instance == null) {
            instance = new MyDataBase();
        }
        return instance;
    }

    public Connection getConnection() {
        return this.connection;
    }
}
