package com.wellcare.javafx;

import com.wellcare.javafx.model.User;
import com.wellcare.javafx.service.UserService;
import com.wellcare.javafx.util.MyDataBase;
import com.wellcare.javafx.util.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.Statement;

/**
 * Main JavaFX Application class for WellCare Connect
 */
public class MainApplication extends Application {

    private UserService userService;

    @Override
    public void start(Stage primaryStage) {
        System.out.println("MainApplication.start() called");
        try {
            System.out.println("Initializing database connection and schema...");
            // Initialize database connection and schema
            initializeDatabase();
            System.out.println("Database initialization completed");

            // Create UserService
            try {
                userService = new UserService();
            } catch (Exception e) {
                System.err.println("Failed to create UserService: " + e.getMessage());
                System.err.println("Continuing with limited functionality (database not available)");
                userService = null;
            }

            // Create admin user for testing if it doesn't exist
            try {
                createAdminUserIfNotExists();
            } catch (Exception e) {
                System.err.println("Failed to create admin user: " + e.getMessage());
                System.err.println("Continuing without admin user creation (database schema issue)");
            }

            // Initialize SceneManager
            SceneManager sceneManager = SceneManager.getInstance();
            if (userService != null) {
                sceneManager.initialize(primaryStage, userService);
            } else {
                // Initialize with null service for UI testing
                sceneManager.initialize(primaryStage, null);
            }

            // Start with login screen
            sceneManager.switchTo(SceneManager.LOGIN);

            System.out.println("Application started successfully!");
            System.out.println("Admin login: admin@wellcare.com / admin123");

        } catch (Exception e) {
            System.err.println("Failed to start application: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Initializes the database connection and creates tables if they don't exist.
     */
    private void initializeDatabase() {
        try {
            System.out.println("Getting database connection...");
            // Get database connection (this will trigger schema creation in MyDataBase)
            Connection conn = MyDataBase.getInstance().getConnection();
            System.out.println("Database connection obtained successfully");

            // Initialize database schema
            initializeDatabaseSchema(conn);

        } catch (Exception e) {
            System.err.println("Failed to initialize database: " + e.getMessage());
            e.printStackTrace();
            System.err.println("Continuing with application startup (database schema skipped for UI testing)");
        }
    }

    /**
     * Creates the database tables if they don't exist.
     */
    private void initializeDatabaseSchema(Connection conn) throws Exception {
        System.out.println("Initializing database schema...");

        try (Statement stmt = conn.createStatement()) {
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
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                    """);
            } catch (Exception e) { System.err.println("Error creating users table: " + e.getMessage()); }

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
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                    """);

                // Migration: Fix user_id type in nutrition_goals if it was created as INT
                try {
                    stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
                    stmt.execute("ALTER TABLE nutrition_goals MODIFY COLUMN user_id VARCHAR(36)");
                    stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
                    System.out.println("Nutrition goals table migrated successfully in MainApplication.");
                } catch (Exception e) {
                    System.err.println("Migration failed in MainApplication (might be already correct): " + e.getMessage());
                }
            } catch (Exception e) { System.err.println("Error fixing nutrition_goals in MainApplication: " + e.getMessage()); }

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
            } catch (Exception e) { System.out.println("Note: user_sessions skipped in MainApp: " + e.getMessage()); }

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
            } catch (Exception e) { System.out.println("Note: password_reset_tokens skipped in MainApp: " + e.getMessage()); }

            try {
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS user_logs (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        user_uuid VARCHAR(36),
                        action VARCHAR(100) NOT NULL,
                        details TEXT,
                        ip_address VARCHAR(45),
                        user_agent TEXT,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                    """);
            } catch (Exception e) { System.out.println("Note: user_logs skipped in MainApp: " + e.getMessage()); }

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
            } catch (Exception e) { System.out.println("Note: food_logs skipped in MainApp: " + e.getMessage()); }

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
            } catch (Exception e) { System.out.println("Note: meal_plans skipped in MainApp: " + e.getMessage()); }

            System.out.println("Database schema initialization finished successfully.");

        } catch (Exception ex) {
            System.err.println("Fatal error in initializeDatabaseSchema (MainApp): " + ex.getMessage());
            throw ex;
        }
    }

    /**
     * Creates an admin user for testing if one doesn't already exist
     */
    private void createAdminUserIfNotExists() {
        try {
            // Try to find existing admin user
            User existingAdmin = userService.authenticate("admin123@gmail.com", "admin123");
            if (existingAdmin != null) {
                System.out.println("Admin user already exists and can login.");
                return;
            }
        } catch (Exception e) {
            // Admin doesn't exist or authentication failed, create new one
            System.out.println("Admin user not found or authentication failed, creating new admin user...");
        }

        try {
            // Create admin user
            System.out.println("Creating admin user for testing...");

            User adminUser = new User();
            adminUser.setUuid(java.util.UUID.randomUUID().toString());
            adminUser.setEmail("admin123@gmail.com");
            adminUser.setFirstName("WellCare");
            adminUser.setLastName("Admin");
            adminUser.setPassword("admin123"); // Will be hashed by service
            adminUser.setRole("ROLE_ADMIN");
            adminUser.setActive(true);
            adminUser.setVerifiedByAdmin(true);
            adminUser.setEmailVerified(true);
            adminUser.setCreatedAt(java.time.LocalDateTime.now());
            adminUser.setUpdatedAt(java.time.LocalDateTime.now());

            // Bypass Service validation and insert directly through DAO for this testing admin
            com.wellcare.javafx.dao.UserDAO userDAO = new com.wellcare.javafx.dao.UserDAO();
            String hash = at.favre.lib.crypto.bcrypt.BCrypt.withDefaults().hashToString(12, "admin123".toCharArray());
            userDAO.createUser(adminUser, hash);
            System.out.println("✅ Admin user created successfully!");
            System.out.println("Email: admin123@gmail.com");
            System.out.println("Password: admin123");

        } catch (Exception e) {
            System.err.println("❌ Error creating admin user: " + e.getMessage());
            e.printStackTrace();
            // Don't fail the application if admin creation fails
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}