/**
 * Test setup file
 */

// Mock environment variables for testing
process.env.NODE_ENV = 'test';
process.env.DB_PATH = ':memory:'; // Use in-memory database for tests
process.env.JWT_SECRET = 'test_jwt_secret';
process.env.LOG_LEVEL = 'error'; // Reduce log noise during tests

// Global test utilities
global.testUtils = {
  // Helper functions for tests can be added here
};