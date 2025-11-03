import { Locator, Page, expect } from '@playwright/test';

/**
 * Custom assertion utilities
 * Reusable assertions for common test scenarios
 */

/**
 * Assert element has specific class
 */
export async function assertHasClass(locator: Locator, className: string): Promise<void> {
  const classes = await locator.getAttribute('class');
  if (!classes?.includes(className)) {
    throw new Error(`Element does not have class "${className}". Classes: ${classes}`);
  }
}

/**
 * Assert element does not have specific class
 */
export async function assertNotHasClass(locator: Locator, className: string): Promise<void> {
  const classes = await locator.getAttribute('class');
  if (classes?.includes(className)) {
    throw new Error(`Element should not have class "${className}". Classes: ${classes}`);
  }
}

/**
 * Assert URL matches pattern
 */
export async function assertUrlMatches(page: Page, pattern: RegExp): Promise<void> {
  const url = page.url();
  if (!pattern.test(url)) {
    throw new Error(`URL "${url}" does not match pattern ${pattern}`);
  }
}

/**
 * Assert page has no console errors
 */
export async function assertNoConsoleErrors(page: Page): Promise<void> {
  const errors: string[] = [];
  page.on('console', (msg) => {
    if (msg.type() === 'error') {
      errors.push(msg.text());
    }
  });

  if (errors.length > 0) {
    throw new Error(`Console errors detected:\n${errors.join('\n')}`);
  }
}

/**
 * Assert download was triggered
 */
export async function assertDownloadTriggered(
  page: Page,
  action: () => Promise<void>,
  expectedFilename?: string
): Promise<string> {
  const downloadPromise = page.waitForEvent('download');
  await action();
  const download = await downloadPromise;
  const filename = download.suggestedFilename();

  if (expectedFilename && filename !== expectedFilename) {
    throw new Error(`Expected filename "${expectedFilename}", got "${filename}"`);
  }

  return filename;
}

/**
 * Assert table has specific number of rows
 */
export async function assertTableRowCount(
  tableLocator: Locator,
  expectedCount: number
): Promise<void> {
  const rows = tableLocator.locator('tbody tr');
  await expect(rows).toHaveCount(expectedCount);
}

/**
 * Assert table contains specific text in any cell
 */
export async function assertTableContainsText(
  tableLocator: Locator,
  text: string
): Promise<void> {
  await expect(tableLocator).toContainText(text);
}

/**
 * Assert select/dropdown has specific options
 */
export async function assertSelectHasOptions(
  selectLocator: Locator,
  expectedOptions: string[]
): Promise<void> {
  const options = await selectLocator.locator('option').allTextContents();
  for (const expectedOption of expectedOptions) {
    if (!options.includes(expectedOption)) {
      throw new Error(
        `Expected option "${expectedOption}" not found. Available: ${options.join(', ')}`
      );
    }
  }
}

/**
 * Assert localStorage has specific key
 */
export async function assertLocalStorageHasKey(page: Page, key: string): Promise<void> {
  const value = await page.evaluate((k) => localStorage.getItem(k), key);
  if (value === null) {
    throw new Error(`localStorage does not have key "${key}"`);
  }
}

/**
 * Assert localStorage has specific value
 */
export async function assertLocalStorageValue(
  page: Page,
  key: string,
  expectedValue: string
): Promise<void> {
  const value = await page.evaluate((k) => localStorage.getItem(k), key);
  if (value !== expectedValue) {
    throw new Error(
      `localStorage key "${key}" has value "${value}", expected "${expectedValue}"`
    );
  }
}

/**
 * Assert cookie exists
 */
export async function assertCookieExists(page: Page, cookieName: string): Promise<void> {
  const cookies = await page.context().cookies();
  const cookie = cookies.find((c) => c.name === cookieName);
  if (!cookie) {
    throw new Error(`Cookie "${cookieName}" not found`);
  }
}

/**
 * Assert cookie has specific value
 */
export async function assertCookieValue(
  page: Page,
  cookieName: string,
  expectedValue: string
): Promise<void> {
  const cookies = await page.context().cookies();
  const cookie = cookies.find((c) => c.name === cookieName);
  if (!cookie) {
    throw new Error(`Cookie "${cookieName}" not found`);
  }
  if (cookie.value !== expectedValue) {
    throw new Error(
      `Cookie "${cookieName}" has value "${cookie.value}", expected "${expectedValue}"`
    );
  }
}

/**
 * Assert form validation error is displayed
 */
export async function assertFormValidationError(
  locator: Locator,
  errorMessage?: string
): Promise<void> {
  await expect(locator).toBeVisible();
  if (errorMessage) {
    await expect(locator).toContainText(errorMessage);
  }
}

/**
 * Assert loading state
 */
export async function assertLoading(locator: Locator): Promise<void> {
  await expect(locator).toBeVisible();
  // Common loading indicators
  const hasLoadingText = await locator.textContent();
  if (!hasLoadingText?.toLowerCase().includes('loading')) {
    // Check for spinner or loading class
    const hasLoadingClass = await locator.getAttribute('class');
    if (!hasLoadingClass?.includes('loading') && !hasLoadingClass?.includes('spinner')) {
      throw new Error('Element does not appear to be in loading state');
    }
  }
}

/**
 * Assert not loading state
 */
export async function assertNotLoading(locator: Locator): Promise<void> {
  const isVisible = await locator.isVisible().catch(() => false);
  if (isVisible) {
    throw new Error('Loading indicator should not be visible');
  }
}

/**
 * Assert toast/notification is displayed
 */
export async function assertToastDisplayed(
  page: Page,
  message?: string,
  type?: 'success' | 'error' | 'warning' | 'info'
): Promise<void> {
  // Adjust selector based on your toast/notification library
  const toast = page.locator('[role="status"], [role="alert"], .toast, .notification');
  await expect(toast).toBeVisible();

  if (message) {
    await expect(toast).toContainText(message);
  }

  if (type) {
    // Adjust this based on your toast implementation
    await assertHasClass(toast, type);
  }
}

/**
 * Assert API call was made with specific parameters
 */
export async function assertApiCallMade(
  page: Page,
  urlPattern: string | RegExp,
  method?: string
): Promise<void> {
  let callMade = false;
  page.on('request', (request) => {
    const url = request.url();
    const matchesPattern = typeof urlPattern === 'string'
      ? url.includes(urlPattern)
      : urlPattern.test(url);

    if (matchesPattern && (!method || request.method() === method)) {
      callMade = true;
    }
  });

  // Wait a bit for the call to be made
  await page.waitForTimeout(1000);

  if (!callMade) {
    throw new Error(
      `Expected API call to ${urlPattern} ${method ? `with method ${method}` : ''} was not made`
    );
  }
}

/**
 * Assert element is in viewport
 */
export async function assertInViewport(locator: Locator): Promise<void> {
  const isInViewport = await locator.evaluate((element) => {
    const rect = element.getBoundingClientRect();
    return (
      rect.top >= 0 &&
      rect.left >= 0 &&
      rect.bottom <= window.innerHeight &&
      rect.right <= window.innerWidth
    );
  });

  if (!isInViewport) {
    throw new Error('Element is not in viewport');
  }
}

/**
 * Assert chart/canvas is rendered
 */
export async function assertChartRendered(chartLocator: Locator): Promise<void> {
  await expect(chartLocator).toBeVisible();

  // Check if it's a canvas element with content
  const tagName = await chartLocator.evaluate((el) => el.tagName.toLowerCase());
  if (tagName === 'canvas') {
    const hasContent = await chartLocator.evaluate((canvas: HTMLCanvasElement) => {
      const ctx = canvas.getContext('2d');
      if (!ctx) return false;
      const imageData = ctx.getImageData(0, 0, canvas.width, canvas.height);
      return imageData.data.some((channel) => channel !== 0);
    });

    if (!hasContent) {
      throw new Error('Chart canvas is empty');
    }
  }
}
