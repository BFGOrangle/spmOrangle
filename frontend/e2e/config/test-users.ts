/**
 * Test user credentials for different roles
 * IMPORTANT: These should be environment-specific test accounts
 * DO NOT use real user credentials
 */

export interface TestUser {
  email: string;
  password: string;
  role: 'MANAGER' | 'STAFF' | 'HR';
  name: string;
}

/**
 * Test users for different roles
 * TEMPORARY: Hardcoded for testing. Update with actual credentials.
 * TODO: Once working, switch back to environment variables
 */
export const TEST_USERS: Record<string, TestUser> = {
  manager: {
    // HARDCODED: Replace with actual manager credentials
    email: process.env.TEST_MANAGER_EMAIL || 'YOUR_MANAGER_EMAIL@domain.com',
    password: process.env.TEST_MANAGER_PASSWORD || 'YOUR_MANAGER_PASSWORD',
    role: 'MANAGER',
    name: 'Test Manager',
  },
  staff: {
    // HARDCODED: Replace with actual staff credentials
    email: process.env.TEST_STAFF_EMAIL || 'YOUR_STAFF_EMAIL@domain.com',
    password: process.env.TEST_STAFF_PASSWORD || 'YOUR_STAFF_PASSWORD',
    role: 'STAFF',
    name: 'Test Staff',
  },
  hr: {
    // HARDCODED: Testing with actual HR credentials
    email: process.env.TEST_HR_EMAIL || 'qyprojects@gmail.com',
    password: process.env.TEST_HR_PASSWORD || 'Orangle255!',
    role: 'HR',
    name: 'Test HR',
  },
};

// Debug: Log what credentials are being used (remove after testing)
if (typeof process !== 'undefined') {
  console.log('[TEST USERS] HR Email:', TEST_USERS.hr.email);
  console.log('[TEST USERS] Manager Email:', TEST_USERS.manager.email);
  console.log('[TEST USERS] Staff Email:', TEST_USERS.staff.email);
  console.log('[TEST USERS] Using env vars:', {
    TEST_HR_EMAIL: process.env.TEST_HR_EMAIL ? 'SET' : 'NOT SET',
    TEST_MANAGER_EMAIL: process.env.TEST_MANAGER_EMAIL ? 'SET' : 'NOT SET',
    TEST_STAFF_EMAIL: process.env.TEST_STAFF_EMAIL ? 'SET' : 'NOT SET',
  });
}

/**
 * Get test user by role
 */
export function getTestUser(role: 'MANAGER' | 'STAFF' | 'HR'): TestUser {
  const user = Object.values(TEST_USERS).find((u) => u.role === role);
  if (!user) {
    throw new Error(`No test user found for role: ${role}`);
  }
  return user;
}
