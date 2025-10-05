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
  assignedUserIds?: number[];
  userHasEditAccess: boolean;
  userHasDeleteAccess: boolean;
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

export interface UpdateTaskRequest {
  taskId: number;
  title?: string;
  description?: string;
  status?: 'TODO' | 'IN_PROGRESS' | 'COMPLETED' | 'BLOCKED';
  taskType?: 'BUG' | 'FEATURE' | 'CHORE' | 'RESEARCH';
  tags?: string[];
}

export interface CreateFileUpload{
  taskId: number;
  projectId: number;
  file: File;
}

export interface FileUploadResponse {
  id: number;
  taskId: number;
  projectId: number;
  fileUrl: string;
  createdAt: string;
  updatedAt?: string;
  createdBy: number;
  updatedBy?: number;
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
  async createProject(projectData: CreateProjectRequest): Promise<ProjectResponse> {
    return this.authenticatedClient.post(`/api/projects`, projectData);
  }

  /**
   * Delete a project
   */
  async deleteProject(projectId: number): Promise<void> {
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
   * Create a new task with specified owner (manager only)
   */
  async createTaskWithSpecifiedOwner(taskData: CreateTaskRequest): Promise<TaskResponse> {
    return this.authenticatedClient.post('/api/tasks/with-owner-id', taskData);
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
  async createSubtask(subtaskData: CreateSubtaskRequest): Promise<SubtaskResponse> {
    return this.authenticatedClient.post(`/api/subtasks`, subtaskData);
  }

  /**
   * Update a subtask
   */
  async updateSubtask(subtaskId: number, updateData: UpdateSubtaskRequest): Promise<SubtaskResponse> {
    return this.authenticatedClient.put(`/api/subtasks/${subtaskId}`, updateData);
  }

  /**
   * Delete a subtask
   */
  async deleteSubtask(subtaskId: number): Promise<void> {
    return this.authenticatedClient.delete(`/api/subtasks/${subtaskId}`);
  }

  /**
   * Update a task
   */
  async updateTask(updateData: UpdateTaskRequest): Promise<TaskResponse> {
    return this.authenticatedClient.put('/api/tasks', updateData);
  }

  /**
   * Delete a task
   */
  async deleteTask(taskId: number): Promise<void> {
    await this.authenticatedClient.delete(`/api/tasks/${taskId}`);
  }

  /**
   * Upload a file
   */
  async uploadFile(taskId: number, projectId: number, file: File, currentUserId: number): Promise<FileUploadResponse> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('taskId', taskId.toString());
    formData.append('projectId', projectId.toString());

    return this.authenticatedClient.postMultipart('/api/files/upload', formData);
  }
}

export const projectService = new ProjectService();
