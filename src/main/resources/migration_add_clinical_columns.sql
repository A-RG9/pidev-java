-- Migration to add clinical notes columns to consultation table
-- Run this SQL in your MySQL database to add missing columns
-- This script is safe to run multiple times - it uses procedure to check column existence

USE wellora;

-- Add columns only if they don't exist (using MySQL procedure workaround)
DELIMITER //

-- Procedure to add column if not exists
CREATE PROCEDURE add_column_if_missing()
BEGIN
    DECLARE column_exists INT DEFAULT 0;
    
    -- Check chief_complaint
    SELECT COUNT(*) INTO column_exists FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'consultation' AND COLUMN_NAME = 'chief_complaint';
    IF column_exists = 0 THEN
        ALTER TABLE consultation ADD COLUMN chief_complaint VARCHAR(255);
    END IF;
    
    -- Check bp_systolic
    SELECT COUNT(*) INTO column_exists FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'consultation' AND COLUMN_NAME = 'bp_systolic';
    IF column_exists = 0 THEN
        ALTER TABLE consultation ADD COLUMN bp_systolic INT;
    END IF;
    
    -- Check bp_diastolic
    SELECT COUNT(*) INTO column_exists FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'consultation' AND COLUMN_NAME = 'bp_diastolic';
    IF column_exists = 0 THEN
        ALTER TABLE consultation ADD COLUMN bp_diastolic INT;
    END IF;
    
    -- Check pulse
    SELECT COUNT(*) INTO column_exists FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'consultation' AND COLUMN_NAME = 'pulse';
    IF column_exists = 0 THEN
        ALTER TABLE consultation ADD COLUMN pulse INT;
    END IF;
    
    -- Check temperature
    SELECT COUNT(*) INTO column_exists FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'consultation' AND COLUMN_NAME = 'temperature';
    IF column_exists = 0 THEN
        ALTER TABLE consultation ADD COLUMN temperature DECIMAL(4,1);
    END IF;
    
    -- Check spo2
    SELECT COUNT(*) INTO column_exists FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'consultation' AND COLUMN_NAME = 'spo2';
    IF column_exists = 0 THEN
        ALTER TABLE consultation ADD COLUMN spo2 INT;
    END IF;
    
    -- Check follow_up_date
    SELECT COUNT(*) INTO column_exists FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'consultation' AND COLUMN_NAME = 'follow_up_date';
    IF column_exists = 0 THEN
        ALTER TABLE consultation ADD COLUMN follow_up_date DATE;
    END IF;
    
    -- Check follow_up_type
    SELECT COUNT(*) INTO column_exists FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'consultation' AND COLUMN_NAME = 'follow_up_type';
    IF column_exists = 0 THEN
        ALTER TABLE consultation ADD COLUMN follow_up_type VARCHAR(50);
    END IF;
    
    -- Check follow_up_priority
    SELECT COUNT(*) INTO column_exists FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'consultation' AND COLUMN_NAME = 'follow_up_priority';
    IF column_exists = 0 THEN
        ALTER TABLE consultation ADD COLUMN follow_up_priority VARCHAR(50);
    END IF;
END//

DELIMITER ;

-- Execute the procedure
CALL add_column_if_missing();

-- Clean up the procedure
DROP PROCEDURE add_column_if_missing;

-- Done! All missing columns have been added.