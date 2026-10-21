package com.library;

import com.library.config.DatabaseConnection;
import com.library.ui.ConsoleUI;

/**
 * Application Entry Point for BookHaven Library Management System.
 */
public class Main {
    public static void main(String[] args) {
        // Register shutdown hook for resource cleanup
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            DatabaseConnection.closeConnection();
        }));

        ConsoleUI ui = new ConsoleUI();
        ui.start();
    }
}
