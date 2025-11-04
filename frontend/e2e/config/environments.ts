/**
 * Environment configuration for E2E tests
 * Allows switching between different environments (local And production)
 */

export type Environment = 'local' | 'production';

export interface EnvironmentConfig {
  baseURL: string;
  apiURL: string;
  timeout: number;
}

const environments: Record<Environment, EnvironmentConfig> = {
  local: {
    baseURL: process.env.PLAYWRIGHT_BASE_URL || 'http://localhost:3000',
    apiURL: process.env.PLAYWRIGHT_API_URL || 'http://localhost:8080',
    timeout: 30000,
  },
  production: {
    baseURL: process.env.PROD_BASE_URL || 'spm-orangle.vercel.app',
    apiURL: process.env.PROD_API_URL || 'https://spmorangle-85091184623.asia-east1.run.app',
    timeout: 60000,
  },
};

export function getEnvironment(): Environment {
  const env = (process.env.TEST_ENV || 'local') as Environment;
  if (!environments[env]) {
    throw new Error(`Unknown environment: ${env}`);
  }
  return env;
}

export function getConfig(): EnvironmentConfig {
  const env = getEnvironment();
  return environments[env];
}
