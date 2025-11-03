import { Page, Locator, expect } from '@playwright/test';

/**
 * Base Page Object
 * Contains common functionality shared across all pages
 * All page objects should extend this class
 */
export class BasePage {
  protected page: Page;

  constructor(page: Page) {
    this.page = page;
  }

  /**
   * Navigate to a specific path
   */
  async goto(path: string): Promise<void> {
    await this.page.goto(path);
  }

  /**
   * Wait for page to be fully loaded
   * Best practice: Use 'load' instead of 'networkidle' because:
   * - networkidle waits for 500ms of no network activity (unreliable in SPAs with WebSockets, polling, etc.)
   * - 'load' waits for the load event which is more reliable for modern SPAs
   */
  async waitForPageLoad(): Promise<void> {
    await this.page.waitForLoadState('load');
  }

  /**
   * Wait for DOM content to be loaded (faster alternative)
   * Use this when you only need DOM to be ready, not all resources
   */
  async waitForDOMContentLoaded(): Promise<void> {
    await this.page.waitForLoadState('domcontentloaded');
  }

  /**
   * Get element by test ID
   * Encourages use of data-testid for stable selectors
   */
  getByTestId(testId: string): Locator {
    return this.page.getByTestId(testId);
  }

  /**
   * Get element by role
   */
  getByRole(
    role: Parameters<Page['getByRole']>[0],
    options?: Parameters<Page['getByRole']>[1]
  ): Locator {
    return this.page.getByRole(role, options);
  }

  /**
   * Get element by text
   */
  getByText(text: string | RegExp, options?: { exact?: boolean }): Locator {
    return this.page.getByText(text, options);
  }

  /**
   * Get element by label
   */
  getByLabel(text: string | RegExp): Locator {
    return this.page.getByLabel(text);
  }

  /**
   * Get element by placeholder
   */
  getByPlaceholder(text: string | RegExp): Locator {
    return this.page.getByPlaceholder(text);
  }

  /**
   * Click on an element
   */
  async click(locator: Locator): Promise<void> {
    await locator.click();
  }

  /**
   * Fill input field
   */
  async fill(locator: Locator, value: string): Promise<void> {
    await locator.fill(value);
  }

  /**
   * Select option from dropdown
   */
  async selectOption(locator: Locator, value: string): Promise<void> {
    await locator.selectOption(value);
  }

  /**
   * Wait for element to be visible
   */
  async waitForVisible(locator: Locator, timeout?: number): Promise<void> {
    await locator.waitFor({ state: 'visible', timeout });
  }

  /**
   * Wait for element to be hidden
   */
  async waitForHidden(locator: Locator, timeout?: number): Promise<void> {
    await locator.waitFor({ state: 'hidden', timeout });
  }

  /**
   * Check if element is visible
   */
  async isVisible(locator: Locator): Promise<boolean> {
    return await locator.isVisible();
  }

  /**
   * Get current URL
   */
  async getCurrentUrl(): Promise<string> {
    return this.page.url();
  }

  /**
   * Wait for URL to match pattern
   */
  async waitForUrl(urlPattern: string | RegExp, timeout?: number): Promise<void> {
    await this.page.waitForURL(urlPattern, { timeout });
  }

  /**
   * Reload the current page
   */
  async reload(): Promise<void> {
    await this.page.reload();
  }

  /**
   * Take a screenshot
   */
  async screenshot(options?: { path?: string; fullPage?: boolean }): Promise<Buffer> {
    return await this.page.screenshot(options);
  }

  /**
   * Wait for a specific time (use sparingly, prefer waitFor methods)
   */
  async wait(ms: number): Promise<void> {
    await this.page.waitForTimeout(ms);
  }

  /**
   * Assert page title
   */
  async assertTitle(title: string | RegExp): Promise<void> {
    await expect(this.page).toHaveTitle(title);
  }

  /**
   * Assert URL contains text
   */
  async assertUrlContains(text: string): Promise<void> {
    await expect(this.page).toHaveURL(new RegExp(text));
  }

  /**
   * Assert element is visible
   */
  async assertVisible(locator: Locator): Promise<void> {
    await expect(locator).toBeVisible();
  }

  /**
   * Assert element is hidden
   */
  async assertHidden(locator: Locator): Promise<void> {
    await expect(locator).toBeHidden();
  }

  /**
   * Assert element has text
   */
  async assertText(locator: Locator, text: string | RegExp): Promise<void> {
    await expect(locator).toHaveText(text);
  }

  /**
   * Assert element contains text
   */
  async assertContainsText(locator: Locator, text: string | RegExp): Promise<void> {
    await expect(locator).toContainText(text);
  }

  /**
   * Assert element has value
   */
  async assertValue(locator: Locator, value: string | RegExp): Promise<void> {
    await expect(locator).toHaveValue(value);
  }

  /**
   * Assert element is enabled
   */
  async assertEnabled(locator: Locator): Promise<void> {
    await expect(locator).toBeEnabled();
  }

  /**
   * Assert element is disabled
   */
  async assertDisabled(locator: Locator): Promise<void> {
    await expect(locator).toBeDisabled();
  }

  /**
   * Assert element count
   */
  async assertCount(locator: Locator, count: number): Promise<void> {
    await expect(locator).toHaveCount(count);
  }

  /**
   * Wait for API response
   */
  async waitForResponse(urlPattern: string | RegExp, timeout?: number): Promise<void> {
    await this.page.waitForResponse(urlPattern, { timeout });
  }

  /**
   * Wait for navigation
   */
  async waitForNavigation(options?: { url?: string | RegExp; timeout?: number }): Promise<void> {
    await this.page.waitForURL(options?.url || /.*/, { timeout: options?.timeout });
  }

  /**
   * Get page title
   */
  async getTitle(): Promise<string> {
    return await this.page.title();
  }

  /**
   * Press keyboard key
   */
  async pressKey(key: string): Promise<void> {
    await this.page.keyboard.press(key);
  }

  /**
   * Hover over element
   */
  async hover(locator: Locator): Promise<void> {
    await locator.hover();
  }

  /**
   * Double click element
   */
  async doubleClick(locator: Locator): Promise<void> {
    await locator.dblclick();
  }

  /**
   * Right click element
   */
  async rightClick(locator: Locator): Promise<void> {
    await locator.click({ button: 'right' });
  }

  /**
   * Check checkbox or radio button
   */
  async check(locator: Locator): Promise<void> {
    await locator.check();
  }

  /**
   * Uncheck checkbox
   */
  async uncheck(locator: Locator): Promise<void> {
    await locator.uncheck();
  }

  /**
   * Get element count
   */
  async getCount(locator: Locator): Promise<number> {
    return await locator.count();
  }

  /**
   * Get text content
   */
  async getText(locator: Locator): Promise<string | null> {
    return await locator.textContent();
  }

  /**
   * Get all text contents
   */
  async getAllTexts(locator: Locator): Promise<string[]> {
    return await locator.allTextContents();
  }

  /**
   * Get attribute value
   */
  async getAttribute(locator: Locator, name: string): Promise<string | null> {
    return await locator.getAttribute(name);
  }

  /**
   * Wait for selector
   */
  async waitForSelector(selector: string, options?: { state?: 'attached' | 'detached' | 'visible' | 'hidden'; timeout?: number }): Promise<void> {
    if (options) {
      await this.page.waitForSelector(selector, options);
    } else {
      await this.page.waitForSelector(selector);
    }
  }

  /**
   * Execute custom JavaScript
   */
  async evaluate<T>(pageFunction: () => T): Promise<T> {
    return await this.page.evaluate(pageFunction);
  }

  /**
   * Scroll element into view
   */
  async scrollIntoView(locator: Locator): Promise<void> {
    await locator.scrollIntoViewIfNeeded();
  }

  /**
   * Focus on element
   */
  async focus(locator: Locator): Promise<void> {
    await locator.focus();
  }

  /**
   * Blur element
   */
  async blur(locator: Locator): Promise<void> {
    await locator.blur();
  }
}
