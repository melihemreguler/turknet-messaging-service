// Global test setup
global.console = {
  ...console,
  // Suppress console.log during tests unless it's an error
  log: jest.fn(),
  debug: jest.fn(),
  info: jest.fn(),
  warn: console.warn,
  error: console.error,
};
