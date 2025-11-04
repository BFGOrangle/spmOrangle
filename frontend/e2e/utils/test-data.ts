/**
 * Test data generators and factories
 * Use these to create consistent test data across tests
 */

export interface ReportFilters {
  department?: string;
  projectIds?: number[];
  timeRange: 'WEEKLY' | 'MONTHLY' | 'QUARTERLY' | 'YEARLY' | 'CUSTOM';
  format: 'JSON' | 'PDF' | 'CSV';
  startDate: string;
  endDate: string;
}

/**
 * Generate a date string in YYYY-MM-DD format
 */
export function formatDate(date: Date): string {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

/**
 * Get date range for current month
 */
export function getCurrentMonthRange(): { startDate: string; endDate: string } {
  const now = new Date();
  const startDate = new Date(now.getFullYear(), now.getMonth(), 1);
  const endDate = new Date(now.getFullYear(), now.getMonth() + 1, 0);
  return {
    startDate: formatDate(startDate),
    endDate: formatDate(endDate),
  };
}

/**
 * Get date range for current year
 */
export function getCurrentYearRange(): { startDate: string; endDate: string } {
  const now = new Date();
  const startDate = new Date(now.getFullYear(), 0, 1);
  const endDate = new Date(now.getFullYear(), 11, 31);
  return {
    startDate: formatDate(startDate),
    endDate: formatDate(endDate),
  };
}

/**
 * Get date range for last N days
 */
export function getLastNDaysRange(days: number): { startDate: string; endDate: string } {
  const endDate = new Date();
  const startDate = new Date();
  startDate.setDate(startDate.getDate() - days);
  return {
    startDate: formatDate(startDate),
    endDate: formatDate(endDate),
  };
}

/**
 * Get custom date range
 */
export function getCustomDateRange(
  startDate: string,
  endDate: string
): { startDate: string; endDate: string } {
  return { startDate, endDate };
}

/**
 * Default report filters
 */
export function getDefaultReportFilters(): ReportFilters {
  return {
    timeRange: 'MONTHLY',
    format: 'JSON',
    ...getCurrentMonthRange(),
  };
}

/**
 * Report filter builder for fluent API
 */
export class ReportFilterBuilder {
  private filters: Partial<ReportFilters>;

  constructor() {
    this.filters = getDefaultReportFilters();
  }

  withDepartment(department: string): this {
    this.filters.department = department;
    return this;
  }

  withProjects(projectIds: number[]): this {
    this.filters.projectIds = projectIds;
    return this;
  }

  withTimeRange(timeRange: ReportFilters['timeRange']): this {
    this.filters.timeRange = timeRange;
    return this;
  }

  withFormat(format: ReportFilters['format']): this {
    this.filters.format = format;
    return this;
  }

  withDateRange(startDate: string, endDate: string): this {
    this.filters.startDate = startDate;
    this.filters.endDate = endDate;
    return this;
  }

  withCurrentMonth(): this {
    const range = getCurrentMonthRange();
    this.filters.startDate = range.startDate;
    this.filters.endDate = range.endDate;
    return this;
  }

  withCurrentYear(): this {
    const range = getCurrentYearRange();
    this.filters.startDate = range.startDate;
    this.filters.endDate = range.endDate;
    return this;
  }

  withLastNDays(days: number): this {
    const range = getLastNDaysRange(days);
    this.filters.startDate = range.startDate;
    this.filters.endDate = range.endDate;
    return this;
  }

  build(): ReportFilters {
    return this.filters as ReportFilters;
  }
}

/**
 * Create a report filter builder
 */
export function createReportFilter(): ReportFilterBuilder {
  return new ReportFilterBuilder();
}

/**
 * Random string generator
 */
export function randomString(length: number = 10): string {
  const chars = 'abcdefghijklmnopqrstuvwxyz0123456789';
  let result = '';
  for (let i = 0; i < length; i++) {
    result += chars.charAt(Math.floor(Math.random() * chars.length));
  }
  return result;
}

/**
 * Random number between min and max
 */
export function randomInt(min: number, max: number): number {
  return Math.floor(Math.random() * (max - min + 1)) + min;
}

/**
 * Random email generator
 */
export function randomEmail(): string {
  return `test-${randomString()}@example.com`;
}

/**
 * Wait for a specific duration (use sparingly)
 */
export async function wait(ms: number): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

/**
 * Get date range for current quarter
 */
export function getCurrentQuarterRange(): { startDate: string; endDate: string } {
  const now = new Date();
  const currentMonth = now.getMonth();
  const quarterStartMonth = Math.floor(currentMonth / 3) * 3;
  
  const startDate = new Date(now.getFullYear(), quarterStartMonth, 1);
  const endDate = new Date(now.getFullYear(), quarterStartMonth + 3, 0);
  
  return {
    startDate: formatDate(startDate),
    endDate: formatDate(endDate),
  };
}

/**
 * Get date range for current week
 */
export function getCurrentWeekRange(): { startDate: string; endDate: string } {
  const now = new Date();
  const dayOfWeek = now.getDay();
  const diff = now.getDate() - dayOfWeek; // Monday as start of week
  
  const startDate = new Date(now);
  startDate.setDate(diff);
  
  const endDate = new Date(startDate);
  endDate.setDate(startDate.getDate() + 6);
  
  return {
    startDate: formatDate(startDate),
    endDate: formatDate(endDate),
  };
}

/**
 * Get date range for previous month
 */
export function getPreviousMonthRange(): { startDate: string; endDate: string } {
  const now = new Date();
  const startDate = new Date(now.getFullYear(), now.getMonth() - 1, 1);
  const endDate = new Date(now.getFullYear(), now.getMonth(), 0);
  
  return {
    startDate: formatDate(startDate),
    endDate: formatDate(endDate),
  };
}

/**
 * Get non-overlapping date ranges for testing filter changes
 */
export function getNonOverlappingDateRanges(): {
  range1: { startDate: string; endDate: string };
  range2: { startDate: string; endDate: string };
} {
  const now = new Date();
  
  // Range 1: Last month
  const range1StartDate = new Date(now.getFullYear(), now.getMonth() - 1, 1);
  const range1EndDate = new Date(now.getFullYear(), now.getMonth(), 0);
  
  // Range 2: Two months ago
  const range2StartDate = new Date(now.getFullYear(), now.getMonth() - 2, 1);
  const range2EndDate = new Date(now.getFullYear(), now.getMonth() - 1, 0);
  
  return {
    range1: {
      startDate: formatDate(range1StartDate),
      endDate: formatDate(range1EndDate),
    },
    range2: {
      startDate: formatDate(range2StartDate),
      endDate: formatDate(range2EndDate),
    },
  };
}

/**
 * Get date range with specific project context (for testing project filtering)
 */
export function getProjectDateRange(projectStartDate?: Date): { startDate: string; endDate: string } {
  const start = projectStartDate || new Date(new Date().getFullYear(), 0, 1);
  const end = new Date();
  
  return {
    startDate: formatDate(start),
    endDate: formatDate(end),
  };
}

/**
 * Extended report filter builder with project-specific methods
 */
export class ExtendedReportFilterBuilder extends ReportFilterBuilder {
  withProject(projectId: number): this {
    return this.withProjects([projectId]);
  }
  
  withMultipleProjects(...projectIds: number[]): this {
    return this.withProjects(projectIds);
  }
  
  withCurrentWeek(): this {
    const range = getCurrentWeekRange();
    this.withDateRange(range.startDate, range.endDate);
    return this;
  }
  
  withCurrentQuarter(): this {
    const range = getCurrentQuarterRange();
    this.withDateRange(range.startDate, range.endDate);
    return this;
  }
  
  withPreviousMonth(): this {
    const range = getPreviousMonthRange();
    this.withDateRange(range.startDate, range.endDate);
    return this;
  }
  
  forJsonDisplay(): this {
    return this.withFormat('JSON');
  }
  
  forPdfExport(): this {
    return this.withFormat('PDF');
  }
  
  forCsvExport(): this {
    return this.withFormat('CSV');
  }
}

/**
 * Create an extended report filter builder with project-specific helpers
 */
export function createProjectReportFilter(): ExtendedReportFilterBuilder {
  return new ExtendedReportFilterBuilder();
}

/**
 * Create report filters for different export formats
 */
export function createReportFiltersForAllFormats(baseOptions?: {
  department?: string;
  projectIds?: number[];
  startDate?: string;
  endDate?: string;
}): { json: ReportFilters; pdf: ReportFilters; csv: ReportFilters } {
  const dateRange = baseOptions?.startDate && baseOptions?.endDate
    ? { startDate: baseOptions.startDate, endDate: baseOptions.endDate }
    : getCurrentMonthRange();
  
  const base = {
    department: baseOptions?.department,
    projectIds: baseOptions?.projectIds,
    timeRange: 'MONTHLY' as const,
    ...dateRange,
  };
  
  return {
    json: { ...base, format: 'JSON' },
    pdf: { ...base, format: 'PDF' },
    csv: { ...base, format: 'CSV' },
  };
}

/**
 * Generate test report filename based on filters
 */
export function generateExpectedReportFilename(filters: Partial<ReportFilters>): string {
  const format = (filters.format || 'json').toLowerCase();
  const dateStr = filters.startDate ? filters.startDate.replace(/-/g, '') : 'default';
  
  return `report-${dateStr}.${format}`;
}

/**
 * Create filters for testing date range effects
 */
export function createDateRangeTestFilters(): {
  currentMonth: ReportFilters;
  previousMonth: ReportFilters;
  currentYear: ReportFilters;
  lastWeek: ReportFilters;
} {
  return {
    currentMonth: {
      ...getCurrentMonthRange(),
      timeRange: 'MONTHLY',
      format: 'JSON',
    },
    previousMonth: {
      ...getPreviousMonthRange(),
      timeRange: 'MONTHLY',
      format: 'JSON',
    },
    currentYear: {
      ...getCurrentYearRange(),
      timeRange: 'YEARLY',
      format: 'JSON',
    },
    lastWeek: {
      ...getLastNDaysRange(7),
      timeRange: 'WEEKLY',
      format: 'JSON',
    },
  };
}
