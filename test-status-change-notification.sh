#!/bin/bash

# Test Status Change Notification Script
# This tests if status change notifications are being created in the database

echo "🔄 Testing Status Change Notifications"
echo "======================================"
echo ""

# Check if token is provided
if [ -z "$1" ]; then
    echo "❌ Error: Bearer token not provided"
    echo ""
    echo "Usage:"
    echo "  ./test-status-change-notification.sh YOUR_BEARER_TOKEN"
    exit 1
fi

TOKEN="$1"

# Add "Bearer " prefix if not present
if [[ ! "$TOKEN" =~ ^Bearer ]]; then
    TOKEN="Bearer $TOKEN"
fi

echo "✅ Token configured"
echo ""

# Step 1: Create a task
echo "📝 Step 1: Creating test task..."
TIMESTAMP=$(date +%s)

TASK_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST http://localhost:8080/api/tasks \
    -H "Authorization: $TOKEN" \
    -H "Content-Type: application/json" \
    -d "{
        \"title\": \"Status Change Test - $TIMESTAMP\",
        \"taskType\": \"FEATURE\",
        \"description\": \"Testing status change notifications\",
        \"status\": \"TODO\",
        \"priority\": 1
    }")

HTTP_CODE=$(echo "$TASK_RESPONSE" | tail -n 1)
TASK_BODY=$(echo "$TASK_RESPONSE" | sed '$d')

if [ "$HTTP_CODE" != "200" ] && [ "$HTTP_CODE" != "201" ]; then
    echo "❌ Failed to create task (HTTP $HTTP_CODE)"
    exit 1
fi

TASK_ID=$(echo "$TASK_BODY" | jq -r '.id // .taskId // empty' 2>/dev/null)
echo "✅ Task created: $TASK_ID"
echo ""

# Step 2: Get initial notification count
echo "📊 Step 2: Getting initial notification count..."
INITIAL_COUNT=$(curl -s -X GET http://localhost:8080/api/notifications \
    -H "Authorization: $TOKEN" | jq 'length' 2>/dev/null || echo "0")
echo "   Initial count: $INITIAL_COUNT"
echo ""

# Step 3: Change task status
echo "🔄 Step 3: Changing task status from TODO to IN_PROGRESS..."
UPDATE_RESPONSE=$(curl -s -w "\n%{http_code}" -X PATCH http://localhost:8080/api/tasks/$TASK_ID \
    -H "Authorization: $TOKEN" \
    -H "Content-Type: application/json" \
    -d '{"status": "IN_PROGRESS"}')

UPDATE_CODE=$(echo "$UPDATE_RESPONSE" | tail -n 1)
if [ "$UPDATE_CODE" != "200" ]; then
    echo "❌ Failed to update task (HTTP $UPDATE_CODE)"
    exit 1
fi

echo "✅ Task status changed"
echo ""

# Step 4: Wait for RabbitMQ
echo "⏳ Step 4: Waiting for RabbitMQ to process (10 seconds)..."
for i in {10..1}; do
    echo -ne "   $i seconds remaining...\r"
    sleep 1
done
echo "   ✅ Processing complete                    "
echo ""

# Step 5: Check for new notifications
echo "📬 Step 5: Checking for status change notifications..."
NOTIFICATIONS=$(curl -s -X GET http://localhost:8080/api/notifications \
    -H "Authorization: $TOKEN")

NEW_COUNT=$(echo "$NOTIFICATIONS" | jq 'length' 2>/dev/null || echo "0")
echo "   New count: $NEW_COUNT"

STATUS_NOTIFS=$(echo "$NOTIFICATIONS" | jq '[.[] | select(.notificationType == "STATUS_UPDATED" or (.subject // "" | contains("status")))]' 2>/dev/null)
STATUS_COUNT=$(echo "$STATUS_NOTIFS" | jq 'length' 2>/dev/null || echo "0")

echo ""
if [ "$STATUS_COUNT" -gt 0 ]; then
    echo "🎉 SUCCESS! Found $STATUS_COUNT status change notification(s)"
    echo ""
    echo "Status change notifications:"
    echo "$STATUS_NOTIFS" | jq -r '.[] | "  • [\(.notificationType)] \(.subject)"' 2>/dev/null
else
    echo "❌ FAILED: No status change notifications found"
    echo ""
    echo "💡 This means one of the following:"
    echo "   1. RabbitMQ is not running or not connected"
    echo "   2. RabbitMQ consumers are not listening"
    echo "   3. Notification creation is failing silently"
    echo "   4. Status change notifications only go to assigned users"
    echo ""
    echo "🔍 Debugging steps:"
    echo "   1. Check backend logs for '🔔 RECEIVED task notification message'"
    echo "   2. Check RabbitMQ management UI: http://localhost:15672"
    echo "   3. Check if RabbitMQ is running: docker ps | grep rabbitmq"
    echo "   4. Try assigning the task to yourself before changing status"
fi
echo ""

# Step 6: Try with assignment
echo "🎯 Step 6: Testing with task assignment..."
USER_ID=$(echo "$TOKEN" | sed 's/Bearer //' | cut -d '.' -f 2 | base64 -d 2>/dev/null | jq -r '.sub // .username // empty' 2>/dev/null)

if [ -n "$USER_ID" ]; then
    echo "   Assigning task to user: $USER_ID"

    ASSIGN_RESPONSE=$(curl -s -w "\n%{http_code}" -X PATCH http://localhost:8080/api/tasks/$TASK_ID \
        -H "Authorization: $TOKEN" \
        -H "Content-Type: application/json" \
        -d "{\"assignedUserIds\": [\"$USER_ID\"]}")

    ASSIGN_CODE=$(echo "$ASSIGN_RESPONSE" | tail -n 1)
    if [ "$ASSIGN_CODE" = "200" ]; then
        echo "   ✅ Task assigned"
        echo "   ⏳ Waiting 10 seconds..."
        sleep 10

        # Change status again
        curl -s -X PATCH http://localhost:8080/api/tasks/$TASK_ID \
            -H "Authorization: $TOKEN" \
            -H "Content-Type: application/json" \
            -d '{"status": "DONE"}' > /dev/null

        echo "   ✅ Changed status to DONE"
        echo "   ⏳ Waiting 10 seconds..."
        sleep 10

        # Check again
        NOTIFICATIONS=$(curl -s -X GET http://localhost:8080/api/notifications -H "Authorization: $TOKEN")
        STATUS_NOTIFS=$(echo "$NOTIFICATIONS" | jq '[.[] | select(.notificationType == "STATUS_UPDATED" or (.subject // "" | contains("status")))]' 2>/dev/null)
        STATUS_COUNT=$(echo "$STATUS_NOTIFS" | jq 'length' 2>/dev/null || echo "0")

        if [ "$STATUS_COUNT" -gt 0 ]; then
            echo ""
            echo "🎉 SUCCESS! Found $STATUS_COUNT status change notification(s) after assignment"
        else
            echo ""
            echo "❌ Still no status change notifications even after assignment"
            echo "   This indicates RabbitMQ or consumer issues"
        fi
    fi
fi

echo ""
echo "======================================"
echo "✅ Test completed!"
