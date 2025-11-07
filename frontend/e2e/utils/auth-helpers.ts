/**
 * Authentication Helpers for E2E Tests
 *
 * These helpers extract AWS Cognito tokens from the browser
 * and add them to API requests for authenticated testing.
 */

import { Page } from '@playwright/test';

/**
 * Get the Cognito access token for API testing
 *
 * This function checks for a token in the following order:
 * 1. Environment variable TEST_HR_AUTH_TOKEN (for hardcoded tokens)
 * 2. Browser storage (IndexedDB/cookies)
 *
 * For E2E tests, it's recommended to use environment variables for simplicity.
 *
 * @param page - Playwright page instance (optional, used for browser token extraction)
 * @returns Bearer token string (e.g., "Bearer eyJhbGc...")
 */
export async function getCognitoToken(page?: Page): Promise<string> {
  // Option 1: Use hardcoded token from environment variable (recommended for E2E tests)
  const envToken = process.env.TEST_HR_AUTH_TOKEN || process.env.TEST_AUTH_TOKEN;

  if (envToken) {
    // Check if token already has "Bearer " prefix
    return envToken.startsWith('Bearer ') ? envToken : `Bearer ${envToken}`;
  }

  // Option 2: Try to extract from browser storage
  if (!page) {
    throw new Error('No auth token found in environment variables and no page provided for browser extraction');
  }

  try {
    const token = await page.evaluate(async () => {
      // Try IndexedDB first
      try {
        const dbNames = await indexedDB.databases();
        const amplifyDb = dbNames.find(db =>
          db.name?.includes('amplify') || db.name?.includes('aws')
        );

        if (amplifyDb && amplifyDb.name) {
          return new Promise<string | null>((resolve) => {
            const request = indexedDB.open(amplifyDb.name!);

            request.onsuccess = () => {
              const db = request.result;
              const possibleStores = ['keyvaluestorage', 'amplify', 'auth'];

              for (const storeName of possibleStores) {
                if (db.objectStoreNames.contains(storeName)) {
                  const transaction = db.transaction(storeName, 'readonly');
                  const store = transaction.objectStore(storeName);
                  const getAllRequest = store.getAll();

                  getAllRequest.onsuccess = () => {
                    const items = getAllRequest.result;
                    for (const item of items) {
                      if (item && typeof item === 'object') {
                        const accessToken = item.accessToken || item.idToken ||
                          (item.data && (item.data.accessToken || item.data.idToken));

                        if (accessToken) {
                          resolve(`Bearer ${accessToken}`);
                          return;
                        }
                      }
                    }
                  };
                }
              }
              resolve(null);
            };

            request.onerror = () => resolve(null);
          });
        }
      } catch (error) {
        console.error('Error accessing IndexedDB:', error);
      }

      return null;
    });

    if (token) {
      return token;
    }

    throw new Error('Could not find auth token in browser storage');
  } catch (error) {
    throw new Error(`Could not extract Cognito token: ${error}`);
  }
}

/**
 * Make an authenticated API request using the browser's Cognito token
 *
 * This is the recommended way to make API calls in E2E tests because:
 * - It uses the actual user's authentication token
 * - It works exactly like the frontend's AuthenticatedApiClient
 * - It goes through the Next.js proxy with proper auth
 *
 * @param page - Playwright page instance (must be authenticated)
 * @param url - API endpoint URL (e.g., '/api/notifications')
 * @param options - Fetch options (method, body, etc.)
 * @returns Response object
 *
 * @example
 * ```typescript
 * const response = await authenticatedRequest(hrPage, '/api/notifications');
 * const notifications = await response.json();
 * ```
 */
export async function authenticatedRequest(
  page: Page,
  url: string,
  options: RequestInit = {}
): Promise<Response> {
  // Get the Cognito token from the browser
  const token = await getCognitoToken(page);

  // Make the request with the Authorization header
  const response = await page.request.fetch(url, {
    ...options,
    headers: {
      ...options.headers,
      'Authorization': token,
      'Content-Type': 'application/json',
    },
  });

  return response;
}

/**
 * Helper to make authenticated GET request
 */
export async function authenticatedGet(page: Page, url: string): Promise<Response> {
  return authenticatedRequest(page, url, { method: 'GET' });
}

/**
 * Helper to make authenticated POST request
 */
export async function authenticatedPost(
  page: Page,
  url: string,
  data: any
): Promise<Response> {
  return authenticatedRequest(page, url, {
    method: 'POST',
    body: JSON.stringify(data),
  });
}

/**
 * Helper to make authenticated PATCH request
 */
export async function authenticatedPatch(
  page: Page,
  url: string,
  data: any = null
): Promise<Response> {
  return authenticatedRequest(page, url, {
    method: 'PATCH',
    body: data ? JSON.stringify(data) : null,
  });
}

/**
 * Helper to make authenticated DELETE request
 */
export async function authenticatedDelete(page: Page, url: string): Promise<Response> {
  return authenticatedRequest(page, url, { method: 'DELETE' });
}