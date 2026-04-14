package com.wellora.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * DBConnection — JDBC Connection Utility
 *
 * Symfony equivalent: config/packages/doctrine.yaml + EntityManagerInterface
 *
 * In Symfony, Doctrine managed connections automatically via dependency injection.
 * Here we manually load db.properties and open/close connections using JDBC.
 *
 * Usage:
 *   Connection conn = DBConnection.getConnection();
 *   // ... use conn ...
 *   conn.close();
 */
public class DBConnection {

    private static String url;
    private static String username;
    private static String password;

    static {
        try (InputStream input = DBConnection.class
                .getClassLoader()
                .getResourceAsStream("db.properties")) {

            Properties props = new Properties();
            props.load(input);

            Class.forName(props.getProperty("db.driver"));
            url      = props.getProperty("db.url");
            username = props.getProperty("db.username");
            password = props.getProperty("db.password");

        } catch (Exception e) {
            throw new RuntimeException("Failed to load database configuration", e);
        }
    }

    /**
     * Returns a new JDBC Connection.
     * Caller is responsible for closing it (use try-with-resources).
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }
}
