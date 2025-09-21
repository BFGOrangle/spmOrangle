CREATE FUNCTION log_update()
RETURNS TRIGGER AS $$
BEGIN
  INSERT INTO syncup.update_logs (table_type, row_id, updated_by)
  VALUES (TG_TABLE_NAME, NEW.id, NEW.updated_by);

  RETURN NEW;
END;
$$ LANGUAGE plpgsql;
