import { Page } from '@playwright/test';
import { BasePage } from './base-page';
import { TestUser } from '../../config/test-users';

/**
 * Sign In Page Object
 * Handles all interactions with the signin page
 */
export class SigninPage extends BasePage {
  // Route
  private readonly route = '/auth/signin';

  // Selectors
  private readonly emailInput = () => this.page.locator('#email');
  private readonly passwordInput = () => this.page.locator('#password');
  private readonly signInButton = () => this.getByRole('button', { name: /sign in/i });
  private readonly errorCallout = () => this.page.locator('[role="alert"]').filter({ hasText: /error|incorrect|invalid|failed/i }).first();
  private readonly forgotPasswordLink = () => this.getByRole('link', { name: /reset password/i });
  private readonly signUpLink = () => this.getByRole('link', { name: /sign up/i });
  private readonly welcomeText = () => this.getByText('Welcome Back');

  constructor(page: Page) {
    super(page);
  }

  /**
   * Navigate to signin page
   * Best practice: Wait for specific element instead of networkidle
   */
  async navigate(): Promise<void> {
    await this.goto(this.route);
    // Wait for signin page element instead of networkidle
    await this.welcomeText().waitFor({ state: 'visible', timeout: 30000 });
  }

  /**
   * Fill email field
   */
  async fillEmail(email: string): Promise<void> {
    await this.fill(this.emailInput(), email);
  }

  /**
   * Fill password field
   */
  async fillPassword(password: string): Promise<void> {
    await this.fill(this.passwordInput(), password);
  }

  /**
   * Click sign in button
   */
  async clickSignIn(): Promise<void> {
    await this.click(this.signInButton());
  }

  /**
   * Complete sign in with credentials
   */
  async signIn(email: string, password: string): Promise<void> {
    await this.fillEmail(email);
    await this.fillPassword(password);
    await this.clickSignIn();
  }

  /**
   * Sign in with test user object
   */
  async signInWithUser(user: TestUser): Promise<void> {
    await this.signIn(user.email, user.password);
  }

  /**
   * Wait for successful sign in (redirect to dashboard)
   */
  async waitForSuccessfulSignIn(timeout?: number): Promise<void> {
    // Increased default timeout for authentication
    const defaultTimeout = timeout || 60000;
    await this.waitForUrl(/\/dashboard/, defaultTimeout);
  }

  /**
   * Get error message
   */
  async getErrorMessage(): Promise<string | null> {
    try {
      // Try to find error message with specific text filter
      const errorElement = this.page.locator('[role="alert"]').filter({ hasText: /error|incorrect|invalid|failed/i }).first();
      if (await errorElement.isVisible({ timeout: 2000 })) {
        return await errorElement.textContent();
      }
    } catch {
      // Error element not visible or doesn't exist
    }
    
    // Try alternative selector - look for error text
    try {
      const altError = this.page.locator('text=/error|incorrect|invalid|failed/i').first();
      if (await altError.isVisible({ timeout: 1000 })) {
        return await altError.textContent();
      }
    } catch {
      // No error found
    }
    
    return null;
  }

  /**
   * Check if error is displayed
   */
  async hasError(): Promise<boolean> {
    return await this.isVisible(this.errorCallout());
  }

  /**
   * Click forgot password link
   */
  async clickForgotPassword(): Promise<void> {
    await this.click(this.forgotPasswordLink());
  }

  /**
   * Click sign up link
   */
  async clickSignUp(): Promise<void> {
    await this.click(this.signUpLink());
  }

  /**
   * Check if sign in button is disabled
   */
  async isSignInButtonDisabled(): Promise<boolean> {
    const button = this.signInButton();
    const disabled = await button.getAttribute('disabled');
    return disabled !== null;
  }

  /**
   * Check if on signin page
   */
  async isOnSigninPage(): Promise<boolean> {
    return await this.isVisible(this.welcomeText());
  }

  /**
   * Assert signin page is displayed
   */
  async assertOnSigninPage(): Promise<void> {
    await this.assertVisible(this.welcomeText());
    await this.assertUrlContains('/auth/signin');
  }

  /**
   * Assert error message is displayed
   */
  async assertErrorDisplayed(expectedMessage?: string): Promise<void> {
    await this.assertVisible(this.errorCallout());
    if (expectedMessage) {
      await this.assertContainsText(this.errorCallout(), expectedMessage);
    }
  }

  /**
   * Assert no error is displayed
   */
  async assertNoError(): Promise<void> {
    await this.assertHidden(this.errorCallout());
  }

  /**
   * Complete full signin flow and wait for success
   */
  async completeSignIn(email: string, password: string): Promise<void> {
    await this.navigate();
    await this.signIn(email, password);
    await this.waitForSuccessfulSignIn();
  }

  /**
   * Complete full signin flow with test user
   */
  async completeSignInWithUser(user: TestUser): Promise<void> {
    await this.navigate();
    
    // Ensure we're on the signin page
    await this.assertOnSigninPage();
    
    // Fill in credentials
    await this.fillEmail(user.email);
    await this.fillPassword(user.password);
    
    // Wait for sign in button to be enabled
    const signInBtn = this.signInButton();
    await signInBtn.waitFor({ state: 'visible' });
    
    // Click sign in button (form submission)
    await signInBtn.click();
    
    // Wait for either navigation or error to appear
    try {
      // Wait for navigation to dashboard with a reasonable timeout
      await this.page.waitForURL(/\/dashboard/, { timeout: 60000 });
    } catch (error) {
      // Navigation didn't happen - check for errors
      await this.page.waitForTimeout(3000); // Give time for error to appear
      
      const errorMessage = await this.getErrorMessage();
      if (errorMessage) {
        throw new Error(`Sign-in failed: ${errorMessage}`);
      }
      
      // Check current URL
      const currentUrl = this.page.url();
      if (currentUrl.includes('/auth/signin')) {
        throw new Error(`Sign-in failed: Still on signin page after timeout. Current URL: ${currentUrl}`);
      }
      
      // If redirected elsewhere, wait for dashboard
      await this.page.waitForURL(/\/dashboard/, { timeout: 5000 });
    }
  }
}
