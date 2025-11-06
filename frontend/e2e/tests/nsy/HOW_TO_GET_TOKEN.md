# How to Get Your Cognito Auth Token for E2E Tests

## Why Do We Need This?

The E2E tests need to make authenticated API calls to your backend. To do this, they need a valid Cognito JWT token. The simplest approach is to get a token from your browser and add it to the `.env` file.

## Step-by-Step Guide

### Option 1: Get Token from Browser DevTools (Easiest)

1. **Open your application in the browser**
   ```
   http://localhost:3000
   ```

2. **Login** with your HR user credentials
   - Email: `qyprojects@gmail.com`
   - Password: (your test password)

3. **Open DevTools** (F12 or Right-click → Inspect)

4. **Go to Console tab**

5. **Run this JavaScript command**:
   ```javascript
   // Get the token using Amplify
   (async () => {
     const { fetchAuthSession } = await import('aws-amplify/auth');
     const session = await fetchAuthSession();
     console.log('ACCESS_TOKEN:', session.tokens.accessToken.toString());
   })();
   ```

6. **Copy the token** that appears in the console (it starts with `eyJ...`)

7. **Add to your `.env` file**:
   ```bash
   # Add this line to frontend/.env
   TEST_HR_AUTH_TOKEN=eyJraWQiOiJxxx...
   ```

### Option 2: Get Token from Network Tab

1. **Open your application** at `http://localhost:3000`

2. **Open DevTools** → **Network tab**

3. **Login** with HR credentials

4. **Go to `/notifications-test` page** (this will make an API call)

5. **Click on any API request** in the Network tab (e.g., `/api/notifications/unread-count`)

6. **Look at Request Headers** → Find `Authorization: Bearer eyJ...`

7. **Copy the entire token** (everything after "Bearer ")

8. **Add to `.env` file**:
   ```bash
   TEST_HR_AUTH_TOKEN=eyJraWQiOiJxxx...
   ```

## Update Your `.env` File

Edit `/Users/taneeherng/Desktop/SMU/Y3S1/SPM/spmOrangle/frontend/.env` and add:

```bash
# Cognito Auth Token for E2E Tests (HR User)
TEST_HR_AUTH_TOKEN=your_token_here
```

## Run the Tests

After adding the token:

```bash
cd frontend
npx playwright test e2e/tests/reports/nsy/notifications-simple.spec.ts --headed
```

## Important Notes

### Token Expiration
- Cognito tokens typically expire after **1 hour**
- If tests start failing with 401 errors, get a new token
- This is normal - just repeat the steps above

### Security
- **DO NOT commit tokens to git!**
- The `.env` file is already in `.gitignore`
- Tokens are specific to your test environment

### Alternative: Using Multiple User Tokens

If you need different tokens for different users:

```bash
# frontend/.env
TEST_HR_AUTH_TOKEN=eyJ...      # HR user token
TEST_MANAGER_AUTH_TOKEN=eyJ... # Manager user token
TEST_STAFF_AUTH_TOKEN=eyJ...   # Staff user token
```

## Troubleshooting

### "Token not found" error
- Check that you added `TEST_HR_AUTH_TOKEN` to the `.env` file
- Make sure there are no quotes around the token
- Restart your test if the `.env` was just updated

### "401 Unauthorized" error
- Your token has expired (get a new one)
- Token is invalid (copy it again carefully)
- Backend server is not running

### Still not working?
1. **Check the token format**:
   - Should start with `eyJ`
   - Should be a long string (500+ characters)
   - Should NOT have "Bearer " prefix in `.env` (the code adds it automatically)

2. **Test the token manually**:
   ```bash
   curl -H "Authorization: Bearer YOUR_TOKEN" \
     http://localhost:8080/api/notifications/unread-count
   ```

   Should return: `{"count":5}` (or similar)

## Example Token (Expired, for reference only)

```
eyJraWQiOiJxNEtxN1wvXC93TWhhSGo4Q2JkdHJcL1hkNmhvSzFcL0NrM0Y3N1wvZDBqbzArbz0iLCJhbGciOiJSUzI1NiJ9...
```

Your actual token will be much longer (typically 800-1000 characters).
