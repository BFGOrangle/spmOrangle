-- Seed test notifications for user_id 11

-- Insert sample notifications
INSERT INTO syncup.notifications (
    author_id,
    target_id,
    notification_type,
    subject,
    message,
    read_status,
    dismissed_status,
    priority,
    link,
    metadata
) VALUES
-- A mention
(2, 11, 'MENTION', 'You were mentioned in a comment',
 'User 2 mentioned you in a project discussion.', false, false, 'MEDIUM',
 '/tasks/1', '{"comment_id":123,"project_id":42}'),

-- Task assigned
(3, 11, 'TASK_ASSIGNED', 'New Task Assigned',
 'User 3 assigned you the task "Update Project Documentation".', false, false, 'HIGH',
 '/tasks/1', '{"task_id":456,"due_date":"2025-10-20"}'),

-- Task completed
(11, 11, 'TASK_COMPLETED', 'Task Completed',
 'You have completed the task "Fix login bug".', true, false, 'LOW',
 '/tasks/1', '{"task_id":789,"completed_by":11}'),

-- Project invite
(4, 11, 'PROJECT_INVITE', 'Project Collaboration Invite',
 'User 4 invited you to join the project "New Analytics Dashboard".', false, false, 'HIGH',
 '/tasks/1', '{"project_id":88,"invited_by":4}');

-- Now seed notification channels for each notification
-- (using currval to capture the last inserted ID)

-- Mention channels
INSERT INTO syncup.notification_channels (notification_id, channel)
VALUES
((SELECT notification_id FROM syncup.notifications WHERE subject = 'You were mentioned in a comment'), 'IN_APP'),
((SELECT notification_id FROM syncup.notifications WHERE subject = 'You were mentioned in a comment'), 'EMAIL');

-- Task assigned channels
INSERT INTO syncup.notification_channels (notification_id, channel)
VALUES
((SELECT notification_id FROM syncup.notifications WHERE subject = 'New Task Assigned'), 'IN_APP'),
((SELECT notification_id FROM syncup.notifications WHERE subject = 'New Task Assigned'), 'EMAIL'),
((SELECT notification_id FROM syncup.notifications WHERE subject = 'New Task Assigned'), 'SMS');

-- Task completed channels
INSERT INTO syncup.notification_channels (notification_id, channel)
VALUES
((SELECT notification_id FROM syncup.notifications WHERE subject = 'Task Completed'), 'IN_APP');

-- Project invite channels
INSERT INTO syncup.notification_channels (notification_id, channel)
VALUES
((SELECT notification_id FROM syncup.notifications WHERE subject = 'Project Collaboration Invite'), 'IN_APP'),
((SELECT notification_id FROM syncup.notifications WHERE subject = 'Project Collaboration Invite'), 'EMAIL');
