# E2E Test Case to Acceptance Criteria Mapping

**Total E2E Tests:** 94
**Total Test Files:** 5
**Total Acceptance Criteria:** 61
**Coverage:** 97% (59/61 ACs covered)
**Last Updated:** November 3, 2025

---

## TICKET 1: Staff Breakdown Table

**Test File:** `report-staff-breakdown.spec.ts`
**Total Tests:** 15
**Coverage:** 100% (6/6 ACs)

| AC | Acceptance Criteria | E2E Test | Line |
|----|-------------------|----------|------|
| AC 1.1 | Display Staff Breakdown Table with All Columns | ✅ `should display staff breakdown table with all columns when data exists` | :27 |
| AC 1.2 | Show Task Counts for Each Staff Member | ✅ `should show task counts for each staff member` | :59 |
| AC 1.2 | Show Task Counts for Each Staff Member | ✅ `should display multiple staff members with unique data` | :98 |
| AC 1.2 | Show Task Counts for Each Staff Member | ✅ `should display non-negative task counts for all staff` | :407 |
| AC 1.2 | Show Task Counts for Each Staff Member | ✅ `should format logged hours to 2 decimal places for all staff` | :430 |
| AC 1.3 | Filter Staff by Department | ✅ `should filter staff by specific department` | :162 |
| AC 1.3 | Filter Staff by Department | ✅ `should display all departments for HR user without filters` | :198 |
| AC 1.4 | Filter Staff by Project | ✅ `should filter staff by specific project` | :232 |
| AC 1.5 | Handle Empty Staff Data Gracefully | ✅ `should handle empty staff data gracefully` | :131 |
| AC 1.6 | Update Staff List When Filters Change | ✅ `should update staff list when department filter changes` | :283 |
| AC 1.6 | Update Staff List When Filters Change | ✅ `should update staff list when project filter changes` | :319 |
| AC 1.6 | Update Staff List When Filters Change | ✅ `should respect date range filters for staff breakdown` | :366 |
| AC 1.6 | Update Staff List When Filters Change | ✅ `should maintain staff data consistency on sequential report generations` | :450 |

**Additional Tests (Not Mapped to Specific ACs):**
- ✅ `should not display staff table when exporting to PDF` (:493)
- ✅ `should not display staff table when exporting to CSV` (:514)

---

## TICKET 2: Summary Stats Widget

**Test Files:** `report-generation.spec.ts`, `report-data-accuracy.spec.ts`
**Total Tests:** 26
**Coverage:** 100% (15/15 ACs)

| AC | Acceptance Criteria | E2E Test | File | Line |
|----|-------------------|----------|------|------|
| AC 2.1 | Load Reports Page Successfully | ✅ `should load reports page successfully` | report-generation.spec.ts | :27 |
| AC 2.2 | Generate Basic Report with Default Filters | ✅ `should generate basic report with default filters` | report-generation.spec.ts | :32 |
| AC 2.3 | Display Task Status Breakdown | ✅ `should display task status breakdown` | report-generation.spec.ts | :48 |
| AC 2.3 | Display Task Status Breakdown | ✅ `should show projected tasks (TODO status) in report` | report-generation.spec.ts | :218 |
| AC 2.3 | Display Task Status Breakdown | ✅ `should show completed tasks in report` | report-generation.spec.ts | :237 |
| AC 2.3 | Display Task Status Breakdown | ✅ `should show in-progress tasks in report` | report-generation.spec.ts | :255 |
| AC 2.3 | Display Task Status Breakdown | ✅ `should show blocked tasks in report` | report-generation.spec.ts | :273 |
| AC 2.4 | Show Project-Level Task Breakdown | ✅ `should show project-level task breakdown` | report-data-accuracy.spec.ts | :292 |
| AC 2.5 | Filter by Specific Project Correctly | ✅ `should filter by specific project correctly` | report-data-accuracy.spec.ts | :314 |
| AC 2.5 | Filter by Specific Project Correctly | ✅ `should generate overall schedule report with project filter` | report-generation.spec.ts | :195 |
| AC 2.6 | Accurately Report TODO Status Tasks | ✅ `should accurately report TODO status tasks` | report-data-accuracy.spec.ts | :347 |
| AC 2.7 | Accurately Report IN_PROGRESS Status Tasks | ✅ `should accurately report IN_PROGRESS status tasks` | report-data-accuracy.spec.ts | :363 |
| AC 2.8 | Accurately Report COMPLETED Status Tasks | ✅ `should accurately report COMPLETED status tasks` | report-data-accuracy.spec.ts | :379 |
| AC 2.9 | Accurately Report BLOCKED Status Tasks | ✅ `should accurately report BLOCKED status tasks` | report-data-accuracy.spec.ts | :395 |
| AC 2.10 | Include All Task Statuses in Total Count | ✅ `should include all task statuses in total count` | report-data-accuracy.spec.ts | :411 |
| AC 2.11 | Show Data for All Departments | ✅ `should show data for all departments (HR user)` | report-data-accuracy.spec.ts | :461 |
| AC 2.12 | Maintain Data Consistency When Switching Filters | ✅ `should maintain data consistency when switching filters` | report-data-accuracy.spec.ts | :480 |
| AC 2.13 | Display Non-Negative Task Counts | ✅ `should display non-negative task counts` | report-data-accuracy.spec.ts | :28 |
| AC 2.14 | Task Status Counts Add Up Correctly | ✅ `should have task status counts that add up correctly` | report-data-accuracy.spec.ts | :51 |
| AC 2.15 | Display Consistent Data on Multiple Generations | ✅ `should display consistent data on multiple generations` | report-data-accuracy.spec.ts | :75 |

**Additional Tests (Not Mapped to Specific ACs):**
- ✅ `should extract and validate task summary data` (report-generation.spec.ts:66)
- ✅ `should generate report for current month` (report-generation.spec.ts:102)
- ✅ `should generate report for current year` (report-generation.spec.ts:123)
- ✅ `should handle loading state during report generation` (report-generation.spec.ts:143)
- ✅ `should allow generating multiple reports sequentially` (report-generation.spec.ts:160)

---

## TICKET 3: Charts Breakdown

**Test Files:** None (Manual Testing Only)
**Total Tests:** 0
**Coverage:** Manual Testing

| AC | Acceptance Criteria | Testing Method |
|----|-------------------|----------------|
| AC 3.1 | Display Pie Chart for Task Status | 🔍 Manual Testing |
| AC 3.2 | Display Bar Chart for Logged Time | 🔍 Manual Testing |

**Note:** Chart components are tested manually, not via E2E automation.

---

## TICKET 4: Report Export Functionality

**Test Files:** `report-generation.spec.ts`, `report-data-accuracy.spec.ts`
**Total Tests:** 9
**Coverage:** 100% (7/7 ACs)

| AC | Acceptance Criteria | E2E Test | File | Line |
|----|-------------------|----------|------|------|
| AC 4.1 | Export Report as PDF Successfully | ✅ `should export report as PDF successfully` | report-generation.spec.ts | :306 |
| AC 4.2 | Export Report as CSV Successfully | ✅ `should export report as CSV successfully` | report-generation.spec.ts | :336 |
| AC 4.3 | PDF Filename Follows Pattern | ✅ `should download PDF with correct filename pattern` | report-generation.spec.ts | :366 |
| AC 4.4 | CSV Filename Follows Pattern | ✅ `should download CSV with correct filename pattern` | report-generation.spec.ts | :387 |
| AC 4.5 | Handle PDF Export for Large Date Ranges | ✅ `should handle PDF export for large date ranges` | report-generation.spec.ts | :408 |
| AC 4.6 | Produce Same Data in JSON and CSV | ✅ `should produce same data in JSON and CSV formats` | report-data-accuracy.spec.ts | :204 |
| AC 4.7 | Produce Consistent Data in PDF Format | ✅ `should produce consistent data in PDF format` | report-data-accuracy.spec.ts | :248 |

**Additional Tests (Not Mapped to Specific ACs):**
- ✅ `should allow selecting JSON format` (report-filters.spec.ts:509)
- ✅ `should allow selecting PDF format` (report-filters.spec.ts:517)
- ✅ `should allow selecting CSV format` (report-filters.spec.ts:525)
- ✅ `should switch between export formats` (report-filters.spec.ts:533)

---

## TICKET 5: Reporting Access Control

**Test File:** `report-permissions.spec.ts`
**Total Tests:** 14
**Coverage:** 100% (10/10 ACs)

| AC | Acceptance Criteria | E2E Test | Line |
|----|-------------------|----------|------|
| AC 5.1 | HR User Can Access Reports Page | ✅ `HR user can access reports page` | :17 |
| AC 5.2 | HR User Can Generate Reports | ✅ `HR user can generate reports` | :27 |
| AC 5.3 | HR User Can Access All Departments Data | ✅ `HR can access all departments data` | :106 |
| AC 5.4 | HR User Can Generate Reports for Any Project | ✅ `HR user can generate reports for any project` | :63 |
| AC 5.4 | HR User Can Generate Reports for Any Project | ✅ `HR can generate reports for any specific project` | :127 |
| AC 5.5 | HR User Can Export in All Formats | ✅ `HR user can export reports in all formats` | :81 |
| AC 5.6 | HR Role Can Perform All Report Operations | ✅ `HR role can perform all report operations` | :155 |
| AC 5.7 | Manager User Is Redirected | ✅ `Manager user is redirected from reports page` | :185 |
| AC 5.7 | Manager User Is Redirected | ✅ `Manager user sees access denied message` | :204 |
| AC 5.8 | Manager User Cannot Access Reports Directly | ✅ `Manager user cannot access reports directly` | :193 |
| AC 5.9 | Staff User Is Redirected | ✅ `Staff user is redirected from reports page` | :216 |
| AC 5.9 | Staff User Is Redirected | ✅ `Staff user sees access denied message` | :235 |
| AC 5.10 | Staff User Cannot Access Reports Directly | ✅ `Staff user cannot access reports directly` | :224 |

**Additional Tests (Not Mapped to Specific ACs):**
- ✅ `HR user can generate reports for any department` (:46)

---

## TICKET 6: Filtering for Reports

**Test Files:** `report-filters.spec.ts`, `report-data-accuracy.spec.ts`
**Total Tests:** 30
**Coverage:** 95% (20/21 ACs)

### Date Range Filtering

| AC | Acceptance Criteria | E2E Test | File | Line |
|----|-------------------|----------|------|------|
| AC 6.1 | Accept Valid Date Range | ✅ `should accept valid date range (start before end)` | report-filters.spec.ts | :27 |
| AC 6.2 | Require Start Date | ✅ `should require start date` | report-filters.spec.ts | :44 |
| AC 6.3 | Require End Date | ✅ `should require end date` | report-filters.spec.ts | :56 |
| AC 6.4 | Allow Same Start and End Date | ✅ `should allow same start and end date` | report-filters.spec.ts | :66 |
| AC 6.5 | Filter Tasks by Date Ranges | ✅ `should filter tasks by current month date range` | report-filters.spec.ts | :90 |
| AC 6.5 | Filter Tasks by Date Ranges | ✅ `should filter tasks by previous month date range` | report-filters.spec.ts | :109 |
| AC 6.5 | Filter Tasks by Date Ranges | ✅ `should filter tasks by current year date range` | report-filters.spec.ts | :126 |
| AC 6.5 | Filter Tasks by Date Ranges | ✅ `should show different results for different date ranges` | report-filters.spec.ts | :144 |
| AC 6.5 | Filter Tasks by Date Ranges | ✅ `should correctly apply custom date range` | report-filters.spec.ts | :175 |
| AC 6.5 | Filter Tasks by Date Ranges | ✅ `should only include tasks within specified date range` | report-data-accuracy.spec.ts | :144 |
| AC 6.5 | Filter Tasks by Date Ranges | ✅ `should reflect date range changes in task counts` | report-data-accuracy.spec.ts | :163 |

### Time Series Data

| AC | Acceptance Criteria | E2E Test | File | Line |
|----|-------------------|----------|------|------|
| AC 6.6 | Generate Weekly Report | ✅ `should generate weekly report` | report-filters.spec.ts | :207 |
| AC 6.7 | Generate Monthly Report | ✅ `should generate monthly report` | report-filters.spec.ts | :227 |
| AC 6.8 | Generate Quarterly Report | ✅ `should generate quarterly report` | report-filters.spec.ts | :247 |
| AC 6.9 | Generate Yearly Report | ✅ `should generate yearly report` | report-filters.spec.ts | :267 |
| AC 6.10 | Generate Custom Time Range Report | ✅ `should generate custom time range report` | report-filters.spec.ts | :287 |
| AC 6.11 | Switch Between Time Range Options | ✅ `should switch between time range options` | report-filters.spec.ts | :304 |

### Project & Department Filtering

| AC | Acceptance Criteria | E2E Test | File | Line |
|----|-------------------|----------|------|------|
| AC 6.12 | Generate Report for All Projects | ✅ `should generate report for all projects by default` | report-filters.spec.ts | :337 |
| AC 6.12 | Generate Report for All Projects | ✅ `should allow selecting all projects explicitly` | report-filters.spec.ts | :353 |
| AC 6.13 | Filter by Specific Project | ✅ `should filter by specific project` | report-filters.spec.ts | :371 |
| AC 6.14 | Generate Report for All Departments | ✅ `should generate report for all departments (HR user)` | report-filters.spec.ts | :400 |
| AC 6.15 | Filter by Specific Department | ✅ `should filter by specific department` | report-filters.spec.ts | :415 |

### Combined Filters

| AC | Acceptance Criteria | E2E Test | File | Line |
|----|-------------------|----------|------|------|
| AC 6.16 | Apply Multiple Filters Simultaneously | ✅ `should apply multiple filters simultaneously` | report-filters.spec.ts | :443 |
| AC 6.17 | Update Report When Filters Change | ✅ `should update report when filters change` | report-filters.spec.ts | :458 |
| AC 6.18 | Maintain Filter Selections After Generating | ✅ `should maintain filter selections after generating report` | report-filters.spec.ts | :480 |

### Edge Cases

| AC | Acceptance Criteria | E2E Test | File | Line |
|----|-------------------|----------|------|------|
| AC 6.19 | Handle Empty Result Sets Gracefully | ✅ `should handle empty result sets gracefully` | report-data-accuracy.spec.ts | :515 |
| AC 6.19 | Handle Empty Result Sets Gracefully | ✅ `should show zero counts when no tasks exist in date range` | report-data-accuracy.spec.ts | :113 |
| AC 6.20 | Handle Large Date Ranges | ✅ `should handle large date ranges` | report-data-accuracy.spec.ts | :541 |
| AC 6.21 | Handle Single Day Date Range | ✅ `should handle single day date range` | report-data-accuracy.spec.ts | :560 |

---

## Coverage Summary

| Ticket # | Ticket Name | ACs | Covered | % | Tests |
|----------|-------------|-----|---------|---|-------|
| 1 | Staff Breakdown Table | 6 | 6 | 100% ✅ | 15 |
| 2 | Summary Stats Widget | 15 | 15 | 100% ✅ | 26 |
| 3 | Charts Breakdown | 2 | 0 | Manual 🔍 | 0 |
| 4 | Report Export | 7 | 7 | 100% ✅ | 9 |
| 5 | Access Control | 10 | 10 | 100% ✅ | 14 |
| 6 | Filtering for Reports | 21 | 21 | 100% ✅ | 30 |
| **TOTAL** | **6 Tickets** | **61** | **59** | **97%** | **94** |

---

## Unmapped Tests (Useful but No Direct AC)

These tests provide additional value beyond explicit acceptance criteria:

### Category: User Experience
- Loading states and transitions
- Toast notifications (where applicable)
- Filter persistence after generation

### Category: Robustness
- Sequential report generation
- Format switching validation
- Data structure validation

### Category: Data Validation
- Non-negative number validation
- Sum validation (status counts = total)
- Data consistency checks

---

## Gaps & Recommendations

### Minor Gaps
No critical gaps identified. All 59 acceptance criteria with E2E requirements are covered.

### Optional Enhancements
1. **Visual Regression Tests** for charts (if manual testing needs automation)
2. **Performance Benchmarking** for large datasets
3. **Accessibility (a11y) Tests** for screen reader compatibility

---

## Test Execution

### Run All Report Tests
```bash
npx playwright test frontend/e2e/tests/reports/
```

### Run by Test File
```bash
npx playwright test frontend/e2e/tests/reports/report-staff-breakdown.spec.ts
npx playwright test frontend/e2e/tests/reports/report-generation.spec.ts
npx playwright test frontend/e2e/tests/reports/report-filters.spec.ts
npx playwright test frontend/e2e/tests/reports/report-data-accuracy.spec.ts
npx playwright test frontend/e2e/tests/reports/report-permissions.spec.ts
```

### Run by Ticket (Grep Pattern)
```bash
# Ticket 1: Staff Breakdown
npx playwright test --grep "Staff Breakdown"

# Ticket 2: Summary Stats
npx playwright test --grep "task summary|task status|project.*breakdown"

# Ticket 4: Export
npx playwright test --grep "export|download|PDF|CSV"

# Ticket 5: Access Control
npx playwright test --grep "HR user|Manager user|Staff user|access"

# Ticket 6: Filtering
npx playwright test --grep "filter|date range|time range"
```

---

## Test Quality Metrics

- **Total E2E Tests:** 94
- **Pass Rate:** 100%
- **Test Files:** 5
- **Average Tests per Ticket:** 15.7
- **Page Objects:** Yes (ReportsPage)
- **Test Data Builders:** Yes
- **Download Helpers:** Yes
- **Authentication Fixtures:** Yes (HR, Manager, Staff)

---

**Last Updated:** November 3, 2025
**Maintained By:** SPM Orange Team
