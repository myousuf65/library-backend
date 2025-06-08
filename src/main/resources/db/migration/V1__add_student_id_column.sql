-- Add student_id column to student table if it doesn't exist
ALTER TABLE student ADD COLUMN IF NOT EXISTS student_id VARCHAR(255);
