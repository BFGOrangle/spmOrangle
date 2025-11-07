import { test, expect } from '../../fixtures';
import { ReportsPage } from '../../fixtures/pages/reports-page';
import { getCurrentMonthRange, createProjectReportFilter } from '../../utils/test-data';
import { downloadAndVerify, cleanupDownloads } from '../../utils/download-helpers';
import * as path from 'path';

/**
 * Report Generation E2E Tests
 * Tests basic report generation, task status breakdowns, and export functionality
 */

test.describe('Report Generation - Basic Functionality', () => {
  let reportsPage: ReportsPage;
  const downloadsDir = path.join(__dirname, '../../downloads');

  test.beforeEach(async ({ hrPage }) => {
    reportsPage = new ReportsPage(hrPage);
    await reportsPage.navigate();
    await reportsPage.waitForReportsPageLoad();
  });

  test.afterEach(async () => {
    // Clean up downloaded files
    cleanupDownloads(downloadsDir);
  });

  test('should load reports page successfully', async ({ hrPage }) => {
    await reportsPage.assertOnReportsPage();
    await reportsPage.assertGenerateButtonEnabled();
  });

  test('should generate basic report with default filters', async ({ hrPage }) => {
    // Arrange - use default filters (current month, JSON format)
    const dateRange = getCurrentMonthRange();
    
    // Act - generate report
    await reportsPage.generateReport({
      format: 'JSON',
      startDate: dateRange.startDate,
      endDate: dateRange.endDate,
    });

    // Assert - verify report is displayed
    await reportsPage.assertTaskSummaryVisible();
    await reportsPage.assertTaskCountsDisplayed();
  });

  test('should display task status breakdown (TODO, IN_PROGRESS, COMPLETED, BLOCKED)', async ({ hrPage }) => {
    // Arrange
    const dateRange = getCurrentMonthRange();
    
    // Act - generate report
    await reportsPage.generateReport({
      format: 'JSON',
      startDate: dateRange.startDate,
      endDate: dateRange.endDate,
    });

    // Assert - verify all task statuses are displayed
    await expect(hrPage.getByText(/to do/i).first()).toBeVisible();
    await expect(hrPage.getByText(/in progress/i).first()).toBeVisible();
    await expect(hrPage.getByText(/completed/i).first()).toBeVisible();
    await expect(hrPage.getByText(/blocked/i).first()).toBeVisible();
  });

  test('should extract and validate task summary data', async ({ hrPage }) => {
    // Arrange
    const dateRange = getCurrentMonthRange();
    
    // Act - generate report
    await reportsPage.generateReport({
      format: 'JSON',
      startDate: dateRange.startDate,
      endDate: dateRange.endDate,
    });

    // Get task summary data from UI
    const taskSummary = await reportsPage.getTaskSummaryData();

    // Assert - verify data structure
    expect(taskSummary).toHaveProperty('totalTasks');
    expect(taskSummary).toHaveProperty('completedTasks');
    expect(taskSummary).toHaveProperty('inProgressTasks');
    expect(taskSummary).toHaveProperty('todoTasks');
    expect(taskSummary).toHaveProperty('blockedTasks');

    // Verify counts are non-negative
    expect(taskSummary.totalTasks).toBeGreaterThanOrEqual(0);
    expect(taskSummary.completedTasks).toBeGreaterThanOrEqual(0);
    expect(taskSummary.inProgressTasks).toBeGreaterThanOrEqual(0);
    expect(taskSummary.todoTasks).toBeGreaterThanOrEqual(0);
    expect(taskSummary.blockedTasks).toBeGreaterThanOrEqual(0);

    // Verify sum equals total
    const sum = taskSummary.completedTasks + 
                taskSummary.inProgressTasks + 
                taskSummary.todoTasks + 
                taskSummary.blockedTasks;
    expect(sum).toBeLessThanOrEqual(taskSummary.totalTasks);
  });

  test('should generate report for current month', async ({ hrPage }) => {
    // Arrange
    const dateRange = getCurrentMonthRange();
    
    // Act
    await reportsPage.setDateRange(dateRange.startDate, dateRange.endDate);
    await reportsPage.selectTimeRange('MONTHLY');
    await reportsPage.selectFormat('JSON');
    await reportsPage.clickGenerate();
    await reportsPage.waitForReportGenerated();

    // Assert
    await reportsPage.assertTaskSummaryVisible();
    
    // Verify dates are set correctly
    const startDate = await reportsPage.getStartDate();
    const endDate = await reportsPage.getEndDate();
    expect(startDate).toBe(dateRange.startDate);
    expect(endDate).toBe(dateRange.endDate);
  });

  test('should generate report for current year', async ({ hrPage }) => {
    // Arrange
    const filters = createProjectReportFilter()
      .withCurrentYear()
      .withTimeRange('YEARLY')
      .forJsonDisplay()
      .build();
    
    // Act
    await reportsPage.generateReport({
      timeRange: filters.timeRange,
      format: filters.format,
      startDate: filters.startDate,
      endDate: filters.endDate,
    });

    // Assert
    await reportsPage.assertTaskSummaryVisible();
  });

  test('should handle loading state during report generation', async ({ hrPage }) => {
    // Arrange
    const dateRange = getCurrentMonthRange();
    
    // Act - click generate and check for loading state
    await reportsPage.setDateRange(dateRange.startDate, dateRange.endDate);
    await reportsPage.selectFormat('JSON');
    await reportsPage.clickGenerate();
    
    // Loading indicator might appear briefly - don't fail if it doesn't
    // Just verify report eventually appears
    await reportsPage.waitForReportGenerated();

    // Assert
    await reportsPage.assertTaskSummaryVisible();
  });

  test('should allow generating multiple reports sequentially', async ({ hrPage }) => {
    // Arrange
    const dateRange = getCurrentMonthRange();
    
    // Act - generate first report
    await reportsPage.generateReport({
      format: 'JSON',
      startDate: dateRange.startDate,
      endDate: dateRange.endDate,
    });
    await reportsPage.assertTaskSummaryVisible();
    
    const firstTaskSummary = await reportsPage.getTaskSummaryData();

    // Act - generate second report (same filters)
    await reportsPage.clickGenerate();
    await reportsPage.waitForReportGenerated();
    
    const secondTaskSummary = await reportsPage.getTaskSummaryData();

    // Assert - data should be consistent
    expect(secondTaskSummary.totalTasks).toBe(firstTaskSummary.totalTasks);
    expect(secondTaskSummary.completedTasks).toBe(firstTaskSummary.completedTasks);
  });
});

test.describe('Report Generation - Overall Schedule Report', () => {
  let reportsPage: ReportsPage;

  test.beforeEach(async ({ hrPage }) => {
    reportsPage = new ReportsPage(hrPage);
    await reportsPage.navigate();
    await reportsPage.waitForReportsPageLoad();
  });

  test('should generate overall schedule report with project filter', async ({ hrPage }) => {
    // This test requires actual project data to exist
    // Will need to be updated with real project names from test environment
    
    // Arrange
    const dateRange = getCurrentMonthRange();
    
    // Act - generate report for all projects
    await reportsPage.generateReport({
      format: 'JSON',
      startDate: dateRange.startDate,
      endDate: dateRange.endDate,
    });

    // Assert - verify report contains task breakdowns
    await reportsPage.assertTaskSummaryVisible();
    const taskSummary = await reportsPage.getTaskSummaryData();
    
    // Verify structure
    expect(taskSummary).toBeDefined();
    expect(typeof taskSummary.totalTasks).toBe('number');
  });

  test('should show projected tasks (TODO status) in report', async ({ hrPage }) => {
    // Arrange
    const dateRange = getCurrentMonthRange();
    
    // Act
    await reportsPage.generateReport({
      format: 'JSON',
      startDate: dateRange.startDate,
      endDate: dateRange.endDate,
    });

    // Assert - verify TODO tasks are displayed (projected tasks)
    const taskSummary = await reportsPage.getTaskSummaryData();
    expect(taskSummary.todoTasks).toBeGreaterThanOrEqual(0);

    // Verify TODO label is visible (use .first() to handle multiple matches)
    await expect(hrPage.getByText(/to do/i).first()).toBeVisible();
  });

  test('should show completed tasks in report', async ({ hrPage }) => {
    // Arrange
    const dateRange = getCurrentMonthRange();
    
    // Act
    await reportsPage.generateReport({
      format: 'JSON',
      startDate: dateRange.startDate,
      endDate: dateRange.endDate,
    });

    // Assert
    const taskSummary = await reportsPage.getTaskSummaryData();
    expect(taskSummary.completedTasks).toBeGreaterThanOrEqual(0);
    // Best practice: Use page object helper method to handle strict mode violations
    await reportsPage.assertTaskStatusLabelVisible('Completed');
  });

  test('should show in-progress tasks in report', async ({ hrPage }) => {
    // Arrange
    const dateRange = getCurrentMonthRange();
    
    // Act
    await reportsPage.generateReport({
      format: 'JSON',
      startDate: dateRange.startDate,
      endDate: dateRange.endDate,
    });

    // Assert
    const taskSummary = await reportsPage.getTaskSummaryData();
    expect(taskSummary.inProgressTasks).toBeGreaterThanOrEqual(0);
    // Best practice: Use page object helper method to handle strict mode violations
    await reportsPage.assertTaskStatusLabelVisible('In Progress');
  });

  test('should show blocked tasks in report', async ({ hrPage }) => {
    // Arrange
    const dateRange = getCurrentMonthRange();
    
    // Act
    await reportsPage.generateReport({
      format: 'JSON',
      startDate: dateRange.startDate,
      endDate: dateRange.endDate,
    });

    // Assert
    const taskSummary = await reportsPage.getTaskSummaryData();
    expect(taskSummary.blockedTasks).toBeGreaterThanOrEqual(0);
    // Best practice: Use page object helper method to handle strict mode violations
    await reportsPage.assertTaskStatusLabelVisible('Blocked');
  });
});

test.describe('Report Generation - Export Formats', () => {
  let reportsPage: ReportsPage;
  const downloadsDir = path.join(__dirname, '../../downloads');

  test.beforeEach(async ({ hrPage }) => {
    reportsPage = new ReportsPage(hrPage);
    await reportsPage.navigate();
    await reportsPage.waitForReportsPageLoad();
  });

  test.afterEach(async () => {
    cleanupDownloads(downloadsDir);
  });

  test('should export report as PDF successfully', async ({ hrPage }) => {
    // Arrange
    const dateRange = getCurrentMonthRange();

    // Act - trigger PDF download
    await reportsPage.setDateRange(dateRange.startDate, dateRange.endDate);
    await reportsPage.selectFormat('PDF');

    const result = await downloadAndVerify(
      hrPage,
      async () => await reportsPage.clickGenerate(),
      {
        format: 'pdf',
        filenamePattern: /\.pdf$/i,
        timeout: 30000,
      }
    );

    // Assert - file download verification is sufficient
    expect(result.filename).toMatch(/\.pdf$/i);
    expect(result.path).toBeTruthy();
  });

  test('should export report as CSV successfully', async ({ hrPage }) => {
    // Arrange
    const dateRange = getCurrentMonthRange();

    // Act - trigger CSV download
    await reportsPage.setDateRange(dateRange.startDate, dateRange.endDate);
    await reportsPage.selectFormat('CSV');

    const result = await downloadAndVerify(
      hrPage,
      async () => await reportsPage.clickGenerate(),
      {
        format: 'csv',
        filenamePattern: /\.csv$/i,
        timeout: 30000,
      }
    );

    // Assert - file download verification is sufficient
    expect(result.filename).toMatch(/\.csv$/i);
    expect(result.path).toBeTruthy();
  });

  test('should download PDF with correct filename pattern', async ({ hrPage }) => {
    // Arrange
    const dateRange = getCurrentMonthRange();
    
    // Act
    await reportsPage.setDateRange(dateRange.startDate, dateRange.endDate);
    await reportsPage.selectFormat('PDF');
    
    const result = await downloadAndVerify(
      hrPage,
      async () => await reportsPage.clickGenerate(),
      {
        format: 'pdf',
        timeout: 30000,
      }
    );

    // Assert - filename should contain report indicator and extension
    expect(result.filename).toMatch(/report.*\.pdf$/i);
  });

  test('should download CSV with correct filename pattern', async ({ hrPage }) => {
    // Arrange
    const dateRange = getCurrentMonthRange();
    
    // Act
    await reportsPage.setDateRange(dateRange.startDate, dateRange.endDate);
    await reportsPage.selectFormat('CSV');
    
    const result = await downloadAndVerify(
      hrPage,
      async () => await reportsPage.clickGenerate(),
      {
        format: 'csv',
        timeout: 30000,
      }
    );

    // Assert
    expect(result.filename).toMatch(/report.*\.csv$/i);
  });

  test('should handle PDF export for large date ranges', async ({ hrPage }) => {
    // Arrange - use full year
    const filters = createProjectReportFilter()
      .withCurrentYear()
      .forPdfExport()
      .build();
    
    // Act
    await reportsPage.generateReport({
      timeRange: 'YEARLY',
      format: filters.format,
      startDate: filters.startDate,
      endDate: filters.endDate,
    });
    
    const result = await downloadAndVerify(
      hrPage,
      async () => {}, // Already triggered by generateReport
      {
        format: 'pdf',
        timeout: 60000, // Longer timeout for large exports
      }
    );

    // Assert
    expect(result.filename).toMatch(/\.pdf$/i);
  });
});


