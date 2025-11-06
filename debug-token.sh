#!/bin/bash

# Debug token to see what's inside

cd /Users/taneeherng/Desktop/SMU/Y3S1/SPM/spmOrangle/frontend

if [ ! -f .env ]; then
    echo "❌ .env file not found"
    exit 1
fi

TOKEN=$(grep TEST_HR_AUTH_TOKEN .env | cut -d '=' -f2)

if [ -z "$TOKEN" ]; then
    echo "❌ TEST_HR_AUTH_TOKEN not found in .env"
    exit 1
fi

echo "Token value (first 50 chars): ${TOKEN:0:50}..."
echo ""

# Remove Bearer prefix if present
TOKEN_CLEAN=$(echo "$TOKEN" | sed 's/Bearer //' | sed 's/bearer //')

echo "Attempting to decode JWT payload..."
echo ""

# Try different approaches
PAYLOAD=$(echo "$TOKEN_CLEAN" | cut -d '.' -f 2)

echo "Method 1: Standard base64 decode"
echo "$PAYLOAD" | base64 -d 2>/dev/null | jq . 2>/dev/null || echo "Failed"

echo ""
echo "Method 2: base64 with padding fix"
# Add padding if needed
PADDING_LENGTH=$(( (4 - ${#PAYLOAD} % 4) % 4 ))
PADDED_PAYLOAD="$PAYLOAD"
for i in $(seq 1 $PADDING_LENGTH); do
    PADDED_PAYLOAD="${PADDED_PAYLOAD}="
done
echo "$PADDED_PAYLOAD" | base64 -d 2>/dev/null | jq . 2>/dev/null || echo "Failed"

echo ""
echo "Method 3: macOS specific base64 decode"
echo "$PAYLOAD" | base64 --decode 2>/dev/null | jq . 2>/dev/null || echo "Failed"

echo ""
echo "If all methods failed, the token might be:"
echo "  - Not a JWT token"
echo "  - Corrupted"
echo "  - Using a different encoding"
