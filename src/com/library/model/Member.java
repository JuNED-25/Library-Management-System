package com.library.model;

import java.sql.Date;
import java.sql.Timestamp;

/**
 * Represents a registered library patron / member.
 */
public class Member {
    private int id;
    private String fullName;
    private String email;
    private String phone;
    private String status; // "ACTIVE" or "SUSPENDED"
    private Date registeredAt;
    private Timestamp createdAt;

    public Member() {}

    public Member(String fullName, String email, String phone, String status, Date registeredAt) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.status = status;
        this.registeredAt = registeredAt;
    }

    public Member(int id, String fullName, String email, String phone, String status, Date registeredAt, Timestamp createdAt) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.status = status;
        this.registeredAt = registeredAt;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getRegisteredAt() { return registeredAt; }
    public void setRegisteredAt(Date registeredAt) { this.registeredAt = registeredAt; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return String.format("Member[ID=%d, Name='%s', Email='%s', Phone='%s', Status='%s', Registered=%s]",
                id, fullName, email, phone, status, registeredAt);
    }
}
