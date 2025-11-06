import { Page, Locator, expect } from '@playwright/test';
import { BasePage } from './base-page';
import { TaskData, SubtaskData, CommentData, TaskStatus, TaskPriority, CollaboratorRole, CommentFilter, ActivityLogEntry } from '../data/task-data-types';

/**
 * Task Detail Page Object
 * Encapsulates interactions with individual task detail pages
 */
export class TaskDetailPage extends BasePage {
  constructor(page: Page) {
    super(page);
  }

  /**
   * Selectors - using user-facing methods and data-testid where appropriate
   */
  private get taskTitle(): Locator {
    return this.getByTestId('task-title');
  }

  private get taskDescription(): Locator {
    return this.getByTestId('task-description');
  }

  private get taskStatus(): Locator {
    return this.getByTestId('task-status-dropdown');
  }

  private get taskPriority(): Locator {
    return this.getByTestId('task-priority-dropdown');
  }

  private get taskDueDate(): Locator {
    return this.getByTestId('task-due-date-input');
  }

  private get editButton(): Locator {
    return this.getByRole('button', { name: /edit/i });
  }

  private get saveButton(): Locator {
    return this.getByRole('button', { name: /save/i });
  }

  private get cancelButton(): Locator {
    return this.getByRole('button', { name: /cancel/i });
  }

  private get deleteButton(): Locator {
    return this.getByRole('button', { name: /delete/i });
  }

  // Subtask selectors
  private get subtaskSection(): Locator {
    return this.getByTestId('subtask-section');
  }

  private get subtaskList(): Locator {
    return this.getByTestId('subtask-list');
  }

  private get addSubtaskButton(): Locator {
    return this.getByRole('button', { name: /add subtask/i });
  }

  private get subtaskRollup(): Locator {
    return this.getByTestId('subtask-rollup');
  }

  private get expandSubtasksButton(): Locator {
    return this.getByRole('button', { name: /expand subtasks/i });
  }

  // Collaboration selectors
  private get collaboratorSection(): Locator {
    return this.getByTestId('collaborator-section');
  }

  private get addCollaboratorButton(): Locator {
    return this.getByRole('button', { name: /add collaborator/i });
  }

  private get collaboratorList(): Locator {
    return this.getByTestId('collaborator-list');
  }

  // Comment selectors
  private get commentSection(): Locator {
    return this.getByTestId('comment-section');
  }

  private get commentInput(): Locator {
    return this.getByTestId('comment-input');
  }

  private get postCommentButton(): Locator {
    return this.getByRole('button', { name: /post comment/i });
  }

  private get commentList(): Locator {
    return this.getByTestId('comment-list');
  }

  private get commentFilter(): Locator {
    return this.getByTestId('comment-filter');
  }

  // Activity log selectors
  private get activityLogSection(): Locator {
    return this.getByTestId('activity-log-section');
  }

  private get activityLogList(): Locator {
    return this.getByTestId('activity-log-list');
  }

  private get activityLogFilter(): Locator {
    return this.getByTestId('activity-log-filter');
  }

  /**
   * Navigation methods
   */
  async navigate(taskId: string): Promise<void> {
    await this.goto(`/tasks/${taskId}`);
    await this.taskTitle.waitFor({ state: 'visible', timeout: 30000 });
  }

  /**
   * Task property update methods
   */
  async updateTitle(title: string): Promise<void> {
    await this.click(this.editButton);
    
    const titleInput = this.getByLabel(/title/i);
    await this.fill(titleInput, title);
    
    await this.click(this.saveButton);
    await this.waitForToast(/task updated/i);
  }

  async updateDescription(description: string): Promise<void> {
    await this.click(this.editButton);
    
    const descriptionInput = this.getByLabel(/description/i);
    await this.fill(descriptionInput, description);
    
    await this.click(this.saveButton);
    await this.waitForToast(/task updated/i);
  }

  async updatePriority(priority: TaskPriority): Promise<void> {
    await this.click(this.taskPriority);
    await this.page.getByRole('option', { name: priority.toString() }).click();
    await this.waitForToast(/priority updated/i);
  }

  async updateDueDate(dueDate: string): Promise<void> {
    await this.click(this.editButton);
    await this.fill(this.taskDueDate, dueDate);
    await this.click(this.saveButton);
    await this.waitForToast(/due date updated/i);
  }

  async updateStatus(status: TaskStatus): Promise<void> {
    await this.click(this.taskStatus);
    await this.page.getByRole('option', { name: status }).click();
    await this.waitForToast(/status updated/i);
  }

  /**
   * Subtask management methods
   */
  async createSubtask(subtaskData: SubtaskData): Promise<string> {
    await this.click(this.addSubtaskButton);
    
    // Fill subtask form
    const titleInput = this.getByLabel(/subtask title/i);
    await this.fill(titleInput, subtaskData.title);
    
    if (subtaskData.notes) {
      const notesInput = this.getByLabel(/notes/i);
      await this.fill(notesInput, subtaskData.notes);
    }
    
    if (subtaskData.dueDate) {
      const dueDateInput = this.getByLabel(/due date/i);
      await this.fill(dueDateInput, subtaskData.dueDate);
    }
    
    // Submit subtask creation
    const createButton = this.getByRole('button', { name: /create subtask/i });
    await this.click(createButton);
    
    await this.waitForToast(/subtask created/i);
    
    // Extract subtask ID (placeholder implementation)
    return 'generated-subtask-id';
  }

  async expandSubtasks(): Promise<void> {
    if (await this.expandSubtasksButton.isVisible()) {
      await this.click(this.expandSubtasksButton);
      await this.assertVisible(this.subtaskList);
    }
  }

  async getSubtaskCount(): Promise<number> {
    const rollupText = await this.subtaskRollup.textContent();
    const match = rollupText?.match(/(\d+)\/(\d+)/);
    return match ? parseInt(match[2]) : 0;
  }

  async deleteSubtask(subtaskId: string): Promise<void> {
    const subtaskElement = this.getSubtaskElement(subtaskId);
    const deleteButton = subtaskElement.locator('[data-testid="subtask-delete-button"]');
    
    await this.click(deleteButton);
    
    // Handle confirmation
    const confirmButton = this.getByRole('button', { name: /confirm|delete/i });
    await this.click(confirmButton);
    
    await this.waitForToast(/subtask deleted/i);
  }

  private getSubtaskElement(subtaskId: string): Locator {
    return this.page.locator(`[data-testid="subtask-item"][data-subtask-id="${subtaskId}"]`);
  }

  /**
   * Collaboration methods
   */
  async addCollaborator(userId: string, role: CollaboratorRole): Promise<void> {
    await this.click(this.addCollaboratorButton);
    
    // Search for user
    const userSearchInput = this.getByLabel(/search users/i);
    await this.fill(userSearchInput, userId);
    
    // Select user from dropdown
    const userOption = this.page.getByRole('option', { name: userId });
    await this.click(userOption);
    
    // Select role
    const roleSelect = this.getByLabel(/role/i);
    await this.click(roleSelect);
    await this.page.getByRole('option', { name: role }).click();
    
    // Add collaborator
    const addButton = this.getByRole('button', { name: /add/i });
    await this.click(addButton);
    
    await this.waitForToast(/collaborator added/i);
  }

  async removeCollaborator(userId: string): Promise<void> {
    const collaboratorElement = this.getCollaboratorElement(userId);
    const removeButton = collaboratorElement.locator('[data-testid="remove-collaborator-button"]');
    
    await this.click(removeButton);
    
    // Handle confirmation
    const confirmButton = this.getByRole('button', { name: /confirm|remove/i });
    await this.click(confirmButton);
    
    await this.waitForToast(/collaborator removed/i);
  }

  async getCollaboratorCount(): Promise<number> {
    const collaborators = await this.collaboratorList.locator('[data-testid="collaborator-item"]').all();
    return collaborators.length;
  }

  private getCollaboratorElement(userId: string): Locator {
    return this.page.locator(`[data-testid="collaborator-item"][data-user-id="${userId}"]`);
  }

  /**
   * Comment methods
   */
  async postComment(content: string): Promise<string> {
    await this.fill(this.commentInput, content);
    await this.click(this.postCommentButton);
    
    await this.waitForToast(/comment posted/i);
    
    // Extract comment ID (placeholder implementation)
    return 'generated-comment-id';
  }

  async replyToComment(commentId: string, content: string): Promise<string> {
    const commentElement = this.getCommentElement(commentId);
    const replyButton = commentElement.locator('[data-testid="reply-button"]');
    
    await this.click(replyButton);
    
    const replyInput = commentElement.locator('[data-testid="reply-input"]');
    await this.fill(replyInput, content);
    
    const submitReplyButton = commentElement.locator('[data-testid="submit-reply-button"]');
    await this.click(submitReplyButton);
    
    await this.waitForToast(/reply posted/i);
    
    return 'generated-reply-id';
  }

  async mentionUser(username: string): Promise<void> {
    await this.fill(this.commentInput, `@${username} `);
    
    // Wait for mention suggestions to appear
    const mentionSuggestions = this.page.locator('[data-testid="mention-suggestions"]');
    await this.assertVisible(mentionSuggestions);
    
    // Select the user from suggestions
    const userSuggestion = this.page.getByRole('option', { name: username });
    await this.click(userSuggestion);
  }

  async filterComments(filter: CommentFilter): Promise<void> {
    await this.click(this.commentFilter);
    
    const filterLabels: Record<CommentFilter, string> = {
      [CommentFilter.ALL]: 'All Comments',
      [CommentFilter.UNRESOLVED]: 'Unresolved',
      [CommentFilter.BY_COMMENTER]: 'By Commenter'
    };
    
    await this.page.getByRole('option', { name: filterLabels[filter] }).click();
    
    // Wait for filter to be applied
    await this.waitForCommentListUpdate();
  }

  private getCommentElement(commentId: string): Locator {
    return this.page.locator(`[data-testid="comment-item"][data-comment-id="${commentId}"]`);
  }

  private async waitForCommentListUpdate(): Promise<void> {
    // Wait for comment list to update
    await this.commentList.waitFor({ state: 'visible', timeout: 5000 });
  }

  /**
   * Assertion methods
   */
  async assertTaskTitle(expectedTitle: string): Promise<void> {
    await this.assertText(this.taskTitle, expectedTitle);
  }

  async assertSubtaskRollup(expectedRollup: string): Promise<void> {
    await this.assertText(this.subtaskRollup, expectedRollup);
  }

  async assertCollaboratorPresent(userId: string): Promise<void> {
    const collaboratorElement = this.getCollaboratorElement(userId);
    await this.assertVisible(collaboratorElement);
  }

  async assertCommentVisible(commentId: string): Promise<void> {
    const commentElement = this.getCommentElement(commentId);
    await this.assertVisible(commentElement);
  }

  async assertTaskStatus(expectedStatus: TaskStatus): Promise<void> {
    const statusText = await this.taskStatus.textContent();
    expect(statusText).toContain(expectedStatus);
  }

  async assertTaskPriority(expectedPriority: TaskPriority): Promise<void> {
    const priorityText = await this.taskPriority.textContent();
    expect(priorityText).toContain(expectedPriority.toString());
  }

  async assertProjectAssociation(projectId: string): Promise<void> {
    // Look for project information in the task detail page
    const projectSection = this.getByTestId('task-project-info');
    await this.assertVisible(projectSection);
    
    // Verify project ID or name is displayed
    const projectInfo = await projectSection.textContent();
    expect(projectInfo).toContain(projectId);
  }

  /**
   * Activity log methods
   */
  async getActivityLog(): Promise<ActivityLogEntry[]> {
    await this.assertVisible(this.activityLogSection);
    
    const activityItems = await this.activityLogList.locator('[data-testid="activity-log-item"]').all();
    const activities: ActivityLogEntry[] = [];
    
    for (const item of activityItems) {
      const type = await item.getAttribute('data-activity-type') || '';
      const timestamp = await item.locator('[data-testid="activity-timestamp"]').textContent() || '';
      const editor = await item.locator('[data-testid="activity-editor"]').textContent() || '';
      const details = await item.locator('[data-testid="activity-details"]').textContent() || '';
      const source = await item.getAttribute('data-activity-source') || '';
      
      activities.push({
        type,
        timestamp,
        editor,
        details,
        source
      });
    }
    
    return activities;
  }

  async filterActivityLog(filterType: string): Promise<void> {
    await this.click(this.activityLogFilter);
    await this.page.getByRole('option', { name: filterType }).click();
    
    // Wait for filter to be applied
    await this.activityLogList.waitFor({ state: 'visible', timeout: 5000 });
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

  async isEditMode(): Promise<boolean> {
    return await this.saveButton.isVisible();
  }

  async enterEditMode(): Promise<void> {
    if (!(await this.isEditMode())) {
      await this.click(this.editButton);
      await this.assertVisible(this.saveButton);
    }
  }

  async exitEditMode(): Promise<void> {
    if (await this.isEditMode()) {
      await this.click(this.cancelButton);
      await this.assertVisible(this.editButton);
    }
  }
}