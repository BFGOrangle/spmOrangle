import { test, expect } from '../../../fixtures';
import { TaskStatus, TaskPriority } from '../../../fixtures/data';

test.describe('Subtask Roll-up Functionality @integration @subtask-management', () => {
  test('should display aggregated subtask status on parent task', async ({ 
    staffPage, 
    tasksPage, 
    taskDetailPage, 
    taskBuilder, 
    subtaskBuilder,
    taskCleanup 
  }) => {
    // Create a parent task
    const parentTaskData = taskBuilder
      .withTitle('Parent Task for Roll-up Display')
      .withPriority(TaskPriority.MEDIUM)
      .build();

    await tasksPage.navigate();
    const parentTaskId = await tasksPage.createStandaloneTask(parentTaskData);
    await taskCleanup.trackTask(parentTaskId);

    // Navigate to task detail page
    await taskDetailPage.navigate(parentTaskId);

    // Create multiple subtasks
    const subtask1Data = subtaskBuilder
      .withTitle('Subtask 1')
      .withStatus(TaskStatus.TODO)
      .build();

    const subtask2Data = subtaskBuilder
      .withTitle('Subtask 2')
      .withStatus(TaskStatus.TODO)
      .build();

    const subtask3Data = subtaskBuilder
      .withTitle('Subtask 3')
      .withStatus(TaskStatus.COMPLETED)
      .build();

    await taskDetailPage.createSubtask(subtask1Data);
    await taskDetailPage.createSubtask(subtask2Data);
    await taskDetailPage.createSubtask(subtask3Data);

    // Verify roll-up status shows correct aggregation
    await taskDetailPage.assertSubtaskRollup('1/3 subtasks complete');

    // Verify total subtask count
    const subtaskCount = await taskDetailPage.getSubtaskCount();
    expect(subtaskCount).toBe(3);
  });

  test('should update roll-up when subtask statuses change', async ({ 
    staffPage, 
    tasksPage, 
    taskDetailPage, 
    taskBuilder, 
    subtaskBuilder,
    taskCleanup 
  }) => {
    // Create a parent task
    const parentTaskData = taskBuilder
      .withTitle('Parent Task for Roll-up Updates')
      .withPriority(TaskPriority.HIGH)
      .build();

    await tasksPage.navigate();
    const parentTaskId = await tasksPage.createStandaloneTask(parentTaskData);
    await taskCleanup.trackTask(parentTaskId);

    // Navigate to task detail page
    await taskDetailPage.navigate(parentTaskId);

    // Create subtasks with different statuses
    const subtask1Data = subtaskBuilder
      .withTitle('Subtask To Complete')
      .withStatus(TaskStatus.TODO)
      .build();

    const subtask2Data = subtaskBuilder
      .withTitle('Subtask In Progress')
      .withStatus(TaskStatus.IN_PROGRESS)
      .build();

    const subtask1Id = await taskDetailPage.createSubtask(subtask1Data);
    await taskDetailPage.createSubtask(subtask2Data);

    // Initial roll-up should show 0/2 complete
    await taskDetailPage.assertSubtaskRollup('0/2 subtasks complete');

    // Update first subtask to completed
    await taskDetailPage.expandSubtasks();
    const subtask1Element = staffPage.locator(`[data-testid="subtask-item"][data-subtask-id="${subtask1Id}"]`);
    const statusDropdown = subtask1Element.locator('[data-testid="subtask-status-dropdown"]');
    
    await statusDropdown.click();
    await staffPage.getByRole('option', { name: TaskStatus.COMPLETED }).click();

    // Verify roll-up updates to 1/2 complete
    await taskDetailPage.assertSubtaskRollup('1/2 subtasks complete');
  });

  test('should display correct roll-up format for different completion states', async ({ 
    staffPage, 
    tasksPage, 
    taskDetailPage, 
    taskBuilder, 
    subtaskBuilder,
    taskCleanup 
  }) => {
    // Create a parent task
    const parentTaskData = taskBuilder
      .withTitle('Parent Task for Roll-up Format Testing')
      .withPriority(TaskPriority.LOW)
      .build();

    await tasksPage.navigate();
    const parentTaskId = await tasksPage.createStandaloneTask(parentTaskData);
    await taskCleanup.trackTask(parentTaskId);

    // Navigate to task detail page
    await taskDetailPage.navigate(parentTaskId);

    // Test case 1: No subtasks
    await taskDetailPage.assertSubtaskRollup('0/0 subtasks complete');

    // Test case 2: All subtasks completed
    const completedSubtask1 = subtaskBuilder
      .withTitle('Completed Subtask 1')
      .withStatus(TaskStatus.COMPLETED)
      .build();

    const completedSubtask2 = subtaskBuilder
      .withTitle('Completed Subtask 2')
      .withStatus(TaskStatus.COMPLETED)
      .build();

    await taskDetailPage.createSubtask(completedSubtask1);
    await taskDetailPage.createSubtask(completedSubtask2);

    // Verify all completed format
    await taskDetailPage.assertSubtaskRollup('2/2 subtasks complete');

    // Test case 3: Mixed statuses
    const todoSubtask = subtaskBuilder
      .withTitle('Todo Subtask')
      .withStatus(TaskStatus.TODO)
      .build();

    await taskDetailPage.createSubtask(todoSubtask);

    // Verify mixed status format
    await taskDetailPage.assertSubtaskRollup('2/3 subtasks complete');
  });

  test('should handle blocked subtasks in roll-up calculation', async ({ 
    staffPage, 
    tasksPage, 
    taskDetailPage, 
    taskBuilder, 
    subtaskBuilder,
    taskCleanup 
  }) => {
    // Create a parent task
    const parentTaskData = taskBuilder
      .withTitle('Parent Task with Blocked Subtasks')
      .withPriority(TaskPriority.MEDIUM)
      .build();

    await tasksPage.navigate();
    const parentTaskId = await tasksPage.createStandaloneTask(parentTaskData);
    await taskCleanup.trackTask(parentTaskId);

    // Navigate to task detail page
    await taskDetailPage.navigate(parentTaskId);

    // Create subtasks with various statuses including blocked
    const completedSubtask = subtaskBuilder
      .withTitle('Completed Subtask')
      .withStatus(TaskStatus.COMPLETED)
      .build();

    const blockedSubtask = subtaskBuilder
      .withTitle('Blocked Subtask')
      .withStatus(TaskStatus.BLOCKED)
      .build();

    const todoSubtask = subtaskBuilder
      .withTitle('Todo Subtask')
      .withStatus(TaskStatus.TODO)
      .build();

    await taskDetailPage.createSubtask(completedSubtask);
    await taskDetailPage.createSubtask(blockedSubtask);
    await taskDetailPage.createSubtask(todoSubtask);

    // Verify roll-up only counts completed subtasks
    await taskDetailPage.assertSubtaskRollup('1/3 subtasks complete');

    // Verify blocked status is visible in subtask list
    await taskDetailPage.expandSubtasks();
    const blockedElement = staffPage.locator('[data-testid="subtask-item"]').filter({ hasText: 'Blocked Subtask' });
    await expect(blockedElement).toContainText(TaskStatus.BLOCKED);
  });
});