DROP DATABASE IF EXISTS reservation_system;
CREATE DATABASE reservation_system;
USE reservation_system;

CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(50) NOT NULL
);

CREATE TABLE trains (
    train_no VARCHAR(20) PRIMARY KEY,
    train_name VARCHAR(100) NOT NULL,
    source VARCHAR(50),
    destination VARCHAR(50)
);

INSERT INTO trains VALUES
('101', 'Vande Bharat', 'Mumbai', 'Delhi'),
('102', 'Rajdhani Express', 'Delhi', 'Kolkata'),
('103', 'Shatabdi Express', 'Chennai', 'Bangalore');

CREATE TABLE reservations (
    pnr INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50),
    train_no VARCHAR(20),
    class_type VARCHAR(20),
    journey_date DATE,
    FOREIGN KEY (username) REFERENCES users(username),
    FOREIGN KEY (train_no) REFERENCES trains(train_no)
);
