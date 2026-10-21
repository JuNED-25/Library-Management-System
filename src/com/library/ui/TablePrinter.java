package com.library.ui;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility to format and render visually appealing ASCII tables in console.
 */
public class TablePrinter {
    public static final String ANSI_RESET  = "\u001B[0m";
    public static final String ANSI_CYAN   = "\u001B[36m";
    public static final String ANSI_GREEN  = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_RED    = "\u001B[31m";
    public static final String ANSI_BLUE   = "\u001B[34m";
    public static final String ANSI_BOLD   = "\u001B[1m";
    public static final String ANSI_DIM    = "\u001B[2m";

    private final List<String> headers = new ArrayList<>();
    private final List<List<String>> rows = new ArrayList<>();
    private final List<Integer> colWidths = new ArrayList<>();

    public TablePrinter(String... columnHeaders) {
        for (String h : columnHeaders) {
            headers.add(h);
            colWidths.add(h.length());
        }
    }

    public void addRow(String... rowValues) {
        List<String> row = new ArrayList<>();
        for (int i = 0; i < headers.size(); i++) {
            String val = (i < rowValues.length && rowValues[i] != null) ? rowValues[i] : "";
            row.add(val);
            if (val.length() > colWidths.get(i)) {
                colWidths.set(i, Math.min(val.length(), 40)); // Max cap for column width
            }
        }
        rows.add(row);
    }

    public void print() {
        if (rows.isEmpty()) {
            System.out.println("  " + ANSI_YELLOW + "(No records found to display)" + ANSI_RESET);
            return;
        }

        // Horizontal separator
        String border = buildBorder();
        System.out.println("  " + border);

        // Header Row
        StringBuilder headerLine = new StringBuilder("  |");
        for (int i = 0; i < headers.size(); i++) {
            headerLine.append(" ")
                      .append(ANSI_BOLD).append(ANSI_CYAN)
                      .append(padRight(headers.get(i), colWidths.get(i)))
                      .append(ANSI_RESET)
                      .append(" |");
        }
        System.out.println(headerLine);
        System.out.println("  " + border);

        // Data Rows
        for (List<String> row : rows) {
            StringBuilder rowLine = new StringBuilder("  |");
            for (int i = 0; i < headers.size(); i++) {
                String val = row.get(i);
                // Color formatting for status keywords
                String coloredVal = formatStatusColor(val);
                String padded = padRight(truncate(val, colWidths.get(i)), colWidths.get(i));
                if (!coloredVal.equals(val)) {
                    padded = padded.replace(val, coloredVal);
                }
                rowLine.append(" ").append(padded).append(" |");
            }
            System.out.println(rowLine);
        }

        System.out.println("  " + border);
        System.out.println("  " + ANSI_DIM + "Total Records: " + rows.size() + ANSI_RESET);
    }

    private String buildBorder() {
        StringBuilder sb = new StringBuilder("+");
        for (int w : colWidths) {
            sb.append("-".repeat(w + 2)).append("+");
        }
        return sb.toString();
    }

    private String padRight(String s, int n) {
        if (s == null) s = "";
        if (s.length() >= n) return s;
        return s + " ".repeat(n - s.length());
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        if (s.length() <= max) return s;
        return s.substring(0, max - 3) + "...";
    }

    private String formatStatusColor(String val) {
        if ("ACTIVE".equalsIgnoreCase(val) || "AVAILABLE".equalsIgnoreCase(val) || "RETURNED".equalsIgnoreCase(val)) {
            return ANSI_GREEN + val + ANSI_RESET;
        }
        if ("ISSUED".equalsIgnoreCase(val) || "BORROWED".equalsIgnoreCase(val)) {
            return ANSI_YELLOW + val + ANSI_RESET;
        }
        if ("OVERDUE".equalsIgnoreCase(val) || "SUSPENDED".equalsIgnoreCase(val)) {
            return ANSI_RED + val + ANSI_RESET;
        }
        return val;
    }
}
