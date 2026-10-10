package com.yax.library.model;

import java.time.LocalDate;

public class Transaction {

    private int transactionId, bookId, memberId;
    private LocalDate issueDate, dueDate, returnDate;
    private double fine;

    public  Transaction(int bookId, int memberId, LocalDate issueDate) {
        this.bookId = bookId;
        this.memberId = memberId;
        this.issueDate = issueDate;
        dueDate = issueDate.plusDays(21);
        returnDate = null;
        fine = 0;
    }

    public Transaction(int transactionId, int bookId, int memberId, LocalDate issueDate, LocalDate dueDate, LocalDate returnDate,  double fine) {
        this.transactionId = transactionId;
        this.bookId = bookId;
        this.memberId = memberId;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.fine = fine;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public int getMemberId() {
        return memberId;
    }

    public void setMemberId(int memberId) {
        this.memberId = memberId;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public double getFine() {
        return fine;
    }

    public void setFine(double fine) {
        this.fine = fine;
    }

    @Override
    public String toString(){
        if (returnDate == null) return LocalDate.now().isAfter(dueDate)?
                    String.format("[%d] Book [%d] issued by member [%d] on %s, was due on %s >> Return Pending",
                            transactionId, bookId, memberId, issueDate, dueDate) :
                    String.format("[%d] Book [%d] issued by member [%d] on %s, due on %s",
                            transactionId, bookId, memberId, issueDate, dueDate);
        else return returnDate.isAfter(dueDate)?
                    String.format("[%d] Book [%d] issued by member [%d] on %s, was due on %s but returned on %s >> Fine $%.2f",
                            transactionId, bookId, memberId, issueDate, dueDate, returnDate, fine) :
                    String.format("[%d] Book [%d] issued by member [%d] on %s, was returned by the due date of %s",
                    transactionId, bookId, memberId, issueDate, dueDate);
    }
}
