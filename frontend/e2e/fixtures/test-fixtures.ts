import { test as base, Page } from '@playwright/test';
import { SigninPage } from './pages/signin-page';
import { TasksPage } from './pages/tasks-page';
import { TaskDetailPage } from './pages/task-detail-page';
import { KanbanBoardPage } from './pages/kanban-board-page';
import { MyTasksPage } from './pages/my-tasks-page';
import { TEST_USERS, TestUser } from '../config/test-users';
import { TaskCleanupService } from './services/task-cleanup-service';
import { TestDataFactory } from './data/task-data-factory';
import { TaskDataBuilder, SubtaskDataBuilder, CommentDataBuilder, ProjectDataBuilder } from './data/task-data-builder';
import { TaskData, SubtaskData, CommentData, ProjectData } from './data/task-data-types';

/**
 * Custom test fixtures
 * Extends Playwright's base test with custom fixtures for authentication and page objects
 */

export type TestFixtures = {
  // Authenticated page contexts for different roles
  managerPage: Page;
  staffPage: Page;
  hrPage: Page;

  // Page objects
  signinPage: SigninPage;
  tasksPage: TasksPage;
  taskDetailPage: TaskDetailPage;
  kanbanBoardPage: KanbanBoardPage;
  myTasksPage: MyTasksPage;

  // Task management fixtures
  taskCleanup: TaskCleanupService;
  testTask: TaskData;
  testSubtask: SubtaskData;
  testComment: CommentData;
  testProject: ProjectData;
  
  // Data builders
  taskBuilder: TaskDataBuilder;
  subtaskBuilder: SubtaskDataBuilder;
  commentBuilder: CommentDataBuilder;
  projectBuilder: ProjectDataBuilder;
  
  // Data factory
  taskDataFactory: TestDataFactory;
};

/**
 * Helper function to authenticate a user and return the page
 */
async function authenticateUser(page: Page, user: TestUser): Promise<Page> {
  const signinPage = new SigninPage(page);
  await signinPage.completeSignInWithUser(user);
  return page;
}

/**
 * Extended test with custom fixtures
 */
export const test = base.extend<TestFixtures>({
  /**
   * Manager authenticated page
   * Use this fixture when you need a manager user session
   *
   * Example:
   * test('manager can generate reports', async ({ managerPage }) => {
   *   await managerPage.goto('/reports');
   *   // test code
   * });
   */
  managerPage: async ({ browser }, use) => {
    const context = await browser.newContext();
    const page = await context.newPage();
    await authenticateUser(page, TEST_USERS.manager);
    
    // Best practice: Wait for page to be ready after authentication
    await page.waitForLoadState('load');
    
    await use(page);
    await context.close();
  },

  /**
   * Staff authenticated page
   * Use this fixture when you need a staff user session
   *
   * Example:
   * test('staff cannot access reports', async ({ staffPage }) => {
   *   await staffPage.goto('/reports');
   *   // test code
   * });
   */
  staffPage: async ({ browser }, use) => {
    const context = await browser.newContext();
    const page = await context.newPage();
    await authenticateUser(page, TEST_USERS.staff);
    
    // Best practice: Wait for page to be ready after authentication
    await page.waitForLoadState('load');
    
    await use(page);
    await context.close();
  },

  /**
   * HR authenticated page
   * Use this fixture when you need an HR user session
   *
   * Example:
   * test('hr can generate reports for any project', async ({ hrPage }) => {
   *   await hrPage.goto('/reports');
   *   // test code
   * });
   */
  hrPage: async ({ browser }, use) => {
    const context = await browser.newContext();
    const page = await context.newPage();
    await authenticateUser(page, TEST_USERS.hr);
    
    // Best practice: Wait for page to be ready after authentication
    // This prevents race conditions where tests start before the page is fully loaded
    await page.waitForLoadState('load');
    
    await use(page);
    await context.close();
  },

  /**
   * Signin page object
   * Use this for tests that need to interact with the signin page
   *
   * Example:
   * test('can sign in', async ({ page, signinPage }) => {
   *   await signinPage.navigate();
   *   await signinPage.signIn('user@test.com', 'password');
   * });
   */
  signinPage: async ({ page }, use) => {
    const signinPage = new SigninPage(page);
    await use(signinPage);
  },

  /**
   * Task cleanup service
   * Automatically cleans up test data after each test
   *
   * Example:
   * test('creates task', async ({ staffPage, taskCleanup }) => {
   *   const taskId = await createTask();
   *   await taskCleanup.trackTask(taskId);
   *   // Cleanup happens automatically after test
   * });
   */
  taskCleanup: async ({ page }, use) => {
    const cleanup = new TaskCleanupService(page);
    await use(cleanup);
    // Cleanup after test completes
    await cleanup.cleanupAll();
  },

  /**
   * Pre-built test task data
   * Use this for tests that need a standard task
   *
   * Example:
   * test('displays task', async ({ testTask }) => {
   *   expect(testTask.title).toBe('E2E Test Task');
   * });
   */
  testTask: async ({}, use) => {
    const task = TestDataFactory.createStandaloneTask();
    await use(task);
  },

  /**
   * Pre-built test subtask data
   * Use this for tests that need a standard subtask
   */
  testSubtask: async ({}, use) => {
    const subtask = TestDataFactory.createSubtask('test-parent-id');
    await use(subtask);
  },

  /**
   * Pre-built test comment data
   * Use this for tests that need a standard comment
   */
  testComment: async ({}, use) => {
    const comment = TestDataFactory.createComment('test-task-id');
    await use(comment);
  },

  /**
   * Pre-built test project data
   * Use this for tests that need a standard project
   */
  testProject: async ({}, use) => {
    const project = TestDataFactory.createTestProject();
    await use(project);
  },

  /**
   * Task data builder
   * Use this for tests that need custom task data
   *
   * Example:
   * test('creates high priority task', async ({ taskBuilder }) => {
   *   const task = taskBuilder
   *     .withTitle('Urgent Task')
   *     .withPriority(TaskPriority.HIGH)
   *     .build();
   * });
   */
  taskBuilder: async ({}, use) => {
    await use(new TaskDataBuilder());
  },

  /**
   * Subtask data builder
   * Use this for tests that need custom subtask data
   */
  subtaskBuilder: async ({}, use) => {
    await use(new SubtaskDataBuilder());
  },

  /**
   * Comment data builder
   * Use this for tests that need custom comment data
   */
  commentBuilder: async ({}, use) => {
    await use(new CommentDataBuilder());
  },

  /**
   * Project data builder
   * Use this for tests that need custom project data
   */
  projectBuilder: async ({}, use) => {
    await use(new ProjectDataBuilder());
  },

  /**
   * Tasks page object
   * Use this for tests that interact with the main tasks page
   *
   * Example:
   * test('creates task', async ({ staffPage, tasksPage }) => {
   *   await tasksPage.navigate();
   *   await tasksPage.createStandaloneTask(taskData);
   * });
   */
  tasksPage: async ({ page }, use) => {
    await use(new TasksPage(page));
  },

  /**
   * Task detail page object
   * Use this for tests that interact with individual task pages
   */
  taskDetailPage: async ({ page }, use) => {
    await use(new TaskDetailPage(page));
  },

  /**
   * Kanban board page object
   * Use this for tests that interact with the Kanban board
   */
  kanbanBoardPage: async ({ page }, use) => {
    await use(new KanbanBoardPage(page));
  },

  /**
   * My Tasks page object
   * Use this for tests that interact with the My Tasks view
   */
  myTasksPage: async ({ page }, use) => {
    await use(new MyTasksPage(page));
  },

  /**
   * Task data factory
   * Use this for creating test data via factory methods
   *
   * Example:
   * test('creates task', async ({ taskDataFactory }) => {
   *   const taskData = taskDataFactory.createStandaloneTask();
   *   const taskId = await taskDataFactory.createTaskViaAPI(taskData);
   * });
   */
  taskDataFactory: async ({}, use) => {
    await use(TestDataFactory);
  },
});

/**
 * Export expect from Playwright
 */
export { expect } from '@playwright/test';
