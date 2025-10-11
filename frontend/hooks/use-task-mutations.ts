/**
 * Custom hooks for task mutations with automatic cache invalidation
 */

import { useMutation, useQueryClient } from '@tanstack/react-query';
import { projectService, CreateTaskRequest, UpdateTaskRequest, TaskResponse } from '@/services/project-service';

/**
 * Helper function to invalidate all task-related queries
 */
const invalidateAllTaskQueries = (queryClient: any) => {
  // Invalidate ALL variations of calendar-tasks queries
  queryClient.invalidateQueries({ 
    queryKey: ['calendar-tasks'],
    exact: false,
    refetchType: 'all' // Force refetch of both active and inactive queries
  });
  
  // Invalidate projects
  queryClient.invalidateQueries({ 
    queryKey: ['projects'],
    refetchType: 'all'
  });
  
  // Invalidate personal tasks
  queryClient.invalidateQueries({ 
    queryKey: ['personal-tasks'],
    refetchType: 'all'
  });
  
  // Invalidate all project tasks
  queryClient.invalidateQueries({ 
    queryKey: ['project-tasks'],
    exact: false,
    refetchType: 'all'
  });
};

/**
 * Custom hook for creating tasks with automatic cache invalidation
 */
export function useCreateTask() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (data: { taskData: CreateTaskRequest; useManagerEndpoint?: boolean }) => {
      if (data.useManagerEndpoint) {
        return await projectService.createTaskWithSpecifiedOwner(data.taskData);
      } else {
        return await projectService.createTask(data.taskData);
      }
    },
    onSuccess: (newTask: TaskResponse) => {
      console.log('=== TASK CREATION SUCCESS ===');
      console.log('Task created successfully, invalidating caches for new taskId:', newTask.id);
      console.log('Task details:', newTask);
      const isPersonalTask = !newTask.projectId || newTask.projectId === 0;
      console.log('Is personal task?', isPersonalTask);
      
      // Use the helper function to invalidate all task-related queries
      invalidateAllTaskQueries(queryClient);
      
      // If it's a personal task, also directly invalidate personal task queries
      if (isPersonalTask) {
        console.log('Invalidating personal task queries specifically...');
        // Force refetch of personal tasks by invalidating with specific user ID patterns
        queryClient.invalidateQueries({ 
          queryKey: ['calendar-tasks'],
          predicate: (query) => {
            const key = query.queryKey;
            // Invalidate calendar-tasks queries that might contain personal tasks
            return Array.isArray(key) && key[0] === 'calendar-tasks' && key[2] === 'Personal Tasks';
          }
        });
      }
      
      // Dispatch custom event for calendar to listen to
      console.log('Dispatching taskCreated event...');
      window.dispatchEvent(new CustomEvent('taskCreated', { 
        detail: { task: newTask, isPersonalTask } 
      }));
      
      console.log('Cache invalidation completed for task creation + custom event dispatched');
      console.log('=== END TASK CREATION ===');
    },
  });
}

/**
 * Custom hook for updating tasks with automatic cache invalidation
 */
export function useUpdateTask() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (updateRequest: UpdateTaskRequest) => {
      return await projectService.updateTask(updateRequest);
    },
    onSuccess: (updatedTask: TaskResponse) => {
      console.log('Task updated successfully, invalidating caches for taskId:', updatedTask.id);
      const isPersonalTask = !updatedTask.projectId || updatedTask.projectId === 0;
      console.log('Is personal task?', isPersonalTask);
      
      // Use the helper function to invalidate all task-related queries
      invalidateAllTaskQueries(queryClient);

      // Update the specific task in cache if it exists
      queryClient.setQueryData(['task', updatedTask.id], updatedTask);
      
      // If it's a personal task, also directly invalidate personal task queries
      if (isPersonalTask) {
        console.log('Invalidating personal task queries specifically...');
        // Force refetch of personal tasks by invalidating with specific user ID patterns
        queryClient.invalidateQueries({ 
          queryKey: ['calendar-tasks'],
          predicate: (query) => {
            const key = query.queryKey;
            // Invalidate calendar-tasks queries that might contain personal tasks
            return Array.isArray(key) && key[0] === 'calendar-tasks' && key[2] === 'Personal Tasks';
          }
        });
      }
      
      // Dispatch custom event for calendar to listen to
      window.dispatchEvent(new CustomEvent('taskUpdated', { 
        detail: { task: updatedTask, isPersonalTask } 
      }));
      
      console.log('Cache invalidation completed for task update + custom event dispatched');
    },
  });
}

/**
 * Custom hook for deleting tasks with automatic cache invalidation
 */
export function useDeleteTask() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (taskId: number) => {
      return await projectService.deleteTask(taskId);
    },
    onSuccess: (_, taskId) => {
      console.log('Task deleted successfully, invalidating caches for taskId:', taskId);
      
      // Remove the specific task from cache first
      queryClient.removeQueries({ queryKey: ['task', taskId] });
      
      // TARGETED APPROACH: Focus specifically on calendar queries
      console.log('Starting targeted cache invalidation for delete...');
      
      // Get all queries in cache to see what we're working with
      const allQueries = queryClient.getQueryCache().getAll();
      const calendarQueries = allQueries.filter(query => 
        Array.isArray(query.queryKey) && query.queryKey[0] === 'calendar-tasks'
      );
      
      console.log('Found calendar queries:', calendarQueries.map(q => q.queryKey));
      
      // Remove each calendar query individually
      calendarQueries.forEach(query => {
        console.log('Removing query:', query.queryKey);
        queryClient.removeQueries({ queryKey: query.queryKey });
      });
      
      // Force invalidation with refetch
      queryClient.invalidateQueries({ 
        queryKey: ['calendar-tasks'],
        exact: false,
        refetchType: 'all'
      });
      
      // Dispatch custom event for calendar to listen to
      window.dispatchEvent(new CustomEvent('taskDeleted', { 
        detail: { taskId } 
      }));
      
      console.log('Targeted cache invalidation completed + custom event dispatched');
    },
    onError: (error) => {
      console.error('Failed to delete task:', error);
      // Don't invalidate cache if deletion failed
    },
  });
}
