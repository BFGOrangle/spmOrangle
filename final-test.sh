#!/bin/bash

echo "🔔 Final Notification Test"
echo "=========================="
echo ""

# Get token
cd /Users/taneeherng/Desktop/SMU/Y3S1/SPM/spmOrangle/frontend
TOKEN=$(grep TEST_HR_AUTH_TOKEN .env | cut -d '=' -f2)
[[ ! "$TOKEN" =~ ^Bearer ]] && TOKEN="Bearer $TOKEN"

cd /Users/taneeherng/Desktop/SMU/Y3S1/SPM/spmOrangle

# Check current notifications
echo "📊 Current notifications..."
INITIAL=$(curl -s http://localhost:8080/api/notifications -H "Authorization: $TOKEN")
INITIAL_COUNT=$(echo "$INITIAL" | jq 'length' 2>/dev/null || echo "0")
echo "   Count: $INITIAL_COUNT"
echo ""

# Create task with numeric user ID 1 (most common test user ID)
echo "📝 Creating task..."
TIMESTAMP=$(date +%s)

curl -s -X POST http://localhost:8080/api/tasks \
    -H "Authorization: $TOKEN" \
    -H "Content-Type: application/json" \
    -d "{
        \"title\": \"Final Test - $TIMESTAMP\",
        \"taskType\": \"FEATURE\",
        \"description\": \"Testing\",
        \"status\": \"TODO\",
        \"priority\": 1,
        \"assignedUserIds\": [1]
    }" > /tmp/task_response.json

TASK_ID=$(cat /tmp/task_response.json | jq -r '.id // .taskId' 2>/dev/null)

if [ -z "$TASK_ID" ] || [ "$TASK_ID" = "null" ]; then
    echo "❌ Task creation failed"
    echo "   Response:"
    cat /tmp/task_response.json | jq '.' 2>/dev/null || cat /tmp/task_response.json
    echo ""
    echo "Trying with projectId=1..."

    curl -s -X POST http://localhost:8080/api/tasks \
        -H "Authorization: $TOKEN" \
        -H "Content-Type: application/json" \
        -d "{
            \"projectId\": 1,
            \"title\": \"Final Test - $TIMESTAMP\",
            \"taskType\": \"FEATURE\",
            \"description\": \"Testing\",
            \"status\": \"TODO\",
            \"priority\": 1,
            \"assignedUserIds\": [1]
        }" > /tmp/task_response2.json

    TASK_ID=$(cat /tmp/task_response2.json | jq -r '.id // .taskId' 2>/dev/null)

    if [ -z "$TASK_ID" ] || [ "$TASK_ID" = "null" ]; then
        echo "❌ Still failed with projectId"
        cat /tmp/task_response2.json | jq '.' 2>/dev/null || cat /tmp/task_response2.json
        exit 1
    fi
fi

echo "✅ Task created: $TASK_ID"
echo ""

# Wait
echo "⏳ Waiting 10 seconds..."
sleep 10

# Check notifications
NEW=$(curl -s http://localhost:8080/api/notifications -H "Authorization: $TOKEN")
NEW_COUNT=$(echo "$NEW" | jq 'length' 2>/dev/null || echo "0")

echo "📊 New count: $NEW_COUNT"
echo ""

if [ "$NEW_COUNT" -gt "$INITIAL_COUNT" ]; then
    echo "🎉 SUCCESS! Notification created!"
    echo ""
    echo "$NEW" | jq -r '.[0] | "   Type: \(.notificationType)\n   Subject: \(.subject)\n   Message: \(.message)"'
    echo ""
    echo "✅ RabbitMQ and notification system are WORKING!"
else
    echo "❌ No new notifications"
    echo ""
    echo "Check:"
    echo "  1. Is RabbitMQ running? docker ps | grep rabbitmq"
    echo "  2. Backend logs for '🔔 RECEIVED'"
    echo "  3. Did you restart backend after adding @EnableRabbit?"
fi
