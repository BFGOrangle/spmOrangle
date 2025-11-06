import { Page, Locator } from '@playwright/test';
import { BasePage } from './base-page';
import { TaskStatus, TaskSummary } from '../data/task-data-types';

/**
 * My Tasks Page Object
 * Encapsulates interactions with the My Tasks view
 */
export class MyTasksPage extends BasePage {
  private route = '/tasks/my-tasks';

  constructor(page: Page) {
    super(page);
  }

  /**
   * Selectors
   */
  private get myTasksHeading(): Locator {
    return this.getByRole('heading', { name: /my tasks/i });
  }

  private get taskGroups(): Locator {
    return this.getByTestId('task-group');
  }

  private get overdueGroup(): Locator {
    return this.getByTestId('task-group-overdue');
  }

  private get todayGroup(): Locator {
    return this.getByTestId('task-group-today');
  }

  private get upcomingGroup(): Locator {
    return this.getByTestId('task-group-upcoming');
  }

  private get noDateGroup(): Locator {
    return this.getByTestId('task-group-no-date');
  }

  private get taskCards(): Locator {
    return this.getByTestId('my-task-card');
  }

  private get emptyState(): Locator {
    return this.getByText(/no tasks assigned/i);
  }

  /**
   * Navigation
   */
  async navigate(): Promise<void> {
    await this.goto(this.route);
    await this.myTasksHeading.waitFor({ state: 'visible', timeout: 30000 });
  }

  /**
   * Task interaction methods
   */
  async openTask(taskId: string): Promise<void> {
    const taskCard = this.getTaskCard(taskId);
    await this.click(taskCard);
  }

  async updateTaskStatus(taskId: string, status: TaskStatus): Promise<void> {
    const taskCard = this.getTaskCard(taskId);
    const statusDropdown = taskCard.locator('[data-testid="task-status-dropdown"]');
    
    await this.click(statusDropdown);
    await this.page.getByRole('option', { name: status }).click();
    
    await this.waitForToast(/status updated/i);
  }

  private getTaskCard(taskId: string): Locator {
    return this.page.locator(`[data-testid="my-task-card"][data-task-id="${taskId}"]`);
  }

  /**
   * Data extraction methods
   */
  async getTasksByGroup(groupType: 'overdue' | 'today' | 'upcoming' | 'no-date'): Promise<TaskSummary[]> {
    const group = this.getTaskGroup(groupType);
    const taskCards = group.locator('[data-testid="my-task-card"]');
    
    const tasks: TaskSummary[] = [];
    const count = await taskCards.count();
    
    for (let i = 0; i < count; i++) {
      const taskCard = taskCards.nth(i);
      const id = await taskCard.getAttribute('data-task-id') || '';
      const title = await taskCard.locator('[data-testid="task-title"]').textContent() || '';
      const statusText = await taskCard.locator('[data-testid="task-status"]').textContent() || '';
      const priorityText = await taskCard.locator('[data-testid="task-priority"]').textContent() || '';
      const dueDateText = await taskCard.locator('[data-testid="task-due-date"]').textContent();
      
      tasks.push({
        id,
        title,
        status: statusText as TaskStatus,
        priority: parseInt(priorityText),
        dueDate: dueDateText || undefined,
        assigneeCount: 1 // User is always assigned to their own tasks
      });
    }
    
    return tasks;
  }

  async getAllMyTasks(): Promise<TaskSummary[]> {
    const allTasks: TaskSummary[] = [];
    
    const groupTypes: Array<'overdue' | 'today' | 'upcoming' | 'no-date'> = 
      ['overdue', 'today', 'upcoming', 'no-date'];
    
    for (const groupType of groupTypes) {
      try {
        const groupTasks = await this.getTasksByGroup(groupType);
        allTasks.push(...groupTasks);
      } catch {
        // Group might not exist if empty
      }
    }
    
    return allTasks;
  }

  async getTaskCountByGroup(groupType: 'overdue' | 'today' | 'upcoming' | 'no-date'): Promise<number> {
    try {
      const group = this.getTaskGroup(groupType);
      const taskCards = group.locator('[data-testid="my-task-card"]');
      return await taskCards.count();
    } catch {
      return 0;
    }
  }

  private getTaskGroup(groupType: 'overdue' | 'today' | 'upcoming' | 'no-date'): Locator {
    switch (groupType) {
      case 'overdue':
        return this.overdueGroup;
      case 'today':
        return this.todayGroup;
      case 'upcoming':
        return this.upcomingGroup;
      case 'no-date':
        return this.noDateGroup;
      default:
        throw new Error(`Unknown group type: ${groupType}`);
    }
  }

  /**
   * Assertion methods
   */
  async assertOnMyTasksPage(): Promise<void> {
    await this.assertUrlContains('/my-tasks');
    await this.assertVisible(this.myTasksHeading);
  }

  async assertTaskInGroup(taskId: string, groupType: 'overdue' | 'today' | 'upcoming' | 'no-date'): Promise<void> {
    const group = this.getTaskGroup(groupType);
    const taskCard = group.locator(`[data-task-id="${taskId}"]`);
    await this.assertVisible(taskCard);
  }

  async assertGroupVisible(groupType: 'overdue' | 'today' | 'upcoming' | 'no-date'): Promise<void> {
    const group = this.getTaskGroup(groupType);
    await this.assertVisible(group);
  }

  async assertEmptyState(): Promise<void> {
    await this.assertVisible(this.emptyState);
  }

  async assertTaskCount(expectedCount: number): Promise<void> {
    await this.assertCount(this.taskCards, expectedCount);
  }

  async assertTaskVisible(taskId: string): Promise<void> {
    const taskCard = this.getTaskCard(taskId);
    await this.assertVisible(taskCard);
  }

  async getTaskCount(): Promise<number> {
    return await this.taskCards.count();
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

  async refreshMyTasks(): Promise<void> {
    await this.reload();
    await this.myTasksHeading.waitFor({ state: 'visible', timeout: 30000 });
  }
}