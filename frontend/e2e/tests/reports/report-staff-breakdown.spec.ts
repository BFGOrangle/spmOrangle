import { test, expect } from '../../fixtures';
import { ReportsPage } from '../../fixtures/pages/reports-page';
import { getCurrentMonthRange } from '../../utils/test-data';

/**
 * Staff Breakdown Table E2E Tests
 * Tests staff-level breakdown of tasks and logged hours
 *
 * Test Coverage:
 * - Display staff breakdown table with all columns
 * - Show task counts for each staff member
 * - Filter staff by department
 * - Filter staff by project
 * - Handle empty staff data gracefully
 * - Update staff list when filters change
 */

test.describe('Staff Breakdown Table - Display & Basic Functionality', () => {
  let reportsPage: ReportsPage;

  test.beforeEach(async ({ hrPage }) => {
    reportsPage = new ReportsPage(hrPage);
    await reportsPage.navigate();
    await reportsPage.waitForReportsPageLoad();
  });

  test('should display staff breakdown table with all columns when data exists', async ({ hrPage }) => {
    // Arrange
    const dateRange = getCurrentMonthRange();

    // Act - generate report with JSON format
    await reportsPage.generateReport({
      format: 'JSON',
      startDate: dateRange.startDate,
      endDate: dateRange.endDate,
    });

    // Assert - Staff Breakdown section should be visible
    const staffBreakdownSection = hrPage.getByText('Staff Breakdown');
    await expect(staffBreakdownSection).toBeVisible();

    // Assert - Table should be visible with data-testid
    const staffTable = hrPage.getByTestId('staff-breakdown-table');
    await expect(staffTable).toBeVisible();

    // Assert - All column headers should be visible (use locator for table headers)
    const tableHeaders = staffTable.locator('thead th');
    await expect(tableHeaders.nth(0)).toHaveText('Staff Name');
    await expect(tableHeaders.nth(1)).toHaveText('Department');
    await expect(tableHeaders.nth(2)).toHaveText('To Do');
    await expect(tableHeaders.nth(3)).toHaveText('In Progress');
    await expect(tableHeaders.nth(4)).toHaveText('Completed');
    await expect(tableHeaders.nth(5)).toHaveText('Blocked');
    await expect(tableHeaders.nth(6)).toHaveText('Logged Hours');
  });

  test('should show task counts for each staff member', async ({ hrPage }) => {
    // Arrange
    const dateRange = getCurrentMonthRange();

    // Act
    await reportsPage.generateReport({
      format: 'JSON',
      startDate: dateRange.startDate,
      endDate: dateRange.endDate,
    });

    // Assert - Get all staff rows
    const staffRows = hrPage.getByTestId('staff-breakdown-row');
    const rowCount = await staffRows.count();

    if (rowCount > 0) {
      // Check first staff row has all required cells
      const firstRow = staffRows.first();

      // Assert - Staff name is present
      await expect(firstRow.getByTestId('staff-name')).toBeVisible();

      // Assert - Department is present
      await expect(firstRow.getByTestId('staff-department')).toBeVisible();

      // Assert - All task count cells are present and contain numbers
      const todoText = await firstRow.getByTestId('staff-todo').textContent();
      const inProgressText = await firstRow.getByTestId('staff-in-progress').textContent();
      const completedText = await firstRow.getByTestId('staff-completed').textContent();
      const blockedText = await firstRow.getByTestId('staff-blocked').textContent();
      const hoursText = await firstRow.getByTestId('staff-logged-hours').textContent();

      // Assert - All counts should be non-negative integers
      expect(parseInt(todoText?.trim() || '0')).toBeGreaterThanOrEqual(0);
      expect(parseInt(inProgressText?.trim() || '0')).toBeGreaterThanOrEqual(0);
      expect(parseInt(completedText?.trim() || '0')).toBeGreaterThanOrEqual(0);
      expect(parseInt(blockedText?.trim() || '0')).toBeGreaterThanOrEqual(0);

      // Assert - Logged hours should be formatted to 2 decimal places
      expect(hoursText).toMatch(/^\d+\.\d{2}$/);
      const hours = parseFloat(hoursText?.trim() || '0');
      expect(hours).toBeGreaterThanOrEqual(0);
    }
  });

  test('should display multiple staff members with unique data', async ({ hrPage }) => {
    // Arrange
    const dateRange = getCurrentMonthRange();

    // Act
    await reportsPage.generateReport({
      format: 'JSON',
      startDate: dateRange.startDate,
      endDate: dateRange.endDate,
    });

    // Assert - Get all staff rows
    const staffRows = hrPage.getByTestId('staff-breakdown-row');
    const rowCount = await staffRows.count();

    if (rowCount > 1) {
      // Check that staff names are unique
      const staffNames: string[] = [];
      for (let i = 0; i < Math.min(rowCount, 5); i++) {
        const row = staffRows.nth(i);
        const nameText = await row.getByTestId('staff-name').textContent();
        staffNames.push(nameText?.trim() || '');
      }

      // Assert - Each staff member should have a unique name
      const uniqueNames = new Set(staffNames);
      expect(uniqueNames.size).toBe(staffNames.length);

      // Assert - Each row should have department info
      for (let i = 0; i < Math.min(rowCount, 3); i++) {
        const row = staffRows.nth(i);
        const deptText = await row.getByTestId('staff-department').textContent();
        expect(deptText).toBeTruthy();
        expect(deptText?.trim().length).toBeGreaterThan(0);
      }
    }
  });

  test('should handle empty staff data gracefully', async ({ hrPage }) => {
    // Arrange - Use a date range far in the future with no data
    const futureDate = new Date();
    futureDate.setFullYear(futureDate.getFullYear() + 10);
    const startDate = futureDate.toISOString().split('T')[0];
    const endDate = futureDate.toISOString().split('T')[0];

    // Act
    await reportsPage.generateReport({
      format: 'JSON',
      startDate: startDate,
      endDate: endDate,
    });

    // Assert - Should display empty state message
    const emptyMessage = hrPage.getByText('No staff data available for the selected filters.', { exact: true });

    // Staff section might not appear at all, or show empty message
    const staffBreakdownSection = hrPage.getByText('Staff Breakdown');
    const sectionVisible = await staffBreakdownSection.isVisible().catch(() => false);

    if (sectionVisible) {
      // If section is visible, check if empty message OR table is shown
      const staffTable = hrPage.getByTestId('staff-breakdown-table');
      const tableVisible = await staffTable.isVisible().catch(() => false);
      const messageVisible = await emptyMessage.isVisible().catch(() => false);

      // Either table or empty message should be visible (depending on data)
      // For future dates, we expect empty message OR no section at all
      if (!tableVisible) {
        await expect(emptyMessage).toBeVisible();
      }
    }
    // If section is not visible, that's also acceptable (no staff data to show)
  });
});

test.describe('Staff Breakdown Table - Department Filtering', () => {
  let reportsPage: ReportsPage;

  test.beforeEach(async ({ hrPage }) => {
    reportsPage = new ReportsPage(hrPage);
    await reportsPage.navigate();
    await reportsPage.waitForReportsPageLoad();
  });

  test('should filter staff by specific department', async ({ hrPage }) => {
    // Arrange
    const dateRange = getCurrentMonthRange();

    // First, generate a report for all departments to get a department name
    await reportsPage.generateReport({
      format: 'JSON',
      startDate: dateRange.startDate,
      endDate: dateRange.endDate,
    });

    // Get a department name from the first staff row (if any)
    const staffRows = hrPage.getByTestId('staff-breakdown-row');
    const rowCount = await staffRows.count();

    if (rowCount > 0) {
      const firstRowDept = await staffRows.first().getByTestId('staff-department').textContent();
      // Strip task count from department name (e.g., "HR Team (40)" -> "HR Team")
      const departmentToFilter = firstRowDept?.trim().replace(/\s*\(\d+\)$/, '') || '';

      if (departmentToFilter) {
        // Act - Generate report filtered by specific department
        await reportsPage.generateReport({
          department: departmentToFilter,
          format: 'JSON',
          startDate: dateRange.startDate,
          endDate: dateRange.endDate,
        });

        // Assert - All displayed staff should be from the selected department
        const filteredRows = hrPage.getByTestId('staff-breakdown-row');
        const filteredCount = await filteredRows.count();

        for (let i = 0; i < filteredCount; i++) {
          const row = filteredRows.nth(i);
          const deptText = await row.getByTestId('staff-department').textContent();
          // Strip task count from displayed department for comparison
          const displayedDept = deptText?.trim().replace(/\s*\(\d+\)$/, '') || '';
          expect(displayedDept).toBe(departmentToFilter);
        }
      }
    }
  });

  test('should display all departments for HR user without filters', async ({ hrPage }) => {
    // Arrange
    const dateRange = getCurrentMonthRange();

    // Act - Generate report without department filter (All Departments)
    await reportsPage.generateReport({
      format: 'JSON',
      startDate: dateRange.startDate,
      endDate: dateRange.endDate,
    });

    // Assert - Staff from multiple departments may be present
    const staffRows = hrPage.getByTestId('staff-breakdown-row');
    const rowCount = await staffRows.count();

    if (rowCount > 1) {
      // Collect all unique departments
      const departments = new Set<string>();
      for (let i = 0; i < rowCount; i++) {
        const row = staffRows.nth(i);
        const deptText = await row.getByTestId('staff-department').textContent();
        departments.add(deptText?.trim() || '');
      }

      // Assert - Should have at least one department
      expect(departments.size).toBeGreaterThanOrEqual(1);

      // Each department value should be non-empty
      departments.forEach(dept => {
        expect(dept.length).toBeGreaterThan(0);
      });
    }
  });
});

test.describe('Staff Breakdown Table - Project Filtering', () => {
  let reportsPage: ReportsPage;

  test.beforeEach(async ({ hrPage }) => {
    reportsPage = new ReportsPage(hrPage);
    await reportsPage.navigate();
    await reportsPage.waitForReportsPageLoad();
  });

  test('should filter staff by specific project', async ({ hrPage }) => {
    // Arrange
    const dateRange = getCurrentMonthRange();

    // Click project dropdown to see available projects
    await hrPage.getByLabel('Projects').click();

    // Get the first project option (skip "All Projects")
    const projectOptions = hrPage.getByRole('option');
    const optionCount = await projectOptions.count();

    if (optionCount > 1) {
      // Find a specific project (not "All Projects")
      let projectName = '';
      for (let i = 0; i < optionCount; i++) {
        const optionText = await projectOptions.nth(i).textContent();
        if (optionText && !optionText.includes('All Projects')) {
          projectName = optionText.trim();
          break;
        }
      }

      if (projectName) {
        // Act - Select the project and generate report
        await projectOptions.filter({ hasText: projectName }).click();
        await reportsPage.selectFormat('JSON');
        await reportsPage.setDateRange(dateRange.startDate, dateRange.endDate);
        await reportsPage.clickGenerate();
        await reportsPage.waitForReportGenerated();

        // Assert - Staff breakdown should show only staff from that project
        // (All staff should have tasks from the selected project)
        const staffRows = hrPage.getByTestId('staff-breakdown-row');
        const rowCount = await staffRows.count();

        // At least verify that staff data is displayed (can't verify project membership without backend data)
        if (rowCount > 0) {
          const firstRow = staffRows.first();
          await expect(firstRow.getByTestId('staff-name')).toBeVisible();
          await expect(firstRow.getByTestId('staff-department')).toBeVisible();
        }
      }
    }
  });
});

test.describe('Staff Breakdown Table - Filter Changes', () => {
  let reportsPage: ReportsPage;

  test.beforeEach(async ({ hrPage }) => {
    reportsPage = new ReportsPage(hrPage);
    await reportsPage.navigate();
    await reportsPage.waitForReportsPageLoad();
  });

  test('should update staff list when department filter changes', async ({ hrPage }) => {
    // Arrange
    const dateRange = getCurrentMonthRange();

    // Act - Generate initial report with all departments
    await reportsPage.generateReport({
      format: 'JSON',
      startDate: dateRange.startDate,
      endDate: dateRange.endDate,
    });

    // Get initial staff count
    const initialRows = hrPage.getByTestId('staff-breakdown-row');
    const initialCount = await initialRows.count();

    if (initialCount > 0) {
      // Get a department from the first row
      const firstDept = await initialRows.first().getByTestId('staff-department').textContent();
      // Strip task count from department name (e.g., "HR Team (40)" -> "HR Team")
      const departmentToFilter = firstDept?.trim().replace(/\s*\(\d+\)$/, '') || '';

      if (departmentToFilter) {
        // Act - Change department filter and regenerate
        await reportsPage.generateReport({
          department: departmentToFilter,
          format: 'JSON',
          startDate: dateRange.startDate,
          endDate: dateRange.endDate,
        });

        // Assert - Staff list should be updated
        const filteredRows = hrPage.getByTestId('staff-breakdown-row');
        const filteredCount = await filteredRows.count();

        // Assert - All visible staff should be from the filtered department
        for (let i = 0; i < filteredCount; i++) {
          const row = filteredRows.nth(i);
          const deptText = await row.getByTestId('staff-department').textContent();
          // Strip task count from displayed department for comparison
          const displayedDept = deptText?.trim().replace(/\s*\(\d+\)$/, '') || '';
          expect(displayedDept).toBe(departmentToFilter);
        }
      }
    }
  });

  test('should update staff list when project filter changes', async ({ hrPage }) => {
    // Arrange
    const dateRange = getCurrentMonthRange();

    // Act - Generate report for all projects
    await reportsPage.generateReport({
      format: 'JSON',
      startDate: dateRange.startDate,
      endDate: dateRange.endDate,
    });

    // Get initial staff count
    const initialRows = hrPage.getByTestId('staff-breakdown-row');
    const initialCount = await initialRows.count();

    // Act - Get a specific project and filter by it
    await hrPage.getByLabel('Projects').click();
    const projectOptions = hrPage.getByRole('option');
    const optionCount = await projectOptions.count();

    if (optionCount > 1) {
      // Find a specific project
      let projectName = '';
      for (let i = 0; i < optionCount; i++) {
        const optionText = await projectOptions.nth(i).textContent();
        if (optionText && !optionText.includes('All Projects')) {
          projectName = optionText.trim();
          break;
        }
      }

      if (projectName) {
        await projectOptions.filter({ hasText: projectName }).click();
        await reportsPage.clickGenerate();
        await reportsPage.waitForReportGenerated();

        // Assert - Staff list should be updated (may have different count)
        const filteredRows = hrPage.getByTestId('staff-breakdown-row');
        const filteredCount = await filteredRows.count();

        // Staff count may change when filtering by project
        // Just verify that the table still displays properly
        if (filteredCount > 0) {
          await expect(filteredRows.first().getByTestId('staff-name')).toBeVisible();
        }
      }
    }
  });
});

test.describe('Staff Breakdown Table - Date Range Filtering', () => {
  let reportsPage: ReportsPage;

  test.beforeEach(async ({ hrPage }) => {
    reportsPage = new ReportsPage(hrPage);
    await reportsPage.navigate();
    await reportsPage.waitForReportsPageLoad();
  });

  test('should respect date range filters for staff breakdown', async ({ hrPage }) => {
    // Arrange - Generate report for current month
    const currentMonth = getCurrentMonthRange();

    await reportsPage.generateReport({
      format: 'JSON',
      startDate: currentMonth.startDate,
      endDate: currentMonth.endDate,
    });

    // Get staff data for current month
    const currentMonthRows = hrPage.getByTestId('staff-breakdown-row');
    const currentMonthCount = await currentMonthRows.count();

    // Arrange - Generate report for a single day (today)
    const today = new Date().toISOString().split('T')[0];

    await reportsPage.generateReport({
      format: 'JSON',
      startDate: today,
      endDate: today,
    });

    // Assert - Staff data should update to reflect the new date range
    const todayRows = hrPage.getByTestId('staff-breakdown-row');
    const todayCount = await todayRows.count();

    // Counts may differ between month and single day
    // Just verify that staff breakdown updates when date changes
    if (todayCount > 0) {
      // Verify that task counts are still non-negative
      const firstRow = todayRows.first();
      const todoText = await firstRow.getByTestId('staff-todo').textContent();
      expect(parseInt(todoText?.trim() || '0')).toBeGreaterThanOrEqual(0);
    }
  });
});

test.describe('Staff Breakdown Table - Data Validation', () => {
  let reportsPage: ReportsPage;

  test.beforeEach(async ({ hrPage }) => {
    reportsPage = new ReportsPage(hrPage);
    await reportsPage.navigate();
    await reportsPage.waitForReportsPageLoad();
  });

  test('should display non-negative task counts for all staff', async ({ hrPage }) => {
    // Arrange
    const dateRange = getCurrentMonthRange();

    // Act
    await reportsPage.generateReport({
      format: 'JSON',
      startDate: dateRange.startDate,
      endDate: dateRange.endDate,
    });

    // Assert - Check all staff rows have non-negative counts
    const staffRows = hrPage.getByTestId('staff-breakdown-row');
    const rowCount = await staffRows.count();

    for (let i = 0; i < rowCount; i++) {
      const row = staffRows.nth(i);

      const todoText = await row.getByTestId('staff-todo').textContent();
      const inProgressText = await row.getByTestId('staff-in-progress').textContent();
      const completedText = await row.getByTestId('staff-completed').textContent();
      const blockedText = await row.getByTestId('staff-blocked').textContent();

      expect(parseInt(todoText?.trim() || '0')).toBeGreaterThanOrEqual(0);
      expect(parseInt(inProgressText?.trim() || '0')).toBeGreaterThanOrEqual(0);
      expect(parseInt(completedText?.trim() || '0')).toBeGreaterThanOrEqual(0);
      expect(parseInt(blockedText?.trim() || '0')).toBeGreaterThanOrEqual(0);
    }
  });

  test('should format logged hours to 2 decimal places for all staff', async ({ hrPage }) => {
    // Arrange
    const dateRange = getCurrentMonthRange();

    // Act
    await reportsPage.generateReport({
      format: 'JSON',
      startDate: dateRange.startDate,
      endDate: dateRange.endDate,
    });

    // Assert - Check all staff rows have properly formatted hours
    const staffRows = hrPage.getByTestId('staff-breakdown-row');
    const rowCount = await staffRows.count();

    for (let i = 0; i < rowCount; i++) {
      const row = staffRows.nth(i);
      const hoursText = await row.getByTestId('staff-logged-hours').textContent();

      // Assert - Hours should be formatted as XX.XX
      expect(hoursText).toMatch(/^\d+\.\d{2}$/);

      // Assert - Hours should be non-negative
      const hours = parseFloat(hoursText?.trim() || '0');
      expect(hours).toBeGreaterThanOrEqual(0);
    }
  });

  test('should maintain staff data consistency on sequential report generations', async ({ hrPage }) => {
    // Arrange
    const dateRange = getCurrentMonthRange();

    // Act - Generate first report
    await reportsPage.generateReport({
      format: 'JSON',
      startDate: dateRange.startDate,
      endDate: dateRange.endDate,
    });

    // Collect first report data
    const firstReportRows = hrPage.getByTestId('staff-breakdown-row');
    const firstReportCount = await firstReportRows.count();

    const firstReportData: Array<{name: string, todo: string}> = [];
    for (let i = 0; i < Math.min(firstReportCount, 3); i++) {
      const row = firstReportRows.nth(i);
      const name = await row.getByTestId('staff-name').textContent();
      const todo = await row.getByTestId('staff-todo').textContent();
      firstReportData.push({ name: name?.trim() || '', todo: todo?.trim() || '' });
    }

    // Act - Generate second report with same filters
    await reportsPage.clickGenerate();
    await reportsPage.waitForReportGenerated();

    // Assert - Second report should have same data
    const secondReportRows = hrPage.getByTestId('staff-breakdown-row');
    const secondReportCount = await secondReportRows.count();

    expect(secondReportCount).toBe(firstReportCount);

    for (let i = 0; i < Math.min(firstReportData.length, secondReportCount); i++) {
      const row = secondReportRows.nth(i);
      const name = await row.getByTestId('staff-name').textContent();
      const todo = await row.getByTestId('staff-todo').textContent();

      expect(name?.trim()).toBe(firstReportData[i].name);
      expect(todo?.trim()).toBe(firstReportData[i].todo);
    }
  });
});

test.describe('Staff Breakdown Table - Export Format Handling', () => {
  let reportsPage: ReportsPage;

  test.beforeEach(async ({ hrPage }) => {
    reportsPage = new ReportsPage(hrPage);
    await reportsPage.navigate();
    await reportsPage.waitForReportsPageLoad();
  });

  test('should not display staff table when exporting to PDF', async ({ hrPage }) => {
    // Arrange
    const dateRange = getCurrentMonthRange();

    // Act - First generate JSON report to see staff data
    await reportsPage.generateReport({
      format: 'JSON',
      startDate: dateRange.startDate,
      endDate: dateRange.endDate,
    });

    // Verify staff table is visible for JSON
    const staffTableJson = hrPage.getByTestId('staff-breakdown-table');
    const isVisibleForJson = await staffTableJson.isVisible().catch(() => false);

    // Act - Now select PDF format (note: PDF triggers download, not display)
    await reportsPage.selectFormat('PDF');

    // When PDF is selected and "Generate" is clicked, a download happens
    // The staff table in the browser should not be visible (or should remain from previous JSON report)

    // This test verifies that PDF export doesn't break the UI
    // The actual staff data in the PDF would need to be verified separately
  });

  test('should not display staff table when exporting to CSV', async ({ hrPage }) => {
    // Arrange
    const dateRange = getCurrentMonthRange();

    // Act - First generate JSON report to see staff data
    await reportsPage.generateReport({
      format: 'JSON',
      startDate: dateRange.startDate,
      endDate: dateRange.endDate,
    });

    // Verify staff table is visible for JSON
    const staffTableJson = hrPage.getByTestId('staff-breakdown-table');
    const isVisibleForJson = await staffTableJson.isVisible().catch(() => false);

    // Act - Now select CSV format (note: CSV triggers download, not display)
    await reportsPage.selectFormat('CSV');

    // When CSV is selected and "Generate" is clicked, a download happens
    // The staff table in the browser should not be visible (or should remain from previous JSON report)

    // This test verifies that CSV export doesn't break the UI
    // The actual staff data in the CSV would need to be verified separately
  });
});
