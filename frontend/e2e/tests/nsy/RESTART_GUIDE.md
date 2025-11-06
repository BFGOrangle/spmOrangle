# How to Apply Next.js Proxy Configuration

## What Changed?

I added a **Next.js proxy configuration** to [next.config.ts](../../../../next.config.ts) that forwards all `/api/*` requests to your backend server.

## Configuration Details

```typescript
async rewrites() {
  return [
    {
      source: '/api/:path*',  // Requests to /api/*
      destination: 'http://localhost:8080/api/:path*',  // Get proxied to backend
    },
  ];
}
```

## How It Works

Before (without proxy):
```
Test → /api/notifications → Next.js → 404 Not Found ❌
```

After (with proxy):
```
Test → /api/notifications → Next.js Proxy → Backend (localhost:8080) → Success ✅
```

## IMPORTANT: Restart Required

**Next.js configuration changes require a server restart!**

### Steps to Apply:

1. **Stop your Next.js development server** (if running)
   - Press `Ctrl+C` in the terminal where `npm run dev` is running

2. **Start the Next.js server again**
   ```bash
   cd frontend
   npm run dev
   ```

3. **Verify the proxy is working**
   - Open browser to http://localhost:3000
   - Open DevTools Network tab
   - Navigate to a page that calls `/api/notifications`
   - You should see requests to `/api/notifications` succeeding (not 404)

4. **Run the tests**
   ```bash
   npx playwright test e2e/tests/reports/nsy/notifications-simple.spec.ts --headed
   ```

## Why This Is Needed

- Your frontend uses AWS Cognito authentication
- Tokens are stored in browser (localStorage/cookies)
- Direct calls to `localhost:8080` bypass authentication
- **Next.js proxy** forwards requests WITH authentication headers
- Backend validates tokens and returns data

## Troubleshooting

If tests still fail after restart:

1. **Check backend is running**
   ```bash
   curl http://localhost:8080/api/notifications/unread-count
   ```
   - Should return 401 Unauthorized (needs auth) ✅
   - Should NOT return "Connection refused" ❌

2. **Check Next.js proxy is working**
   - Visit http://localhost:3000 in browser
   - Login as HR user
   - Open `/notifications-test` page
   - Check if notifications load (should work now)

3. **Check environment variables**
   - Ensure `.env` has:
     ```
     NEXT_PUBLIC_API_BASE_URL=http://localhost:8080/api
     ```

## Expected Test Results

After restarting Next.js:
- ✅ 7 tests should pass
- ✅ API endpoints should return data (not 404)
- ✅ Authentication should work automatically

## Next Steps

Once tests pass:
1. Consider uncommenting the skipped tests in other spec files
2. Update `notification-helpers.ts` to use relative URLs
3. Run all notification tests together
