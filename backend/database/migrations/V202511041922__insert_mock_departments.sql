INSERT INTO syncup.departments (name, parent_id)
VALUES
    ('Managing Director (Jack Sim)', NULL);

-- Directors
INSERT INTO syncup.departments (name, parent_id)
SELECT 'Sales Director (Derek Tan)', id FROM syncup.departments WHERE name = 'Managing Director (Jack Sim)';
INSERT INTO syncup.departments (name, parent_id)
SELECT 'Consultancy Division Director (Ernst Sim)', id FROM syncup.departments WHERE name = 'Managing Director (Jack Sim)';
INSERT INTO syncup.departments (name, parent_id)
SELECT 'System Solutioning Division Director (Eric Loh)', id FROM syncup.departments WHERE name = 'Managing Director (Jack Sim)';
INSERT INTO syncup.departments (name, parent_id)
SELECT 'Engineering Operation Division Director (Philip Lee)', id FROM syncup.departments WHERE name = 'Managing Director (Jack Sim)';
INSERT INTO syncup.departments (name, parent_id)
SELECT 'HR and Admin Director (Sally Loh)', id FROM syncup.departments WHERE name = 'Managing Director (Jack Sim)';
INSERT INTO syncup.departments (name, parent_id)
SELECT 'Finance Director (David Yap)', id FROM syncup.departments WHERE name = 'Managing Director (Jack Sim)';
INSERT INTO syncup.departments (name, parent_id)
SELECT 'IT Director (Peter Yap)', id FROM syncup.departments WHERE name = 'Managing Director (Jack Sim)';

-- Sales hierarchy
INSERT INTO syncup.departments (name, parent_id)
SELECT 'Sales Manager (5)', id FROM syncup.departments WHERE name = 'Sales Director (Derek Tan)';
INSERT INTO syncup.departments (name, parent_id)
SELECT 'Account Managers (60)', id FROM syncup.departments WHERE name = 'Sales Manager (5)';

-- Consultancy hierarchy
INSERT INTO syncup.departments (name, parent_id)
SELECT 'Consultant (30)', id FROM syncup.departments WHERE name = 'Consultancy Division Director (Ernst Sim)';

-- System Solutioning hierarchy
INSERT INTO syncup.departments (name, parent_id)
SELECT 'Developers (50)', id FROM syncup.departments WHERE name = 'System Solutioning Division Director (Eric Loh)';
INSERT INTO syncup.departments (name, parent_id)
SELECT 'Support Team (30)', id FROM syncup.departments WHERE name = 'System Solutioning Division Director (Eric Loh)';

-- Engineering Operations hierarchy
INSERT INTO syncup.departments (name, parent_id)
SELECT 'Senior Engineers (20)', id FROM syncup.departments WHERE name = 'Engineering Operation Division Director (Philip Lee)';
INSERT INTO syncup.departments (name, parent_id)
SELECT 'Junior Engineers (150)', id FROM syncup.departments WHERE name = 'Engineering Operation Division Director (Philip Lee)';
INSERT INTO syncup.departments (name, parent_id)
SELECT 'Call Centre (60)', id FROM syncup.departments WHERE name = 'Engineering Operation Division Director (Philip Lee)';
INSERT INTO syncup.departments (name, parent_id)
SELECT 'Operation Planning Team (30)', id FROM syncup.departments WHERE name = 'Engineering Operation Division Director (Philip Lee)';

-- HR and Admin hierarchy
INSERT INTO syncup.departments (name, parent_id)
SELECT 'HR Team (40)', id FROM syncup.departments WHERE name = 'HR and Admin Director (Sally Loh)';
INSERT INTO syncup.departments (name, parent_id)
SELECT 'L&D Team (20)', id FROM syncup.departments WHERE name = 'HR and Admin Director (Sally Loh)';
INSERT INTO syncup.departments (name, parent_id)
SELECT 'Admin Team (10)', id FROM syncup.departments WHERE name = 'HR and Admin Director (Sally Loh)';

-- Finance hierarchy
INSERT INTO syncup.departments (name, parent_id)
SELECT 'Finance Managers (5)', id FROM syncup.departments WHERE name = 'Finance Director (David Yap)';
INSERT INTO syncup.departments (name, parent_id)
SELECT 'Finance Executive (50)', id FROM syncup.departments WHERE name = 'Finance Managers (5)';

-- IT hierarchy
INSERT INTO syncup.departments (name, parent_id)
SELECT 'IT Team (30)', id FROM syncup.departments WHERE name = 'IT Director (Peter Yap)';
