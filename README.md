Remove if completed

- code cleaning


LIBRARY IMPORT
----------------

Locate 'jdatepicker-1.3.4.jar' to resolve library import





DATABASE INFORMATION
---------------------
Database name:PayrollAssignment 
username:group18
password:group18


-- Table for users
CREATE TABLE USERS (
    USERNAME VARCHAR(255) PRIMARY KEY,
    PASSWORD VARCHAR(255),
    ROLE VARCHAR(50),
    FIRSTNAME VARCHAR(255),
    LASTNAME VARCHAR(255),
    IC_PASSPORT VARCHAR(20),
    STATUS VARCHAR(50)
);

-- Table for subrole
CREATE TABLE EmployeeSubRoles (
    USERNAME VARCHAR(255) PRIMARY KEY,
    ROLE VARCHAR(20) NOT NULL,
    SUBROLE VARCHAR(50) NOT NULL,
    CONSTRAINT fk_user_subrole FOREIGN KEY (username)
        REFERENCES USERS(username)
        ON DELETE CASCADE
);
 

-- Table for payrolls
CREATE TABLE Payroll (
    ID INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    USERNAME VARCHAR(255),
    PAY_DATE DATE,
    BASE_SALARY DOUBLE,
    BONUS DOUBLE,
    EPF DOUBLE,
    SOCSO DOUBLE,
    TAX DOUBLE,
    ANNUALINCOME DOUBLE
);


-- Table for Payroll Configuration 
CREATE TABLE PayrollSettings (
    ID INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    EPF_RATE DOUBLE,
    SOCSO_RATE DOUBLE,
    TAX_RATE DOUBLE
);

-- Optional: Insert default payroll settings
INSERT INTO PayrollSettings (EPF_RATE, SOCSO_RATE, TAX_RATE)
VALUES (0.11, 0.01, 0.05);

