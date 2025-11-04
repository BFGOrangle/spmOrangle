/**
 * Task Management Test Data Types
 * Defines interfaces and enums for task management test data
 */

export enum TaskStatus {
  TODO = 'To Do',
  IN_PROGRESS = 'In Progress',
  COMPLETED = 'Completed',
  BLOCKED = 'Blocked'
}

export enum TaskPriority {
  LOW = 1,
  MEDIUM = 5,
  HIGH = 10
}

export enum CollaboratorRole {
  VIEWER = 'Viewer',
  EDITOR = 'Editor'
}

export enum CommentFilter {
  ALL = 'all',
  UNRESOLVED = 'unresolved',
  BY_COMMENTER = 'by_commenter'
}

export enum RecurrenceType {
  DAILY = 'daily',
  WEEKLY = 'weekly',
  MONTHLY = 'monthly'
}

export enum RecurrenceEndType {
  DATE = 'date',
  OCCURRENCES = 'occurrences',
  NEVER = 'never'
}

export interface TaskData {
  title: string;
  description?: string;
  priority: TaskPriority;
  dueDate?: string;
  status: TaskStatus;
  assignees?: string[];
  tags?: string[];
  projectId?: string;
  recurrence?: RecurrenceData;
}

export interface SubtaskData {
  title: string;
  notes?: string;
  dueDate?: string;
  status: TaskStatus;
  parentTaskId?: string;
}

export interface RecurrenceData {
  type: RecurrenceType;
  endType: RecurrenceEndType;
  endValue?: string | number;
}

export interface CommentData {
  content: string;
  mentions?: string[];
  parentCommentId?: string;
  taskId?: string;
}

export interface ProjectData {
  name: string;
  description?: string;
  departmentId?: string;
  managerId?: string;
}

export interface CollaboratorData {
  userId: string;
  role: CollaboratorRole;
  taskId?: string;
}

export interface TaskSummary {
  id: string;
  title: string;
  status: TaskStatus;
  priority: TaskPriority;
  dueDate?: string;
  assigneeCount: number;
  subtaskCount?: number;
}

export interface StatusCounts {
  todo: number;
  inProgress: number;
  completed: number;
  blocked: number;
}

export interface TaskFilters {
  status?: TaskStatus[];
  priority?: TaskPriority[];
  dueDate?: {
    start?: string;
    end?: string;
  };
  projectId?: string;
  assigneeId?: string;
}

export interface SortOptions {
  field: 'dueDate' | 'status' | 'priority' | 'lastUpdated' | 'title';
  direction: 'asc' | 'desc';
}

export interface ActivityLogEntry {
  type: string;
  timestamp: string;
  editor: string;
  details: string;
  source?: string;
}