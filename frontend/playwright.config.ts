import { defineConfig, devices } from '@playwright/test';

/**
 * Read environment variables from file.
 * https://github.com/motdotla/dotenv
 */
import { config } from 'dotenv';
import path from 'path';
import fs from 'fs';

// Debug: Check if .env files exist and load them
// Try actual project path first, then fallback to worktree path
const actualProjectPath = 'C:\\Users\\yeoyu\\OneDrive\\Desktop\\UNI\\class\\Y3S1\\SPM\\Project\\spmOrangle\\frontend';
const worktreePath = __dirname;

// Check both locations
const possibleEnvPaths = [
  path.join(actualProjectPath, '.env'),
  path.resolve(__dirname, '.env'),
];

const possibleEnvLocalPaths = [
  path.join(actualProjectPath, '.env.local'),
  path.resolve(__dirname, '.env.local'),
];

// Find the first .env file that exists
let envPath = possibleEnvPaths.find(p => fs.existsSync(p)) || possibleEnvPaths[0];
let envLocalPath = possibleEnvLocalPaths.find(p => fs.existsSync(p)) || possibleEnvLocalPaths[0];

console.log('\n=== Loading .env files ===');
console.log('Checking actual project path:', actualProjectPath);
console.log('Checking worktree path:', worktreePath);
console.log('Looking for .env at:', envPath);
console.log('  File exists:', fs.existsSync(envPath));
console.log('Looking for .env.local at:', envLocalPath);
console.log('  File exists:', fs.existsSync(envLocalPath));

// Load .env.local if it exists, fallback to .env
const localResult = config({ path: envLocalPath });
const envResult = config({ path: envPath });

console.log('.env.local loaded:', localResult.parsed ? `${Object.keys(localResult.parsed).length} variables` : 'NOT FOUND or EMPTY');
console.log('.env loaded:', envResult.parsed ? `${Object.keys(envResult.parsed).length} variables` : 'NOT FOUND or EMPTY');

if (localResult.parsed) {
  console.log('.env.local variables:', Object.keys(localResult.parsed));
}
if (envResult.parsed) {
  console.log('.env variables:', Object.keys(envResult.parsed));
}

// Show test user vars (without passwords)
console.log('\nTest User Environment Variables:');
console.log('  TEST_HR_EMAIL:', process.env.TEST_HR_EMAIL || 'NOT SET');
console.log('  TEST_MANAGER_EMAIL:', process.env.TEST_MANAGER_EMAIL || 'NOT SET');
console.log('  TEST_STAFF_EMAIL:', process.env.TEST_STAFF_EMAIL || 'NOT SET');
console.log('  PLAYWRIGHT_BASE_URL:', process.env.PLAYWRIGHT_BASE_URL || 'NOT SET');
console.log('  PLAYWRIGHT_API_URL:', process.env.PLAYWRIGHT_API_URL || 'NOT SET');
console.log('================================\n');

/**
 * See https://playwright.dev/docs/test-configuration.
 */
export default defineConfig({
  testDir: './e2e/tests',
  /* Run tests in files in parallel */
  fullyParallel: true,
  /* Fail the build on CI if you accidentally left test.only in the source code. */
  forbidOnly: !!process.env.CI,
  /* Retry on CI only */
  retries: process.env.CI ? 2 : 0,
  /* Opt out of parallel tests on CI. */
  workers: process.env.CI ? 1 : undefined,
  /* Reporter to use. See https://playwright.dev/docs/test-reporters */
  reporter: [
    ['html', { outputFolder: 'e2e-results/html' }],
    ['json', { outputFile: 'e2e-results/results.json' }],
    ['list'],
  ],
  /* Shared settings for all the projects below. See https://playwright.dev/docs/api/class-testoptions. */
  use: {
    /* Base URL to use in actions like `await page.goto('/')`. */
    baseURL: process.env.PLAYWRIGHT_BASE_URL || 'http://localhost:3000',

    /* Collect trace when retrying the failed test. See https://playwright.dev/docs/trace-viewer */
    trace: 'on-first-retry',

    /* Screenshot on failure */
    screenshot: 'only-on-failure',

    /* Video on first retry */
    video: 'retain-on-failure',

    /* Maximum time each action such as `click()` can take */
    actionTimeout: 15000,

    /* Maximum time each navigation action can take
     * Increased from 30s to 60s to handle authentication flows and slow network conditions
     */
    navigationTimeout: 60000,
  },

  /* Configure projects for major browsers */
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },

    // Uncomment these if you want to test on multiple browsers
    // {
    //   name: 'firefox',
    //   use: { ...devices['Desktop Firefox'] },
    // },

    // {
    //   name: 'webkit',
    //   use: { ...devices['Desktop Safari'] },
    // },

    /* Test against mobile viewports. */
    // {
    //   name: 'Mobile Chrome',
    //   use: { ...devices['Pixel 5'] },
    // },
    // {
    //   name: 'Mobile Safari',
    //   use: { ...devices['iPhone 12'] },
    // },
  ],

  /* Run your local dev server before starting the tests */
  webServer: {
    command: 'npm run dev',
    url: 'http://localhost:3000',
    reuseExistingServer: !process.env.CI,
    timeout: 120000,
    stdout: 'ignore',
    stderr: 'pipe',
  },

  /* Global setup and teardown */
  // globalSetup: './e2e/setup/global-setup.ts',
  // globalTeardown: './e2e/setup/global-teardown.ts',

  /* Folder for test artifacts such as screenshots, videos, traces, etc. */
  outputDir: 'e2e-results/test-artifacts',

  /* Timeout for each test */
  timeout: 60000,

  /* Expect timeout */
  expect: {
    timeout: 5000,
  },
});
