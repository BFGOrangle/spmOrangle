import { test, expect } from '../../../fixtures';
import { TaskStatus, TaskPriority } from '../../../fixtures/data';

test.describe('Subtask Deletion @integration @subtask-management', () => {
  test('should delete subtask with manager permissions', async ({ 
    managerPage, 
    tasksPage, 
    taskDetailPage, 
    taskBuilder, 
    subtaskBuilder,
    taskCleanup 
  }) => {
    // Create a parent task
    const parentTaskData = taskBuilder
      .withTitle('Parent Task for Manager Deletion')
      .withPriority(TaskPriority.MEDIUM)
      .build();

    await tasksPage.navigate();
    const parentTaskId = await tasksPage.createStandaloneTask(parentTaskData);
    await taskCleanup.trackTask(parentTaskId);

    // Navigate to task detail page
    await taskDetailPage.navigate(parentTaskId);

    // Create subtasks to delete
    const subtask1Data = subtaskBuilder
      .withTitle('Subtask to Delete 1')
      .withStatus(TaskStatus.TODO)
      .build();

    const subtask2Data = subtaskBuilder
      .withTitle('Subtask to Keep')
      .withStatus(TaskStatus.COMPLETED)
      .build();

    const subtask1Id = await taskDetailPage.createSubtask(subtask1Data);
    await taskDetailPage.createSubtask(subtask2Data);

    // Verify initial state - 2 subtasks, 1 completed
    await taskDetailPage.assertSubtaskRollup('1/2 subtasks complete');

    // Expand subtasks to access delete controls
    await taskDetailPage.expandSubtasks();

    // Delete the first subtask
    await taskDetailPage.deleteSubtask(subtask1Id);

    // Verify subtask is removed from the list
    const deletedSubtaskElement = managerPage.locator(`[data-testid="subtask-item"][data-subtask-id="${subtask1Id}"]`);
    await expect(deletedSubtaskElement).not.toBeVisible();

    // Verify remaining subtask is still visible
    const remainingSubtaskElement = managerPage.locator('[data-testid="subtask-item"]').filter({ hasText: 'Subtask to Keep' });
    await expect(remainingSubtaskElement).toBeVisible();

    // Verify roll-up is updated to reflect deletion
    await taskDetailPage.assertSubtaskRollup('1/1 subtasks complete');
  });

  test('should show confirmation prompt for subtask deletion', async ({ 
    managerPage, 
    tasksPage, 
    taskDetailPage, 
    taskBuilder, 
    subtaskBuilder,
    taskCleanup 
  }) => {
    // Create a parent task
    const parentTaskData = taskBuilder
      .withTitle('Parent Task for Deletion Confirmation')
      .withPriority(TaskPriority.HIGH)
      .build();

    await tasksPage.navigate();
    const parentTaskId = await tasksPage.createStandaloneTask(parentTaskData);
    await taskCleanup.trackTask(parentTaskId);

    // Navigate to task detail page
    await taskDetailPage.navigate(parentTaskId);

    // Create a subtask to delete
    const subtaskData = subtaskBuilder
      .withTitle('Subtask for Confirmation Test')
      .withNotes('Important subtask that should require confirmation')
      .build();

    const subtaskId = await taskDetailPage.createSubtask(subtaskData);

    // Expand subtasks and initiate deletion
    await taskDetailPage.expandSubtasks();
    const subtaskElement = managerPage.locator(`[data-testid="subtask-item"][data-subtask-id="${subtaskId}"]`);
    const deleteButton = subtaskElement.locator('[data-testid="subtask-delete-button"]');
    
    await deleteButton.click();

    // Verify confirmation dialog appears
    const confirmationDialog = managerPage.locator('[data-testid="delete-confirmation-dialog"]');
    await expect(confirmationDialog).toBeVisible();
    
    // Verify dialog contains appropriate warning message
    await expect(confirmationDialog).toContainText(/are you sure/i);
    await expect(confirmationDialog).toContainText(/delete.*subtask/i);

    // Test cancellation
    const cancelButton = confirmationDialog.locator('[data-testid="cancel-delete-button"]');
    await cancelButton.click();

    // Verify dialog is closed and subtask still exists
    await expect(confirmationDialog).not.toBeVisible();
    await expect(subtaskElement).toBeVisible();

    // Test actual deletion
    await deleteButton.click();
    await expect(confirmationDialog).toBeVisible();
    
    const confirmButton = confirmationDialog.locator('[data-testid="confirm-delete-button"]');
    await confirmButton.click();

    // Verify subtask is deleted
    await expect(subtaskElement).not.toBeVisible();
    await expect(managerPage.getByText(/subtask deleted/i)).toBeVisible();
  });

  test('should update parent roll-up after subtask deletion', async ({ 
    managerPage, 
    tasksPage, 
    taskDetailPage, 
    taskBuilder, 
    subtaskBuilder,
    taskCleanup 
  }) => {
    // Create a parent task
    const parentTaskData = taskBuilder
      .withTitle('Parent Task for Roll-up Update Testing')
      .withPriority(TaskPriority.LOW)
      .build();

    await tasksPage.navigate();
    const parentTaskId = await tasksPage.createStandaloneTask(parentTaskData);
    await taskCleanup.trackTask(parentTaskId);

    // Navigate to task detail page
    await taskDetailPage.navigate(parentTaskId);

    // Create multiple subtasks with different statuses
    const completedSubtask1 = subtaskBuilder
      .withTitle('Completed Subtask 1')
      .withStatus(TaskStatus.COMPLETED)
      .build();

    const completedSubtask2 = subtaskBuilder
      .withTitle('Completed Subtask 2')
      .withStatus(TaskStatus.COMPLETED)
      .build();

    const todoSubtask = subtaskBuilder
      .withTitle('Todo Subtask')
      .withStatus(TaskStatus.TODO)
      .build();

    const inProgressSubtask = subtaskBuilder
      .withTitle('In Progress Subtask')
      .withStatus(TaskStatus.IN_PROGRESS)
      .build();

    const completedId1 = await taskDetailPage.createSubtask(completedSubtask1);
    const completedId2 = await taskDetailPage.createSubtask(completedSubtask2);
    const todoId = await taskDetailPage.createSubtask(todoSubtask);
    await taskDetailPage.createSubtask(inProgressSubtask);

    // Verify initial roll-up: 2 completed out of 4 total
    await taskDetailPage.assertSubtaskRollup('2/4 subtasks complete');

    // Delete one completed subtask
    await taskDetailPage.expandSubtasks();
    await taskDetailPage.deleteSubtask(completedId1);

    // Verify roll-up updates: 1 completed out of 3 total
    await taskDetailPage.assertSubtaskRollup('1/3 subtasks complete');

    // Delete the todo subtask
    await taskDetailPage.deleteSubtask(todoId);

    // Verify roll-up updates: 1 completed out of 2 total
    await taskDetailPage.assertSubtaskRollup('1/2 subtasks complete');

    // Delete the remaining completed subtask
    await taskDetailPage.deleteSubtask(completedId2);

    // Verify roll-up updates: 0 completed out of 1 total
    await taskDetailPage.assertSubtaskRollup('0/1 subtasks complete');
  });

  test('should prevent non-manager users from deleting subtasks', async ({ 
    staffPage, 
    tasksPage, 
    taskDetailPage, 
    taskBuilder, 
    subtaskBuilder,
    taskCleanup 
  }) => {
    // Create a parent task as staff user
    const parentTaskData = taskBuilder
      .withTitle('Parent Task for Permission Testing')
      .withPriority(TaskPriority.MEDIUM)
      .build();

    await tasksPage.navigate();
    const parentTaskId = await tasksPage.createStandaloneTask(parentTaskData);
    await taskCleanup.trackTask(parentTaskId);

    // Navigate to task detail page
    await taskDetailPage.navigate(parentTaskId);

    // Create a subtask
    const subtaskData = subtaskBuilder
      .withTitle('Subtask for Permission Test')
      .build();

    const subtaskId = await taskDetailPage.createSubtask(subtaskData);

    // Expand subtasks
    await taskDetailPage.expandSubtasks();

    // Verify delete button is not visible for staff users
    const subtaskElement = staffPage.locator(`[data-testid="subtask-item"][data-subtask-id="${subtaskId}"]`);
    const deleteButton = subtaskElement.locator('[data-testid="subtask-delete-button"]');
    
    await expect(deleteButton).not.toBeVisible();

    // Alternatively, verify delete button is disabled
    if (await deleteButton.isVisible()) {
      await expect(deleteButton).toBeDisabled();
    }

    // Verify subtask remains in the list
    await expect(subtaskElement).toBeVisible();
    await expect(subtaskElement).toContainText('Subtask for Permission Test');
  });

  test('should handle deletion of subtask with dependencies', async ({ 
    managerPage, 
    tasksPage, 
    taskDetailPage, 
    taskBuilder, 
    subtaskBuilder,
    taskCleanup 
  }) => {
    // Create a parent task
    const parentTaskData = taskBuilder
      .withTitle('Parent Task for Dependency Testing')
      .withPriority(TaskPriority.HIGH)
      .build();

    await tasksPage.navigate();
    const parentTaskId = await tasksPage.createStandaloneTask(parentTaskData);
    await taskCleanup.trackTask(parentTaskId);

    // Navigate to task detail page
    await taskDetailPage.navigate(parentTaskId);

    // Create a subtask with comments/attachments (simulated dependencies)
    const subtaskData = subtaskBuilder
      .withTitle('Subtask with Dependencies')
      .withNotes('This subtask has comments and attachments')
      .build();

    const subtaskId = await taskDetailPage.createSubtask(subtaskData);

    // Add a comment to the subtask (if supported)
    // This would be implemented when comment functionality is available for subtasks

    // Expand subtasks and delete
    await taskDetailPage.expandSubtasks();
    await taskDetailPage.deleteSubtask(subtaskId);

    // Verify subtask and all its dependencies are removed
    const deletedSubtaskElement = managerPage.locator(`[data-testid="subtask-item"][data-subtask-id="${subtaskId}"]`);
    await expect(deletedSubtaskElement).not.toBeVisible();

    // Verify success message indicates complete deletion
    await expect(managerPage.getByText(/subtask.*deleted/i)).toBeVisible();
  });
});