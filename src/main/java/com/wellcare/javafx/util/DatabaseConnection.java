package com.wellcare.javafx.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.InputStream;
import java.util.Properties;

/**
 * Singleton database connection manager.
 */
public class DatabaseConnection {
    private static volatile DatabaseConnection instance;
    private Connection connection;

    // Database configuration
    private static final String DB_URL;
    private static final String DB_USER;
    private static final String DB_PASSWORD;

    static {
        String url;
        String user;
        String pass;

        Properties props = new Properties();
        // Try to load from config.properties in resources
        try (InputStream input = DatabaseConnection.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                System.out.println("config.properties not found. Using default 'wellora' settings.");
                url = "jdbc:mysql://127.0.0.1:3306/wellora?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
                user = "root";
                pass = "";
            } else {
                props.load(input);
                url = props.getProperty("db.url", "jdbc:mysql://127.0.0.1:3306/wellora?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true");
                user = props.getProperty("db.username", "root");
                pass = props.getProperty("db.password", "");
                System.out.println("Database configuration loaded from config.properties");
            }
        } catch (Exception e) {
            System.err.println("Error loading config.properties, using defaults: " + e.getMessage());
            url = "jdbc:mysql://127.0.0.1:3306/wellora?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
            user = "root";
            pass = "";
        }

        // Final assignment to static final fields
        DB_URL = url;
        DB_USER = user;
        DB_PASSWORD = pass;
    }

    private DatabaseConnection() throws SQLException {
        try {
            System.out.println("Connecting to: " + DB_URL);
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("Database connection established successfully.");

            initializeDatabaseSchema();

        } catch (ClassNotFoundException ex) {
            throw new SQLException("MySQL JDBC Driver not found", ex);
        } catch (SQLException ex) {
            System.err.println("Connection failed! Error: " + ex.getMessage());
            throw ex;
        }
    }

    /**
     * Creates the user_sessions table with engine/charset matching the existing users table.
     * This ensures foreign key compatibility without modifying the existing users table.
     */
    private void createUserSessionsTableWithCompatibleSettings(Statement stmt) throws SQLException {
        // Try to detect the users table's storage engine and charset
        String usersEngine = "InnoDB"; // default fallback
        String usersCharset = "utf8mb4"; // default fallback
        String uuidCharset = null;
        String uuidCollation = null;
        
        try {
            // Query the users table status for engine and table-level charset
            String sql = "SHOW TABLE STATUS WHERE Name = 'users'";
            try (ResultSet rs = stmt.executeQuery(sql)) {
                if (rs.next()) {
                    String engine = rs.getString("Engine");
                    String collation = rs.getString("Collation");
                    
                    if (engine != null) {
                        usersEngine = engine;
                    }
                    if (collation != null && collation.contains("_")) {
                        usersCharset = collation.split("_")[0];
                    }
                    
                    System.out.println("Detected users table: Engine=" + usersEngine + ", Charset=" + usersCharset);
                }
            }
            
            // Query the uuid column's specific character set and collation
            String colSql = """
                SELECT CHARACTER_SET_NAME, COLLATION_NAME
                FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = 'users'
                  AND COLUMN_NAME = 'uuid'
                """;
            try (ResultSet rs = stmt.executeQuery(colSql)) {
                if (rs.next()) {
                    uuidCharset = rs.getString("CHARACTER_SET_NAME");
                    uuidCollation = rs.getString("COLLATION_NAME");
                    System.out.println("Detected users.uuid: Charset=" + uuidCharset + ", Collation=" + uuidCollation);
                }
            }
        } catch (SQLException e) {
            System.out.println("Could not detect users table settings, using defaults: " + e.getMessage());
        }
        
        // Build CREATE TABLE with detected settings
        // Explicitly set user_uuid column charset/collation to match users.uuid exactly
        String columnDef = "user_uuid VARCHAR(36)";
        if (uuidCharset != null) {
            columnDef += " CHARACTER SET " + uuidCharset;
        }
        if (uuidCollation != null) {
            columnDef += " COLLATE " + uuidCollation;
        }
        columnDef += " NOT NULL";
        
        String createSql = String.format("""
            CREATE TABLE IF NOT EXISTS user_sessions (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                %s,
                token VARCHAR(255) UNIQUE NOT NULL,
                expires_at TIMESTAMP NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_uuid) REFERENCES users(uuid) ON DELETE CASCADE
            ) ENGINE=%s DEFAULT CHARSET=%s
            """, columnDef, usersEngine, usersCharset);
        
        stmt.execute(createSql);
    }

    private void initializeDatabaseSchema() throws SQLException {
        System.out.println("Initializing database schema...");
        try (Statement stmt = connection.createStatement()) {
            // Users table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    uuid VARCHAR(36) PRIMARY KEY,
                    email VARCHAR(255) UNIQUE NOT NULL,
                    password VARCHAR(255) NOT NULL,
                    first_name VARCHAR(100) NOT NULL,
                    last_name VARCHAR(100) NOT NULL,
                    role VARCHAR(50) NOT NULL,
                    is_active BOOLEAN DEFAULT FALSE,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);

            // Sessions table - create with engine/charset matching the existing users table
            createUserSessionsTableWithCompatibleSettings(stmt);

            System.out.println("Database schema ready.");
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public static DatabaseConnection getInstance() throws SQLException {
        if (instance == null) {
            synchronized (DatabaseConnection.class) {
                if (instance == null) {
                    instance = new DatabaseConnection();
                }
            }
        } else if (instance.getConnection() == null || instance.getConnection().isClosed()) {
            instance = new DatabaseConnection();
        }
        return instance;
    }
}