import { test, expect } from '../../../fixtures';
import { TaskStatus } from '../../../fixtures/data/task-data-types';

test.describe('List View Status Updates @integration @task-status', () => {
  test.beforeEach(async ({ tasksPage, taskCleanup }) => {
    await tasksPage.navigate();
  });

  test.afterEach(async ({ taskCleanup }) => {
    await taskCleanup.cleanupAll();
  });

  test('should update task status via dropdown in list view', async ({ 
    staffPage, 
    tasksPage, 
    taskDataFactory, 
    taskCleanup 
  }) => {
    // Create a test task
    const taskData = taskDataFactory.createStandaloneTask();
    taskData.status = TaskStatus.TODO;
    
    const taskId = await taskDataFactory.createTaskViaAPI(taskData);
    await taskCleanup.trackTask(taskId);

    // Navigate to tasks list view
    await tasksPage.navigate();
    await tasksPage.assertTasksLoaded();

    // Verify initial status
    await tasksPage.assertTaskStatus(taskId, TaskStatus.TODO);

    // Update status via dropdown
    await tasksPage.updateTaskStatus(taskId, TaskStatus.IN_PROGRESS);

    // Verify status updated in list view
    await tasksPage.assertTaskStatus(taskId, TaskStatus.IN_PROGRESS);
  });

  test('should propagate status changes to board and detail views', async ({ 
    staffPage, 
    tasksPage, 
    kanbanBoardPage, 
    taskDetailPage, 
    taskDataFactory, 
    taskCleanup 
  }) => {
    // Create a test task
    const taskData = taskDataFactory.createStandaloneTask();
    taskData.status = TaskStatus.TODO;
    
    const taskId = await taskDataFactory.createTaskViaAPI(taskData);
    await taskCleanup.trackTask(taskId);

    // Update status in list view
    await tasksPage.navigate();
    await tasksPage.assertTasksLoaded();
    await tasksPage.updateTaskStatus(taskId, TaskStatus.COMPLETED);

    // Verify status in Kanban board view
    await kanbanBoardPage.navigate();
    await kanbanBoardPage.assertBoardLoaded();
    await kanbanBoardPage.assertTaskInColumn(taskId, TaskStatus.COMPLETED);

    // Verify status in detail view
    await taskDetailPage.navigate(taskId);
    await taskDetailPage.assertTaskStatus(TaskStatus.COMPLETED);

    // Return to list view and verify status persisted
    await tasksPage.navigate();
    await tasksPage.assertTaskStatus(taskId, TaskStatus.COMPLETED);
  });

  test('should update multiple task statuses in sequence', async ({ 
    staffPage, 
    tasksPage, 
    taskDataFactory, 
    taskCleanup 
  }) => {
    // Create multiple test tasks
    const task1Data = taskDataFactory.createStandaloneTask();
    task1Data.status = TaskStatus.TODO;
    task1Data.title = 'Task 1 for Status Update';
    const task1Id = await taskDataFactory.createTaskViaAPI(task1Data);
    await taskCleanup.trackTask(task1Id);

    const task2Data = taskDataFactory.createStandaloneTask();
    task2Data.status = TaskStatus.TODO;
    task2Data.title = 'Task 2 for Status Update';
    const task2Id = await taskDataFactory.createTaskViaAPI(task2Data);
    await taskCleanup.trackTask(task2Id);

    // Navigate to tasks list view
    await tasksPage.navigate();
    await tasksPage.assertTasksLoaded();

    // Update first task status
    await tasksPage.updateTaskStatus(task1Id, TaskStatus.IN_PROGRESS);
    await tasksPage.assertTaskStatus(task1Id, TaskStatus.IN_PROGRESS);

    // Update second task status
    await tasksPage.updateTaskStatus(task2Id, TaskStatus.BLOCKED);
    await tasksPage.assertTaskStatus(task2Id, TaskStatus.BLOCKED);

    // Verify both tasks maintain their updated statuses
    await tasksPage.assertTaskStatus(task1Id, TaskStatus.IN_PROGRESS);
    await tasksPage.assertTaskStatus(task2Id, TaskStatus.BLOCKED);
  });

  test('should handle all status transitions in list view', async ({ 
    staffPage, 
    tasksPage, 
    taskDataFactory, 
    taskCleanup 
  }) => {
    // Create a test task
    const taskData = taskDataFactory.createStandaloneTask();
    taskData.status = TaskStatus.TODO;
    
    const taskId = await taskDataFactory.createTaskViaAPI(taskData);
    await taskCleanup.trackTask(taskId);

    // Navigate to tasks list view
    await tasksPage.navigate();
    await tasksPage.assertTasksLoaded();

    // Test all status transitions
    const statusTransitions = [
      { from: TaskStatus.TODO, to: TaskStatus.IN_PROGRESS },
      { from: TaskStatus.IN_PROGRESS, to: TaskStatus.BLOCKED },
      { from: TaskStatus.BLOCKED, to: TaskStatus.COMPLETED },
      { from: TaskStatus.COMPLETED, to: TaskStatus.TODO }
    ];

    for (const transition of statusTransitions) {
      // Verify current status
      await tasksPage.assertTaskStatus(taskId, transition.from);
      
      // Update to new status
      await tasksPage.updateTaskStatus(taskId, transition.to);
      
      // Verify status changed
      await tasksPage.assertTaskStatus(taskId, transition.to);
    }
  });

  test('should update status counts after list view changes', async ({ 
    staffPage, 
    tasksPage, 
    taskDataFactory, 
    taskCleanup 
  }) => {
    // Create tasks with different statuses
    const todoTask = taskDataFactory.createStandaloneTask();
    todoTask.status = TaskStatus.TODO;
    const todoTaskId = await taskDataFactory.createTaskViaAPI(todoTask);
    await taskCleanup.trackTask(todoTaskId);

    const inProgressTask = taskDataFactory.createStandaloneTask();
    inProgressTask.status = TaskStatus.IN_PROGRESS;
    const inProgressTaskId = await taskDataFactory.createTaskViaAPI(inProgressTask);
    await taskCleanup.trackTask(inProgressTaskId);

    // Navigate to tasks list view
    await tasksPage.navigate();
    await tasksPage.assertTasksLoaded();

    // Get initial status counts
    const initialCounts = await tasksPage.getTaskStatusCounts();

    // Update a task status
    await tasksPage.updateTaskStatus(todoTaskId, TaskStatus.COMPLETED);

    // Get updated status counts
    const updatedCounts = await tasksPage.getTaskStatusCounts();

    // Verify counts changed appropriately
    expect(updatedCounts.todo).toBe(initialCounts.todo - 1);
    expect(updatedCounts.completed).toBe(initialCounts.completed + 1);
    expect(updatedCounts.inProgress).toBe(initialCounts.inProgress);
  });

  test('should show immediate visual feedback on status change', async ({ 
    staffPage, 
    tasksPage, 
    taskDataFactory, 
    taskCleanup 
  }) => {
    // Create a test task
    const taskData = taskDataFactory.createStandaloneTask();
    taskData.status = TaskStatus.TODO;
    
    const taskId = await taskDataFactory.createTaskViaAPI(taskData);
    await taskCleanup.trackTask(taskId);

    // Navigate to tasks list view
    await tasksPage.navigate();
    await tasksPage.assertTasksLoaded();

    // Update status and verify immediate feedback
    await tasksPage.updateTaskStatus(taskId, TaskStatus.IN_PROGRESS);
    
    // Verify status updated immediately without page refresh
    await tasksPage.assertTaskStatus(taskId, TaskStatus.IN_PROGRESS);
    
    // Verify task is still visible in the list
    await tasksPage.assertTaskVisible(taskId);
  });

  test('should maintain task position in list after status update', async ({ 
    staffPage, 
    tasksPage, 
    taskDataFactory, 
    taskCleanup 
  }) => {
    // Create multiple tasks to test positioning
    const tasks = [];
    for (let i = 1; i <= 3; i++) {
      const taskData = taskDataFactory.createStandaloneTask();
      taskData.status = TaskStatus.TODO;
      taskData.title = `Position Test Task ${i}`;
      const taskId = await taskDataFactory.createTaskViaAPI(taskData);
      await taskCleanup.trackTask(taskId);
      tasks.push(taskId);
    }

    // Navigate to tasks list view
    await tasksPage.navigate();
    await tasksPage.assertTasksLoaded();

    // Get initial task list
    const initialTaskList = await tasksPage.getTaskList();
    const initialTaskIds = initialTaskList.map(task => task.id);

    // Update middle task status
    await tasksPage.updateTaskStatus(tasks[1], TaskStatus.IN_PROGRESS);

    // Get updated task list
    const updatedTaskList = await tasksPage.getTaskList();
    
    // Verify all tasks are still present
    for (const taskId of tasks) {
      const taskExists = updatedTaskList.some(task => task.id === taskId);
      expect(taskExists).toBe(true);
    }

    // Verify the updated task has the correct status
    const updatedTask = updatedTaskList.find(task => task.id === tasks[1]);
    expect(updatedTask?.status).toBe(TaskStatus.IN_PROGRESS);
  });

  test('should handle rapid consecutive status updates', async ({ 
    staffPage, 
    tasksPage, 
    taskDataFactory, 
    taskCleanup 
  }) => {
    // Create a test task
    const taskData = taskDataFactory.createStandaloneTask();
    taskData.status = TaskStatus.TODO;
    
    const taskId = await taskDataFactory.createTaskViaAPI(taskData);
    await taskCleanup.trackTask(taskId);

    // Navigate to tasks list view
    await tasksPage.navigate();
    await tasksPage.assertTasksLoaded();

    // Perform rapid status updates
    await tasksPage.updateTaskStatus(taskId, TaskStatus.IN_PROGRESS);
    await tasksPage.updateTaskStatus(taskId, TaskStatus.BLOCKED);
    await tasksPage.updateTaskStatus(taskId, TaskStatus.COMPLETED);

    // Verify final status is correct
    await tasksPage.assertTaskStatus(taskId, TaskStatus.COMPLETED);
  });
});