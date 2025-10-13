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
  ownerId: number;
  taskType: TaskType;
  title: string;
  description?: string;
  status: TaskStatus;
  tags?: string[];
  assignedUserIds?: number[];
  userHasEditAccess: boolean;
  userHasDeleteAccess: boolean;
  createdAt: string;
  updatedAt?: string;
  createdBy: number;
  updatedBy?: number;
  subtasks?: SubtaskResponseDto[];
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