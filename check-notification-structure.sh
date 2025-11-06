#!/bin/bash

cd /Users/taneeherng/Desktop/SMU/Y3S1/SPM/spmOrangle/frontend
TOKEN=$(grep TEST_HR_AUTH_TOKEN .env | cut -d '=' -f2)

echo "Fetching first notification to check structure..."
echo ""

curl -s http://localhost:8080/api/notifications -H "Authorization: Bearer $TOKEN" | jq '.[0]' 2>/dev/null
