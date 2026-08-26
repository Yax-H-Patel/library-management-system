# Library Management System

A console-based Library Management System built with Java, JDBC, and MySQL.

## Status
🚧 In development — following a phased build.

## Tech Stack
- Java 17
- JDBC (mysql-connector-j)
- MySQL 8
- Maven

## Features (planned)
- [ ] Add/search/update/delete books
- [ ] Register and manage members
- [ ] Issue and return books
- [ ] Track overdue fines
- [ ] Admin login

## Setup
1. Create the database using `schema.sql`
2. Configure DB credentials in `src/main/resources/db.properties`
3. Run `Main.java`

## Project Structure
```
model/  - plain data classes (Book, Member, Transaction)
dao/    - database access objects (one per table)
util/   - DBConnection and helpers
ui/     - console menu / entry point
```
