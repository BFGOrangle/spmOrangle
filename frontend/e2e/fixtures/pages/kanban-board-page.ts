import { Page, Locator, expect } from '@playwright/test';
import { BasePage } from './base-page';
import { TaskStatus, StatusCounts } from '../data/task-data-types';

/**
 * Kanban Board Page Object
 * Encapsulates interactions with the Kanban board view
 */
export class KanbanBoardPage extends BasePage {
  private route = '/tasks/board';

  constructor(page: Page) {
    super(page);
  }

  /**
   * Selectors - using user-facing methods and data-testid where appropriate
   */
  private get kanbanBoard(): Locator {
    return this.getByTestId('kanban-board');
  }

  private get todoColumn(): Locator {
    return this.getByTestId('kanban-column-todo');
  }

  private get inProgressColumn(): Locator {
    return this.getByTestId('kanban-column-in-progress');
  }

  private get completedColumn(): Locator {
    return this.getByTestId('kanban-column-completed');
  }

  private get blockedColumn(): Locator {
    return this.getByTestId('kanban-column-blocked');
  }

  private get taskCards(): Locator {
    return this.getByTestId('kanban-task-card');
  }

  private get loadingIndicator(): Locator {
    return this.getByText(/loading board/i);
  }

  private get refreshButton(): Locator {
    return this.getByRole('button', { name: /refresh/i });
  }

  private get viewToggle(): Locator {
    return this.getByTestId('view-toggle');
  }

  private get boardHeading(): Locator {
    return this.getByRole('heading', { name: /kanban board/i });
  }

  /**
   * Navigation methods
   */
  async navigate(projectId?: string): Promise<void> {
    const url = projectId ? `/projects/${projectId}/board` : this.route;
    await this.goto(url);
    await this.boardHeading.waitFor({ state: 'visible', timeout: 30000 });
    await this.waitForBoardLoaded();
  }

  private async waitForBoardLoaded(): Promise<void> {
    // Wait for loading indicator to disappear
    try {
      await this.loadingIndicator.waitFor({ state: 'visible', timeout: 2000 });
      await this.loadingIndicator.waitFor({ state: 'hidden', timeout: 15000 });
    } catch {
      // Loading might be too fast to catch
    }
    
    // Ensure board is visible
    await this.assertVisible(this.kanbanBoard);
  }

  /**
   * Kanban operations
   */
  async dragTaskToColumn(taskId: string, targetStatus: TaskStatus): Promise<void> {
    const taskCard = this.getTaskCard(taskId);
    const targetColumn = this.getColumnByStatus(targetStatus);
    
    // Perform drag and drop
    await taskCard.dragTo(targetColumn);
    
    // Wait for the drag operation to complete and status to update
    await this.waitForStatusUpdate(taskId, targetStatus);
  }

  async getTasksInColumn(status: TaskStatus): Promise<string[]> {
    const column = this.getColumnByStatus(status);
    const taskCards = column.locator('[data-testid="kanban-task-card"]');
    
    const taskIds: string[] = [];
    const count = await taskCards.count();
    
    for (let i = 0; i < count; i++) {
      const taskId = await taskCards.nth(i).getAttribute('data-task-id');
      if (taskId) {
        taskIds.push(taskId);
      }
    }
    
    return taskIds;
  }

  async getColumnTaskCount(status: TaskStatus): Promise<number> {
    const column = this.getColumnByStatus(status);
    const taskCards = column.locator('[data-testid="kanban-task-card"]');
    return await taskCards.count();
  }

  private getColumnByStatus(status: TaskStatus): Locator {
    switch (status) {
      case TaskStatus.TODO:
        return this.todoColumn;
      case TaskStatus.IN_PROGRESS:
        return this.inProgressColumn;
      case TaskStatus.COMPLETED:
        return this.completedColumn;
      case TaskStatus.BLOCKED:
        return this.blockedColumn;
      default:
        throw new Error(`Unknown status: ${status}`);
    }
  }

  private getTaskCard(taskId: string): Locator {
    return this.page.locator(`[data-testid="kanban-task-card"][data-task-id="${taskId}"]`);
  }

  private async waitForStatusUpdate(taskId: string, expectedStatus: TaskStatus): Promise<void> {
    // Wait for the task to appear in the target column
    const targetColumn = this.getColumnByStatus(expectedStatus);
    const taskInColumn = targetColumn.locator(`[data-task-id="${taskId}"]`);
    
    await taskInColumn.waitFor({ state: 'visible', timeout: 10000 });
    
    // Optional: Wait for toast notification
    try {
      await this.waitForToast(/status updated/i, 5000);
    } catch {
      // Toast might not appear for drag operations
    }
  }

  /**
   * Task quick actions
   */
  async quickEditTask(taskId: string): Promise<void> {
    const taskCard = this.getTaskCard(taskId);
    
    // Right-click or hover to show quick actions
    await taskCard.hover();
    
    const quickEditButton = taskCard.locator('[data-testid="quick-edit-button"]');
    await this.click(quickEditButton);
  }

  async quickViewTask(taskId: string): Promise<void> {
    const taskCard = this.getTaskCard(taskId);
    await this.click(taskCard);
    
    // Wait for task detail modal or navigation
    const taskModal = this.page.locator('[data-testid="task-detail-modal"]');
    if (await taskModal.isVisible()) {
      await this.assertVisible(taskModal);
    } else {
      // If it navigates to detail page
      await this.waitForUrl(new RegExp(`/tasks/${taskId}`));
    }
  }

  /**
   * Board management
   */
  async refreshBoard(): Promise<void> {
    await this.click(this.refreshButton);
    await this.waitForBoardLoaded();
  }

  async switchView(viewType: 'list' | 'board'): Promise<void> {
    await this.click(this.viewToggle);
    
    const viewOption = this.page.getByRole('option', { name: viewType });
    await this.click(viewOption);
    
    if (viewType === 'list') {
      await this.waitForUrl(/\/tasks$/);
    } else {
      await this.waitForUrl(/\/tasks\/board$/);
    }
  }

  /**
   * Drag and drop utilities
   */
  async dragTaskBetweenColumns(taskId: string, fromStatus: TaskStatus, toStatus: TaskStatus): Promise<void> {
    // Verify task is in source column first
    await this.assertTaskInColumn(taskId, fromStatus);
    
    // Perform drag operation
    await this.dragTaskToColumn(taskId, toStatus);
    
    // Verify task moved to target column
    await this.assertTaskInColumn(taskId, toStatus);
  }

  async simulateDragAndDrop(taskId: string, targetStatus: TaskStatus): Promise<void> {
    const taskCard = this.getTaskCard(taskId);
    const targetColumn = this.getColumnByStatus(targetStatus);
    
    // Get bounding boxes for drag calculation
    const taskBox = await taskCard.boundingBox();
    const columnBox = await targetColumn.boundingBox();
    
    if (!taskBox || !columnBox) {
      throw new Error('Could not get bounding boxes for drag operation');
    }
    
    // Perform mouse-based drag and drop
    await this.page.mouse.move(taskBox.x + taskBox.width / 2, taskBox.y + taskBox.height / 2);
    await this.page.mouse.down();
    await this.page.mouse.move(columnBox.x + columnBox.width / 2, columnBox.y + columnBox.height / 2);
    await this.page.mouse.up();
    
    // Wait for status update
    await this.waitForStatusUpdate(taskId, targetStatus);
  }

  /**
   * Assertion methods
   */
  async assertTaskInColumn(taskId: string, expectedStatus: TaskStatus): Promise<void> {
    const column = this.getColumnByStatus(expectedStatus);
    const taskInColumn = column.locator(`[data-task-id="${taskId}"]`);
    await this.assertVisible(taskInColumn);
  }

  async assertColumnCounts(expectedCounts: StatusCounts): Promise<void> {
    const actualCounts = await this.getColumnCounts();
    
    expect(actualCounts.todo).toBe(expectedCounts.todo);
    expect(actualCounts.inProgress).toBe(expectedCounts.inProgress);
    expect(actualCounts.completed).toBe(expectedCounts.completed);
    expect(actualCounts.blocked).toBe(expectedCounts.blocked);
  }

  async assertBoardLoaded(): Promise<void> {
    await this.assertVisible(this.kanbanBoard);
    await this.assertHidden(this.loadingIndicator);
  }

  async assertColumnVisible(status: TaskStatus): Promise<void> {
    const column = this.getColumnByStatus(status);
    await this.assertVisible(column);
  }

  async assertTaskCardVisible(taskId: string): Promise<void> {
    const taskCard = this.getTaskCard(taskId);
    await this.assertVisible(taskCard);
  }

  /**
   * Data extraction methods
   */
  async getColumnCounts(): Promise<StatusCounts> {
    return {
      todo: await this.getColumnTaskCount(TaskStatus.TODO),
      inProgress: await this.getColumnTaskCount(TaskStatus.IN_PROGRESS),
      completed: await this.getColumnTaskCount(TaskStatus.COMPLETED),
      blocked: await this.getColumnTaskCount(TaskStatus.BLOCKED)
    };
  }

  async getAllTasksOnBoard(): Promise<string[]> {
    const allTasks: string[] = [];
    
    for (const status of Object.values(TaskStatus)) {
      const tasksInColumn = await this.getTasksInColumn(status);
      allTasks.push(...tasksInColumn);
    }
    
    return allTasks;
  }

  async getTaskPosition(taskId: string): Promise<{ column: TaskStatus; index: number }> {
    for (const status of Object.values(TaskStatus)) {
      const tasksInColumn = await this.getTasksInColumn(status);
      const index = tasksInColumn.indexOf(taskId);
      
      if (index !== -1) {
        return { column: status, index };
      }
    }
    
    throw new Error(`Task ${taskId} not found on board`);
  }

  /**
   * Utility methods
   */
  async waitForToast(message: string | RegExp, timeout: number = 10000): Promise<void> {
    const toastSelectors = [
      this.page.locator('[data-state="open"]').filter({ hasText: message }),
      this.page.locator('[role="status"]').filter({ hasText: message }),
      this.page.locator('[class*="toast"]').filter({ hasText: message })
    ];
    
    for (const selector of toastSelectors) {
      try {
        await selector.first().waitFor({ state: 'visible', timeout: timeout / toastSelectors.length });
        return;
      } catch {
        continue;
      }
    }
    
    await this.page.getByText(message).first().waitFor({ state: 'visible', timeout: 3000 });
  }

  async waitForDragOperation(): Promise<void> {
    // Wait for any drag operation animations to complete
    await this.page.waitForTimeout(500);
  }

  async isTaskDraggable(taskId: string): Promise<boolean> {
    const taskCard = this.getTaskCard(taskId);
    const draggableAttribute = await taskCard.getAttribute('draggable');
    return draggableAttribute === 'true';
  }

  async getColumnHeader(status: TaskStatus): Promise<string> {
    const column = this.getColumnByStatus(status);
    const header = column.locator('[data-testid="column-header"]');
    return await header.textContent() || '';
  }
}