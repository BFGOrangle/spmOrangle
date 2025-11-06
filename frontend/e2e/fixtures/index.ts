/**
 * Fixtures Index
 * Re-exports all fixtures for easy importing
 */

export { test, expect } from './test-fixtures';
export type { TestFixtures } from './test-fixtures';

// Re-export data types for convenience
export * from './data/task-data-types';
export * from './data/task-data-builder';
export * from './data/task-data-factory';

// Re-export page objects for convenience
export * from './pages/tasks-page';
export * from './pages/task-detail-page';
export * from './pages/kanban-board-page';
export * from './pages/my-tasks-page';

// Re-export services for convenience
export * from './services/task-cleanup-service';