package com.yax.library.ui;

import com.yax.library.dao.BookDAO;
import com.yax.library.dao.MemberDAO;
import com.yax.library.model.Book;
import com.yax.library.model.Member;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static final BookDAO bookDAO = new BookDAO();
    private static final MemberDAO memberDAO = new MemberDAO();
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
                case 6 -> listAllMembers();
                case 7 -> addMember();
                case 8 -> updateMember();
                case 9 -> deleteMember();
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
        System.out.println("6. List all members");
        System.out.println("7. Add a member");
        System.out.println("8. Update a member");
        System.out.println("9. Delete a member");
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

    private static void listAllMembers() {
        List<Member> members = memberDAO.getAllMembers();
        if (members.isEmpty()) {
            System.out.println("No members found.");
            return;
        }
        for (Member m : members) {
            System.out.println(m);
        }
    }

    private static void addMember() {
        System.out.print("Name: ");
        String name = scanner.nextLine();

        System.out.print("Email: ");
        String email = scanner.nextLine();

        LocalDate joinDate = LocalDate.now();

        System.out.print("Phone number: ");
        String phone = scanner.nextLine();

        Member member = new Member(name, email, joinDate, phone);
        boolean success = memberDAO.addMember(member);

        if (success) {
            System.out.println("Added! New member ID: " + member.getMemberId());
        } else {
            System.out.println("Failed to add member.");
        }
    }

    private static void updateMember() {
        int choice, id;
        id = readInt("Enter member ID: ");
        Member member = memberDAO.getMemberById(id);

        if (member == null) {
            System.out.println("No member with that ID.");
            return;
        }

        System.out.println("Current: " + member);

        do {
            System.out.println("    Enter 1 to change the name");
            System.out.println("    Enter 2 to change the email");
            System.out.println("    Enter 3 to change the phone number");
            System.out.println("    Enter 0 to save the changes and exit");
            choice = readInt("Choose an option: ", "    ");
            if (choice == 1) {
                System.out.print("Enter new name: ");
                member.setName(scanner.nextLine());
            } else if (choice == 2) {
                System.out.print("Enter new email: ");
                member.setEmail(scanner.nextLine());
            } else if (choice == 3) {
                System.out.print("Enter new phone number: ");
                member.setPhone(scanner.nextLine());
            } else if (choice == 0) {
                boolean success = memberDAO.updateMember(member);
                System.out.println(success ? "    -----SAVED-----" : "    Update failed.");
            } else {
                System.out.println("Invalid choice.");
            }
        } while (choice != 0);
    }

    private static void deleteMember() {
        int id =  readInt("Enter member ID: ");
        boolean success = memberDAO.deleteMember(id);
        if (success) {
            System.out.println("Deleted! Member ID: " + id);
        } else {
            System.out.println("Member with that ID does not exist.");
        }
    }

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

    private static int readInt(String prompt, String indent) {
        while (true) {
            System.out.print(indent + prompt);
            String input = scanner.nextLine();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println(indent + "Please enter a valid number.");
            }
        }
    }
}
