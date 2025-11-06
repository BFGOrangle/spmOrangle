#!/bin/bash

echo "🎯 Generating Test Data for All Notification Types"
echo "===================================================="
echo ""

# Get token
cd /Users/taneeherng/Desktop/SMU/Y3S1/SPM/spmOrangle/frontend
TOKEN=$(grep TEST_HR_AUTH_TOKEN .env | cut -d '=' -f2)
[[ ! "$TOKEN" =~ ^Bearer ]] && TOKEN="Bearer $TOKEN"

cd /Users/taneeherng/Desktop/SMU/Y3S1/SPM/spmOrangle

# Get user ID from token
TOKEN_CLEAN=$(echo "$TOKEN" | sed 's/Bearer //' | sed 's/bearer //')
PAYLOAD=$(echo "$TOKEN_CLEAN" | cut -d '.' -f 2)
PADDING_LENGTH=$(( (4 - ${#PAYLOAD} % 4) % 4 ))
PADDED_PAYLOAD="$PAYLOAD"
for i in $(seq 1 $PADDING_LENGTH); do
    PADDED_PAYLOAD="${PADDED_PAYLOAD}="
done
USER_ID=$(echo "$PADDED_PAYLOAD" | base64 -d 2>/dev/null | jq -r '.sub' 2>/dev/null)

# Get numeric user ID from existing tasks
EXISTING_TASKS=$(curl -s http://localhost:8080/api/tasks -H "Authorization: $TOKEN" 2>/dev/null)
USER_NUMERIC_ID=$(echo "$EXISTING_TASKS" | jq -r '.[0].assignedUsers[0].id // 1' 2>/dev/null)

echo "✅ User UUID: $USER_ID"
echo "✅ User Numeric ID: $USER_NUMERIC_ID"
echo ""

TIMESTAMP=$(date +%s)

# 1. Create task for TASK_ASSIGNED notification
echo "1️⃣  Creating task for TASK_ASSIGNED notification..."
TASK1=$(curl -s -X POST http://localhost:8080/api/tasks \
    -H "Authorization: $TOKEN" \
    -H "Content-Type: application/json" \
    -d "{
        \"title\": \"Test Task for Assignment - $TIMESTAMP\",
        \"taskType\": \"FEATURE\",
        \"description\": \"Testing task assignment notifications\",
        \"status\": \"TODO\",
        \"priority\": 1,
        \"assignedUserIds\": [$USER_NUMERIC_ID]
    }" 2>/dev/null)

TASK1_ID=$(echo "$TASK1" | jq -r '.id // .taskId' 2>/dev/null)
if [ -n "$TASK1_ID" ] && [ "$TASK1_ID" != "null" ]; then
    echo "   ✅ Created task $TASK1_ID"
else
    echo "   ❌ Failed to create task"
fi
echo ""

sleep 2

# 2. Create another task and update status for STATUS_UPDATED notification
echo "2️⃣  Creating task for STATUS_UPDATED notification..."
TASK2=$(curl -s -X POST http://localhost:8080/api/tasks \
    -H "Authorization: $TOKEN" \
    -H "Content-Type: application/json" \
    -d "{
        \"title\": \"Test Task for Status Update - $TIMESTAMP\",
        \"taskType\": \"BUG\",
        \"description\": \"Testing status change notifications\",
        \"status\": \"TODO\",
        \"priority\": 2,
        \"assignedUserIds\": [$USER_NUMERIC_ID]
    }" 2>/dev/null)

TASK2_ID=$(echo "$TASK2" | jq -r '.id // .taskId' 2>/dev/null)
if [ -n "$TASK2_ID" ] && [ "$TASK2_ID" != "null" ]; then
    echo "   ✅ Created task $TASK2_ID"

    sleep 2

    # Update status
    echo "   🔄 Updating task status to IN_PROGRESS..."
    STATUS_UPDATE=$(curl -s -X PATCH http://localhost:8080/api/tasks/$TASK2_ID \
        -H "Authorization: $TOKEN" \
        -H "Content-Type: application/json" \
        -d "{
            \"status\": \"IN_PROGRESS\"
        }" 2>/dev/null)

    if [ -n "$STATUS_UPDATE" ]; then
        echo "   ✅ Status updated"
    fi
else
    echo "   ❌ Failed to create task"
fi
echo ""

sleep 2

# 3. Create task and add comment for COMMENT_ADDED notification
echo "3️⃣  Creating task for COMMENT_ADDED notification..."
TASK3=$(curl -s -X POST http://localhost:8080/api/tasks \
    -H "Authorization: $TOKEN" \
    -H "Content-Type: application/json" \
    -d "{
        \"title\": \"Test Task for Comments - $TIMESTAMP\",
        \"taskType\": \"FEATURE\",
        \"description\": \"Testing comment notifications\",
        \"status\": \"TODO\",
        \"priority\": 1,
        \"assignedUserIds\": [$USER_NUMERIC_ID]
    }" 2>/dev/null)

TASK3_ID=$(echo "$TASK3" | jq -r '.id // .taskId' 2>/dev/null)
if [ -n "$TASK3_ID" ] && [ "$TASK3_ID" != "null" ]; then
    echo "   ✅ Created task $TASK3_ID"

    sleep 2

    # Add comment
    echo "   💬 Adding comment to task..."
    COMMENT=$(curl -s -X POST http://localhost:8080/api/tasks/$TASK3_ID/comments \
        -H "Authorization: $TOKEN" \
        -H "Content-Type: application/json" \
        -d "{
            \"content\": \"This is a test comment to trigger notification\"
        }" 2>/dev/null)

    if [ -n "$COMMENT" ]; then
        echo "   ✅ Comment added"
    fi
else
    echo "   ❌ Failed to create task"
fi
echo ""

sleep 2

# 4. Create task with mention for MENTION notification
echo "4️⃣  Creating task for MENTION notification..."
TASK4=$(curl -s -X POST http://localhost:8080/api/tasks \
    -H "Authorization: $TOKEN" \
    -H "Content-Type: application/json" \
    -d "{
        \"title\": \"Test Task for Mentions - $TIMESTAMP\",
        \"taskType\": \"FEATURE\",
        \"description\": \"Testing mention notifications @$USER_ID\",
        \"status\": \"TODO\",
        \"priority\": 1,
        \"assignedUserIds\": [$USER_NUMERIC_ID]
    }" 2>/dev/null)

TASK4_ID=$(echo "$TASK4" | jq -r '.id // .taskId' 2>/dev/null)
if [ -n "$TASK4_ID" ] && [ "$TASK4_ID" != "null" ]; then
    echo "   ✅ Created task $TASK4_ID"

    sleep 2

    # Add comment with mention
    echo "   💬 Adding comment with mention..."
    COMMENT_MENTION=$(curl -s -X POST http://localhost:8080/api/tasks/$TASK4_ID/comments \
        -H "Authorization: $TOKEN" \
        -H "Content-Type: application/json" \
        -d "{
            \"content\": \"Hey @$USER_ID, can you check this task?\"
        }" 2>/dev/null)

    if [ -n "$COMMENT_MENTION" ]; then
        echo "   ✅ Mention added"
    fi
else
    echo "   ❌ Failed to create task"
fi
echo ""

sleep 2

# 5. Create task with due date for REMINDER notification
echo "5️⃣  Creating task for REMINDER notification..."
# Set due date to 2 hours from now
DUE_DATE=$(date -u -v+2H +"%Y-%m-%dT%H:%M:%S.000Z" 2>/dev/null || date -u -d "+2 hours" +"%Y-%m-%dT%H:%M:%S.000Z" 2>/dev/null)

TASK5=$(curl -s -X POST http://localhost:8080/api/tasks \
    -H "Authorization: $TOKEN" \
    -H "Content-Type: application/json" \
    -d "{
        \"title\": \"Test Task with Reminder - $TIMESTAMP\",
        \"taskType\": \"FEATURE\",
        \"description\": \"Testing reminder notifications\",
        \"status\": \"TODO\",
        \"priority\": 3,
        \"dueDateTime\": \"$DUE_DATE\",
        \"assignedUserIds\": [$USER_NUMERIC_ID]
    }" 2>/dev/null)

TASK5_ID=$(echo "$TASK5" | jq -r '.id // .taskId' 2>/dev/null)
if [ -n "$TASK5_ID" ] && [ "$TASK5_ID" != "null" ]; then
    echo "   ✅ Created task $TASK5_ID with due date: $DUE_DATE"
else
    echo "   ❌ Failed to create task"
fi
echo ""

# Wait for all notifications to be processed
echo "⏳ Waiting 10 seconds for all notifications to be processed..."
sleep 10
echo ""

# Check final notification count
echo "📊 Checking final notification count..."
FINAL_NOTIFICATIONS=$(curl -s http://localhost:8080/api/notifications -H "Authorization: $TOKEN" 2>/dev/null)
FINAL_COUNT=$(echo "$FINAL_NOTIFICATIONS" | jq 'length' 2>/dev/null || echo "0")

echo "   Total notifications: $FINAL_COUNT"
echo ""

if [ "$FINAL_COUNT" -gt "0" ]; then
    echo "📋 Recent notifications:"
    echo "$FINAL_NOTIFICATIONS" | jq -r '.[:5] | .[] | "   • [\(.notificationType)] \(.subject)"' 2>/dev/null
    echo ""
    echo "🎉 Test data generation complete!"
    echo ""
    echo "Next step: Run E2E tests"
    echo "   cd frontend && npx playwright test e2e/tests/nsy/"
else
    echo "⚠️  No notifications found - something might be wrong"
fi
