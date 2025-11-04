import { test, expect } from '../../../fixtures';
import { TaskStatus, TaskPriority } from '../../../fixtures/data/task-data-types';

test.describe('Task Property Updates @integration @task-management', () => {
  test('should update task title and description', async ({ 
    staffPage, 
    tasksPage, 
    taskDetailPage, 
    taskBuilder, 
    taskCleanup 
  }) => {
    // Create a test task first
    await tasksPage.navigate();
    
    const originalTaskData = taskBuilder
      .withTitle('Original Task Title')
      .withDescription('Original task description')
      .withPriority(TaskPriority.MEDIUM)
      .withStatus(TaskStatus.TODO)
      .build();
    
    const taskId = await tasksPage.createStandaloneTask(originalTaskData);
    await taskCleanup.trackTask(taskId);
    
    // Navigate to task detail page
    await taskDetailPage.navigate(taskId);
    
    // Update title
    const newTitle = 'Updated Task Title - E2E Test';
    await taskDetailPage.updateTitle(newTitle);
    
    // Verify title was updated
    await taskDetailPage.assertTaskTitle(newTitle);
    
    // Update description
    const newDescription = 'Updated task description with comprehensive details for E2E testing';
    await taskDetailPage.updateDescription(newDescription);
    
    // Verify description was updated by checking if edit mode shows the new description
    await taskDetailPage.enterEditMode();
    const descriptionInput = taskDetailPage.getByLabel(/description/i);
    const descriptionValue = await descriptionInput.inputValue();
    expect(descriptionValue).toBe(newDescription);
    await taskDetailPage.exitEditMode();
  });

  test('should update task priority and due date', async ({ 
    staffPage, 
    tasksPage, 
    taskDetailPage, 
    taskBuilder, 
    taskCleanup 
  }) => {
    // Create a test task first
    await tasksPage.navigate();
    
    const originalTaskData = taskBuilder
      .withTitle('Task for Priority and Due Date Updates')
      .withPriority(TaskPriority.LOW)
      .withStatus(TaskStatus.TODO)
      .build();
    
    const taskId = await tasksPage.createStandaloneTask(originalTaskData);
    await taskCleanup.trackTask(taskId);
    
    // Navigate to task detail page
    await taskDetailPage.navigate(taskId);
    
    // Update priority from LOW to HIGH
    await taskDetailPage.updatePriority(TaskPriority.HIGH);
    
    // Verify priority was updated
    await taskDetailPage.assertTaskPriority(TaskPriority.HIGH);
    
    // Update due date
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    const newDueDate = tomorrow.toISOString().split('T')[0]; // YYYY-MM-DD format
    
    await taskDetailPage.updateDueDate(newDueDate);
    
    // Verify due date was updated by checking the input value
    await taskDetailPage.enterEditMode();
    const dueDateInput = taskDetailPage.getByTestId('task-due-date-input');
    const dueDateValue = await dueDateInput.inputValue();
    expect(dueDateValue).toBe(newDueDate);
    await taskDetailPage.exitEditMode();
  });

  test('should reflect updates across all views', async ({ 
    staffPage, 
    tasksPage, 
    taskDetailPage, 
    myTasksPage, 
    kanbanBoardPage, 
    taskBuilder, 
    taskCleanup 
  }) => {
    // Create a test task first
    await tasksPage.navigate();
    
    const originalTaskData = taskBuilder
      .withTitle('Cross-View Update Test Task')
      .withPriority(TaskPriority.MEDIUM)
      .withStatus(TaskStatus.TODO)
      .build();
    
    const taskId = await tasksPage.createStandaloneTask(originalTaskData);
    await taskCleanup.trackTask(taskId);
    
    // Navigate to task detail and make updates
    await taskDetailPage.navigate(taskId);
    
    const updatedTitle = 'Updated Cross-View Task Title';
    await taskDetailPage.updateTitle(updatedTitle);
    await taskDetailPage.updateStatus(TaskStatus.IN_PROGRESS);
    await taskDetailPage.updatePriority(TaskPriority.HIGH);
    
    // Verify updates in task list view
    await tasksPage.navigate();
    await tasksPage.assertTaskVisible(taskId);
    await tasksPage.assertTaskStatus(taskId, TaskStatus.IN_PROGRESS);
    
    // Verify updates in My Tasks view
    await myTasksPage.navigate();
    await myTasksPage.assertTaskVisible(taskId);
    
    // Verify updates in Kanban board view
    await kanbanBoardPage.navigate();
    await kanbanBoardPage.assertTaskInColumn(taskId, TaskStatus.IN_PROGRESS);
    
    // Go back to detail view to confirm all updates persisted
    await taskDetailPage.navigate(taskId);
    await taskDetailPage.assertTaskTitle(updatedTitle);
    await taskDetailPage.assertTaskStatus(TaskStatus.IN_PROGRESS);
    await taskDetailPage.assertTaskPriority(TaskPriority.HIGH);
  });
});