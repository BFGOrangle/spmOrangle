import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import React from 'react';
import { useCreateTask, useUpdateTask, useDeleteTask } from '@/hooks/use-task-mutations';
import { projectService } from '@/services/project-service';
import { TaskResponse, CreateTaskRequest, UpdateTaskRequest } from '@/services/project-service';

// Mock the project service
jest.mock('@/services/project-service', () => ({
  projectService: {
    createTask: jest.fn(),
    createTaskWithSpecifiedOwner: jest.fn(),
    updateTask: jest.fn(),
    deleteTask: jest.fn(),
  },
}));

// Mock window events
const mockDispatchEvent = jest.fn();
Object.defineProperty(window, 'dispatchEvent', {
  value: mockDispatchEvent,
  writable: true,
});

const mockProjectService = projectService as jest.Mocked<typeof projectService>;

// Test wrapper with QueryClient
const createWrapper = () => {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false },
      mutations: { retry: false },
    },
  });

  return ({ children }: { children: React.ReactNode }) =>
    React.createElement(QueryClientProvider, { client: queryClient }, children);
};

describe('useCreateTask', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockDispatchEvent.mockClear();
  });

  it('should create a project task successfully', async () => {
    const mockTask: TaskResponse = {
      id: 1,
      projectId: 123,
      ownerId: 1,
      taskType: 'FEATURE',
      title: 'Test Task',
      description: 'Test Description',
      status: 'TODO',
      userHasEditAccess: true,
      userHasDeleteAccess: true,
      createdAt: '2025-01-01T00:00:00Z',
      createdBy: 1,
    };

    mockProjectService.createTask.mockResolvedValue(mockTask);

    const { result } = renderHook(() => useCreateTask(), {
      wrapper: createWrapper(),
    });

    const createRequest: CreateTaskRequest = {
      projectId: 123,
      ownerId: 1,
      title: 'Test Task',
      description: 'Test Description',
      taskType: 'FEATURE',
    };

    await waitFor(async () => {
      await result.current.mutateAsync({ taskData: createRequest });
    });

    expect(mockProjectService.createTask).toHaveBeenCalledWith(createRequest);
    expect(mockDispatchEvent).toHaveBeenCalledWith(
      expect.objectContaining({
        type: 'taskCreated',
        detail: { task: mockTask, isPersonalTask: false },
      })
    );
  });

  it('should create a personal task successfully', async () => {
    const mockPersonalTask: TaskResponse = {
      id: 2,
      projectId: 0, // Personal task
      ownerId: 1,
      taskType: 'CHORE',
      title: 'Personal Task',
      status: 'TODO',
      userHasEditAccess: true,
      userHasDeleteAccess: true,
      createdAt: '2025-01-01T00:00:00Z',
      createdBy: 1,
    };

    mockProjectService.createTask.mockResolvedValue(mockPersonalTask);

    const { result } = renderHook(() => useCreateTask(), {
      wrapper: createWrapper(),
    });

    const createRequest: CreateTaskRequest = {
      projectId: 0,
      ownerId: 1,
      title: 'Personal Task',
      taskType: 'CHORE',
    };

    await waitFor(async () => {
      await result.current.mutateAsync({ taskData: createRequest });
    });

    expect(mockProjectService.createTask).toHaveBeenCalledWith(createRequest);
    expect(mockDispatchEvent).toHaveBeenCalledWith(
      expect.objectContaining({
        type: 'taskCreated',
        detail: { task: mockPersonalTask, isPersonalTask: true },
      })
    );
  });

  it('should create task with specified owner when useManagerEndpoint is true', async () => {
    const mockTask: TaskResponse = {
      id: 3,
      projectId: 123,
      ownerId: 2, // Different owner
      taskType: 'BUG',
      title: 'Assigned Task',
      status: 'TODO',
      userHasEditAccess: true,
      userHasDeleteAccess: true,
      createdAt: '2025-01-01T00:00:00Z',
      createdBy: 1,
    };

    mockProjectService.createTaskWithSpecifiedOwner.mockResolvedValue(mockTask);

    const { result } = renderHook(() => useCreateTask(), {
      wrapper: createWrapper(),
    });

    const createRequest: CreateTaskRequest = {
      projectId: 123,
      ownerId: 2,
      title: 'Assigned Task',
      taskType: 'BUG',
    };

    await waitFor(async () => {
      await result.current.mutateAsync({ taskData: createRequest, useManagerEndpoint: true });
    });

    expect(mockProjectService.createTaskWithSpecifiedOwner).toHaveBeenCalledWith(createRequest);
  });

  it('should handle creation errors', async () => {
    const error = new Error('Creation failed');
    mockProjectService.createTask.mockRejectedValue(error);

    const { result } = renderHook(() => useCreateTask(), {
      wrapper: createWrapper(),
    });

    const createRequest: CreateTaskRequest = {
      projectId: 123,
      ownerId: 1,
      title: 'Test Task',
      taskType: 'FEATURE',
    };

    await expect(result.current.mutateAsync({ taskData: createRequest })).rejects.toThrow('Creation failed');
    expect(mockDispatchEvent).not.toHaveBeenCalled();
  });
});

describe('useUpdateTask', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockDispatchEvent.mockClear();
  });

  it('should update a project task successfully', async () => {
    const mockUpdatedTask: TaskResponse = {
      id: 1,
      projectId: 123,
      ownerId: 1,
      taskType: 'FEATURE',
      title: 'Updated Task',
      description: 'Updated Description',
      status: 'IN_PROGRESS',
      userHasEditAccess: true,
      userHasDeleteAccess: true,
      createdAt: '2025-01-01T00:00:00Z',
      updatedAt: '2025-01-02T00:00:00Z',
      createdBy: 1,
      updatedBy: 1,
    };

    mockProjectService.updateTask.mockResolvedValue(mockUpdatedTask);

    const { result } = renderHook(() => useUpdateTask(), {
      wrapper: createWrapper(),
    });

    const updateRequest: UpdateTaskRequest = {
      taskId: 1,
      title: 'Updated Task',
      description: 'Updated Description',
      status: 'IN_PROGRESS',
    };

    await waitFor(async () => {
      await result.current.mutateAsync(updateRequest);
    });

    expect(mockProjectService.updateTask).toHaveBeenCalledWith(updateRequest);
    expect(mockDispatchEvent).toHaveBeenCalledWith(
      expect.objectContaining({
        type: 'taskUpdated',
        detail: { task: mockUpdatedTask, isPersonalTask: false },
      })
    );
  });

  it('should update a personal task successfully', async () => {
    const mockUpdatedPersonalTask: TaskResponse = {
      id: 2,
      projectId: 0, // Personal task
      ownerId: 1,
      taskType: 'CHORE',
      title: 'Updated Personal Task',
      status: 'COMPLETED',
      userHasEditAccess: true,
      userHasDeleteAccess: true,
      createdAt: '2025-01-01T00:00:00Z',
      updatedAt: '2025-01-02T00:00:00Z',
      createdBy: 1,
      updatedBy: 1,
    };

    mockProjectService.updateTask.mockResolvedValue(mockUpdatedPersonalTask);

    const { result } = renderHook(() => useUpdateTask(), {
      wrapper: createWrapper(),
    });

    const updateRequest: UpdateTaskRequest = {
      taskId: 2,
      title: 'Updated Personal Task',
      status: 'COMPLETED',
    };

    await waitFor(async () => {
      await result.current.mutateAsync(updateRequest);
    });

    expect(mockProjectService.updateTask).toHaveBeenCalledWith(updateRequest);
    expect(mockDispatchEvent).toHaveBeenCalledWith(
      expect.objectContaining({
        type: 'taskUpdated',
        detail: { task: mockUpdatedPersonalTask, isPersonalTask: true },
      })
    );
  });

  it('should handle update errors', async () => {
    const error = new Error('Update failed');
    mockProjectService.updateTask.mockRejectedValue(error);

    const { result } = renderHook(() => useUpdateTask(), {
      wrapper: createWrapper(),
    });

    const updateRequest: UpdateTaskRequest = {
      taskId: 1,
      title: 'Updated Task',
    };

    await expect(result.current.mutateAsync(updateRequest)).rejects.toThrow('Update failed');
    expect(mockDispatchEvent).not.toHaveBeenCalled();
  });
});

describe('useDeleteTask', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockDispatchEvent.mockClear();
  });

  it('should delete a task successfully', async () => {
    mockProjectService.deleteTask.mockResolvedValue();

    const { result } = renderHook(() => useDeleteTask(), {
      wrapper: createWrapper(),
    });

    const taskId = 1;

    await waitFor(async () => {
      await result.current.mutateAsync(taskId);
    });

    expect(mockProjectService.deleteTask).toHaveBeenCalledWith(taskId);
    expect(mockDispatchEvent).toHaveBeenCalledWith(
      expect.objectContaining({
        type: 'taskDeleted',
        detail: { taskId },
      })
    );
  });

  it('should handle delete errors and not dispatch event', async () => {
    const error = new Error('Delete failed');
    mockProjectService.deleteTask.mockRejectedValue(error);

    const { result } = renderHook(() => useDeleteTask(), {
      wrapper: createWrapper(),
    });

    const taskId = 1;

    await expect(result.current.mutateAsync(taskId)).rejects.toThrow('Delete failed');
    expect(mockDispatchEvent).not.toHaveBeenCalled();
  });
});

describe('Cache Invalidation and Events', () => {
  it('should invalidate calendar queries for personal tasks', async () => {
    const queryClient = new QueryClient();
    const invalidateQueriesSpy = jest.spyOn(queryClient, 'invalidateQueries');

    const mockPersonalTask: TaskResponse = {
      id: 1,
      projectId: 0,
      ownerId: 1,
      taskType: 'CHORE',
      title: 'Personal Task',
      status: 'TODO',
      userHasEditAccess: true,
      userHasDeleteAccess: true,
      createdAt: '2025-01-01T00:00:00Z',
      createdBy: 1,
    };

    mockProjectService.createTask.mockResolvedValue(mockPersonalTask);

    const wrapper = ({ children }: { children: React.ReactNode }) =>
      React.createElement(QueryClientProvider, { client: queryClient }, children);

    const { result } = renderHook(() => useCreateTask(), { wrapper });

    await waitFor(async () => {
      await result.current.mutateAsync({
        taskData: {
          projectId: 0,
          ownerId: 1,
          title: 'Personal Task',
          taskType: 'CHORE',
        }
      });
    });

    // Verify that invalidateQueries was called for calendar tasks
    expect(invalidateQueriesSpy).toHaveBeenCalledWith(
      expect.objectContaining({
        queryKey: ['calendar-tasks'],
      })
    );

    // Verify that the custom event was dispatched
    expect(mockDispatchEvent).toHaveBeenCalledWith(
      expect.objectContaining({
        type: 'taskCreated',
        detail: { task: mockPersonalTask, isPersonalTask: true },
      })
    );
  });

  it('should differentiate between personal and project tasks', async () => {
    const projectTask: TaskResponse = {
      id: 1,
      projectId: 123,
      ownerId: 1,
      taskType: 'FEATURE',
      title: 'Project Task',
      status: 'TODO',
      userHasEditAccess: true,
      userHasDeleteAccess: true,
      createdAt: '2025-01-01T00:00:00Z',
      createdBy: 1,
    };

    const personalTask: TaskResponse = {
      id: 2,
      projectId: 0,
      ownerId: 1,
      taskType: 'CHORE',
      title: 'Personal Task',
      status: 'TODO',
      userHasEditAccess: true,
      userHasDeleteAccess: true,
      createdAt: '2025-01-01T00:00:00Z',
      createdBy: 1,
    };

    mockProjectService.createTask
      .mockResolvedValueOnce(projectTask)
      .mockResolvedValueOnce(personalTask);

    const { result } = renderHook(() => useCreateTask(), {
      wrapper: createWrapper(),
    });

    // Create project task
    await waitFor(async () => {
      await result.current.mutateAsync({
        taskData: {
          projectId: 123,
          ownerId: 1,
          title: 'Project Task',
          taskType: 'FEATURE',
        }
      });
    });

    expect(mockDispatchEvent).toHaveBeenCalledWith(
      expect.objectContaining({
        detail: { task: projectTask, isPersonalTask: false },
      })
    );

    mockDispatchEvent.mockClear();

    // Create personal task
    await waitFor(async () => {
      await result.current.mutateAsync({
        taskData: {
          projectId: 0,
          ownerId: 1,
          title: 'Personal Task',
          taskType: 'CHORE',
        }
      });
    });

    expect(mockDispatchEvent).toHaveBeenCalledWith(
      expect.objectContaining({
        detail: { task: personalTask, isPersonalTask: true },
      })
    );
  });
});
