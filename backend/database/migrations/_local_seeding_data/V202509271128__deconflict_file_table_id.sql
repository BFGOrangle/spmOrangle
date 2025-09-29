-- Reset the identity sequence to prevent duplicate key violations
SELECT setval(pg_get_serial_sequence('syncup.files', 'id'), COALESCE(MAX(id), 1)) FROM syncup.files;
