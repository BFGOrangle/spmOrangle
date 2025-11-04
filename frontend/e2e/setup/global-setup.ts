import { chromium, FullConfig } from '@playwright/test';
import { getConfig } from '../config/environments';

/**
 * Global setup
 * Runs once before all tests
 * Use this for:
 * - Setting up test database
 * - Creating test users
 * - Starting services
 * - Warming up servers
 */
async function globalSetup(config: FullConfig) {
  console.log('🚀 Starting global setup...');

  const environment = getConfig();
  console.log(`📍 Environment: ${environment.baseURL}`);

  // Example: Check if the application is running
  const browser = await chromium.launch();
  const page = await browser.newPage();

  try {
    console.log('🔍 Checking if application is accessible...');
    await page.goto(environment.baseURL, { timeout: 30000 });
    console.log('✅ Application is accessible');
  } catch (error) {
    console.error('❌ Application is not accessible. Make sure it is running.');
    console.error(error);
    throw error;
  } finally {
    await browser.close();
  }

  // Example: Setup test data via API
  // await setupTestData();

  // Example: Create test users if they don't exist
  // await ensureTestUsersExist();

  console.log('✅ Global setup complete');
}

/**
 * Setup test data (example)
 */
// async function setupTestData() {
//   console.log('📝 Setting up test data...');
//   // Make API calls to seed database
//   // const response = await fetch(`${getConfig().apiURL}/api/test/seed`, {
//   //   method: 'POST',
//   // });
//   // console.log('✅ Test data ready');
// }

/**
 * Ensure test users exist (example)
 */
// async function ensureTestUsersExist() {
//   console.log('👥 Checking test users...');
//   // Check if test users exist, create if not
//   // const response = await fetch(`${getConfig().apiURL}/api/test/users`, {
//   //   method: 'POST',
//   //   body: JSON.stringify({ users: TEST_USERS }),
//   // });
//   // console.log('✅ Test users ready');
// }

export default globalSetup;
