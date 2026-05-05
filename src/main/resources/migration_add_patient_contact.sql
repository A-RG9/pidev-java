-- Add patient contact columns to consultation table
-- Run this if your consultation table already exists without these columns

ALTER TABLE consultation
ADD COLUMN patient_email VARCHAR(255) NULL,
ADD COLUMN patient_first_name VARCHAR(100) NULL,
ADD COLUMN patient_last_name VARCHAR(100) NULL,
ADD COLUMN patient_phone VARCHAR(20) NULL;
