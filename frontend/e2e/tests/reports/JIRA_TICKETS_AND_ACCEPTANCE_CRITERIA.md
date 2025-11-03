# Reports Feature - Jira Tickets & Acceptance Criteria

**Total Tests:** 94
**Test Files:** 5
**Total Tickets:** 6
**Last Updated:** November 3, 2025

---

## TICKET 1: Staff Breakdown Table

### User Story
As a reporting user,
I want a staff-level table of tasks and logged hours,
so that I can see workload distribution across my team.

### Acceptance Criteria

**AC 1.1: Display Staff Breakdown Table with All Columns**
```
Given I am an HR user and staff exist under the selected scope
When I generate a report with JSON format
Then the Staff Breakdown section should be visible
And the table should display columns: Staff Name, Department, To Do, In Progress, Completed, Blocked, Logged Hours
```

**AC 1.2: Show Task Counts for Each Staff Member**
```
Given I have generated a report
When I view the Staff Breakdown table
Then each staff row should show counts for To-Do, In-Progress, Completed, and Blocked tasks
And all counts should be non-negative integers
And the logged hours should be formatted to 2 decimal places
```

**AC 1.3: Filter Staff by Department**
```
Given I select a specific department filter
When I generate a report
Then only staff members from that department should be listed
And the Department column should show the selected department for all rows
```

**AC 1.4: Filter Staff by Project**
```
Given I select a specific project filter
When I generate a report
Then only staff members assigned to that project should be listed
And task counts should reflect only tasks from that project
```

**AC 1.5: Handle Empty Staff Data Gracefully**
```
Given I have selected filters that match no staff members
When I generate a report
Then the Staff Breakdown section should display: "No staff data available for the selected filters."
And no table should be displayed
```

**AC 1.6: Update Staff List When Filters Change**
```
Given I have generated a report with specific filters
When I change the department or project filter and regenerate
Then the staff list should update to show only relevant staff
And previously displayed staff not in the new filter scope should be removed
```

---

## TICKET 2: Summary Stats Widget

### User Story
As an HR user,
I want reports to accurately represent project breakdowns, task statuses, and department scoping,
so that I can drill down into specific areas with confidence.

### Acceptance Criteria

**AC 2.1: Load Reports Page Successfully**
```
Given I am an authenticated HR user
When I navigate to the reports page
Then the page should load with all filter controls visible
And the generate button should be enabled
```

**AC 2.2: Generate Basic Report with Default Filters**
```
Given I am on the reports page
When I generate a report using default filters (current month, JSON format)
Then the report should display with task summary visible
And task counts should be displayed
```

**AC 2.3: Display Task Status Breakdown**
```
Given I have generated a report
When I view the task summary section
Then all task statuses should be visible (TODO, IN_PROGRESS, COMPLETED, BLOCKED)
```

**AC 2.4: Show Project-Level Task Breakdown**
```
Given I generate a report without project filter
When I view the report data
Then the report should show a breakdown of tasks by project
```

**AC 2.5: Filter by Specific Project Correctly**
```
Given I select a specific project
When I generate the report
Then only tasks from that project should be included in the counts
And other projects should be excluded
```

**AC 2.6: Accurately Report TODO Status Tasks**
```
Given I generate a report
When I check the TODO count
Then it should match the actual number of tasks with TODO status
```

**AC 2.7: Accurately Report IN_PROGRESS Status Tasks**
```
Given I generate a report
When I check the IN_PROGRESS count
Then it should match the actual number of tasks with IN_PROGRESS status
```

**AC 2.8: Accurately Report COMPLETED Status Tasks**
```
Given I generate a report
When I check the COMPLETED count
Then it should match the actual number of tasks with COMPLETED status
```

**AC 2.9: Accurately Report BLOCKED Status Tasks**
```
Given I generate a report
When I check the BLOCKED count
Then it should match the actual number of tasks with BLOCKED status
```

**AC 2.10: Include All Task Statuses in Total Count**
```
Given I generate a report
When I check the total tasks count
Then it should equal the sum of all status counts (TODO + IN_PROGRESS + COMPLETED + BLOCKED)
And no tasks should be missing from the total
```

**AC 2.11: Show Data for All Departments (HR User)**
```
Given I am an HR user with no department filter
When I generate the report
Then the report should include data from all departments
```

**AC 2.12: Maintain Data Consistency When Switching Filters**
```
Given I generate a report with filter A
When I switch to filter B and back to filter A
Then the data should be the same as the first time (consistent)
And no data discrepancies should occur
```

**AC 2.13: Display Non-Negative Task Counts**
```
Given I generate a report
When I view the task summary
Then all task counts should be ≥ 0 (no negative numbers)
```

**AC 2.14: Task Status Counts Add Up Correctly**
```
Given I generate a report
When I check the task summary
Then completed + in-progress + todo + blocked should equal total tasks
```

**AC 2.15: Display Consistent Data on Multiple Generations**
```
Given I generate a report
When I regenerate the same report (same filters) immediately
Then the task counts should be identical (no random fluctuations)
```

---

## TICKET 3: Charts Breakdown (Manual Testing)

### User Story
As a reporting user,
I want to see charts of task progress and logged time,
so that I can quickly visualize workload.

### Acceptance Criteria

**AC 3.1: Display Pie Chart for Task Status**
```
Given a report is generated
When I view charts
Then a pie chart shows task status percentages (to-do, in-progress, completed, blocked)
```

**AC 3.2: Display Bar Chart for Logged Time**
```
Given logged time data is available
When I view charts
Then a bar chart shows hours by project or department
```

**Note:** Charts are tested manually - no E2E automation required.

---

## TICKET 4: Report Export Functionality

### User Story
As an HR user,
I want to export reports as PDF or CSV files,
so that I can share them with stakeholders or analyze data in Excel.

### Acceptance Criteria

**AC 4.1: Export Report as PDF Successfully**
```
Given I have set report filters
When I select PDF format and click generate
Then a PDF file should be downloaded
And a success toast message should appear
```

**AC 4.2: Export Report as CSV Successfully**
```
Given I have set report filters
When I select CSV format and click generate
Then a CSV file should be downloaded
And a success toast message should appear
```

**AC 4.3: PDF Filename Follows Pattern**
```
Given I generate a PDF report
When the file downloads
Then its filename should follow the pattern: report_[timestamp].pdf
```

**AC 4.4: CSV Filename Follows Pattern**
```
Given I generate a CSV report
When the file downloads
Then its filename should follow the pattern: report_[timestamp].csv
```

**AC 4.5: Handle PDF Export for Large Date Ranges**
```
Given I select a large date range (e.g., entire year)
When I generate a PDF report
Then the PDF should download successfully without timeout errors
And the file should contain all relevant data
```

**AC 4.6: Produce Same Data in JSON and CSV Formats**
```
Given I generate a report with specific filters
When I export as JSON and then as CSV (same filters)
Then the task counts should be identical in both formats
```

**AC 4.7: Produce Consistent Data in PDF Format**
```
Given I generate a report with specific filters
When I export as JSON and then as PDF (same filters)
Then the data should be consistent (PDF contains same counts as JSON)
```

---

## TICKET 5: Reporting Access Control

### User Story
As an HR user,
I want exclusive access to generate reports for any department and project,
while Managers and Staff are restricted from report access.

### Acceptance Criteria

**AC 5.1: HR User Can Access Reports Page**
```
Given I am authenticated as an HR user
When I navigate to /reports
Then the page should load successfully
And all filter controls should be visible
```

**AC 5.2: HR User Can Generate Reports**
```
Given I am on the reports page as an HR user
When I set filters and click generate
Then the report should be generated successfully
```

**AC 5.3: HR User Can Access All Departments Data**
```
Given I am an HR user
When I generate a report without department filter
Then I should see data from all departments combined
```

**AC 5.4: HR User Can Generate Reports for Any Project**
```
Given I am an HR user
When I select any project from any department
Then the report should generate successfully
```

**AC 5.5: HR User Can Export in All Formats**
```
Given I am an HR user
When I select JSON, PDF, or CSV format
Then the report should generate/download successfully in any format
```

**AC 5.6: HR Role Can Perform All Report Operations**
```
Given I am authenticated as an HR user
When I perform any report operation (access, generate, export, filter)
Then all report actions should succeed across all departments and projects
```

**AC 5.7: Manager User Is Redirected from Reports Page**
```
Given I am authenticated as a Manager user
When I navigate to /reports
Then I should see an access denied message
And I should be redirected to the dashboard
```

**AC 5.8: Manager User Cannot Access Reports Directly**
```
Given I am a Manager user
When I try to navigate directly to /reports
Then I should be redirected to the dashboard
And I should not see the reports page content
```

**AC 5.9: Staff User Is Redirected from Reports Page**
```
Given I am authenticated as a Staff user
When I navigate to /reports
Then I should see an access denied message
And I should be redirected to the dashboard
```

**AC 5.10: Staff User Cannot Access Reports Directly**
```
Given I am a Staff user
When I try to navigate directly to /reports
Then I should be redirected to the dashboard
And I should not see the reports page content
```

---

## TICKET 6: Filtering for Reports

### User Story
As an HR user,
I want to filter reports by different criteria,
so that I can analyze tasks within specific time periods, projects, and departments.

### Acceptance Criteria

### Date Range Filtering

**AC 6.1: Accept Valid Date Range (Start Before End)**
```
Given I am on the reports page
When I set a start date before the end date
Then the generate button should be enabled
And the report should generate successfully when clicked
```

**AC 6.2: Require Start Date**
```
Given I am on the reports page
When I leave the start date empty
Then the form should indicate that start date is required
And I should see a validation error or toast message
```

**AC 6.3: Require End Date**
```
Given I am on the reports page
When I leave the end date empty
Then the form should indicate that end date is required
And I should see a validation error or toast message
```

**AC 6.4: Allow Same Start and End Date**
```
Given I am on the reports page
When I set the same date for start and end (single day)
Then the generate button should be enabled
And the report should generate for that single day
```

**AC 6.5: Filter Tasks by Date Ranges (Month, Year, Custom)**
```
Given I select month, year, or custom ranges
When I generate the report
Then only tasks within those ranges should be displayed
```

### Time Series Data

**AC 6.6: Generate Weekly Report**
```
Given I select WEEKLY as the time range
When I generate the report
Then the report should cover the current week
And data should be split by week
```

**AC 6.7: Generate Monthly Report**
```
Given I select MONTHLY as the time range
When I generate the report
Then the report should cover the current month
And data should be split by month
```

**AC 6.8: Generate Quarterly Report**
```
Given I select QUARTERLY as the time range
When I generate the report
Then the report should cover the current quarter
And data should be split by quarter
```

**AC 6.9: Generate Yearly Report**
```
Given I select YEARLY as the time range
When I generate the report
Then the report should cover the current year
And data should be split by year
```

**AC 6.10: Generate Custom Time Range Report**
```
Given I select CUSTOM as the time range
When I set specific start and end dates
Then the report should cover my custom date range
```

**AC 6.11: Switch Between Time Range Options**
```
Given I have selected one time range
When I switch to a different time range option
Then the date fields should update to reflect the new time range
```

### Project & Department Filtering

**AC 6.12: Generate Report for All Projects by Default**
```
Given I am on the reports page
And I don't select a specific project or select "All Projects"
When I generate the report
Then the report should include data from all projects
```

**AC 6.13: Filter by Specific Project**
```
Given I select a specific project from the dropdown
When I generate the report
Then only tasks from that project should be included
And the report should show project-specific data
```

**AC 6.14: Generate Report for All Departments (HR User)**
```
Given I am an HR user
And I select "All Departments"
When I generate the report
Then the report should include data from all departments
```

**AC 6.15: Filter by Specific Department**
```
Given I select a specific department
When I generate the report
Then only tasks/projects from that department should be displayed
And staff from other departments should be excluded
```

### Combined Filters

**AC 6.16: Apply Multiple Filters Simultaneously**
```
Given I am on the reports page
When I select department, project, date range, and format together
Then the report should apply all filters correctly
```

**AC 6.17: Update Report When Filters Change**
```
Given I have generated a report
When I change one or more filters and regenerate
Then the new report should reflect the updated filters
```

**AC 6.18: Maintain Filter Selections After Generating Report**
```
Given I have set multiple filters
When I generate a report
Then the filter selections should remain in the UI (not reset)
```

### Edge Cases

**AC 6.19: Handle Empty Result Sets Gracefully**
```
Given I set filters that match no tasks
When I generate the report
Then the report should display with all counts at 0 (no error)
And appropriate empty state messages should be shown
```

**AC 6.20: Handle Large Date Ranges**
```
Given I set a very large date range (e.g., 10 years)
When I generate the report
Then the report should generate successfully without timeout
```

**AC 6.21: Handle Single Day Date Range**
```
Given I set start and end date to the same day
When I generate the report
Then the report should show tasks from that single day only
```

---

## Summary

| Ticket # | Ticket Name | Total ACs | Test Priority |
|----------|-------------|-----------|---------------|
| 1 | Staff Breakdown Table | 6 | P0 (Critical) |
| 2 | Summary Stats Widget | 15 | P0 (Critical) |
| 3 | Charts Breakdown | 2 | Manual Testing |
| 4 | Report Export | 7 | P0 (Critical) |
| 5 | Access Control | 10 | P0 (Critical) |
| 6 | Filtering for Reports | 21 | P0 (Critical) |
| **TOTAL** | **6 Tickets** | **61** | - |

---

## Copy-Paste Instructions for Jira

1. **Create a new Story** in Jira
2. **Copy the User Story** section into the Description field
3. **Add each Acceptance Criteria** as separate items in Jira's AC section
4. **Use the GWT format blocks** directly (already formatted)
5. **Add labels:** `reports`, `e2e-tested`, `frontend`
6. **Set priority** based on table above

---

## Story Points Estimation

- **Ticket 1** (Staff Breakdown): 5 points
- **Ticket 2** (Summary Stats): 8 points
- **Ticket 3** (Charts): 3 points (manual only)
- **Ticket 4** (Export): 5 points
- **Ticket 5** (Access Control): 5 points
- **Ticket 6** (Filtering): 8 points
