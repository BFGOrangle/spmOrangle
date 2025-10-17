/**
 * Report Service
 * Handles all report-related API calls
 */

import { AuthenticatedApiClient } from './authenticated-api-client';
import {
  TaskSummaryReportDto,
  TimeAnalyticsReportDto,
  TimeSeriesDataPoint,
  ReportFilterDto,
  ComprehensiveReportDto,
  ProjectOptionDto,
} from '@/types/report';

export class ReportService extends AuthenticatedApiClient {
  constructor() {
    super();
  }

  /**
   * Get task summary report
   */
  async getTaskSummaryReport(filters: ReportFilterDto): Promise<TaskSummaryReportDto> {
    const params = this.buildQueryParams(filters);
    const url = `/api/reports/task-summary${params}`;
    
    return this.get<TaskSummaryReportDto>(url);
  }

  /**
   * Get time analytics report
   */
  async getTimeAnalyticsReport(filters: ReportFilterDto): Promise<TimeAnalyticsReportDto> {
    const params = this.buildQueryParams(filters);
    const url = `/api/reports/time-analytics${params}`;
    
    return this.get<TimeAnalyticsReportDto>(url);
  }

  /**
   * Get available departments for filtering
   */
  async getAvailableDepartments(): Promise<string[]> {
    const url = `/api/reports/departments`;
    return this.get<string[]>(url);
  }

  /**
   * Get available projects, optionally filtered by department
   */
  async getAvailableProjects(department?: string): Promise<ProjectOptionDto[]> {
    const params = department ? `?department=${encodeURIComponent(department)}` : '';
    const url = `/api/reports/projects${params}`;
    
    const response = await this.get<{ projects: ProjectOptionDto[] }>(url);
    return response.projects;
  }

  /**
   * Generate comprehensive report (can return PDF, CSV, or JSON)
   * For PDF/CSV, this will trigger a download
   */
  async generateReport(filters: ReportFilterDto): Promise<ComprehensiveReportDto | Blob> {
    const url = `/api/reports/generate`;
    
    // If format is PDF or CSV, we need to handle blob response
    if (filters.format === 'PDF' || filters.format === 'CSV') {
      return this.postForBlob(url, filters);
    }
    
    // Otherwise return JSON
    return this.post<ComprehensiveReportDto>(url, filters);
  }

  /**
   * Helper method to download a blob as a file
   */
  downloadBlob(blob: Blob, filename: string): void {
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
  }

  /**
   * Generate filename for report download
   */
  generateReportFilename(filters: ReportFilterDto): string {
    const timestamp = new Date().toISOString().split('T')[0];
    const format = filters.format?.toLowerCase() || 'report';
    const dept = filters.department ? `-${filters.department}` : '';
    return `report${dept}-${timestamp}.${format}`;
  }

  /**
   * Build query parameters from filters
   */
  private buildQueryParams(filters: ReportFilterDto): string {
    const params = new URLSearchParams();
    
    if (filters.department) {
      params.append('department', filters.department);
    }
    
    if (filters.projectIds && filters.projectIds.length > 0) {
      filters.projectIds.forEach(id => params.append('projectIds', id.toString()));
    }
    
    if (filters.startDate) {
      params.append('startDate', filters.startDate);
    }
    
    if (filters.endDate) {
      params.append('endDate', filters.endDate);
    }
    
    if (filters.timeRange) {
      params.append('timeRange', filters.timeRange);
    }
    
    const queryString = params.toString();
    return queryString ? `?${queryString}` : '';
  }

  /**
   * POST request that returns a Blob (for file downloads)
   */
  private async postForBlob(url: string, body: any): Promise<Blob> {
    const BACKEND_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080';
    const fullUrl = `${BACKEND_BASE_URL}${url}`;
    
    const { createAuthenticatedRequestConfig } = await import('@/lib/auth-utils');
    const config = await createAuthenticatedRequestConfig('POST', body);

    const response = await fetch(fullUrl, {
      ...config,
      headers: {
        ...config.headers,
        'Content-Type': 'application/json',
      },
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    return response.blob();
  }
}

// Export singleton instance
export const reportService = new ReportService();
