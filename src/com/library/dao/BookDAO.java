package com.library.dao;

import com.library.config.DatabaseConnection;
import com.library.model.Book;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for 'books' table using JDBC PreparedStatements.
 */
public class BookDAO {

    public int create(Book book) throws SQLException {
        String sql = "INSERT INTO books (title, author, isbn, genre, published_year, total_copies, available_copies) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, book.getTitle());
            ps.setString(2, book.getAuthor());
            ps.setString(3, book.getIsbn());
            ps.setString(4, book.getGenre());
            ps.setInt(5, book.getPublishedYear());
            ps.setInt(6, book.getTotalCopies());
            ps.setInt(7, book.getAvailableCopies());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        book.setId(id);
                        return id;
                    }
                }
            }
            return -1;
        }
    }

    public Book findById(int id) throws SQLException {
        String sql = "SELECT * FROM books WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToBook(rs);
                }
            }
        }
        return null;
    }

    public Book findByIsbn(String isbn) throws SQLException {
        String sql = "SELECT * FROM books WHERE isbn = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, isbn.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToBook(rs);
                }
            }
        }
        return null;
    }

    public List<Book> findAll() throws SQLException {
        List<Book> list = new ArrayList<>();
        String sql = "SELECT * FROM books ORDER BY id ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToBook(rs));
            }
        }
        return list;
    }

    public List<Book> search(String keyword) throws SQLException {
        List<Book> list = new ArrayList<>();
        String sql = "SELECT * FROM books WHERE title LIKE ? OR author LIKE ? OR genre LIKE ? OR isbn LIKE ? ORDER BY id ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String pattern = "%" + keyword.trim() + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ps.setString(4, pattern);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToBook(rs));
                }
            }
        }
        return list;
    }

    public boolean update(Book book) throws SQLException {
        String sql = "UPDATE books SET title = ?, author = ?, isbn = ?, genre = ?, published_year = ?, " +
                     "total_copies = ?, available_copies = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, book.getTitle());
            ps.setString(2, book.getAuthor());
            ps.setString(3, book.getIsbn());
            ps.setString(4, book.getGenre());
            ps.setInt(5, book.getPublishedYear());
            ps.setInt(6, book.getTotalCopies());
            ps.setInt(7, book.getAvailableCopies());
            ps.setInt(8, book.getId());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean adjustAvailableCopies(int bookId, int delta, Connection externalConn) throws SQLException {
        String sql = "UPDATE books SET available_copies = available_copies + ? WHERE id = ? AND (available_copies + ?) >= 0";
        PreparedStatement ps = externalConn.prepareStatement(sql);
        try {
            ps.setInt(1, delta);
            ps.setInt(2, bookId);
            ps.setInt(3, delta);
            return ps.executeUpdate() > 0;
        } finally {
            ps.close();
        }
    }

    public boolean hasActiveLoans(int bookId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM loans WHERE book_id = ? AND status IN ('ISSUED', 'OVERDUE')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM books WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Book mapResultSetToBook(ResultSet rs) throws SQLException {
        return new Book(
            rs.getInt("id"),
            rs.getString("title"),
            rs.getString("author"),
            rs.getString("isbn"),
            rs.getString("genre"),
            rs.getInt("published_year"),
            rs.getInt("total_copies"),
            rs.getInt("available_copies"),
            rs.getTimestamp("created_at")
        );
    }
}
