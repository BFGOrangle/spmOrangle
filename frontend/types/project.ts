/**
 * TypeScript types for project-related data structures
 * These match the backend DTOs from the Java Spring Boot application
 */

// Project-related types
export interface ProjectResponseDto {
  id: number;
  name: string;
  description: string;
  ownerId: number;
  createdAt: string;
  updatedAt: string;
  taskCount: number;
  completedTaskCount: number;
  // Permission metadata fields
  isOwner: boolean;
  isRelated: boolean;
  departmentName?: string;
}

export interface CreateProjectDto {
  name: string;
  description?: string;
}

// Task-related types
export type TaskType = "BUG" | "FEATURE" | "CHORE" | "RESEARCH";
export type TaskStatus = "TODO" | "IN_PROGRESS" | "COMPLETED" | "BLOCKED";

export interface SubtaskResponseDto {
  id: number;
  taskId: number;
  projectId: number;
  taskType: TaskType;
  title: string;
  details?: string;
  status: TaskStatus;
  createdAt: string;
  updatedAt?: string;
  createdBy: number;
  updatedBy?: number;
}

export interface TaskResponseDto {
  id: number;
  projectId?: number;
  projectName?: string;
  // DEPRECATED: ownerId, ownerName, ownerDepartment will be removed in future phase - use createdBy/createdByName instead
  ownerId?: number;
  ownerName?: string;
  ownerDepartment?: string;
  taskType: TaskType;
  title: string;
  description?: string;
  status: TaskStatus;
  tags?: string[];
  assignedUserIds?: number[];
  // Backend-calculated permission flags - ALWAYS use these instead of calculating permissions in frontend
  userHasEditAccess: boolean;  // Can current user edit this task?
  userHasDeleteAccess: boolean; // Can current user delete this task?
  createdAt: string;
  updatedAt?: string;
  createdBy: number;
  createdByName?: string;      // Name of user who created the task
  updatedBy?: number;
  subtasks?: SubtaskResponseDto[];
  dueDateTime?: string;
  priority?: number;            // Priority from 1 (lowest) to 10 (highest)
  isRecurring?: boolean;
  recurrenceRuleStr?: string;
  startDate?: string;
  endDate?: string;
}

// UI-specific interfaces for project and task management
export interface ProjectCardProps {
  project: ProjectResponseDto;
  onProjectClick: (projectId: number) => void;
}

export interface TaskListProps {
  projectId: number;
  tasks: TaskResponseDto[];
  isLoading?: boolean;
  onBackToProjects: () => void;
}

export interface ProjectsListProps {
  projects: ProjectResponseDto[];
  isLoading?: boolean;
  onProjectSelect: (projectId: number) => void;
}