/**
 * Unit tests for Calendar Page
 * Tests the two-step logic for fetching projects and tasks
 */

import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import CalendarPage from '../../../app/(app)/calendar/page';
import { projectService } from '../../../services/project-service';
import { UserProvider } from '../../../contexts/user-context';

// Mock the services
jest.mock('../../../services/project-service');
jest.mock('../../../contexts/user-context', () => ({
  ...jest.requireActual('../../../contexts/user-context'),
  useCurrentUser: () => ({
    currentUser: { backendStaffId: 1, id: '1', email: 'test@test.com' },
    isLoading: false,
  }),
}));

// Mock UI components
jest.mock('@/components/ui/sidebar', () => ({
  SidebarInset: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
  SidebarTrigger: () => <div>Sidebar Trigger</div>,
}));

jest.mock('@/components/ui/separator', () => ({
  Separator: () => <div>Separator</div>,
}));

jest.mock('../../../components/calendar/calendar-controls', () => ({
  CalendarControls: ({ selectedTaskType, onTaskTypeChange }: any) => (
    <div>
      <div>Calendar Controls</div>
      <button onClick={() => onTaskTypeChange('My Projects')}>My Projects</button>
      <button onClick={() => onTaskTypeChange('Personal Tasks')}>Personal Tasks</button>
      <button onClick={() => onTaskTypeChange('Project Tasks')}>Project Tasks</button>
      <div data-testid="selected-task-type">{selectedTaskType}</div>
    </div>
  ),
}));

jest.mock('../../../components/calendar/calendar-month-view', () => ({
  CalendarMonthView: ({ events }: any) => (
    <div data-testid="calendar-month-view">
      {events.map((event: any) => (
        <div key={event.id} data-testid={`event-${event.id}`}>
          {event.title}
        </div>
      ))}
    </div>
  ),
}));

jest.mock('../../../components/full-page-spinner-loader', () => ({
  __esModule: true,
  default: () => <div>Loading...</div>,
}));

const mockProjects = [
  {
    id: 1,
    name: 'Project Alpha',
    description: 'Test project',
    ownerId: 1,
    createdAt: '2024-01-01',
    updatedAt: '2024-01-01',
    taskCount: 5,
    completedTaskCount: 2,
    isOwner: true,
    isRelated: false,
  },
  {
    id: 2,
    name: 'Project Beta',
    description: 'Test project 2',
    ownerId: 2,
    createdAt: '2024-01-01',
    updatedAt: '2024-01-01',
    taskCount: 3,
    completedTaskCount: 1,
    isOwner: false,
    isRelated: true,
  },
];

const mockTasksProject1 = [
  {
    id: 101,
    projectId: 1,
    projectName: 'Project Alpha',
    ownerId: 1,
    taskType: 'FEATURE' as const,
    title: 'Task 1',
    description: 'Description 1',
    status: 'TODO' as const,
    tags: [],
    assignedUserIds: [1],
    userHasEditAccess: true,
    userHasDeleteAccess: true,
    createdAt: '2024-01-01',
    createdBy: 1,
    dueDateTime: '2024-12-01T10:00:00Z',
  },
];

const mockTasksProject2 = [
  {
    id: 201,
    projectId: 2,
    projectName: 'Project Beta',
    ownerId: 2,
    taskType: 'CHORE' as const,
    title: 'Task 3',
    description: 'Description 3',
    status: 'COMPLETED' as const,
    tags: [],
    assignedUserIds: [1],
    userHasEditAccess: true,
    userHasDeleteAccess: false,
    createdAt: '2024-01-01',
    createdBy: 2,
    dueDateTime: '2024-12-03T09:00:00Z',
  },
];

const mockPersonalTasks = [
  {
    id: 301,
    projectId: 0,
    ownerId: 1,
    taskType: 'FEATURE' as const,
    title: 'Personal Task 1',
    description: 'Personal description',
    status: 'TODO' as const,
    tags: [],
    assignedUserIds: [1],
    userHasEditAccess: true,
    userHasDeleteAccess: true,
    createdAt: '2024-01-01',
    createdBy: 1,
    dueDateTime: '2024-12-05T16:00:00Z',
  },
];

describe('CalendarPage - Two-Step Logic', () => {
  let queryClient: QueryClient;

  beforeEach(() => {
    queryClient = new QueryClient({
      defaultOptions: {
        queries: { retry: false },
      },
    });
    jest.clearAllMocks();

    // Setup default mocks
    (projectService.getUserProjects as jest.Mock).mockResolvedValue(mockProjects);
    (projectService.getProjectTasks as jest.Mock).mockImplementation((projectId: number) => {
      if (projectId === 1) return Promise.resolve(mockTasksProject1);
      if (projectId === 2) return Promise.resolve(mockTasksProject2);
      return Promise.resolve([]);
    });
    (projectService.getPersonalTasks as jest.Mock).mockResolvedValue(mockPersonalTasks);
  });

  const renderCalendarPage = () => {
    return render(
      <QueryClientProvider client={queryClient}>
        <UserProvider>
          <CalendarPage />
        </UserProvider>
      </QueryClientProvider>
    );
  };

  describe('Step 1: Fetch User Projects', () => {
    it('should call getUserProjects on mount', async () => {
      renderCalendarPage();

      await waitFor(() => {
        expect(projectService.getUserProjects).toHaveBeenCalledWith(1);
      });
    });

    it('should fetch projects before fetching tasks', async () => {
      const callOrder: string[] = [];
      
      (projectService.getUserProjects as jest.Mock).mockImplementation(() => {
        callOrder.push('getUserProjects');
        return Promise.resolve(mockProjects);
      });

      (projectService.getProjectTasks as jest.Mock).mockImplementation(() => {
        callOrder.push('getProjectTasks');
        return Promise.resolve(mockTasksProject1);
      });

      renderCalendarPage();

      await waitFor(() => {
        expect(callOrder[0]).toBe('getUserProjects');
        expect(callOrder.filter(call => call === 'getProjectTasks').length).toBeGreaterThan(0);
      });
    });
  });

  describe('Step 2: Fetch Tasks for Each Project', () => {
    it('should fetch tasks for all projects in "My Projects" view', async () => {
      renderCalendarPage();

      await waitFor(() => {
        expect(projectService.getProjectTasks).toHaveBeenCalledWith(
          1,
          undefined,
          'month',
          expect.any(Date)
        );
        expect(projectService.getProjectTasks).toHaveBeenCalledWith(
          2,
          undefined,
          'month',
          expect.any(Date)
        );
      });
    });

    it('should pass calendar view parameter to backend', async () => {
      renderCalendarPage();

      await waitFor(() => {
        expect(projectService.getProjectTasks).toHaveBeenCalledWith(
          expect.any(Number),
          undefined,
          'month', // Default view (lowercase for backend)
          expect.any(Date)
        );
      });
    });

    it('should pass reference date for recurring task expansion', async () => {
      renderCalendarPage();

      await waitFor(() => {
        expect(projectService.getProjectTasks).toHaveBeenCalledWith(
          expect.any(Number),
          undefined,
          expect.any(String),
          expect.any(Date)
        );
      });
    });

    it('should aggregate tasks from multiple projects', async () => {
      renderCalendarPage();

      await waitFor(() => {
        // Should show tasks from both projects
        expect(screen.getByText('Task 1')).toBeInTheDocument();
        expect(screen.getByText('Task 3')).toBeInTheDocument();
      });
    });

    it('should skip personal tasks (projectId = 0) in "My Projects" view', async () => {
      const mockProjectsWithPersonal = [
        ...mockProjects,
        {
          id: 0,
          name: 'Personal',
          description: 'Personal tasks',
          ownerId: 1,
          createdAt: '2024-01-01',
          updatedAt: '2024-01-01',
          taskCount: 1,
          completedTaskCount: 0,
          isOwner: true,
          isRelated: false,
        },
      ];

      (projectService.getUserProjects as jest.Mock).mockResolvedValue(mockProjectsWithPersonal);
      renderCalendarPage();

      await waitFor(() => {
        // Should not call getProjectTasks with projectId = 0
        const calls = (projectService.getProjectTasks as jest.Mock).mock.calls;
        const projectIds = calls.map(call => call[0]);
        expect(projectIds).not.toContain(0);
      });
    });
  });

  describe('Personal Tasks View', () => {
    it('should fetch only personal tasks when "Personal Tasks" is selected', async () => {
      renderCalendarPage();

      // Wait for page to load
      await waitFor(() => {
        expect(screen.getByText('Calendar Controls')).toBeInTheDocument();
      });

      const personalTasksButton = screen.getByText('Personal Tasks');
      personalTasksButton.click();

      await waitFor(() => {
        expect(projectService.getPersonalTasks).toHaveBeenCalledWith(
          1,
          undefined,
          'month',
          expect.any(Date)
        );
      });
    });

    it('should not fetch project tasks in "Personal Tasks" view', async () => {
      renderCalendarPage();

      // Wait for page to load
      await waitFor(() => {
        expect(screen.getByText('Calendar Controls')).toBeInTheDocument();
      });

      const personalTasksButton = screen.getByText('Personal Tasks');
      personalTasksButton.click();

      await waitFor(() => {
        expect(projectService.getPersonalTasks).toHaveBeenCalled();
      });

      // Clear previous calls
      (projectService.getProjectTasks as jest.Mock).mockClear();

      // Wait a bit to ensure no additional calls are made
      await new Promise(resolve => setTimeout(resolve, 100));

      expect(projectService.getProjectTasks).not.toHaveBeenCalled();
    });
  });

  describe('Project Tasks View - Show All Tasks', () => {
    it('should show all tasks including colleague tasks in "Project Tasks" view', async () => {
      renderCalendarPage();

      // Wait for page to load
      await waitFor(() => {
        expect(screen.getByText('Calendar Controls')).toBeInTheDocument();
      });

      const projectTasksButton = screen.getByText('Project Tasks');
      projectTasksButton.click();

      await waitFor(() => {
        // Should show tasks from both projects
        expect(screen.getByText('Task 1')).toBeInTheDocument();
        expect(screen.getByText('Task 3')).toBeInTheDocument();
      });
    });

    it('should fetch tasks for all projects in "Project Tasks" view', async () => {
      renderCalendarPage();

      // Wait for page to load
      await waitFor(() => {
        expect(screen.getByText('Calendar Controls')).toBeInTheDocument();
      });

      const projectTasksButton = screen.getByText('Project Tasks');
      projectTasksButton.click();

      await waitFor(() => {
        expect(projectService.getProjectTasks).toHaveBeenCalledWith(1, undefined, 'month', expect.any(Date));
        expect(projectService.getProjectTasks).toHaveBeenCalledWith(2, undefined, 'month', expect.any(Date));
      });
    });
  });

  describe('Error Handling', () => {
    it('should handle project fetch errors gracefully', async () => {
      (projectService.getUserProjects as jest.Mock).mockRejectedValue(new Error('Network error'));

      renderCalendarPage();

      await waitFor(() => {
        expect(screen.getByText(/error loading the calendar/i)).toBeInTheDocument();
      });
    });

    it('should continue fetching other projects if one fails', async () => {
      const consoleErrorSpy = jest.spyOn(console, 'error').mockImplementation();
      
      (projectService.getProjectTasks as jest.Mock).mockImplementation((projectId: number) => {
        if (projectId === 1) return Promise.reject(new Error('Failed to fetch'));
        if (projectId === 2) return Promise.resolve(mockTasksProject2);
        return Promise.resolve([]);
      });

      renderCalendarPage();

      await waitFor(() => {
        // Should still show tasks from project 2
        expect(screen.getByText('Task 3')).toBeInTheDocument();
      });

      expect(consoleErrorSpy).toHaveBeenCalled();
      consoleErrorSpy.mockRestore();
    });
  });

  describe('Console Logging', () => {
    it('should log detailed information when fetching tasks', async () => {
      const consoleLogSpy = jest.spyOn(console, 'log').mockImplementation();

      renderCalendarPage();

      await waitFor(() => {
        // Check for the detailed logging
        expect(consoleLogSpy).toHaveBeenCalledWith('🔄 Fetching tasks for My Projects');
        expect(consoleLogSpy).toHaveBeenCalledWith('📋 Total projects:', 2);
      });

      consoleLogSpy.mockRestore();
    });

    it('should log task details for each project', async () => {
      const consoleLogSpy = jest.spyOn(console, 'log').mockImplementation();

      renderCalendarPage();

      await waitFor(() => {
        // Check for project-specific logs
        expect(consoleLogSpy).toHaveBeenCalledWith(
          expect.stringContaining('📂 Fetching tasks for Project ID: 1')
        );
        expect(consoleLogSpy).toHaveBeenCalledWith(
          `✅ Received ${mockTasksProject1.length} tasks from backend for project 1`
        );
      });

      consoleLogSpy.mockRestore();
    });
  });
});
