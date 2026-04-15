-- Migration to add clinical notes columns to consultation table
-- Run this SQL in your MySQL database to add missing columns

USE wellora;

-- Add missing clinical columns one by one (MySQL doesn't support IF NOT EXISTS for ALTER)

-- Check and add chief_complaint
-- This will fail if column exists, but we can ignore the error
-- For MySQL 8.0+, you can use:
ALTER TABLE consultation ADD COLUMN chief_complaint VARCHAR(255) AFTER appointment_mode;

-- If the above fails because column exists, run these individually:
-- ALTER TABLE consultation ADD COLUMN IF NOT EXISTS chief_complaint VARCHAR(255);

-- Add the rest of the columns
ALTER TABLE consultation ADD COLUMN bp_systolic INT;
ALTER TABLE consultation ADD COLUMN bp_diastolic INT;
ALTER TABLE consultation ADD COLUMN pulse INT;
ALTER TABLE consultation ADD COLUMN temperature DECIMAL(4,1);
ALTER TABLE consultation ADD COLUMN spo2 INT;
ALTER TABLE consultation ADD COLUMN follow_up_date DATE;
ALTER TABLE consultation ADD COLUMN follow_up_type VARCHAR(50);
ALTER TABLE consultation ADD COLUMN follow_up_priority VARCHAR(50);

-- If any of the above fail because columns already exist, that's okay - just continue