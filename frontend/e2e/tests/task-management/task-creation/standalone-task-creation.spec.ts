import { test, expect } from '../../../fixtures';
import { TaskStatus, TaskPriority } from '../../../fixtures/data/task-data-types';

test.describe('Standalone Task Creation @smoke @task-creation', () => {
  test('should create a standalone task with required fields', async ({ 
    staffPage, 
    tasksPage, 
    taskBuilder, 
    taskCleanup 
  }) => {
    // Navigate to tasks page
    await tasksPage.navigate();
    
    // Create task data with only required fields
    const taskData = taskBuilder
      .withTitle('E2E Test Task - Required Fields Only')
      .withPriority(TaskPriority.MEDIUM)
      .withStatus(TaskStatus.TODO)
      .build();
    
    // Create the task
    const taskId = await tasksPage.createStandaloneTask(taskData);
    await taskCleanup.trackTask(taskId);
    
    // Verify task was created and is visible
    await tasksPage.assertTaskVisible(taskId);
    
    // Verify task has correct properties
    await tasksPage.assertTaskStatus(taskId, TaskStatus.TODO);
  });

  test('should create a standalone task with all optional fields', async ({ 
    staffPage, 
    tasksPage, 
    taskBuilder, 
    taskCleanup 
  }) => {
    // Navigate to tasks page
    await tasksPage.navigate();
    
    // Create task data with all optional fields
    const taskData = taskBuilder
      .withTitle('E2E Test Task - All Fields')
      .withDescription('This is a comprehensive test task with all optional fields')
      .withPriority(TaskPriority.HIGH)
      .withStatus(TaskStatus.TODO)
      .withDueDate('2024-12-31')
      .withTags(['e2e-test', 'comprehensive'])
      .build();
    
    // Create the task
    const taskId = await tasksPage.createStandaloneTask(taskData);
    await taskCleanup.trackTask(taskId);
    
    // Verify task was created and is visible
    await tasksPage.assertTaskVisible(taskId);
    
    // Verify task has correct properties
    await tasksPage.assertTaskStatus(taskId, TaskStatus.TODO);
  });

  test('should display created task in My Tasks view', async ({ 
    staffPage, 
    tasksPage, 
    myTasksPage, 
    taskBuilder, 
    taskCleanup 
  }) => {
    // Navigate to tasks page and create a task
    await tasksPage.navigate();
    
    const taskData = taskBuilder
      .withTitle('E2E Test Task - My Tasks Visibility')
      .withPriority(TaskPriority.LOW)
      .withStatus(TaskStatus.TODO)
      .build();
    
    const taskId = await tasksPage.createStandaloneTask(taskData);
    await taskCleanup.trackTask(taskId);
    
    // Navigate to My Tasks view
    await myTasksPage.navigate();
    
    // Verify task appears in My Tasks view
    await myTasksPage.assertTaskVisible(taskId);
    
    // Verify task count is at least 1
    const taskCount = await myTasksPage.getTaskCount();
    expect(taskCount).toBeGreaterThanOrEqual(1);
  });
});