-- Migration: Add status column to ordonnance table if it doesn't exist
-- Run this SQL in your MySQL database if the status column is missing

USE wellora;

-- Check if column exists before adding (MySQL syntax)
ALTER TABLE ordonnance ADD COLUMN status VARCHAR(50) DEFAULT 'active' AFTER diagnosis_code;

-- If you get an error saying the column already exists, that's fine - it means it's already there

