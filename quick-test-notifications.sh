#!/bin/bash

# Quick Notification Test - Just creates a task and checks if notification appears

echo "🔔 Quick Notification Test"
echo "=========================="
echo ""

if [ -z "$1" ]; then
    echo "❌ Usage: ./quick-test-notifications.sh YOUR_BEARER_TOKEN"
    exit 1
fi

TOKEN="$1"
[[ ! "$TOKEN" =~ ^Bearer ]] && TOKEN="Bearer $TOKEN"

# Get user ID
USER_ID=$(echo "$TOKEN" | sed 's/Bearer //' | cut -d '.' -f 2 | base64 -d 2>/dev/null | jq -r '.sub // .username // empty' 2>/dev/null)

if [ -z "$USER_ID" ]; then
    echo "❌ Could not extract user ID from token"
    exit 1
fi

echo "✅ Testing with user ID: $USER_ID"
echo ""

# Get initial notification count
INITIAL=$(curl -s http://localhost:8080/api/notifications -H "Authorization: $TOKEN" | jq 'length' 2>/dev/null || echo "0")
echo "📊 Current notifications: $INITIAL"
echo ""

# Create task with assignment
echo "📝 Creating task..."
TIMESTAMP=$(date +%s)
TASK_RESPONSE=$(curl -s -X POST http://localhost:8080/api/tasks \
    -H "Authorization: $TOKEN" \
    -H "Content-Type: application/json" \
    -d "{
        \"title\": \"Quick Test - $TIMESTAMP\",
        \"taskType\": \"FEATURE\",
        \"description\": \"Testing notifications\",
        \"status\": \"TODO\",
        \"priority\": 1,
        \"assignedUserIds\": [\"$USER_ID\"]
    }")

TASK_ID=$(echo "$TASK_RESPONSE" | jq -r '.id // .taskId // empty' 2>/dev/null)

if [ -z "$TASK_ID" ]; then
    echo "❌ Failed to create task"
    echo "Response: $TASK_RESPONSE"
    exit 1
fi

echo "✅ Task created: $TASK_ID"
echo ""

# Wait for RabbitMQ
echo "⏳ Waiting 10 seconds for RabbitMQ..."
sleep 10

# Check for new notifications
NEW=$(curl -s http://localhost:8080/api/notifications -H "Authorization: $TOKEN" | jq 'length' 2>/dev/null || echo "0")
echo ""
echo "📊 New notification count: $NEW"
echo ""

if [ "$NEW" -gt "$INITIAL" ]; then
    DIFF=$((NEW - INITIAL))
    echo "🎉 SUCCESS! $DIFF new notification(s) created!"
    echo ""

    # Show the new notification
    curl -s http://localhost:8080/api/notifications -H "Authorization: $TOKEN" | \
        jq -r ".[0] | \"📬 Latest notification:\n   Type: \(.notificationType)\n   Subject: \(.subject)\n   Message: \(.message)\""

    echo ""
    echo "✅ Notification system is WORKING!"
else
    echo "❌ FAILED: No new notifications created"
    echo ""
    echo "Possible issues:"
    echo "  1. RabbitMQ not running"
    echo "  2. Backend not connected to RabbitMQ"
    echo "  3. Check backend logs for errors"
fi
