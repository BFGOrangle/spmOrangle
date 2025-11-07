/**
 * Utility functions for calendar operations
 */

import { format, startOfWeek, endOfWeek, startOfMonth, endOfMonth, eachDayOfInterval, addDays, isToday, isSameMonth, isSameDay } from 'date-fns';
import { CalendarEvent, TaskToEventConverter, EventColorGenerator, BorderColorGenerator } from '../types/calendar';
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

// Generate border color to distinguish own tasks vs colleague tasks
export const generateBorderColor: BorderColorGenerator = (isOwnTask) => {
  // Green border for own tasks (assigned to me or owned by me)
  // Purple border for colleague tasks (visible through department access but not a collaborator)
  return isOwnTask ? 'border-green-500' : 'border-purple-400';
};

// Convert task to calendar event
export const taskToEvent: TaskToEventConverter = (task, project, currentUserId) => {
  const startDate = new Date(task.createdAt);

  // Use most relevant date for display filtering: dueDateTime > startDate > createdAt
  // This determines which day the task appears on in the calendar
  let displayDate: Date;
  if (task.dueDateTime) {
    displayDate = new Date(task.dueDateTime);
  } else if (task.startDate) {
    displayDate = new Date(task.startDate);  // For recurring tasks without due date
  } else {
    displayDate = new Date(task.createdAt);
  }

  // Check if task is overdue
  const taskIsOverdue = isTaskOverdue(task);

  // Determine if this is the user's own task (assigned to them or owned by them)
  let isOwnTask: boolean = true; // Default to true if no currentUserId provided (treat as own task)
  if (currentUserId !== undefined) {
    isOwnTask = task.ownerId === currentUserId ||
      (task.assignedUserIds !== undefined && task.assignedUserIds.includes(currentUserId));
  }

  const event = {
    id: task.id,
    title: task.title,
    description: task.description,
    startDate,
    endDate: displayDate,  // Used for filtering which day to show the task
    dueDate: task.dueDateTime ? new Date(task.dueDateTime) : undefined,  // Only set if task has actual due date
    taskType: task.taskType,
    status: task.status,
    projectId: task.projectId,
    projectName: project?.name,
    tags: task.tags,
    assignedUserIds: task.assignedUserIds,
    ownerId: task.ownerId, // DEPRECATED: kept for backward compatibility
    createdBy: task.createdBy,
    createdByName: task.createdByName,
    color: generateEventColor(task.taskType, task.status, taskIsOverdue),
    borderColor: generateBorderColor(isOwnTask),
  };

  // Log virtual instances for debugging
  if (task.isRecurring && !task.dueDateTime) {
    console.log('Converting virtual recurring task without due date:', {
      id: task.id,
      title: task.title,
      dueDateTime: task.dueDateTime,
      startDate: task.startDate,
      createdAt: task.createdAt,
      endDate: displayDate,
      eventDueDate: event.dueDate,
    });
  }

  return event;
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

// Filter events for week view - uses endDate for tasks without due dates
export const filterEventsForWeekView = (events: CalendarEvent[], startDate: Date, endDate: Date) => {
  return events.filter(event => {
    // Use dueDate if available, otherwise use endDate (which contains the occurrence date)
    const eventDate = event.dueDate || event.endDate;
    const start = new Date(startDate);
    const end = new Date(endDate);
    start.setHours(0, 0, 0, 0);
    end.setHours(23, 59, 59, 999);
    return eventDate >= start && eventDate <= end;
  });
};

// Filter events for month view - uses endDate for tasks without due dates
export const filterEventsForMonthView = (events: CalendarEvent[], startDate: Date, endDate: Date) => {
  const start = new Date(startDate);
  const end = new Date(endDate);
  start.setHours(0, 0, 0, 0);
  end.setHours(23, 59, 59, 999);

  console.log('=== filterEventsForMonthView ===');
  console.log('Date range:', start, 'to', end);
  console.log('Total events to filter:', events.length);

  const filtered = events.filter(event => {
    // Use dueDate if available, otherwise use endDate (which contains the occurrence date)
    const eventDate = event.dueDate || event.endDate;
    const passes = eventDate >= start && eventDate <= end;

    // Log recurring tasks without due dates
    if (!event.dueDate && event.title.includes('Test')) {
      console.log('Filtering task:', {
        title: event.title,
        dueDate: event.dueDate,
        endDate: event.endDate,
        eventDate,
        start,
        end,
        passes
      });
    }

    return passes;
  });

  console.log('Filtered events:', filtered.length);
  return filtered;
};

// Filter events by user assignment or created by user
export const filterEventsByUserAccess = (events: CalendarEvent[], currentUserId?: number) => {
  if (!currentUserId) {
    return events; // Return all events if no user ID provided
  }

  return events.filter(event => {
    // Check if user created the task
    if (event.createdBy === currentUserId) {
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

// Group events by date for week view - uses endDate for tasks without due dates
export const groupEventsByDateForWeekView = (events: CalendarEvent[], weekDays: Date[]) => {
  return events.reduce((groups, event) => {
    // Use dueDate if available, otherwise use endDate (which contains the occurrence date)
    const eventDate = event.dueDate || event.endDate;
    const dateKey = format(eventDate, 'yyyy-MM-dd');

    if (!groups[dateKey]) {
      groups[dateKey] = [];
    }
    groups[dateKey].push(event);
    return groups;
  }, {} as Record<string, CalendarEvent[]>);
};

// Group events by date for month view - uses endDate for tasks without due dates
export const groupEventsByDateForMonthView = (events: CalendarEvent[], monthDays: Date[]) => {
  console.log('=== groupEventsByDateForMonthView ===');
  console.log('Events to group:', events.length);

  const groups = events.reduce((groups, event) => {
    // Use dueDate if available, otherwise use endDate (which contains the occurrence date)
    const eventDate = event.dueDate || event.endDate;
    const dateKey = format(eventDate, 'yyyy-MM-dd');

    // Log recurring tasks without due dates
    if (!event.dueDate && event.title.includes('Test')) {
      console.log('Grouping task:', {
        title: event.title,
        dueDate: event.dueDate,
        endDate: event.endDate,
        eventDate,
        dateKey
      });
    }

    if (!groups[dateKey]) {
      groups[dateKey] = [];
    }
    groups[dateKey].push(event);
    return groups;
  }, {} as Record<string, CalendarEvent[]>);

  console.log('Grouped into', Object.keys(groups).length, 'days');
  return groups;
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
