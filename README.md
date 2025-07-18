Latest update: 
- Added Payroll and Payslip Buttons
- Added Functionality to both
- Added the database backend
- Updated the HR Dashboard to accomodate

To do:
- Clean up data to make sure correct ones are being used for all functionalities
- Clean up UI
- Adjust Login/Register to fix brand new user not being able to access anything
- Add admin dashboard to control the system
- Add Automated calculation of gross pay, deductions, and net pay based on predefined formulas and rules (For Admin?)
- Discuss multi-threading use
- 



DATABASE INFORMATION
---------------------
Database name:PayrollAssignment 
username:group18
password:group18


-- Table for login/authentication
CREATE TABLE USERS (
    USERNAME VARCHAR(50) PRIMARY KEY,
    PASSWORD VARCHAR(100),
    ROLE VARCHAR(20),
    STATUS VARCHAR(20)
);
 
-- Table for personal details
CREATE TABLE USERINFO (
    USERNAME VARCHAR(50) PRIMARY KEY,
    FIRSTNAME VARCHAR(50),
    LASTNAME VARCHAR(50),
    ICPASSPORT VARCHAR(50),
    FOREIGN KEY (USERNAME) REFERENCES USERS(USERNAME)
);

-- Table for payrolls (for now)
CREATE TABLE payroll (
    username VARCHAR(255) PRIMARY KEY,
    base_salary DOUBLE NOT NULL,
    bonus DOUBLE NOT NULL,
    epf DOUBLE NOT NULL,
    tax DOUBLE NOT NULL
);


-- Dummy data (Run this after creating the tables)
INSERT INTO payroll (username, base_salary, bonus, epf, tax) VALUES
('Amjad', 3000, 500, 200, 100),
('JJ', 2800, 300, 180, 90),
('DCOMASS', 3200, 400, 220, 110);
