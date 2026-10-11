# Library Management System

A console-based application built with Core Java, JDBC, MySQL, and Maven. The project is being developed to practice database-backed application design.

## Current development phase

**Book and member CRUD is implemented. Borrowing and returning books is in progress.**

The console currently supports catalog and member management. The borrowing model and database table exist, but the issue/return DAO implementation is unfinished and is not connected to the console menu. This is a learning project under active development.

## Features

### Implemented

- List all books, ordered by title.
- Add books and retrieve their generated IDs.
- Search books by a title keyword.
- Update a book's total and available copy counts.
- Delete books, subject to database foreign-key constraints.
- List and register members.
- Update member names, email addresses, and phone numbers.
- Delete members, subject to database foreign-key constraints.
- Persist data using JDBC prepared statements and MySQL.

The book DAO also supports updating title, author, genre, and ISBN. The console currently exposes only copy-count updates. Member status is represented in the model and database, but changing it is not exposed in the console.

### In progress

- Issue and return operations in `TransactionDAO`.
- Consistent inventory updates when a book is borrowed or returned.
- Database transaction handling for borrowing operations.

The current `Transaction` model assigns a 21-day borrowing period. Issue and return operations are not yet usable end to end.

### Planned

- Active loan lists and member borrowing history.
- Overdue tracking and fine calculation.
- Admin/librarian login; the `users` table is already defined.
- Input validation, clearer error handling, and automated tests.

## Technology stack

| Component | Current configuration |
| --- | --- |
| Java | JDK 25, matching the source and target levels in `pom.xml` |
| Build | Maven |
| Database | MySQL 8; the schema uses expression defaults |
| Database access | JDBC |
| JDBC driver | MySQL Connector/J 8.3.0 |
| Interface | Interactive console |

## Project structure

```text
library-management-system/
├── pom.xml
├── schema.sql
├── src/main/java/com/yax/library/
│   ├── model/
│   │   ├── Book.java
│   │   ├── Member.java
│   │   └── Transaction.java
│   ├── dao/
│   │   ├── BookDAO.java
│   │   ├── MemberDAO.java
│   │   └── TransactionDAO.java    # Work in progress
│   ├── util/
│   │   └── DBConnection.java
│   └── ui/
│       └── Main.java             # Application entry point
├── src/main/resources/
│   └── db.properties             # Local database configuration
└── src/test/java/                # No automated tests yet
```

The console calls DAOs directly. DAOs execute SQL and map database rows to model objects. `DBConnection` loads connection settings from the classpath. A service layer for business rules and transaction coordination is a planned improvement.

## Setup and running

### 1. Install prerequisites

- JDK 25.
- Maven available on your `PATH`.
- A running MySQL server and the `mysql` command-line client.

Run the following commands from the project root to check your Java and Maven installations:

```powershell
java -version
mvn -version
```

### 2. Initialize the database

**`schema.sql` drops and recreates the application's tables. Run it only against a development database whose existing data can be discarded.** It inserts three sample books and two sample members.

From the project root, open the MySQL client using an account with database creation permissions:

```powershell
mysql -u root -p
```

Then run:

```sql
CREATE DATABASE IF NOT EXISTS library_db;
SOURCE schema.sql;
```

The database must be created first because `schema.sql` starts with `USE library_db`.

For application access, create a dedicated user rather than using the administrator account. Replace the password placeholder before executing:

```sql
CREATE USER 'library_app'@'localhost' IDENTIFIED BY 'replace-with-your-local-password';
GRANT SELECT, INSERT, UPDATE, DELETE ON library_db.* TO 'library_app'@'localhost';
EXIT;
```

If an application user already exists, use its credentials instead.

### 3. Configure the database connection

Create or edit `src/main/resources/db.properties`:

```properties
db.url=jdbc:mysql://localhost:3306/library_db
db.user=library_app
db.password=replace-with-your-local-password
```

`DBConnection` requires this file on the classpath. Adjust the host, port, and credentials for your local server.

Keep real credentials out of version control. Although `.gitignore` contains a rule for `db.properties`, an already tracked file remains tracked. Maven also copies this resource into build output, so keep generated artifacts containing local credentials private.

### 4. Build and start the console

Build the classes and copy runtime dependencies:

```powershell
mvn compile dependency:copy-dependencies
```

On Windows PowerShell:

```powershell
java -cp "target/classes;target/dependency/*" com.yax.library.ui.Main
```

On macOS or Linux, use `:` as the classpath separator:

```sh
java -cp "target/classes:target/dependency/*" com.yax.library.ui.Main
```

Alternatively, import the project into IntelliJ IDEA as a Maven project, select JDK 25, and run `com.yax.library.ui.Main`.

## Console options

| Option | Action |
| --- | --- |
| 1 | List all books |
| 2 | Add a book |
| 3 | Search books by title |
| 4 | Update copies of a book |
| 5 | Delete a book |
| 6 | List all members |
| 7 | Add a member |
| 8 | Update a member |
| 9 | Delete a member |
| 0 | Exit |

## Next development priorities

1. Complete and test issue/return workflows, with a service layer, shared JDBC transactions, member eligibility checks, and safe inventory updates.
2. Add active loan/history queries, overdue reports, and fine calculation using decimal monetary values.
3. Strengthen validation and error handling, add automated tests, and make database setup reproducible before exposing the services through a REST API.
