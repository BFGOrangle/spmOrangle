import type { Config } from "jest";

const config: Config = {
  clearMocks: true,
  collectCoverage: true,
  coverageDirectory: "coverage",
  coverageReporters: ["json-summary", "json", "lcov", "text", "html"],
  testEnvironment: "jsdom",
  preset: "ts-jest",
  setupFilesAfterEnv: ["<rootDir>/jest.setup.ts"],
  moduleNameMapper: {
    "^@/(.*)$": "<rootDir>/$1",
    "\\.(css|less|scss|sass)$": "<rootDir>/__tests__/mocks/styleMock.js",
  },
  transform: {
    "^.+\\.(ts|tsx)$": [
      "ts-jest",
      {
        tsconfig: {
          jsx: "react-jsx",
        },
      },
    ],
  },
  moduleFileExtensions: ["ts", "tsx", "js", "jsx", "json"],
  testMatch: [
    "**/__tests__/**/*.{test,spec}.{ts,tsx}",
    "**/*.(test|spec).{ts,tsx}",
  ],
  testPathIgnorePatterns: [
    "<rootDir>/node_modules/",
    "<rootDir>/.next/",
    "<rootDir>/__tests__/test-utils.tsx",
  ],
  transformIgnorePatterns: ["/node_modules/(?!(@tanstack)/)"],
  coverageProvider: "v8",
  collectCoverageFrom: [
    "**/*.{ts,tsx}",
    "!**/*.d.ts",
    "!**/node_modules/**",
    "!**/.next/**",
    "!**/coverage/**",
    "!jest.config.ts",
    "!jest.setup.ts",
    "!**/__tests__/test-utils.tsx",
    "!**/__tests__/utils/**",
    "!**/__tests__/mocks/**",
    "!**/components/ui/**",
    // Next.js configuration and build files
    "!**/app/layout.tsx",
    "!**/app/page.tsx",
    "!next.config.ts",
    "!next-env.d.ts",
    "!eslint.config.mjs",
    "!postcss.config.mjs",
    // Next.js layout files (minimal wrapper files)
    "!**/app/(app)/layout.tsx",
    "!**/app/(app)/auth/layout.tsx",
    // Next.js page wrappers (only import and export components)
    "!**/app/(app)/auth/forgot-password/page.tsx",
    "!**/app/(app)/auth/reset-password/page.tsx",
    "!**/app/(app)/auth/signin/page.tsx",
    "!**/app/(app)/auth/signup/page.tsx",
    // Type definitions (no runtime logic)
    "!**/types/**",
    // Configuration files
    "!**/lib/amplify-cognito-config.ts",
    // Utility scripts
    "!**/scripts/**",
  ],
};

export default config;
