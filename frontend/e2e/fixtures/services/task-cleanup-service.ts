/**
 * Task Cleanup Service
 * Manages test data cleanup for task management tests
 */

import { Page } from '@playwright/test';

export class TaskCleanupService {
  private createdTasks: string[] = [];
  private createdSubtasks: string[] = [];
  private createdProjects: string[] = [];
  private createdComments: string[] = [];
  private page: Page;

  constructor(page: Page) {
    this.page = page;
  }

  /**
   * Track created entities for cleanup
   */
  async trackTask(taskId: string): Promise<void> {
    if (!this.createdTasks.includes(taskId)) {
      this.createdTasks.push(taskId);
    }
  }

  async trackSubtask(subtaskId: string): Promise<void> {
    if (!this.createdSubtasks.includes(subtaskId)) {
      this.createdSubtasks.push(subtaskId);
    }
  }

  async trackProject(projectId: string): Promise<void> {
    if (!this.createdProjects.includes(projectId)) {
      this.createdProjects.push(projectId);
    }
  }

  async trackComment(commentId: string): Promise<void> {
    if (!this.createdComments.includes(commentId)) {
      this.createdComments.push(commentId);
    }
  }

  /**
   * Cleanup methods
   */
  async cleanupAll(): Promise<void> {
    try {
      await this.cleanupComments();
      await this.cleanupSubtasks();
      await this.cleanupTasks();
      await this.cleanupProjects();
    } catch (error) {
      console.warn('Cleanup failed, but continuing with test execution:', error);
    }
  }

  async cleanupTasks(): Promise<void> {
    for (const taskId of this.createdTasks) {
      try {
        await this.deleteTaskViaAPI(taskId);
      } catch (error) {
        console.warn(`Failed to cleanup task ${taskId}:`, error);
      }
    }
    this.createdTasks = [];
  }

  async cleanupSubtasks(): Promise<void> {
    for (const subtaskId of this.createdSubtasks) {
      try {
        await this.deleteSubtaskViaAPI(subtaskId);
      } catch (error) {
        console.warn(`Failed to cleanup subtask ${subtaskId}:`, error);
      }
    }
    this.createdSubtasks = [];
  }

  async cleanupProjects(): Promise<void> {
    for (const projectId of this.createdProjects) {
      try {
        await this.deleteProjectViaAPI(projectId);
      } catch (error) {
        console.warn(`Failed to cleanup project ${projectId}:`, error);
      }
    }
    this.createdProjects = [];
  }

  async cleanupComments(): Promise<void> {
    for (const commentId of this.createdComments) {
      try {
        await this.deleteCommentViaAPI(commentId);
      } catch (error) {
        console.warn(`Failed to cleanup comment ${commentId}:`, error);
      }
    }
    this.createdComments = [];
  }

  /**
   * API cleanup methods (to be implemented when API helpers are available)
   */
  private async deleteTaskViaAPI(taskId: string): Promise<void> {
    // TODO: Implement API call to delete task
    // This will use the TaskManagementAPI helper when available
    console.log(`Would delete task ${taskId} via API`);
  }

  private async deleteSubtaskViaAPI(subtaskId: string): Promise<void> {
    // TODO: Implement API call to delete subtask
    console.log(`Would delete subtask ${subtaskId} via API`);
  }

  private async deleteProjectViaAPI(projectId: string): Promise<void> {
    // TODO: Implement API call to delete project
    console.log(`Would delete project ${projectId} via API`);
  }

  private async deleteCommentViaAPI(commentId: string): Promise<void> {
    // TODO: Implement API call to delete comment
    console.log(`Would delete comment ${commentId} via API`);
  }

  /**
   * UI cleanup methods (fallback when API is not available)
   */
  async cleanupTaskViaUI(taskId: string): Promise<void> {
    try {
      // Navigate to task detail page
      await this.page.goto(`/tasks/${taskId}`);
      
      // Look for delete button (implementation depends on UI structure)
      const deleteButton = this.page.getByRole('button', { name: /delete/i });
      if (await deleteButton.isVisible()) {
        await deleteButton.click();
        
        // Handle confirmation dialog
        const confirmButton = this.page.getByRole('button', { name: /confirm|yes|delete/i });
        if (await confirmButton.isVisible()) {
          await confirmButton.click();
        }
      }
    } catch (error) {
      console.warn(`Failed to cleanup task ${taskId} via UI:`, error);
    }
  }

  /**
   * Verification methods
   */
  async verifyTaskDeleted(taskId: string): Promise<boolean> {
    try {
      // Try to navigate to the task - should result in 404 or error
      const response = await this.page.goto(`/tasks/${taskId}`);
      return response?.status() === 404;
    } catch {
      return true; // Assume deleted if navigation fails
    }
  }

  async verifyProjectDeleted(projectId: string): Promise<boolean> {
    try {
      const response = await this.page.goto(`/projects/${projectId}`);
      return response?.status() === 404;
    } catch {
      return true;
    }
  }

  /**
   * Utility methods
   */
  getTrackedTaskCount(): number {
    return this.createdTasks.length;
  }

  getTrackedProjectCount(): number {
    return this.createdProjects.length;
  }

  getTrackedSubtaskCount(): number {
    return this.createdSubtasks.length;
  }

  getTrackedCommentCount(): number {
    return this.createdComments.length;
  }

  clearTracking(): void {
    this.createdTasks = [];
    this.createdSubtasks = [];
    this.createdProjects = [];
    this.createdComments = [];
  }

  /**
   * Bulk cleanup for performance
   */
  async bulkCleanupTasks(taskIds: string[]): Promise<void> {
    // Add to tracking
    taskIds.forEach(id => this.createdTasks.push(id));
    
    // Perform bulk cleanup
    await this.cleanupTasks();
  }

  /**
   * Emergency cleanup - removes all test data regardless of tracking
   */
  async emergencyCleanup(): Promise<void> {
    try {
      // This would implement a more aggressive cleanup strategy
      // For example, deleting all tasks with specific test prefixes
      console.log('Performing emergency cleanup of test data');
      
      // TODO: Implement emergency cleanup logic
      // This might involve API calls to delete all tasks with "E2E Test" in the title
    } catch (error) {
      console.error('Emergency cleanup failed:', error);
    }
  }
}