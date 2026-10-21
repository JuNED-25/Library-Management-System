package com.library.service;

import com.library.config.DatabaseConnection;
import com.library.dao.BookDAO;
import com.library.dao.LoanDAO;
import com.library.dao.MemberDAO;
import com.library.model.Book;
import com.library.model.Loan;
import com.library.model.Member;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

/**
 * Service Layer coordinating DAOs, business rules, and ACID transactions.
 */
public class LibraryService {
    private final BookDAO bookDAO = new BookDAO();
    private final MemberDAO memberDAO = new MemberDAO();
    private final LoanDAO loanDAO = new LoanDAO();

    // =========================================================================
    // Book Operations
    // =========================================================================

    public int registerBook(Book book) throws SQLException {
        if (book.getTotalCopies() <= 0) {
            throw new IllegalArgumentException("Total copies must be at least 1.");
        }
        Book existing = bookDAO.findByIsbn(book.getIsbn());
        if (existing != null) {
            throw new IllegalArgumentException("A book with ISBN '" + book.getIsbn() + "' already exists (ID: " + existing.getId() + ").");
        }
        return bookDAO.create(book);
    }

    public List<Book> getAllBooks() throws SQLException {
        return bookDAO.findAll();
    }

    public Book getBookById(int id) throws SQLException {
        return bookDAO.findById(id);
    }

    public List<Book> searchBooks(String keyword) throws SQLException {
        return bookDAO.search(keyword);
    }

    public boolean updateBook(Book book) throws SQLException {
        if (book.getAvailableCopies() > book.getTotalCopies()) {
            throw new IllegalArgumentException("Available copies cannot exceed total copies.");
        }
        return bookDAO.update(book);
    }

    public boolean removeBook(int bookId) throws SQLException {
        if (bookDAO.hasActiveLoans(bookId)) {
            throw new IllegalStateException("Cannot delete book: There are active loans currently associated with this book.");
        }
        return bookDAO.delete(bookId);
    }

    // =========================================================================
    // Member Operations
    // =========================================================================

    public int registerMember(Member member) throws SQLException {
        Member existing = memberDAO.findByEmail(member.getEmail());
        if (existing != null) {
            throw new IllegalArgumentException("A member with email '" + member.getEmail() + "' is already registered (ID: " + existing.getId() + ").");
        }
        return memberDAO.create(member);
    }

    public List<Member> getAllMembers() throws SQLException {
        return memberDAO.findAll();
    }

    public Member getMemberById(int id) throws SQLException {
        return memberDAO.findById(id);
    }

    public List<Member> searchMembers(String keyword) throws SQLException {
        return memberDAO.search(keyword);
    }

    public boolean updateMember(Member member) throws SQLException {
        return memberDAO.update(member);
    }

    public boolean removeMember(int memberId) throws SQLException {
        if (memberDAO.hasActiveLoans(memberId)) {
            throw new IllegalStateException("Cannot delete member: The member still has outstanding borrowed books.");
        }
        return memberDAO.delete(memberId);
    }

    // =========================================================================
    // Circulation / Transaction Operations (ACID Protected)
    // =========================================================================

    /**
     * Issues a book to a member using an atomic JDBC transaction.
     */
    public Loan issueBook(int bookId, int memberId, int loanPeriodDays) throws SQLException {
        Member member = memberDAO.findById(memberId);
        if (member == null) {
            throw new IllegalArgumentException("Member ID #" + memberId + " not found.");
        }
        if (!"ACTIVE".equalsIgnoreCase(member.getStatus())) {
            throw new IllegalStateException("Member account is '" + member.getStatus() + "'. Books cannot be loaned to inactive members.");
        }

        Book book = bookDAO.findById(bookId);
        if (book == null) {
            throw new IllegalArgumentException("Book ID #" + bookId + " not found.");
        }
        if (book.getAvailableCopies() <= 0) {
            throw new IllegalStateException("Book '" + book.getTitle() + "' currently has 0 available copies.");
        }

        LocalDate today = LocalDate.now();
        LocalDate due = today.plusDays(loanPeriodDays);

        Loan loan = new Loan(bookId, memberId, Date.valueOf(today), Date.valueOf(due));

        // Execute as transactional unit
        Connection conn = DatabaseConnection.getConnection();
        boolean originalAutoCommit = conn.getAutoCommit();
        try {
            conn.setAutoCommit(false);

            // 1. Decrement available copies
            boolean decremented = bookDAO.adjustAvailableCopies(bookId, -1, conn);
            if (!decremented) {
                throw new SQLException("Failed to reserve book copy. It may have been checked out simultaneously.");
            }

            // 2. Insert loan record
            int loanId = loanDAO.create(loan, conn);
            if (loanId <= 0) {
                throw new SQLException("Failed to create loan record.");
            }

            conn.commit();
            loan.setId(loanId);
            loan.setBookTitle(book.getTitle());
            loan.setMemberName(member.getFullName());
            return loan;

        } catch (SQLException ex) {
            conn.rollback();
            throw ex;
        } finally {
            conn.setAutoCommit(originalAutoCommit);
        }
    }

    /**
     * Completes book return and calculates overdue fines using an atomic JDBC transaction.
     */
    public ReturnResult returnBook(int loanId) throws SQLException {
        Loan loan = loanDAO.findById(loanId);
        if (loan == null) {
            throw new IllegalArgumentException("Loan ID #" + loanId + " not found.");
        }
        if ("RETURNED".equalsIgnoreCase(loan.getStatus())) {
            throw new IllegalStateException("Loan #" + loanId + " was already marked as RETURNED on " + loan.getReturnDate());
        }

        LocalDate returnDate = LocalDate.now();
        LocalDate dueDate = loan.getDueDate().toLocalDate();

        long daysOverdue = ChronoUnit.DAYS.between(dueDate, returnDate);
        double fine = 0.0;
        if (daysOverdue > 0) {
            fine = daysOverdue * 1.00; // $1.00 per day overdue
        }

        Connection conn = DatabaseConnection.getConnection();
        boolean originalAutoCommit = conn.getAutoCommit();
        try {
            conn.setAutoCommit(false);

            // 1. Increment available copies
            boolean incremented = bookDAO.adjustAvailableCopies(loan.getBookId(), +1, conn);
            if (!incremented) {
                throw new SQLException("Failed to restore available book inventory.");
            }

            // 2. Update loan record
            boolean updated = loanDAO.completeReturn(loanId, Date.valueOf(returnDate), fine, conn);
            if (!updated) {
                throw new SQLException("Failed to update loan return status.");
            }

            conn.commit();
            return new ReturnResult(loan, daysOverdue > 0 ? (int) daysOverdue : 0, fine);

        } catch (SQLException ex) {
            conn.rollback();
            throw ex;
        } finally {
            conn.setAutoCommit(originalAutoCommit);
        }
    }

    public boolean renewBookLoan(int loanId, int additionalDays) throws SQLException {
        Loan loan = loanDAO.findById(loanId);
        if (loan == null) {
            throw new IllegalArgumentException("Loan ID #" + loanId + " not found.");
        }
        if ("RETURNED".equalsIgnoreCase(loan.getStatus())) {
            throw new IllegalStateException("Cannot renew a book that has already been returned.");
        }
        LocalDate currentDue = loan.getDueDate().toLocalDate();
        LocalDate newDue = currentDue.plusDays(additionalDays);
        return loanDAO.renewLoan(loanId, Date.valueOf(newDue));
    }

    public List<Loan> getAllLoans() throws SQLException {
        return loanDAO.findAll();
    }

    public List<Loan> getActiveLoans() throws SQLException {
        return loanDAO.findActiveLoans();
    }

    public List<Loan> getOverdueLoans() throws SQLException {
        loanDAO.refreshOverdueStatuses();
        return loanDAO.findOverdueLoans();
    }

    public List<Loan> getLoansByMember(int memberId) throws SQLException {
        return loanDAO.findByMemberId(memberId);
    }

    public Map<String, Object> getSystemStatistics() throws SQLException {
        loanDAO.refreshOverdueStatuses();
        return loanDAO.getDashboardStats();
    }

    // Helper Result Object for Returns
    public static class ReturnResult {
        public final Loan loan;
        public final int overdueDays;
        public final double fineAmount;

        public ReturnResult(Loan loan, int overdueDays, double fineAmount) {
            this.loan = loan;
            this.overdueDays = overdueDays;
            this.fineAmount = fineAmount;
        }
    }
}
