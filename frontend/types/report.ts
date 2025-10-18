/**
 * Report types matching backend DTOs
 */

export type TimeRange = 'WEEKLY' | 'MONTHLY' | 'QUARTERLY' | 'YEARLY' | 'CUSTOM';
export type ReportFormat = 'PDF' | 'CSV' | 'JSON';

export interface ReportFilterDto {
  department?: string;
  projectIds?: number[];
  startDate?: string;
  endDate?: string;
  timeRange?: TimeRange;
  format?: ReportFormat;
}

export interface TaskSummaryReportDto {
  totalTasks: number;
  completedTasks: number;
  inProgressTasks: number;
  blockedTasks: number;
  todoTasks: number;
  completedPercentage: number;
  inProgressPercentage: number;
  todoPercentage: number;
  blockedPercentage: number;
  departmentBreakdown?: {
    [key: string]: TaskStatusCounts;
  };
  projectBreakdown?: {
    [key: string]: TaskStatusCounts;
  };
}

export interface TaskStatusCounts {
  total: number;
  completed: number;
  inProgress: number;
  todo: number;
  blocked: number;
}

export interface TimeAnalyticsReportDto {
  totalHours: number;
  hoursByDepartment?: {
    [key: string]: number;
  };
  hoursByProject?: {
    [key: string]: number;
  };
  projectDetails?: {
    [key: string]: ProjectTimeDetails;
  };
}

export interface ProjectTimeDetails {
  projectName: string;
  department: string;
  totalHours: number;
  completedTasks: number;
  inProgressTasks: number;
  averageHoursPerTask: number;
}

export interface TimeSeriesDataPoint {
  period: string;
  periodLabel: string;
  startDate: string;
  endDate: string;
  taskSummary: TaskSummaryReportDto;
  timeAnalytics: TimeAnalyticsReportDto;
}

export interface ComprehensiveReportDto {
  taskSummary: TaskSummaryReportDto;
  timeAnalytics: TimeAnalyticsReportDto;
  timeSeriesData?: TimeSeriesDataPoint[];
  filters: ReportFilterDto;
  generatedAt: string;
}

export interface DepartmentDto {
  name: string;
}

export interface ProjectOptionDto {
  id: number;
  name: string;
}
