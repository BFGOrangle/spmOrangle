#!/bin/bash

cd /Users/taneeherng/Desktop/SMU/Y3S1/SPM/spmOrangle/frontend
TOKEN=$(grep TEST_HR_AUTH_TOKEN .env | cut -d '=' -f2)

echo "Creating new task to generate unread notification..."

TIMESTAMP=$(date +%s)

curl -s -X POST http://localhost:8080/api/tasks \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d "{
        \"title\": \"Test - $TIMESTAMP\",
        \"taskType\": \"FEATURE\",
        \"description\": \"For E2E test\",
        \"status\": \"TODO\",
        \"priority\": 1,
        \"assignedUserIds\": [1]
    }" | jq -r '.id // .taskId'

echo ""
echo "Waiting 3 seconds for notification to be created..."
sleep 3

echo "Checking unread notifications..."
curl -s "http://localhost:8080/api/notifications?unreadOnly=false" \
    -H "Authorization: Bearer $TOKEN" | jq '[.[] | {id: .notificationId, read: .readStatus, subject: .subject}] | .[:3]'
