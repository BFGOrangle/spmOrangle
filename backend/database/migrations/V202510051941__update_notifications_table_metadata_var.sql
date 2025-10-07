-- Fix metadata column type mismatch
ALTER TABLE notifications ALTER COLUMN metadata TYPE TEXT;
