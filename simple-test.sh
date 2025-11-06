#!/bin/bash

# Simplest Notification Test - Gets token from .env automatically

echo "🔔 Simple Notification Test"
echo "============================"
echo ""

# Get token from .env file
cd /Users/taneeherng/Desktop/SMU/Y3S1/SPM/spmOrangle/frontend

if [ ! -f .env ]; then
    echo "❌ .env file not found in frontend directory"
    exit 1
fi

TOKEN=$(grep TEST_HR_AUTH_TOKEN .env | cut -d '=' -f2)

if [ -z "$TOKEN" ]; then
    echo "❌ TEST_HR_AUTH_TOKEN not found in .env"
    exit 1
fi

echo "✅ Found token in .env"
[[ ! "$TOKEN" =~ ^Bearer ]] && TOKEN="Bearer $TOKEN"

cd /Users/taneeherng/Desktop/SMU/Y3S1/SPM/spmOrangle
echo ""

# Get initial notification count
echo "📊 Checking current notifications..."
INITIAL=$(curl -s http://localhost:8080/api/notifications -H "Authorization: $TOKEN" 2>/dev/null)
INITIAL_COUNT=$(echo "$INITIAL" | jq 'length' 2>/dev/null || echo "0")

echo "   Current count: $INITIAL_COUNT"

if [ "$INITIAL_COUNT" != "0" ]; then
    echo ""
    echo "   Latest notification:"
    echo "$INITIAL" | jq -r '.[0] | "   • [\(.notificationType)] \(.subject)"' 2>/dev/null || echo "   (Could not parse)"
fi
echo ""

# Decode user ID from JWT token
echo "🔍 Decoding user ID from token..."
TOKEN_CLEAN=$(echo "$TOKEN" | sed 's/Bearer //' | sed 's/bearer //')
PAYLOAD=$(echo "$TOKEN_CLEAN" | cut -d '.' -f 2)

# Add padding if needed for proper base64 decoding
PADDING_LENGTH=$(( (4 - ${#PAYLOAD} % 4) % 4 ))
PADDED_PAYLOAD="$PAYLOAD"
for i in $(seq 1 $PADDING_LENGTH); do
    PADDED_PAYLOAD="${PADDED_PAYLOAD}="
done

USER_ID=$(echo "$PADDED_PAYLOAD" | base64 -d 2>/dev/null | jq -r '.sub' 2>/dev/null)

if [ -z "$USER_ID" ] || [ "$USER_ID" = "null" ]; then
    echo "❌ Could not decode user ID from token"
    echo "   Token might be invalid or expired"
    exit 1
fi

echo "✅ User ID: $USER_ID"
echo ""

# Create task
echo "📝 Creating task assigned to you..."
TIMESTAMP=$(date +%s)

# Get user's numeric ID from a task they're assigned to
EXISTING_TASKS=$(curl -s http://localhost:8080/api/tasks -H "Authorization: $TOKEN" 2>/dev/null)
USER_NUMERIC_ID=$(echo "$EXISTING_TASKS" | jq -r '.[0].assignedUsers[0].id // 1' 2>/dev/null)

echo "   Numeric user ID: $USER_NUMERIC_ID"

TASK_JSON=$(cat <<EOF
{
    "title": "Simple Test - $TIMESTAMP",
    "taskType": "FEATURE",
    "description": "Testing notifications",
    "status": "TODO",
    "priority": 1,
    "assignedUserIds": [$USER_NUMERIC_ID]
}
EOF
)

TASK_RESPONSE=$(curl -s -X POST http://localhost:8080/api/tasks \
    -H "Authorization: $TOKEN" \
    -H "Content-Type: application/json" \
    -d "$TASK_JSON" 2>/dev/null)

TASK_ID=$(echo "$TASK_RESPONSE" | jq -r '.id // .taskId' 2>/dev/null)

if [ -z "$TASK_ID" ] || [ "$TASK_ID" = "null" ]; then
    echo "❌ Failed to create task"
    echo "   Response: $TASK_RESPONSE"
    exit 1
fi

echo "✅ Task created: $TASK_ID"
echo ""

# Wait
echo "⏳ Waiting 10 seconds for RabbitMQ..."
for i in {10..1}; do
    echo -ne "   $i...\r"
    sleep 1
done
echo "   Done!   "
echo ""

# Check again
echo "📊 Checking notifications again..."
NEW=$(curl -s http://localhost:8080/api/notifications -H "Authorization: $TOKEN" 2>/dev/null)
NEW_COUNT=$(echo "$NEW" | jq 'length' 2>/dev/null || echo "0")

echo "   New count: $NEW_COUNT"
echo ""

if [ "$NEW_COUNT" -gt "$INITIAL_COUNT" ]; then
    DIFF=$((NEW_COUNT - INITIAL_COUNT))
    echo "🎉 SUCCESS! $DIFF new notification(s) created!"
    echo ""
    echo "Latest notification:"
    echo "$NEW" | jq -r '.[0] | "   Type: \(.notificationType)\n   Subject: \(.subject)\n   Message: \(.message)"' 2>/dev/null
    echo ""
    echo "✅ Notification system is WORKING!"
    echo ""
    echo "Run E2E tests now:"
    echo "   cd frontend && npx playwright test e2e/tests/nsy/"
else
    echo "❌ No new notifications created"
    echo ""
    echo "Debugging info:"
    echo "   1. Check if RabbitMQ is running: docker ps | grep rabbitmq"
    echo "   2. Check backend logs for '🔔 RECEIVED task notification message'"
    echo "   3. Check backend logs for RabbitMQ connection errors"
    echo ""
    echo "Try checking RabbitMQ:"
    echo "   docker ps | grep rabbitmq"
fi
