j-- CREATE DATABASE script
CREATE DATABASE IF NOT EXISTS lifelink_db;
USE lifelink_db;

-- 1. users table
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(256) NOT NULL, -- To store hashed password
    email VARCHAR(100) UNIQUE NOT NULL,
    role VARCHAR(20) NOT NULL -- ADMIN, DONOR, SEEKER
);

-- 2. donors table
CREATE TABLE IF NOT EXISTS donors (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    name VARCHAR(100) NOT NULL,
    age INT NOT NULL,
    weight DOUBLE NOT NULL, -- weight in kg
    blood_group VARCHAR(5) NOT NULL,
    city VARCHAR(50) NOT NULL,
    last_donation_date DATE DEFAULT NULL,
    is_available BOOLEAN DEFAULT TRUE NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 3. hospitals table
CREATE TABLE IF NOT EXISTS hospitals (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    address VARCHAR(255) NOT NULL,
    contact_no VARCHAR(20) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL
);

-- 4. blood_requests table
CREATE TABLE IF NOT EXISTS blood_requests (
    id INT AUTO_INCREMENT PRIMARY KEY,
    seeker_id INT DEFAULT NULL, -- Links to user table (SEEKER role)
    hospital_id INT DEFAULT NULL, -- Links to hospital table
    blood_group VARCHAR(5) NOT NULL,
    units_requested INT NOT NULL,
    priority VARCHAR(20) NOT NULL, -- Critical, High, Normal
    request_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL, -- PENDING, APPROVED, REJECTED, COMPLETED
    FOREIGN KEY (seeker_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (hospital_id) REFERENCES hospitals(id) ON DELETE SET NULL
);

-- 5. donation_history table
CREATE TABLE IF NOT EXISTS donation_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    donor_id INT NOT NULL,
    donation_date DATE NOT NULL,
    units_donated INT NOT NULL,
    location VARCHAR(100) NOT NULL,
    FOREIGN KEY (donor_id) REFERENCES donors(id) ON DELETE CASCADE
);

-- SAMPLE SEED DATA
-- Default Hashed password for adminpassword is fc3298d2643a60a77519a4e4785461c28c89ad0e0600f91753c15354964e526a
-- Default Hashed password for donor1234 is f39de664e107df687d896173003058a6237fffa9b47e24ef5476a669ff8c9ef3
-- Default Hashed password for seeker123 is ddf42719a9b708fa8fa7d3d1964177d6118b6e3f4e24ef54766f7f6ec4a3903a
INSERT INTO users (username, password, email, role) VALUES 
('admin', 'fc3298d2643a60a77519a4e4785461c28c89ad0e0600f91753c15354964e526a', 'admin@lifelink.com', 'ADMIN'),
('donor1', 'f39de664e107df687d896173003058a6237fffa9b47e24ef5476a669ff8c9ef3', 'donor1@gmail.com', 'DONOR'),
('seeker1', 'ddf42719a9b708fa8fa7d3d1964177d6118b6e3f4e24ef54766f7f6ec4a3903a', 'seeker1@gmail.com', 'SEEKER');

INSERT INTO donors (user_id, name, age, weight, blood_group, city, last_donation_date, is_available) VALUES 
(2, 'Robert Downey', 35, 75.5, 'O+', 'Chicago', '2026-01-10', TRUE);

INSERT INTO hospitals (name, address, contact_no, email) VALUES 
('St. Mary Hospital', '456 Healthcare Blvd, Chicago', '312-555-0199', 'info@stmary.org');

INSERT INTO blood_requests (seeker_id, hospital_id, blood_group, units_requested, priority, request_date, status) VALUES 
(3, 1, 'O+', 2, 'High', '2026-06-05', 'PENDING');

INSERT INTO donation_history (donor_id, donation_date, units_donated, location) VALUES 
(1, '2026-01-10', 1, 'St. Mary Hospital Blood Drive');
