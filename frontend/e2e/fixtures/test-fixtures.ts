import { test as base, Page } from '@playwright/test';
import { SigninPage } from './pages/signin-page';
import { TEST_USERS, TestUser } from '../config/test-users';

/**
 * Custom test fixtures
 * Extends Playwright's base test with custom fixtures for authentication and page objects
 */

export type TestFixtures = {
  // Authenticated page contexts for different roles
  managerPage: Page;
  staffPage: Page;
  hrPage: Page;

  // Page objects
  signinPage: SigninPage;
};

/**
 * Helper function to authenticate a user and return the page
 */
async function authenticateUser(page: Page, user: TestUser): Promise<Page> {
  const signinPage = new SigninPage(page);
  await signinPage.completeSignInWithUser(user);
  return page;
}

/**
 * Extended test with custom fixtures
 */
export const test = base.extend<TestFixtures>({
  /**
   * Manager authenticated page
   * Use this fixture when you need a manager user session
   *
   * Example:
   * test('manager can generate reports', async ({ managerPage }) => {
   *   await managerPage.goto('/reports');
   *   // test code
   * });
   */
  managerPage: async ({ browser }, use) => {
    const context = await browser.newContext();
    const page = await context.newPage();
    await authenticateUser(page, TEST_USERS.manager);
    
    // Best practice: Wait for page to be ready after authentication
    await page.waitForLoadState('load');
    
    await use(page);
    await context.close();
  },

  /**
   * Staff authenticated page
   * Use this fixture when you need a staff user session
   *
   * Example:
   * test('staff cannot access reports', async ({ staffPage }) => {
   *   await staffPage.goto('/reports');
   *   // test code
   * });
   */
  staffPage: async ({ browser }, use) => {
    const context = await browser.newContext();
    const page = await context.newPage();
    await authenticateUser(page, TEST_USERS.staff);
    
    // Best practice: Wait for page to be ready after authentication
    await page.waitForLoadState('load');
    
    await use(page);
    await context.close();
  },

  /**
   * HR authenticated page
   * Use this fixture when you need an HR user session
   *
   * Example:
   * test('hr can generate reports for any project', async ({ hrPage }) => {
   *   await hrPage.goto('/reports');
   *   // test code
   * });
   */
  hrPage: async ({ browser }, use) => {
    const context = await browser.newContext();
    const page = await context.newPage();
    await authenticateUser(page, TEST_USERS.hr);
    
    // Best practice: Wait for page to be ready after authentication
    // This prevents race conditions where tests start before the page is fully loaded
    await page.waitForLoadState('load');
    
    await use(page);
    await context.close();
  },

  /**
   * Signin page object
   * Use this for tests that need to interact with the signin page
   *
   * Example:
   * test('can sign in', async ({ page, signinPage }) => {
   *   await signinPage.navigate();
   *   await signinPage.signIn('user@test.com', 'password');
   * });
   */
  signinPage: async ({ page }, use) => {
    const signinPage = new SigninPage(page);
    await use(signinPage);
  },
});

/**
 * Export expect from Playwright
 */
export { expect } from '@playwright/test';
