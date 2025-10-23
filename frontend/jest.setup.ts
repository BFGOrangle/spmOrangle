// Jest setup file
import "@testing-library/jest-dom";

// Polyfill for JSDOM to support Radix UI Select
if (!HTMLElement.prototype.hasPointerCapture) {
  HTMLElement.prototype.hasPointerCapture = jest.fn(() => false);
}
if (!HTMLElement.prototype.setPointerCapture) {
  HTMLElement.prototype.setPointerCapture = jest.fn();
}
if (!HTMLElement.prototype.releasePointerCapture) {
  HTMLElement.prototype.releasePointerCapture = jest.fn();
}

// Mock ResizeObserver
global.ResizeObserver = jest.fn().mockImplementation(() => ({
  observe: jest.fn(),
  unobserve: jest.fn(),
  disconnect: jest.fn(),
}));

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

// Suppress React act() warnings for async operations in tests
// This is common in testing environments where we can't control all async operations
const originalError = console.error;
beforeAll(() => {
  console.error = (...args: any[]) => {
    if (
      typeof args[0] === 'string' &&
      args[0].includes('Warning: An update to') &&
      args[0].includes('was not wrapped in act') &&
      args[0].includes('when testing, code that causes React state updates should be wrapped into act(...)') &&
      args[0].includes('API Error 0: Network Error')
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
    userId: 'mock-user-id',
    username: 'mock-username'
  }),
  fetchUserAttributes: jest.fn().mockResolvedValue({
    email: 'test@example.com',
    given_name: 'Test',
    family_name: 'User'
  }),
  signOut: jest.fn().mockResolvedValue(undefined),
}));