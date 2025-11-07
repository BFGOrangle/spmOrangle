import { Page, Locator, expect } from '@playwright/test';
import { BasePage } from './base-page';

/**
 * Reports Page Object
 * Encapsulates interactions with the reports page
 */
export class ReportsPage extends BasePage {
  private route = '/reports';

  constructor(page: Page) {
    super(page);
  }

  /**
   * Selectors - using user-facing methods and data-testid where appropriate
   */
  private get departmentSelect(): Locator {
    return this.getByLabel('Department');
  }

  private get projectSelect(): Locator {
    return this.getByLabel('Projects');
  }

  private get timeRangeSelect(): Locator {
    return this.getByLabel('Time Range');
  }

  private get exportFormatSelect(): Locator {
    return this.getByLabel('Export Format');
  }

  private get startDateInput(): Locator {
    return this.page.locator('#startDate');
  }

  private get endDateInput(): Locator {
    return this.page.locator('#endDate');
  }

  private get generateButton(): Locator {
    return this.getByRole('button', { name: /generate report/i });
  }

  private get loadingIndicator(): Locator {
    return this.getByText(/generating/i);
  }

  private get reportFiltersHeading(): Locator {
    return this.getByText('Report Filters');
  }

  private get taskSummarySection(): Locator {
    return this.page.locator('text=Task Summary').locator('..');
  }

  /**
   * Navigate to the reports page
   * Best practice: goto() already waits for navigation, we just need to wait for elements
   */
  async navigate(): Promise<void> {
    await this.goto(this.route);
    // Wait for specific page element instead of networkidle
    // This is more reliable than waiting for network to be idle
    await this.reportFiltersHeading.waitFor({ state: 'visible', timeout: 30000 });
  }

  /**
   * Wait for the reports page to be fully loaded
   * Best practice: Use element-based wait instead of networkidle
   */
  async waitForReportsPageLoad(): Promise<void> {
    // assertVisible already waits for the element, no need for additional waitForPageLoad
    await this.assertVisible(this.reportFiltersHeading);
    // Optional: Wait for DOM to be ready (faster than networkidle)
    await this.waitForDOMContentLoaded();
  }

  /**
   * Select a department from the dropdown
   */
  async selectDepartment(department: string): Promise<void> {
    await this.click(this.departmentSelect);
    await this.page.getByRole('option', { name: department }).click();
  }

  /**
   * Select a project from the dropdown
   */
  async selectProject(projectName: string): Promise<void> {
    await this.click(this.projectSelect);
    await this.page.getByRole('option', { name: projectName }).click();
  }

  /**
   * Select all projects
   */
  async selectAllProjects(): Promise<void> {
    await this.click(this.projectSelect);
    await this.page.getByRole('option', { name: 'All Projects' }).click();
  }

  /**
   * Select time range
   */
  async selectTimeRange(timeRange: 'WEEKLY' | 'MONTHLY' | 'QUARTERLY' | 'YEARLY' | 'CUSTOM'): Promise<void> {
    await this.click(this.timeRangeSelect);
    
    const timeRangeLabels: Record<string, string> = {
      'WEEKLY': 'Weekly',
      'MONTHLY': 'Monthly',
      'QUARTERLY': 'Quarterly',
      'YEARLY': 'Yearly',
      'CUSTOM': 'Custom'
    };
    
    await this.page.getByRole('option', { name: timeRangeLabels[timeRange] }).click();
  }

  /**
   * Select export format
   */
  async selectFormat(format: 'JSON' | 'PDF' | 'CSV'): Promise<void> {
    await this.click(this.exportFormatSelect);
    
    const formatLabels: Record<string, RegExp> = {
      'JSON': /view online.*json/i,
      'PDF': /pdf/i,
      'CSV': /csv/i
    };
    
    await this.page.getByRole('option', { name: formatLabels[format] }).click();
  }

  /**
   * Set start date
   */
  async setStartDate(date: string): Promise<void> {
    await this.fill(this.startDateInput, date);
  }

  /**
   * Set end date
   */
  async setEndDate(date: string): Promise<void> {
    await this.fill(this.endDateInput, date);
  }

  /**
   * Set date range
   */
  async setDateRange(startDate: string, endDate: string): Promise<void> {
    await this.setStartDate(startDate);
    await this.setEndDate(endDate);
  }

  /**
   * Click the generate button
   */
  async clickGenerate(): Promise<void> {
    await this.click(this.generateButton);
  }

  /**
   * Wait for report generation to complete
   * Best practice: Use element-based wait instead of networkidle
   * networkidle is unreliable in SPAs with WebSockets, polling, or continuous network activity
   */
  async waitForReportGenerated(timeout: number = 30000): Promise<void> {
    // Wait for loading indicator to appear (optional, might not always appear)
    try {
      await this.loadingIndicator.waitFor({ state: 'visible', timeout: 2000 });
      // Wait for it to disappear
      await this.loadingIndicator.waitFor({ state: 'hidden', timeout });
    } catch {
      // Loading indicator might not appear for fast responses, that's ok
    }
    
    // Best practice: Wait for the actual report element instead of networkidle
    // This is more reliable and faster than waiting for network to be idle
    await this.taskSummarySection.waitFor({ state: 'visible', timeout });
    
    // Optional: Wait for DOM to be stable (much faster than networkidle)
    // Only wait for 'load' if taskSummarySection wait didn't catch it
    try {
      await this.page.waitForLoadState('load', { timeout: 5000 });
    } catch {
      // If load already happened, that's fine
    }
  }

  /**
   * Generate a report with specified filters
   */
  async generateReport(options: {
    department?: string;
    project?: string;
    timeRange?: 'WEEKLY' | 'MONTHLY' | 'QUARTERLY' | 'YEARLY' | 'CUSTOM';
    format?: 'JSON' | 'PDF' | 'CSV';
    startDate?: string;
    endDate?: string;
  }): Promise<void> {
    if (options.department) {
      await this.selectDepartment(options.department);
    }
    
    if (options.project) {
      await this.selectProject(options.project);
    }
    
    if (options.timeRange) {
      await this.selectTimeRange(options.timeRange);
    }
    
    if (options.format) {
      await this.selectFormat(options.format);
    }
    
    if (options.startDate) {
      await this.setStartDate(options.startDate);
    }
    
    if (options.endDate) {
      await this.setEndDate(options.endDate);
    }
    
    await this.clickGenerate();
    
    // Only wait for report if format is JSON (otherwise it's a download)
    if (!options.format || options.format === 'JSON') {
      await this.waitForReportGenerated();
    }
  }

  /**
   * Get task summary data from the displayed report (JSON format only)
   * Uses data-testid selectors for reliable data extraction
   */
  async getTaskSummaryData(): Promise<{
    totalTasks: number;
    completedTasks: number;
    inProgressTasks: number;
    todoTasks: number;
    blockedTasks: number;
  }> {
    // Wait for task summary section to be visible
    await this.assertVisible(this.taskSummarySection);

    // Extract task counts using data-testid selectors (more reliable than text-based selectors)
    const totalTasksText = await this.page.getByTestId('task-summary-total-value').textContent({ timeout: 5000 });
    const completedTasksText = await this.page.getByTestId('task-summary-completed-value').textContent({ timeout: 5000 });
    const inProgressTasksText = await this.page.getByTestId('task-summary-in-progress-value').textContent({ timeout: 5000 });
    const blockedTasksText = await this.page.getByTestId('task-summary-blocked-value').textContent({ timeout: 5000 });

    const totalTasks = parseInt(totalTasksText?.trim() || '0', 10);
    const completedTasks = parseInt(completedTasksText?.trim() || '0', 10);
    const inProgressTasks = parseInt(inProgressTasksText?.trim() || '0', 10);
    const blockedTasks = parseInt(blockedTasksText?.trim() || '0', 10);

    // Calculate TODO tasks: total - (completed + inProgress + blocked)
    // Since TODO card is not displayed in UI, we calculate it from other values
    const todoTasks = Math.max(0, totalTasks - (completedTasks + inProgressTasks + blockedTasks));

    return {
      totalTasks,
      completedTasks,
      inProgressTasks,
      todoTasks,
      blockedTasks,
    };
  }

  /**
   * Assert that task summary is visible
   */
  async assertTaskSummaryVisible(): Promise<void> {
    await this.assertVisible(this.taskSummarySection);
  }

  /**
   * Assert that task counts are displayed
   * Best practice: Use .first() to handle multiple elements with same text (card titles vs table headers)
   */
  async assertTaskCountsDisplayed(): Promise<void> {
    // Use .first() to handle multiple elements with same text
    await expect(this.page.getByText(/total tasks/i).first()).toBeVisible();
    await expect(this.page.getByText(/completed/i).first()).toBeVisible();
    await expect(this.page.getByText(/in progress/i).first()).toBeVisible();
  }

  /**
   * Assert that a specific task status label is visible in the task summary cards
   * Best practice: Target card titles specifically using data-slot attribute for more reliable selectors
   */
  async assertTaskStatusLabelVisible(status: 'Completed' | 'In Progress' | 'Blocked' | 'To Do' | 'TODO'): Promise<void> {
    // First try to find in card title (more specific selector)
    const cardTitleSelector = this.page.locator('[data-slot="card-title"]').filter({ hasText: new RegExp(status, 'i') });
    
    try {
      await expect(cardTitleSelector.first()).toBeVisible({ timeout: 5000 });
    } catch {
      // Fallback: use general text search with .first()
      await expect(this.page.getByText(new RegExp(status, 'i')).first()).toBeVisible();
    }
  }

  /**
   * Assert that a specific task count matches expected value
   */
  async assertTaskCount(label: string, expectedCount: number): Promise<void> {
    const element = this.page.locator(`text=${label}`).locator('..').locator(`text=${expectedCount}`);
    await expect(element).toBeVisible();
  }

  /**
   * Assert that user is on the reports page
   */
  async assertOnReportsPage(): Promise<void> {
    await this.assertUrlContains('/reports');
    await this.assertVisible(this.reportFiltersHeading);
  }

  /**
   * Assert that generate button is enabled
   */
  async assertGenerateButtonEnabled(): Promise<void> {
    await this.assertEnabled(this.generateButton);
  }

  /**
   * Assert that generate button is disabled
   */
  async assertGenerateButtonDisabled(): Promise<void> {
    await this.assertDisabled(this.generateButton);
  }

  /**
   * Get current department selection
   */
  async getCurrentDepartment(): Promise<string | null> {
    return await this.departmentSelect.textContent();
  }

  /**
   * Get current project selection
   */
  async getCurrentProject(): Promise<string | null> {
    return await this.projectSelect.textContent();
  }

  /**
   * Get current format selection
   */
  async getCurrentFormat(): Promise<string | null> {
    return await this.exportFormatSelect.textContent();
  }

  /**
   * Get start date value
   */
  async getStartDate(): Promise<string> {
    return await this.startDateInput.inputValue();
  }

  /**
   * Get end date value
   */
  async getEndDate(): Promise<string> {
    return await this.endDateInput.inputValue();
  }

  /**
   * Check if report data is displayed (for JSON format)
   */
  async isReportDataDisplayed(): Promise<boolean> {
    return await this.taskSummarySection.isVisible();
  }

  /**
   * Wait for toast notification
   * Best practice: Target Radix UI toast (used in this app) with multiple selector strategies
   * The app uses @radix-ui/react-toast, not Sonner
   */
  async waitForToast(message: string | RegExp, timeout: number = 10000): Promise<void> {
    // Radix UI toast selectors (this app uses Radix UI, not Sonner)
    // Radix UI toasts have data-state="open" when visible
    const toastSelectors = [
      // Radix UI toast with data-state="open" (primary selector)
      this.page.locator('[data-state="open"]').filter({ hasText: message }),
      // Radix UI toast viewport children (toast container)
      this.page.locator('[role="status"][data-state="open"]').filter({ hasText: message }),
      // Radix UI toast by role within viewport
      this.page.locator('[role="status"]').filter({ hasText: message }),
      // Generic toast pattern (fallback)
      this.page.locator('[class*="toast"]').filter({ hasText: message }),
      // Sonner toast library (in case other pages use it)
      this.page.locator('[data-sonner-toast]').filter({ hasText: message }),
    ];
    
    // Try each selector strategy with increasing timeout
    const perSelectorTimeout = Math.max(2000, timeout / toastSelectors.length);
    for (const selector of toastSelectors) {
      try {
        // Wait for toast to be visible and contain the message
        await selector.first().waitFor({ state: 'visible', timeout: perSelectorTimeout });
        
        // Additional verification: ensure toast has data-state="open" or is actually visible
        const isVisible = await selector.first().isVisible();
        if (isVisible) {
          return; // Success, exit early
        }
      } catch {
        // Continue to next selector
      }
    }
    
    // Final fallback: use page-level text search
    // This is less reliable but will catch toasts even if structure is different
    try {
      const toast = this.page.getByText(message).first();
      await toast.waitFor({ state: 'visible', timeout: Math.max(3000, timeout / 3) });
    } catch (error) {
      // If all strategies fail, log for debugging but don't fail the test
      // The file download verification is the primary success indicator
      console.warn(`Toast notification "${message}" not detected within ${timeout}ms. File download verification should confirm success.`);
      // Re-throw to fail the test if toast detection is required
      throw error;
    }
  }
  
  /**
   * Wait for toast notification (optional - doesn't fail test if not found)
   * Useful when file download verification already confirms success
   */
  async waitForToastOptional(message: string | RegExp, timeout: number = 10000): Promise<boolean> {
    try {
      await this.waitForToast(message, timeout);
      return true;
    } catch {
      return false;
    }
  }

  /**
   * Assert error message is displayed
   */
  async assertErrorDisplayed(errorMessage?: string | RegExp): Promise<void> {
    if (errorMessage) {
      await expect(this.page.getByText(errorMessage)).toBeVisible();
    } else {
      // Check for generic error indicators
      await expect(this.page.getByText(/error/i)).toBeVisible();
    }
  }
}


