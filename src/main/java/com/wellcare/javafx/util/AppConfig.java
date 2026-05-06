package com.wellcare.javafx.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Centralized configuration loader.
 * Reads from config.properties (local, gitignored) or falls back to environment variables.
 */
public class AppConfig {

    private static final Properties props = new Properties();

    static {
        // 1. Try to load from local config.properties file (for development)
        try (InputStream input = new FileInputStream("config.properties")) {
            props.load(input);
            System.out.println("AppConfig: Loaded from config.properties");
        } catch (IOException ex) {
            // 2. Fallback: try classpath (packaged jar)
            try (InputStream input = AppConfig.class.getClassLoader().getResourceAsStream("config.properties")) {
                if (input != null) {
                    props.load(input);
                    System.out.println("AppConfig: Loaded from classpath");
                } else {
                    System.out.println("AppConfig: No config.properties found, using environment variables.");
                }
            } catch (IOException e2) {
                System.out.println("AppConfig: Falling back to environment variables.");
            }
        }
    }

    /**
     * Gets a config value, checking properties first, then environment variables.
     */
    public static String get(String key) {
        // Check properties file
        String value = props.getProperty(key);
        if (value != null && !value.isEmpty()) return value;

        // Fallback to environment variable (e.g. google.client.id -> GOOGLE_CLIENT_ID)
        String envKey = key.toUpperCase().replace(".", "_");
        value = System.getenv(envKey);
        if (value != null && !value.isEmpty()) return value;

        System.err.println("AppConfig: Missing configuration key: " + key);
        return "";
    }

    public static String getGoogleClientId()     { return get("google.client.id"); }
    public static String getGoogleClientSecret() { return get("google.client.secret"); }
    public static String getSendGridApiKey()      { return get("sendgrid.api.key"); }
    public static String getSendGridFromEmail()   { return get("sendgrid.from.email"); }
    public static String getOcrApiKey()           { return get("ocr.api.key"); }
}
