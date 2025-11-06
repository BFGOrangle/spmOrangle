import { Page, Locator, expect } from '@playwright/test';
import { BasePage } from './base-page';
import { TaskData, TaskStatus, TaskPriority, TaskFilters, SortOptions, TaskSummary, StatusCounts } from '../data/task-data-types';

/**
 * Tasks Page Object
 * Encapsulates interactions with the main tasks page
 */
export class TasksPage extends BasePage {
  private route = '/tasks';

  constructor(page: Page) {
    super(page);
  }

  /**
   * Selectors - using user-facing methods and data-testid where appropriate
   */
  private get createTaskButton(): Locator {
    return this.getByRole('button', { name: /create task/i });
  }

  private get taskList(): Locator {
    return this.getByTestId('task-list');
  }

  private get taskCards(): Locator {
    return this.getByTestId('task-card');
  }

  private get statusFilter(): Locator {
    return this.getByLabel('Status Filter');
  }

  private get priorityFilter(): Locator {
    return this.getByLabel('Priority Filter');
  }

  private get projectFilter(): Locator {
    return this.getByLabel('Project Filter');
  }

  private get dueDateFilter(): Locator {
    return this.getByLabel('Due Date Filter');
  }

  private get sortDropdown(): Locator {
    return this.getByLabel('Sort By');
  }

  private get searchInput(): Locator {
    return this.getByPlaceholder(/search tasks/i);
  }

  private get loadingIndicator(): Locator {
    return this.getByText(/loading/i);
  }

  private get emptyState(): Locator {
    return this.getByText(/no tasks found/i);
  }

  private get tasksHeading(): Locator {
    return this.getByRole('heading', { name: 'Tasks', exact: true });
  }

  /**
   * Navigation methods
   */
  async navigate(): Promise<void> {
    await this.goto(this.route);
    await this.tasksHeading.waitFor({ state: 'visible', timeout: 30000 });
  }

  async navigateToMyTasks(): Promise<void> {
    await this.goto('/tasks/my-tasks');
    await this.waitForPageLoad();
  }

  async navigateToProjectTasks(projectId: string): Promise<void> {
    await this.goto(`/projects/${projectId}/tasks`);
    await this.waitForPageLoad();
  }

  /**
   * Task creation methods
   */
  async clickCreateTask(): Promise<void> {
    await this.click(this.createTaskButton);
  }

  async createStandaloneTask(taskData: TaskData): Promise<string> {
    await this.clickCreateTask();
    
    // Fill task creation form (implementation depends on actual form structure)
    await this.fillTaskForm(taskData);
    
    // Submit form
    const submitButton = this.getByRole('button', { name: /create|save/i });
    await this.click(submitButton);
    
    // Wait for task to be created and get ID
    await this.waitForToast(/task created/i);
    
    // Extract task ID from URL or response (placeholder implementation)
    const taskId = await this.extractTaskIdFromResponse();
    return taskId;
  }

  async createProjectTask(taskData: TaskData, projectId: string): Promise<string> {
    // Navigate to project tasks first
    await this.navigateToProjectTasks(projectId);
    
    // Create task within project context
    const taskWithProject = { ...taskData, projectId };
    return await this.createStandaloneTask(taskWithProject);
  }

  private async fillTaskForm(taskData: TaskData): Promise<void> {
    // Fill title
    const titleInput = this.getByLabel(/title/i);
    await this.fill(titleInput, taskData.title);
    
    // Fill description if provided
    if (taskData.description) {
      const descriptionInput = this.getByLabel(/description/i);
      await this.fill(descriptionInput, taskData.description);
    }
    
    // Set priority
    const prioritySelect = this.getByLabel(/priority/i);
    await this.click(prioritySelect);
    await this.page.getByRole('option', { name: taskData.priority.toString() }).click();
    
    // Set due date if provided
    if (taskData.dueDate) {
      const dueDateInput = this.getByLabel(/due date/i);
      await this.fill(dueDateInput, taskData.dueDate);
    }
    
    // Add tags if provided
    if (taskData.tags && taskData.tags.length > 0) {
      for (const tag of taskData.tags) {
        const tagInput = this.getByLabel(/tags/i);
        await this.fill(tagInput, tag);
        await this.pressKey('Enter');
      }
    }
  }

  private async extractTaskIdFromResponse(): Promise<string> {
    // TODO: Implement based on actual API response or URL structure
    // This is a placeholder implementation
    const url = await this.getCurrentUrl();
    const match = url.match(/\/tasks\/([^\/]+)/);
    return match ? match[1] : 'generated-task-id';
  }

  /**
   * Task management methods
   */
  async openTaskDetail(taskId: string): Promise<void> {
    const taskCard = this.getTaskCard(taskId);
    await this.click(taskCard);
  }

  async updateTaskStatus(taskId: string, status: TaskStatus): Promise<void> {
    const taskCard = this.getTaskCard(taskId);
    const statusDropdown = taskCard.locator('[data-testid="task-status-dropdown"]');
    
    await this.click(statusDropdown);
    await this.page.getByRole('option', { name: status }).click();
    
    // Wait for update to complete
    await this.waitForToast(/status updated/i);
  }

  async deleteTask(taskId: string): Promise<void> {
    const taskCard = this.getTaskCard(taskId);
    const deleteButton = taskCard.locator('[data-testid="task-delete-button"]');
    
    await this.click(deleteButton);
    
    // Handle confirmation dialog
    const confirmButton = this.getByRole('button', { name: /confirm|delete/i });
    await this.click(confirmButton);
    
    // Wait for deletion to complete
    await this.waitForToast(/task deleted/i);
  }

  private getTaskCard(taskId: string): Locator {
    return this.page.locator(`[data-testid="task-card"][data-task-id="${taskId}"]`);
  }

  /**
   * Filtering and sorting methods
   */
  async applyStatusFilter(statuses: TaskStatus[]): Promise<void> {
    await this.click(this.statusFilter);
    
    // Clear existing selections first
    const clearButton = this.page.getByRole('button', { name: /clear/i });
    if (await clearButton.isVisible()) {
      await this.click(clearButton);
    }
    
    // Select new statuses
    for (const status of statuses) {
      await this.page.getByRole('option', { name: status }).click();
    }
    
    // Apply filter
    const applyButton = this.page.getByRole('button', { name: /apply/i });
    await this.click(applyButton);
    
    // Wait for filter to be applied
    await this.waitForTaskListUpdate();
  }

  async applyDateRangeFilter(startDate: string, endDate: string): Promise<void> {
    await this.click(this.dueDateFilter);
    
    const startDateInput = this.page.getByLabel(/start date/i);
    const endDateInput = this.page.getByLabel(/end date/i);
    
    await this.fill(startDateInput, startDate);
    await this.fill(endDateInput, endDate);
    
    const applyButton = this.page.getByRole('button', { name: /apply/i });
    await this.click(applyButton);
    
    await this.waitForTaskListUpdate();
  }

  async applyProjectFilter(projectId: string): Promise<void> {
    await this.click(this.projectFilter);
    await this.page.getByRole('option', { name: projectId }).click();
    await this.waitForTaskListUpdate();
  }

  async sortBy(sortField: SortOptions['field']): Promise<void> {
    await this.click(this.sortDropdown);
    
    const sortLabels: Record<SortOptions['field'], string> = {
      dueDate: 'Due Date',
      status: 'Status',
      priority: 'Priority',
      lastUpdated: 'Last Updated',
      title: 'Title'
    };
    
    await this.page.getByRole('option', { name: sortLabels[sortField] }).click();
    await this.waitForTaskListUpdate();
  }

  async searchTasks(query: string): Promise<void> {
    await this.fill(this.searchInput, query);
    await this.pressKey('Enter');
    await this.waitForTaskListUpdate();
  }

  private async waitForTaskListUpdate(): Promise<void> {
    // Wait for loading indicator to appear and disappear
    try {
      await this.loadingIndicator.waitFor({ state: 'visible', timeout: 2000 });
      await this.loadingIndicator.waitFor({ state: 'hidden', timeout: 10000 });
    } catch {
      // Loading might be too fast to catch
    }
    
    // Wait for task list to be stable
    await this.taskList.waitFor({ state: 'visible', timeout: 10000 });
  }

  /**
   * Assertion methods
   */
  async assertTaskVisible(taskId: string): Promise<void> {
    const taskCard = this.getTaskCard(taskId);
    await this.assertVisible(taskCard);
  }

  async assertTaskCount(expectedCount: number): Promise<void> {
    await this.assertCount(this.taskCards, expectedCount);
  }

  async assertTaskStatus(taskId: string, expectedStatus: TaskStatus): Promise<void> {
    const taskCard = this.getTaskCard(taskId);
    const statusElement = taskCard.locator('[data-testid="task-status"]');
    await this.assertText(statusElement, expectedStatus);
  }

  async assertEmptyState(): Promise<void> {
    await this.assertVisible(this.emptyState);
  }

  async assertTasksLoaded(): Promise<void> {
    await this.assertVisible(this.taskList);
    await this.assertHidden(this.loadingIndicator);
  }

  /**
   * Data extraction methods
   */
  async getTaskList(): Promise<TaskSummary[]> {
    await this.assertTasksLoaded();
    
    const taskElements = await this.taskCards.all();
    const tasks: TaskSummary[] = [];
    
    for (const taskElement of taskElements) {
      const id = await taskElement.getAttribute('data-task-id') || '';
      const title = await taskElement.locator('[data-testid="task-title"]').textContent() || '';
      const statusText = await taskElement.locator('[data-testid="task-status"]').textContent() || '';
      const priorityText = await taskElement.locator('[data-testid="task-priority"]').textContent() || '';
      const dueDateText = await taskElement.locator('[data-testid="task-due-date"]').textContent();
      const assigneeCountText = await taskElement.locator('[data-testid="task-assignee-count"]').textContent() || '0';
      
      tasks.push({
        id,
        title,
        status: statusText as TaskStatus,
        priority: parseInt(priorityText) as TaskPriority,
        dueDate: dueDateText || undefined,
        assigneeCount: parseInt(assigneeCountText)
      });
    }
    
    return tasks;
  }

  async getTaskStatusCounts(): Promise<StatusCounts> {
    const tasks = await this.getTaskList();
    
    return {
      todo: tasks.filter(t => t.status === TaskStatus.TODO).length,
      inProgress: tasks.filter(t => t.status === TaskStatus.IN_PROGRESS).length,
      completed: tasks.filter(t => t.status === TaskStatus.COMPLETED).length,
      blocked: tasks.filter(t => t.status === TaskStatus.BLOCKED).length
    };
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
    
    // Fallback to page-level text search
    await this.page.getByText(message).first().waitFor({ state: 'visible', timeout: 3000 });
  }

  async refreshTaskList(): Promise<void> {
    await this.reload();
    await this.waitForTaskListUpdate();
  }
}