import { test, expect } from '../../../fixtures';
import { TaskStatus } from '../../../fixtures/data/task-data-types';

test.describe('Task Status Management Integration @integration @task-management', () => {
  test.beforeEach(async ({ tasksPage, taskCleanup }) => {
    await tasksPage.navigate();
  });

  test.afterEach(async ({ taskCleanup }) => {
    await taskCleanup.cleanupAll();
  });

  test('should update status via Kanban board drag and drop', async ({ 
    staffPage, 
    kanbanBoardPage, 
    taskDataFactory, 
    taskCleanup 
  }) => {
    // Create a test task
    const taskData = taskDataFactory.createStandaloneTask();
    taskData.status = TaskStatus.TODO;
    
    const taskId = await taskDataFactory.createTaskViaAPI(taskData);
    await taskCleanup.trackTask(taskId);

    // Navigate to Kanban board and perform drag operation
    await kanbanBoardPage.navigate();
    await kanbanBoardPage.assertBoardLoaded();
    await kanbanBoardPage.dragTaskToColumn(taskId, TaskStatus.COMPLETED);

    // Verify task moved to correct column
    await kanbanBoardPage.assertTaskInColumn(taskId, TaskStatus.COMPLETED);
  });

  test('should update status via list view dropdown', async ({ 
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

    // Navigate to list view and update status
    await tasksPage.navigate();
    await tasksPage.assertTasksLoaded();
    await tasksPage.updateTaskStatus(taskId, TaskStatus.IN_PROGRESS);

    // Verify status updated
    await tasksPage.assertTaskStatus(taskId, TaskStatus.IN_PROGRESS);
  });

  test('should log status changes in activity log', async ({ 
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

    // Navigate to task detail and update status
    await taskDetailPage.navigate(taskId);
    await taskDetailPage.updateStatus(TaskStatus.BLOCKED);

    // Verify activity log contains status change
    const activityLog = await taskDetailPage.getActivityLog();
    const statusChangeEntry = activityLog.find(entry => 
      entry.type === 'status_change' && 
      entry.details.includes('Blocked')
    );
    
    expect(statusChangeEntry).toBeDefined();
  });

  test('should maintain status consistency across all views', async ({ 
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

    // Update status in one view
    await taskDetailPage.navigate(taskId);
    await taskDetailPage.updateStatus(TaskStatus.IN_PROGRESS);

    // Verify status consistency across all views
    await tasksPage.navigate();
    await tasksPage.assertTaskStatus(taskId, TaskStatus.IN_PROGRESS);

    await kanbanBoardPage.navigate();
    await kanbanBoardPage.assertTaskInColumn(taskId, TaskStatus.IN_PROGRESS);

    await taskDetailPage.navigate(taskId);
    await taskDetailPage.assertTaskStatus(TaskStatus.IN_PROGRESS);
  });
});