import { test, expect } from '../../../fixtures';
import { TaskStatus } from '../../../fixtures/data/task-data-types';

test.describe('Status Change Activity Logging @integration @task-status @activity-log', () => {
  test.beforeEach(async ({ tasksPage, taskCleanup }) => {
    await tasksPage.navigate();
  });

  test.afterEach(async ({ taskCleanup }) => {
    await taskCleanup.cleanupAll();
  });

  test('should log status changes with timestamp and editor in detail view', async ({ 
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

    // Update status and verify activity log entry
    await taskDetailPage.updateStatus(TaskStatus.IN_PROGRESS);

    // Check activity log for status change entry
    const activityLog = await taskDetailPage.getActivityLog();
    
    // Verify activity log contains status change entry
    const statusChangeEntry = activityLog.find(entry => 
      entry.type === 'status_change' && 
      entry.details.includes('To Do') && 
      entry.details.includes('In Progress')
    );
    
    expect(statusChangeEntry).toBeDefined();
    expect(statusChangeEntry?.timestamp).toBeDefined();
    expect(statusChangeEntry?.editor).toBeDefined();
  });

  test('should display previous and new status in activity log', async ({ 
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

    // Perform multiple status changes
    const statusTransitions = [
      { from: TaskStatus.TODO, to: TaskStatus.IN_PROGRESS },
      { from: TaskStatus.IN_PROGRESS, to: TaskStatus.BLOCKED },
      { from: TaskStatus.BLOCKED, to: TaskStatus.COMPLETED }
    ];

    for (const transition of statusTransitions) {
      await taskDetailPage.updateStatus(transition.to);
      
      // Verify activity log entry for this transition
      const activityLog = await taskDetailPage.getActivityLog();
      const latestEntry = activityLog[0]; // Most recent entry should be first
      
      expect(latestEntry.type).toBe('status_change');
      expect(latestEntry.details).toContain(transition.from);
      expect(latestEntry.details).toContain(transition.to);
    }
  });

  test('should log status changes from Kanban board drag and drop', async ({ 
    staffPage, 
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

    // Navigate to Kanban board and perform drag operation
    await kanbanBoardPage.navigate();
    await kanbanBoardPage.assertBoardLoaded();
    await kanbanBoardPage.dragTaskToColumn(taskId, TaskStatus.IN_PROGRESS);

    // Navigate to task detail to check activity log
    await taskDetailPage.navigate(taskId);
    
    // Verify activity log contains drag operation entry
    const activityLog = await taskDetailPage.getActivityLog();
    const dragEntry = activityLog.find(entry => 
      entry.type === 'status_change' && 
      entry.details.includes('To Do') && 
      entry.details.includes('In Progress')
    );
    
    expect(dragEntry).toBeDefined();
    expect(dragEntry?.source).toBe('kanban_board');
  });

  test('should log status changes from list view updates', async ({ 
    staffPage, 
    tasksPage, 
    taskDetailPage, 
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
    await tasksPage.updateTaskStatus(taskId, TaskStatus.COMPLETED);

    // Navigate to task detail to check activity log
    await taskDetailPage.navigate(taskId);
    
    // Verify activity log contains list view update entry
    const activityLog = await taskDetailPage.getActivityLog();
    const listUpdateEntry = activityLog.find(entry => 
      entry.type === 'status_change' && 
      entry.details.includes('To Do') && 
      entry.details.includes('Completed')
    );
    
    expect(listUpdateEntry).toBeDefined();
    expect(listUpdateEntry?.source).toBe('list_view');
  });

  test('should show activity log visibility for different user roles', async ({ 
    staffPage, 
    managerPage, 
    taskDetailPage, 
    taskDataFactory, 
    taskCleanup 
  }) => {
    // Create a test task as staff user
    const taskData = taskDataFactory.createStandaloneTask();
    taskData.status = TaskStatus.TODO;
    
    const taskId = await taskDataFactory.createTaskViaAPI(taskData);
    await taskCleanup.trackTask(taskId);

    // Update status as staff user
    await taskDetailPage.navigate(taskId);
    await taskDetailPage.updateStatus(TaskStatus.IN_PROGRESS);

    // Verify staff user can see activity log
    const staffActivityLog = await taskDetailPage.getActivityLog();
    expect(staffActivityLog.length).toBeGreaterThan(0);

    // Switch to manager user and verify they can also see activity log
    await managerPage.goto(await taskDetailPage.getCurrentUrl());
    
    const managerActivityLog = await taskDetailPage.getActivityLog();
    expect(managerActivityLog.length).toBeGreaterThan(0);
    
    // Verify both users see the same activity entries
    expect(managerActivityLog.length).toBe(staffActivityLog.length);
  });

  test('should include editor information in activity log entries', async ({ 
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

    // Verify activity log includes editor information
    const activityLog = await taskDetailPage.getActivityLog();
    const statusChangeEntry = activityLog.find(entry => entry.type === 'status_change');
    
    expect(statusChangeEntry).toBeDefined();
    expect(statusChangeEntry?.editor).toBeDefined();
    expect(statusChangeEntry?.editor).toContain('staff'); // Assuming staff user context
  });

  test('should maintain chronological order in activity log', async ({ 
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

    // Perform multiple status changes with delays
    await taskDetailPage.updateStatus(TaskStatus.IN_PROGRESS);
    await taskDetailPage.page.waitForTimeout(1000); // Small delay
    
    await taskDetailPage.updateStatus(TaskStatus.BLOCKED);
    await taskDetailPage.page.waitForTimeout(1000); // Small delay
    
    await taskDetailPage.updateStatus(TaskStatus.COMPLETED);

    // Verify activity log maintains chronological order (newest first)
    const activityLog = await taskDetailPage.getActivityLog();
    const statusEntries = activityLog.filter(entry => entry.type === 'status_change');
    
    expect(statusEntries.length).toBe(3);
    
    // Verify chronological order (newest first)
    expect(statusEntries[0].details).toContain('Completed');
    expect(statusEntries[1].details).toContain('Blocked');
    expect(statusEntries[2].details).toContain('In Progress');
    
    // Verify timestamps are in descending order
    for (let i = 0; i < statusEntries.length - 1; i++) {
      const currentTime = new Date(statusEntries[i].timestamp);
      const nextTime = new Date(statusEntries[i + 1].timestamp);
      expect(currentTime.getTime()).toBeGreaterThanOrEqual(nextTime.getTime());
    }
  });

  test('should filter activity log by status change events', async ({ 
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

    // Perform various actions to create different activity types
    await taskDetailPage.updateStatus(TaskStatus.IN_PROGRESS);
    await taskDetailPage.updateTitle('Updated Task Title');
    await taskDetailPage.updateStatus(TaskStatus.COMPLETED);

    // Filter activity log to show only status changes
    await taskDetailPage.filterActivityLog('status_change');
    
    const filteredLog = await taskDetailPage.getActivityLog();
    
    // Verify only status change entries are shown
    for (const entry of filteredLog) {
      expect(entry.type).toBe('status_change');
    }
    
    // Verify we have the expected number of status change entries
    expect(filteredLog.length).toBe(2); // Two status changes
  });

  test('should persist activity log across page refreshes', async ({ 
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

    // Get activity log before refresh
    const activityLogBefore = await taskDetailPage.getActivityLog();
    
    // Refresh the page
    await taskDetailPage.reload();

    // Get activity log after refresh
    const activityLogAfter = await taskDetailPage.getActivityLog();
    
    // Verify activity log persisted
    expect(activityLogAfter.length).toBe(activityLogBefore.length);
    
    const statusChangeEntry = activityLogAfter.find(entry => entry.type === 'status_change');
    expect(statusChangeEntry).toBeDefined();
  });
});