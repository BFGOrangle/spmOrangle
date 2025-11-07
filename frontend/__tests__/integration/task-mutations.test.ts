import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import React from 'react';
import { useCreateTask, useUpdateTask, useDeleteTask } from '@/hooks/use-task-mutations';
import { projectService } from '@/services/project-service';

// Mock the project service
jest.mock('@/services/project-service');

const mockProjectService = projectService as jest.Mocked<typeof projectService>;

// Mock console methods to avoid test output noise
const originalConsoleLog = console.log;
const originalConsoleError = console.error;

beforeAll(() => {
  console.log = jest.fn();
  console.error = jest.fn();
});

afterAll(() => {
  console.log = originalConsoleLog;
  console.error = originalConsoleError;
});

// Mock window.dispatchEvent
const mockDispatchEvent = jest.fn();
Object.defineProperty(window, 'dispatchEvent', {
  value: mockDispatchEvent,
  writable: true,
});

describe('Task Mutations Integration', () => {
  let queryClient: QueryClient;

  beforeEach(() => {
    queryClient = new QueryClient({
      defaultOptions: {
        queries: { retry: false },
        mutations: { retry: false },
      },
    });
    
    jest.clearAllMocks();
    mockDispatchEvent.mockClear();
  });

  const createWrapper = () => ({ children }: { children: React.ReactNode }) =>
    React.createElement(QueryClientProvider, { client: queryClient }, children);

  describe('Personal Task Workflow', () => {
    it('should handle complete personal task lifecycle (create, update, delete)', async () => {
      // Mock responses
      const createdTask = {
        id: 1,
        projectId: 0,
        ownerId: 1,
        taskType: 'CHORE' as const,
        title: 'Personal Task',
        status: 'TODO' as const,
        userHasWriteAccess: true,
        userHasDeleteAccess: true,
        createdAt: '2025-01-01T00:00:00Z',
        createdBy: 1,
      };

      const updatedTask = {
        ...createdTask,
        title: 'Updated Personal Task',
        status: 'COMPLETED' as const,
        updatedAt: '2025-01-02T00:00:00Z',
      };

      mockProjectService.createTask.mockResolvedValue(createdTask);
      mockProjectService.updateTask.mockResolvedValue(updatedTask);
      mockProjectService.deleteTask.mockResolvedValue();

      // Test create
      const { result: createResult } = renderHook(() => useCreateTask(), { wrapper: createWrapper() });

      await waitFor(async () => {
        await createResult.current.mutateAsync({
          taskData: {
            projectId: 0,
            ownerId: 1,
            title: 'Personal Task',
            taskType: 'CHORE',
          }
        });
      });

      expect(mockProjectService.createTask).toHaveBeenCalledWith({
        projectId: 0,
        ownerId: 1,
        title: 'Personal Task',
        taskType: 'CHORE',
      });

      expect(mockDispatchEvent).toHaveBeenCalledWith(
        expect.objectContaining({
          type: 'taskCreated',
          detail: { task: createdTask, isPersonalTask: true },
        })
      );

      // Test update
      const { result: updateResult } = renderHook(() => useUpdateTask(), { wrapper: createWrapper() });

      await waitFor(async () => {
        await updateResult.current.mutateAsync({
          taskId: 1,
          title: 'Updated Personal Task',
          status: 'COMPLETED',
        });
      });

      expect(mockProjectService.updateTask).toHaveBeenCalledWith({
        taskId: 1,
        title: 'Updated Personal Task',
        status: 'COMPLETED',
      });

      expect(mockDispatchEvent).toHaveBeenCalledWith(
        expect.objectContaining({
          type: 'taskUpdated',
          detail: { task: updatedTask, isPersonalTask: true },
        })
      );

      // Test delete
      const { result: deleteResult } = renderHook(() => useDeleteTask(), { wrapper: createWrapper() });

      await waitFor(async () => {
        await deleteResult.current.mutateAsync(1);
      });

      expect(mockProjectService.deleteTask).toHaveBeenCalledWith(1);
      expect(mockDispatchEvent).toHaveBeenCalledWith(
        expect.objectContaining({
          type: 'taskDeleted',
          detail: { taskId: 1 },
        })
      );
    });
  });

  describe('Project Task Workflow', () => {
    it('should handle complete project task lifecycle (create, update, delete)', async () => {
      // Mock responses
      const createdTask = {
        id: 2,
        projectId: 123,
        ownerId: 1,
        taskType: 'FEATURE' as const,
        title: 'Project Task',
        status: 'TODO' as const,
        userHasWriteAccess: true,
        userHasDeleteAccess: true,
        createdAt: '2025-01-01T00:00:00Z',
        createdBy: 1,
      };

      const updatedTask = {
        ...createdTask,
        title: 'Updated Project Task',
        status: 'IN_PROGRESS' as const,
        updatedAt: '2025-01-02T00:00:00Z',
      };

      mockProjectService.createTask.mockResolvedValue(createdTask);
      mockProjectService.updateTask.mockResolvedValue(updatedTask);
      mockProjectService.deleteTask.mockResolvedValue();

      // Test create
      const { result: createResult } = renderHook(() => useCreateTask(), { wrapper: createWrapper() });

      await waitFor(async () => {
        await createResult.current.mutateAsync({
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
          type: 'taskCreated',
          detail: { task: createdTask, isPersonalTask: false },
        })
      );

      // Test update
      const { result: updateResult } = renderHook(() => useUpdateTask(), { wrapper: createWrapper() });

      await waitFor(async () => {
        await updateResult.current.mutateAsync({
          taskId: 2,
          title: 'Updated Project Task',
          status: 'IN_PROGRESS',
        });
      });

      expect(mockDispatchEvent).toHaveBeenCalledWith(
        expect.objectContaining({
          type: 'taskUpdated',
          detail: { task: updatedTask, isPersonalTask: false },
        })
      );

      // Test delete
      const { result: deleteResult } = renderHook(() => useDeleteTask(), { wrapper: createWrapper() });

      await waitFor(async () => {
        await deleteResult.current.mutateAsync(2);
      });

      expect(mockDispatchEvent).toHaveBeenCalledWith(
        expect.objectContaining({
          type: 'taskDeleted',
          detail: { taskId: 2 },
        })
      );
    });
  });

  describe('Error Handling', () => {
    it('should handle creation errors without dispatching events', async () => {
      const error = new Error('Network error');
      mockProjectService.createTask.mockRejectedValue(error);

      const { result } = renderHook(() => useCreateTask(), { wrapper: createWrapper() });

      await expect(
        result.current.mutateAsync({
          taskData: {
            projectId: 123,
            ownerId: 1,
            title: 'Test Task',
            taskType: 'BUG',
          }
        })
      ).rejects.toThrow('Network error');

      expect(mockDispatchEvent).not.toHaveBeenCalled();
    });

    it('should handle update errors without dispatching events', async () => {
      const error = new Error('Update failed');
      mockProjectService.updateTask.mockRejectedValue(error);

      const { result } = renderHook(() => useUpdateTask(), { wrapper: createWrapper() });

      await expect(
        result.current.mutateAsync({
          taskId: 1,
          title: 'Updated Task',
        })
      ).rejects.toThrow('Update failed');

      expect(mockDispatchEvent).not.toHaveBeenCalled();
    });

    it('should handle delete errors without dispatching events', async () => {
      const error = new Error('Delete failed');
      mockProjectService.deleteTask.mockRejectedValue(error);

      const { result } = renderHook(() => useDeleteTask(), { wrapper: createWrapper() });

      await expect(result.current.mutateAsync(1)).rejects.toThrow('Delete failed');

      expect(mockDispatchEvent).not.toHaveBeenCalled();
    });
  });

  describe('Cache Invalidation', () => {
    it('should call invalidateQueries for all task operations', async () => {
      const invalidateQueriesSpy = jest.spyOn(queryClient, 'invalidateQueries');

      const mockTask = {
        id: 1,
        projectId: 0,
        ownerId: 1,
        taskType: 'CHORE' as const,
        title: 'Test Task',
        status: 'TODO' as const,
        userHasWriteAccess: true,
        userHasDeleteAccess: true,
        createdAt: '2025-01-01T00:00:00Z',
        createdBy: 1,
      };

      mockProjectService.createTask.mockResolvedValue(mockTask);
      mockProjectService.updateTask.mockResolvedValue(mockTask);
      mockProjectService.deleteTask.mockResolvedValue();

      // Test create invalidation
      const { result: createResult } = renderHook(() => useCreateTask(), { wrapper: createWrapper() });
      await waitFor(async () => {
        await createResult.current.mutateAsync({
          taskData: {
            projectId: 0,
            ownerId: 1,
            title: 'Test Task',
            taskType: 'CHORE',
          }
        });
      });

      expect(invalidateQueriesSpy).toHaveBeenCalledWith(
        expect.objectContaining({
          queryKey: ['calendar-tasks'],
        })
      );

      // Test update invalidation
      const { result: updateResult } = renderHook(() => useUpdateTask(), { wrapper: createWrapper() });
      await waitFor(async () => {
        await updateResult.current.mutateAsync({
          taskId: 1,
          title: 'Updated Task',
        });
      });

      expect(invalidateQueriesSpy).toHaveBeenCalledWith(
        expect.objectContaining({
          queryKey: ['calendar-tasks'],
        })
      );

      // Test delete invalidation
      const { result: deleteResult } = renderHook(() => useDeleteTask(), { wrapper: createWrapper() });
      await waitFor(async () => {
        await deleteResult.current.mutateAsync(1);
      });

      expect(invalidateQueriesSpy).toHaveBeenCalled();
    });
  });
});
