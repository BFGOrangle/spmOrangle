# Notification System Verification Guide

Your notification service IS fully implemented! Use this guide to verify why tests show 0 notifications.

## Step 1: Check if RabbitMQ is Running

```bash
# Option 1: If using Docker
docker ps | grep rabbitmq

# Option 2: Check RabbitMQ directly
rabbitmq-diagnostics status

# Option 3: Check if backend has RabbitMQ connection errors
# Look in backend logs for:
# - "🔔 RECEIVED task notification message"
# - "Creating X in-app notifications"
# - Any RabbitMQ connection errors
```

**If RabbitMQ is NOT running**, start it:
```bash
# Using Docker
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management

# OR if you have it installed
rabbitmq-server
```

## Step 2: Check Database for Existing Notifications

```sql
-- Connect to your database and run:
SELECT
    notification_id,
    recipient_id,
    notification_type,
    subject,
    created_at
FROM syncup.notifications
ORDER BY created_at DESC
LIMIT 20;

-- Check total count
SELECT COUNT(*) FROM syncup.notifications;

-- Check for your HR user specifically
SELECT * FROM syncup.notifications
WHERE recipient_id = 'YOUR_HR_USER_ID'  -- Replace with actual user ID
ORDER BY created_at DESC;
```

## Step 3: Check What User ID the Test is Using

The E2E test uses `TEST_HR_AUTH_TOKEN`. Let's decode it to see which user:

```bash
# In your terminal
cd frontend

# Install jwt-decode if needed
npm install -g jwt-cli

# Decode the token
jwt decode $(grep TEST_HR_AUTH_TOKEN .env | cut -d '=' -f2)

# Or use online: https://jwt.io/
# Paste your TEST_HR_AUTH_TOKEN value
```

Look for the `sub` field - that's your user ID. Notifications will only appear if that user is involved in tasks.

## Step 4: Create Test Notifications Manually

If database is empty, create some test data:

```sql
-- Get your HR user ID first
SELECT user_id, email, full_name
FROM syncup.users
WHERE email = 'qyprojects@gmail.com';

-- Insert test notifications (replace 'YOUR_USER_ID' with actual ID)
INSERT INTO syncup.notifications (
    notification_id,
    recipient_id,
    author_id,
    notification_type,
    subject,
    message,
    link,
    read_status,
    dismissed_status,
    channel,
    created_at,
    updated_at
) VALUES
(
    'test-notif-1',
    'YOUR_USER_ID',  -- Replace with HR user ID
    'system',
    'TASK_ASSIGNED',
    'You were assigned to a task',
    'Task "Complete Project Report" has been assigned to you',
    '/tasks/1',
    false,
    false,
    'IN_APP',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
),
(
    'test-notif-2',
    'YOUR_USER_ID',  -- Replace with HR user ID
    'system',
    'STATUS_UPDATED',
    'Task status changed',
    'Task "Review Documentation" status changed from To Do to In Progress',
    '/tasks/2',
    false,
    false,
    'IN_APP',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);
```

## Step 5: Trigger Real Notifications via API

Instead of manual database inserts, trigger the actual notification system:

```bash
# 1. Get your auth token
export TOKEN="Bearer $(grep TEST_HR_AUTH_TOKEN frontend/.env | cut -d '=' -f2)"

# 2. Create a task and assign it to your HR user
curl -X POST http://localhost:8080/api/tasks \
  -H "Authorization: $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Test Task for Notifications",
    "description": "Testing notification system",
    "status": "TODO",
    "assignedUserIds": ["YOUR_USER_ID"]
  }'

# 3. Wait a few seconds for RabbitMQ to process
sleep 5

# 4. Check notifications
curl -X GET http://localhost:8080/api/notifications \
  -H "Authorization: $TOKEN"
```

## Step 6: Check Backend Logs

Look for notification-related logs:

```bash
# In your backend terminal/logs
grep "notification" logs/application.log

# Look for:
# - "🔔 RECEIVED task notification message"
# - "Creating X in-app notifications"
# - "Notification saved successfully"
# - Any errors related to RabbitMQ or database
```

## Step 7: Run Tests Again

After verifying notifications exist:

```bash
cd frontend
npx playwright test e2e/tests/nsy/notifications-simple.spec.ts --headed
```

You should now see:
- ✅ More tests passing
- ✅ Notification count > 0
- ✅ Tests that were skipping now running

## Common Issues & Solutions

### Issue 1: RabbitMQ Not Running
**Symptom**: No notifications created even after task events
**Solution**: Start RabbitMQ (see Step 1)

### Issue 2: Wrong User ID
**Symptom**: Notifications exist but not for test user
**Solution**: Assign tasks to the HR user, or update TEST_HR_AUTH_TOKEN

### Issue 3: Notifications Expired/Cleaned Up
**Symptom**: Old notifications were deleted
**Solution**: Create new test data (Step 4 or 5)

### Issue 4: Database Connection Issues
**Symptom**: Backend logs show database errors
**Solution**: Check database is running and credentials are correct

## Expected Results

Once working, you should see:
- 16-20+ tests passing (instead of just 16)
- Notification count > 0
- Tests for sorting, marking as read, etc. all passing
- 0 tests failing (some may still skip for unimplemented features like comments/reminders)

## Still Having Issues?

Check:
1. Backend is running on port 8080
2. Database is accessible
3. RabbitMQ is running and backend can connect
4. No errors in backend logs
5. User ID in token matches user who should receive notifications
