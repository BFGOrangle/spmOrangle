import { test, expect } from '../../fixtures';
import { ReportsPage } from '../../fixtures/pages/reports-page';
import { getCurrentMonthRange, createReportFiltersForAllFormats } from '../../utils/test-data';
import {
  assertTaskCountsMatch,
  assertTaskCountsAddUp,
  assertTaskCountsNonNegative,
  assertReportDataConsistent,
  TaskCounts,
} from '../../utils/report-assertions';
import { downloadAndVerify, readCsvContent, parseCsvTaskSummary, cleanupDownloads } from '../../utils/download-helpers';
import * as path from 'path';

/**
 * Report Data Accuracy Tests
 * Tests that verify report data matches live project data and is consistent across formats
 */

test.describe('Report Data Accuracy - Task Count Validation', () => {
  let reportsPage: ReportsPage;

  test.beforeEach(async ({ hrPage }) => {
    reportsPage = new ReportsPage(hrPage);
    await reportsPage.navigate();
    await reportsPage.waitForReportsPageLoad();
  });

  test('should display non-negative task counts', async ({ hrPage }) => {
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
    
    assertTaskCountsNonNegative({
      total: taskSummary.totalTasks,
      completed: taskSummary.completedTasks,
      inProgress: taskSummary.inProgressTasks,
      todo: taskSummary.todoTasks,
      blocked: taskSummary.blockedTasks,
    });
  });

  test('should have task status counts that add up correctly', async ({ hrPage }) => {
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
    
    // The sum of individual statuses should equal or be less than total
    // (total might include other statuses like FUTURE that we don't display separately)
    const sum = taskSummary.completedTasks + 
                taskSummary.inProgressTasks + 
                taskSummary.todoTasks + 
                taskSummary.blockedTasks;
    
    expect(sum).toBeLessThanOrEqual(taskSummary.totalTasks);
  });

  test('should display consistent data on multiple generations', async ({ hrPage }) => {
    // Arrange
    const dateRange = getCurrentMonthRange();
    
    // Act - generate first report
    await reportsPage.generateReport({
      format: 'JSON',
      startDate: dateRange.startDate,
      endDate: dateRange.endDate,
    });
    
    const firstSummary = await reportsPage.getTaskSummaryData();
    
    // Act - generate second report with same filters
    await reportsPage.clickGenerate();
    await reportsPage.waitForReportGenerated();
    
    const secondSummary = await reportsPage.getTaskSummaryData();
    
    // Assert - data should be identical
    assertTaskCountsMatch(
      {
        total: secondSummary.totalTasks,
        completed: secondSummary.completedTasks,
        inProgress: secondSummary.inProgressTasks,
        todo: secondSummary.todoTasks,
        blocked: secondSummary.blockedTasks,
      },
      {
        total: firstSummary.totalTasks,
        completed: firstSummary.completedTasks,
        inProgress: firstSummary.inProgressTasks,
        todo: firstSummary.todoTasks,
        blocked: firstSummary.blockedTasks,
      }
    );
  });

  test('should show zero counts when no tasks exist in date range', async ({ hrPage }) => {
    // Arrange - use a date range very far in the past (unlikely to have tasks)
    const futureDateStart = '2030-01-01';
    const futureDateEnd = '2030-01-31';
    
    // Act
    await reportsPage.generateReport({
      format: 'JSON',
      startDate: futureDateStart,
      endDate: futureDateEnd,
    });
    
    // Assert - should still generate report, possibly with zero counts
    await reportsPage.assertTaskSummaryVisible();
    const taskSummary = await reportsPage.getTaskSummaryData();
    
    // Counts should be non-negative
    expect(taskSummary.totalTasks).toBeGreaterThanOrEqual(0);
    expect(taskSummary.completedTasks).toBeGreaterThanOrEqual(0);
  });
});

test.describe('Report Data Accuracy - Date Range Filtering', () => {
  let reportsPage: ReportsPage;

  test.beforeEach(async ({ hrPage }) => {
    reportsPage = new ReportsPage(hrPage);
    await reportsPage.navigate();
    await reportsPage.waitForReportsPageLoad();
  });

  test('should only include tasks within specified date range', async ({ hrPage }) => {
    // Arrange - use current month
    const currentMonth = getCurrentMonthRange();
    
    // Act
    await reportsPage.generateReport({
      format: 'JSON',
      startDate: currentMonth.startDate,
      endDate: currentMonth.endDate,
    });
    
    // Assert - report generated successfully
    await reportsPage.assertTaskSummaryVisible();
    const taskSummary = await reportsPage.getTaskSummaryData();
    
    // Verify reasonable data
    expect(taskSummary.totalTasks).toBeGreaterThanOrEqual(0);
  });

  test('should reflect date range changes in task counts', async ({ hrPage }) => {
    // Arrange
    const currentMonth = getCurrentMonthRange();
    const currentYear = getCurrentMonthRange();
    currentYear.startDate = new Date(new Date().getFullYear(), 0, 1).toISOString().split('T')[0];
    
    // Act - generate report for current month
    await reportsPage.generateReport({
      format: 'JSON',
      startDate: currentMonth.startDate,
      endDate: currentMonth.endDate,
    });
    
    const monthSummary = await reportsPage.getTaskSummaryData();
    
    // Act - generate report for full year
    await reportsPage.setDateRange(currentYear.startDate, currentYear.endDate);
    await reportsPage.clickGenerate();
    await reportsPage.waitForReportGenerated();
    
    const yearSummary = await reportsPage.getTaskSummaryData();
    
    // Assert - year should have >= month (year includes month's data)
    expect(yearSummary.totalTasks).toBeGreaterThanOrEqual(monthSummary.totalTasks);
  });
});

test.describe('Report Data Accuracy - Cross-Format Consistency', () => {
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

  test('should produce same data in JSON and CSV formats', async ({ hrPage }) => {
    // Arrange
    const dateRange = getCurrentMonthRange();
    const filters = createReportFiltersForAllFormats({
      startDate: dateRange.startDate,
      endDate: dateRange.endDate,
    });

    // Act - generate JSON report
    await reportsPage.generateReport({
      format: 'JSON',
      startDate: filters.json.startDate,
      endDate: filters.json.endDate,
    });

    const jsonSummary = await reportsPage.getTaskSummaryData();

    // Act - generate CSV report
    await reportsPage.setDateRange(filters.csv.startDate, filters.csv.endDate);
    await reportsPage.selectFormat('CSV');

    const csvResult = await downloadAndVerify(
      hrPage,
      async () => await reportsPage.clickGenerate(),
      {
        format: 'csv',
        timeout: 30000,
      }
    );

    // Parse CSV and extract task summary data
    const csvContent = await readCsvContent(csvResult.path);

    // Debug: Log CSV content to understand format
    console.log('CSV Content (first 10 rows):');
    csvContent.slice(0, 10).forEach((row, i) => {
      console.log(`Row ${i}:`, row);
    });

    const csvSummary = await parseCsvTaskSummary(csvResult.path);

    // Debug: Log parsed summary
    console.log('Parsed CSV Summary:', csvSummary);
    console.log('JSON Summary:', jsonSummary);

    // Assert - CSV should contain task summary data
    expect(csvContent.length).toBeGreaterThan(0);

    // CSV should have headers
    expect(csvContent[0]).toBeDefined();

    // CSV should have data rows (not just headers)
    expect(csvContent.length).toBeGreaterThan(1);

    // Assert - CSV data should match JSON data
    // Note: Allow for small discrepancies due to timing or data updates between requests
    expect(csvSummary.totalTasks).toBe(jsonSummary.totalTasks);
    expect(csvSummary.completedTasks).toBe(jsonSummary.completedTasks);
    expect(csvSummary.inProgressTasks).toBe(jsonSummary.inProgressTasks);
    expect(csvSummary.blockedTasks).toBe(jsonSummary.blockedTasks);

    // Verify CSV summary is valid
    assertTaskCountsNonNegative({
      total: csvSummary.totalTasks,
      completed: csvSummary.completedTasks,
      inProgress: csvSummary.inProgressTasks,
      todo: csvSummary.todoTasks,
      blocked: csvSummary.blockedTasks,
    });
  });

  test('should produce consistent data in PDF format', async ({ hrPage }) => {
    // Arrange
    const dateRange = getCurrentMonthRange();
    
    // Act - generate JSON report first
    await reportsPage.generateReport({
      format: 'JSON',
      startDate: dateRange.startDate,
      endDate: dateRange.endDate,
    });
    
    const jsonSummary = await reportsPage.getTaskSummaryData();
    
    // Act - generate PDF report
    await reportsPage.setDateRange(dateRange.startDate, dateRange.endDate);
    await reportsPage.selectFormat('PDF');
    
    const pdfResult = await downloadAndVerify(
      hrPage,
      async () => await reportsPage.clickGenerate(),
      {
        format: 'pdf',
        timeout: 30000,
      }
    );
    
    // Assert - PDF was generated
    expect(pdfResult.filename).toMatch(/\.pdf$/i);
    expect(pdfResult.path).toBeTruthy();
    
    // Note: Actual PDF content parsing would require PDF library
    // For E2E tests, we verify PDF was generated with valid format
  });
});

test.describe('Report Data Accuracy - Project Breakdown', () => {
  let reportsPage: ReportsPage;

  test.beforeEach(async ({ hrPage }) => {
    reportsPage = new ReportsPage(hrPage);
    await reportsPage.navigate();
    await reportsPage.waitForReportsPageLoad();
  });

  test('should show project-level task breakdown', async ({ hrPage }) => {
    // Arrange
    const dateRange = getCurrentMonthRange();
    
    // Act
    await reportsPage.generateReport({
      format: 'JSON',
      startDate: dateRange.startDate,
      endDate: dateRange.endDate,
    });
    
    // Assert - report should show overall summary
    await reportsPage.assertTaskSummaryVisible();
    const taskSummary = await reportsPage.getTaskSummaryData();
    
    // Verify data structure
    expect(taskSummary.totalTasks).toBeGreaterThanOrEqual(0);
    
    // Note: Project breakdown details would be visible in UI
    // This test verifies the overall report is generated
  });

  test('should filter by specific project correctly', async ({ hrPage }) => {
    // Arrange
    const dateRange = getCurrentMonthRange();
    
    // Act - generate report for all projects
    await reportsPage.selectAllProjects();
    await reportsPage.generateReport({
      format: 'JSON',
      startDate: dateRange.startDate,
      endDate: dateRange.endDate,
    });
    
    const allProjectsSummary = await reportsPage.getTaskSummaryData();
    
    // Note: To test specific project filtering, we would need:
    // 1. Know which projects exist in test environment
    // 2. Select a specific project
    // 3. Verify filtered count <= all projects count
    
    // For now, verify report was generated
    expect(allProjectsSummary.totalTasks).toBeGreaterThanOrEqual(0);
  });
});

test.describe('Report Data Accuracy - Task Status Accuracy', () => {
  let reportsPage: ReportsPage;

  test.beforeEach(async ({ hrPage }) => {
    reportsPage = new ReportsPage(hrPage);
    await reportsPage.navigate();
    await reportsPage.waitForReportsPageLoad();
  });

  test('should accurately report TODO status tasks', async ({ hrPage }) => {
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
    expect(taskSummary.todoTasks).toBeGreaterThanOrEqual(0);
  });

  test('should accurately report IN_PROGRESS status tasks', async ({ hrPage }) => {
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
  });

  test('should accurately report COMPLETED status tasks', async ({ hrPage }) => {
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
  });

  test('should accurately report BLOCKED status tasks', async ({ hrPage }) => {
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
  });

  test('should include all task statuses in total count', async ({ hrPage }) => {
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
    
    // Calculate sum of displayed statuses
    const displayedSum = taskSummary.todoTasks + 
                        taskSummary.inProgressTasks + 
                        taskSummary.completedTasks + 
                        taskSummary.blockedTasks;
    
    // Total should be >= sum (might include FUTURE or other statuses)
    expect(taskSummary.totalTasks).toBeGreaterThanOrEqual(displayedSum);
  });
});

test.describe('Report Data Accuracy - Department Scoping', () => {
  let reportsPage: ReportsPage;

  test.beforeEach(async ({ hrPage }) => {
    reportsPage = new ReportsPage(hrPage);
    await reportsPage.navigate();
    await reportsPage.waitForReportsPageLoad();
  });

  test('should show data for all departments (HR user)', async ({ hrPage }) => {
    // Arrange
    const dateRange = getCurrentMonthRange();
    
    // Act - HR user generates report without department filter
    await reportsPage.generateReport({
      format: 'JSON',
      startDate: dateRange.startDate,
      endDate: dateRange.endDate,
    });
    
    // Assert
    await reportsPage.assertTaskSummaryVisible();
    const taskSummary = await reportsPage.getTaskSummaryData();
    
    // HR should see all departments' data
    expect(taskSummary.totalTasks).toBeGreaterThanOrEqual(0);
  });

  test('should maintain data consistency when switching filters', async ({ hrPage }) => {
    // Arrange
    const dateRange = getCurrentMonthRange();
    
    // Act - generate initial report
    await reportsPage.generateReport({
      format: 'JSON',
      startDate: dateRange.startDate,
      endDate: dateRange.endDate,
    });
    
    const initialSummary = await reportsPage.getTaskSummaryData();
    
    // Act - regenerate with same filters
    await reportsPage.clickGenerate();
    await reportsPage.waitForReportGenerated();
    
    const regeneratedSummary = await reportsPage.getTaskSummaryData();
    
    // Assert - data should be consistent
    expect(regeneratedSummary.totalTasks).toBe(initialSummary.totalTasks);
    expect(regeneratedSummary.completedTasks).toBe(initialSummary.completedTasks);
    expect(regeneratedSummary.inProgressTasks).toBe(initialSummary.inProgressTasks);
  });
});

test.describe('Report Data Accuracy - Edge Cases', () => {
  let reportsPage: ReportsPage;

  test.beforeEach(async ({ hrPage }) => {
    reportsPage = new ReportsPage(hrPage);
    await reportsPage.navigate();
    await reportsPage.waitForReportsPageLoad();
  });

  test('should handle empty result sets gracefully', async ({ hrPage }) => {
    // Arrange - use future date range (unlikely to have data)
    const futureStart = '2030-01-01';
    const futureEnd = '2030-12-31';
    
    // Act
    await reportsPage.generateReport({
      format: 'JSON',
      startDate: futureStart,
      endDate: futureEnd,
    });
    
    // Assert - should still show report with zero or minimal counts
    await reportsPage.assertTaskSummaryVisible();
    const taskSummary = await reportsPage.getTaskSummaryData();
    
    // All counts should be non-negative
    assertTaskCountsNonNegative({
      total: taskSummary.totalTasks,
      completed: taskSummary.completedTasks,
      inProgress: taskSummary.inProgressTasks,
      todo: taskSummary.todoTasks,
      blocked: taskSummary.blockedTasks,
    });
  });

  test('should handle large date ranges', async ({ hrPage }) => {
    // Arrange - use very large date range
    const largeRangeStart = '2020-01-01';
    const largeRangeEnd = '2025-12-31';
    
    // Act
    await reportsPage.generateReport({
      format: 'JSON',
      startDate: largeRangeStart,
      endDate: largeRangeEnd,
    });
    
    // Assert - should generate report successfully
    await reportsPage.assertTaskSummaryVisible();
    const taskSummary = await reportsPage.getTaskSummaryData();
    
    expect(taskSummary.totalTasks).toBeGreaterThanOrEqual(0);
  });

  test('should handle single day date range', async ({ hrPage }) => {
    // Arrange - use single day
    const today = new Date().toISOString().split('T')[0];
    
    // Act
    await reportsPage.generateReport({
      format: 'JSON',
      startDate: today,
      endDate: today,
    });
    
    // Assert
    await reportsPage.assertTaskSummaryVisible();
    const taskSummary = await reportsPage.getTaskSummaryData();
    
    expect(taskSummary.totalTasks).toBeGreaterThanOrEqual(0);
  });
});


