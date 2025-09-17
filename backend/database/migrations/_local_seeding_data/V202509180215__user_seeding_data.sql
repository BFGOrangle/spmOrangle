INSERT INTO syncup.user_types (name)
VALUES 
  ('Admin'),
  ('Manager'),
  ('Staff'),
  ('Intern'),
  ('Support');

-- 2) Seed users
INSERT INTO syncup.users (full_name, email, role_type, cognito_sub)
VALUES
  ('Alice Johnson',  'alice.johnson@example.com',  1, gen_random_uuid()),
  ('Bob Smith',      'bob.smith@example.com',      2, gen_random_uuid()),
  ('Charlie Lee',    'charlie.lee@example.com',    2, gen_random_uuid()),
  ('Dana Williams',  'dana.williams@example.com',  3, gen_random_uuid()),
  ('Ethan Brown',    'ethan.brown@example.com',    3, gen_random_uuid()),
  ('Fiona Miller',   'fiona.miller@example.com',   4, gen_random_uuid()),
  ('George Harris',  'george.harris@example.com',  5, gen_random_uuid()),
  ('Hannah Clark',   'hannah.clark@example.com',   5, gen_random_uuid()),
  ('Ian Thompson',   'ian.thompson@example.com',   1, gen_random_uuid()),
  ('Julia Adams',    'julia.adams@example.com',    3, gen_random_uuid());