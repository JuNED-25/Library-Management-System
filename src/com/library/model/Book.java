package com.library.model;

import java.sql.Timestamp;

/**
 * Represents a book item in the library catalog.
 */
public class Book {
    private int id;
    private String title;
    private String author;
    private String isbn;
    private String genre;
    private int publishedYear;
    private int totalCopies;
    private int availableCopies;
    private Timestamp createdAt;

    public Book() {}

    public Book(String title, String author, String isbn, String genre, int publishedYear, int totalCopies) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.genre = genre;
        this.publishedYear = publishedYear;
        this.totalCopies = totalCopies;
        this.availableCopies = totalCopies;
    }

    public Book(int id, String title, String author, String isbn, String genre, int publishedYear, int totalCopies, int availableCopies, Timestamp createdAt) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.genre = genre;
        this.publishedYear = publishedYear;
        this.totalCopies = totalCopies;
        this.availableCopies = availableCopies;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public int getPublishedYear() { return publishedYear; }
    public void setPublishedYear(int publishedYear) { this.publishedYear = publishedYear; }

    public int getTotalCopies() { return totalCopies; }
    public void setTotalCopies(int totalCopies) { this.totalCopies = totalCopies; }

    public int getAvailableCopies() { return availableCopies; }
    public void setAvailableCopies(int availableCopies) { this.availableCopies = availableCopies; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return String.format("Book[ID=%d, Title='%s', Author='%s', ISBN='%s', Genre='%s', Year=%d, Copies=%d/%d]",
                id, title, author, isbn, genre, publishedYear, availableCopies, totalCopies);
    }
}
