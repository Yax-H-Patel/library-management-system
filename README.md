# Library Management System

A console application for managing a small library's books and members, written in Core Java with JDBC and MySQL and built with Maven. It is a learning project for practising database-backed application design. Book and member management work from the console menu. Issuing and returning books are written in the data layer but are not yet available from the menu.

## Tech stack

| Component | Version / detail |
| --- | --- |
| Java | 25 (`maven.compiler.source` and `maven.compiler.target` in `pom.xml`) |
| Build | Maven (project version `1.0-SNAPSHOT`) |
| JDBC driver | MySQL Connector/J (`com.mysql:mysql-connector-j`) 8.3.0 |
| Database | MySQL 8. `pom.xml` does not pin a server version. `schema.sql` uses `DEFAULT (CURRENT_DATE)` and `CHECK` constraints, which need a recent MySQL 8.0 release. |
| Interface | Text menu in the console |

There are no other dependencies and no test libraries.

## Features

- [x] List all books, ordered by title (menu 1)
- [x] Add a book, showing its generated ID (menu 2)
- [x] Search books by a word in the title (menu 3)
- [x] Change a book's total copies (menu 4). Available copies are recalculated from active loans, and the total cannot go below the number of copies on loan.
- [x] Delete a book (menu 5)
- [x] List all members (menu 6)
- [x] Add a member; the join date is today and the status is `ACTIVE` (menu 7)
- [x] Update a member's name, email or phone number (menu 8)
- [x] Delete a member (menu 9)
- [ ] Issue a book to a member
- [ ] Return a book, with a late fine
- [ ] List all loans or active loans
- [ ] Edit a book's title, author, genre or ISBN
- [ ] Change a member's status (`ACTIVE` / `SUSPENDED`)
- [ ] Librarian / admin login
- [ ] Overdue reports

The issue, return and loan-listing features exist in `TransactionDAO` but are not in the menu. Issuing checks that copies are available and that the member is not suspended, and sets a due date 21 days after the issue date. Returning charges 2.5 per day late. Editing book details exists as `BookDAO.updateBook` and is not in the menu either. The model, DAO and database all support member status, but nothing in the console changes it. Login exists only as the `users` table.

## Project structure

```text
src/main/java/com/yax/library/
├── model/   Book, Member, Transaction: plain objects holding one row's data
├── dao/     BookDAO, MemberDAO, TransactionDAO: all SQL; turn rows into model objects
├── util/    DBConnection: reads db.properties and opens JDBC connections
└── ui/      Main: the console menu and input handling (entry point)
```

The project root also holds `pom.xml` and `schema.sql`. Database settings go in `src/main/resources/db.properties`, which you create yourself (see Setup) and which `.gitignore` excludes.

## Setup

### 1. Create the database

In the MySQL client:

```sql
CREATE DATABASE IF NOT EXISTS library_db;
```

`schema.sql` starts with `USE library_db`, so the database must exist first.

### 2. Run schema.sql

> **Warning:** `schema.sql` DROPS the `transactions`, `books`, `members` and `users` tables and creates them again. Any data already in them is deleted. Run it only when you are happy to lose that data.

From the project root:

```sh
mysql -u root -p library_db < schema.sql
```

It also inserts three sample books and two sample members.

### 3. Create db.properties

Create `src/main/resources/db.properties` with these three keys, replacing the placeholder values with your own:

```properties
db.url=jdbc:mysql://localhost:3306/library_db
db.user=your_mysql_user
db.password=your_mysql_password
```

`DBConnection` loads this file from the classpath and stops with "db.properties not found on classpath" if the file is missing. Do not commit it.

### 4. Run the app

`pom.xml` does not configure a way to run the app, so open the project in your IDE as a Maven project, set the JDK to 25, and run `com.yax.library.ui.Main`.

Note: `Main` currently issues book 1 to member 1 every time it starts (see Known limitations).

## Database design

- **books**: the catalogue. Each book has a unique ISBN plus `total_copies` and `available_copies`.
- **members**: library members. Each has a unique email, a join date, and a status of `ACTIVE` or `SUSPENDED`.
- **transactions**: one row per loan, linking a book to a member, with issue date, due date, return date (`NULL` while the book is still out) and fine. `book_id` and `member_id` are foreign keys. They have no `ON DELETE` rule, so a book or member that has any loan history cannot be deleted. Through this table, books and members have a many-to-many relationship.
- **users**: usernames, password hashes and roles (`ADMIN` / `LIBRARIAN`) for a future login. No other table refers to it and no code uses it yet.

## Design notes

- **Layers.** `model` holds data, `dao` holds all SQL, and `ui` holds all console input and output. The UI calls the DAOs directly; there is no service layer. Each DAO method opens its own connection through `DBConnection`.
- **Prepared statements.** Every query uses `PreparedStatement` with `?` placeholders, so user input never becomes part of the SQL text.
- **JDBC transactions.** `issueBook` and `returnBook` switch off auto-commit, update both the `books` row and the `transactions` row, then commit, rolling back if either step fails. The inventory updates are conditional (`available_copies > 0` when issuing, `available_copies < total_copies` when returning). A return of a loan that was already returned changes no rows and is rolled back.
- **CHECK constraints.** The `books` table requires both copy counts to be zero or more, and available copies to be no more than total copies. The database therefore refuses bad counts even if the Java code has a bug.

## Known limitations

- `Main` has a leftover test line that calls `issueBook(1, 1)` at startup, so every launch creates a real loan and lowers book 1's available copies.
- Issue, return and loan listing are not in the console menu.
- There are no automated tests (no `src/test` folder or test dependencies). `BookDAO` and `MemberDAO` have `main` methods used for manual testing, and these write to whatever database `db.properties` points at.
- There is no login, even though the `users` table exists.
- Members are never suspended automatically (for example for overdue books or unpaid fines). The status can only be changed directly in the database.
- Deleting a book or member with loan history fails because of the foreign keys, and the console then wrongly says the ID does not exist.
- Input checking is minimal. A blank ISBN is saved as an empty string, so a second book without an ISBN fails the unique constraint. Negative copy counts are stopped only by the database's CHECK constraints.
- Changing total copies counts loans and updates the book in two separate steps rather than one transaction.
- Fines are calculated as a Java `double`, while the database column is `DECIMAL(6,2)`.
