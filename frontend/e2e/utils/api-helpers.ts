import { Page, APIRequestContext, APIResponse, Response } from '@playwright/test';
import { getConfig } from '../config/environments';

/**
 * API Helper utilities
 * Use these to interact with backend APIs during tests
 */

/**
 * Base API client
 */
export class ApiClient {
  private context: APIRequestContext;
  private baseURL: string;

  constructor(context: APIRequestContext) {
    this.context = context;
    this.baseURL = getConfig().apiURL;
  }

  /**
   * Make GET request
   */
  async get(endpoint: string, options?: { headers?: Record<string, string> }): Promise<APIResponse> {
    return await this.context.get(`${this.baseURL}${endpoint}`, {
      headers: options?.headers,
    });
  }

  /**
   * Make POST request
   */
  async post(
    endpoint: string,
    options?: { data?: any; headers?: Record<string, string> }
  ): Promise<APIResponse> {
    return await this.context.post(`${this.baseURL}${endpoint}`, {
      data: options?.data,
      headers: options?.headers,
    });
  }

  /**
   * Make PUT request
   */
  async put(
    endpoint: string,
    options?: { data?: any; headers?: Record<string, string> }
  ): Promise<APIResponse> {
    return await this.context.put(`${this.baseURL}${endpoint}`, {
      data: options?.data,
      headers: options?.headers,
    });
  }

  /**
   * Make DELETE request
   */
  async delete(endpoint: string, options?: { headers?: Record<string, string> }): Promise<APIResponse> {
    return await this.context.delete(`${this.baseURL}${endpoint}`, {
      headers: options?.headers,
    });
  }

  /**
   * Make PATCH request
   */
  async patch(
    endpoint: string,
    options?: { data?: any; headers?: Record<string, string> }
  ): Promise<APIResponse> {
    return await this.context.patch(`${this.baseURL}${endpoint}`, {
      data: options?.data,
      headers: options?.headers,
    });
  }
}

/**
 * Extract authentication token from page context
 * This assumes your app stores auth token in localStorage or cookies
 */
export async function getAuthToken(page: Page): Promise<string | null> {
  // Adjust this based on how your app stores authentication
  // Example for localStorage:
  const token = await page.evaluate(() => {
    return localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
  });

  // Example for cookies:
  // const cookies = await page.context().cookies();
  // const authCookie = cookies.find(c => c.name === 'auth_token');
  // return authCookie?.value || null;

  return token;
}

/**
 * Create authorized API client from authenticated page
 */
export async function createAuthorizedApiClient(
  context: APIRequestContext,
  page: Page
): Promise<ApiClient> {
  const token = await getAuthToken(page);
  if (!token) {
    throw new Error('No authentication token found');
  }

  // Create client with auth header
  const client = new ApiClient(context);
  return client;
}

/**
 * Wait for API call to complete
 */
export async function waitForApiCall(
  page: Page,
  urlPattern: string | RegExp,
  options?: { timeout?: number }
): Promise<Response> {
  const response = await page.waitForResponse(urlPattern, {
    timeout: options?.timeout,
  });
  return response;
}

/**
 * Mock API response
 */
export async function mockApiResponse(
  page: Page,
  urlPattern: string | RegExp,
  responseData: any,
  options?: { status?: number; contentType?: string }
): Promise<void> {
  await page.route(urlPattern, async (route) => {
    await route.fulfill({
      status: options?.status || 200,
      contentType: options?.contentType || 'application/json',
      body: JSON.stringify(responseData),
    });
  });
}

/**
 * Intercept and modify API request
 */
export async function interceptApiRequest(
  page: Page,
  urlPattern: string | RegExp,
  modifier: (request: any) => any
): Promise<void> {
  await page.route(urlPattern, async (route) => {
    const request = route.request();
    const modifiedData = modifier(request);
    await route.continue(modifiedData);
  });
}

/**
 * Assert API response status
 */
export function assertResponseStatus(response: APIResponse, expectedStatus: number): void {
  if (response.status() !== expectedStatus) {
    throw new Error(
      `Expected status ${expectedStatus}, but got ${response.status()}`
    );
  }
}

/**
 * Assert API response contains data
 */
export async function assertResponseContains(
  response: APIResponse,
  expectedData: any
): Promise<void> {
  const body = await response.json();
  const stringBody = JSON.stringify(body);
  const stringExpected = JSON.stringify(expectedData);

  if (!stringBody.includes(stringExpected)) {
    throw new Error(
      `Response does not contain expected data.\nExpected: ${stringExpected}\nActual: ${stringBody}`
    );
  }
}

/**
 * Parse JSON response
 */
export async function parseJsonResponse<T = any>(response: APIResponse): Promise<T> {
  return await response.json();
}

/**
 * Download file from response
 */
export async function downloadFile(
  page: Page,
  downloadPromise: Promise<any>,
  filename?: string
): Promise<string> {
  const download = await downloadPromise;
  const path = await download.path();
  if (filename) {
    await download.saveAs(`./downloads/${filename}`);
  }
  return path || '';
}
