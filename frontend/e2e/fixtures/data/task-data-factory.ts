/**
 * Task Data Factory
 * Provides pre-configured test data for common scenarios
 */

import { 
  TaskDataBuilder, 
  SubtaskDataBuilder, 
  CommentDataBuilder, 
  ProjectDataBuilder,
  RecurrenceDataBuilder
} from './task-data-builder';
import { 
  TaskData, 
  SubtaskData, 
  CommentData, 
  ProjectData, 
  RecurrenceData,
  TaskStatus, 
  TaskPriority, 
  RecurrenceType,
  CollaboratorData,
  CollaboratorRole
} from './task-data-types';

export class TestDataFactory {
  /**
   * API Integration Methods
   */
  static async createTaskViaAPI(taskData: TaskData): Promise<string> {
    // TODO: Implement actual API call to create task
    // For now, return a mock task ID
    const mockTaskId = `task-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
    console.log(`Mock: Created task ${mockTaskId} with data:`, taskData);
    return mockTaskId;
  }

  /**
   * Task Factory Methods
   */
  static createStandaloneTask(): TaskData {
    return new TaskDataBuilder()
      .withTitle('Standalone E2E Test Task')
      .withDescription('This is a standalone task for E2E testing')
      .withPriority(TaskPriority.MEDIUM)
      .withStatus(TaskStatus.TODO)
      .build();
  }

  static createProjectTask(projectId: string): TaskData {
    return new TaskDataBuilder()
      .withTitle('Project E2E Test Task')
      .withDescription('This is a project task for E2E testing')
      .withProject(projectId)
      .withPriority(TaskPriority.HIGH)
      .withStatus(TaskStatus.TODO)
      .build();
  }

  static createHighPriorityTask(): TaskData {
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    
    return new TaskDataBuilder()
      .withTitle('High Priority E2E Test Task')
      .withDescription('This is a high priority task with due date')
      .withPriority(TaskPriority.HIGH)
      .withStatus(TaskStatus.TODO)
      .withDueDate(tomorrow.toISOString().split('T')[0])
      .withTags(['urgent', 'e2e-test'])
      .build();
  }

  static createRecurringTask(recurrence: RecurrenceData): TaskData {
    return new TaskDataBuilder()
      .withTitle('Recurring E2E Test Task')
      .withDescription('This is a recurring task for E2E testing')
      .withPriority(TaskPriority.MEDIUM)
      .withStatus(TaskStatus.TODO)
      .withRecurrence(recurrence)
      .build();
  }

  static createTaskWithSubtasks(subtaskCount: number = 3): TaskData {
    return new TaskDataBuilder()
      .withTitle(`E2E Test Task with ${subtaskCount} Subtasks`)
      .withDescription('This task will have multiple subtasks for testing')
      .withPriority(TaskPriority.MEDIUM)
      .withStatus(TaskStatus.IN_PROGRESS)
      .build();
  }

  static createTaskWithCollaborators(collaboratorIds: string[]): TaskData {
    return new TaskDataBuilder()
      .withTitle('E2E Test Task with Collaborators')
      .withDescription('This task has multiple collaborators for testing')
      .withPriority(TaskPriority.MEDIUM)
      .withStatus(TaskStatus.TODO)
      .withAssignees(collaboratorIds)
      .build();
  }

  static createOverdueTask(): TaskData {
    const yesterday = new Date();
    yesterday.setDate(yesterday.getDate() - 1);
    
    return new TaskDataBuilder()
      .withTitle('Overdue E2E Test Task')
      .withDescription('This task is overdue for testing overdue indicators')
      .withPriority(TaskPriority.HIGH)
      .withStatus(TaskStatus.IN_PROGRESS)
      .withDueDate(yesterday.toISOString().split('T')[0])
      .build();
  }

  /**
   * Subtask Factory Methods
   */
  static createSubtask(parentTaskId: string): SubtaskData {
    return new SubtaskDataBuilder()
      .withTitle('E2E Test Subtask')
      .withNotes('This is a subtask for E2E testing')
      .withStatus(TaskStatus.TODO)
      .withParentTask(parentTaskId)
      .build();
  }

  static createMultipleSubtasks(parentTaskId: string, count: number): SubtaskData[] {
    const subtasks: SubtaskData[] = [];
    for (let i = 1; i <= count; i++) {
      subtasks.push(
        new SubtaskDataBuilder()
          .withTitle(`E2E Test Subtask ${i}`)
          .withNotes(`This is subtask ${i} for E2E testing`)
          .withStatus(i <= Math.floor(count / 2) ? TaskStatus.COMPLETED : TaskStatus.TODO)
          .withParentTask(parentTaskId)
          .build()
      );
    }
    return subtasks;
  }

  /**
   * Comment Factory Methods
   */
  static createComment(taskId: string): CommentData {
    return new CommentDataBuilder()
      .withContent('This is an E2E test comment')
      .withTask(taskId)
      .build();
  }

  static createThreadedComment(parentCommentId: string, taskId: string): CommentData {
    return new CommentDataBuilder()
      .withContent('This is a threaded reply for E2E testing')
      .withParentComment(parentCommentId)
      .withTask(taskId)
      .build();
  }

  static createCommentWithMention(taskId: string, mentionedUser: string): CommentData {
    return new CommentDataBuilder()
      .withContent(`@${mentionedUser} Please review this task`)
      .withMentions([mentionedUser])
      .withTask(taskId)
      .build();
  }

  /**
   * Project Factory Methods
   */
  static createTestProject(): ProjectData {
    return new ProjectDataBuilder()
      .withName('E2E Test Project')
      .withDescription('This is a test project for E2E testing')
      .build();
  }

  static createProjectWithManager(managerId: string): ProjectData {
    return new ProjectDataBuilder()
      .withName('E2E Test Project with Manager')
      .withDescription('This project has a specific manager for testing')
      .withManager(managerId)
      .build();
  }

  /**
   * Recurrence Factory Methods
   */
  static createDailyRecurrence(occurrences: number): RecurrenceData {
    return new RecurrenceDataBuilder()
      .buildDaily(occurrences);
  }

  static createWeeklyRecurrence(endDate: string): RecurrenceData {
    return new RecurrenceDataBuilder()
      .buildWeekly(endDate);
  }

  static createNeverEndingRecurrence(): RecurrenceData {
    return new RecurrenceDataBuilder()
      .withType(RecurrenceType.WEEKLY)
      .build(); // Uses default NEVER end type
  }

  /**
   * Collaborator Factory Methods
   */
  static createCollaborator(userId: string, role: CollaboratorRole = CollaboratorRole.EDITOR): CollaboratorData {
    return {
      userId,
      role
    };
  }

  static createMultipleCollaborators(userIds: string[], role: CollaboratorRole = CollaboratorRole.EDITOR): CollaboratorData[] {
    return userIds.map(userId => this.createCollaborator(userId, role));
  }

  /**
   * Bulk Data Factory Methods
   */
  static createBulkTasks(count: number, projectId?: string): TaskData[] {
    const tasks: TaskData[] = [];
    for (let i = 1; i <= count; i++) {
      const builder = new TaskDataBuilder()
        .withTitle(`Bulk E2E Test Task ${i}`)
        .withDescription(`This is bulk task ${i} for E2E testing`)
        .withPriority(i % 3 === 0 ? TaskPriority.HIGH : TaskPriority.MEDIUM)
        .withStatus(i % 4 === 0 ? TaskStatus.COMPLETED : TaskStatus.TODO);
      
      if (projectId) {
        builder.withProject(projectId);
      }
      
      tasks.push(builder.build());
    }
    return tasks;
  }

  static createMixedStatusTasks(count: number): TaskData[] {
    const tasks: TaskData[] = [];
    const statuses = [TaskStatus.TODO, TaskStatus.IN_PROGRESS, TaskStatus.COMPLETED, TaskStatus.BLOCKED];
    
    for (let i = 1; i <= count; i++) {
      tasks.push(
        new TaskDataBuilder()
          .withTitle(`Mixed Status Task ${i}`)
          .withStatus(statuses[i % statuses.length])
          .withPriority(TaskPriority.MEDIUM)
          .build()
      );
    }
    return tasks;
  }
}