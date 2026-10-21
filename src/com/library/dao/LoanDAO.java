package com.library.dao;

import com.library.config.DatabaseConnection;
import com.library.model.Loan;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object for 'loans' table handling circulation, returns, and stats.
 */
public class LoanDAO {

    public int create(Loan loan, Connection conn) throws SQLException {
        String sql = "INSERT INTO loans (book_id, member_id, borrow_date, due_date, status, fine_amount) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, loan.getBookId());
            ps.setInt(2, loan.getMemberId());
            ps.setDate(3, loan.getBorrowDate());
            ps.setDate(4, loan.getDueDate());
            ps.setString(5, loan.getStatus());
            ps.setDouble(6, loan.getFineAmount());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        loan.setId(id);
                        return id;
                    }
                }
            }
            return -1;
        }
    }

    public Loan findById(int id) throws SQLException {
        String sql = "SELECT l.*, b.title AS book_title, m.full_name AS member_name, m.email AS member_email " +
                     "FROM loans l " +
                     "JOIN books b ON l.book_id = b.id " +
                     "JOIN members m ON l.member_id = m.id " +
                     "WHERE l.id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToLoan(rs);
                }
            }
        }
        return null;
    }

    public List<Loan> findAll() throws SQLException {
        List<Loan> list = new ArrayList<>();
        String sql = "SELECT l.*, b.title AS book_title, m.full_name AS member_name, m.email AS member_email " +
                     "FROM loans l " +
                     "JOIN books b ON l.book_id = b.id " +
                     "JOIN members m ON l.member_id = m.id " +
                     "ORDER BY l.id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToLoan(rs));
            }
        }
        return list;
    }

    public List<Loan> findActiveLoans() throws SQLException {
        List<Loan> list = new ArrayList<>();
        String sql = "SELECT l.*, b.title AS book_title, m.full_name AS member_name, m.email AS member_email " +
                     "FROM loans l " +
                     "JOIN books b ON l.book_id = b.id " +
                     "JOIN members m ON l.member_id = m.id " +
                     "WHERE l.status IN ('ISSUED', 'OVERDUE') " +
                     "ORDER BY l.due_date ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToLoan(rs));
            }
        }
        return list;
    }

    public List<Loan> findOverdueLoans() throws SQLException {
        List<Loan> list = new ArrayList<>();
        String sql = "SELECT l.*, b.title AS book_title, m.full_name AS member_name, m.email AS member_email " +
                     "FROM loans l " +
                     "JOIN books b ON l.book_id = b.id " +
                     "JOIN members m ON l.member_id = m.id " +
                     "WHERE (l.status = 'OVERDUE' OR (l.status = 'ISSUED' AND l.due_date < CURRENT_DATE())) " +
                     "ORDER BY l.due_date ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToLoan(rs));
            }
        }
        return list;
    }

    public List<Loan> findByMemberId(int memberId) throws SQLException {
        List<Loan> list = new ArrayList<>();
        String sql = "SELECT l.*, b.title AS book_title, m.full_name AS member_name, m.email AS member_email " +
                     "FROM loans l " +
                     "JOIN books b ON l.book_id = b.id " +
                     "JOIN members m ON l.member_id = m.id " +
                     "WHERE l.member_id = ? " +
                     "ORDER BY l.id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, memberId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToLoan(rs));
                }
            }
        }
        return list;
    }

    public boolean completeReturn(int loanId, Date returnDate, double fine, Connection conn) throws SQLException {
        String sql = "UPDATE loans SET return_date = ?, fine_amount = ?, status = 'RETURNED' WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, returnDate);
            ps.setDouble(2, fine);
            ps.setInt(3, loanId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean renewLoan(int loanId, Date newDueDate) throws SQLException {
        String sql = "UPDATE loans SET due_date = ?, status = 'ISSUED' WHERE id = ? AND status != 'RETURNED'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, newDueDate);
            ps.setInt(2, loanId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Scans for past-due active loans, marks them as OVERDUE and calculates fine ($1.00/day).
     */
    public int refreshOverdueStatuses() throws SQLException {
        String sql = "UPDATE loans " +
                     "SET status = 'OVERDUE', " +
                     "    fine_amount = GREATEST(0.00, DATEDIFF(CURRENT_DATE(), due_date) * 1.00) " +
                     "WHERE status IN ('ISSUED', 'OVERDUE') AND due_date < CURRENT_DATE()";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            return stmt.executeUpdate(sql);
        }
    }

    public Map<String, Object> getDashboardStats() throws SQLException {
        Map<String, Object> stats = new HashMap<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS total_titles, IFNULL(SUM(total_copies), 0) AS total_inventory, IFNULL(SUM(available_copies), 0) AS available_inventory FROM books")) {
                if (rs.next()) {
                    stats.put("total_titles", rs.getInt("total_titles"));
                    stats.put("total_inventory", rs.getInt("total_inventory"));
                    stats.put("available_inventory", rs.getInt("available_inventory"));
                }
            }

            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS total_members, SUM(CASE WHEN status='ACTIVE' THEN 1 ELSE 0 END) AS active_members FROM members")) {
                if (rs.next()) {
                    stats.put("total_members", rs.getInt("total_members"));
                    stats.put("active_members", rs.getInt("active_members"));
                }
            }

            try (ResultSet rs = stmt.executeQuery("SELECT " +
                    "COUNT(*) AS total_loans, " +
                    "SUM(CASE WHEN status IN ('ISSUED', 'OVERDUE') THEN 1 ELSE 0 END) AS active_loans, " +
                    "SUM(CASE WHEN status = 'OVERDUE' OR (status = 'ISSUED' AND due_date < CURRENT_DATE()) THEN 1 ELSE 0 END) AS overdue_loans, " +
                    "IFNULL(SUM(fine_amount), 0.0) AS total_fines " +
                    "FROM loans")) {
                if (rs.next()) {
                    stats.put("total_loans", rs.getInt("total_loans"));
                    stats.put("active_loans", rs.getInt("active_loans"));
                    stats.put("overdue_loans", rs.getInt("overdue_loans"));
                    stats.put("total_fines", rs.getDouble("total_fines"));
                }
            }
        }
        return stats;
    }

    private Loan mapResultSetToLoan(ResultSet rs) throws SQLException {
        Loan loan = new Loan(
            rs.getInt("id"),
            rs.getInt("book_id"),
            rs.getInt("member_id"),
            rs.getDate("borrow_date"),
            rs.getDate("due_date"),
            rs.getDate("return_date"),
            rs.getString("status"),
            rs.getDouble("fine_amount"),
            rs.getTimestamp("created_at")
        );
        loan.setBookTitle(rs.getString("book_title"));
        loan.setMemberName(rs.getString("member_name"));
        loan.setMemberEmail(rs.getString("member_email"));
        return loan;
    }
}
