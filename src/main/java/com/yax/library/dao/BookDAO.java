package com.yax.library.dao;

import com.yax.library.model.Book;
import com.yax.library.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles all database operations for the "books" table.
 * Nothing in here should know about the console/UI - it only
 * talks to Book objects and the database.
 */
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

    /**
     * Returns every book in the database, ordered by title.
     */
    public List<Book> getAllBooks() {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM books ORDER BY title";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                books.add(mapRowToBook(rs));
            }

        } catch (SQLException e) {
            System.out.println("Error fetching books: " + e.getMessage());
        }

        return books;
    }

    /**
     * Finds a single book by its ID.
     * Returns null if not found.
     */
    public Book getBookById(int bookId) {
        String sql = "SELECT * FROM books WHERE book_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, bookId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToBook(rs);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error fetching book: " + e.getMessage());
        }

        return null; // not found
    }

    /**
     * Converts one row of a ResultSet into a Book object.
     * Pulling this into its own method avoids repeating this mapping
     * code in every method that reads books back from the DB.
     */
    private Book mapRowToBook(ResultSet rs) throws SQLException {
        return new Book(
                rs.getInt("book_id"),
                rs.getString("title"),
                rs.getString("author"),
                rs.getString("genre"),
                rs.getString("isbn"),
                rs.getInt("total_copies"),
                rs.getInt("available_copies")
        );
    }

    /**
     * Updates an existing book's details.
     * Matches by book_id - the book must already exist.
     */
    public boolean updateBook(Book book) {
        String sql = "UPDATE books SET title = ?, author = ?, genre = ?, isbn = ?" + " WHERE book_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, book.getTitle());
            stmt.setString(2, book.getAuthor());
            stmt.setString(3, book.getGenre());
            stmt.setString(4, book.getIsbn());
            stmt.setInt(5, book.getBookId());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.out.println("Error updating book: " + e.getMessage());
            return false;
        }
    }

    /**
     * Deletes a book by ID. Returns false if no book had that ID.
     */
    public boolean deleteBook(int bookId) {
        String sql = "DELETE FROM books WHERE book_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, bookId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.out.println("Error deleting book: " + e.getMessage());
            return false;
        }
    }

    /**
     * Searches books where the title contains the given keyword (case-insensitive).
     */
    public List<Book> searchBooksByTitle(String keyword) {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM books WHERE title LIKE ? ORDER BY title";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + keyword + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    books.add(mapRowToBook(rs));
                }
            }

        } catch (SQLException e) {
            System.out.println("Error searching books: " + e.getMessage());
        }

        return books;
    }

    public int countActiveLoans(int bookId) {
        String sql = "SELECT COUNT(*) FROM transactions WHERE book_id = ? AND return_date IS NULL";
        int count = -999;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, bookId);

            try(ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    count = rs.getInt(1);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error counting loans for book: " + e.getMessage());
        }
        return count;
    }

    public boolean updateTotalCopies(int bookId, int newTotal) {
        int activeLoanCount =  countActiveLoans(bookId);
        if (activeLoanCount < 0 || newTotal < activeLoanCount) return false;
        String sql = "UPDATE books SET " +
                "total_copies = ?, available_copies = ? WHERE book_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, newTotal);
            stmt.setInt(2, newTotal - activeLoanCount);
            stmt.setInt(3, bookId);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error updating total copies for book: " + e.getMessage());
        }
        return false;
    }

    public static void main(String[] args) {
        BookDAO bdao = new BookDAO();

        Book book = bdao.getBookById(1);

        System.out.println(book);
        book.setAvailableCopies(99);
        System.out.println(bdao.updateBook(book));
        book = bdao.getBookById(1);
        System.out.println(book);

    }
}