# Playwright E2E Testing Guide
## Complete Setup & Extension Guide for Developers

**Last Updated:** November 2, 2025  
**Playwright Version:** Latest  
**Framework:** Next.js + TypeScript

---

## 📚 Table of Contents

1. [Overview](#overview)
2. [Quick Start](#quick-start)
3. [Project Structure](#project-structure)
4. [Authentication Setup](#authentication-setup)
5. [Writing Tests](#writing-tests)
6. [Page Object Model](#page-object-model)
7. [Test Fixtures](#test-fixtures)
8. [Running Tests](#running-tests)
9. [Debugging](#debugging)
10. [Best Practices](#best-practices)
11. [Extending the Framework](#extending-the-framework)
12. [Troubleshooting](#troubleshooting)

---

## 🎯 Overview

### What is Playwright?

Playwright is a modern end-to-end testing framework that allows you to:
- Test across multiple browsers (Chromium, Firefox, WebKit)
- Run tests in parallel for speed
- Capture screenshots and videos on failures
- Debug with browser DevTools
- Test with real user interactions

### Why E2E Testing?

E2E tests verify that entire user workflows function correctly from start to finish, catching issues that unit tests might miss, such as:
- Authentication flows
- API integrations
- Cross-page navigation
- User permissions and access control
- File downloads and uploads

---

## 🚀 Quick Start

### Prerequisites

```bash
# Node.js 18+ required
node --version

# Install dependencies
cd frontend
npm install
```

### First Test Run

```bash
# Run all E2E tests
npm run test:e2e

# Run specific test file
npx playwright test e2e/tests/reports/report-generation.spec.ts

# Run in headed mode (see browser)
npx playwright test --headed

# Run with UI (interactive mode)
npx playwright test --ui
```

### Environment Setup

Create `.env` file in `frontend/` directory:

```env
# Test User Credentials
TEST_HR_EMAIL=your-hr-user@domain.com
TEST_HR_PASSWORD=your-hr-password

TEST_MANAGER_EMAIL=your-manager-user@domain.com
TEST_MANAGER_PASSWORD=your-manager-password

TEST_STAFF_EMAIL=your-staff-user@domain.com
TEST_STAFF_PASSWORD=your-staff-password

# Application URLs
PLAYWRIGHT_BASE_URL=http://localhost:3000
PLAYWRIGHT_API_URL=http://localhost:8080

# Cognito Configuration (if using AWS Cognito)
NEXT_PUBLIC_AWS_COGNITO_PUBLIC_USER_POOL_ID=your-pool-id
NEXT_PUBLIC_AWS_COGNITO_APP_CLIENT_ID=your-client-id
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
```

---

## 📁 Project Structure

```
frontend/e2e/
├── config/
│   ├── environments.ts          # Environment configuration
│   └── test-users.ts            # Test user credentials
├── fixtures/
│   ├── index.ts                 # Main fixture exports
│   ├── test-fixtures.ts         # Custom fixtures (auth, page objects)
│   └── pages/
│       ├── base-page.ts         # Base page object class
│       ├── signin-page.ts       # Sign-in page object
│       └── reports-page.ts      # Reports page object
├── tests/
│   ├── example.spec.ts          # Example test patterns
│   └── reports/
│       ├── report-generation.spec.ts
│       ├── report-filters.spec.ts
│       ├── report-permissions.spec.ts
│       └── report-data-accuracy.spec.ts
├── utils/
│   ├── api-helpers.ts           # API interaction helpers
│   ├── assertions.ts            # Custom assertion functions
│   ├── download-helpers.ts      # File download utilities
│   ├── report-assertions.ts     # Report-specific assertions
│   └── test-data.ts             # Test data builders
├── setup/
│   ├── global-setup.ts          # Runs once before all tests
│   └── global-teardown.ts       # Runs once after all tests
└── README.md                    # Basic E2E documentation
```

---

## 🔐 Authentication Setup

### How Authentication Works

Our E2E framework uses **test fixtures** to provide pre-authenticated browser contexts for different user roles. This means you don't need to manually sign in for every test.

### Test Fixtures Overview

```typescript
// frontend/e2e/fixtures/test-fixtures.ts

export type TestFixtures = {
  // Pre-authenticated pages for different roles
  hrPage: Page;          // HR user session
  managerPage: Page;     // Manager user session
  staffPage: Page;       // Staff user session
  
  // Page objects
  signinPage: SigninPage;
};
```

### How to Use Pre-Authenticated Pages

```typescript
import { test, expect } from '../../fixtures';

test('HR user can access reports', async ({ hrPage }) => {
  // hrPage is already authenticated as HR user!
  await hrPage.goto('/reports');
  
  // Your test code here
  await expect(hrPage.getByText('Report Filters')).toBeVisible();
});

test('Manager can view dashboard', async ({ managerPage }) => {
  // managerPage is already authenticated as Manager!
  await managerPage.goto('/dashboard');
  
  await expect(managerPage.getByText('Welcome')).toBeVisible();
});
```

### Under the Hood: Authentication Implementation

```typescript
// How fixtures authenticate users automatically

async function authenticateUser(page: Page, user: TestUser): Promise<Page> {
  const signinPage = new SigninPage(page);
  await signinPage.completeSignInWithUser(user);
  return page;
}

// Fixture definition
hrPage: async ({ browser }, use) => {
  const context = await browser.newContext();
  const page = await context.newPage();
  
  // Authenticate user
  await authenticateUser(page, TEST_USERS.hr);
  
  // Wait for page to be ready
  await page.waitForLoadState('load');
  
  // Provide authenticated page to test
  await use(page);
  
  // Cleanup
  await context.close();
}
```

### Test User Configuration

```typescript
// frontend/e2e/config/test-users.ts

export interface TestUser {
  email: string;
  password: string;
  role: 'MANAGER' | 'STAFF' | 'HR';
  name: string;
}

export const TEST_USERS: Record<string, TestUser> = {
  hr: {
    email: process.env.TEST_HR_EMAIL || 'hr@test.com',
    password: process.env.TEST_HR_PASSWORD || 'password',
    role: 'HR',
    name: 'Test HR',
  },
  manager: {
    email: process.env.TEST_MANAGER_EMAIL || 'manager@test.com',
    password: process.env.TEST_MANAGER_PASSWORD || 'password',
    role: 'MANAGER',
    name: 'Test Manager',
  },
  staff: {
    email: process.env.TEST_STAFF_EMAIL || 'staff@test.com',
    password: process.env.TEST_STAFF_PASSWORD || 'password',
    role: 'STAFF',
    name: 'Test Staff',
  },
};
```

### When to Test Authentication Manually

Only test the actual sign-in flow when you're specifically testing authentication features:

```typescript
test('should display error for invalid credentials', async ({ page, signinPage }) => {
  await signinPage.navigate();
  await signinPage.signIn('invalid@test.com', 'wrongpassword');
  
  await signinPage.assertErrorDisplayed();
});
```

---

## ✍️ Writing Tests

### Basic Test Structure

```typescript
import { test, expect } from '../../fixtures';
import { ReportsPage } from '../../fixtures/pages/reports-page';

test.describe('Feature Name', () => {
  let reportsPage: ReportsPage;

  // Runs before each test in this describe block
  test.beforeEach(async ({ hrPage }) => {
    reportsPage = new ReportsPage(hrPage);
    await reportsPage.navigate();
  });

  // Runs after each test (cleanup)
  test.afterEach(async () => {
    // Cleanup code (e.g., delete created data)
  });

  test('should do something', async ({ hrPage }) => {
    // Arrange - set up test data
    const testData = { name: 'Test' };
    
    // Act - perform the action
    await reportsPage.clickGenerate();
    
    // Assert - verify the result
    await reportsPage.assertTaskSummaryVisible();
  });
});
```

### GWT Pattern (Given-When-Then)

```typescript
test('should generate report for current month', async ({ hrPage }) => {
  // Given: I am on the reports page with current month selected
  const dateRange = getCurrentMonthRange();
  await reportsPage.setDateRange(dateRange.startDate, dateRange.endDate);
  
  // When: I generate the report
  await reportsPage.clickGenerate();
  await reportsPage.waitForReportGenerated();
  
  // Then: The report should display task summary
  await reportsPage.assertTaskSummaryVisible();
  const taskSummary = await reportsPage.getTaskSummaryData();
  expect(taskSummary.totalTasks).toBeGreaterThanOrEqual(0);
});
```

### Selecting Elements

```typescript
// ✅ Best: Use test IDs
await page.getByTestId('submit-button').click();

// ✅ Good: Use role and accessible name
await page.getByRole('button', { name: 'Submit' }).click();

// ✅ Good: Use text content
await page.getByText('Welcome').waitFor();

// ⚠️ Avoid: CSS selectors (brittle)
await page.locator('.btn-submit').click();
```

### Waiting Strategies

```typescript
// ✅ Best: Wait for specific element
await page.getByText('Success').waitFor({ state: 'visible' });

// ✅ Good: Wait for load state
await page.waitForLoadState('load');

// ✅ Good: Wait for navigation
await Promise.all([
  page.waitForURL('/dashboard'),
  page.click('a[href="/dashboard"]')
]);

// ❌ Avoid: Fixed timeouts (flaky)
await page.waitForTimeout(5000);

// ❌ Avoid: networkidle (unreliable in SPAs)
await page.waitForLoadState('networkidle');
```

---

## 📄 Page Object Model

### What is Page Object Model?

POM is a design pattern that:
- Encapsulates page interactions in reusable classes
- Reduces code duplication
- Makes tests easier to maintain
- Improves test readability

### Base Page Class

```typescript
// frontend/e2e/fixtures/pages/base-page.ts

export abstract class BasePage {
  protected readonly page: Page;
  protected abstract readonly route: string;

  constructor(page: Page) {
    this.page = page;
  }

  // Navigation
  async goto(path: string): Promise<void> {
    await this.page.goto(path);
  }

  async navigate(): Promise<void> {
    await this.goto(this.route);
  }

  // Wait helpers
  async waitForPageLoad(): Promise<void> {
    await this.page.waitForLoadState('load');
  }

  // Interaction helpers
  async click(locator: Locator): Promise<void> {
    await locator.click();
  }

  async fill(locator: Locator, value: string): Promise<void> {
    await locator.fill(value);
  }

  // Assertion helpers
  async assertVisible(locator: Locator): Promise<void> {
    await expect(locator).toBeVisible();
  }
}
```

### Creating a New Page Object

```typescript
// frontend/e2e/fixtures/pages/dashboard-page.ts

import { Page, Locator } from '@playwright/test';
import { BasePage } from './base-page';

export class DashboardPage extends BasePage {
  protected readonly route = '/dashboard';

  // Selectors (private properties)
  private get welcomeMessage(): Locator {
    return this.page.getByText(/welcome/i);
  }

  private get projectsList(): Locator {
    return this.page.getByRole('list', { name: 'Projects' });
  }

  private get createProjectButton(): Locator {
    return this.page.getByRole('button', { name: 'Create Project' });
  }

  // Actions (public methods)
  async clickCreateProject(): Promise<void> {
    await this.click(this.createProjectButton);
  }

  async selectProject(projectName: string): Promise<void> {
    await this.page.getByRole('listitem', { name: projectName }).click();
  }

  // Assertions (public methods)
  async assertOnDashboard(): Promise<void> {
    await this.assertVisible(this.welcomeMessage);
  }

  async assertProjectVisible(projectName: string): Promise<void> {
    const project = this.page.getByText(projectName);
    await this.assertVisible(project);
  }

  // Data extraction (public methods)
  async getProjectCount(): Promise<number> {
    const items = await this.projectsList.locator('li').count();
    return items;
  }
}
```

### Using Page Objects in Tests

```typescript
import { test, expect } from '../../fixtures';
import { DashboardPage } from '../../fixtures/pages/dashboard-page';

test.describe('Dashboard', () => {
  let dashboardPage: DashboardPage;

  test.beforeEach(async ({ hrPage }) => {
    dashboardPage = new DashboardPage(hrPage);
    await dashboardPage.navigate();
  });

  test('should display projects list', async () => {
    await dashboardPage.assertOnDashboard();
    const projectCount = await dashboardPage.getProjectCount();
    expect(projectCount).toBeGreaterThan(0);
  });

  test('should create new project', async () => {
    await dashboardPage.clickCreateProject();
    // ... rest of test
  });
});
```

---

## 🔧 Test Fixtures

### What are Fixtures?

Fixtures are a way to set up the testing environment before tests run. They provide:
- Pre-authenticated user sessions
- Reusable page objects
- Test data
- Configuration

### Creating Custom Fixtures

```typescript
// frontend/e2e/fixtures/test-fixtures.ts

import { test as base, Page } from '@playwright/test';
import { DashboardPage } from './pages/dashboard-page';

type CustomFixtures = {
  hrPage: Page;
  dashboardPage: DashboardPage;
  testProject: ProjectData;
};

export const test = base.extend<CustomFixtures>({
  // Existing fixtures...

  dashboardPage: async ({ hrPage }, use) => {
    const dashboardPage = new DashboardPage(hrPage);
    await dashboardPage.navigate();
    await use(dashboardPage);
  },

  testProject: async ({ hrPage }, use) => {
    // Create test project
    const projectData = await createTestProject();
    
    await use(projectData);
    
    // Cleanup: delete test project
    await deleteTestProject(projectData.id);
  },
});
```

### Using Custom Fixtures

```typescript
test('should display test project', async ({ dashboardPage, testProject }) => {
  // dashboardPage is already initialized and navigated
  // testProject is created before test and deleted after
  
  await dashboardPage.assertProjectVisible(testProject.name);
});
```

---

## 🏃 Running Tests

### NPM Scripts

```bash
# Run all E2E tests
npm run test:e2e

# Run with UI mode (interactive)
npm run test:e2e:ui

# Run specific test file
npm run test:e2e -- report-generation.spec.ts

# Run tests matching pattern
npm run test:e2e -- -g "should generate report"
```

### Command Line Options

```bash
# Run in headed mode (see browser)
npx playwright test --headed

# Run specific browser
npx playwright test --project=chromium
npx playwright test --project=firefox
npx playwright test --project=webkit

# Run with debug mode
npx playwright test --debug

# Run with trace
npx playwright test --trace on

# Run specific test file
npx playwright test e2e/tests/reports/report-generation.spec.ts

# Run tests matching a grep pattern
npx playwright test -g "should export"

# Run in parallel (default)
npx playwright test --workers=4

# Run sequentially
npx playwright test --workers=1

# Update snapshots
npx playwright test --update-snapshots
```

### Test Reports

```bash
# Generate HTML report
npx playwright show-report

# Generate JSON report
npx playwright test --reporter=json

# Generate list report (terminal output)
npx playwright test --reporter=list

# Multiple reporters
npx playwright test --reporter=html,json,list
```

---

## 🐛 Debugging

### Debug Mode

```bash
# Open test in debug mode
npx playwright test --debug

# Debug specific test
npx playwright test report-generation.spec.ts:27 --debug
```

### VS Code Debugging

Add to `.vscode/launch.json`:

```json
{
  "version": "0.2.0",
  "configurations": [
    {
      "type": "node",
      "request": "launch",
      "name": "Playwright Debug",
      "program": "${workspaceFolder}/frontend/node_modules/@playwright/test/cli.js",
      "args": ["test", "${file}"],
      "cwd": "${workspaceFolder}/frontend",
      "console": "integratedTerminal"
    }
  ]
}
```

### Playwright Inspector

```typescript
// Add to test
await page.pause(); // Opens Playwright Inspector

// Step through test
test('debug test', async ({ page }) => {
  await page.goto('/reports');
  await page.pause(); // Execution stops here
  await page.click('button');
});
```

### Screenshots & Videos

```typescript
// Take screenshot manually
await page.screenshot({ path: 'screenshot.png' });

// Screenshot on failure (automatic in playwright.config.ts)
screenshot: 'only-on-failure'

// Record video (automatic in playwright.config.ts)
video: 'retain-on-failure'
```

### Trace Viewer

```bash
# Run with trace
npx playwright test --trace on

# View trace
npx playwright show-trace trace.zip
```

---

## 💎 Best Practices

### 1. Use Element-Based Waits (Not Fixed Timeouts)

```typescript
// ✅ Good
await page.getByText('Success').waitFor({ state: 'visible' });

// ❌ Bad
await page.waitForTimeout(5000);
```

### 2. Avoid `networkidle` in SPAs

```typescript
// ✅ Good: Wait for specific element
await page.waitForLoadState('load');
await page.getByText('Content').waitFor();

// ❌ Bad: networkidle is unreliable in SPAs
await page.waitForLoadState('networkidle');
```

### 3. Use Page Object Model

```typescript
// ✅ Good: Encapsulated in page object
await reportsPage.generateReport({ format: 'PDF' });

// ❌ Bad: Direct page manipulation
await page.click('[data-testid="format-select"]');
await page.selectOption('select', 'PDF');
await page.click('[data-testid="generate-btn"]');
```

### 4. Use Test Data Builders

```typescript
// ✅ Good: Reusable test data
const dateRange = getCurrentMonthRange();

// ❌ Bad: Hardcoded dates
const startDate = '2025-01-01';
const endDate = '2025-01-31';
```

### 5. Clean Up Test Data

```typescript
test.afterEach(async () => {
  // Clean up created projects, files, etc.
  await deleteTestProject(projectId);
});
```

### 6. Use Meaningful Test Names

```typescript
// ✅ Good
test('should display error when email is invalid')

// ❌ Bad
test('test 1')
```

### 7. Keep Tests Independent

```typescript
// ✅ Good: Each test is self-contained
test('test A', async ({ hrPage }) => {
  await createTestData();
  // test logic
  await cleanupTestData();
});

test('test B', async ({ hrPage }) => {
  await createTestData();
  // test logic
  await cleanupTestData();
});

// ❌ Bad: test B depends on test A
test('test A', async () => {
  await createTestData(); // test B relies on this
});

test('test B', async () => {
  // assumes test A ran first
});
```

### 8. Use Custom Assertions

```typescript
// ✅ Good: Reusable custom assertion
assertTaskCountsNonNegative(taskSummary);

// ❌ Bad: Repeated assertion logic
expect(taskSummary.total).toBeGreaterThanOrEqual(0);
expect(taskSummary.completed).toBeGreaterThanOrEqual(0);
expect(taskSummary.inProgress).toBeGreaterThanOrEqual(0);
```

---

## 🔨 Extending the Framework

### Adding a New Page Object

1. Create new file in `frontend/e2e/fixtures/pages/`:

```typescript
// frontend/e2e/fixtures/pages/new-feature-page.ts

import { Page, Locator } from '@playwright/test';
import { BasePage } from './base-page';

export class NewFeaturePage extends BasePage {
  protected readonly route = '/new-feature';

  // Selectors
  private get mainHeading(): Locator {
    return this.page.getByRole('heading', { name: 'New Feature' });
  }

  // Actions
  async performAction(): Promise<void> {
    // Implementation
  }

  // Assertions
  async assertOnNewFeaturePage(): Promise<void> {
    await this.assertVisible(this.mainHeading);
  }

  // Data extraction
  async getData(): Promise<any> {
    // Implementation
  }
}
```

2. Export from `frontend/e2e/fixtures/index.ts`:

```typescript
export { NewFeaturePage } from './pages/new-feature-page';
```

3. Use in tests:

```typescript
import { test } from '../../fixtures';
import { NewFeaturePage } from '../../fixtures/pages/new-feature-page';

test('should test new feature', async ({ hrPage }) => {
  const newFeaturePage = new NewFeaturePage(hrPage);
  await newFeaturePage.navigate();
  await newFeaturePage.assertOnNewFeaturePage();
});
```

### Adding a New Test Suite

1. Create test file in `frontend/e2e/tests/`:

```typescript
// frontend/e2e/tests/new-feature.spec.ts

import { test, expect } from '../fixtures';
import { NewFeaturePage } from '../fixtures/pages/new-feature-page';

test.describe('New Feature', () => {
  let newFeaturePage: NewFeaturePage;

  test.beforeEach(async ({ hrPage }) => {
    newFeaturePage = new NewFeaturePage(hrPage);
    await newFeaturePage.navigate();
  });

  test('should do something', async () => {
    // Test implementation
  });
});
```

### Adding a New Fixture

```typescript
// frontend/e2e/fixtures/test-fixtures.ts

export type TestFixtures = {
  // Existing fixtures
  hrPage: Page;
  managerPage: Page;
  
  // New fixture
  newFixture: SomeType;
};

export const test = base.extend<TestFixtures>({
  // Existing fixtures...
  
  newFixture: async ({ hrPage }, use) => {
    // Setup
    const data = await setupNewFixture();
    
    // Provide to test
    await use(data);
    
    // Cleanup
    await cleanupNewFixture(data);
  },
});
```

### Adding Test Utilities

```typescript
// frontend/e2e/utils/new-helpers.ts

export async function helperFunction(page: Page) {
  // Implementation
}

export function createTestData() {
  // Implementation
}
```

### Adding Custom Assertions

```typescript
// frontend/e2e/utils/assertions.ts

export function assertCustomCondition(data: any) {
  expect(data.property).toBe(expectedValue);
  expect(data.count).toBeGreaterThan(0);
}
```

---

## 🔍 Troubleshooting

### Common Issues

#### 1. Tests Fail with "Cannot find test user credentials"

**Problem:** Environment variables not loaded.

**Solution:**
```bash
# Ensure .env file exists in frontend/
ls frontend/.env

# Check if variables are set
echo $TEST_HR_EMAIL

# Restart tests
npm run test:e2e
```

#### 2. Tests Timeout During Authentication

**Problem:** Authentication taking too long or failing silently.

**Solution:**
- Check if backend is running (`http://localhost:8080`)
- Verify test user credentials are correct
- Check Cognito configuration
- Increase timeout in `playwright.config.ts`:

```typescript
use: {
  navigationTimeout: 60000,
  actionTimeout: 15000,
}
```

#### 3. "Target page, context or browser has been closed"

**Problem:** Page closed before test finished.

**Solution:**
- Check for early `context.close()` calls
- Ensure fixtures are properly managed
- Add waits after navigation:

```typescript
await page.goto('/reports');
await page.waitForLoadState('load');
```

#### 4. File Download Tests Fail

**Problem:** Downloads not detected.

**Solution:**
```typescript
// Wait for download
const downloadPromise = page.waitForEvent('download');
await page.click('button[data-download]');
const download = await downloadPromise;

// Save file
const path = await download.path();
```

#### 5. Toast Notification Not Detected

**Problem:** Toast appears/disappears too quickly.

**Solution:**
```typescript
// Use optional toast detection
const toastAppeared = await reportsPage.waitForToastOptional(/success/i, 15000);
if (!toastAppeared) {
  console.warn('Toast not detected, but operation succeeded');
}
```

### Debug Checklist

- [ ] Backend server running (`http://localhost:8080`)
- [ ] Frontend server running (`http://localhost:3000`)
- [ ] `.env` file configured with test credentials
- [ ] Test user accounts exist in system
- [ ] Database has test data
- [ ] No conflicting processes on ports 3000/8080

### Getting Help

1. **Check Documentation:**
   - [Playwright Official Docs](https://playwright.dev)
   - This guide
   - `frontend/e2e/tests/reports/README.md`

2. **Run with Debug:**
   ```bash
   npx playwright test --debug
   ```

3. **Check Test Reports:**
   ```bash
   npx playwright show-report
   ```

4. **View Trace:**
   ```bash
   npx playwright test --trace on
   npx playwright show-trace trace.zip
   ```

---

## 📚 Additional Resources

### Official Documentation
- [Playwright Docs](https://playwright.dev)
- [Playwright Best Practices](https://playwright.dev/docs/best-practices)
- [Playwright API Reference](https://playwright.dev/docs/api/class-playwright)

### Project Documentation
- [E2E Test Tickets](./tests/reports/E2E_TEST_TICKETS.md)
- [Test Coverage](./tests/reports/TEST_COVERAGE.md)
- [Timeout Fixes](./tests/reports/TIMEOUT_FIXES.md)
- [Toast Detection Fix](./tests/reports/TOAST_DETECTION_FIX.md)

### Videos & Tutorials
- [Playwright Official YouTube Channel](https://www.youtube.com/@Playwrightdev)
- [Playwright Tutorial Series](https://playwright.dev/docs/intro)

---

## 🎓 Quick Reference

### Common Commands

```bash
# Run tests
npm run test:e2e
npx playwright test
npx playwright test --headed
npx playwright test --debug

# View reports
npx playwright show-report

# Generate code
npx playwright codegen http://localhost:3000

# Install browsers
npx playwright install

# Check Playwright version
npx playwright --version
```

### Test Structure Template

```typescript
import { test, expect } from '../../fixtures';
import { PageObject } from '../../fixtures/pages/page-object';

test.describe('Feature Name', () => {
  let pageObject: PageObject;

  test.beforeEach(async ({ hrPage }) => {
    pageObject = new PageObject(hrPage);
    await pageObject.navigate();
  });

  test('should test something', async ({ hrPage }) => {
    // Arrange
    const testData = setupTestData();
    
    // Act
    await pageObject.performAction(testData);
    
    // Assert
    await pageObject.assertExpectedState();
    const data = await pageObject.getData();
    expect(data).toMatchExpectedValue();
  });
});
```

---

**Happy Testing! 🎭**

For questions or issues, please contact the QA team or refer to the project documentation.

