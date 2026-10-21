package com.library.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Utility class for safe and validated console user input.
 */
public class InputValidator {
    private static final Scanner scanner = new Scanner(System.in);
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static String readNonEmptyString(String prompt) {
        while (true) {
            System.out.print(prompt);
            if (!scanner.hasNextLine()) {
                System.out.println("\n [!] Input stream ended. Exiting.");
                System.exit(0);
            }
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) {
                return input;
            }
            System.out.println(" \u001B[31m[!] Input cannot be empty. Please try again.\u001B[0m");
        }
    }

    public static String readOptionalString(String prompt, String defaultValue) {
        System.out.print(prompt + (defaultValue != null && !defaultValue.isEmpty() ? " [" + defaultValue + "]: " : ": "));
        if (!scanner.hasNextLine()) {
            return defaultValue != null ? defaultValue : "";
        }
        String input = scanner.nextLine().trim();
        if (input.isEmpty() && defaultValue != null) {
            return defaultValue;
        }
        return input;
    }

    public static int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            if (!scanner.hasNextLine()) {
                System.out.println("\n [!] Input stream ended. Exiting.");
                System.exit(0);
            }
            String input = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(input);
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.printf(" \u001B[31m[!] Please enter a number between %d and %d.\u001B[0m\n", min, max);
            } catch (NumberFormatException e) {
                System.out.println(" \u001B[31m[!] Invalid numeric format. Please enter a valid integer.\u001B[0m");
            }
        }
    }

    public static int readOptionalInt(String prompt, int defaultValue, int min, int max) {
        while (true) {
            System.out.print(prompt + " [" + defaultValue + "]: ");
            if (!scanner.hasNextLine()) {
                return defaultValue;
            }
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return defaultValue;
            }
            try {
                int value = Integer.parseInt(input);
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.printf(" \u001B[31m[!] Please enter a number between %d and %d.\u001B[0m\n", min, max);
            } catch (NumberFormatException e) {
                System.out.println(" \u001B[31m[!] Invalid numeric format. Please enter a valid integer.\u001B[0m");
            }
        }
    }

    public static String readValidEmail(String prompt) {
        while (true) {
            System.out.print(prompt);
            if (!scanner.hasNextLine()) {
                System.out.println("\n [!] Input stream ended. Exiting.");
                System.exit(0);
            }
            String email = scanner.nextLine().trim();
            if (EMAIL_PATTERN.matcher(email).matches()) {
                return email;
            }
            System.out.println(" \u001B[31m[!] Invalid email format (example: user@domain.com). Try again.\u001B[0m");
        }
    }

    public static String readOptionalEmail(String prompt, String defaultEmail) {
        while (true) {
            System.out.print(prompt + " [" + defaultEmail + "]: ");
            if (!scanner.hasNextLine()) {
                return defaultEmail;
            }
            String email = scanner.nextLine().trim();
            if (email.isEmpty()) {
                return defaultEmail;
            }
            if (EMAIL_PATTERN.matcher(email).matches()) {
                return email;
            }
            System.out.println(" \u001B[31m[!] Invalid email format. Try again.\u001B[0m");
        }
    }

    public static String readPhoneNumber(String prompt) {
        while (true) {
            System.out.print(prompt);
            if (!scanner.hasNextLine()) {
                System.out.println("\n [!] Input stream ended. Exiting.");
                System.exit(0);
            }
            String phone = scanner.nextLine().trim();
            if (phone.matches("^[+0-9\\s-]{7,20}$")) {
                return phone;
            }
            System.out.println(" \u001B[31m[!] Invalid phone number. Please enter at least 7 digits.\u001B[0m");
        }
    }

    public static LocalDate readDate(String prompt) {
        while (true) {
            System.out.print(prompt + " (YYYY-MM-DD): ");
            if (!scanner.hasNextLine()) {
                return LocalDate.now();
            }
            String input = scanner.nextLine().trim();
            try {
                return LocalDate.parse(input, DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                System.out.println(" \u001B[31m[!] Invalid date format. Use YYYY-MM-DD (e.g. 2026-10-21).\u001B[0m");
            }
        }
    }

    public static boolean readConfirmation(String prompt) {
        while (true) {
            System.out.print(prompt + " (y/n): ");
            if (!scanner.hasNextLine()) {
                return false;
            }
            String input = scanner.nextLine().trim().toLowerCase();
            if (input.equals("y") || input.equals("yes")) return true;
            if (input.equals("n") || input.equals("no")) return false;
            System.out.println(" \u001B[31m[!] Please answer 'y' or 'n'.\u001B[0m");
        }
    }

    public static void pauseForEnter() {
        System.out.print("\n Press [ENTER] to continue...");
        if (scanner.hasNextLine()) {
            scanner.nextLine();
        }
    }
}
