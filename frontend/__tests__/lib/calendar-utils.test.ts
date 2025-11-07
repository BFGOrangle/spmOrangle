import {
  isTaskOverdue,
  taskToEvent,
  generateEventColor,
  getCalendarWeeks,
  getWeekDays,
  filterEventsByDateRange,
  filterEventsForWeekView,
  filterEventsForMonthView,
  filterEventsByUserAccess,
  groupEventsByDate,
  groupEventsByDateForWeekView,
  groupEventsByDateForMonthView,
  formatDisplayDate,
  isDateInView,
} from '../../lib/calendar-utils';
import { CalendarEvent } from '../../types/calendar';
import { TaskResponseDto } from '../../types/project';
import { ProjectResponse } from '../../services/project-service';

describe('calendar-utils', () => {
  describe('isTaskOverdue', () => {
    it('returns false when task has no due date', () => {
      const task = { status: 'TODO' };
      expect(isTaskOverdue(task as any)).toBe(false);
    });

    it('returns false when task is completed', () => {
      const task = {
        dueDateTime: new Date('2020-01-01').toISOString(),
        status: 'COMPLETED',
      };
      expect(isTaskOverdue(task)).toBe(false);
    });

    it('returns true when task is overdue', () => {
      const task = {
        dueDateTime: new Date('2020-01-01').toISOString(),
        status: 'TODO',
      };
      expect(isTaskOverdue(task)).toBe(true);
    });

    it('returns false when task due date is in the future', () => {
      const futureDate = new Date();
      futureDate.setDate(futureDate.getDate() + 10);
      const task = {
        dueDateTime: futureDate.toISOString(),
        status: 'TODO',
      };
      expect(isTaskOverdue(task)).toBe(false);
    });

    it('returns true for in-progress task that is overdue', () => {
      const task = {
        dueDateTime: new Date('2020-01-01').toISOString(),
        status: 'IN_PROGRESS',
      };
      expect(isTaskOverdue(task)).toBe(true);
    });
  });

  describe('taskToEvent', () => {
    const baseTask: TaskResponseDto = {
      id: 1,
      title: 'Test Task',
      description: 'Test Description',
      createdAt: '2024-01-01T00:00:00Z',
      taskType: 'FEATURE',
      status: 'TODO',
      projectId: 1,
      tags: ['tag1'],
      assignedUserIds: [1, 2],
      ownerId: 1,
      userHasEditAccess: true,
      userHasDeleteAccess: true,
      createdBy: 1,
    };

    const project: ProjectResponse = {
      id: 1,
      name: 'Test Project',
      description: 'Test Description',
      createdAt: '2024-01-01T00:00:00Z',
      updatedAt: '2024-01-01T00:00:00Z',
      ownerId: 1,
      taskCount: 0,
      completedTaskCount: 0,
      isOwner: true,
      isRelated: false,
    };

    it('converts task with due date to calendar event', () => {
      const task = {
        ...baseTask,
        dueDateTime: '2024-01-15T10:00:00Z',
      };

      const event = taskToEvent(task, project);

      expect(event.id).toBe(1);
      expect(event.title).toBe('Test Task');
      expect(event.dueDate).toEqual(new Date('2024-01-15T10:00:00Z'));
      expect(event.endDate).toEqual(new Date('2024-01-15T10:00:00Z'));
      expect(event.projectName).toBe('Test Project');
    });

    it('converts task without due date using createdAt for display', () => {
      const event = taskToEvent(baseTask, project);

      expect(event.dueDate).toBeUndefined();
      expect(event.endDate).toEqual(new Date('2024-01-01T00:00:00Z'));
    });

    it('converts recurring task without due date using startDate', () => {
      const task = {
        ...baseTask,
        isRecurring: true,
        startDate: '2024-02-01T00:00:00Z',
      };

      const event = taskToEvent(task, project);

      expect(event.dueDate).toBeUndefined();
      expect(event.endDate).toEqual(new Date('2024-02-01T00:00:00Z'));
    });

    it('prioritizes dueDateTime over startDate for display date', () => {
      const task = {
        ...baseTask,
        dueDateTime: '2024-01-15T10:00:00Z',
        startDate: '2024-02-01T00:00:00Z',
      };

      const event = taskToEvent(task, project);

      expect(event.endDate).toEqual(new Date('2024-01-15T10:00:00Z'));
    });

    it('includes all task properties in the event', () => {
      const event = taskToEvent(baseTask, project);

      expect(event.taskType).toBe('FEATURE');
      expect(event.status).toBe('TODO');
      expect(event.projectId).toBe(1);
      expect(event.tags).toEqual(['tag1']);
      expect(event.assignedUserIds).toEqual([1, 2]);
      expect(event.ownerId).toBe(1);
    });
  });

  describe('generateEventColor', () => {
    it('returns red color for overdue tasks', () => {
      const color = generateEventColor('FEATURE', 'TODO', true);
      expect(color).toBe('bg-red-600 opacity-100');
    });

    it('returns correct color for bug task type', () => {
      const color = generateEventColor('BUG', 'TODO');
      expect(color).toContain('bg-red-500');
    });

    it('returns correct color for feature task type', () => {
      const color = generateEventColor('FEATURE', 'TODO');
      expect(color).toContain('bg-blue-500');
    });

    it('returns correct color for chore task type', () => {
      const color = generateEventColor('CHORE', 'TODO');
      expect(color).toContain('bg-yellow-500');
    });

    it('returns correct color for research task type', () => {
      const color = generateEventColor('RESEARCH', 'TODO');
      expect(color).toContain('bg-purple-500');
    });

    it('applies correct opacity for TODO status', () => {
      const color = generateEventColor('FEATURE', 'TODO');
      expect(color).toContain('opacity-70');
    });

    it('applies correct opacity for IN_PROGRESS status', () => {
      const color = generateEventColor('FEATURE', 'IN_PROGRESS');
      expect(color).toContain('opacity-90');
    });

    it('applies correct opacity for COMPLETED status', () => {
      const color = generateEventColor('FEATURE', 'COMPLETED');
      expect(color).toContain('opacity-50');
    });

    it('applies correct opacity for BLOCKED status', () => {
      const color = generateEventColor('FEATURE', 'BLOCKED');
      expect(color).toContain('opacity-40');
    });
  });

  describe('getCalendarWeeks', () => {
    it('returns correct number of weeks for a month', () => {
      const date = new Date('2024-01-15');
      const weeks = getCalendarWeeks(date);

      expect(weeks.length).toBeGreaterThanOrEqual(4);
      expect(weeks.length).toBeLessThanOrEqual(6);
    });

    it('each week has 7 days', () => {
      const date = new Date('2024-01-15');
      const weeks = getCalendarWeeks(date);

      weeks.forEach(week => {
        expect(week).toHaveLength(7);
      });
    });

    it('starts on Sunday', () => {
      const date = new Date('2024-01-15');
      const weeks = getCalendarWeeks(date);

      expect(weeks[0][0].getDay()).toBe(0); // Sunday
    });

    it('covers entire month including adjacent days', () => {
      const date = new Date('2024-02-15');
      const weeks = getCalendarWeeks(date);
      const allDays = weeks.flat();

      // Should include days from previous/next months
      expect(allDays.length).toBeGreaterThanOrEqual(28);
    });
  });

  describe('getWeekDays', () => {
    it('returns 7 days', () => {
      const date = new Date('2024-01-15');
      const days = getWeekDays(date);

      expect(days).toHaveLength(7);
    });

    it('starts on Sunday', () => {
      const date = new Date('2024-01-15');
      const days = getWeekDays(date);

      expect(days[0].getDay()).toBe(0); // Sunday
    });

    it('ends on Saturday', () => {
      const date = new Date('2024-01-15');
      const days = getWeekDays(date);

      expect(days[6].getDay()).toBe(6); // Saturday
    });
  });

  describe('filterEventsByDateRange', () => {
    const createEvent = (date: Date): CalendarEvent => ({
      id: 1,
      title: 'Test Event',
      description: 'Test',
      startDate: date,
      endDate: date,
      dueDate: date,
      taskType: 'FEATURE',
      status: 'TODO',
      projectId: 1,
      projectName: 'Test',
      tags: [],
      assignedUserIds: [],
      ownerId: 1,
      createdBy: 1,
      color: 'bg-blue-500',
    });

    it('includes events within date range', () => {
      const startDate = new Date('2024-01-01');
      const endDate = new Date('2024-01-31');
      const event = createEvent(new Date('2024-01-15'));

      const filtered = filterEventsByDateRange([event], startDate, endDate);

      expect(filtered).toHaveLength(1);
    });

    it('excludes events outside date range', () => {
      const startDate = new Date('2024-01-01');
      const endDate = new Date('2024-01-31');
      const event = createEvent(new Date('2024-02-15'));

      const filtered = filterEventsByDateRange([event], startDate, endDate);

      expect(filtered).toHaveLength(0);
    });

    it('includes events on start date boundary', () => {
      const startDate = new Date('2024-01-01');
      const endDate = new Date('2024-01-31');
      const event = createEvent(new Date('2024-01-01'));

      const filtered = filterEventsByDateRange([event], startDate, endDate);

      expect(filtered).toHaveLength(1);
    });

    it('includes events on end date boundary', () => {
      const startDate = new Date('2024-01-01');
      const endDate = new Date('2024-01-31');
      const event = createEvent(new Date('2024-01-31T12:00:00Z'));

      const filtered = filterEventsByDateRange([event], startDate, endDate);

      expect(filtered).toHaveLength(1);
    });
  });

  describe('filterEventsForWeekView', () => {
    const createEvent = (dueDate?: Date, endDate?: Date): CalendarEvent => ({
      id: 1,
      title: 'Test Event',
      description: 'Test',
      startDate: new Date(),
      endDate: endDate || new Date(),
      dueDate,
      taskType: 'FEATURE',
      status: 'TODO',
      projectId: 1,
      projectName: 'Test',
      tags: [],
      assignedUserIds: [],
      ownerId: 1,
      createdBy: 1,
      color: 'bg-blue-500',
    });

    it('filters events with due dates within week range', () => {
      const startDate = new Date('2024-01-01');
      const endDate = new Date('2024-01-07');
      const event = createEvent(new Date('2024-01-05'));

      const filtered = filterEventsForWeekView([event], startDate, endDate);

      expect(filtered).toHaveLength(1);
    });

    it('uses endDate for events without due date', () => {
      const startDate = new Date('2024-01-01');
      const endDate = new Date('2024-01-07');
      const event = createEvent(undefined, new Date('2024-01-05'));

      const filtered = filterEventsForWeekView([event], startDate, endDate);

      expect(filtered).toHaveLength(1);
    });

    it('filters out events outside week range', () => {
      const startDate = new Date('2024-01-01');
      const endDate = new Date('2024-01-07');
      const event = createEvent(new Date('2024-01-15'));

      const filtered = filterEventsForWeekView([event], startDate, endDate);

      expect(filtered).toHaveLength(0);
    });
  });

  describe('filterEventsForMonthView', () => {
    const createEvent = (dueDate?: Date, endDate?: Date, title = 'Test Event'): CalendarEvent => ({
      id: 1,
      title,
      description: 'Test',
      startDate: new Date(),
      endDate: endDate || new Date(),
      dueDate,
      taskType: 'FEATURE',
      status: 'TODO',
      projectId: 1,
      projectName: 'Test',
      tags: [],
      assignedUserIds: [],
      ownerId: 1,
      createdBy: 1,
      color: 'bg-blue-500',
    });

    it('filters events with due dates within month range', () => {
      const startDate = new Date('2024-01-01');
      const endDate = new Date('2024-01-31');
      const event = createEvent(new Date('2024-01-15'));

      const filtered = filterEventsForMonthView([event], startDate, endDate);

      expect(filtered).toHaveLength(1);
    });

    it('uses endDate for recurring events without due date', () => {
      const startDate = new Date('2024-01-01');
      const endDate = new Date('2024-01-31');
      const event = createEvent(undefined, new Date('2024-01-15'));

      const filtered = filterEventsForMonthView([event], startDate, endDate);

      expect(filtered).toHaveLength(1);
    });

    it('filters out events outside month range', () => {
      const startDate = new Date('2024-01-01');
      const endDate = new Date('2024-01-31');
      const event = createEvent(new Date('2024-02-15'));

      const filtered = filterEventsForMonthView([event], startDate, endDate);

      expect(filtered).toHaveLength(0);
    });
  });

  describe('filterEventsByUserAccess', () => {
    const createEvent = (ownerId?: number, assignedUserIds?: number[]): CalendarEvent => ({
      id: 1,
      title: 'Test Event',
      description: 'Test',
      startDate: new Date(),
      endDate: new Date(),
      taskType: 'FEATURE',
      status: 'TODO',
      projectId: 1,
      projectName: 'Test',
      tags: [],
      assignedUserIds: assignedUserIds || [],
      ownerId: ownerId || 0,
      createdBy: ownerId || 1,
      color: 'bg-blue-500',
    });

    it('returns all events when no user ID provided', () => {
      const events = [createEvent(1), createEvent(2)];
      const filtered = filterEventsByUserAccess(events);

      expect(filtered).toHaveLength(2);
    });

    it('excludes events where user is only owner but not assigned', () => {
      const event = createEvent(5); // owner but not in assignedUserIds
      const filtered = filterEventsByUserAccess([event], 5);

      expect(filtered).toHaveLength(0);
    });

    it('includes events assigned to user', () => {
      const event = createEvent(1, [3, 5, 7]);
      const filtered = filterEventsByUserAccess([event], 5);

      expect(filtered).toHaveLength(1);
    });

    it('excludes events user has no access to', () => {
      const event = createEvent(1, [2, 3]);
      const filtered = filterEventsByUserAccess([event], 5);

      expect(filtered).toHaveLength(0);
    });

    it('only includes events user is assigned to', () => {
      const events = [
        createEvent(5), // owned but not assigned - excluded
        createEvent(1, [5]), // assigned - included
        createEvent(1, [2, 3]), // no access - excluded
      ];
      const filtered = filterEventsByUserAccess(events, 5);

      expect(filtered).toHaveLength(1);
    });
  });

  describe('groupEventsByDate', () => {
    const createEvent = (date: Date, id: number): CalendarEvent => ({
      id,
      title: `Event ${id}`,
      description: 'Test',
      startDate: date,
      endDate: date,
      dueDate: date,
      taskType: 'FEATURE',
      status: 'TODO',
      projectId: 1,
      projectName: 'Test',
      tags: [],
      assignedUserIds: [],
      ownerId: 1,
      createdBy: 1,
      color: 'bg-blue-500',
    });

    it('groups events by date', () => {
      const date1 = new Date('2024-01-15');
      const date2 = new Date('2024-01-16');

      const events = [
        createEvent(date1, 1),
        createEvent(date1, 2),
        createEvent(date2, 3),
      ];

      const grouped = groupEventsByDate(events);

      expect(grouped['2024-01-15']).toHaveLength(2);
      expect(grouped['2024-01-16']).toHaveLength(1);
    });

    it('uses endDate when dueDate is not available', () => {
      const date = new Date('2024-01-15');
      const event: CalendarEvent = {
        id: 1,
        title: 'Event',
        description: 'Test',
        startDate: date,
        endDate: date,
        taskType: 'FEATURE',
        status: 'TODO',
        createdBy: 1,
        projectId: 1,
        projectName: 'Test',
        tags: [],
        assignedUserIds: [],
        ownerId: 1,
        color: 'bg-blue-500',
      };

      const grouped = groupEventsByDate([event]);

      expect(grouped['2024-01-15']).toHaveLength(1);
    });
  });

  describe('groupEventsByDateForWeekView', () => {
    const weekDays = [
      new Date('2024-01-01'),
      new Date('2024-01-02'),
      new Date('2024-01-03'),
      new Date('2024-01-04'),
      new Date('2024-01-05'),
      new Date('2024-01-06'),
      new Date('2024-01-07'),
    ];

    const createEvent = (dueDate: Date | undefined, endDate: Date, id: number): CalendarEvent => ({
      id,
      title: `Event ${id}`,
      description: 'Test',
      startDate: new Date(),
      endDate,
      dueDate,
      taskType: 'FEATURE',
      status: 'TODO',
      projectId: 1,
      projectName: 'Test',
      tags: [],
      assignedUserIds: [],
      ownerId: 1,
      createdBy: 1,
      color: 'bg-blue-500',
    });

    it('groups events by date using dueDate', () => {
      const events = [
        createEvent(new Date('2024-01-01'), new Date('2024-01-01'), 1),
        createEvent(new Date('2024-01-02'), new Date('2024-01-02'), 2),
      ];

      const grouped = groupEventsByDateForWeekView(events, weekDays);

      expect(grouped['2024-01-01']).toHaveLength(1);
      expect(grouped['2024-01-02']).toHaveLength(1);
    });

    it('uses endDate for events without dueDate', () => {
      const events = [
        createEvent(undefined, new Date('2024-01-03'), 1),
      ];

      const grouped = groupEventsByDateForWeekView(events, weekDays);

      expect(grouped['2024-01-03']).toHaveLength(1);
    });
  });

  describe('groupEventsByDateForMonthView', () => {
    const monthDays = Array.from({ length: 31 }, (_, i) =>
      new Date(`2024-01-${(i + 1).toString().padStart(2, '0')}`)
    );

    const createEvent = (dueDate: Date | undefined, endDate: Date, id: number, title = `Event ${id}`): CalendarEvent => ({
      id,
      title,
      description: 'Test',
      startDate: new Date(),
      endDate,
      dueDate,
      taskType: 'FEATURE',
      status: 'TODO',
      projectId: 1,
      projectName: 'Test',
      tags: [],
      assignedUserIds: [],
      ownerId: 1,
      createdBy: 1,
      color: 'bg-blue-500',
    });

    it('groups events by date using dueDate', () => {
      const events = [
        createEvent(new Date('2024-01-15'), new Date('2024-01-15'), 1),
        createEvent(new Date('2024-01-16'), new Date('2024-01-16'), 2),
      ];

      const grouped = groupEventsByDateForMonthView(events, monthDays);

      expect(grouped['2024-01-15']).toHaveLength(1);
      expect(grouped['2024-01-16']).toHaveLength(1);
    });

    it('uses endDate for recurring events without dueDate', () => {
      const events = [
        createEvent(undefined, new Date('2024-01-20'), 1),
      ];

      const grouped = groupEventsByDateForMonthView(events, monthDays);

      expect(grouped['2024-01-20']).toHaveLength(1);
    });
  });

  describe('formatDisplayDate', () => {
    it('formats day view correctly', () => {
      const date = new Date('2024-01-15');
      const formatted = formatDisplayDate(date, 'day');

      expect(formatted).toContain('January');
      expect(formatted).toContain('15');
      expect(formatted).toContain('2024');
    });

    it('formats week view correctly', () => {
      const date = new Date('2024-01-15');
      const formatted = formatDisplayDate(date, 'week');

      expect(formatted).toContain('-');
      expect(formatted).toContain('2024');
    });

    it('formats month view correctly', () => {
      const date = new Date('2024-01-15');
      const formatted = formatDisplayDate(date, 'month');

      expect(formatted).toBe('January 2024');
    });

    it('formats timeline view correctly', () => {
      const date = new Date('2024-01-15');
      const formatted = formatDisplayDate(date, 'timeline');

      expect(formatted).toBe('January 2024');
    });

    it('formats default view correctly', () => {
      const date = new Date('2024-01-15');
      const formatted = formatDisplayDate(date, 'other');

      expect(formatted).toContain('January');
      expect(formatted).toContain('15');
      expect(formatted).toContain('2024');
    });
  });

  describe('isDateInView', () => {
    it('returns true for same day in day view', () => {
      const date = new Date('2024-01-15');
      const currentDate = new Date('2024-01-15');

      expect(isDateInView(date, currentDate, 'day')).toBe(true);
    });

    it('returns false for different day in day view', () => {
      const date = new Date('2024-01-15');
      const currentDate = new Date('2024-01-16');

      expect(isDateInView(date, currentDate, 'day')).toBe(false);
    });

    it('returns true for date in same week in week view', () => {
      const date = new Date('2024-01-15'); // Monday
      const currentDate = new Date('2024-01-17'); // Wednesday

      expect(isDateInView(date, currentDate, 'week')).toBe(true);
    });

    it('returns false for date in different week in week view', () => {
      const date = new Date('2024-01-15');
      const currentDate = new Date('2024-01-22');

      expect(isDateInView(date, currentDate, 'week')).toBe(false);
    });

    it('returns true for date in same month in month view', () => {
      const date = new Date('2024-01-15');
      const currentDate = new Date('2024-01-25');

      expect(isDateInView(date, currentDate, 'month')).toBe(true);
    });

    it('returns false for date in different month in month view', () => {
      const date = new Date('2024-01-15');
      const currentDate = new Date('2024-02-15');

      expect(isDateInView(date, currentDate, 'month')).toBe(false);
    });
  });
});
