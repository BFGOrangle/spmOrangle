import { test, expect } from '../../../fixtures';
import { TaskStatus } from '../../../fixtures/data/task-data-types';

test.describe('Kanban Board Status Updates @integration @task-status', () => {
  test.beforeEach(async ({ tasksPage, taskCleanup }) => {
    await tasksPage.navigate();
  });

  test.afterEach(async ({ taskCleanup }) => {
    await taskCleanup.cleanupAll();
  });

  test('should update task status via drag and drop between columns', async ({
    staffPage,
    kanbanBoardPage,
    taskDataFactory,
    taskCleanup
  }) => {
    // Create a test task in TODO status
    const taskData = taskDataFactory.createStandaloneTask();
    taskData.status = TaskStatus.TODO;

    // Create task via API to ensure it exists
    const taskId = await taskDataFactory.createTaskViaAPI(taskData);
    await taskCleanup.trackTask(taskId);

    // Navigate to Kanban board
    await kanbanBoardPage.navigate();
    await kanbanBoardPage.assertBoardLoaded();

    // Verify task is initially in TODO column
    await kanbanBoardPage.assertTaskInColumn(taskId, TaskStatus.TODO);

    // Drag task from TODO to IN_PROGRESS
    await kanbanBoardPage.dragTaskToColumn(taskId, TaskStatus.IN_PROGRESS);

    // Verify task moved to IN_PROGRESS column
    await kanbanBoardPage.assertTaskInColumn(taskId, TaskStatus.IN_PROGRESS);

    // Drag task from IN_PROGRESS to COMPLETED
    await kanbanBoardPage.dragTaskToColumn(taskId, TaskStatus.COMPLETED);

    // Verify task moved to COMPLETED column
    await kanbanBoardPage.assertTaskInColumn(taskId, TaskStatus.COMPLETED);
  });

  test('should reflect status updates immediately after column change', async ({
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

    // Navigate to Kanban board
    await kanbanBoardPage.navigate();
    await kanbanBoardPage.assertBoardLoaded();

    // Record initial column counts
    const initialCounts = await kanbanBoardPage.getColumnCounts();

    // Drag task to IN_PROGRESS
    await kanbanBoardPage.dragTaskToColumn(taskId, TaskStatus.IN_PROGRESS);

    // Verify column counts updated immediately
    const updatedCounts = await kanbanBoardPage.getColumnCounts();
    expect(updatedCounts.todo).toBe(initialCounts.todo - 1);
    expect(updatedCounts.inProgress).toBe(initialCounts.inProgress + 1);

    // Verify task is visually in the correct column
    await kanbanBoardPage.assertTaskInColumn(taskId, TaskStatus.IN_PROGRESS);
  });

  test('should update status across all views after drag and drop', async ({
    staffPage,
    kanbanBoardPage,
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

    // Navigate to Kanban board and update status
    await kanbanBoardPage.navigate();
    await kanbanBoardPage.assertBoardLoaded();
    await kanbanBoardPage.dragTaskToColumn(taskId, TaskStatus.IN_PROGRESS);

    // Verify status in list view
    await tasksPage.navigate();
    await tasksPage.assertTaskStatus(taskId, TaskStatus.IN_PROGRESS);

    // Verify status in detail view
    await taskDetailPage.navigate(taskId);
    await taskDetailPage.assertTaskStatus(TaskStatus.IN_PROGRESS);

    // Return to Kanban board and verify status persisted
    await kanbanBoardPage.navigate();
    await kanbanBoardPage.assertTaskInColumn(taskId, TaskStatus.IN_PROGRESS);
  });

  test('should handle drag and drop between all status columns', async ({
    staffPage,
    kanbanBoardPage,
    taskDataFactory,
    taskCleanup
  }) => {
    // Create tasks in different statuses
    const todoTask = taskDataFactory.createStandaloneTask();
    todoTask.status = TaskStatus.TODO;
    const todoTaskId = await taskDataFactory.createTaskViaAPI(todoTask);
    await taskCleanup.trackTask(todoTaskId);

    const inProgressTask = taskDataFactory.createStandaloneTask();
    inProgressTask.status = TaskStatus.IN_PROGRESS;
    const inProgressTaskId = await taskDataFactory.createTaskViaAPI(inProgressTask);
    await taskCleanup.trackTask(inProgressTaskId);

    // Navigate to Kanban board
    await kanbanBoardPage.navigate();
    await kanbanBoardPage.assertBoardLoaded();

    // Test all possible drag operations
    const statusTransitions = [
      { taskId: todoTaskId, from: TaskStatus.TODO, to: TaskStatus.BLOCKED },
      { taskId: inProgressTaskId, from: TaskStatus.IN_PROGRESS, to: TaskStatus.COMPLETED },
      { taskId: todoTaskId, from: TaskStatus.BLOCKED, to: TaskStatus.IN_PROGRESS },
      { taskId: inProgressTaskId, from: TaskStatus.COMPLETED, to: TaskStatus.TODO }
    ];

    for (const transition of statusTransitions) {
      // Verify task is in source column
      await kanbanBoardPage.assertTaskInColumn(transition.taskId, transition.from);

      // Perform drag operation
      await kanbanBoardPage.dragTaskToColumn(transition.taskId, transition.to);

      // Verify task moved to target column
      await kanbanBoardPage.assertTaskInColumn(transition.taskId, transition.to);
    }
  });

  test('should maintain task order within columns after drag operations', async ({
    staffPage,
    kanbanBoardPage,
    taskDataFactory,
    taskCleanup
  }) => {
    // Create multiple tasks in TODO status
    const task1Data = taskDataFactory.createStandaloneTask();
    task1Data.status = TaskStatus.TODO;
    task1Data.title = 'Task 1';
    const task1Id = await taskDataFactory.createTaskViaAPI(task1Data);
    await taskCleanup.trackTask(task1Id);

    const task2Data = taskDataFactory.createStandaloneTask();
    task2Data.status = TaskStatus.TODO;
    task2Data.title = 'Task 2';
    const task2Id = await taskDataFactory.createTaskViaAPI(task2Data);
    await taskCleanup.trackTask(task2Id);

    // Navigate to Kanban board
    await kanbanBoardPage.navigate();
    await kanbanBoardPage.assertBoardLoaded();

    // Get initial task order in TODO column
    const initialTodoTasks = await kanbanBoardPage.getTasksInColumn(TaskStatus.TODO);
    expect(initialTodoTasks).toContain(task1Id);
    expect(initialTodoTasks).toContain(task2Id);

    // Move one task to IN_PROGRESS
    await kanbanBoardPage.dragTaskToColumn(task1Id, TaskStatus.IN_PROGRESS);

    // Verify task order in both columns
    const updatedTodoTasks = await kanbanBoardPage.getTasksInColumn(TaskStatus.TODO);
    const inProgressTasks = await kanbanBoardPage.getTasksInColumn(TaskStatus.IN_PROGRESS);

    expect(updatedTodoTasks).toContain(task2Id);
    expect(updatedTodoTasks).not.toContain(task1Id);
    expect(inProgressTasks).toContain(task1Id);
  });

  test('should show visual feedback during drag operations', async ({
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

    // Navigate to Kanban board
    await kanbanBoardPage.navigate();
    await kanbanBoardPage.assertBoardLoaded();

    // Verify task is draggable
    const isDraggable = await kanbanBoardPage.isTaskDraggable(taskId);
    expect(isDraggable).toBe(true);

    // Perform drag operation and verify it completes
    await kanbanBoardPage.dragTaskToColumn(taskId, TaskStatus.IN_PROGRESS);

    // Wait for any drag operation animations to complete
    await kanbanBoardPage.waitForDragOperation();

    // Verify final position
    await kanbanBoardPage.assertTaskInColumn(taskId, TaskStatus.IN_PROGRESS);
  });
});