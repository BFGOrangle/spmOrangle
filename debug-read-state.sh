#!/bin/bash

cd /Users/taneeherng/Desktop/SMU/Y3S1/SPM/spmOrangle/frontend
TOKEN=$(grep TEST_HR_AUTH_TOKEN .env | cut -d '=' -f2)

echo "🔍 Testing Read State Persistence"
echo "=================================="
echo ""

# Get all notifications
echo "1. Getting all notifications..."
NOTIFS=$(curl -s http://localhost:8080/api/notifications -H "Authorization: Bearer $TOKEN")
echo "   Total count: $(echo "$NOTIFS" | jq 'length')"

# Find an unread notification
UNREAD_ID=$(echo "$NOTIFS" | jq -r '.[] | select(.readStatus == false) | .notificationId' | head -1)

if [ -z "$UNREAD_ID" ] || [ "$UNREAD_ID" = "null" ]; then
    echo ""
    echo "⚠️  No unread notifications found. Let's check what we have:"
    echo "$NOTIFS" | jq -r '.[] | "   ID: \(.notificationId) | Read: \(.readStatus) | Dismissed: \(.dismissedStatus)"' | head -5

    # Use the first notification regardless
    NOTIF_ID=$(echo "$NOTIFS" | jq -r '.[0].notificationId')
    echo ""
    echo "Using notification ID: $NOTIF_ID (may already be read)"
else
    NOTIF_ID=$UNREAD_ID
    echo "   Found unread notification ID: $NOTIF_ID"
fi

echo ""

# Get notification details before marking as read
echo "2. Notification BEFORE marking as read:"
BEFORE=$(curl -s http://localhost:8080/api/notifications/$NOTIF_ID -H "Authorization: Bearer $TOKEN")
echo "$BEFORE" | jq '{notificationId, readStatus, dismissedStatus, subject}'
echo ""

# Mark as read
echo "3. Marking notification $NOTIF_ID as read..."
MARK_RESPONSE=$(curl -s -w "\nHTTP: %{http_code}" -X PATCH \
    http://localhost:8080/api/notifications/$NOTIF_ID/read \
    -H "Authorization: Bearer $TOKEN")

echo "   Response: $MARK_RESPONSE"
echo ""

# Wait a moment
sleep 1

# Get notification details after marking as read
echo "4. Notification AFTER marking as read (direct fetch):"
AFTER=$(curl -s http://localhost:8080/api/notifications/$NOTIF_ID -H "Authorization: Bearer $TOKEN")
echo "$AFTER" | jq '{notificationId, readStatus, dismissedStatus, subject}'
echo ""

# Get all notifications again to see if it's still in the list
echo "5. Checking if notification appears in list..."
ALL_NOTIFS=$(curl -s http://localhost:8080/api/notifications -H "Authorization: Bearer $TOKEN")
FOUND=$(echo "$ALL_NOTIFS" | jq --arg id "$NOTIF_ID" '.[] | select(.notificationId == ($id | tonumber))')

if [ -z "$FOUND" ]; then
    echo "   ❌ Notification NOT found in list!"
    echo ""
    echo "   Checking with unreadOnly=false explicitly..."
    ALL_NOTIFS_EXPLICIT=$(curl -s "http://localhost:8080/api/notifications?unreadOnly=false" -H "Authorization: Bearer $TOKEN")
    FOUND_EXPLICIT=$(echo "$ALL_NOTIFS_EXPLICIT" | jq --arg id "$NOTIF_ID" '.[] | select(.notificationId == ($id | tonumber))')

    if [ -z "$FOUND_EXPLICIT" ]; then
        echo "   ❌ Still NOT found even with unreadOnly=false!"
    else
        echo "   ✅ Found with unreadOnly=false:"
        echo "$FOUND_EXPLICIT" | jq '{notificationId, readStatus, dismissedStatus}'
    fi
else
    echo "   ✅ Notification found in list:"
    echo "$FOUND" | jq '{notificationId, readStatus, dismissedStatus}'
fi
