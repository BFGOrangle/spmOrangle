// Jest setup file
import '@testing-library/jest-dom'

// Mock Next.js router
jest.mock('next/navigation', () => ({
  useRouter() {
    return {
      push: jest.fn(),
      replace: jest.fn(),
      refresh: jest.fn(),
      back: jest.fn(),
      forward: jest.fn(),
      prefetch: jest.fn(),
    }
  },
  useSearchParams() {
    return new URLSearchParams()
  },
  usePathname() {
    return '/'
  },
}))

// Mock fetch globally
global.fetch = jest.fn()

// Mock Next.js font imports
jest.mock('next/font/google', () => ({
  Geist: () => ({
    className: 'mocked-geist-font',
    variable: '--font-geist-sans'
  }),
  Geist_Mono: () => ({
    className: 'mocked-geist-mono-font',
    variable: '--font-geist-mono'
  })
}))