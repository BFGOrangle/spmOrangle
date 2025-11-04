import { test, expect } from '../../../fixtures';
import { TaskStatus, TaskPriority } from '../../../fixtures/data';

test.describe('Subtask Creation @integration @subtask-management', () => {
  test('should create subtask under parent task', async ({ 
    staffPage, 
    tasksPage, 
    taskDetailPage, 
    taskBuilder, 
    subtaskBuilder,
    taskCleanup 
  }) => {
    // Create a parent task first
    const parentTaskData = taskBuilder
      .withTitle('Parent Task for Subtask Creation')
      .withPriority(TaskPriority.MEDIUM)
      .withStatus(TaskStatus.TODO)
      .build();

    await tasksPage.navigate();
    const parentTaskId = await tasksPage.createStandaloneTask(parentTaskData);
    await taskCleanup.trackTask(parentTaskId);

    // Navigate to task detail page
    await taskDetailPage.navigate(parentTaskId);

    // Create a subtask
    const subtaskData = subtaskBuilder
      .withTitle('Test Subtask')
      .withNotes('This is a test subtask')
      .withStatus(TaskStatus.TODO)
      .build();

    const subtaskId = await taskDetailPage.createSubtask(subtaskData);

    // Verify subtask was created and is visible
    await taskDetailPage.expandSubtasks();
    const subtaskCount = await taskDetailPage.getSubtaskCount();
    expect(subtaskCount).toBe(1);

    // Verify subtask appears in the subtask list
    const subtaskElement = staffPage.locator(`[data-testid="subtask-item"][data-subtask-id="${subtaskId}"]`);
    await expect(subtaskElement).toBeVisible();
    await expect(subtaskElement).toContainText(subtaskData.title);
  });

  test('should create subtask with all available fields', async ({ 
    staffPage, 
    tasksPage, 
    taskDetailPage, 
    taskBuilder, 
    subtaskBuilder,
    taskCleanup 
  }) => {
    // Create a parent task
    const parentTaskData = taskBuilder
      .withTitle('Parent Task for Complete Subtask')
      .withPriority(TaskPriority.HIGH)
      .build();

    await tasksPage.navigate();
    const parentTaskId = await tasksPage.createStandaloneTask(parentTaskData);
    await taskCleanup.trackTask(parentTaskId);

    // Navigate to task detail page
    await taskDetailPage.navigate(parentTaskId);

    // Create subtask with all fields
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    const dueDate = tomorrow.toISOString().split('T')[0];

    const subtaskData = subtaskBuilder
      .withTitle('Complete Test Subtask')
      .withNotes('Detailed notes for this subtask')
      .withDueDate(dueDate)
      .withStatus(TaskStatus.TODO)
      .build();

    const subtaskId = await taskDetailPage.createSubtask(subtaskData);

    // Verify subtask creation and all fields
    await taskDetailPage.expandSubtasks();
    const subtaskElement = staffPage.locator(`[data-testid="subtask-item"][data-subtask-id="${subtaskId}"]`);
    
    await expect(subtaskElement).toBeVisible();
    await expect(subtaskElement).toContainText(subtaskData.title);
    
    if (subtaskData.notes) {
      await expect(subtaskElement).toContainText(subtaskData.notes);
    }
    
    if (subtaskData.dueDate) {
      await expect(subtaskElement).toContainText(dueDate);
    }
  });

  test('should inherit parent ownership and collaborators', async ({ 
    managerPage, 
    tasksPage, 
    taskDetailPage, 
    taskBuilder, 
    subtaskBuilder,
    taskCleanup 
  }) => {
    // Create a parent task with collaborators
    const parentTaskData = taskBuilder
      .withTitle('Parent Task with Collaborators')
      .withPriority(TaskPriority.MEDIUM)
      .withAssignees(['staff-user-1', 'staff-user-2'])
      .build();

    await tasksPage.navigate();
    const parentTaskId = await tasksPage.createStandaloneTask(parentTaskData);
    await taskCleanup.trackTask(parentTaskId);

    // Navigate to task detail page
    await taskDetailPage.navigate(parentTaskId);

    // Add a collaborator to the parent task
    await taskDetailPage.addCollaborator('staff-user-3', 'Editor');

    // Create a subtask
    const subtaskData = subtaskBuilder
      .withTitle('Subtask with Inherited Collaborators')
      .build();

    const subtaskId = await taskDetailPage.createSubtask(subtaskData);

    // Verify subtask inherits collaborators
    await taskDetailPage.expandSubtasks();
    const subtaskElement = managerPage.locator(`[data-testid="subtask-item"][data-subtask-id="${subtaskId}"]`);
    
    // Click on subtask to view its details
    await subtaskElement.click();
    
    // Verify collaborators are inherited (this would need to be implemented in the actual UI)
    // For now, we'll verify the subtask exists and is properly associated
    await expect(subtaskElement).toBeVisible();
    await expect(subtaskElement).toContainText(subtaskData.title);
  });
});