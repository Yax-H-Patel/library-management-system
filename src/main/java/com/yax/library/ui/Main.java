package com.yax.library.ui;

import com.yax.library.dao.BookDAO;
import com.yax.library.model.Book;

import java.util.List;
import java.util.Scanner;

public class Main {

    private static final BookDAO bookDAO = new BookDAO();
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        boolean running = true;

        while (running) {
            printMenu();
            int choice = readInt("Choose an option: ");

            switch (choice) {
                case 1 -> listAllBooks();
                case 2 -> addBook();
                case 3 -> searchBooks();
                case 4 -> updateBookCopies();
                case 5 -> deleteBook();
                case 0 -> {
                    running = false;
                    System.out.println("Goodbye!");
                }
                default -> System.out.println("Invalid option, try again.");
            }
            System.out.println();
        }

        scanner.close();
    }

    private static void printMenu() {
        System.out.println("===== Library Management System =====");
        System.out.println("1. List all books");
        System.out.println("2. Add a book");
        System.out.println("3. Search books by title");
        System.out.println("4. Update copies of a book");
        System.out.println("5. Delete a book");
        System.out.println("0. Exit");
    }

    private static void listAllBooks() {
        List<Book> books = bookDAO.getAllBooks();
        if (books.isEmpty()) {
            System.out.println("No books found.");
            return;
        }
        for (Book b : books) {
            System.out.println(b);
        }
    }

    private static void addBook() {
        System.out.print("Title: ");
        String title = scanner.nextLine();

        System.out.print("Author: ");
        String author = scanner.nextLine();

        System.out.print("Genre: ");
        String genre = scanner.nextLine();

        System.out.print("ISBN: ");
        String isbn = scanner.nextLine();

        int copies = readInt("Number of copies: ");

        Book book = new Book(title, author, genre, isbn, copies, copies);
        boolean success = bookDAO.addBook(book);

        if (success) {
            System.out.println("Added! New book ID: " + book.getBookId());
        } else {
            System.out.println("Failed to add book.");
        }
    }

    private static void searchBooks() {
        System.out.print("Enter keyword: ");
        String keyword = scanner.nextLine();

        List<Book> results = bookDAO.searchBooksByTitle(keyword);
        if (results.isEmpty()) {
            System.out.println("No matches found.");
            return;
        }
        for (Book b : results) {
            System.out.println(b);
        }
    }

    private static void updateBookCopies() {
        int id = readInt("Enter book ID to update: ");
        Book book = bookDAO.getBookById(id);

        if (book == null) {
            System.out.println("No book with that ID.");
            return;
        }

        System.out.println("Current: " + book);
        int newTotal = readInt("New total copies: ");
        int newAvailable = readInt("New available copies: ");

        book.setTotalCopies(newTotal);
        book.setAvailableCopies(newAvailable);

        boolean success = bookDAO.updateBook(book);
        System.out.println(success ? "Updated!" : "Update failed.");
    }

    private static void deleteBook() {
        int id = readInt("Enter book ID to delete: ");
        boolean success = bookDAO.deleteBook(id);
        System.out.println(success ? "Deleted." : "No book with that ID.");
    }

    // Reads an integer from the user, re-prompting on invalid input
    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }
}
