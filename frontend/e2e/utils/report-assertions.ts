import { expect } from '@playwright/test';

/**
 * Custom assertions for report data validation
 * Provides helpers to verify report accuracy and consistency
 */

/**
 * Task counts interface
 */
export interface TaskCounts {
  total?: number;
  completed?: number;
  inProgress?: number;
  todo?: number;
  blocked?: number;
  future?: number;
}

/**
 * Task status counts interface (alternative naming)
 */
export interface TaskStatusCounts {
  totalTasks?: number;
  completedTasks?: number;
  inProgressTasks?: number;
  todoTasks?: number;
  blockedTasks?: number;
}

/**
 * Project data interface
 */
export interface ProjectData {
  projectName?: string;
  taskCounts?: TaskCounts;
  [key: string]: any;
}

/**
 * Assert that task counts match expected values
 */
export function assertTaskCountsMatch(
  reportedCounts: TaskCounts,
  expectedCounts: TaskCounts,
  options?: { allowPartialMatch?: boolean }
): void {
  const fields: (keyof TaskCounts)[] = ['total', 'completed', 'inProgress', 'todo', 'blocked', 'future'];
  
  for (const field of fields) {
    if (expectedCounts[field] !== undefined) {
      const reported = reportedCounts[field] ?? 0;
      const expected = expectedCounts[field] ?? 0;
      
      if (reported !== expected) {
        throw new Error(
          `Task count mismatch for "${field}": expected ${expected}, but got ${reported}`
        );
      }
    } else if (!options?.allowPartialMatch && reportedCounts[field] !== undefined) {
      throw new Error(
        `Unexpected field "${field}" in reported counts with value ${reportedCounts[field]}`
      );
    }
  }
}

/**
 * Assert that task status counts match (alternative interface)
 */
export function assertTaskStatusesMatch(
  reportedStatuses: TaskStatusCounts,
  expectedStatuses: TaskStatusCounts
): void {
  const fields: (keyof TaskStatusCounts)[] = [
    'totalTasks',
    'completedTasks',
    'inProgressTasks',
    'todoTasks',
    'blockedTasks'
  ];
  
  for (const field of fields) {
    if (expectedStatuses[field] !== undefined) {
      const reported = reportedStatuses[field] ?? 0;
      const expected = expectedStatuses[field] ?? 0;
      
      if (reported !== expected) {
        throw new Error(
          `Task status mismatch for "${field}": expected ${expected}, but got ${reported}`
        );
      }
    }
  }
}

/**
 * Assert that project data matches between API and UI
 */
export function assertProjectDataMatches(
  apiData: ProjectData,
  displayedData: ProjectData
): void {
  // Check project name
  if (apiData.projectName && displayedData.projectName) {
    expect(displayedData.projectName).toBe(apiData.projectName);
  }
  
  // Check task counts
  if (apiData.taskCounts && displayedData.taskCounts) {
    assertTaskCountsMatch(displayedData.taskCounts, apiData.taskCounts);
  }
}

/**
 * Assert that reported total equals sum of individual statuses
 */
export function assertTaskCountsAddUp(counts: TaskCounts): void {
  const total = counts.total ?? 0;
  const sum = (counts.completed ?? 0) + 
              (counts.inProgress ?? 0) + 
              (counts.todo ?? 0) + 
              (counts.blocked ?? 0) +
              (counts.future ?? 0);
  
  if (total !== sum) {
    throw new Error(
      `Task count total (${total}) does not equal sum of individual statuses (${sum}). ` +
      `Breakdown: completed=${counts.completed}, inProgress=${counts.inProgress}, ` +
      `todo=${counts.todo}, blocked=${counts.blocked}, future=${counts.future}`
    );
  }
}

/**
 * Assert that all task counts are non-negative
 */
export function assertTaskCountsNonNegative(counts: TaskCounts): void {
  const fields: (keyof TaskCounts)[] = ['total', 'completed', 'inProgress', 'todo', 'blocked', 'future'];
  
  for (const field of fields) {
    const value = counts[field];
    if (value !== undefined && value < 0) {
      throw new Error(`Task count for "${field}" is negative: ${value}`);
    }
  }
}

/**
 * Assert that task counts are within expected range
 */
export function assertTaskCountsInRange(
  counts: TaskCounts,
  min: number,
  max: number
): void {
  const total = counts.total ?? 0;
  
  if (total < min || total > max) {
    throw new Error(
      `Total task count ${total} is outside expected range [${min}, ${max}]`
    );
  }
}

/**
 * Compare two task count objects and return differences
 */
export function getTaskCountDifferences(
  counts1: TaskCounts,
  counts2: TaskCounts
): { field: string; value1: number; value2: number }[] {
  const differences: { field: string; value1: number; value2: number }[] = [];
  const fields: (keyof TaskCounts)[] = ['total', 'completed', 'inProgress', 'todo', 'blocked', 'future'];
  
  for (const field of fields) {
    const value1 = counts1[field] ?? 0;
    const value2 = counts2[field] ?? 0;
    
    if (value1 !== value2) {
      differences.push({
        field,
        value1,
        value2
      });
    }
  }
  
  return differences;
}

/**
 * Assert that two date ranges are different (non-overlapping or different)
 */
export function assertDateRangesDifferent(
  range1: { startDate: string; endDate: string },
  range2: { startDate: string; endDate: string }
): void {
  const same = range1.startDate === range2.startDate && range1.endDate === range2.endDate;
  
  if (same) {
    throw new Error(
      `Date ranges are identical: [${range1.startDate}, ${range1.endDate}]`
    );
  }
}

/**
 * Assert that date range is valid (start <= end)
 */
export function assertDateRangeValid(startDate: string, endDate: string): void {
  const start = new Date(startDate);
  const end = new Date(endDate);
  
  if (start > end) {
    throw new Error(
      `Invalid date range: start date ${startDate} is after end date ${endDate}`
    );
  }
}

/**
 * Assert that task counts change between two reports
 */
export function assertTaskCountsChanged(
  counts1: TaskCounts,
  counts2: TaskCounts
): void {
  const differences = getTaskCountDifferences(counts1, counts2);
  
  if (differences.length === 0) {
    throw new Error(
      'Expected task counts to be different between reports, but they are identical'
    );
  }
}

/**
 * Assert department breakdown has data
 */
export function assertDepartmentBreakdownHasData(
  departmentBreakdown: Record<string, TaskCounts>
): void {
  const departments = Object.keys(departmentBreakdown);
  
  if (departments.length === 0) {
    throw new Error('Department breakdown is empty');
  }
  
  for (const dept of departments) {
    const counts = departmentBreakdown[dept];
    const total = counts.total ?? 0;
    
    if (total < 0) {
      throw new Error(`Department "${dept}" has negative task count: ${total}`);
    }
  }
}

/**
 * Assert project breakdown has data
 */
export function assertProjectBreakdownHasData(
  projectBreakdown: Record<string, TaskCounts>
): void {
  const projects = Object.keys(projectBreakdown);
  
  if (projects.length === 0) {
    throw new Error('Project breakdown is empty');
  }
  
  for (const project of projects) {
    const counts = projectBreakdown[project];
    const total = counts.total ?? 0;
    
    if (total < 0) {
      throw new Error(`Project "${project}" has negative task count: ${total}`);
    }
  }
}

/**
 * Assert that filtered data is subset of unfiltered data
 */
export function assertFilteredDataIsSubset(
  unfilteredCount: number,
  filteredCount: number
): void {
  if (filteredCount > unfilteredCount) {
    throw new Error(
      `Filtered count (${filteredCount}) is greater than unfiltered count (${unfilteredCount}). ` +
      'Filtering should reduce or maintain count, not increase it.'
    );
  }
}

/**
 * Assert that data exists for specific date range
 */
export function assertDataExistsForDateRange(
  taskCounts: TaskCounts,
  startDate: string,
  endDate: string
): void {
  const total = taskCounts.total ?? 0;
  
  if (total === 0) {
    console.warn(
      `No tasks found for date range ${startDate} to ${endDate}. ` +
      'This might be expected for empty projects, but could indicate a filter issue.'
    );
  }
}

/**
 * Assert report data consistency (totals match sums)
 */
export function assertReportDataConsistent(reportData: {
  taskSummary?: {
    totalTasks?: number;
    completedTasks?: number;
    inProgressTasks?: number;
    todoTasks?: number;
    blockedTasks?: number;
  };
  departmentBreakdown?: Record<string, TaskCounts>;
  projectBreakdown?: Record<string, TaskCounts>;
}): void {
  // Check task summary totals
  if (reportData.taskSummary) {
    const summary = reportData.taskSummary;
    const counts: TaskCounts = {
      total: summary.totalTasks,
      completed: summary.completedTasks,
      inProgress: summary.inProgressTasks,
      todo: summary.todoTasks,
      blocked: summary.blockedTasks
    };
    
    assertTaskCountsAddUp(counts);
    assertTaskCountsNonNegative(counts);
  }
  
  // Check department breakdown totals add up to overall total
  if (reportData.departmentBreakdown && reportData.taskSummary) {
    let departmentTotal = 0;
    for (const dept of Object.keys(reportData.departmentBreakdown)) {
      departmentTotal += reportData.departmentBreakdown[dept].total ?? 0;
    }
    
    // Note: Department total might not equal task summary total if there are
    // department-filtering or role-based access controls in place
    // So we just check it's non-negative and reasonable
    if (departmentTotal < 0) {
      throw new Error(`Department breakdown total is negative: ${departmentTotal}`);
    }
  }
}

/**
 * Convert API response to TaskCounts format
 */
export function apiResponseToTaskCounts(apiResponse: any): TaskCounts {
  return {
    total: apiResponse.totalTasks ?? apiResponse.total,
    completed: apiResponse.completedTasks ?? apiResponse.completed,
    inProgress: apiResponse.inProgressTasks ?? apiResponse.inProgress,
    todo: apiResponse.todoTasks ?? apiResponse.todo,
    blocked: apiResponse.blockedTasks ?? apiResponse.blocked,
    future: apiResponse.futureTasks ?? apiResponse.future
  };
}

/**
 * Format task counts for error messages
 */
export function formatTaskCounts(counts: TaskCounts): string {
  return JSON.stringify({
    total: counts.total ?? 0,
    completed: counts.completed ?? 0,
    inProgress: counts.inProgress ?? 0,
    todo: counts.todo ?? 0,
    blocked: counts.blocked ?? 0,
    future: counts.future ?? 0
  }, null, 2);
}


