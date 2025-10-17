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
  completionRate: number;
  tasksByType: {
    BUG: number;
    FEATURE: number;
    CHORE: number;
    RESEARCH: number;
  };
  tasksByPriority?: {
    HIGH: number;
    MEDIUM: number;
    LOW: number;
  };
}

export interface TimeAnalyticsReportDto {
  totalTimeSpent: number;
  averageTimePerTask: number;
  timeByProject: {
    projectId: number;
    projectName: string;
    timeSpent: number;
  }[];
  timeByDepartment: {
    department: string;
    timeSpent: number;
  }[];
  timeByUser?: {
    userId: number;
    userName: string;
    timeSpent: number;
  }[];
}

export interface TimeSeriesDataPoint {
  date: string;
  tasksCompleted: number;
  tasksCreated: number;
  timeSpent: number;
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
