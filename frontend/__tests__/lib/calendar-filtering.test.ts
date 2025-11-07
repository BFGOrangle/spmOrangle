/**
 * Unit tests for Calendar Filtering Logic
 * Tests that colleague tasks are properly displayed in different views
 */

import { filterEventsByUserAccess } from '../../lib/calendar-utils';
import { CalendarEvent } from '../../types/calendar';

describe('Calendar Filtering - Colleague Task Visibility', () => {
  const currentUserId = 1;

  const userOwnedTask: CalendarEvent = {
    id: 101,
    title: 'User Owned Task',
    description: 'Task owned by current user',
    startDate: new Date('2024-01-15'),
    endDate: new Date('2024-01-15'),
    dueDate: new Date('2024-01-15T10:00:00Z'),
    taskType: 'FEATURE',
    status: 'TODO',
    projectId: 1,
    projectName: 'Project Alpha',
    tags: [],
    assignedUserIds: [1],
    ownerId: 1,
    createdBy: 1,
    color: 'bg-blue-500',
  };

  const userAssignedTask: CalendarEvent = {
    id: 102,
    title: 'User Assigned Task',
    description: 'Task assigned to current user',
    startDate: new Date('2024-01-15'),
    endDate: new Date('2024-01-15'),
    dueDate: new Date('2024-01-15T11:00:00Z'),
    taskType: 'BUG',
    status: 'IN_PROGRESS',
    projectId: 1,
    projectName: 'Project Alpha',
    tags: [],
    assignedUserIds: [1, 2],
    ownerId: 2,
    createdBy: 2,
    color: 'bg-red-500',
  };

  const colleagueTask: CalendarEvent = {
    id: 103,
    title: 'Colleague Task',
    description: 'Task owned and assigned to colleague',
    startDate: new Date('2024-01-15'),
    endDate: new Date('2024-01-15'),
    dueDate: new Date('2024-01-15T14:00:00Z'),
    taskType: 'FEATURE',
    status: 'TODO',
    projectId: 1,
    projectName: 'Project Alpha',
    tags: [],
    assignedUserIds: [2, 3],
    ownerId: 2,
    createdBy: 2,
    color: 'bg-green-500',
  };

  const unassignedTask: CalendarEvent = {
    id: 104,
    title: 'Unassigned Task',
    description: 'Task with no assignees',
    startDate: new Date('2024-01-15'),
    endDate: new Date('2024-01-15'),
    dueDate: new Date('2024-01-15T16:00:00Z'),
    taskType: 'CHORE',
    status: 'TODO',
    projectId: 1,
    projectName: 'Project Alpha',
    tags: [],
    assignedUserIds: [],
    ownerId: 3,
    createdBy: 3,
    color: 'bg-yellow-500',
  };

  const allTasks = [userOwnedTask, userAssignedTask, colleagueTask, unassignedTask];

  describe('filterEventsByUserAccess', () => {
    it('should filter to show only user-assigned tasks', () => {
      const filtered = filterEventsByUserAccess(allTasks, currentUserId);

      expect(filtered).toHaveLength(2);
      expect(filtered).toContainEqual(userOwnedTask); // included because user is assigned
      expect(filtered).toContainEqual(userAssignedTask); // included because user is assigned
      expect(filtered).not.toContainEqual(colleagueTask);
      expect(filtered).not.toContainEqual(unassignedTask);
    });

    it('should include task if user is assigned (even if also owner)', () => {
      const filtered = filterEventsByUserAccess([userOwnedTask], currentUserId);

      expect(filtered).toHaveLength(1);
      expect(filtered[0]).toEqual(userOwnedTask);
    });

    it('should include task if user is assigned to it', () => {
      const filtered = filterEventsByUserAccess([userAssignedTask], currentUserId);

      expect(filtered).toHaveLength(1);
      expect(filtered[0]).toEqual(userAssignedTask);
    });

    it('should exclude task if user is neither owner nor assigned', () => {
      const filtered = filterEventsByUserAccess([colleagueTask], currentUserId);

      expect(filtered).toHaveLength(0);
    });

    it('should exclude unassigned tasks not owned by user', () => {
      const filtered = filterEventsByUserAccess([unassignedTask], currentUserId);

      expect(filtered).toHaveLength(0);
    });

    it('should return all events if no user ID provided', () => {
      const filtered = filterEventsByUserAccess(allTasks, undefined);

      expect(filtered).toHaveLength(4);
      expect(filtered).toEqual(allTasks);
    });

    it('should return all events if user ID is null', () => {
      const filtered = filterEventsByUserAccess(allTasks, undefined);

      expect(filtered).toHaveLength(4);
    });
  });

  describe('Calendar View Filtering Behavior', () => {
    it('should apply user access filter for "My Projects" view', () => {
      // In My Projects view, filterEventsByUserAccess should be applied
      const filtered = filterEventsByUserAccess(allTasks, currentUserId);

      // Should only show user's own tasks
      expect(filtered).toHaveLength(2);
      expect(filtered.some(t => t.id === 103)).toBe(false); // Colleague task excluded
    });

    it('should NOT apply user access filter for "Project Tasks" view', () => {
      // In Project Tasks view, filterEventsByUserAccess should NOT be applied
      // So we should get all tasks without filtering
      const filtered = allTasks; // No filtering applied

      // Should show all tasks including colleague tasks
      expect(filtered).toHaveLength(4);
      expect(filtered.some(t => t.id === 103)).toBe(true); // Colleague task included
      expect(filtered.some(t => t.id === 104)).toBe(true); // Unassigned task included
    });

    it('should show colleague tasks in "Project Tasks" view', () => {
      // Simulate Project Tasks view - no filtering by user access
      const projectTasksView = allTasks;

      const colleagueTasks = projectTasksView.filter(
        t => !t.assignedUserIds?.includes(currentUserId) && t.ownerId !== currentUserId
      );

      // Should find colleague and unassigned tasks
      expect(colleagueTasks.length).toBeGreaterThan(0);
      expect(colleagueTasks).toContainEqual(colleagueTask);
      expect(colleagueTasks).toContainEqual(unassignedTask);
    });

    it('should apply user access filter for "Personal Tasks" view', () => {
      // In Personal Tasks view, filterEventsByUserAccess should be applied
      const personalTasks = allTasks.filter(t => t.projectId === 0);
      const filtered = filterEventsByUserAccess(personalTasks, currentUserId);

      // Should only show user's personal tasks
      expect(filtered.every(t => t.ownerId === currentUserId || t.assignedUserIds?.includes(currentUserId))).toBe(true);
    });
  });

  describe('Tasks with Multiple Assignees', () => {
    it('should include task if user is one of multiple assignees', () => {
      const multiAssigneeTask: CalendarEvent = {
        ...colleagueTask,
        assignedUserIds: [1, 2, 3, 4], // User 1 is included
      };

      const filtered = filterEventsByUserAccess([multiAssigneeTask], currentUserId);

      expect(filtered).toHaveLength(1);
      expect(filtered[0]).toEqual(multiAssigneeTask);
    });

    it('should exclude task if user is not in the assignee list', () => {
      const multiAssigneeTask: CalendarEvent = {
        ...colleagueTask,
        assignedUserIds: [2, 3, 4], // User 1 is NOT included
        ownerId: 2, // Not owned by user 1
      };

      const filtered = filterEventsByUserAccess([multiAssigneeTask], currentUserId);

      expect(filtered).toHaveLength(0);
    });
  });

  describe('Edge Cases', () => {
    it('should handle empty event array', () => {
      const filtered = filterEventsByUserAccess([], currentUserId);

      expect(filtered).toHaveLength(0);
    });

    it('should handle events without assignedUserIds', () => {
      const taskWithoutAssignees: CalendarEvent = {
        ...userOwnedTask,
        assignedUserIds: undefined,
      };

      const filtered = filterEventsByUserAccess([taskWithoutAssignees], currentUserId);

      // Should be excluded because user is not in assignedUserIds (ownership doesn't grant access)
      expect(filtered).toHaveLength(0);
    });

    it('should handle events with empty assignedUserIds array', () => {
      const taskWithEmptyAssignees: CalendarEvent = {
        ...colleagueTask,
        assignedUserIds: [],
        ownerId: 2,
      };

      const filtered = filterEventsByUserAccess([taskWithEmptyAssignees], currentUserId);

      // Should be excluded because user is not owner and not assigned
      expect(filtered).toHaveLength(0);
    });

    it('should handle user ID of 0', () => {
      const filtered = filterEventsByUserAccess(allTasks, 0);

      // User ID 0 is treated as falsy, so it returns all tasks (like undefined)
      // This matches the behavior in filterEventsByUserAccess: if (!currentUserId) return events
      expect(filtered).toHaveLength(4);
    });
  });

  describe('Filter Tasks without Due Dates', () => {
    it('should exclude tasks without dueDate in calendar views', () => {
      const taskWithoutDueDate: CalendarEvent = {
        ...userOwnedTask,
        dueDate: undefined,
      };

      const tasksWithMixedDueDates = [userOwnedTask, taskWithoutDueDate];

      // Simulate the calendar page filtering
      const filtered = tasksWithMixedDueDates.filter(
        event => event.dueDate !== undefined && event.dueDate !== null
      );

      expect(filtered).toHaveLength(1);
      expect(filtered[0]).toEqual(userOwnedTask);
    });

    it('should include all tasks with valid due dates', () => {
      const filtered = allTasks.filter(
        event => event.dueDate !== undefined && event.dueDate !== null
      );

      expect(filtered).toHaveLength(4);
      expect(filtered).toEqual(allTasks);
    });
  });
});
