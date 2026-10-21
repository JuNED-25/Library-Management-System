package com.library.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Manages JDBC Database Connection lifecycle and schema initialization.
 */
public class DatabaseConnection {
    private static Connection connection = null;

    static {
        try {
            // Load MySQL Connector/J driver class explicitly
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("[ERROR] MySQL JDBC Driver not found in classpath: " + e.getMessage());
        }
    }

    private DatabaseConnection() {}

    /**
     * Obtains an active connection to the MySQL database.
     * Reopens a new connection if the previous one is closed or invalid.
     */
    public static synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed() || !connection.isValid(DatabaseConfig.getTimeoutSeconds())) {
            String url = DatabaseConfig.getUrl();
            String user = DatabaseConfig.getUser();
            String pass = DatabaseConfig.getPassword();
            connection = DriverManager.getConnection(url, user, pass);
        }
        return connection;
    }

    /**
     * Tests the database connection with current configuration.
     */
    public static boolean testConnection() {
        try (Connection conn = DriverManager.getConnection(
                DatabaseConfig.getUrl(),
                DatabaseConfig.getUser(),
                DatabaseConfig.getPassword())) {
            return conn.isValid(DatabaseConfig.getTimeoutSeconds());
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Initializes database tables and seed data if they do not exist yet.
     */
    public static boolean initializeDatabaseAndTables() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Check if 'books' table exists
            boolean booksExist = false;
            try (ResultSet rs = stmt.executeQuery("SHOW TABLES LIKE 'books'")) {
                if (rs.next()) {
                    booksExist = true;
                }
            }

            if (booksExist) {
                return true; // Already initialized
            }

            // Read and execute sql/schema.sql
            File schemaFile = new File("sql/schema.sql");
            StringBuilder sqlBuilder = new StringBuilder();

            if (schemaFile.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(schemaFile))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String trimmed = line.trim();
                        // Skip SQL comments
                        if (!trimmed.startsWith("--") && !trimmed.startsWith("/*")) {
                            sqlBuilder.append(line).append("\n");
                        }
                    }
                }
            } else {
                // Built-in fallback schema DDL if file not found
                sqlBuilder.append(getFallbackSchemaDDL());
            }

            String[] statements = sqlBuilder.toString().split(";");
            for (String sql : statements) {
                String cmd = sql.trim();
                if (!cmd.isEmpty()) {
                    try {
                        stmt.execute(cmd);
                    } catch (SQLException ex) {
                        // Ignore minor warnings or already existing indexes
                        if (!ex.getMessage().toLowerCase().contains("already exists")) {
                            System.err.println("[SCHEMA NOTICE] " + ex.getMessage());
                        }
                    }
                }
            }
            return true;
        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to initialize database tables: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("[ERROR] Schema file reading error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Closes the active database connection safely upon application exit.
     */
    public static synchronized void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException ignored) {}
    }

    private static String getFallbackSchemaDDL() {
        return "CREATE TABLE IF NOT EXISTS books (" +
               "  id INT AUTO_INCREMENT PRIMARY KEY," +
               "  title VARCHAR(200) NOT NULL," +
               "  author VARCHAR(150) NOT NULL," +
               "  isbn VARCHAR(20) NOT NULL UNIQUE," +
               "  genre VARCHAR(60) NOT NULL," +
               "  published_year INT NOT NULL," +
               "  total_copies INT NOT NULL DEFAULT 1," +
               "  available_copies INT NOT NULL DEFAULT 1," +
               "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
               ");" +
               "CREATE TABLE IF NOT EXISTS members (" +
               "  id INT AUTO_INCREMENT PRIMARY KEY," +
               "  full_name VARCHAR(120) NOT NULL," +
               "  email VARCHAR(120) NOT NULL UNIQUE," +
               "  phone VARCHAR(25) NOT NULL," +
               "  status ENUM('ACTIVE', 'SUSPENDED') NOT NULL DEFAULT 'ACTIVE'," +
               "  registered_at DATE NOT NULL," +
               "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
               ");" +
               "CREATE TABLE IF NOT EXISTS loans (" +
               "  id INT AUTO_INCREMENT PRIMARY KEY," +
               "  book_id INT NOT NULL," +
               "  member_id INT NOT NULL," +
               "  borrow_date DATE NOT NULL," +
               "  due_date DATE NOT NULL," +
               "  return_date DATE NULL," +
               "  status ENUM('ISSUED', 'RETURNED', 'OVERDUE') NOT NULL DEFAULT 'ISSUED'," +
               "  fine_amount DECIMAL(8, 2) NOT NULL DEFAULT 0.00," +
               "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
               "  FOREIGN KEY (book_id) REFERENCES books (id)," +
               "  FOREIGN KEY (member_id) REFERENCES members (id)" +
               ");";
    }
}
