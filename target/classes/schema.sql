-- ============================================================
-- Wellora Health - Database Schema
-- Symfony equivalent: migrations/Version202502112314.php
-- Run this once in MySQL to create the tables
-- ============================================================

CREATE DATABASE IF NOT EXISTS wellora_health
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE wellora_health;

-- Replaces: Healthjournal entity (Doctrine creates this via migration)
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
