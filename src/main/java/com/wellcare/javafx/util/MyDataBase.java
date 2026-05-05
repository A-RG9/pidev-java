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
            // Create users table
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
                )
                """);

            // Create user_sessions table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS user_sessions (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    user_uuid VARCHAR(36) NOT NULL,
                    token VARCHAR(255) UNIQUE NOT NULL,
                    expires_at TIMESTAMP NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (user_uuid) REFERENCES users(uuid) ON DELETE CASCADE
                )
                """);

            // Create password_reset_tokens table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS password_reset_tokens (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    user_uuid VARCHAR(36) NOT NULL,
                    token VARCHAR(255) UNIQUE NOT NULL,
                    expires_at TIMESTAMP NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (user_uuid) REFERENCES users(uuid) ON DELETE CASCADE
                )
                """);

            // Create user_logs table
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
                )
                """);

            // Create professional_verifications table
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
                )
                """);

            System.out.println("Database schema initialized successfully.");

        } catch (Exception ex) {
            System.err.println("Failed to initialize database schema: " + ex.getMessage());
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
