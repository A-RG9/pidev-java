
-- Create the Examens (Lab Results) table for WellCare Connect
-- Run this SQL in your MySQL database to create the required table

CREATE DATABASE IF NOT EXISTS wellora;
USE wellora;

-- Create the consultation table
CREATE TABLE IF NOT EXISTS consultation (
    id INT PRIMARY KEY AUTO_INCREMENT,
    consultation_type VARCHAR(100),
    reason_for_visit VARCHAR(255),
    symptoms_description TEXT,
    date_consultation DATE,
    time_consultation TIME,
    duration INT,
    location VARCHAR(255),
    fee INT,
    status VARCHAR(50) DEFAULT 'pending',
    notes TEXT,
    subjective TEXT,
    objective TEXT,
    assessment TEXT,
    plan TEXT,
    diagnoses TEXT,
    appointment_mode VARCHAR(50) DEFAULT 'in-person',
    chief_complaint VARCHAR(255),
    bp_systolic INT,
    bp_diastolic INT,
    pulse INT,
    temperature DECIMAL(4,1),
    spo2 INT,
    follow_up_date DATE,
    follow_up_type VARCHAR(50),
    follow_up_priority VARCHAR(50),
    patient_id VARCHAR(36) DEFAULT NULL,
    patient_email VARCHAR(255),
    patient_first_name VARCHAR(100),
    patient_last_name VARCHAR(100),
    patient_phone VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_patient_id (patient_id)
);

-- Create the user table
CREATE TABLE IF NOT EXISTS user (
    id VARCHAR(36) PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    password VARCHAR(255),
    phone VARCHAR(20),
    date_of_birth DATE,
    gender VARCHAR(20),
    address TEXT,
    role VARCHAR(50) DEFAULT 'patient',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_role (role)
);

-- Create the Examens (Lab Results) table for WellCare Connect
CREATE TABLE IF NOT EXISTS examens (
    id INT PRIMARY KEY AUTO_INCREMENT,
    type_examen VARCHAR(255) NOT NULL,
    date_examen DATE,
    resultat TEXT,
    status VARCHAR(50),
    notes TEXT,
    nom_examen VARCHAR(255),
    date_realisation DATE,
    result_file VARCHAR(500),
    doctor_analysis TEXT,
    doctor_treatment TEXT,
    consultation_id INT DEFAULT 0,
    medecin_id INT DEFAULT 0,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create the ordonnance table
CREATE TABLE IF NOT EXISTS ordonnance (
    id INT PRIMARY KEY AUTO_INCREMENT,
    date_ordonnance DATE,
    medicament VARCHAR(255),
    dosage VARCHAR(255),
    forme VARCHAR(100),
    duree_traitement VARCHAR(100),
    instructions TEXT,
    frequency VARCHAR(100),
    diagnosis_code VARCHAR(50),
    status VARCHAR(50) DEFAULT 'active',
    consultation_id INT DEFAULT 0,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Insert sample data for testing
INSERT INTO examens (type_examen, nom_examen, date_examen, resultat, status, notes) VALUES
('Blood Test', 'Complete Blood Count (CBC)', DATE_SUB(CURDATE(), INTERVAL 5 DAY), 
 'hemoglobin: 14.5 g/dL\nWBC: 7,500 cells/μL\nPlatelets: 250,000/μL', 
 'COMPLETED', 'All values within normal range'),
('Lipid Panel', 'Cholesterol Test', DATE_SUB(CURDATE(), INTERVAL 10 DAY),
 'Total Cholesterol: 195 mg/dL\nHDL: 55 mg/dL\nLDL: 120 mg/dL',
 'COMPLETED', 'Borderline cholesterol levels, recommend dietary changes'),
('Urinalysis', 'Urine Test', DATE_SUB(CURDATE(), INTERVAL 3 DAY),
 'pH: 6.0\nSpecific Gravity: 1.015\nNo abnormalities detected',
 'COMPLETED', 'Kidney function normal');

CREATE TABLE IF NOT EXISTS healthjournal (
    id        INT AUTO_INCREMENT PRIMARY KEY,
    name      VARCHAR(255) NOT NULL,
    datedebut DATE         NOT NULL,
    datefin   DATE         NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Replaces: Healthentry entity
-- UNIQUE(date, journal_id) mirrors PHP: #[ORM\UniqueConstraint(columns: ['date', 'journal_id'])]
CREATE TABLE IF NOT EXISTS healthentry (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    date       DATE           NOT NULL,
    poids      DOUBLE         NOT NULL,
    glycemie   DOUBLE         NOT NULL,
    tension    VARCHAR(20)    NOT NULL,
    sommeil    INT            NOT NULL,
    journal_id INT            NOT NULL,
    UNIQUE KEY uq_date_journal (date, journal_id),
    FOREIGN KEY (journal_id) REFERENCES healthjournal(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Replaces: Symptom entity
CREATE TABLE IF NOT EXISTS symptom (
    id        INT AUTO_INCREMENT PRIMARY KEY,
    type      VARCHAR(100) NOT NULL,
    intensite INT          NOT NULL,
    zone      VARCHAR(100) NULL,
    entry_id  INT          NOT NULL,
    FOREIGN KEY (entry_id) REFERENCES healthentry(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

