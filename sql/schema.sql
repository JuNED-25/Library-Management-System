-- =====================================================================
-- BookHaven Library Management System - Database Schema & Seed Data
-- Database Engine: MySQL 8.0+
-- =====================================================================

CREATE DATABASE IF NOT EXISTS library_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE library_db;

-- ---------------------------------------------------------------------
-- Table 1: books
-- Stores library catalog information and inventory count
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS books (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    author VARCHAR(150) NOT NULL,
    isbn VARCHAR(20) NOT NULL UNIQUE,
    genre VARCHAR(60) NOT NULL,
    published_year INT NOT NULL,
    total_copies INT NOT NULL DEFAULT 1,
    available_copies INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    KEY idx_books_title (title),
    KEY idx_books_author (author),
    KEY idx_books_genre (genre),
    CONSTRAINT chk_positive_copies CHECK (total_copies >= 0 AND available_copies >= 0 AND available_copies <= total_copies)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Table 2: members
-- Stores registered library patrons
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS members (
    id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(120) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    phone VARCHAR(25) NOT NULL,
    status ENUM('ACTIVE', 'SUSPENDED') NOT NULL DEFAULT 'ACTIVE',
    registered_at DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Table 3: loans
-- Tracks book borrowing transactions, due dates, returns, and fines
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS loans (
    id INT AUTO_INCREMENT PRIMARY KEY,
    book_id INT NOT NULL,
    member_id INT NOT NULL,
    borrow_date DATE NOT NULL,
    due_date DATE NOT NULL,
    return_date DATE NULL,
    status ENUM('ISSUED', 'RETURNED', 'OVERDUE') NOT NULL DEFAULT 'ISSUED',
    fine_amount DECIMAL(8, 2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    KEY idx_loans_status (status),
    KEY idx_loans_due_date (due_date),
    CONSTRAINT fk_loans_book FOREIGN KEY (book_id)
        REFERENCES books (id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT fk_loans_member FOREIGN KEY (member_id)
        REFERENCES members (id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Seed Initial Sample Data (if tables are empty)
-- ---------------------------------------------------------------------
INSERT IGNORE INTO books (id, title, author, isbn, genre, published_year, total_copies, available_copies) VALUES
(1, 'Clean Code: A Handbook of Agile Software Craftsmanship', 'Robert C. Martin', '978-0132350884', 'Software Engineering', 2008, 5, 4),
(2, 'The Pragmatic Programmer: Your Journey to Mastery', 'David Thomas, Andrew Hunt', '978-0135957059', 'Computer Science', 2019, 4, 3),
(3, 'Design Patterns: Elements of Reusable Object-Oriented Software', 'Erich Gamma et al.', '978-0201633610', 'Software Architecture', 1994, 3, 3),
(4, 'Effective Java (3rd Edition)', 'Joshua Bloch', '978-0134685991', 'Programming Languages', 2018, 5, 5),
(5, 'Introduction to Algorithms (4th Edition)', 'Thomas H. Cormen', '978-0262046305', 'Algorithms', 2022, 3, 2),
(6, 'To Kill a Mockingbird', 'Harper Lee', '978-0061120084', 'Classic Fiction', 1960, 6, 6),
(7, '1984', 'George Orwell', '978-0451524935', 'Dystopian Fiction', 1949, 5, 4),
(8, 'Atomic Habits', 'James Clear', '978-0735211292', 'Self-Help', 2018, 7, 7),
(9, 'Designing Data-Intensive Applications', 'Martin Kleppmann', '978-1449373320', 'Software Architecture', 2017, 5, 4),
(10, 'Clean Architecture: A Craftsman''s Guide', 'Robert C. Martin', '978-0134494166', 'Software Engineering', 2017, 4, 3),
(11, 'Grokking Algorithms', 'Aditya Bhargava', '978-1617292231', 'Algorithms', 2016, 6, 5),
(12, 'Dune', 'Frank Herbert', '978-0441172719', 'Science Fiction', 1965, 8, 7),
(13, 'Project Hail Mary', 'Andy Weir', '978-0593135204', 'Science Fiction', 2021, 6, 6),
(14, 'The Hobbit', 'J.R.R. Tolkien', '978-0547928227', 'Fantasy', 1937, 7, 5),
(15, 'Harry Potter and the Sorcerer''s Stone', 'J.K. Rowling', '978-0590353427', 'Fantasy', 1997, 10, 8),
(16, 'The Great Gatsby', 'F. Scott Fitzgerald', '978-0743273565', 'Classic Fiction', 1925, 5, 5),
(17, 'Pride and Prejudice', 'Jane Austen', '978-0141439518', 'Romance / Classic', 1813, 4, 4),
(18, 'Sapiens: A Brief History of Humankind', 'Yuval Noah Harari', '978-0062316097', 'History / Non-Fiction', 2014, 6, 5),
(19, 'Deep Work: Rules for Focused Success', 'Cal Newport', '978-1455586691', 'Productivity', 2016, 5, 4),
(20, 'The Psychology of Money', 'Morgan Housel', '978-0857197689', 'Finance / Economics', 2020, 6, 6);

INSERT IGNORE INTO members (id, full_name, email, phone, status, registered_at) VALUES
(1, 'Alice Johnson', 'alice.johnson@example.com', '+1-555-0101', 'ACTIVE', '2026-01-15'),
(2, 'Bob Smith', 'bob.smith@example.com', '+1-555-0102', 'ACTIVE', '2026-02-10'),
(3, 'Charlie Davis', 'charlie.davis@example.com', '+1-555-0103', 'ACTIVE', '2026-03-01'),
(4, 'Diana Prince', 'diana.prince@example.com', '+1-555-0104', 'ACTIVE', '2026-03-20'),
(5, 'Evan Wright', 'evan.wright@example.com', '+1-555-0105', 'ACTIVE', '2026-04-12'),
(6, 'Fiona Gallagher', 'fiona.g@example.com', '+1-555-0106', 'ACTIVE', '2026-05-01'),
(7, 'George Martin', 'george.rr@example.com', '+1-555-0107', 'ACTIVE', '2026-06-18'),
(8, 'Hannah Abbott', 'hannah.a@example.com', '+1-555-0108', 'ACTIVE', '2026-07-22'),
(9, 'Ian Malcolm', 'ian.malcolm@example.com', '+1-555-0109', 'SUSPENDED', '2026-08-05'),
(10, 'Julia Roberts', 'julia.r@example.com', '+1-555-0110', 'ACTIVE', '2026-09-14');

INSERT IGNORE INTO loans (id, book_id, member_id, borrow_date, due_date, return_date, status, fine_amount) VALUES
(1, 1, 1, '2026-10-01', '2026-10-15', NULL, 'OVERDUE', 6.00),
(2, 2, 2, '2026-10-10', '2026-10-24', NULL, 'ISSUED', 0.00),
(3, 5, 3, '2026-10-08', '2026-10-22', NULL, 'ISSUED', 0.00),
(4, 7, 4, '2026-09-15', '2026-09-29', '2026-09-28', 'RETURNED', 0.00),
(5, 9, 5, '2026-10-12', '2026-10-26', NULL, 'ISSUED', 0.00),
(6, 10, 6, '2026-10-15', '2026-10-29', NULL, 'ISSUED', 0.00),
(7, 11, 7, '2026-09-20', '2026-10-04', NULL, 'OVERDUE', 17.00),
(8, 12, 8, '2026-10-18', '2026-11-01', NULL, 'ISSUED', 0.00),
(9, 14, 5, '2026-09-10', '2026-09-24', '2026-09-23', 'RETURNED', 0.00),
(10, 15, 10, '2026-10-05', '2026-10-19', NULL, 'OVERDUE', 2.00);
