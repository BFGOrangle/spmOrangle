import { test, expect } from '../../../fixtures';
import { TaskStatus } from '../../../fixtures/data/task-data-types';

test.describe('Task Detail Status Updates @integration @task-status', () => {
  test.beforeEach(async ({ tasksPage, taskCleanup }) => {
    await tasksPage.navigate();
  });

  test.afterEach(async ({ taskCleanup }) => {
    await taskCleanup.cleanupAll();
  });

  test('should update task status from detail page', async ({ 
    staffPage, 
    taskDetailPage, 
    taskDataFactory, 
    taskCleanup 
  }) => {
    // Create a test task
    const taskData = taskDataFactory.createStandaloneTask();
    taskData.status = TaskStatus.TODO;
    
    const taskId = await taskDataFactory.createTaskViaAPI(taskData);
    await taskCleanup.trackTask(taskId);

    // Navigate to task detail page
    await taskDetailPage.navigate(taskId);

    // Verify initial status
    await taskDetailPage.assertTaskStatus(TaskStatus.TODO);

    // Update status
    await taskDetailPage.updateStatus(TaskStatus.IN_PROGRESS);

    // Verify status updated
    await taskDetailPage.assertTaskStatus(TaskStatus.IN_PROGRESS);
  });

  test('should save status changes and reflect in all views', async ({ 
    staffPage, 
    taskDetailPage, 
    tasksPage, 
    kanbanBoardPage, 
    taskDataFactory, 
    taskCleanup 
  }) => {
    // Create a test task
    const taskData = taskDataFactory.createStandaloneTask();
    taskData.status = TaskStatus.TODO;
    
    const taskId = await taskDataFactory.createTaskViaAPI(taskData);
    await taskCleanup.trackTask(taskId);

    // Update status in detail view
    await taskDetailPage.navigate(taskId);
    await taskDetailPage.updateStatus(TaskStatus.COMPLETED);

    // Verify status in list view
    await tasksPage.navigate();
    await tasksPage.assertTaskStatus(taskId, TaskStatus.COMPLETED);

    // Verify status in Kanban board view
    await kanbanBoardPage.navigate();
    await kanbanBoardPage.assertBoardLoaded();
    await kanbanBoardPage.assertTaskInColumn(taskId, TaskStatus.COMPLETED);

    // Return to detail view and verify status persisted
    await taskDetailPage.navigate(taskId);
    await taskDetailPage.assertTaskStatus(TaskStatus.COMPLETED);
  });

  test('should handle all status transitions in detail view', async ({ 
    staffPage, 
    taskDetailPage, 
    taskDataFactory, 
    taskCleanup 
  }) => {
    // Create a test task
    const taskData = taskDataFactory.createStandaloneTask();
    taskData.status = TaskStatus.TODO;
    
    const taskId = await taskDataFactory.createTaskViaAPI(taskData);
    await taskCleanup.trackTask(taskId);

    // Navigate to task detail page
    await taskDetailPage.navigate(taskId);

    // Test all status transitions
    const statusTransitions = [
      { from: TaskStatus.TODO, to: TaskStatus.IN_PROGRESS },
      { from: TaskStatus.IN_PROGRESS, to: TaskStatus.BLOCKED },
      { from: TaskStatus.BLOCKED, to: TaskStatus.COMPLETED },
      { from: TaskStatus.COMPLETED, to: TaskStatus.TODO }
    ];

    for (const transition of statusTransitions) {
      // Verify current status
      await taskDetailPage.assertTaskStatus(transition.from);
      
      // Update to new status
      await taskDetailPage.updateStatus(transition.to);
      
      // Verify status changed
      await taskDetailPage.assertTaskStatus(transition.to);
    }
  });

  test('should show immediate feedback on status update', async ({ 
    staffPage, 
    taskDetailPage, 
    taskDataFactory, 
    taskCleanup 
  }) => {
    // Create a test task
    const taskData = taskDataFactory.createStandaloneTask();
    taskData.status = TaskStatus.TODO;
    
    const taskId = await taskDataFactory.createTaskViaAPI(taskData);
    await taskCleanup.trackTask(taskId);

    // Navigate to task detail page
    await taskDetailPage.navigate(taskId);

    // Update status and verify immediate feedback
    await taskDetailPage.updateStatus(TaskStatus.IN_PROGRESS);
    
    // Verify status updated immediately
    await taskDetailPage.assertTaskStatus(TaskStatus.IN_PROGRESS);
  });

  test('should update status while in edit mode', async ({ 
    staffPage, 
    taskDetailPage, 
    taskDataFactory, 
    taskCleanup 
  }) => {
    // Create a test task
    const taskData = taskDataFactory.createStandaloneTask();
    taskData.status = TaskStatus.TODO;
    
    const taskId = await taskDataFactory.createTaskViaAPI(taskData);
    await taskCleanup.trackTask(taskId);

    // Navigate to task detail page
    await taskDetailPage.navigate(taskId);

    // Enter edit mode
    await taskDetailPage.enterEditMode();

    // Update status while in edit mode
    await taskDetailPage.updateStatus(TaskStatus.IN_PROGRESS);

    // Verify status updated
    await taskDetailPage.assertTaskStatus(TaskStatus.IN_PROGRESS);

    // Exit edit mode and verify status persisted
    await taskDetailPage.exitEditMode();
    await taskDetailPage.assertTaskStatus(TaskStatus.IN_PROGRESS);
  });

  test('should maintain other task properties when updating status', async ({ 
    staffPage, 
    taskDetailPage, 
    taskDataFactory, 
    taskCleanup 
  }) => {
    // Create a test task with multiple properties
    const taskData = taskDataFactory.createHighPriorityTask();
    taskData.status = TaskStatus.TODO;
    
    const taskId = await taskDataFactory.createTaskViaAPI(taskData);
    await taskCleanup.trackTask(taskId);

    // Navigate to task detail page
    await taskDetailPage.navigate(taskId);

    // Verify initial properties
    await taskDetailPage.assertTaskTitle(taskData.title);
    await taskDetailPage.assertTaskPriority(taskData.priority);

    // Update only the status
    await taskDetailPage.updateStatus(TaskStatus.COMPLETED);

    // Verify status updated but other properties maintained
    await taskDetailPage.assertTaskStatus(TaskStatus.COMPLETED);
    await taskDetailPage.assertTaskTitle(taskData.title);
    await taskDetailPage.assertTaskPriority(taskData.priority);
  });

  test('should handle status updates with subtasks present', async ({ 
    staffPage, 
    taskDetailPage, 
    taskDataFactory, 
    taskCleanup 
  }) => {
    // Create a test task
    const taskData = taskDataFactory.createTaskWithSubtasks(3);
    taskData.status = TaskStatus.IN_PROGRESS;
    
    const taskId = await taskDataFactory.createTaskViaAPI(taskData);
    await taskCleanup.trackTask(taskId);

    // Navigate to task detail page
    await taskDetailPage.navigate(taskId);

    // Expand subtasks to see rollup
    await taskDetailPage.expandSubtasks();

    // Update parent task status
    await taskDetailPage.updateStatus(TaskStatus.COMPLETED);

    // Verify parent status updated
    await taskDetailPage.assertTaskStatus(TaskStatus.COMPLETED);

    // Verify subtask rollup is still visible and functional
    const subtaskCount = await taskDetailPage.getSubtaskCount();
    expect(subtaskCount).toBeGreaterThan(0);
  });

  test('should handle concurrent status updates gracefully', async ({ 
    staffPage, 
    taskDetailPage, 
    taskDataFactory, 
    taskCleanup 
  }) => {
    // Create a test task
    const taskData = taskDataFactory.createStandaloneTask();
    taskData.status = TaskStatus.TODO;
    
    const taskId = await taskDataFactory.createTaskViaAPI(taskData);
    await taskCleanup.trackTask(taskId);

    // Navigate to task detail page
    await taskDetailPage.navigate(taskId);

    // Perform rapid status updates
    await taskDetailPage.updateStatus(TaskStatus.IN_PROGRESS);
    await taskDetailPage.updateStatus(TaskStatus.BLOCKED);
    await taskDetailPage.updateStatus(TaskStatus.COMPLETED);

    // Verify final status is correct
    await taskDetailPage.assertTaskStatus(TaskStatus.COMPLETED);
  });

  test('should preserve status after page refresh', async ({ 
    staffPage, 
    taskDetailPage, 
    taskDataFactory, 
    taskCleanup 
  }) => {
    // Create a test task
    const taskData = taskDataFactory.createStandaloneTask();
    taskData.status = TaskStatus.TODO;
    
    const taskId = await taskDataFactory.createTaskViaAPI(taskData);
    await taskCleanup.trackTask(taskId);

    // Navigate to task detail page and update status
    await taskDetailPage.navigate(taskId);
    await taskDetailPage.updateStatus(TaskStatus.IN_PROGRESS);

    // Refresh the page
    await taskDetailPage.reload();

    // Verify status persisted after refresh
    await taskDetailPage.assertTaskStatus(TaskStatus.IN_PROGRESS);
  });

  test('should show status in task breadcrumb or header', async ({ 
    staffPage, 
    taskDetailPage, 
    taskDataFactory, 
    taskCleanup 
  }) => {
    // Create a test task
    const taskData = taskDataFactory.createStandaloneTask();
    taskData.status = TaskStatus.TODO;
    
    const taskId = await taskDataFactory.createTaskViaAPI(taskData);
    await taskCleanup.trackTask(taskId);

    // Navigate to task detail page
    await taskDetailPage.navigate(taskId);

    // Update status
    await taskDetailPage.updateStatus(TaskStatus.BLOCKED);

    // Verify status is visible in the page (header, breadcrumb, or status indicator)
    await taskDetailPage.assertTaskStatus(TaskStatus.BLOCKED);
  });
});