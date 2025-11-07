/**
 * TypeScript types for calendar-related data structures
 */

import { TaskResponseDto, ProjectResponseDto } from './project';
import { ProjectResponse } from '../services/project-service';

// Calendar view types
export type CalendarView = 'day' | 'week' | 'month' | 'timeline';

// Calendar event interface based on task data
export interface CalendarEvent {
  id: number;
  title: string;
  description?: string;
  startDate: Date;
  endDate: Date;
  dueDate?: Date;
  taskType: 'BUG' | 'FEATURE' | 'CHORE' | 'RESEARCH';
  status: 'TODO' | 'IN_PROGRESS' | 'COMPLETED' | 'BLOCKED';
  projectId?: number;
  projectName?: string;
  tags?: string[];
  assignedUserIds?: number[];
  // DEPRECATED: Use createdBy instead
  ownerId?: number;
  createdBy: number;
  createdByName?: string;
  color?: string;
  borderColor?: string; // Border color to distinguish own tasks vs colleague tasks
  // Search-related properties
  isHighlighted?: boolean;
  matchedFields?: string[];
}

// Calendar filter options
export interface CalendarFilters {
  projectId?: number;
  status?: ('TODO' | 'IN_PROGRESS' | 'COMPLETED' | 'BLOCKED')[];
  taskType?: ('BUG' | 'FEATURE' | 'CHORE' | 'RESEARCH')[];
  assignedUserId?: number;
}

// Calendar navigation
export interface CalendarNavigation {
  currentDate: Date;
  view: CalendarView;
}

// Calendar view props
export interface CalendarViewProps {
  events: CalendarEvent[];
  currentDate: Date;
  searchKeyword?: string;
  onEventClick: (event: CalendarEvent) => void;
  onDateClick: (date: Date) => void;
}

// Timeline specific props
export interface TimelineViewProps extends CalendarViewProps {
  startDate: Date;
  endDate: Date;
  timeScale: 'days' | 'weeks' | 'months';
}

// Calendar controls props
export interface CalendarControlsProps {
  currentView: CalendarView;
  currentDate: Date;
  projects: ProjectResponse[];
  selectedProjectId?: number;
  selectedTaskType?: string;
  searchKeyword?: string;
  searchResultsCount?: number;
  onViewChange: (view: CalendarView) => void;
  onDateChange: (date: Date) => void;
  onProjectFilter: (projectId?: number) => void;
  onTaskTypeChange?: (taskType: string) => void;
  onSearchChange?: (keyword: string) => void;
  onToday: () => void;
}

// Calendar page state
export interface CalendarPageState {
  view: CalendarView;
  currentDate: Date;
  filters: CalendarFilters;
  events: CalendarEvent[];
  projects: ProjectResponse[];
  isLoading: boolean;
}

// Utility function types
export type TaskToEventConverter = (task: TaskResponseDto, project?: ProjectResponse, currentUserId?: number) => CalendarEvent;
export type EventColorGenerator = (taskType: string, status: string, isOverdue?: boolean) => string;
export type BorderColorGenerator = (isOwnTask: boolean) => string;
