-- Create projects
INSERT INTO syncup.projects (name, owner_id, description, delete_ind, created_by, updated_by)
VALUES
  ('Project Apollo', 1, 'Research and development of new platform', false, 1, 1),
  ('Project Orion',  2, 'Customer engagement and outreach project', false, 2, 2),
  ('Project Nebula', 3, 'Internal tooling and productivity upgrade', false, 3, 3),
  ('Project Atlas',  4, 'Company-wide knowledge base and wiki', false, 4, 4),
  ('Project Titan',  5, 'New support ticketing and chat system', false, 5, 5);

-- Add project members (collaborators)
INSERT INTO syncup.project_members (project_id, user_id, added_by)
VALUES
  (1, 2, 1),  -- Bob joins Apollo
  (1, 3, 1),  -- Charlie joins Apollo
  (1, 4, 1),  -- Dana joins Apollo

  (2, 1, 2),  -- Alice joins Orion
  (2, 3, 2),  -- Charlie joins Orion
  (2, 5, 2),  -- Ethan joins Orion

  (3, 1, 3),  -- Alice joins Nebula
  (3, 2, 3),  -- Bob joins Nebula
  (3, 6, 3),  -- Fiona joins Nebula

  (4, 1, 4),  -- Alice joins Atlas
  (4, 7, 4),  -- George joins Atlas
  (4, 8, 4),  -- Hannah joins Atlas

  (5, 1, 5),  -- Alice joins Titan
  (5, 2, 5),  -- Bob joins Titan
  (5, 9, 5);  -- Ian joins Titan