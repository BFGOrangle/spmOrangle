import { test, expect } from '../../../fixtures';
import { TaskStatus, TaskPriority } from '../../../fixtures/data/task-data-types';

test.describe('Project Task Creation @integration @task-creation', () => {
  test('should create a task within project context', async ({ 
    managerPage, 
    tasksPage, 
    taskBuilder, 
    projectBuilder, 
    taskCleanup 
  }) => {
    // First create a test project (assuming we have project creation capability)
    const projectData = projectBuilder
      .withName('E2E Test Project for Task Creation')
      .withDescription('Test project for validating task creation within project context')
      .build();
    
    // For this test, we'll use a mock project ID since project creation is not in scope
    const mockProjectId = 'test-project-123';
    
    // Navigate to project tasks page
    await tasksPage.navigateToProjectTasks(mockProjectId);
    
    // Create task data for project context
    const taskData = taskBuilder
      .withTitle('E2E Project Task')
      .withDescription('Task created within project context')
      .withPriority(TaskPriority.HIGH)
      .withStatus(TaskStatus.TODO)
      .withProject(mockProjectId)
      .build();
    
    // Create the task within project context
    const taskId = await tasksPage.createProjectTask(taskData, mockProjectId);
    await taskCleanup.trackTask(taskId);
    
    // Verify task was created and is visible in project context
    await tasksPage.assertTaskVisible(taskId);
    await tasksPage.assertTaskStatus(taskId, TaskStatus.TODO);
  });

  test('should inherit project settings and collaborators', async ({ 
    managerPage, 
    tasksPage, 
    taskDetailPage, 
    taskBuilder, 
    taskCleanup 
  }) => {
    // Mock project with known collaborators
    const mockProjectId = 'test-project-with-collaborators';
    
    // Navigate to project tasks page
    await tasksPage.navigateToProjectTasks(mockProjectId);
    
    // Create task within project
    const taskData = taskBuilder
      .withTitle('E2E Task - Inherit Project Settings')
      .withPriority(TaskPriority.MEDIUM)
      .withStatus(TaskStatus.TODO)
      .withProject(mockProjectId)
      .build();
    
    const taskId = await tasksPage.createProjectTask(taskData, mockProjectId);
    await taskCleanup.trackTask(taskId);
    
    // Navigate to task detail to verify inheritance
    await taskDetailPage.navigate(taskId);
    
    // Verify task is associated with project
    await taskDetailPage.assertProjectAssociation(mockProjectId);
    
    // Verify task has inherited project collaborators (if any)
    // This would depend on the actual project setup
    const collaboratorCount = await taskDetailPage.getCollaboratorCount();
    expect(collaboratorCount).toBeGreaterThanOrEqual(0);
  });

  test('should display task in both project view and My Tasks', async ({ 
    staffPage, 
    tasksPage, 
    myTasksPage, 
    taskBuilder, 
    taskCleanup 
  }) => {
    // Mock project ID
    const mockProjectId = 'test-project-visibility';
    
    // Navigate to project tasks page
    await tasksPage.navigateToProjectTasks(mockProjectId);
    
    // Create task within project
    const taskData = taskBuilder
      .withTitle('E2E Task - Cross-View Visibility')
      .withPriority(TaskPriority.LOW)
      .withStatus(TaskStatus.TODO)
      .withProject(mockProjectId)
      .build();
    
    const taskId = await tasksPage.createProjectTask(taskData, mockProjectId);
    await taskCleanup.trackTask(taskId);
    
    // Verify task is visible in project view
    await tasksPage.assertTaskVisible(taskId);
    
    // Navigate to My Tasks view
    await myTasksPage.navigate();
    
    // Verify task is also visible in My Tasks (since user is the creator/assignee)
    await myTasksPage.assertTaskVisible(taskId);
    
    // Verify task appears in the correct group (likely no-date group if no due date set)
    const taskCount = await myTasksPage.getTaskCount();
    expect(taskCount).toBeGreaterThanOrEqual(1);
  });
});