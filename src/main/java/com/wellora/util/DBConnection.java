package com.wellora.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * DBConnection — JDBC Connection Utility
 *
 * Supports two databases:
 *   - Nutrition module  → db.properties          (wellora)
 *   - Health/integ module → db-health.properties  (wellora_health)
 *
 * Configure both files in src/main/resources/ with your MySQL credentials.
 */
public class DBConnection {

    private static String url;
    private static String username;
    private static String password;

    private static String healthUrl;
    private static String healthUsername;
    private static String healthPassword;

    static {
        loadConfig("db.properties", false);
        loadConfig("db-health.properties", true);
    }

    private static void loadConfig(String fileName, boolean isHealth) {
        try (InputStream input = DBConnection.class.getClassLoader().getResourceAsStream(fileName)) {
            if (input == null) {
                if (isHealth) {
                    // Fall back to main db.properties for health module
                    healthUrl = url;
                    healthUsername = username;
                    healthPassword = password;
                }
                return;
            }
            Properties props = new Properties();
            props.load(input);
            try { Class.forName(props.getProperty("db.driver")); } catch (Exception ignored) {}
            if (isHealth) {
                healthUrl      = props.getProperty("db.url");
                healthUsername = props.getProperty("db.username");
                healthPassword = props.getProperty("db.password", "");
            } else {
                url      = props.getProperty("db.url");
                username = props.getProperty("db.username");
                password = props.getProperty("db.password", "");
            }
        } catch (Exception e) {
            if (!isHealth) throw new RuntimeException("Failed to load " + fileName, e);
        }
    }

    /** Connection for Nutrition module (wellora DB) */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    /** Connection for Health/integ module (wellora_health DB) */
    public static Connection getHealthConnection() throws SQLException {
        return DriverManager.getConnection(healthUrl, healthUsername, healthPassword);
    }
}
