-- Library Management System - Database Schema
-- Run with: mysql -u root -p library_db < schema.sql

USE library_db;

-- Drop tables if they exist (useful while you're still designing/testing)
DROP TABLE IF EXISTS transactions;
DROP TABLE IF EXISTS books;
DROP TABLE IF EXISTS members;
DROP TABLE IF EXISTS users;

-- ============================================
-- BOOKS
-- ============================================
CREATE TABLE books (
    book_id          INT AUTO_INCREMENT PRIMARY KEY,
    title            VARCHAR(255) NOT NULL,
    author           VARCHAR(255) NOT NULL,
    genre            VARCHAR(100),
    isbn             VARCHAR(20) UNIQUE,
    total_copies     INT NOT NULL DEFAULT 1,
    available_copies INT NOT NULL DEFAULT 1,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- MEMBERS
-- ============================================
CREATE TABLE members (
    member_id   INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    email       VARCHAR(255) UNIQUE NOT NULL,
    phone       VARCHAR(20),
    join_date   DATE NOT NULL DEFAULT (CURRENT_DATE),
    status      ENUM('ACTIVE', 'SUSPENDED') NOT NULL DEFAULT 'ACTIVE'
);

-- ============================================
-- USERS (admin/librarian login)
-- ============================================
CREATE TABLE users (
    user_id       INT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role          ENUM('ADMIN', 'LIBRARIAN') NOT NULL DEFAULT 'LIBRARIAN'
);

-- ============================================
-- TRANSACTIONS (borrow/return records)
-- This table links books <-> members = a many-to-many relationship
-- ============================================
CREATE TABLE transactions (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
    book_id        INT NOT NULL,
    member_id      INT NOT NULL,
    issue_date     DATE NOT NULL DEFAULT (CURRENT_DATE),
    due_date       DATE NOT NULL,
    return_date    DATE DEFAULT NULL,
    fine           DECIMAL(6,2) DEFAULT 0.00,

    FOREIGN KEY (book_id) REFERENCES books(book_id),
    FOREIGN KEY (member_id) REFERENCES members(member_id)
);

-- ============================================
-- Sample data to test with
-- ============================================
INSERT INTO books (title, author, genre, isbn, total_copies, available_copies) VALUES
('Clean Code', 'Robert C. Martin', 'Programming', '9780132350884', 3, 3),
('The Hobbit', 'J.R.R. Tolkien', 'Fantasy', '9780547928227', 2, 2),
('Dune', 'Frank Herbert', 'Sci-Fi', '9780441172719', 1, 1);

INSERT INTO members (name, email, phone) VALUES
('Alice Johnson', 'alice@example.com', '555-1234'),
('Bob Smith', 'bob@example.com', '555-5678');