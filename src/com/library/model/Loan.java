package com.library.model;

import java.sql.Date;
import java.sql.Timestamp;

/**
 * Represents a book borrowing transaction (Loan).
 */
public class Loan {
    private int id;
    private int bookId;
    private int memberId;
    private Date borrowDate;
    private Date dueDate;
    private Date returnDate;
    private String status; // "ISSUED", "RETURNED", "OVERDUE"
    private double fineAmount;
    private Timestamp createdAt;

    // Joined descriptive fields for display
    private String bookTitle;
    private String memberName;
    private String memberEmail;

    public Loan() {}

    public Loan(int bookId, int memberId, Date borrowDate, Date dueDate) {
        this.bookId = bookId;
        this.memberId = memberId;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.status = "ISSUED";
        this.fineAmount = 0.0;
    }

    public Loan(int id, int bookId, int memberId, Date borrowDate, Date dueDate, Date returnDate, String status, double fineAmount, Timestamp createdAt) {
        this.id = id;
        this.bookId = bookId;
        this.memberId = memberId;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.status = status;
        this.fineAmount = fineAmount;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getBookId() { return bookId; }
    public void setBookId(int bookId) { this.bookId = bookId; }

    public int getMemberId() { return memberId; }
    public void setMemberId(int memberId) { this.memberId = memberId; }

    public Date getBorrowDate() { return borrowDate; }
    public void setBorrowDate(Date borrowDate) { this.borrowDate = borrowDate; }

    public Date getDueDate() { return dueDate; }
    public void setDueDate(Date dueDate) { this.dueDate = dueDate; }

    public Date getReturnDate() { return returnDate; }
    public void setReturnDate(Date returnDate) { this.returnDate = returnDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getFineAmount() { return fineAmount; }
    public void setFineAmount(double fineAmount) { this.fineAmount = fineAmount; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }

    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }

    public String getMemberEmail() { return memberEmail; }
    public void setMemberEmail(String memberEmail) { this.memberEmail = memberEmail; }

    @Override
    public String toString() {
        return String.format("Loan[ID=%d, BookId=%d ('%s'), MemberId=%d ('%s'), Borrowed=%s, Due=%s, Returned=%s, Status='%s', Fine=$%.2f]",
                id, bookId, bookTitle != null ? bookTitle : "", memberId, memberName != null ? memberName : "",
                borrowDate, dueDate, returnDate != null ? returnDate : "N/A", status, fineAmount);
    }
}
