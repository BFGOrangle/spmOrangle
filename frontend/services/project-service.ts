import { AuthenticatedApiClient } from './authenticated-api-client';
import { UnauthenticatedApiClient } from './unauthenticated-api-client';

export interface ProjectResponse {
  id: number;
  name: string;
  description?: string;
  ownerId: number;
  createdAt: string;
  updatedAt: string;
  taskCount: number;
  completedTaskCount: number;
}

export interface CreateProjectRequest {
  name: string;
  description?: string;
}

export interface SubtaskResponse {
  id: number;
  taskId: number;
  projectId: number;
  taskType: 'BUG' | 'FEATURE' | 'CHORE' | 'RESEARCH';
  title: string;
  details?: string;
  status: 'TODO' | 'IN_PROGRESS' | 'COMPLETED' | 'BLOCKED';
  createdAt: string;
  updatedAt?: string;
  createdBy: number;
  updatedBy?: number;
}

export interface TaskResponse {
  id: number;
  projectId?: number;
  ownerId: number;
  taskType: 'BUG' | 'FEATURE' | 'CHORE' | 'RESEARCH';
  title: string;
  description?: string;
  status: 'TODO' | 'IN_PROGRESS' | 'COMPLETED' | 'BLOCKED';
  tags?: string[];
  userHasEditAccess: boolean;
  createdAt: string;
  updatedAt?: string;
  createdBy: number;
  updatedBy?: number;
  subtasks?: SubtaskResponse[];
}

export interface CreateTaskRequest {
  projectId?: number;
  ownerId: number;
  title: string;
  description?: string;
  status?: 'TODO' | 'IN_PROGRESS' | 'COMPLETED' | 'BLOCKED';
  taskType: 'BUG' | 'FEATURE' | 'CHORE' | 'RESEARCH';
  tags?: string[];
  assignedUserIds?: number[];
}

export interface CreateSubtaskRequest {
  taskId: number;
  projectId: number;
  title: string;
  details?: string;
  status?: 'TODO' | 'IN_PROGRESS' | 'COMPLETED' | 'BLOCKED';
  taskType: 'BUG' | 'FEATURE' | 'CHORE' | 'RESEARCH';
}

export interface UpdateSubtaskRequest {
  title?: string;
  details?: string;
  status?: 'TODO' | 'IN_PROGRESS' | 'COMPLETED' | 'BLOCKED';
  taskType?: 'BUG' | 'FEATURE' | 'CHORE' | 'RESEARCH';
}

export class ProjectService {

    private authenticatedClient: AuthenticatedApiClient;
    private unauthenticatedClient: UnauthenticatedApiClient;
  
    constructor() {
      this.authenticatedClient = new AuthenticatedApiClient();
      this.unauthenticatedClient = new UnauthenticatedApiClient();
    }
  /**
   * Get all projects for a user
   */
  async getUserProjects(userId: number): Promise<ProjectResponse[]> {
    return this.authenticatedClient.get(`/api/projects`);
  }

  /**
   * Create a new project
   */
  async createProject(projectData: CreateProjectRequest, currentUserId: number): Promise<ProjectResponse> {
    return this.authenticatedClient.post(`/api/projects`, projectData);
  }

  /**
   * Delete a project
   */
  async deleteProject(projectId: number, currentUserId: number): Promise<void> {
    await this.authenticatedClient.delete(`/api/projects/${projectId}`);
  }

  /**
   * Get tasks for a specific project
   */
  async getProjectTasks(projectId: number): Promise<TaskResponse[]> {
    return this.authenticatedClient.get(`/api/tasks/project/${projectId}`);
  }

  /**
   * Get personal tasks (not associated with any project)
   */
  async getPersonalTasks(userId: number): Promise<TaskResponse[]> {
    return this.authenticatedClient.get(`/api/tasks/personal`);
  }

  /**
   * Get all tasks for a user (both personal and project tasks)
   */
  async getAllUserTasks(userId: number): Promise<TaskResponse[]> {
    return this.authenticatedClient.get(`/api/tasks/user`);
  }

  /**
   * Create a new task
   */
  async createTask(taskData: CreateTaskRequest): Promise<TaskResponse> {
    return this.authenticatedClient.post('/api/tasks', taskData);
  }

  /**
   * Get subtasks for a specific task
   */
  async getSubtasksByTaskId(taskId: number): Promise<SubtaskResponse[]> {
    return this.authenticatedClient.get(`/api/subtasks/task/${taskId}`);
  }

  /**
   * Create a new subtask
   */
  async createSubtask(subtaskData: CreateSubtaskRequest, currentUserId: number): Promise<SubtaskResponse> {
    return this.authenticatedClient.post(`/api/subtasks`, subtaskData);
  }

  /**
   * Update a subtask
   */
  async updateSubtask(subtaskId: number, updateData: UpdateSubtaskRequest, currentUserId: number): Promise<SubtaskResponse> {
    return this.authenticatedClient.put(`/api/subtasks/${subtaskId}`, updateData);
  }

  /**
   * Delete a subtask
   */
  async deleteSubtask(subtaskId: number, currentUserId: number): Promise<void> {
    return this.authenticatedClient.delete(`/api/subtasks/${subtaskId}`);
  }

  /**
   * Delete a task
   */
  async deleteTask(taskId: number, currentUserId: number): Promise<void> {
    await this.authenticatedClient.delete(`/api/tasks/${taskId}`);
  }
}

export const projectService = new ProjectService();
