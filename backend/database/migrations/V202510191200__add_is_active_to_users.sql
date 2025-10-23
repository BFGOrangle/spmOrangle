ALTER TABLE syncup.users
ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT true;

CREATE INDEX idx_users_is_active ON syncup.users(is_active);
