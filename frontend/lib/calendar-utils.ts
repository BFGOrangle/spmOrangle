/**
 * Utility functions for calendar operations
 */

import { format, startOfWeek, endOfWeek, startOfMonth, endOfMonth, eachDayOfInterval, addDays, isToday, isSameMonth, isSameDay } from 'date-fns';
import { CalendarEvent, TaskToEventConverter, EventColorGenerator } from '../types/calendar';
import { TaskResponseDto } from '../types/project';
import { ProjectResponse } from '../services/project-service';

/**
 * Check if a task is overdue
 * A task is overdue if it has a due date/time that has passed and the task is not completed
 */
export const isTaskOverdue = (task: { dueDateTime?: string; status: string }): boolean => {
  if (!task.dueDateTime) {
    return false; // No due date means not overdue
  }
  
  if (task.status === 'COMPLETED') {
    return false; // Completed tasks are never overdue
  }
  
  const dueDate = new Date(task.dueDateTime);
  const now = new Date();
  
  return dueDate < now;
};

// Convert task to calendar event
export const taskToEvent: TaskToEventConverter = (task, project) => {
  const startDate = new Date(task.createdAt);
  const endDate = task.dueDateTime ? new Date(task.dueDateTime) : new Date(task.createdAt);
  const taskIsOverdue = isTaskOverdue(task);
  
  return {
    id: task.id,
    title: task.title,
    description: task.description,
    startDate,
    endDate,
    dueDate: task.dueDateTime ? new Date(task.dueDateTime) : undefined,
    taskType: task.taskType,
    status: task.status,
    projectId: task.projectId,
    projectName: project?.name,
    tags: task.tags,
    assignedUserIds: task.assignedUserIds,
    ownerId: task.ownerId,
    color: generateEventColor(task.taskType, task.status, taskIsOverdue),
  };
};

// Generate color for events based on task type and status
export const generateEventColor: EventColorGenerator = (taskType, status, isOverdue?: boolean) => {
  // Overdue tasks always get bright red, regardless of type
  if (isOverdue) {
    return 'bg-red-600 opacity-100';
  }
  
  const taskTypeColors = {
    BUG: 'bg-red-500',
    FEATURE: 'bg-blue-500',
    CHORE: 'bg-yellow-500',
    RESEARCH: 'bg-purple-500',
  };

  const statusOpacity = {
    TODO: 'opacity-70',
    IN_PROGRESS: 'opacity-90',
    COMPLETED: 'opacity-50',
    BLOCKED: 'opacity-40',
  };

  return `${taskTypeColors[taskType as keyof typeof taskTypeColors]} ${statusOpacity[status as keyof typeof statusOpacity]}`;
};

// Get calendar weeks for month view
export const getCalendarWeeks = (date: Date) => {
  const monthStart = startOfMonth(date);
  const monthEnd = endOfMonth(date);
  const calendarStart = startOfWeek(monthStart, { weekStartsOn: 0 });
  const calendarEnd = endOfWeek(monthEnd, { weekStartsOn: 0 });

  const days = eachDayOfInterval({ start: calendarStart, end: calendarEnd });
  const weeks = [];
  
  for (let i = 0; i < days.length; i += 7) {
    weeks.push(days.slice(i, i + 7));
  }
  
  return weeks;
};

// Get days for week view
export const getWeekDays = (date: Date) => {
  const weekStart = startOfWeek(date, { weekStartsOn: 0 });
  return eachDayOfInterval({
    start: weekStart,
    end: addDays(weekStart, 6),
  });
};

// Filter events by date range
export const filterEventsByDateRange = (events: CalendarEvent[], startDate: Date, endDate: Date) => {
  return events.filter(event => {
    const eventDate = event.dueDate || event.endDate;
    const start = new Date(startDate);
    const end = new Date(endDate);
    
    // Set time to start of day for startDate and end of day for endDate for better comparison
    start.setHours(0, 0, 0, 0);
    end.setHours(23, 59, 59, 999);
    
    return eventDate >= start && eventDate <= end;
  });
};

// Filter events for week view - shows tasks with due dates in the week OR active tasks without due dates
export const filterEventsForWeekView = (events: CalendarEvent[], startDate: Date, endDate: Date) => {
  return events.filter(event => {
    // If task has a due date, check if it's in the week range
    if (event.dueDate) {
      const start = new Date(startDate);
      const end = new Date(endDate);
      start.setHours(0, 0, 0, 0);
      end.setHours(23, 59, 59, 999);
      return event.dueDate >= start && event.dueDate <= end;
    }
    
    // If task has no due date, show it if it's not completed
    return event.status !== 'COMPLETED';
  });
};

// Filter events for month view - shows tasks with due dates in the month OR active tasks without due dates
export const filterEventsForMonthView = (events: CalendarEvent[], startDate: Date, endDate: Date) => {
  return events.filter(event => {
    // If task has a due date, check if it's in the month range
    if (event.dueDate) {
      const start = new Date(startDate);
      const end = new Date(endDate);
      start.setHours(0, 0, 0, 0);
      end.setHours(23, 59, 59, 999);
      return event.dueDate >= start && event.dueDate <= end;
    }
    
    // If task has no due date, show it if it's not completed
    return event.status !== 'COMPLETED';
  });
};

// Filter events by user assignment or ownership
export const filterEventsByUserAccess = (events: CalendarEvent[], currentUserId?: number) => {
  if (!currentUserId) {
    return events; // Return all events if no user ID provided
  }
  
  return events.filter(event => {
    // Check if user is the owner
    if (event.ownerId === currentUserId) {
      return true;
    }
    
    // Check if user is assigned to the task
    if (event.assignedUserIds && event.assignedUserIds.includes(currentUserId)) {
      return true;
    }
    
    return false;
  });
};

// Group events by date
export const groupEventsByDate = (events: CalendarEvent[]) => {
  return events.reduce((groups, event) => {
    const dateKey = format(event.dueDate || event.endDate, 'yyyy-MM-dd');
    if (!groups[dateKey]) {
      groups[dateKey] = [];
    }
    groups[dateKey].push(event);
    return groups;
  }, {} as Record<string, CalendarEvent[]>);
};

// Group events by date for week view - tasks without due dates go to today
export const groupEventsByDateForWeekView = (events: CalendarEvent[], weekDays: Date[]) => {
  const today = new Date();
  const todayKey = format(today, 'yyyy-MM-dd');
  
  return events.reduce((groups, event) => {
    let dateKey: string;
    
    if (event.dueDate) {
      // Use due date if available
      dateKey = format(event.dueDate, 'yyyy-MM-dd');
    } else {
      // For tasks without due dates, put them on today if today is in the week range
      const isThisWeek = weekDays.some(day => format(day, 'yyyy-MM-dd') === todayKey);
      if (isThisWeek) {
        dateKey = todayKey;
      } else {
        // If today is not in this week, put them on the first day of the week
        dateKey = format(weekDays[0], 'yyyy-MM-dd');
      }
    }
    
    if (!groups[dateKey]) {
      groups[dateKey] = [];
    }
    groups[dateKey].push(event);
    return groups;
  }, {} as Record<string, CalendarEvent[]>);
};

// Group events by date for month view - tasks without due dates go to today
export const groupEventsByDateForMonthView = (events: CalendarEvent[], monthDays: Date[]) => {
  const today = new Date();
  const todayKey = format(today, 'yyyy-MM-dd');
  
  return events.reduce((groups, event) => {
    let dateKey: string;
    
    if (event.dueDate) {
      // Use due date if available
      dateKey = format(event.dueDate, 'yyyy-MM-dd');
    } else {
      // For tasks without due dates, put them on today if today is in the month range
      const isThisMonth = monthDays.some(day => format(day, 'yyyy-MM-dd') === todayKey);
      if (isThisMonth) {
        dateKey = todayKey;
      } else {
        // If today is not in this month, put them on the first day of the month
        dateKey = format(monthDays[0], 'yyyy-MM-dd');
      }
    }
    
    if (!groups[dateKey]) {
      groups[dateKey] = [];
    }
    groups[dateKey].push(event);
    return groups;
  }, {} as Record<string, CalendarEvent[]>);
};

// Format date for display
export const formatDisplayDate = (date: Date, view: string) => {
  switch (view) {
    case 'day':
      return format(date, 'EEEE, MMMM d, yyyy');
    case 'week':
      const weekStart = startOfWeek(date, { weekStartsOn: 0 });
      const weekEnd = endOfWeek(date, { weekStartsOn: 0 });
      return `${format(weekStart, 'MMM d')} - ${format(weekEnd, 'MMM d, yyyy')}`;
    case 'month':
      return format(date, 'MMMM yyyy');
    case 'timeline':
      return format(date, 'MMMM yyyy');
    default:
      return format(date, 'MMMM d, yyyy');
  }
};

// Check if date is in current view range
export const isDateInView = (date: Date, currentDate: Date, view: string) => {
  switch (view) {
    case 'day':
      return isSameDay(date, currentDate);
    case 'week':
      const weekStart = startOfWeek(currentDate, { weekStartsOn: 0 });
      const weekEnd = endOfWeek(currentDate, { weekStartsOn: 0 });
      return date >= weekStart && date <= weekEnd;
    case 'month':
      return isSameMonth(date, currentDate);
    default:
      return false;
  }
};
