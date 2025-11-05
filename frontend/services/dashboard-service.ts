import { AuthenticatedApiClient } from "./authenticated-api-client";

export interface DashboardMetrics {
  activeProjects: number;
  totalTasks: number;
  completedTasks: number;
  blockedTasks: number;
  highPriorityTasks: number;
  completionRate: number;
}

export interface ProjectHealthCard {
  projectId: number;
  projectName: string;
  status: string;
  completionPercentage: number;
  totalTasks: number;
  completedTasks: number;
  blockedTasks: number;
}

export interface TaskDashboardItem {
  id: number;
  title: string;
  status: string;
  taskType?: string;
  priority?: number;
  ownerId: number;
  ownerName: string;
  ownerDepartment?: string;
  projectId?: number;
  projectName?: string;
  dueDateTime?: string;
  updatedAt?: string;
  createdAt: string;
  assigneeIds: number[];
}

export interface TeamLoadEntry {
  userId: number;
  fullName: string;
  department?: string;
  taskCount: number;
  blockedTaskCount: number;
}

export interface DepartmentDashboardResponse {
  department?: string;
  includedDepartments: string[];
  metrics: DashboardMetrics;
  projects: ProjectHealthCard[];
  upcomingCommitments: TaskDashboardItem[];
  priorityQueue: TaskDashboardItem[];
  teamLoad: TeamLoadEntry[];
}

class DashboardService {
  private client: AuthenticatedApiClient;

  constructor() {
    this.client = new AuthenticatedApiClient();
  }

  async getDepartmentDashboard(): Promise<DepartmentDashboardResponse> {
    return this.client.get<DepartmentDashboardResponse>("/api/dashboard/department");
  }
}

export const dashboardService = new DashboardService();
