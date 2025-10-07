// Jest setup file
import "@testing-library/jest-dom";

// Mock Next.js router
jest.mock("next/navigation", () => ({
  useRouter() {
    return {
      push: jest.fn(),
      replace: jest.fn(),
      refresh: jest.fn(),
      back: jest.fn(),
      forward: jest.fn(),
      prefetch: jest.fn(),
    };
  },
  useSearchParams() {
    return new URLSearchParams();
  },
  usePathname() {
    return "/";
  },
}));

// Mock fetch globally
global.fetch = jest.fn((input, init) => {
  // Simulate a successful JSON response for all requests
  return Promise.resolve(
    new Response(JSON.stringify({}), {
      status: 200,
      headers: { 'Content-Type': 'application/json' },
    })
  );
});

// Mock window.matchMedia
Object.defineProperty(window, "matchMedia", {
  writable: true,
  value: jest.fn().mockImplementation((query) => ({
    matches: false, // Default to light mode in tests
    media: query,
    onchange: null,
    addListener: jest.fn(), // deprecated
    removeListener: jest.fn(), // deprecated
    addEventListener: jest.fn(),
    removeEventListener: jest.fn(),
    dispatchEvent: jest.fn(),
  })),
});

// Mock Next.js font imports
jest.mock("next/font/google", () => ({
  Geist: () => ({
    className: "mocked-geist-font",
    variable: "--font-geist-sans",
  }),
  Geist_Mono: () => ({
    className: "mocked-geist-mono-font",
    variable: "--font-geist-mono",
  }),
}));

// Suppress React act() warnings and expected error logs for async operations in tests
const originalError = console.error;
beforeAll(() => {
  console.error = (...args: any[]) => {
    if (
      typeof args[0] === 'string' && (
        args[0].includes('was not wrapped in act') ||
        args[0].includes('Error loading project members') ||
        args[0].includes('Error creating task') ||
        args[0].includes('Error loading tags') ||
        args[0].includes('API Error') ||
        args[0].includes('Creation failed') ||
        args[0].includes('In HTML, <span> cannot be a child of <select>') ||
        args[0].includes('<select> cannot contain a nested <span>')
      )
    ) {
      return;
    }
    originalError.call(console, ...args);
  };
});

afterAll(() => {
  console.error = originalError;
});

jest.mock('aws-amplify/auth', () => ({
  fetchAuthSession: jest.fn().mockResolvedValue({
    tokens: { accessToken: 'mocked-access-token' }
  }),
  getCurrentUser: jest.fn().mockResolvedValue({
    username: 'testuser',
    userId: 'test-user-id'
  }),
  fetchUserAttributes: jest.fn().mockResolvedValue({
    email: 'test@example.com',
    given_name: 'Test',
    family_name: 'User'
  }),
  signOut: jest.fn().mockResolvedValue(undefined)
}));

jest.mock('@aws-amplify/core', () => ({
  Hub: {
    listen: jest.fn(),
    remove: jest.fn()
  }
}));