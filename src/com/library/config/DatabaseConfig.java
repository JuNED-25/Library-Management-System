package com.library.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Manages database configuration loaded from 'db.properties'
 * with overrides from environment variables.
 */
public class DatabaseConfig {
    private static final String CONFIG_FILE = "db.properties";
    private static String url;
    private static String user;
    private static String password;
    private static int timeoutSeconds = 5;

    static {
        loadConfig();
    }

    public static synchronized void loadConfig() {
        Properties props = new Properties();
        File file = new File(CONFIG_FILE);

        if (file.exists()) {
            try (InputStream in = new FileInputStream(file)) {
                props.load(in);
            } catch (IOException e) {
                System.err.println("[WARN] Could not read " + CONFIG_FILE + ": " + e.getMessage());
            }
        }

        // Priority 1: Environment Variables; Priority 2: db.properties; Priority 3: Fallback Defaults
        url = System.getenv("DB_URL");
        if (url == null || url.trim().isEmpty()) {
            url = props.getProperty("db.url", "jdbc:mysql://localhost:3306/library_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
        }

        user = System.getenv("DB_USER");
        if (user == null || user.trim().isEmpty()) {
            user = props.getProperty("db.user", "root");
        }

        password = System.getenv("DB_PASSWORD");
        if (password == null) {
            password = props.getProperty("db.password", "");
        }

        String timeoutStr = props.getProperty("db.timeout.seconds", "5");
        try {
            timeoutSeconds = Integer.parseInt(timeoutStr.trim());
        } catch (NumberFormatException ignored) {
            timeoutSeconds = 5;
        }
    }

    public static synchronized void updateAndSave(String newUrl, String newUser, String newPassword) {
        if (newUrl != null) url = newUrl;
        if (newUser != null) user = newUser;
        if (newPassword != null) password = newPassword;

        Properties props = new Properties();
        props.setProperty("db.url", url);
        props.setProperty("db.user", user);
        props.setProperty("db.password", password);
        props.setProperty("db.timeout.seconds", String.valueOf(timeoutSeconds));

        try (FileOutputStream out = new FileOutputStream(CONFIG_FILE)) {
            props.store(out, "Updated via BookHaven Console Application");
        } catch (IOException e) {
            System.err.println("[WARN] Failed to save updated credentials to " + CONFIG_FILE + ": " + e.getMessage());
        }
    }

    public static String getUrl() { return url; }
    public static String getUser() { return user; }
    public static String getPassword() { return password; }
    public static int getTimeoutSeconds() { return timeoutSeconds; }

    public static void setPassword(String pwd) { password = pwd; }
    public static void setUser(String usr) { user = usr; }
}
