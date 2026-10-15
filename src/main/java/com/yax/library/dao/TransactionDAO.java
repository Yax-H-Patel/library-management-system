package com.yax.library.dao;

import com.yax.library.model.Book;
import com.yax.library.model.Member;
import com.yax.library.model.Transaction;
import com.yax.library.util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {

    private static final double FINE_PER_DAY = 2.5;

    public boolean issueBook(int bookId, int memberId) {

        BookDAO bookDAO = new BookDAO();
        MemberDAO memberDAO = new MemberDAO();
        Book book = bookDAO.getBookById(bookId);
        Member member = memberDAO.getMemberById(memberId);

        if (book == null || member == null) return false;
        else if (book.getAvailableCopies() == 0 || member.getStatus() == Member.Status.SUSPENDED) return false;

        Connection conn = null;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            String sql = "UPDATE books SET available_copies = available_copies - 1 " +
                    "WHERE book_id = ? AND available_copies > 0";

            PreparedStatement pstmt = conn.prepareStatement(sql);

            pstmt.setInt(1, bookId);

            int rowsAffected = pstmt.executeUpdate();
            pstmt.close();
            if (rowsAffected == 0) {
                conn.rollback();
                return false;
            }

            sql = "INSERT INTO transactions(book_id, member_id, issue_date, due_date, return_date, fine) " + "VALUES (?, ?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(sql,  Statement.RETURN_GENERATED_KEYS);
            Transaction transaction = new Transaction(bookId, memberId, LocalDate.now());

            pstmt.setInt(1, transaction.getBookId());
            pstmt.setInt(2, transaction.getMemberId());
            pstmt.setDate(3, Date.valueOf(transaction.getIssueDate()));
            pstmt.setDate(4, Date.valueOf(transaction.getDueDate()));
            if (transaction.getReturnDate() == null) pstmt.setDate(5, null);
            else pstmt.setDate(5, Date.valueOf(transaction.getReturnDate()));
            pstmt.setDouble(6, transaction.getFine());

            rowsAffected = pstmt.executeUpdate();
            pstmt.close();

            if  (rowsAffected > 0) {
                conn.commit();
                return true;
            } else {
                conn.rollback();
                return false;
            }

        } catch (SQLException e) {
            System.out.println("Error Issuing the Book: " + e.getMessage());
            if (conn != null)
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.out.println("Rollback Failed: " + ex.getMessage());
                }
            return false;
        } finally {
            DBConnection.closeConnection(conn);
        }
    }

    public boolean returnBook(int transactionId) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            Transaction transaction = getTransactionById(transactionId);
            if (transaction == null) return false;

            transaction = new Transaction(transaction.getTransactionId(), transaction.getBookId(), transaction.getMemberId(), transaction.getIssueDate(), transaction.getDueDate(), LocalDate.now(), transaction.getFine());

            String sql = "UPDATE books SET available_copies = available_copies + 1 " +
                    "WHERE book_id = ? AND available_copies < total_copies";
            PreparedStatement pstmt = conn.prepareStatement(sql);

            pstmt.setInt(1, transaction.getBookId());
            int rowsAffected = pstmt.executeUpdate();
            pstmt.close();
            if  (rowsAffected == 0) {
                conn.rollback();
                return false;
            }

            sql = "UPDATE transactions SET return_date = ?, fine = ? WHERE transaction_id = ? AND return_date IS NULL";
            pstmt = conn.prepareStatement(sql);

            double fine = transaction.getReturnDate().isAfter(transaction.getDueDate())? FINE_PER_DAY * (ChronoUnit.DAYS.between(transaction.getDueDate(), LocalDate.now())) : 0;

            pstmt.setDate(1, Date.valueOf(transaction.getReturnDate()));
            pstmt.setDouble(2, fine);
            pstmt.setInt(3, transaction.getTransactionId());

            rowsAffected = pstmt.executeUpdate();
            pstmt.close();
            if  (rowsAffected > 0) {
                conn.commit();
                return true;
            }  else {
                conn.rollback();
                return false;
            }

        } catch (SQLException e) {
            System.out.println("Error Returning the Book: " + e.getMessage());
            if (conn != null)
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.out.println("Rollback Failed: " + ex.getMessage());
                }
            return false;
        } finally {
            DBConnection.closeConnection(conn);
        }
    }

    public Transaction getTransactionById(int transactionId) {
        String sql = "SELECT * FROM transactions WHERE transaction_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);) {

            pstmt.setInt(1, transactionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToTransaction(rs);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error loading transaction: " + e.getMessage());
        }
        return null;
    }

    private Transaction mapRowToTransaction(ResultSet rs) throws SQLException {
        Date returned = rs.getDate("return_date");
        LocalDate returnDate = (returned == null) ? null : returned.toLocalDate();
        return new Transaction(
                rs.getInt("transaction_id"),
                rs.getInt("book_id"),
                rs.getInt("member_id"),
                rs.getDate("issue_date").toLocalDate(),
                rs.getDate("due_date").toLocalDate(),
                returnDate,
                rs.getDouble("fine")
        );
    }

    public List<Transaction> getAllTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions ORDER BY transaction_id ASC";
        try (Connection conn = DBConnection.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapRowToTransaction(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error loading transactions: " + e.getMessage());
        }
        return transactions;
    }

    public List<Transaction> getActiveTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE return_date is NULL ORDER BY transaction_id ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapRowToTransaction(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error loading transactions: " + e.getMessage());
        }
        return transactions;
    }

}
