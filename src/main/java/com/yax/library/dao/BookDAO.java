package com.yax.library.dao;

import com.yax.library.model.Book;
import com.yax.library.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class BookDAO {

    /**
     * Inserts a new book into the database.
     * After insert, sets the auto-generated bookId back onto the Book object.
     */
    public boolean addBook(Book book) {
        String sql = "INSERT INTO books (title, author, genre, isbn, total_copies, available_copies) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, book.getTitle());
            stmt.setString(2, book.getAuthor());
            stmt.setString(3, book.getGenre());
            stmt.setString(4, book.getIsbn());
            stmt.setInt(5, book.getTotalCopies());
            stmt.setInt(6, book.getAvailableCopies());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                // Grab the auto-generated book_id MySQL just created, so our
                // in-memory Book object matches what's actually in the DB.
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        book.setBookId(keys.getInt(1));
                    }
                }
                return true;
            }
            return false;

        } catch (SQLException e) {
            System.out.println("Error adding book: " + e.getMessage());
            return false;
        }
    }

    // Quick manual test
    public static void main(String[] args) {
        BookDAO dao = new BookDAO();

        Book newBook = new Book(
                "The Pragmatic Programmer",
                "Andrew Hunt",
                "Programming",
                "9780135957059",
                2,
                2
        );

        boolean success = dao.addBook(newBook);

        if (success) {
            System.out.println("Book added! Generated ID: " + newBook.getBookId());
        } else {
            System.out.println("Failed to add book.");
        }
    }
}