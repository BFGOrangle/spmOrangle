#!/bin/bash

# Notification System Test Script
# This script creates a test task and verifies notifications are working

echo "🔔 Notification System Test Script"
echo "=================================="
echo ""

# Check if token is provided
if [ -z "$1" ]; then
    echo "❌ Error: Bearer token not provided"
    echo ""
    echo "Usage:"
    echo "  ./test-notifications.sh YOUR_BEARER_TOKEN"
    echo ""
    echo "Or get token from .env:"
    echo "  cd frontend"
    echo "  export TOKEN=\$(grep TEST_HR_AUTH_TOKEN .env | cut -d '=' -f2)"
    echo "  cd .."
    echo "  ./test-notifications.sh \$TOKEN"
    exit 1
fi

TOKEN="$1"

# Add "Bearer " prefix if not present
if [[ ! "$TOKEN" =~ ^Bearer ]]; then
    TOKEN="Bearer $TOKEN"
fi

echo "✅ Token configured"
echo ""

# Step 1: Check backend is running
echo "📡 Step 1: Checking if backend is running..."
# Try multiple endpoints to check if backend is up
BACKEND_CHECK=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/notifications/unread-count 2>/dev/null || echo "000")

if [ "$BACKEND_CHECK" = "000" ]; then
    echo "❌ Backend is not running on port 8080"
    echo "   Please start the backend first:"
    echo "   cd backend/spmorangle && ./mvnw spring-boot:run"
    exit 1
elif [ "$BACKEND_CHECK" = "401" ] || [ "$BACKEND_CHECK" = "403" ]; then
    echo "✅ Backend is running (authentication required)"
elif [ "$BACKEND_CHECK" = "200" ]; then
    echo "✅ Backend is running"
else
    echo "⚠️  Backend responded with status: $BACKEND_CHECK"
    echo "   Continuing anyway..."
fi
echo ""

# Step 2: Get current user ID from token
echo "👤 Step 2: Getting current user ID..."
# Decode JWT to get user ID (sub claim)
USER_ID=$(echo "$TOKEN" | sed 's/Bearer //' | cut -d '.' -f 2 | base64 -d 2>/dev/null | jq -r '.sub // .username // empty' 2>/dev/null)

if [ -z "$USER_ID" ]; then
    echo "⚠️  Could not extract user ID from token"
    echo "   Will try to get it from API..."
    # Try to get user info from API
    USER_INFO=$(curl -s -X GET http://localhost:8080/api/users/me \
        -H "Authorization: $TOKEN" 2>/dev/null)
    USER_ID=$(echo "$USER_INFO" | jq -r '.id // .userId // empty' 2>/dev/null)

    if [ -z "$USER_ID" ]; then
        echo "❌ Could not determine user ID"
        echo "   Tasks won't be assigned, so no notifications will be created"
        echo "   Continuing anyway for demonstration..."
        USER_ID=""
    else
        echo "✅ User ID: $USER_ID"
    fi
else
    echo "✅ User ID from token: $USER_ID"
fi
echo ""

# Step 3: Check current notification count
echo "📊 Step 3: Checking current notification count..."
INITIAL_COUNT=$(curl -s -X GET http://localhost:8080/api/notifications/unread-count \
    -H "Authorization: $TOKEN" \
    -H "Content-Type: application/json" | jq -r '.count' 2>/dev/null || echo "error")

if [ "$INITIAL_COUNT" = "error" ]; then
    echo "⚠️  Could not get notification count (auth may have failed)"
    echo "   Continuing anyway..."
    INITIAL_COUNT="unknown"
else
    echo "✅ Current unread notifications: $INITIAL_COUNT"
fi
echo ""

# Step 4: Create a test task
echo "📝 Step 4: Creating test task..."
TIMESTAMP=$(date +%s)

# Build JSON payload with or without assignedUserIds
if [ -n "$USER_ID" ]; then
    TASK_JSON="{
        \"title\": \"E2E Test Task - $TIMESTAMP\",
        \"taskType\": \"FEATURE\",
        \"description\": \"Testing notification system for E2E tests\",
        \"status\": \"TODO\",
        \"priority\": 1,
        \"assignedUserIds\": [\"$USER_ID\"]
    }"
    echo "   Assigning task to user: $USER_ID"
else
    TASK_JSON="{
        \"title\": \"E2E Test Task - $TIMESTAMP\",
        \"taskType\": \"FEATURE\",
        \"description\": \"Testing notification system for E2E tests\",
        \"status\": \"TODO\",
        \"priority\": 1
    }"
    echo "   ⚠️  Creating unassigned task (no notifications will be generated)"
fi

# Make the request and capture both response and HTTP code
TASK_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST http://localhost:8080/api/tasks \
    -H "Authorization: $TOKEN" \
    -H "Content-Type: application/json" \
    -d "$TASK_JSON")

# Split response and HTTP code
HTTP_CODE=$(echo "$TASK_RESPONSE" | tail -n 1)
TASK_BODY=$(echo "$TASK_RESPONSE" | sed '$d')

echo "   HTTP Status: $HTTP_CODE"

if [ "$HTTP_CODE" != "200" ] && [ "$HTTP_CODE" != "201" ]; then
    echo "❌ Failed to create task (HTTP $HTTP_CODE)"
    echo "   Response: $TASK_BODY"
    echo ""
    echo "💡 Possible issues:"
    echo "   - Token may be expired (401)"
    echo "   - Missing required fields (400)"
    echo "   - Backend error (500)"
    echo ""
    echo "Try creating a task manually:"
    echo "  curl -X POST http://localhost:8080/api/tasks \\"
    echo "    -H \"Authorization: $TOKEN\" \\"
    echo "    -H \"Content-Type: application/json\" \\"
    echo "    -d '{\"title\":\"Test\",\"description\":\"Test\",\"status\":\"TODO\"}'"
    exit 1
fi

TASK_ID=$(echo "$TASK_BODY" | jq -r '.id // .taskId // empty' 2>/dev/null)

if [ -z "$TASK_ID" ]; then
    echo "❌ Task created but could not parse ID"
    echo "   Response: $TASK_BODY"
    exit 1
fi

echo "✅ Task created successfully!"
echo "   Task ID: $TASK_ID"
echo ""

# Step 5: Wait for RabbitMQ to process
echo "⏳ Step 5: Waiting for RabbitMQ to process notification (10 seconds)..."
for i in {10..1}; do
    echo -ne "   $i seconds remaining...\r"
    sleep 1
done
echo "   ✅ Processing complete                    "
echo ""

# Step 6: Check if notification was created
echo "📬 Step 6: Checking for notifications..."
NEW_COUNT=$(curl -s -X GET http://localhost:8080/api/notifications/unread-count \
    -H "Authorization: $TOKEN" \
    -H "Content-Type: application/json" | jq -r '.count' 2>/dev/null || echo "error")

if [ "$NEW_COUNT" = "error" ]; then
    echo "⚠️  Could not get new notification count"
else
    echo "✅ New unread notification count: $NEW_COUNT"

    if [ "$INITIAL_COUNT" != "unknown" ] && [ "$NEW_COUNT" -gt "$INITIAL_COUNT" ]; then
        DIFF=$((NEW_COUNT - INITIAL_COUNT))
        echo "🎉 SUCCESS! $DIFF new notification(s) created!"
    fi
fi
echo ""

# Step 7: Get notification details
echo "📋 Step 7: Fetching notification details..."
NOTIFICATIONS=$(curl -s -X GET http://localhost:8080/api/notifications \
    -H "Authorization: $TOKEN" \
    -H "Content-Type: application/json")

NOTIF_COUNT=$(echo "$NOTIFICATIONS" | jq 'length' 2>/dev/null || echo "0")
echo "✅ Total notifications: $NOTIF_COUNT"

if [ "$NOTIF_COUNT" -gt 0 ]; then
    echo ""
    echo "Recent notifications:"
    echo "$NOTIFICATIONS" | jq -r '.[:3][] | "  • [\(.notificationType)] \(.subject)"' 2>/dev/null || echo "  (Could not parse notifications)"
fi
echo ""

# Step 8: Run E2E tests
echo "🧪 Step 8: Running E2E tests..."
echo ""

cd frontend 2>/dev/null || {
    echo "❌ Frontend directory not found"
    exit 1
}

echo "Running notification tests..."
echo "=============================="
npx playwright test e2e/tests/nsy/notifications-simple.spec.ts --reporter=list

echo ""
echo "=================================="
echo "✅ Test script completed!"
echo ""
echo "Summary:"
echo "  - Initial notifications: $INITIAL_COUNT"
echo "  - Current notifications: $NEW_COUNT"
echo "  - Task created: $TASK_ID"
echo ""
echo "Next steps:"
echo "  1. Check test results above"
echo "  2. Run all tests: cd frontend && npx playwright test e2e/tests/nsy/"
echo "  3. View in browser: open http://localhost:3000"
