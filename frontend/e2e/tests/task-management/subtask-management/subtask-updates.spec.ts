import { test, expect } from '../../../fixtures';
import { TaskStatus, TaskPriority } from '../../../fixtures/data';

test.describe('Subtask Management @integration @subtask-management', () => {
  test('should expand and collapse subtasks in detail view', async ({
    staffPage,
    tasksPage,
    taskDetailPage,
    taskBuilder,
    subtaskBuilder,
    taskCleanup
  }) => {
    // Create a parent task with subtasks
    const parentTaskData = taskBuilder
      .withTitle('Parent Task for Expansion Testing')
      .withPriority(TaskPriority.MEDIUM)
      .build();

    await tasksPage.navigate();
    const parentTaskId = await tasksPage.createStandaloneTask(parentTaskData);
    await taskCleanup.trackTask(parentTaskId);

    // Navigate to task detail page
    await taskDetailPage.navigate(parentTaskId);

    // Create multiple subtasks
    const subtask1Data = subtaskBuilder
      .withTitle('Subtask 1 for Expansion')
      .build();

    const subtask2Data = subtaskBuilder
      .withTitle('Subtask 2 for Expansion')
      .build();

    await taskDetailPage.createSubtask(subtask1Data);
    await taskDetailPage.createSubtask(subtask2Data);

    // Initially, subtasks should be collapsed or not visible in detail
    const subtaskList = staffPage.locator('[data-testid="subtask-list"]');

    // Expand subtasks
    await taskDetailPage.expandSubtasks();
    await expect(subtaskList).toBeVisible();

    // Verify subtasks are visible after expansion
    await expect(subtaskList).toContainText('Subtask 1 for Expansion');
    await expect(subtaskList).toContainText('Subtask 2 for Expansion');

    // Test collapse functionality (if available)
    const collapseButton = staffPage.locator('[data-testid="collapse-subtasks-button"]');
    if (await collapseButton.isVisible()) {
      await collapseButton.click();
      await expect(subtaskList).not.toBeVisible();
    }
  });

  test('should update subtask status and reflect in parent roll-up', async ({
    staffPage,
    tasksPage,
    taskDetailPage,
    taskBuilder,
    subtaskBuilder,
    taskCleanup
  }) => {
    // Create a parent task
    const parentTaskData = taskBuilder
      .withTitle('Parent Task for Status Updates')
      .withPriority(TaskPriority.HIGH)
      .build();

    await tasksPage.navigate();
    const parentTaskId = await tasksPage.createStandaloneTask(parentTaskData);
    await taskCleanup.trackTask(parentTaskId);

    // Navigate to task detail page
    await taskDetailPage.navigate(parentTaskId);

    // Create subtasks with initial TODO status
    const subtask1Data = subtaskBuilder
      .withTitle('Subtask for Status Update 1')
      .withStatus(TaskStatus.TODO)
      .build();

    const subtask2Data = subtaskBuilder
      .withTitle('Subtask for Status Update 2')
      .withStatus(TaskStatus.TODO)
      .build();

    const subtask1Id = await taskDetailPage.createSubtask(subtask1Data);
    const subtask2Id = await taskDetailPage.createSubtask(subtask2Data);

    // Verify initial roll-up shows 0/2 complete
    await taskDetailPage.assertSubtaskRollup('0/2 subtasks complete');

    // Expand subtasks to access status controls
    await taskDetailPage.expandSubtasks();

    // Update first subtask to IN_PROGRESS
    const subtask1Element = staffPage.locator(`[data-testid="subtask-item"][data-subtask-id="${subtask1Id}"]`);
    const status1Dropdown = subtask1Element.locator('[data-testid="subtask-status-dropdown"]');

    await status1Dropdown.click();
    await staffPage.getByRole('option', { name: TaskStatus.IN_PROGRESS }).click();

    // Roll-up should still show 0/2 complete (IN_PROGRESS is not complete)
    await taskDetailPage.assertSubtaskRollup('0/2 subtasks complete');

    // Update second subtask to COMPLETED
    const subtask2Element = staffPage.locator(`[data-testid="subtask-item"][data-subtask-id="${subtask2Id}"]`);
    const status2Dropdown = subtask2Element.locator('[data-testid="subtask-status-dropdown"]');

    await status2Dropdown.click();
    await staffPage.getByRole('option', { name: TaskStatus.COMPLETED }).click();

    // Roll-up should now show 1/2 complete
    await taskDetailPage.assertSubtaskRollup('1/2 subtasks complete');

    // Complete the first subtask as well
    await status1Dropdown.click();
    await staffPage.getByRole('option', { name: TaskStatus.COMPLETED }).click();

    // Roll-up should now show 2/2 complete
    await taskDetailPage.assertSubtaskRollup('2/2 subtasks complete');
  });

  test('should edit subtask properties', async ({
    staffPage,
    tasksPage,
    taskDetailPage,
    taskBuilder,
    subtaskBuilder,
    taskCleanup
  }) => {
    // Create a parent task
    const parentTaskData = taskBuilder
      .withTitle('Parent Task for Subtask Editing')
      .withPriority(TaskPriority.MEDIUM)
      .build();

    await tasksPage.navigate();
    const parentTaskId = await tasksPage.createStandaloneTask(parentTaskData);
    await taskCleanup.trackTask(parentTaskId);

    // Navigate to task detail page
    await taskDetailPage.navigate(parentTaskId);

    // Create a subtask to edit
    const originalSubtaskData = subtaskBuilder
      .withTitle('Original Subtask Title')
      .withNotes('Original notes')
      .withStatus(TaskStatus.TODO)
      .build();

    const subtaskId = await taskDetailPage.createSubtask(originalSubtaskData);

    // Expand subtasks to access edit controls
    await taskDetailPage.expandSubtasks();

    // Find the subtask element
    const subtaskElement = staffPage.locator(`[data-testid="subtask-item"][data-subtask-id="${subtaskId}"]`);

    // Click edit button for the subtask
    const editButton = subtaskElement.locator('[data-testid="subtask-edit-button"]');
    await editButton.click();

    // Update subtask title
    const titleInput = staffPage.locator('[data-testid="subtask-title-input"]');
    await titleInput.fill('Updated Subtask Title');

    // Update subtask notes
    const notesInput = staffPage.locator('[data-testid="subtask-notes-input"]');
    await notesInput.fill('Updated subtask notes');

    // Update due date
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    const dueDate = tomorrow.toISOString().split('T')[0];

    const dueDateInput = staffPage.locator('[data-testid="subtask-due-date-input"]');
    await dueDateInput.fill(dueDate);

    // Save changes
    const saveButton = staffPage.locator('[data-testid="subtask-save-button"]');
    await saveButton.click();

    // Verify changes are reflected
    await expect(subtaskElement).toContainText('Updated Subtask Title');
    await expect(subtaskElement).toContainText('Updated subtask notes');
    await expect(subtaskElement).toContainText(dueDate);

    // Verify toast notification
    await expect(staffPage.getByText(/subtask updated/i)).toBeVisible();
  });

  test('should handle subtask editing with validation errors', async ({
    staffPage,
    tasksPage,
    taskDetailPage,
    taskBuilder,
    subtaskBuilder,
    taskCleanup
  }) => {
    // Create a parent task
    const parentTaskData = taskBuilder
      .withTitle('Parent Task for Validation Testing')
      .withPriority(TaskPriority.LOW)
      .build();

    await tasksPage.navigate();
    const parentTaskId = await tasksPage.createStandaloneTask(parentTaskData);
    await taskCleanup.trackTask(parentTaskId);

    // Navigate to task detail page
    await taskDetailPage.navigate(parentTaskId);

    // Create a subtask to edit
    const subtaskData = subtaskBuilder
      .withTitle('Subtask for Validation')
      .build();

    const subtaskId = await taskDetailPage.createSubtask(subtaskData);

    // Expand subtasks and start editing
    await taskDetailPage.expandSubtasks();
    const subtaskElement = staffPage.locator(`[data-testid="subtask-item"][data-subtask-id="${subtaskId}"]`);
    const editButton = subtaskElement.locator('[data-testid="subtask-edit-button"]');
    await editButton.click();

    // Try to save with empty title (should show validation error)
    const titleInput = staffPage.locator('[data-testid="subtask-title-input"]');
    await titleInput.fill('');

    const saveButton = staffPage.locator('[data-testid="subtask-save-button"]');
    await saveButton.click();

    // Verify validation error is shown
    const errorMessage = staffPage.locator('[data-testid="subtask-title-error"]');
    await expect(errorMessage).toBeVisible();
    await expect(errorMessage).toContainText(/title is required/i);

    // Cancel editing to restore original state
    const cancelButton = staffPage.locator('[data-testid="subtask-cancel-button"]');
    await cancelButton.click();

    // Verify original title is restored
    await expect(subtaskElement).toContainText('Subtask for Validation');
  });
});