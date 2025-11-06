#!/bin/bash

echo "🔍 Backend Status Check"
echo "======================="
echo ""

# Get token
cd /Users/taneeherng/Desktop/SMU/Y3S1/SPM/spmOrangle/frontend
TOKEN=$(grep TEST_HR_AUTH_TOKEN .env | cut -d '=' -f2)
[[ ! "$TOKEN" =~ ^Bearer ]] && TOKEN="Bearer $TOKEN"

echo "1. Testing backend health..."
curl -s http://localhost:8080/api/notifications/unread-count -H "Authorization: $TOKEN" -w "\n   HTTP Code: %{http_code}\n" || echo "   Failed to connect"

echo ""
echo "2. Getting current user's notifications..."
curl -s http://localhost:8080/api/notifications -H "Authorization: $TOKEN" | jq '.' || echo "   Failed"

echo ""
echo "3. Testing task creation with full debug..."
TIMESTAMP=$(date +%s)

# Get user ID
TOKEN_CLEAN=$(echo "$TOKEN" | sed 's/Bearer //')
PAYLOAD=$(echo "$TOKEN_CLEAN" | cut -d '.' -f 2)
PADDING_LENGTH=$(( (4 - ${#PAYLOAD} % 4) % 4 ))
PADDED_PAYLOAD="$PAYLOAD"
for i in $(seq 1 $PADDING_LENGTH); do
    PADDED_PAYLOAD="${PADDED_PAYLOAD}="
done
USER_ID=$(echo "$PADDED_PAYLOAD" | base64 -d 2>/dev/null | jq -r '.sub' 2>/dev/null)

echo "   User ID: $USER_ID"
echo ""

TASK_JSON="{
    \"title\": \"Debug Test - $TIMESTAMP\",
    \"taskType\": \"FEATURE\",
    \"description\": \"Testing\",
    \"status\": \"TODO\",
    \"priority\": 1,
    \"assignedUserIds\": [\"$USER_ID\"]
}"

echo "   Request body:"
echo "$TASK_JSON" | jq '.'
echo ""

echo "   Sending request..."
curl -v -X POST http://localhost:8080/api/tasks \
    -H "Authorization: $TOKEN" \
    -H "Content-Type: application/json" \
    -d "$TASK_JSON" 2>&1 | grep -E "< HTTP|< Content|^\{|\"id\"|\"title\"|error|message"
