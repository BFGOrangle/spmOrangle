/**
 * Task Data Builder
 * Provides fluent interface for creating test task data
 */

import { 
  TaskData, 
  SubtaskData, 
  CommentData, 
  ProjectData, 
  RecurrenceData,
  TaskStatus, 
  TaskPriority, 
  RecurrenceType, 
  RecurrenceEndType,
  CollaboratorRole,
  CollaboratorData
} from './task-data-types';

export class TaskDataBuilder {
  private taskData: Partial<TaskData> = {};

  withTitle(title: string): TaskDataBuilder {
    this.taskData.title = title;
    return this;
  }

  withDescription(description: string): TaskDataBuilder {
    this.taskData.description = description;
    return this;
  }

  withPriority(priority: TaskPriority): TaskDataBuilder {
    this.taskData.priority = priority;
    return this;
  }

  withStatus(status: TaskStatus): TaskDataBuilder {
    this.taskData.status = status;
    return this;
  }

  withDueDate(dueDate: string): TaskDataBuilder {
    this.taskData.dueDate = dueDate;
    return this;
  }

  withProject(projectId: string): TaskDataBuilder {
    this.taskData.projectId = projectId;
    return this;
  }

  withAssignees(assignees: string[]): TaskDataBuilder {
    this.taskData.assignees = assignees;
    return this;
  }

  withTags(tags: string[]): TaskDataBuilder {
    this.taskData.tags = tags;
    return this;
  }

  withRecurrence(recurrence: RecurrenceData): TaskDataBuilder {
    this.taskData.recurrence = recurrence;
    return this;
  }

  build(): TaskData {
    // Ensure required fields have defaults
    return {
      title: this.taskData.title || 'E2E Test Task',
      priority: this.taskData.priority || TaskPriority.MEDIUM,
      status: this.taskData.status || TaskStatus.TODO,
      ...this.taskData
    };
  }

  buildMinimal(): TaskData {
    return {
      title: this.taskData.title || 'Minimal E2E Test Task',
      priority: this.taskData.priority || TaskPriority.MEDIUM,
      status: this.taskData.status || TaskStatus.TODO
    };
  }

  buildComplete(): TaskData {
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    
    return {
      title: this.taskData.title || 'Complete E2E Test Task',
      description: this.taskData.description || 'This is a comprehensive test task with all fields populated',
      priority: this.taskData.priority || TaskPriority.HIGH,
      status: this.taskData.status || TaskStatus.TODO,
      dueDate: this.taskData.dueDate || tomorrow.toISOString().split('T')[0],
      assignees: this.taskData.assignees || [],
      tags: this.taskData.tags || ['e2e-test', 'automation'],
      ...this.taskData
    };
  }
}

export class SubtaskDataBuilder {
  private subtaskData: Partial<SubtaskData> = {};

  withTitle(title: string): SubtaskDataBuilder {
    this.subtaskData.title = title;
    return this;
  }

  withNotes(notes: string): SubtaskDataBuilder {
    this.subtaskData.notes = notes;
    return this;
  }

  withStatus(status: TaskStatus): SubtaskDataBuilder {
    this.subtaskData.status = status;
    return this;
  }

  withDueDate(dueDate: string): SubtaskDataBuilder {
    this.subtaskData.dueDate = dueDate;
    return this;
  }

  withParentTask(parentTaskId: string): SubtaskDataBuilder {
    this.subtaskData.parentTaskId = parentTaskId;
    return this;
  }

  build(): SubtaskData {
    return {
      title: this.subtaskData.title || 'E2E Test Subtask',
      status: this.subtaskData.status || TaskStatus.TODO,
      ...this.subtaskData
    };
  }
}

export class CommentDataBuilder {
  private commentData: Partial<CommentData> = {};

  withContent(content: string): CommentDataBuilder {
    this.commentData.content = content;
    return this;
  }

  withMentions(mentions: string[]): CommentDataBuilder {
    this.commentData.mentions = mentions;
    return this;
  }

  withParentComment(parentCommentId: string): CommentDataBuilder {
    this.commentData.parentCommentId = parentCommentId;
    return this;
  }

  withTask(taskId: string): CommentDataBuilder {
    this.commentData.taskId = taskId;
    return this;
  }

  build(): CommentData {
    return {
      content: this.commentData.content || 'This is an E2E test comment',
      ...this.commentData
    };
  }

  buildWithMention(username: string): CommentData {
    return {
      content: `@${username} This is a test comment with a mention`,
      mentions: [username],
      ...this.commentData
    };
  }
}

export class ProjectDataBuilder {
  private projectData: Partial<ProjectData> = {};

  withName(name: string): ProjectDataBuilder {
    this.projectData.name = name;
    return this;
  }

  withDescription(description: string): ProjectDataBuilder {
    this.projectData.description = description;
    return this;
  }

  withDepartment(departmentId: string): ProjectDataBuilder {
    this.projectData.departmentId = departmentId;
    return this;
  }

  withManager(managerId: string): ProjectDataBuilder {
    this.projectData.managerId = managerId;
    return this;
  }

  build(): ProjectData {
    return {
      name: this.projectData.name || 'E2E Test Project',
      description: this.projectData.description || 'This is a test project for E2E testing',
      ...this.projectData
    };
  }
}

export class RecurrenceDataBuilder {
  private recurrenceData: Partial<RecurrenceData> = {};

  withType(type: RecurrenceType): RecurrenceDataBuilder {
    this.recurrenceData.type = type;
    return this;
  }

  withEndType(endType: RecurrenceEndType): RecurrenceDataBuilder {
    this.recurrenceData.endType = endType;
    return this;
  }

  withEndValue(endValue: string | number): RecurrenceDataBuilder {
    this.recurrenceData.endValue = endValue;
    return this;
  }

  build(): RecurrenceData {
    return {
      type: this.recurrenceData.type || RecurrenceType.DAILY,
      endType: this.recurrenceData.endType || RecurrenceEndType.NEVER,
      ...this.recurrenceData
    };
  }

  buildDaily(occurrences: number): RecurrenceData {
    return {
      type: RecurrenceType.DAILY,
      endType: RecurrenceEndType.OCCURRENCES,
      endValue: occurrences
    };
  }

  buildWeekly(endDate: string): RecurrenceData {
    return {
      type: RecurrenceType.WEEKLY,
      endType: RecurrenceEndType.DATE,
      endValue: endDate
    };
  }
}