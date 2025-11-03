# E2E Test Cases - Report Generation Feature
## Test Coverage Breakdown by Tickets

**Total Tests:** 79
**Test Files:** 4
**Last Updated:** November 3, 2025

---

## 📋 TICKET 1: Basic Report Generation & Page Functionality

**Epic:** Report Generation  
**Priority:** P0 (Critical)  
**Test File:** `report-generation.spec.ts`

### User Story
As an HR user, I want to access the reports page and generate basic reports so that I can view task summaries for my organization.

### Acceptance Criteria & Test Cases

#### AC 1.1: Load Reports Page Successfully
**Given:** I am an authenticated HR user  
**When:** I navigate to the reports page  
**Then:** The page should load with all filter controls visible and the generate button enabled

**E2E Test:** `should load reports page successfully`  
**Test ID:** `report-generation.spec.ts:27`  
**Status:** ✅ Passing

---

#### AC 1.2: Generate Basic Report with Default Filters
**Given:** I am on the reports page  
**When:** I generate a report using default filters (current month, JSON format)  
**Then:** The report should display with task summary and task counts visible

**E2E Test:** `should generate basic report with default filters`  
**Test ID:** `report-generation.spec.ts:32`  
**Status:** ✅ Passing

---

#### AC 1.3: Display Task Status Breakdown
**Given:** I have generated a report  
**When:** I view the task summary section  
**Then:** All task statuses should be visible (TODO, IN_PROGRESS, COMPLETED, BLOCKED)

**E2E Test:** `should display task status breakdown (TODO, IN_PROGRESS, COMPLETED, BLOCKED)`  
**Test ID:** `report-generation.spec.ts:48`  
**Status:** ✅ Passing

---

#### AC 1.4: Extract and Validate Task Summary Data
**Given:** A report has been generated  
**When:** I read the task summary data  
**Then:** All task counts should be numbers ≥ 0 and the data structure should be valid

**E2E Test:** `should extract and validate task summary data`  
**Test ID:** `report-generation.spec.ts:66`  
**Status:** ✅ Passing

---

#### AC 1.5: Generate Report for Current Month
**Given:** I select the current month date range  
**When:** I generate the report  
**Then:** The report should display tasks from the current month only

**E2E Test:** `should generate report for current month`  
**Test ID:** `report-generation.spec.ts:102`  
**Status:** ✅ Passing

---

#### AC 1.6: Generate Report for Current Year
**Given:** I select YEARLY as the time range  
**When:** I generate the report  
**Then:** The report should display tasks from the entire current year

**E2E Test:** `should generate report for current year`  
**Test ID:** `report-generation.spec.ts:123`  
**Status:** ✅ Passing

---

#### AC 1.7: Handle Loading State During Report Generation
**Given:** I click the generate button  
**When:** The report is being generated  
**Then:** A loading indicator should appear and disappear once complete

**E2E Test:** `should handle loading state during report generation`  
**Test ID:** `report-generation.spec.ts:143`  
**Status:** ✅ Passing

---

#### AC 1.8: Allow Generating Multiple Reports Sequentially
**Given:** I have generated one report  
**When:** I change filters and generate another report  
**Then:** The new report should replace the old one with updated data

**E2E Test:** `should allow generating multiple reports sequentially`  
**Test ID:** `report-generation.spec.ts:160`  
**Status:** ✅ Passing

---

## 📊 TICKET 2: Overall Schedule Report - Task Status Visibility

**Epic:** Report Generation  
**Priority:** P0 (Critical)  
**Test File:** `report-generation.spec.ts`

### User Story
As an HR user, I want to see a comprehensive schedule report showing projected, completed, in-progress, and blocked tasks so that I can understand the overall project status.

### Acceptance Criteria & Test Cases

#### AC 2.1: Generate Overall Schedule Report with Project Filter
**Given:** I select a specific project  
**When:** I generate a report  
**Then:** The report should display only tasks from that project

**E2E Test:** `should generate overall schedule report with project filter`  
**Test ID:** `report-generation.spec.ts:195`  
**Status:** ✅ Passing

---

#### AC 2.2: Show Projected Tasks (TODO Status)
**Given:** I have generated a report  
**When:** I view the task summary  
**Then:** The TODO/projected tasks count should be visible and accurate

**E2E Test:** `should show projected tasks (TODO status) in report`  
**Test ID:** `report-generation.spec.ts:218`  
**Status:** ✅ Passing

---

#### AC 2.3: Show Completed Tasks
**Given:** I have generated a report  
**When:** I view the task summary  
**Then:** The completed tasks count and label should be visible

**E2E Test:** `should show completed tasks in report`  
**Test ID:** `report-generation.spec.ts:237`  
**Status:** ✅ Passing

---

#### AC 2.4: Show In-Progress Tasks
**Given:** I have generated a report  
**When:** I view the task summary  
**Then:** The in-progress tasks count and label should be visible

**E2E Test:** `should show in-progress tasks in report`  
**Test ID:** `report-generation.spec.ts:255`  
**Status:** ✅ Passing

---

#### AC 2.5: Show Blocked Tasks
**Given:** I have generated a report  
**When:** I view the task summary  
**Then:** The blocked tasks count and label should be visible

**E2E Test:** `should show blocked tasks in report`  
**Test ID:** `report-generation.spec.ts:273`  
**Status:** ✅ Passing

---

## 📥 TICKET 3: Report Export Functionality (PDF & CSV)

**Epic:** Report Export  
**Priority:** P0 (Critical)  
**Test File:** `report-generation.spec.ts`

### User Story
As an HR user, I want to export reports as PDF or CSV files so that I can share them with stakeholders or analyze data in Excel.

### Acceptance Criteria & Test Cases

#### AC 3.1: Export Report as PDF Successfully
**Given:** I have set report filters  
**When:** I select PDF format and click generate  
**Then:** A PDF file should be downloaded with a toast notification confirming success

**E2E Test:** `should export report as PDF successfully`  
**Test ID:** `report-generation.spec.ts:306`  
**Status:** ✅ Passing

---

#### AC 3.2: Export Report as CSV Successfully
**Given:** I have set report filters  
**When:** I select CSV format and click generate  
**Then:** A CSV file should be downloaded with a toast notification confirming success

**E2E Test:** `should export report as CSV successfully`  
**Test ID:** `report-generation.spec.ts:336`  
**Status:** ✅ Passing

---

#### AC 3.3: Download PDF with Correct Filename Pattern
**Given:** I generate a PDF report  
**When:** The file is downloaded  
**Then:** The filename should follow the pattern: `report_[timestamp].pdf`

**E2E Test:** `should download PDF with correct filename pattern`  
**Test ID:** `report-generation.spec.ts:366`  
**Status:** ✅ Passing

---

#### AC 3.4: Download CSV with Correct Filename Pattern
**Given:** I generate a CSV report  
**When:** The file is downloaded  
**Then:** The filename should follow the pattern: `report_[timestamp].csv`

**E2E Test:** `should download CSV with correct filename pattern`  
**Test ID:** `report-generation.spec.ts:387`  
**Status:** ✅ Passing

---

#### AC 3.5: Handle PDF Export for Large Date Ranges
**Given:** I select a large date range (e.g., entire year)  
**When:** I generate a PDF report  
**Then:** The PDF should download successfully without timeout

**E2E Test:** `should handle PDF export for large date ranges`  
**Test ID:** `report-generation.spec.ts:408`  
**Status:** ✅ Passing

---

## 🔒 TICKET 4: Report Permissions - HR Access Control & Role-Based Access

**Epic:** Security & Access Control
**Priority:** P0 (Critical)
**Test File:** `report-permissions.spec.ts`

### User Story
As an HR user, I want exclusive access to generate reports for any department and project so that I can perform my HR oversight duties. As a Manager or Staff user, I should be denied access to the reports page to maintain data security.

### Acceptance Criteria & Test Cases

#### AC 4.1: HR User Can Access Reports Page
**Given:** I am authenticated as an HR user  
**When:** I navigate to `/reports`  
**Then:** The page should load successfully with all controls visible

**E2E Test:** `HR user can access reports page`  
**Test ID:** `report-permissions.spec.ts:17`  
**Status:** ✅ Passing

---

#### AC 4.2: HR User Can Generate Reports
**Given:** I am on the reports page as an HR user  
**When:** I set filters and click generate  
**Then:** The report should be generated successfully

**E2E Test:** `HR user can generate reports`  
**Test ID:** `report-permissions.spec.ts:27`  
**Status:** ✅ Passing

---

#### AC 4.3: HR User Can Generate Reports for Any Department
**Given:** I am an HR user  
**When:** I select any department or no department filter  
**Then:** The report should generate without access errors

**E2E Test:** `HR user can generate reports for any department`  
**Test ID:** `report-permissions.spec.ts:46`  
**Status:** ✅ Passing

---

#### AC 4.4: HR User Can Generate Reports for Any Project
**Given:** I am an HR user  
**When:** I select any project from any department  
**Then:** The report should generate successfully

**E2E Test:** `HR user can generate reports for any project`  
**Test ID:** `report-permissions.spec.ts:63`  
**Status:** ✅ Passing

---

#### AC 4.5: HR User Can Export Reports in All Formats
**Given:** I am an HR user  
**When:** I select JSON, PDF, or CSV format  
**Then:** The report should generate/download successfully in any format

**E2E Test:** `HR user can export reports in all formats`  
**Test ID:** `report-permissions.spec.ts:81`  
**Status:** ✅ Passing

---

#### AC 4.6: HR Can Access All Departments Data
**Given:** I am an HR user  
**When:** I generate a report without department filter  
**Then:** I should see data from all departments combined

**E2E Test:** `HR can access all departments data`  
**Test ID:** `report-permissions.spec.ts:106`  
**Status:** ✅ Passing

---

#### AC 4.7: HR Can Generate Reports for Any Specific Project
**Given:** I am an HR user  
**When:** I select a specific project ID  
**Then:** The report should generate for that project regardless of department

**E2E Test:** `HR can generate reports for any specific project`  
**Test ID:** `report-permissions.spec.ts:127`  
**Status:** ✅ Passing

---

#### AC 4.8: HR Role Can Perform All Report Operations
**Given:** I am authenticated as an HR user
**When:** I perform any report operation (generate, export, filter)
**Then:** All operations should succeed without permission errors

**E2E Test:** `HR role can perform all report operations`
**Test ID:** `report-permissions.spec.ts:155`
**Status:** ✅ Passing

---

#### AC 4.9: Manager User Is Redirected from Reports Page
**Given:** I am authenticated as a Manager user
**When:** I navigate to `/reports`
**Then:** I should be redirected to the dashboard

**E2E Test:** `Manager user is redirected from reports page`
**Test ID:** `report-permissions.spec.ts:185`
**Status:** ✅ Passing

---

#### AC 4.10: Manager User Cannot Access Reports Directly
**Given:** I am a Manager user
**When:** I try to navigate directly to the reports page
**Then:** I should be redirected to the dashboard and not see the reports page

**E2E Test:** `Manager user cannot access reports directly`
**Test ID:** `report-permissions.spec.ts:193`
**Status:** ✅ Passing

---

#### AC 4.11: Manager User Sees Access Denied Message
**Given:** I am a Manager user attempting to access reports
**When:** I navigate to `/reports`
**Then:** I should see an access denied notification before being redirected

**E2E Test:** `Manager user sees access denied message`
**Test ID:** `report-permissions.spec.ts:204`
**Status:** ✅ Passing

---

#### AC 4.12: Staff User Is Redirected from Reports Page
**Given:** I am authenticated as a Staff user
**When:** I navigate to `/reports`
**Then:** I should be redirected to the dashboard

**E2E Test:** `Staff user is redirected from reports page`
**Test ID:** `report-permissions.spec.ts:216`
**Status:** ✅ Passing

---

#### AC 4.13: Staff User Cannot Access Reports Directly
**Given:** I am a Staff user
**When:** I try to navigate directly to the reports page
**Then:** I should be redirected to the dashboard and not see the reports page

**E2E Test:** `Staff user cannot access reports directly`
**Test ID:** `report-permissions.spec.ts:224`
**Status:** ✅ Passing

---

#### AC 4.14: Staff User Sees Access Denied Message
**Given:** I am a Staff user attempting to access reports
**When:** I navigate to `/reports`
**Then:** I should see an access denied notification before being redirected

**E2E Test:** `Staff user sees access denied message`
**Test ID:** `report-permissions.spec.ts:235`
**Status:** ✅ Passing

---

## 🗓️ TICKET 5: Date Range Filtering

**Epic:** Report Filters  
**Priority:** P0 (Critical)  
**Test File:** `report-filters.spec.ts`

### User Story
As an HR user, I want to filter reports by date ranges so that I can analyze tasks within specific time periods.

### Acceptance Criteria & Test Cases

#### AC 5.1: Accept Valid Date Range (Start Before End)
**Given:** I am on the reports page  
**When:** I set a start date before the end date  
**Then:** The generate button should be enabled and report should generate successfully

**E2E Test:** `should accept valid date range (start before end)`  
**Test ID:** `report-filters.spec.ts:27`  
**Status:** ✅ Passing

---

#### AC 5.2: Require Start Date
**Given:** I am on the reports page  
**When:** I leave the start date empty  
**Then:** The form should indicate that start date is required

**E2E Test:** `should require start date`  
**Test ID:** `report-filters.spec.ts:44`  
**Status:** ✅ Passing

---

#### AC 5.3: Require End Date
**Given:** I am on the reports page  
**When:** I leave the end date empty  
**Then:** The form should indicate that end date is required

**E2E Test:** `should require end date`  
**Test ID:** `report-filters.spec.ts:56`  
**Status:** ✅ Passing

---

#### AC 5.4: Allow Same Start and End Date
**Given:** I am on the reports page  
**When:** I set the same date for start and end (single day)  
**Then:** The report should generate for that single day

**E2E Test:** `should allow same start and end date`  
**Test ID:** `report-filters.spec.ts:66`  
**Status:** ✅ Passing

---

#### AC 5.5: Filter Tasks by Current Month Date Range
**Given:** I select the current month date range  
**When:** I generate the report  
**Then:** Only tasks from the current month should be included

**E2E Test:** `should filter tasks by current month date range`  
**Test ID:** `report-filters.spec.ts:90`  
**Status:** ✅ Passing

---

#### AC 5.6: Filter Tasks by Previous Month Date Range
**Given:** I select the previous month date range  
**When:** I generate the report  
**Then:** Only tasks from the previous month should be included

**E2E Test:** `should filter tasks by previous month date range`  
**Test ID:** `report-filters.spec.ts:109`  
**Status:** ✅ Passing

---

#### AC 5.7: Filter Tasks by Current Year Date Range
**Given:** I select the current year date range  
**When:** I generate the report  
**Then:** Only tasks from the current year should be included

**E2E Test:** `should filter tasks by current year date range`  
**Test ID:** `report-filters.spec.ts:126`  
**Status:** ✅ Passing

---

#### AC 5.8: Show Different Results for Different Date Ranges
**Given:** I generate a report for date range A  
**When:** I change to date range B and regenerate  
**Then:** The task counts should be different (reflecting the new range)

**E2E Test:** `should show different results for different date ranges`  
**Test ID:** `report-filters.spec.ts:144`  
**Status:** ✅ Passing

---

#### AC 5.9: Correctly Apply Custom Date Range
**Given:** I select CUSTOM time range  
**When:** I set specific start and end dates  
**Then:** The report should include only tasks within that custom range

**E2E Test:** `should correctly apply custom date range`  
**Test ID:** `report-filters.spec.ts:175`  
**Status:** ✅ Passing

---

## ⏰ TICKET 6: Time Range Options

**Epic:** Report Filters  
**Priority:** P1 (High)  
**Test File:** `report-filters.spec.ts`

### User Story
As an HR user, I want to quickly select common time ranges (weekly, monthly, quarterly, yearly) so that I don't have to manually enter dates each time.

### Acceptance Criteria & Test Cases

#### AC 6.1: Generate Weekly Report
**Given:** I select WEEKLY as the time range  
**When:** I generate the report  
**Then:** The report should cover the current week

**E2E Test:** `should generate weekly report`  
**Test ID:** `report-filters.spec.ts:207`  
**Status:** ✅ Passing

---

#### AC 6.2: Generate Monthly Report
**Given:** I select MONTHLY as the time range  
**When:** I generate the report  
**Then:** The report should cover the current month

**E2E Test:** `should generate monthly report`  
**Test ID:** `report-filters.spec.ts:227`  
**Status:** ✅ Passing

---

#### AC 6.3: Generate Quarterly Report
**Given:** I select QUARTERLY as the time range  
**When:** I generate the report  
**Then:** The report should cover the current quarter

**E2E Test:** `should generate quarterly report`  
**Test ID:** `report-filters.spec.ts:247`  
**Status:** ✅ Passing

---

#### AC 6.4: Generate Yearly Report
**Given:** I select YEARLY as the time range  
**When:** I generate the report  
**Then:** The report should cover the current year

**E2E Test:** `should generate yearly report`  
**Test ID:** `report-filters.spec.ts:267`  
**Status:** ✅ Passing

---

#### AC 6.5: Generate Custom Time Range Report
**Given:** I select CUSTOM as the time range  
**When:** I set specific dates and generate  
**Then:** The report should cover my custom date range

**E2E Test:** `should generate custom time range report`  
**Test ID:** `report-filters.spec.ts:287`  
**Status:** ✅ Passing

---

#### AC 6.6: Switch Between Time Range Options
**Given:** I have selected one time range  
**When:** I switch to a different time range option  
**Then:** The date fields should update to reflect the new time range

**E2E Test:** `should switch between time range options`  
**Test ID:** `report-filters.spec.ts:304`  
**Status:** ✅ Passing

---

## 📁 TICKET 7: Project & Department Filtering

**Epic:** Report Filters  
**Priority:** P1 (High)  
**Test File:** `report-filters.spec.ts`

### User Story
As an HR user, I want to filter reports by specific projects or departments so that I can generate focused reports for specific teams.

### Acceptance Criteria & Test Cases

#### AC 7.1: Generate Report for All Projects by Default
**Given:** I am on the reports page  
**When:** I don't select any specific project  
**Then:** The report should include data from all projects

**E2E Test:** `should generate report for all projects by default`  
**Test ID:** `report-filters.spec.ts:337`  
**Status:** ✅ Passing

---

#### AC 7.2: Allow Selecting All Projects Explicitly
**Given:** I am on the reports page  
**When:** I explicitly select "All Projects"  
**Then:** The report should include data from all projects

**E2E Test:** `should allow selecting all projects explicitly`  
**Test ID:** `report-filters.spec.ts:353`  
**Status:** ✅ Passing

---

#### AC 7.3: Filter by Specific Project
**Given:** I select a specific project from the dropdown  
**When:** I generate the report  
**Then:** Only tasks from that project should be included

**E2E Test:** `should filter by specific project`  
**Test ID:** `report-filters.spec.ts:371`  
**Status:** ✅ Passing

---

#### AC 7.4: Generate Report for All Departments (HR User)
**Given:** I am an HR user  
**When:** I select "All Departments"  
**Then:** The report should include data from all departments

**E2E Test:** `should generate report for all departments (HR user)`  
**Test ID:** `report-filters.spec.ts:400`  
**Status:** ✅ Passing

---

#### AC 7.5: Filter by Specific Department
**Given:** I select a specific department  
**When:** I generate the report  
**Then:** Only tasks/projects from that department should be included

**E2E Test:** `should filter by specific department`  
**Test ID:** `report-filters.spec.ts:415`  
**Status:** ✅ Passing

---

## 🔀 TICKET 8: Combined Filters & Export Format Selection

**Epic:** Report Filters  
**Priority:** P1 (High)  
**Test File:** `report-filters.spec.ts`

### User Story
As an HR user, I want to apply multiple filters simultaneously and select export formats so that I can generate precisely scoped reports in my preferred format.

### Acceptance Criteria & Test Cases

#### AC 8.1: Apply Multiple Filters Simultaneously
**Given:** I am on the reports page  
**When:** I select department, project, date range, and format together  
**Then:** The report should apply all filters correctly

**E2E Test:** `should apply multiple filters simultaneously`  
**Test ID:** `report-filters.spec.ts:443`  
**Status:** ✅ Passing

---

#### AC 8.2: Update Report When Filters Change
**Given:** I have generated a report  
**When:** I change one or more filters and regenerate  
**Then:** The new report should reflect the updated filters

**E2E Test:** `should update report when filters change`  
**Test ID:** `report-filters.spec.ts:458`  
**Status:** ✅ Passing

---

#### AC 8.3: Maintain Filter Selections After Generating Report
**Given:** I have set multiple filters  
**When:** I generate a report  
**Then:** The filter selections should remain in the UI (not reset)

**E2E Test:** `should maintain filter selections after generating report`  
**Test ID:** `report-filters.spec.ts:480`  
**Status:** ✅ Passing

---

#### AC 8.4: Allow Selecting JSON Format
**Given:** I am on the reports page  
**When:** I select JSON as the export format  
**Then:** The report should display in-page as JSON/structured data

**E2E Test:** `should allow selecting JSON format`  
**Test ID:** `report-filters.spec.ts:509`  
**Status:** ✅ Passing

---

#### AC 8.5: Allow Selecting PDF Format
**Given:** I am on the reports page  
**When:** I select PDF as the export format  
**Then:** A PDF file should be generated and downloaded

**E2E Test:** `should allow selecting PDF format`  
**Test ID:** `report-filters.spec.ts:517`  
**Status:** ✅ Passing

---

#### AC 8.6: Allow Selecting CSV Format
**Given:** I am on the reports page  
**When:** I select CSV as the export format  
**Then:** A CSV file should be generated and downloaded

**E2E Test:** `should allow selecting CSV format`  
**Test ID:** `report-filters.spec.ts:525`  
**Status:** ✅ Passing

---

#### AC 8.7: Switch Between Export Formats
**Given:** I have selected one export format  
**When:** I switch to a different export format  
**Then:** The selection should update and the new format should be used on next generation

**E2E Test:** `should switch between export formats`  
**Test ID:** `report-filters.spec.ts:533`  
**Status:** ✅ Passing

---

## ✅ TICKET 9: Data Accuracy - Task Count Validation

**Epic:** Data Integrity  
**Priority:** P0 (Critical)  
**Test File:** `report-data-accuracy.spec.ts`

### User Story
As an HR user, I want to trust that the task counts in reports are accurate so that I can make informed decisions based on the data.

### Acceptance Criteria & Test Cases

#### AC 9.1: Display Non-Negative Task Counts
**Given:** I generate a report  
**When:** I view the task summary  
**Then:** All task counts should be ≥ 0 (no negative numbers)

**E2E Test:** `should display non-negative task counts`  
**Test ID:** `report-data-accuracy.spec.ts:28`  
**Status:** ✅ Passing

---

#### AC 9.2: Task Status Counts Add Up Correctly
**Given:** I generate a report  
**When:** I check the task summary  
**Then:** completed + in-progress + todo + blocked should equal total tasks

**E2E Test:** `should have task status counts that add up correctly`  
**Test ID:** `report-data-accuracy.spec.ts:51`  
**Status:** ✅ Passing

---

#### AC 9.3: Display Consistent Data on Multiple Generations
**Given:** I generate a report  
**When:** I regenerate the same report (same filters) immediately  
**Then:** The task counts should be identical (no random fluctuations)

**E2E Test:** `should display consistent data on multiple generations`  
**Test ID:** `report-data-accuracy.spec.ts:75`  
**Status:** ✅ Passing

---

#### AC 9.4: Show Zero Counts When No Tasks Exist in Date Range
**Given:** I select a date range with no tasks  
**When:** I generate the report  
**Then:** All task counts should be 0 (not error or undefined)

**E2E Test:** `should show zero counts when no tasks exist in date range`  
**Test ID:** `report-data-accuracy.spec.ts:113`  
**Status:** ✅ Passing

---

## 📅 TICKET 10: Data Accuracy - Date Range & Cross-Format Consistency

**Epic:** Data Integrity  
**Priority:** P0 (Critical)  
**Test File:** `report-data-accuracy.spec.ts`

### User Story
As an HR user, I want reports to accurately reflect date ranges and show consistent data across all export formats so that I can trust the data regardless of how I export it.

### Acceptance Criteria & Test Cases

#### AC 10.1: Only Include Tasks Within Specified Date Range
**Given:** I set a specific date range  
**When:** I generate the report  
**Then:** Only tasks with dates inside that range should be counted

**E2E Test:** `should only include tasks within specified date range`  
**Test ID:** `report-data-accuracy.spec.ts:144`  
**Status:** ✅ Passing

---

#### AC 10.2: Reflect Date Range Changes in Task Counts
**Given:** I generate a report for date range A  
**When:** I change to date range B (non-overlapping) and regenerate  
**Then:** The task counts should change to reflect the new date range

**E2E Test:** `should reflect date range changes in task counts`  
**Test ID:** `report-data-accuracy.spec.ts:163`  
**Status:** ✅ Passing

---

#### AC 10.3: Produce Same Data in JSON and CSV Formats
**Given:** I generate a report with specific filters  
**When:** I export as JSON and then as CSV (same filters)  
**Then:** The task counts should be identical in both formats

**E2E Test:** `should produce same data in JSON and CSV formats`  
**Test ID:** `report-data-accuracy.spec.ts:204`  
**Status:** ✅ Passing

---

#### AC 10.4: Produce Consistent Data in PDF Format
**Given:** I generate a report with specific filters  
**When:** I export as JSON and then as PDF (same filters)  
**Then:** The data should be consistent (PDF contains same counts as JSON)

**E2E Test:** `should produce consistent data in PDF format`  
**Test ID:** `report-data-accuracy.spec.ts:248`  
**Status:** ✅ Passing

---

## 📊 TICKET 11: Data Accuracy - Project, Status & Department Validation

**Epic:** Data Integrity  
**Priority:** P1 (High)  
**Test File:** `report-data-accuracy.spec.ts`

### User Story
As an HR user, I want reports to accurately represent project breakdowns, task statuses, and department scoping so that I can drill down into specific areas with confidence.

### Acceptance Criteria & Test Cases

#### AC 11.1: Show Project-Level Task Breakdown
**Given:** I generate a report without project filter  
**When:** I view the report data  
**Then:** The report should show a breakdown of tasks by project

**E2E Test:** `should show project-level task breakdown`  
**Test ID:** `report-data-accuracy.spec.ts:292`  
**Status:** ✅ Passing

---

#### AC 11.2: Filter by Specific Project Correctly
**Given:** I select a specific project  
**When:** I generate the report  
**Then:** Only tasks from that project should be included in the counts

**E2E Test:** `should filter by specific project correctly`  
**Test ID:** `report-data-accuracy.spec.ts:314`  
**Status:** ✅ Passing

---

#### AC 11.3: Accurately Report TODO Status Tasks
**Given:** I generate a report  
**When:** I check the TODO count  
**Then:** It should match the actual number of tasks with TODO status

**E2E Test:** `should accurately report TODO status tasks`  
**Test ID:** `report-data-accuracy.spec.ts:347`  
**Status:** ✅ Passing

---

#### AC 11.4: Accurately Report IN_PROGRESS Status Tasks
**Given:** I generate a report  
**When:** I check the IN_PROGRESS count  
**Then:** It should match the actual number of tasks with IN_PROGRESS status

**E2E Test:** `should accurately report IN_PROGRESS status tasks`  
**Test ID:** `report-data-accuracy.spec.ts:363`  
**Status:** ✅ Passing

---

#### AC 11.5: Accurately Report COMPLETED Status Tasks
**Given:** I generate a report  
**When:** I check the COMPLETED count  
**Then:** It should match the actual number of tasks with COMPLETED status

**E2E Test:** `should accurately report COMPLETED status tasks`  
**Test ID:** `report-data-accuracy.spec.ts:379`  
**Status:** ✅ Passing

---

#### AC 11.6: Accurately Report BLOCKED Status Tasks
**Given:** I generate a report  
**When:** I check the BLOCKED count  
**Then:** It should match the actual number of tasks with BLOCKED status

**E2E Test:** `should accurately report BLOCKED status tasks`  
**Test ID:** `report-data-accuracy.spec.ts:395`  
**Status:** ✅ Passing

---

#### AC 11.7: Include All Task Statuses in Total Count
**Given:** I generate a report  
**When:** I check the total tasks count  
**Then:** It should equal the sum of all status counts (no tasks missing)

**E2E Test:** `should include all task statuses in total count`  
**Test ID:** `report-data-accuracy.spec.ts:411`  
**Status:** ✅ Passing

---

#### AC 11.8: Show Data for All Departments (HR User)
**Given:** I am an HR user with no department filter  
**When:** I generate the report  
**Then:** The report should include data from all departments

**E2E Test:** `should show data for all departments (HR user)`
**Test ID:** `report-data-accuracy.spec.ts:461`
**Status:** ✅ Passing

---

#### AC 11.9: Maintain Data Consistency When Switching Filters
**Given:** I generate a report with filter A
**When:** I switch to filter B and back to filter A
**Then:** The data should be the same as the first time (consistent)

**E2E Test:** `should maintain data consistency when switching filters`
**Test ID:** `report-data-accuracy.spec.ts:480`
**Status:** ✅ Passing

---

## 🔧 TICKET 12: Data Accuracy - Edge Cases

**Epic:** Data Integrity  
**Priority:** P2 (Medium)  
**Test File:** `report-data-accuracy.spec.ts`

### User Story
As an HR user, I want the reporting system to handle edge cases gracefully so that I don't encounter errors when using unusual date ranges or filters.

### Acceptance Criteria & Test Cases

#### AC 12.1: Handle Empty Result Sets Gracefully
**Given:** I set filters that match no tasks  
**When:** I generate the report  
**Then:** The report should display with all counts at 0 (no error)

**E2E Test:** `should handle empty result sets gracefully`
**Test ID:** `report-data-accuracy.spec.ts:515`
**Status:** ✅ Passing

---

#### AC 12.2: Handle Large Date Ranges
**Given:** I set a very large date range (e.g., 10 years)
**When:** I generate the report
**Then:** The report should generate successfully without timeout

**E2E Test:** `should handle large date ranges`
**Test ID:** `report-data-accuracy.spec.ts:541`
**Status:** ✅ Passing

---

#### AC 12.3: Handle Single Day Date Range
**Given:** I set start and end date to the same day  
**When:** I generate the report  
**Then:** The report should show tasks from that single day only

**E2E Test:** `should handle single day date range`
**Test ID:** `report-data-accuracy.spec.ts:560`
**Status:** ✅ Passing

---

## 📈 Test Coverage Summary

| Ticket | Category | Tests | Pass Rate |
|--------|----------|-------|-----------|
| 1 | Basic Functionality | 8 | 100% |
| 2 | Overall Schedule Report | 5 | 100% |
| 3 | Export Functionality | 5 | 100% |
| 4 | HR Permissions & Access Control | 14 | 100% |
| 5 | Date Range Filtering | 9 | 100% |
| 6 | Time Range Options | 6 | 100% |
| 7 | Project/Department Filters | 5 | 100% |
| 8 | Combined Filters & Formats | 7 | 100% |
| 9 | Task Count Validation | 4 | 100% |
| 10 | Date Range & Cross-Format | 4 | 100% |
| 11 | Project/Status/Dept Accuracy | 9 | 100% |
| 12 | Edge Cases | 3 | 100% |
| **TOTAL** | **12 Tickets** | **79** | **100%** |

---

## 🎯 Priority Breakdown

- **P0 (Critical):** 52 tests across 6 tickets
- **P1 (High):** 24 tests across 4 tickets
- **P2 (Medium):** 3 tests across 2 tickets

---

## 📝 Test Execution Notes

### Best Practices Implemented
1. **Page Object Model (POM):** All UI interactions encapsulated in `ReportsPage`
2. **Test Data Builders:** Fluent API for creating test data (`getCurrentMonthRange()`, etc.)
3. **Custom Assertions:** Reusable assertion helpers (`assertTaskCountsMatch()`, etc.)
4. **Download Verification:** Dedicated helpers for file download validation
5. **Authentication Fixtures:** Pre-authenticated user contexts for different roles

### Known Issues
- Some tests may fail if backend is not running
- Toast notifications are optional (won't fail tests if not detected)
- Tests require valid test user credentials in `.env` file

### Running Tests
```bash
# Run all report tests
npm run test:e2e:reports

# Run specific ticket tests
npx playwright test report-generation.spec.ts  # Tickets 1-3
npx playwright test report-permissions.spec.ts  # Ticket 4
npx playwright test report-filters.spec.ts     # Tickets 5-8
npx playwright test report-data-accuracy.spec.ts # Tickets 9-12
```

