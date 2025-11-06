As a manager / director / HR
I want a departmental dashboard (including sub-departments)
So that I can monitor project health, progress, and team workload

Acceptance Criteria

Scenario: Department & sub-department scope

Given I manage Department D with sub-departments D1 and D2

When I open the Dashboard

Then metrics aggregate tasks and projects from D, D1, and D2

And projects from other departments are excluded

Scenario: Active projects across my scope

Given projects owned by D/D1/D2 exist

When I view “Active projects”

Then the count includes projects with any task assigned to staff in D/D1/D2

Scenario: Departmental task completion rate

Given tasks exist for staff in D/D1/D2 with mixed statuses

When I view “Task completion”

Then the % reflects completed vs total tasks across D/D1/D2

Scenario: Upcoming commitments across my team

Given tasks for my department are due in the next 14 days

When I view “Upcoming commitments”

Then I see all such tasks for staff in D/D1/D2

And I do not see tasks from other departments

Scenario: Project health by department 1

Given multiple projects exist within my scope

When I view “Project health”

Then each project card shows status and completion % for that each project

Scenario: Project health by department 2

Given multiple projects exist within my scope

When I view “Project health”

Then each project card shows number of tasks blocked for each project

Scenario: Team load distribution

Given staff in D/D1/D2 have differing numbers of tasks

When I view “Team load”

Then I see workload per staff member within D/D1/D2

Scenario: Priority queue for my department

Given high-priority or blocked tasks exist in my scope

When I view “Priority queue”

Then I see those tasks across D/D1/D2

Scenario: Cross-department data is restricted

Given I am a Manager of Department D

When I try to view a project/task from Department X (not D/D1/D2)

Then it is not visible on the dashboard

And direct navigation is blocked

Scenario: Drill-down to project detail

Given I am viewing a project card in “Project health”

When I click the card

Then I am taken to the project page of the selected project

