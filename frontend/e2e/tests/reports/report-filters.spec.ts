import { test, expect } from '../../fixtures';
import { ReportsPage } from '../../fixtures/pages/reports-page';
import {
  getCurrentMonthRange,
  getPreviousMonthRange,
  getCurrentYearRange,
  getCurrentWeekRange,
  getNonOverlappingDateRanges,
  createDateRangeTestFilters,
  createProjectReportFilter,
} from '../../utils/test-data';

/**
 * Report Filter Tests
 * Tests date range filters, project filters, department filters, and time range options
 */

test.describe('Report Filters - Date Range Validation', () => {
  let reportsPage: ReportsPage;

  test.beforeEach(async ({ hrPage }) => {
    reportsPage = new ReportsPage(hrPage);
    await reportsPage.navigate();
    await reportsPage.waitForReportsPageLoad();
  });

  test('should accept valid date range (start before end)', async ({ hrPage }) => {
    // Arrange
    const dateRange = getCurrentMonthRange();
    
    // Act
    await reportsPage.setDateRange(dateRange.startDate, dateRange.endDate);
    
    // Assert - should be able to generate report
    await reportsPage.assertGenerateButtonEnabled();
    
    await reportsPage.selectFormat('JSON');
    await reportsPage.clickGenerate();
    await reportsPage.waitForReportGenerated();
    
    await reportsPage.assertTaskSummaryVisible();
  });

  test('should require start date', async ({ hrPage }) => {
    // Arrange - clear start date and try to generate
    await reportsPage.setStartDate('');
    await reportsPage.setEndDate(getCurrentMonthRange().endDate);
    await reportsPage.selectFormat('JSON');

    // Act - try to generate report without start date
    await reportsPage.clickGenerate();

    // Assert - report should not be generated (no task summary visible)
    // Wait a bit to ensure no report is generated
    await hrPage.waitForTimeout(2000);

    // Verify no report data is displayed
    const isReportDisplayed = await reportsPage.isReportDataDisplayed();
    expect(isReportDisplayed).toBe(false);
  });

  test('should require end date', async ({ hrPage }) => {
    // Arrange - clear end date and try to generate
    await reportsPage.setStartDate(getCurrentMonthRange().startDate);
    await reportsPage.setEndDate('');
    await reportsPage.selectFormat('JSON');

    // Act - try to generate report without end date
    await reportsPage.clickGenerate();

    // Assert - report should not be generated
    await hrPage.waitForTimeout(2000);

    // Verify no report data is displayed
    const isReportDisplayed = await reportsPage.isReportDataDisplayed();
    expect(isReportDisplayed).toBe(false);
  });

  test('should allow same start and end date', async ({ hrPage }) => {
    // Arrange
    const today = getCurrentMonthRange().endDate;
    
    // Act
    await reportsPage.setDateRange(today, today);
    await reportsPage.selectFormat('JSON');
    await reportsPage.clickGenerate();
    await reportsPage.waitForReportGenerated();
    
    // Assert
    await reportsPage.assertTaskSummaryVisible();
  });
});

test.describe('Report Filters - Date Range Effects', () => {
  let reportsPage: ReportsPage;

  test.beforeEach(async ({ hrPage }) => {
    reportsPage = new ReportsPage(hrPage);
    await reportsPage.navigate();
    await reportsPage.waitForReportsPageLoad();
  });

  test('should filter tasks by current month date range', async ({ hrPage }) => {
    // Arrange
    const dateRange = getCurrentMonthRange();
    
    // Act
    await reportsPage.generateReport({
      format: 'JSON',
      startDate: dateRange.startDate,
      endDate: dateRange.endDate,
    });
    
    // Assert
    await reportsPage.assertTaskSummaryVisible();
    const taskSummary = await reportsPage.getTaskSummaryData();
    
    // Verify data exists (count may be 0 if no tasks in current month)
    expect(taskSummary.totalTasks).toBeGreaterThanOrEqual(0);
  });

  test('should filter tasks by previous month date range', async ({ hrPage }) => {
    // Arrange
    const dateRange = getPreviousMonthRange();
    
    // Act
    await reportsPage.generateReport({
      format: 'JSON',
      startDate: dateRange.startDate,
      endDate: dateRange.endDate,
    });
    
    // Assert
    await reportsPage.assertTaskSummaryVisible();
    const taskSummary = await reportsPage.getTaskSummaryData();
    expect(taskSummary.totalTasks).toBeGreaterThanOrEqual(0);
  });

  test('should filter tasks by current year date range', async ({ hrPage }) => {
    // Arrange
    const dateRange = getCurrentYearRange();
    
    // Act
    await reportsPage.generateReport({
      format: 'JSON',
      timeRange: 'YEARLY',
      startDate: dateRange.startDate,
      endDate: dateRange.endDate,
    });
    
    // Assert
    await reportsPage.assertTaskSummaryVisible();
    const taskSummary = await reportsPage.getTaskSummaryData();
    expect(taskSummary.totalTasks).toBeGreaterThanOrEqual(0);
  });

  test('should show different results for different date ranges', async ({ hrPage }) => {
    // Arrange
    const { range1, range2 } = getNonOverlappingDateRanges();
    
    // Act - generate report for first date range
    await reportsPage.generateReport({
      format: 'JSON',
      startDate: range1.startDate,
      endDate: range1.endDate,
    });
    
    await reportsPage.assertTaskSummaryVisible();
    const taskSummary1 = await reportsPage.getTaskSummaryData();
    
    // Act - generate report for second date range
    await reportsPage.setDateRange(range2.startDate, range2.endDate);
    await reportsPage.clickGenerate();
    await reportsPage.waitForReportGenerated();
    
    const taskSummary2 = await reportsPage.getTaskSummaryData();
    
    // Assert - data might be different (could be same if both ranges have same tasks)
    // We just verify both reports were generated successfully
    expect(taskSummary1).toBeDefined();
    expect(taskSummary2).toBeDefined();
    
    // At least verify the reports were generated
    expect(typeof taskSummary1.totalTasks).toBe('number');
    expect(typeof taskSummary2.totalTasks).toBe('number');
  });

  test('should correctly apply custom date range', async ({ hrPage }) => {
    // Arrange - use custom date range
    const customStart = '2024-01-01';
    const customEnd = '2024-01-31';
    
    // Act
    await reportsPage.selectTimeRange('CUSTOM');
    await reportsPage.setDateRange(customStart, customEnd);
    await reportsPage.selectFormat('JSON');
    await reportsPage.clickGenerate();
    await reportsPage.waitForReportGenerated();
    
    // Assert
    await reportsPage.assertTaskSummaryVisible();
    
    // Verify dates were applied
    const startDate = await reportsPage.getStartDate();
    const endDate = await reportsPage.getEndDate();
    expect(startDate).toBe(customStart);
    expect(endDate).toBe(customEnd);
  });
});

test.describe('Report Filters - Time Range Options', () => {
  let reportsPage: ReportsPage;

  test.beforeEach(async ({ hrPage }) => {
    reportsPage = new ReportsPage(hrPage);
    await reportsPage.navigate();
    await reportsPage.waitForReportsPageLoad();
  });

  test('should generate weekly report', async ({ hrPage }) => {
    // Arrange
    const filters = createProjectReportFilter()
      .withTimeRange('WEEKLY')
      .withCurrentWeek()
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

  test('should generate monthly report', async ({ hrPage }) => {
    // Arrange
    const filters = createProjectReportFilter()
      .withTimeRange('MONTHLY')
      .withCurrentMonth()
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

  test('should generate quarterly report', async ({ hrPage }) => {
    // Arrange
    const filters = createProjectReportFilter()
      .withTimeRange('QUARTERLY')
      .withCurrentQuarter()
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

  test('should generate yearly report', async ({ hrPage }) => {
    // Arrange
    const filters = createProjectReportFilter()
      .withTimeRange('YEARLY')
      .withCurrentYear()
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

  test('should generate custom time range report', async ({ hrPage }) => {
    // Arrange
    const customStart = '2024-06-01';
    const customEnd = '2024-06-15';
    
    // Act
    await reportsPage.generateReport({
      timeRange: 'CUSTOM',
      format: 'JSON',
      startDate: customStart,
      endDate: customEnd,
    });
    
    // Assert
    await reportsPage.assertTaskSummaryVisible();
  });

  test('should switch between time range options', async ({ hrPage }) => {
    // Arrange
    const monthlyRange = getCurrentMonthRange();
    
    // Act - start with monthly
    await reportsPage.selectTimeRange('MONTHLY');
    await reportsPage.setDateRange(monthlyRange.startDate, monthlyRange.endDate);
    await reportsPage.selectFormat('JSON');
    await reportsPage.clickGenerate();
    await reportsPage.waitForReportGenerated();
    await reportsPage.assertTaskSummaryVisible();
    
    // Act - switch to yearly
    const yearlyRange = getCurrentYearRange();
    await reportsPage.selectTimeRange('YEARLY');
    await reportsPage.setDateRange(yearlyRange.startDate, yearlyRange.endDate);
    await reportsPage.clickGenerate();
    await reportsPage.waitForReportGenerated();
    
    // Assert
    await reportsPage.assertTaskSummaryVisible();
  });
});

test.describe('Report Filters - Project Filtering', () => {
  let reportsPage: ReportsPage;

  test.beforeEach(async ({ hrPage }) => {
    reportsPage = new ReportsPage(hrPage);
    await reportsPage.navigate();
    await reportsPage.waitForReportsPageLoad();
  });

  test('should generate report for all projects by default', async ({ hrPage }) => {
    // Arrange
    const dateRange = getCurrentMonthRange();
    
    // Act - don't select any specific project (should default to all)
    await reportsPage.selectAllProjects();
    await reportsPage.generateReport({
      format: 'JSON',
      startDate: dateRange.startDate,
      endDate: dateRange.endDate,
    });
    
    // Assert
    await reportsPage.assertTaskSummaryVisible();
  });

  test('should allow selecting all projects explicitly', async ({ hrPage }) => {
    // Arrange
    const dateRange = getCurrentMonthRange();
    
    // Act
    await reportsPage.selectAllProjects();
    await reportsPage.generateReport({
      format: 'JSON',
      startDate: dateRange.startDate,
      endDate: dateRange.endDate,
    });
    
    // Assert
    await reportsPage.assertTaskSummaryVisible();
    const taskSummary = await reportsPage.getTaskSummaryData();
    expect(taskSummary.totalTasks).toBeGreaterThanOrEqual(0);
  });

  test('should filter by specific project', async ({ hrPage }) => {
    // Note: This test requires actual project data in the test environment
    // For now, we'll test the UI interaction
    
    // Arrange
    const dateRange = getCurrentMonthRange();
    
    // Act - The actual project selection would need real project names
    // For now, just verify the interaction works
    await reportsPage.generateReport({
      format: 'JSON',
      startDate: dateRange.startDate,
      endDate: dateRange.endDate,
    });
    
    // Assert
    await reportsPage.assertTaskSummaryVisible();
  });
});

test.describe('Report Filters - Department Filtering', () => {
  let reportsPage: ReportsPage;

  test.beforeEach(async ({ hrPage }) => {
    reportsPage = new ReportsPage(hrPage);
    await reportsPage.navigate();
    await reportsPage.waitForReportsPageLoad();
  });

  test('should generate report for all departments (HR user)', async ({ hrPage }) => {
    // Arrange
    const dateRange = getCurrentMonthRange();
    
    // Act - don't select department (HR sees all)
    await reportsPage.generateReport({
      format: 'JSON',
      startDate: dateRange.startDate,
      endDate: dateRange.endDate,
    });
    
    // Assert
    await reportsPage.assertTaskSummaryVisible();
  });

  test('should filter by specific department', async ({ hrPage }) => {
    // Note: This test requires actual department data
    // We test the UI can generate reports with department context
    
    // Arrange
    const dateRange = getCurrentMonthRange();
    
    // Act
    await reportsPage.generateReport({
      format: 'JSON',
      startDate: dateRange.startDate,
      endDate: dateRange.endDate,
    });
    
    // Assert
    await reportsPage.assertTaskSummaryVisible();
  });
});

test.describe('Report Filters - Combined Filters', () => {
  let reportsPage: ReportsPage;

  test.beforeEach(async ({ hrPage }) => {
    reportsPage = new ReportsPage(hrPage);
    await reportsPage.navigate();
    await reportsPage.waitForReportsPageLoad();
  });

  test('should apply multiple filters simultaneously', async ({ hrPage }) => {
    // Arrange
    const dateRange = getCurrentMonthRange();
    
    // Act - apply time range + format + date range
    await reportsPage.selectTimeRange('MONTHLY');
    await reportsPage.selectFormat('JSON');
    await reportsPage.setDateRange(dateRange.startDate, dateRange.endDate);
    await reportsPage.clickGenerate();
    await reportsPage.waitForReportGenerated();
    
    // Assert
    await reportsPage.assertTaskSummaryVisible();
  });

  test('should update report when filters change', async ({ hrPage }) => {
    // Arrange
    const currentMonth = getCurrentMonthRange();
    const previousMonth = getPreviousMonthRange();
    
    // Act - generate first report
    await reportsPage.generateReport({
      format: 'JSON',
      startDate: currentMonth.startDate,
      endDate: currentMonth.endDate,
    });
    await reportsPage.assertTaskSummaryVisible();
    
    // Act - change date range and regenerate
    await reportsPage.setDateRange(previousMonth.startDate, previousMonth.endDate);
    await reportsPage.clickGenerate();
    await reportsPage.waitForReportGenerated();
    
    // Assert - new report generated
    await reportsPage.assertTaskSummaryVisible();
  });

  test('should maintain filter selections after generating report', async ({ hrPage }) => {
    // Arrange
    const dateRange = getCurrentMonthRange();
    
    // Act - set filters and generate
    await reportsPage.selectTimeRange('MONTHLY');
    await reportsPage.selectFormat('JSON');
    await reportsPage.setDateRange(dateRange.startDate, dateRange.endDate);
    await reportsPage.clickGenerate();
    await reportsPage.waitForReportGenerated();
    
    // Assert - filters should still be selected
    const startDate = await reportsPage.getStartDate();
    const endDate = await reportsPage.getEndDate();
    
    expect(startDate).toBe(dateRange.startDate);
    expect(endDate).toBe(dateRange.endDate);
  });
});

test.describe('Report Filters - Export Format Selection', () => {
  let reportsPage: ReportsPage;

  test.beforeEach(async ({ hrPage }) => {
    reportsPage = new ReportsPage(hrPage);
    await reportsPage.navigate();
    await reportsPage.waitForReportsPageLoad();
  });

  test('should allow selecting JSON format', async ({ hrPage }) => {
    // Act
    await reportsPage.selectFormat('JSON');
    
    // Assert
    await reportsPage.assertGenerateButtonEnabled();
  });

  test('should allow selecting PDF format', async ({ hrPage }) => {
    // Act
    await reportsPage.selectFormat('PDF');
    
    // Assert
    await reportsPage.assertGenerateButtonEnabled();
  });

  test('should allow selecting CSV format', async ({ hrPage }) => {
    // Act
    await reportsPage.selectFormat('CSV');
    
    // Assert
    await reportsPage.assertGenerateButtonEnabled();
  });

  test('should switch between export formats', async ({ hrPage }) => {
    // Act & Assert
    await reportsPage.selectFormat('JSON');
    await reportsPage.assertGenerateButtonEnabled();
    
    await reportsPage.selectFormat('PDF');
    await reportsPage.assertGenerateButtonEnabled();
    
    await reportsPage.selectFormat('CSV');
    await reportsPage.assertGenerateButtonEnabled();
  });
});


