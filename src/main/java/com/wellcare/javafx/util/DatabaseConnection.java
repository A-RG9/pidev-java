package com.wellcare.javafx.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Singleton database connection manager.
 * Provides a single point of access to the database connection.
 */
public class DatabaseConnection {
    private static DatabaseConnection instance;
    private Connection connection;
    
    // Database configuration - update these values for your environment
    private static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/wellora?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";
    
    /**
     * Private constructor to enforce singleton pattern.
     * @throws SQLException if connection fails
     */
    private DatabaseConnection() throws SQLException {
        try {
            System.out.println("Attempting to connect to MySQL at: " + DB_URL);
            System.out.println("Username: " + DB_USER);
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("Database connection established successfully.");

            // Initialize database schema
            initializeDatabaseSchema();

        } catch (ClassNotFoundException ex) {
            System.err.println("MySQL JDBC Driver not found: " + ex.getMessage());
            throw new SQLException("Database driver not found", ex);
        } catch (SQLException ex) {
            System.err.println("Database connection failed: " + ex.getMessage());
            System.err.println("SQL State: " + ex.getSQLState());
            System.err.println("Error Code: " + ex.getErrorCode());
            throw ex;
        }
    }

    /**
     * Initializes the database schema by creating tables if they don't exist.
     * @throws SQLException if schema initialization fails
     */
    private void initializeDatabaseSchema() throws SQLException {
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

            System.out.println("Database schema initialized successfully.");

        } catch (SQLException ex) {
            System.err.println("Failed to initialize database schema: " + ex.getMessage());
            throw ex;
        }
    }
    
    /**
     * Returns the active database connection.
     * @return the Connection object
     */
    public Connection getConnection() {
        return connection;
    }
    
    /**
     * Returns the singleton instance of DatabaseConnection.
     * Creates a new instance if none exists or if the connection is closed.
     * @return the DatabaseConnection instance
     * @throws SQLException if connection creation fails
     */
    public static DatabaseConnection getInstance() throws SQLException {
        if (instance == null) {
            instance = new DatabaseConnection();
        } else if (instance.getConnection() == null || instance.getConnection().isClosed()) {
            instance = new DatabaseConnection();
        }
        return instance;
    }
    
    /**
     * Closes the database connection.
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
    }
}
