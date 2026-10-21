package com.library.dao;

import com.library.config.DatabaseConnection;
import com.library.model.Member;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for 'members' table using JDBC PreparedStatements.
 */
public class MemberDAO {

    public int create(Member member) throws SQLException {
        String sql = "INSERT INTO members (full_name, email, phone, status, registered_at) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, member.getFullName());
            ps.setString(2, member.getEmail());
            ps.setString(3, member.getPhone());
            ps.setString(4, member.getStatus());
            ps.setDate(5, member.getRegisteredAt());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        member.setId(id);
                        return id;
                    }
                }
            }
            return -1;
        }
    }

    public Member findById(int id) throws SQLException {
        String sql = "SELECT * FROM members WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToMember(rs);
                }
            }
        }
        return null;
    }

    public Member findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM members WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToMember(rs);
                }
            }
        }
        return null;
    }

    public List<Member> findAll() throws SQLException {
        List<Member> list = new ArrayList<>();
        String sql = "SELECT * FROM members ORDER BY id ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToMember(rs));
            }
        }
        return list;
    }

    public List<Member> search(String keyword) throws SQLException {
        List<Member> list = new ArrayList<>();
        String sql = "SELECT * FROM members WHERE full_name LIKE ? OR email LIKE ? OR phone LIKE ? ORDER BY id ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String pattern = "%" + keyword.trim() + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToMember(rs));
                }
            }
        }
        return list;
    }

    public boolean update(Member member) throws SQLException {
        String sql = "UPDATE members SET full_name = ?, email = ?, phone = ?, status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, member.getFullName());
            ps.setString(2, member.getEmail());
            ps.setString(3, member.getPhone());
            ps.setString(4, member.getStatus());
            ps.setInt(5, member.getId());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean hasActiveLoans(int memberId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM loans WHERE member_id = ? AND status IN ('ISSUED', 'OVERDUE')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, memberId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM members WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Member mapResultSetToMember(ResultSet rs) throws SQLException {
        return new Member(
            rs.getInt("id"),
            rs.getString("full_name"),
            rs.getString("email"),
            rs.getString("phone"),
            rs.getString("status"),
            rs.getDate("registered_at"),
            rs.getTimestamp("created_at")
        );
    }
}
