#!/bin/bash

TOKEN="eyJraWQiOiJQOThhYWlzbnA3YzNMTGhCdlB5MW5BOU9ITjZTYW5TQjVzWStoQ2ZHRG53PSIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiI2OWNhOTVmYy05MGExLTcwNzUtOWM3ZC0zZTlhNWRmZjIzMWQiLCJjb2duaXRvOmdyb3VwcyI6WyJNQU5BR0VSIl0sImlzcyI6Imh0dHBzOlwvXC9jb2duaXRvLWlkcC5hcC1zb3V0aGVhc3QtMS5hbWF6b25hd3MuY29tXC9hcC1zb3V0aGVhc3QtMV9HV3BySVZwdG8iLCJjbGllbnRfaWQiOiI0NXF1Y25jYjlqcGlvbGkwYTY5b2xjdm9xaiIsIm9yaWdpbl9qdGkiOiI5NDgwMmI2Yy1kMTczLTQyYjAtYTM5Zi1iZTViOWUwNDZlYTciLCJldmVudF9pZCI6ImE3ZjQ3MWY3LWYxNGEtNDVhNy1iN2Y5LTYyYzhlMTA0ZmU0MCIsInRva2VuX3VzZSI6ImFjY2VzcyIsInNjb3BlIjoiYXdzLmNvZ25pdG8uc2lnbmluLnVzZXIuYWRtaW4iLCJhdXRoX3RpbWUiOjE3NjIzNDc2MzcsImV4cCI6MTc2MjQxOTQ2NSwiaWF0IjoxNzYyNDE3NjY1LCJqdGkiOiIxM2QwNzFhYy0wNTY1LTQ4NDItOWVjMi1lMWZjNGU1OGJmMGMiLCJ1c2VybmFtZSI6Im9yYW5nbGVtYW5hZ2VydGVzdHVzZXIifQ.oKmITsHthyMHzBclimWKuusDd7bkYwtrm4QB_AcrJ7dmqKYa8mDQgxfn4SHlzCeq_o5nxDwl0AChK52bpLBQWK7hpyyatXulD3wbbPevHtdgBZwPE4Hd9Tlv69cHGY3qjrkmuvtcKHi0PvqiUKfPULA7RPK0lSYL6CMtIj5Ghrn8zz9FRGxP5AYGX0gD7QuqnPEVtnuo_BArMn9-CBKkcxi2Z6CaImZGI2Yo9HHtdXJKYvX0eBuLcCEX5Q-Q73JsGo1wmw-XGFCND9frr-3jKUNfWpsiwoLxR_-_sKaRGkJAGK1mpnjr8C1pxoQKyvCtSNiPgPxSt2CJUvlwIbkt3Q"

PAYLOAD=$(echo "$TOKEN" | cut -d '.' -f 2)
PADDING_LENGTH=$(( (4 - ${#PAYLOAD} % 4) % 4 ))
PADDED_PAYLOAD="$PAYLOAD"
for i in $(seq 1 $PADDING_LENGTH); do
    PADDED_PAYLOAD="${PADDED_PAYLOAD}="
done

echo "Token Payload:"
echo "$PADDED_PAYLOAD" | base64 -d 2>/dev/null | jq .

NOW=$(date +%s)
EXP=$(echo "$PADDED_PAYLOAD" | base64 -d 2>/dev/null | jq -r '.exp')

echo ""
echo "Current time: $NOW"
echo "Expiry time: $EXP"

if [ "$NOW" -gt "$EXP" ]; then
    echo "❌ TOKEN IS EXPIRED!"
else
    REMAINING=$(( EXP - NOW ))
    echo "✅ Token is valid for $REMAINING more seconds"
fi

echo ""
echo "Testing API call..."
curl -s http://localhost:8080/api/notifications \
    -H "Authorization: Bearer $TOKEN" \
    -w "\nHTTP Status: %{http_code}\n"
