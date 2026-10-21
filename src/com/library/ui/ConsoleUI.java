package com.library.ui;

import com.library.config.DatabaseConfig;
import com.library.config.DatabaseConnection;
import com.library.model.Book;
import com.library.model.Loan;
import com.library.model.Member;
import com.library.service.LibraryService;
import com.library.util.InputValidator;

import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Interactive Console User Interface with ANSI styling and guided workflows.
 */
public class ConsoleUI {
    private final LibraryService service = new LibraryService();

    // ANSI Color Constants
    public static final String RESET  = "\u001B[0m";
    public static final String BOLD   = "\u001B[1m";
    public static final String CYAN   = "\u001B[36m";
    public static final String GREEN  = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String RED    = "\u001B[31m";
    public static final String BLUE   = "\u001B[34m";
    public static final String PURPLE = "\u001B[35m";
    public static final String WHITE  = "\u001B[37m";

    public void start() {
        printBanner();
        ensureDatabaseReady();

        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = InputValidator.readInt(" Select an option [1-6]: ", 1, 6);
            System.out.println();

            switch (choice) {
                case 1:
                    handleBookMenu();
                    break;
                case 2:
                    handleMemberMenu();
                    break;
                case 3:
                    handleCirculationMenu();
                    break;
                case 4:
                    handleDashboard();
                    break;
                case 5:
                    handleSettingsMenu();
                    break;
                case 6:
                    running = false;
                    printExitMessage();
                    break;
            }
        }
    }

    private void printBanner() {
        System.out.println(CYAN + BOLD);
        System.out.println(" ===========================================================================");
        System.out.println("  ____              _     _   _                            ");
        System.out.println(" | __ )  ___   ___ | | __| | | | __ ___   _____ _ __       ");
        System.out.println(" |  _ \\ / _ \\ / _ \\| |/ /| |_| |/ _` \\ \\ / / _ \\ '_ \\      ");
        System.out.println(" | |_) | (_) | (_) |   < |  _  | (_| |\\ V /  __/ | | |     ");
        System.out.println(" |____/ \\___/ \\___/|_|\\_\\|_| |_|\\__,_| \\_/ \\___|_| |_|     ");
        System.out.println("            LIBRARY MANAGEMENT CONSOLE SYSTEM              ");
        System.out.println(" ===========================================================================" + RESET);
        System.out.println(WHITE + "  Modern Java + JDBC + MySQL Edition | High-Performance PreparedStatements" + RESET);
        System.out.println();
    }

    private void printMainMenu() {
        System.out.println(CYAN + "+-------------------------------------------------------------------------+" + RESET);
        System.out.println(CYAN + "|                               MAIN MENU                                 |" + RESET);
        System.out.println(CYAN + "+-------------------------------------------------------------------------+" + RESET);
        System.out.println("  [" + BOLD + YELLOW + "1" + RESET + "] 📚 Book Catalog Management   (Add, View, Search, Update, Delete)");
        System.out.println("  [" + BOLD + YELLOW + "2" + RESET + "] 👥 Member Management         (Register, List, Search, Edit, Delete)");
        System.out.println("  [" + BOLD + YELLOW + "3" + RESET + "] 🔄 Circulation & Loans       (Borrow, Return, Renew, Overdue Tracker)");
        System.out.println("  [" + BOLD + YELLOW + "4" + RESET + "] 📊 Analytics & Dashboard     (Real-time Inventory & Financial Stats)");
        System.out.println("  [" + BOLD + YELLOW + "5" + RESET + "] ⚙️  Database & Settings       (Test Connection, Configure Credentials)");
        System.out.println("  [" + BOLD + YELLOW + "6" + RESET + "] 🚪 Exit Application");
        System.out.println(CYAN + "+-------------------------------------------------------------------------+" + RESET);
    }

    private void ensureDatabaseReady() {
        System.out.print(" [SYSTEM] Checking MySQL database connectivity... ");
        if (DatabaseConnection.testConnection()) {
            System.out.println(GREEN + "[CONNECTED]" + RESET);
            DatabaseConnection.initializeDatabaseAndTables();
        } else {
            System.out.println(RED + "[FAILED]" + RESET);
            System.out.println(YELLOW + " [!] Could not connect to MySQL with current settings." + RESET);
            promptDatabaseSetup();
        }
    }

    public void promptDatabaseSetup() {
        System.out.println("\n" + CYAN + "=== MySQL Connection Configuration ===" + RESET);
        System.out.println(" Current URL  : " + DatabaseConfig.getUrl());
        System.out.println(" Current User : " + DatabaseConfig.getUser());
        System.out.println(" Current Pass : " + (DatabaseConfig.getPassword().isEmpty() ? "(blank)" : "********"));
        System.out.println();

        boolean configureNow = InputValidator.readConfirmation(" Would you like to enter your MySQL credentials now?");
        if (!configureNow) {
            System.out.println(YELLOW + " Continuing with existing settings. Note that database queries will fail until MySQL is reached." + RESET);
            return;
        }

        String user = InputValidator.readOptionalString(" MySQL Username", DatabaseConfig.getUser());
        System.out.print(" MySQL Password: ");
        java.util.Scanner sc = new java.util.Scanner(System.in);
        String pass = sc.nextLine().trim();

        DatabaseConfig.updateAndSave(null, user, pass);

        System.out.print(" [TESTING] Verifying connection with new credentials... ");
        if (DatabaseConnection.testConnection()) {
            System.out.println(GREEN + "[SUCCESS]" + RESET);
            DatabaseConnection.initializeDatabaseAndTables();
            System.out.println(GREEN + " [✓] Database verified and tables initialized!" + RESET);
        } else {
            System.out.println(RED + "[FAILED]" + RESET);
            System.out.println(RED + " [!] Still unable to connect. Please verify that MySQL Server is running and password is correct." + RESET);
        }
        InputValidator.pauseForEnter();
    }

    // =========================================================================
    // 1. Books Sub-Menu
    // =========================================================================
    private void handleBookMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n" + CYAN + "------------------- 📚 BOOK MANAGEMENT -------------------" + RESET);
            System.out.println("  1. List All Books");
            System.out.println("  2. Search Books (Title, Author, Genre, ISBN)");
            System.out.println("  3. View Book Details");
            System.out.println("  4. Add New Book");
            System.out.println("  5. Update Book Details");
            System.out.println("  6. Delete Book");
            System.out.println("  7. Return to Main Menu");
            System.out.println(CYAN + "----------------------------------------------------------" + RESET);

            int choice = InputValidator.readInt(" Select an action [1-7]: ", 1, 7);
            System.out.println();

            try {
                switch (choice) {
                    case 1:
                        listAllBooks();
                        break;
                    case 2:
                        searchBooks();
                        break;
                    case 3:
                        viewBookDetails();
                        break;
                    case 4:
                        addNewBook();
                        break;
                    case 5:
                        updateBook();
                        break;
                    case 6:
                        deleteBook();
                        break;
                    case 7:
                        back = true;
                        break;
                }
            } catch (SQLException e) {
                System.out.println(RED + " [DB ERROR] " + e.getMessage() + RESET);
            } catch (Exception e) {
                System.out.println(RED + " [ERROR] " + e.getMessage() + RESET);
            }
        }
    }

    private void listAllBooks() throws SQLException {
        List<Book> books = service.getAllBooks();
        renderBookTable(books);
        InputValidator.pauseForEnter();
    }

    private void searchBooks() throws SQLException {
        String keyword = InputValidator.readNonEmptyString(" Enter search keyword: ");
        List<Book> books = service.searchBooks(keyword);
        System.out.println("\n Search Results for: '" + keyword + "'");
        renderBookTable(books);
        InputValidator.pauseForEnter();
    }

    private void renderBookTable(List<Book> books) {
        TablePrinter table = new TablePrinter("ID", "Title", "Author", "Genre", "Year", "Available", "Total", "ISBN");
        for (Book b : books) {
            table.addRow(
                String.valueOf(b.getId()),
                b.getTitle(),
                b.getAuthor(),
                b.getGenre(),
                String.valueOf(b.getPublishedYear()),
                String.valueOf(b.getAvailableCopies()),
                String.valueOf(b.getTotalCopies()),
                b.getIsbn()
            );
        }
        table.print();
    }

    private void viewBookDetails() throws SQLException {
        int id = InputValidator.readInt(" Enter Book ID: ", 1, Integer.MAX_VALUE);
        Book b = service.getBookById(id);
        if (b == null) {
            System.out.println(YELLOW + " [!] No book found with ID #" + id + RESET);
        } else {
            System.out.println(CYAN + "\n==================== BOOK DETAILS ====================" + RESET);
            System.out.printf("  ID               : %d\n", b.getId());
            System.out.printf("  Title            : %s\n", b.getTitle());
            System.out.printf("  Author           : %s\n", b.getAuthor());
            System.out.printf("  ISBN             : %s\n", b.getIsbn());
            System.out.printf("  Genre            : %s\n", b.getGenre());
            System.out.printf("  Published Year   : %d\n", b.getPublishedYear());
            System.out.printf("  Inventory        : %d Available / %d Total\n", b.getAvailableCopies(), b.getTotalCopies());
            System.out.printf("  Catalog Added At : %s\n", b.getCreatedAt());
            System.out.println(CYAN + "======================================================" + RESET);
        }
        InputValidator.pauseForEnter();
    }

    private void addNewBook() throws SQLException {
        System.out.println(CYAN + "=== Add New Book ===" + RESET);
        String title = InputValidator.readNonEmptyString(" Title: ");
        String author = InputValidator.readNonEmptyString(" Author: ");
        String isbn = InputValidator.readNonEmptyString(" ISBN: ");
        String genre = InputValidator.readNonEmptyString(" Genre (e.g. Fiction, Tech, Science): ");
        int year = InputValidator.readInt(" Publication Year: ", 1000, LocalDate.now().getYear() + 1);
        int copies = InputValidator.readInt(" Number of Copies: ", 1, 1000);

        Book book = new Book(title, author, isbn, genre, year, copies);
        int generatedId = service.registerBook(book);
        System.out.println(GREEN + " [✓] Book successfully added with ID #" + generatedId + "!" + RESET);
        InputValidator.pauseForEnter();
    }

    private void updateBook() throws SQLException {
        int id = InputValidator.readInt(" Enter Book ID to update: ", 1, Integer.MAX_VALUE);
        Book b = service.getBookById(id);
        if (b == null) {
            System.out.println(YELLOW + " [!] Book with ID #" + id + " not found." + RESET);
            InputValidator.pauseForEnter();
            return;
        }

        System.out.println(CYAN + " Updating Book: " + b.getTitle() + " (Press Enter to keep current value)" + RESET);
        String title = InputValidator.readOptionalString(" New Title", b.getTitle());
        String author = InputValidator.readOptionalString(" New Author", b.getAuthor());
        String isbn = InputValidator.readOptionalString(" New ISBN", b.getIsbn());
        String genre = InputValidator.readOptionalString(" New Genre", b.getGenre());
        int year = InputValidator.readOptionalInt(" New Publication Year", b.getPublishedYear(), 1000, LocalDate.now().getYear() + 1);
        int total = InputValidator.readOptionalInt(" New Total Copies", b.getTotalCopies(), 1, 1000);
        int available = InputValidator.readOptionalInt(" New Available Copies", b.getAvailableCopies(), 0, total);

        b.setTitle(title);
        b.setAuthor(author);
        b.setIsbn(isbn);
        b.setGenre(genre);
        b.setPublishedYear(year);
        b.setTotalCopies(total);
        b.setAvailableCopies(available);

        boolean updated = service.updateBook(b);
        if (updated) {
            System.out.println(GREEN + " [✓] Book details updated successfully!" + RESET);
        } else {
            System.out.println(RED + " [!] Failed to update book." + RESET);
        }
        InputValidator.pauseForEnter();
    }

    private void deleteBook() throws SQLException {
        int id = InputValidator.readInt(" Enter Book ID to delete: ", 1, Integer.MAX_VALUE);
        Book b = service.getBookById(id);
        if (b == null) {
            System.out.println(YELLOW + " [!] Book with ID #" + id + " not found." + RESET);
            InputValidator.pauseForEnter();
            return;
        }

        boolean confirm = InputValidator.readConfirmation(" Are you sure you want to delete '" + b.getTitle() + "'?");
        if (confirm) {
            boolean deleted = service.removeBook(id);
            if (deleted) {
                System.out.println(GREEN + " [✓] Book removed from catalog." + RESET);
            } else {
                System.out.println(RED + " [!] Failed to delete book." + RESET);
            }
        } else {
            System.out.println(" Deletion cancelled.");
        }
        InputValidator.pauseForEnter();
    }

    // =========================================================================
    // 2. Members Sub-Menu
    // =========================================================================
    private void handleMemberMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n" + CYAN + "------------------- 👥 MEMBER MANAGEMENT ------------------" + RESET);
            System.out.println("  1. List All Members");
            System.out.println("  2. Search Members (Name, Email, Phone)");
            System.out.println("  3. Register New Member");
            System.out.println("  4. Update Member Profile");
            System.out.println("  5. View Member Borrowing History");
            System.out.println("  6. Delete Member");
            System.out.println("  7. Return to Main Menu");
            System.out.println(CYAN + "-----------------------------------------------------------" + RESET);

            int choice = InputValidator.readInt(" Select an action [1-7]: ", 1, 7);
            System.out.println();

            try {
                switch (choice) {
                    case 1:
                        listAllMembers();
                        break;
                    case 2:
                        searchMembers();
                        break;
                    case 3:
                        registerMember();
                        break;
                    case 4:
                        updateMember();
                        break;
                    case 5:
                        viewMemberHistory();
                        break;
                    case 6:
                        deleteMember();
                        break;
                    case 7:
                        back = true;
                        break;
                }
            } catch (SQLException e) {
                System.out.println(RED + " [DB ERROR] " + e.getMessage() + RESET);
            } catch (Exception e) {
                System.out.println(RED + " [ERROR] " + e.getMessage() + RESET);
            }
        }
    }

    private void listAllMembers() throws SQLException {
        List<Member> members = service.getAllMembers();
        renderMemberTable(members);
        InputValidator.pauseForEnter();
    }

    private void searchMembers() throws SQLException {
        String keyword = InputValidator.readNonEmptyString(" Enter member search keyword: ");
        List<Member> members = service.searchMembers(keyword);
        System.out.println("\n Search Results for: '" + keyword + "'");
        renderMemberTable(members);
        InputValidator.pauseForEnter();
    }

    private void renderMemberTable(List<Member> members) {
        TablePrinter table = new TablePrinter("ID", "Full Name", "Email Address", "Phone", "Status", "Registered Date");
        for (Member m : members) {
            table.addRow(
                String.valueOf(m.getId()),
                m.getFullName(),
                m.getEmail(),
                m.getPhone(),
                m.getStatus(),
                m.getRegisteredAt().toString()
            );
        }
        table.print();
    }

    private void registerMember() throws SQLException {
        System.out.println(CYAN + "=== Register New Member ===" + RESET);
        String name = InputValidator.readNonEmptyString(" Full Name: ");
        String email = InputValidator.readValidEmail(" Email Address: ");
        String phone = InputValidator.readPhoneNumber(" Phone Number: ");

        Member member = new Member(name, email, phone, "ACTIVE", Date.valueOf(LocalDate.now()));
        int generatedId = service.registerMember(member);
        System.out.println(GREEN + " [✓] Member successfully registered with ID #" + generatedId + "!" + RESET);
        InputValidator.pauseForEnter();
    }

    private void updateMember() throws SQLException {
        int id = InputValidator.readInt(" Enter Member ID to update: ", 1, Integer.MAX_VALUE);
        Member m = service.getMemberById(id);
        if (m == null) {
            System.out.println(YELLOW + " [!] Member with ID #" + id + " not found." + RESET);
            InputValidator.pauseForEnter();
            return;
        }

        System.out.println(CYAN + " Updating Member: " + m.getFullName() + " (Press Enter to keep current value)" + RESET);
        String name = InputValidator.readOptionalString(" Full Name", m.getFullName());
        String email = InputValidator.readOptionalEmail(" Email Address", m.getEmail());
        String phone = InputValidator.readOptionalString(" Phone Number", m.getPhone());
        String status = InputValidator.readOptionalString(" Status (ACTIVE / SUSPENDED)", m.getStatus()).toUpperCase();

        if (!status.equals("ACTIVE") && !status.equals("SUSPENDED")) {
            System.out.println(YELLOW + " [!] Unknown status entered; keeping previous status: " + m.getStatus() + RESET);
            status = m.getStatus();
        }

        m.setFullName(name);
        m.setEmail(email);
        m.setPhone(phone);
        m.setStatus(status);

        boolean updated = service.updateMember(m);
        if (updated) {
            System.out.println(GREEN + " [✓] Member details updated successfully!" + RESET);
        } else {
            System.out.println(RED + " [!] Failed to update member." + RESET);
        }
        InputValidator.pauseForEnter();
    }

    private void viewMemberHistory() throws SQLException {
        int memberId = InputValidator.readInt(" Enter Member ID: ", 1, Integer.MAX_VALUE);
        Member m = service.getMemberById(memberId);
        if (m == null) {
            System.out.println(YELLOW + " [!] Member not found with ID #" + memberId + RESET);
            InputValidator.pauseForEnter();
            return;
        }

        System.out.println(CYAN + "\n Borrowing History for: " + m.getFullName() + " (" + m.getEmail() + ")" + RESET);
        List<Loan> loans = service.getLoansByMember(memberId);
        renderLoanTable(loans);
        InputValidator.pauseForEnter();
    }

    private void deleteMember() throws SQLException {
        int id = InputValidator.readInt(" Enter Member ID to delete: ", 1, Integer.MAX_VALUE);
        Member m = service.getMemberById(id);
        if (m == null) {
            System.out.println(YELLOW + " [!] Member with ID #" + id + " not found." + RESET);
            InputValidator.pauseForEnter();
            return;
        }

        boolean confirm = InputValidator.readConfirmation(" Are you sure you want to delete member '" + m.getFullName() + "'?");
        if (confirm) {
            boolean deleted = service.removeMember(id);
            if (deleted) {
                System.out.println(GREEN + " [✓] Member removed from records." + RESET);
            } else {
                System.out.println(RED + " [!] Failed to delete member." + RESET);
            }
        } else {
            System.out.println(" Deletion cancelled.");
        }
        InputValidator.pauseForEnter();
    }

    // =========================================================================
    // 3. Circulation & Loans Sub-Menu
    // =========================================================================
    private void handleCirculationMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n" + CYAN + "----------------- 🔄 CIRCULATION & LOANS ------------------" + RESET);
            System.out.println("  1. Issue / Borrow Book");
            System.out.println("  2. Return Book (Calculate Fines)");
            System.out.println("  3. Renew Book Loan");
            System.out.println("  4. View All Active Loans");
            System.out.println("  5. View Overdue Loans & Fines");
            System.out.println("  6. View Full Loan Audit Trail");
            System.out.println("  7. Return to Main Menu");
            System.out.println(CYAN + "-----------------------------------------------------------" + RESET);

            int choice = InputValidator.readInt(" Select an action [1-7]: ", 1, 7);
            System.out.println();

            try {
                switch (choice) {
                    case 1:
                        issueBook();
                        break;
                    case 2:
                        returnBook();
                        break;
                    case 3:
                        renewLoan();
                        break;
                    case 4:
                        viewActiveLoans();
                        break;
                    case 5:
                        viewOverdueLoans();
                        break;
                    case 6:
                        viewAllLoans();
                        break;
                    case 7:
                        back = true;
                        break;
                }
            } catch (SQLException e) {
                System.out.println(RED + " [DB ERROR] " + e.getMessage() + RESET);
            } catch (Exception e) {
                System.out.println(RED + " [ERROR] " + e.getMessage() + RESET);
            }
        }
    }

    private void issueBook() throws SQLException {
        System.out.println(CYAN + "=== Issue / Borrow Book ===" + RESET);
        int bookId = InputValidator.readInt(" Enter Book ID: ", 1, Integer.MAX_VALUE);
        int memberId = InputValidator.readInt(" Enter Member ID: ", 1, Integer.MAX_VALUE);
        int days = InputValidator.readOptionalInt(" Loan Period in Days", 14, 1, 90);

        Loan loan = service.issueBook(bookId, memberId, days);
        System.out.println(GREEN + "\n [✓] Book Issued Successfully!" + RESET);
        System.out.println("  Loan ID    : #" + loan.getId());
        System.out.println("  Book       : " + loan.getBookTitle());
        System.out.println("  Member     : " + loan.getMemberName());
        System.out.println("  Issue Date : " + loan.getBorrowDate());
        System.out.println("  Due Date   : " + loan.getDueDate() + " (" + days + " days)");
        InputValidator.pauseForEnter();
    }

    private void returnBook() throws SQLException {
        System.out.println(CYAN + "=== Return Book ===" + RESET);
        int loanId = InputValidator.readInt(" Enter Loan ID to return: ", 1, Integer.MAX_VALUE);

        LibraryService.ReturnResult result = service.returnBook(loanId);
        System.out.println(GREEN + "\n [✓] Book Returned Successfully!" + RESET);
        System.out.println("  Loan ID      : #" + result.loan.getId());
        System.out.println("  Book         : " + result.loan.getBookTitle());
        System.out.println("  Returned By  : " + result.loan.getMemberName());
        System.out.println("  Due Date     : " + result.loan.getDueDate());
        System.out.println("  Return Date  : " + LocalDate.now());

        if (result.overdueDays > 0) {
            System.out.println(RED + "  Overdue Days : " + result.overdueDays + " days" + RESET);
            System.out.printf(RED + "  Late Fine    : $%.2f ($1.00/day)\n" + RESET, result.fineAmount);
        } else {
            System.out.println(GREEN + "  Returned on time! No overdue fine." + RESET);
        }
        InputValidator.pauseForEnter();
    }

    private void renewLoan() throws SQLException {
        System.out.println(CYAN + "=== Renew Book Loan ===" + RESET);
        int loanId = InputValidator.readInt(" Enter Loan ID to renew: ", 1, Integer.MAX_VALUE);
        int days = InputValidator.readOptionalInt(" Additional Days to Extend", 14, 1, 60);

        boolean renewed = service.renewBookLoan(loanId, days);
        if (renewed) {
            System.out.println(GREEN + " [✓] Loan #" + loanId + " successfully renewed by " + days + " days!" + RESET);
        } else {
            System.out.println(RED + " [!] Failed to renew loan." + RESET);
        }
        InputValidator.pauseForEnter();
    }

    private void viewActiveLoans() throws SQLException {
        System.out.println(CYAN + "=== Active Loans (Checked Out) ===" + RESET);
        List<Loan> loans = service.getActiveLoans();
        renderLoanTable(loans);
        InputValidator.pauseForEnter();
    }

    private void viewOverdueLoans() throws SQLException {
        System.out.println(RED + "=== Overdue Loans & Accruing Fines ===" + RESET);
        List<Loan> loans = service.getOverdueLoans();
        renderLoanTable(loans);
        InputValidator.pauseForEnter();
    }

    private void viewAllLoans() throws SQLException {
        System.out.println(CYAN + "=== Full Loan History / Audit Trail ===" + RESET);
        List<Loan> loans = service.getAllLoans();
        renderLoanTable(loans);
        InputValidator.pauseForEnter();
    }

    private void renderLoanTable(List<Loan> loans) {
        TablePrinter table = new TablePrinter("Loan #", "Book Title", "Member Name", "Borrow Date", "Due Date", "Return Date", "Status", "Fine ($)");
        for (Loan l : loans) {
            table.addRow(
                String.valueOf(l.getId()),
                l.getBookTitle() != null ? l.getBookTitle() : ("Book #" + l.getBookId()),
                l.getMemberName() != null ? l.getMemberName() : ("Member #" + l.getMemberId()),
                l.getBorrowDate().toString(),
                l.getDueDate().toString(),
                l.getReturnDate() != null ? l.getReturnDate().toString() : "Not Returned",
                l.getStatus(),
                String.format("$%.2f", l.getFineAmount())
            );
        }
        table.print();
    }

    // =========================================================================
    // 4. Analytics & Dashboard
    // =========================================================================
    private void handleDashboard() {
        try {
            Map<String, Object> stats = service.getSystemStatistics();
            System.out.println(CYAN + BOLD);
            System.out.println(" ===========================================================================");
            System.out.println("                       📊 SYSTEM ANALYTICS DASHBOARD                        ");
            System.out.println(" ===========================================================================" + RESET);

            System.out.println("  📚 CATALOG & INVENTORY METRICS:");
            System.out.printf("     • Unique Book Titles        : %s\n", stats.getOrDefault("total_titles", 0));
            System.out.printf("     • Total Physical Copies     : %s\n", stats.getOrDefault("total_inventory", 0));
            System.out.printf("     • Available on Shelves      : %s\n", stats.getOrDefault("available_inventory", 0));

            System.out.println("\n  👥 MEMBERSHIP METRICS:");
            System.out.printf("     • Total Registered Patrons  : %s\n", stats.getOrDefault("total_members", 0));
            System.out.printf("     • Active Accounts           : %s\n", stats.getOrDefault("active_members", 0));

            System.out.println("\n  🔄 CIRCULATION & REVENUE METRICS:");
            System.out.printf("     • Total Historical Loans    : %s\n", stats.getOrDefault("total_loans", 0));
            System.out.printf("     • Currently Checked Out     : %s\n", stats.getOrDefault("active_loans", 0));
            int overdue = (int) stats.getOrDefault("overdue_loans", 0);
            if (overdue > 0) {
                System.out.printf(RED + "     • Currently Overdue Loans   : %d ⚠️\n" + RESET, overdue);
            } else {
                System.out.println(GREEN + "     • Currently Overdue Loans   : 0 (All on time!)\n" + RESET);
            }
            System.out.printf("     • Total Fines Accrued       : $%.2f\n", stats.getOrDefault("total_fines", 0.0));

            System.out.println(CYAN + " ===========================================================================" + RESET);

        } catch (SQLException e) {
            System.out.println(RED + " [DB ERROR] " + e.getMessage() + RESET);
        }
        InputValidator.pauseForEnter();
    }

    // =========================================================================
    // 5. Settings Sub-Menu
    // =========================================================================
    private void handleSettingsMenu() {
        System.out.println(CYAN + "=== Database & System Settings ===" + RESET);
        System.out.println(" 1. Test Database Connection");
        System.out.println(" 2. Reconfigure MySQL Credentials");
        System.out.println(" 3. Re-run Database Schema & Seed Data");
        System.out.println(" 4. Back to Main Menu");

        int choice = InputValidator.readInt(" Select option [1-4]: ", 1, 4);
        switch (choice) {
            case 1:
                System.out.print(" Testing connection... ");
                if (DatabaseConnection.testConnection()) {
                    System.out.println(GREEN + "[CONNECTED OK]" + RESET);
                } else {
                    System.out.println(RED + "[FAILED]" + RESET);
                }
                InputValidator.pauseForEnter();
                break;
            case 2:
                promptDatabaseSetup();
                break;
            case 3:
                System.out.print(" Executing schema.sql script... ");
                boolean ok = DatabaseConnection.initializeDatabaseAndTables();
                if (ok) {
                    System.out.println(GREEN + "[COMPLETED]" + RESET);
                } else {
                    System.out.println(RED + "[FAILED]" + RESET);
                }
                InputValidator.pauseForEnter();
                break;
            case 4:
                break;
        }
    }

    private void printExitMessage() {
        DatabaseConnection.closeConnection();
        System.out.println(CYAN + BOLD);
        System.out.println(" ===========================================================================");
        System.out.println("   Thank you for using BookHaven Library Management System!");
        System.out.println("   Database connections closed cleanly. Have a wonderful day! 👋");
        System.out.println(" ===========================================================================" + RESET);
    }
}
