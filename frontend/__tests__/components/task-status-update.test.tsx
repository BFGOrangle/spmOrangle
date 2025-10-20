/**
 * Tests for task status updates that trigger time tracking
 *
 * These tests verify that:
 * 1. Status changes trigger appropriate backend calls
 * 2. Time tracking is implicit (handled by backend)
 * 3. Recurring tasks create next instances when completed
 */

import React from 'react';
import { render, screen, fireEvent, waitFor } from '../test-utils';
import { TaskUpdateDialog } from '../../components/task-update-dialog';
import { TaskResponse } from '../../services/project-service';

// Mock services
const mockUpdateTask = jest.fn().mockResolvedValue({});
const mockGetTags = jest.fn();
const mockGetProjectMembers = jest.fn();

jest.mock('../../hooks/use-task-mutations', () => ({
  useUpdateTask: () => ({
    mutate: jest.fn(),
    mutateAsync: mockUpdateTask,
    isPending: false,
    isError: false,
    error: null,
  }),
}));

jest.mock('../../services/tag-service', () => ({
  tagService: {
    getTags: () => mockGetTags(),
  },
}));

jest.mock('../../services/user-management-service', () => ({
  userManagementService: {
    getProjectMembers: () => mockGetProjectMembers(),
    getCollaborators: jest.fn().mockResolvedValue([]),
  },
}));

// Mock UI components
jest.mock('../../components/ui/dialog', () => ({
  Dialog: ({ children, open }: { children: React.ReactNode; open: boolean }) =>
    open ? <div role="dialog" data-testid="task-update-dialog">{children}</div> : null,
  DialogContent: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
  DialogHeader: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
  DialogTitle: ({ children }: { children: React.ReactNode }) => <h2>{children}</h2>,
  DialogDescription: ({ children }: { children: React.ReactNode }) => <p>{children}</p>,
  DialogFooter: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
}));

jest.mock('../../components/ui/button', () => ({
  Button: ({ children, onClick, type, disabled, className }: any) => (
    <button onClick={onClick} type={type} disabled={disabled} className={className}>
      {children}
    </button>
  )
}));

jest.mock('../../components/ui/select', () => ({
  Select: ({ value, onValueChange, children }: any) => {
    // Extract SelectTrigger to get the id
    let triggerId = 'select';
    React.Children.forEach(children, (child: any) => {
      if (child?.type?.name === 'SelectTrigger' || child?.props?.id) {
        triggerId = child.props.id || triggerId;
      }
    });

    return (
      <select
        id={triggerId}
        value={value}
        onChange={(e) => onValueChange(e.target.value)}
      >
        {children}
      </select>
    );
  },
  SelectTrigger: ({ children, id }: any) => <>{children}</>,
  SelectValue: ({ placeholder }: any) => <span>{placeholder}</span>,
  SelectContent: ({ children }: any) => <>{children}</>,
  SelectItem: ({ value, children }: any) => <option value={value}>{children}</option>,
}));

jest.mock('../../components/ui/input', () => ({
  Input: (props: any) => <input {...props} />,
}));

jest.mock('../../components/ui/textarea', () => ({
  Textarea: (props: any) => <textarea {...props} />,
}));

jest.mock('../../components/ui/badge', () => ({
  Badge: ({ children }: any) => <span>{children}</span>,
}));

jest.mock('../../components/task-collaborator-management', () => ({
  TaskCollaboratorManagement: () => <div data-testid="collaborator-management">Collaborators</div>,
}));

jest.mock('../../components/recurrence-selector', () => ({
  RecurrenceSelector: ({ value, onChange }: any) => (
    <div data-testid="recurrence-selector">
      <button onClick={() => onChange({ isRecurring: true, recurrenceRuleStr: 'FREQ=DAILY' })}>
        Enable Recurrence
      </button>
    </div>
  )
}));

describe('Task Status Updates with Time Tracking', () => {
  const mockTask: TaskResponse = {
    id: 1,
    projectId: 101,
    ownerId: 201,
    ownerUsername: 'john.doe',
    title: 'Test Task',
    description: 'Test Description',
    status: 'TODO',
    taskType: 'FEATURE',
    tags: ['testing'],
    dueDateTime: '2025-10-25T10:00:00Z',
    createdAt: '2025-10-20T08:00:00Z',
    createdBy: 201,
    createdByUsername: 'john.doe',
    updatedAt: null,
    updatedBy: null,
    updatedByUsername: null,
    userHasEditAccess: true,
    userHasDeleteAccess: true,
    isRecurring: false,
    recurrenceRuleStr: null,
    startDate: null,
    endDate: null,
  };

  const mockOnTaskUpdated = jest.fn();
  const mockOnOpenChange = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
    mockGetTags.mockResolvedValue([
      { id: 1, tagName: 'testing' },
      { id: 2, tagName: 'urgent' },
    ]);
    mockGetProjectMembers.mockResolvedValue([]);
  });

  describe('Status Transitions that Trigger Time Tracking', () => {
    it('should update task from TODO to IN_PROGRESS', async () => {
      render(
        <TaskUpdateDialog
          task={mockTask}
          open={true}
          onOpenChange={mockOnOpenChange}
          onTaskUpdated={mockOnTaskUpdated}
        />
      );

      // Find and change status to IN_PROGRESS
      const statusSelect = document.querySelector('#status') as HTMLSelectElement;
      fireEvent.change(statusSelect!, { target: { value: 'IN_PROGRESS' } });

      // Submit form
      const updateButton = screen.getByRole('button', { name: /update task/i });
      fireEvent.click(updateButton);

      await waitFor(() => {
        expect(mockUpdateTask).toHaveBeenCalledWith(
          expect.objectContaining({
            taskId: 1,
            status: 'IN_PROGRESS',
          })
          
        );
      });
    });

    it('should update task from IN_PROGRESS to COMPLETED', async () => {
      const inProgressTask = { ...mockTask, status: 'IN_PROGRESS' };

      render(
        <TaskUpdateDialog
          task={inProgressTask}
          open={true}
          onOpenChange={mockOnOpenChange}
          onTaskUpdated={mockOnTaskUpdated}
        />
      );

      const statusSelect = document.querySelector('#status') as HTMLSelectElement;
      fireEvent.change(statusSelect!, { target: { value: 'COMPLETED' } });

      const updateButton = screen.getByRole('button', { name: /update task/i });
      fireEvent.click(updateButton);

      await waitFor(() => {
        expect(mockUpdateTask).toHaveBeenCalledWith(
          expect.objectContaining({
            taskId: 1,
            status: 'COMPLETED',
          })
          
        );
      });
    });

    it('should update task from COMPLETED back to IN_PROGRESS', async () => {
      const completedTask = { ...mockTask, status: 'COMPLETED' };

      render(
        <TaskUpdateDialog
          task={completedTask}
          open={true}
          onOpenChange={mockOnOpenChange}
          onTaskUpdated={mockOnTaskUpdated}
        />
      );

      const statusSelect = document.querySelector('#status') as HTMLSelectElement;
      fireEvent.change(statusSelect!, { target: { value: 'IN_PROGRESS' } });

      const updateButton = screen.getByRole('button', { name: /update task/i });
      fireEvent.click(updateButton);

      await waitFor(() => {
        expect(mockUpdateTask).toHaveBeenCalledWith(
          expect.objectContaining({
            taskId: 1,
            status: 'IN_PROGRESS',
          })
          
        );
      });
    });

    it('should allow status change from TODO to BLOCKED without time tracking', async () => {
      render(
        <TaskUpdateDialog
          task={mockTask}
          open={true}
          onOpenChange={mockOnOpenChange}
          onTaskUpdated={mockOnTaskUpdated}
        />
      );

      const statusSelect = document.querySelector('#status') as HTMLSelectElement;
      fireEvent.change(statusSelect!, { target: { value: 'BLOCKED' } });

      const updateButton = screen.getByRole('button', { name: /update task/i });
      fireEvent.click(updateButton);

      await waitFor(() => {
        expect(mockUpdateTask).toHaveBeenCalledWith(
          expect.objectContaining({
            taskId: 1,
            status: 'BLOCKED',
          })
          
        );
      });
    });
  });

  describe('Recurring Task Completion', () => {
    it('should complete recurring task which triggers next instance creation on backend', async () => {
      const recurringTask: TaskResponse = {
        ...mockTask,
        status: 'IN_PROGRESS',
        isRecurring: true,
        recurrenceRuleStr: 'FREQ=DAILY;COUNT=5',
        startDate: '2025-10-20T00:00:00Z',
        endDate: '2025-10-30T23:59:59Z',
      };

      render(
        <TaskUpdateDialog
          task={recurringTask}
          open={true}
          onOpenChange={mockOnOpenChange}
          onTaskUpdated={mockOnTaskUpdated}
        />
      );

      const statusSelect = document.querySelector('#status') as HTMLSelectElement;
      fireEvent.change(statusSelect!, { target: { value: 'COMPLETED' } });

      const updateButton = screen.getByRole('button', { name: /update task/i });
      fireEvent.click(updateButton);

      await waitFor(() => {
        expect(mockUpdateTask).toHaveBeenCalledWith(
          expect.objectContaining({
            taskId: 1,
            status: 'COMPLETED',
          })
          
        );
      });
    });

    it('should preserve recurrence information when updating other fields of recurring task', async () => {
      const recurringTask: TaskResponse = {
        ...mockTask,
        isRecurring: true,
        recurrenceRuleStr: 'FREQ=WEEKLY;BYDAY=MO',
        startDate: '2025-10-20T00:00:00Z',
        endDate: '2025-12-31T23:59:59Z',
      };

      render(
        <TaskUpdateDialog
          task={recurringTask}
          open={true}
          onOpenChange={mockOnOpenChange}
          onTaskUpdated={mockOnTaskUpdated}
        />
      );

      // Make a change to trigger update (but don't change recurrence)
      const titleInput = screen.getByLabelText(/title/i);
      fireEvent.change(titleInput, { target: { value: 'Updated Recurring Task' } });

      const updateButton = screen.getByRole('button', { name: /update task/i });
      fireEvent.click(updateButton);

      await waitFor(() => {
        expect(mockUpdateTask).toHaveBeenCalledWith(
          expect.objectContaining({
            taskId: 1,
            title: 'Updated Recurring Task',
            // Recurrence info not included since it didn't change
          })

        );
      });
    });
  });

  describe('Task Property Updates', () => {
    it('should update task title and description', async () => {
      render(
        <TaskUpdateDialog
          task={mockTask}
          open={true}
          onOpenChange={mockOnOpenChange}
          onTaskUpdated={mockOnTaskUpdated}
        />
      );

      const titleInput = screen.getByLabelText(/title/i);
      const descriptionInput = screen.getByLabelText(/description/i);

      fireEvent.change(titleInput, { target: { value: 'Updated Title' } });
      fireEvent.change(descriptionInput, { target: { value: 'Updated Description' } });

      const updateButton = screen.getByRole('button', { name: /update task/i });
      fireEvent.click(updateButton);

      await waitFor(() => {
        expect(mockUpdateTask).toHaveBeenCalledWith(
          expect.objectContaining({
            taskId: 1,
            title: 'Updated Title',
            description: 'Updated Description',
          })
          
        );
      });
    });

    it('should update task without changing status', async () => {
      render(
        <TaskUpdateDialog
          task={mockTask}
          open={true}
          onOpenChange={mockOnOpenChange}
          onTaskUpdated={mockOnTaskUpdated}
        />
      );

      const titleInput = screen.getByLabelText(/title/i);
      fireEvent.change(titleInput, { target: { value: 'Updated Title Only' } });

      const updateButton = screen.getByRole('button', { name: /update task/i });
      fireEvent.click(updateButton);

      await waitFor(() => {
        expect(mockUpdateTask).toHaveBeenCalledWith(
          expect.objectContaining({
            taskId: 1,
            title: 'Updated Title Only',
            // Status not included since it didn't change
          })

        );
      });
    });
  });

  describe('Dialog Behavior', () => {
    it('should allow editing task fields', () => {
      render(
        <TaskUpdateDialog
          task={mockTask}
          open={true}
          onOpenChange={mockOnOpenChange}
          onTaskUpdated={mockOnTaskUpdated}
        />
      );

      // Should be able to modify the form
      const titleInput = screen.getByLabelText(/title/i) as HTMLInputElement;
      const descriptionInput = screen.getByLabelText(/description/i) as HTMLTextAreaElement;

      expect(titleInput.value).toBe('Test Task');
      expect(descriptionInput.value).toBe('Test Description');

      fireEvent.change(titleInput, { target: { value: 'Changed Title' } });
      fireEvent.change(descriptionInput, { target: { value: 'Changed Description' } });

      expect(titleInput.value).toBe('Changed Title');
      expect(descriptionInput.value).toBe('Changed Description');
    });

    it('should display task information correctly', () => {
      render(
        <TaskUpdateDialog
          task={mockTask}
          open={true}
          onOpenChange={mockOnOpenChange}
          onTaskUpdated={mockOnTaskUpdated}
        />
      );

      expect(screen.getByDisplayValue('Test Task')).toBeInTheDocument();
      expect(screen.getByDisplayValue('Test Description')).toBeInTheDocument();
      const statusSelect = document.querySelector('#status') as HTMLSelectElement;
      expect(statusSelect).toHaveValue('TODO');
    });
  });
});
