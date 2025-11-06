#!/bin/bash

# Comprehensive Notification System Diagnostic Script
# This script helps diagnose why notifications aren't appearing in the database

echo "🔍 Notification System Diagnostic Tool"
echo "========================================"
echo ""

# Check if token is provided
if [ -z "$1" ]; then
    echo "❌ Error: Bearer token not provided"
    echo ""
    echo "Usage:"
    echo "  ./diagnose-notifications.sh YOUR_BEARER_TOKEN"
    exit 1
fi

TOKEN="$1"
if [[ ! "$TOKEN" =~ ^Bearer ]]; then
    TOKEN="Bearer $TOKEN"
fi

echo "✅ Token configured"
echo ""

# ============================================
# SECTION 1: Check RabbitMQ Status
# ============================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "1️⃣  CHECKING RABBITMQ STATUS"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Check if RabbitMQ is running (Docker)
RABBITMQ_CONTAINER=$(docker ps --filter "name=rabbitmq" --format "{{.Names}}" 2>/dev/null | head -1)
if [ -n "$RABBITMQ_CONTAINER" ]; then
    echo "✅ RabbitMQ Docker container found: $RABBITMQ_CONTAINER"
    docker ps --filter "name=rabbitmq" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
else
    echo "⚠️  No RabbitMQ Docker container found"
    echo "   Checking if RabbitMQ is running locally..."

    # Check if RabbitMQ is running locally
    RABBITMQ_LOCAL=$(lsof -ti:5672 2>/dev/null || echo "")
    if [ -n "$RABBITMQ_LOCAL" ]; then
        echo "✅ RabbitMQ is running locally on port 5672 (PID: $RABBITMQ_LOCAL)"
    else
        echo "❌ RabbitMQ is NOT running"
        echo ""
        echo "💡 To start RabbitMQ:"
        echo "   Option 1 (Docker): docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management"
        echo "   Option 2 (Local): rabbitmq-server"
        echo ""
        echo "⚠️  Cannot continue without RabbitMQ. Please start it first."
        exit 1
    fi
fi

# Check RabbitMQ Management UI
echo ""
echo "🌐 Checking RabbitMQ Management UI..."
MGMT_CHECK=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:15672 2>/dev/null || echo "000")
if [ "$MGMT_CHECK" = "200" ] || [ "$MGMT_CHECK" = "401" ]; then
    echo "✅ RabbitMQ Management UI is accessible: http://localhost:15672"
    echo "   Default credentials: guest/guest or admin/admin"
else
    echo "⚠️  RabbitMQ Management UI not accessible (may not be enabled)"
fi

echo ""

# ============================================
# SECTION 2: Check Backend Status
# ============================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "2️⃣  CHECKING BACKEND STATUS"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

BACKEND_CHECK=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/notifications/unread-count 2>/dev/null || echo "000")
if [ "$BACKEND_CHECK" = "000" ]; then
    echo "❌ Backend is NOT running on port 8080"
    echo "   Please start the backend first"
    exit 1
else
    echo "✅ Backend is running (HTTP $BACKEND_CHECK)"
fi

# Get user ID
USER_ID=$(echo "$TOKEN" | sed 's/Bearer //' | cut -d '.' -f 2 | base64 -d 2>/dev/null | jq -r '.sub // .username // empty' 2>/dev/null)
if [ -n "$USER_ID" ]; then
    echo "✅ User ID from token: $USER_ID"
else
    echo "⚠️  Could not extract user ID from token"
fi

echo ""

# ============================================
# SECTION 3: Check Current Notifications
# ============================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "3️⃣  CURRENT NOTIFICATION STATUS"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

NOTIFICATIONS=$(curl -s -X GET http://localhost:8080/api/notifications -H "Authorization: $TOKEN")
NOTIF_COUNT=$(echo "$NOTIFICATIONS" | jq 'length' 2>/dev/null || echo "0")
echo "📊 Total notifications in database: $NOTIF_COUNT"

if [ "$NOTIF_COUNT" -gt 0 ]; then
    echo ""
    echo "Notification types breakdown:"
    echo "$NOTIFICATIONS" | jq -r 'group_by(.notificationType) | map({type: .[0].notificationType, count: length}) | .[] | "  • \(.type): \(.count)"' 2>/dev/null || echo "  (Could not parse)"
fi

echo ""

# ============================================
# SECTION 4: Test Notification Flow
# ============================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "4️⃣  TESTING NOTIFICATION FLOW"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

echo "📝 Creating test task with assignment..."
TIMESTAMP=$(date +%s)

if [ -z "$USER_ID" ]; then
    echo "❌ Cannot test without user ID"
    exit 1
fi

TASK_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST http://localhost:8080/api/tasks \
    -H "Authorization: $TOKEN" \
    -H "Content-Type: application/json" \
    -d "{
        \"title\": \"Diagnostic Test - $TIMESTAMP\",
        \"taskType\": \"FEATURE\",
        \"description\": \"Testing notification system\",
        \"status\": \"TODO\",
        \"priority\": 1,
        \"assignedUserIds\": [\"$USER_ID\"]
    }")

HTTP_CODE=$(echo "$TASK_RESPONSE" | tail -n 1)
TASK_BODY=$(echo "$TASK_RESPONSE" | sed '$d')

if [ "$HTTP_CODE" != "200" ] && [ "$HTTP_CODE" != "201" ]; then
    echo "❌ Failed to create task (HTTP $HTTP_CODE)"
    echo "   Response: $TASK_BODY"
    exit 1
fi

TASK_ID=$(echo "$TASK_BODY" | jq -r '.id // .taskId // empty' 2>/dev/null)
echo "✅ Task created: $TASK_ID"

# Get initial count
INITIAL_NOTIFS=$(curl -s -X GET http://localhost:8080/api/notifications -H "Authorization: $TOKEN")
INITIAL_COUNT=$(echo "$INITIAL_NOTIFS" | jq 'length' 2>/dev/null || echo "0")

echo "📊 Notifications before: $INITIAL_COUNT"
echo ""

# Wait for RabbitMQ
echo "⏳ Waiting 10 seconds for RabbitMQ to process..."
sleep 10

# Check for new notifications
NEW_NOTIFS=$(curl -s -X GET http://localhost:8080/api/notifications -H "Authorization: $TOKEN")
NEW_COUNT=$(echo "$NEW_NOTIFS" | jq 'length' 2>/dev/null || echo "0")

echo "📊 Notifications after: $NEW_COUNT"
echo ""

if [ "$NEW_COUNT" -gt "$INITIAL_COUNT" ]; then
    DIFF=$((NEW_COUNT - INITIAL_COUNT))
    echo "🎉 SUCCESS! $DIFF new notification(s) created!"
    echo ""
    echo "New notifications:"
    echo "$NEW_NOTIFS" | jq -r ".[0:$DIFF] | .[] | \"  • [\(.notificationType)] \(.subject)\"" 2>/dev/null
    echo ""
    echo "✅ RabbitMQ consumers ARE working and saving to database!"
else
    echo "❌ FAILURE: No new notifications created"
    echo ""
    echo "🔍 This means one of the following:"
    echo "   1. RabbitMQ consumers are not receiving messages"
    echo "   2. Backend is not connected to RabbitMQ"
    echo "   3. Consumers are failing to save to database"
    echo ""
    echo "📋 DEBUGGING STEPS:"
    echo ""
    echo "   1. Check backend logs for errors:"
    echo "      - Look for '🔔 RECEIVED task notification message'"
    echo "      - Look for RabbitMQ connection errors"
    echo "      - Look for database errors"
    echo ""
    echo "   2. Check RabbitMQ queues:"
    echo "      - Visit http://localhost:15672"
    echo "      - Go to 'Queues' tab"
    echo "      - Check if 'task.notification.queue' exists and has messages"
    echo ""
    echo "   3. Check backend configuration:"
    echo "      - Verify RABBITMQ_HOST, RABBITMQ_PORT, RABBITMQ_USERNAME, RABBITMQ_PASSWORD"
    echo "      - Check if consumers are enabled (@RabbitListener)"
    echo ""
    echo "   4. Check database connection:"
    echo "      - Verify notifications table exists"
    echo "      - Check database permissions"
fi

echo ""

# ============================================
# SECTION 5: Test Status Change
# ============================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "5️⃣  TESTING STATUS CHANGE NOTIFICATION"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

echo "🔄 Changing task status from TODO to IN_PROGRESS..."
UPDATE_RESPONSE=$(curl -s -w "\n%{http_code}" -X PATCH http://localhost:8080/api/tasks/$TASK_ID \
    -H "Authorization: $TOKEN" \
    -H "Content-Type: application/json" \
    -d '{"status": "IN_PROGRESS"}')

UPDATE_CODE=$(echo "$UPDATE_RESPONSE" | tail -n 1)
if [ "$UPDATE_CODE" = "200" ]; then
    echo "✅ Task status updated"

    BEFORE_STATUS_CHANGE=$(echo "$NEW_NOTIFS" | jq '[.[] | select(.notificationType == "STATUS_UPDATED")] | length' 2>/dev/null || echo "0")

    echo "⏳ Waiting 10 seconds for RabbitMQ..."
    sleep 10

    AFTER_NOTIFS=$(curl -s -X GET http://localhost:8080/api/notifications -H "Authorization: $TOKEN")
    AFTER_STATUS_CHANGE=$(echo "$AFTER_NOTIFS" | jq '[.[] | select(.notificationType == "STATUS_UPDATED")] | length' 2>/dev/null || echo "0")

    echo "📊 Status change notifications: $AFTER_STATUS_CHANGE (was: $BEFORE_STATUS_CHANGE)"

    if [ "$AFTER_STATUS_CHANGE" -gt "$BEFORE_STATUS_CHANGE" ]; then
        echo "🎉 SUCCESS! Status change notification created!"
        echo "$AFTER_NOTIFS" | jq -r '[.[] | select(.notificationType == "STATUS_UPDATED")] | .[0] | "  • Subject: \(.subject)\n  • Message: \(.message)"' 2>/dev/null
    else
        echo "❌ No status change notification created"
        echo "   This means STATUS_UPDATED notifications are not working"
    fi
else
    echo "❌ Failed to update task status (HTTP $UPDATE_CODE)"
fi

echo ""

# ============================================
# SECTION 6: Summary
# ============================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📊 DIAGNOSTIC SUMMARY"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

FINAL_NOTIFS=$(curl -s -X GET http://localhost:8080/api/notifications -H "Authorization: $TOKEN")
FINAL_COUNT=$(echo "$FINAL_NOTIFS" | jq 'length' 2>/dev/null || echo "0")

echo "Final notification count: $FINAL_COUNT (started with $INITIAL_COUNT)"
echo ""

if [ "$FINAL_COUNT" -gt "$INITIAL_COUNT" ]; then
    echo "✅ DIAGNOSIS: Notification system IS WORKING!"
    echo ""
    echo "📝 Next steps:"
    echo "   1. Run E2E tests: cd frontend && npx playwright test e2e/tests/nsy/"
    echo "   2. Generate more test data by:"
    echo "      - Creating tasks with assignments"
    echo "      - Changing task statuses"
    echo "      - Adding/removing assignees"
    echo "      - Adding comments (if implemented)"
else
    echo "❌ DIAGNOSIS: Notification system NOT WORKING"
    echo ""
    echo "🔧 Required actions:"
    echo "   1. Check backend logs for errors"
    echo "   2. Verify RabbitMQ connection in application-local.yml"
    echo "   3. Ensure RabbitMQ queues are properly configured"
    echo "   4. Check if @EnableRabbit is present in Spring Boot application"
fi

echo ""
echo "======================================"
echo "✅ Diagnostic complete!"
echo "======================================"
