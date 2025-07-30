const GraphQLServer = require('../src/server');
const express = require('express');
const { ApolloServer } = require('apollo-server-express');
const logger = require('../src/logger');

jest.mock('express');
jest.mock('apollo-server-express');
jest.mock('../src/logger');

describe('GraphQLServer', () => {
  let server;

beforeEach(() => {
  jest.clearAllMocks();
  // Express mock: app objesi jest.fn fonksiyonları ile
  const appMock = {
    use: jest.fn(),
    get: jest.fn(),
  };
  express.mockReturnValue(appMock);
  server = new GraphQLServer();
});

  describe('constructor', () => {
    it('should initialize app and port (Given)', () => {
      // Given: New instance
      // When: Constructed
      // Then: app and port are set
      expect(server.app).toBeDefined();
      expect(server.port).toBe(process.env.PORT || 4000);
    });
  });

  describe('initialize', () => {
    it('should create ApolloServer and call setupMiddleware/setupRoutes (Given)', async () => {
      // Given: ApolloServer mock
      ApolloServer.mockImplementation(() => ({
        start: jest.fn().mockResolvedValue(),
        applyMiddleware: jest.fn()
      }));
      server.setupMiddleware = jest.fn();
      server.setupRoutes = jest.fn();

      // When: initialize is called
      await server.initialize();

      // Then: ApolloServer created, middleware/routes called
      expect(ApolloServer).toHaveBeenCalled();
      expect(server.setupMiddleware).toHaveBeenCalled();
      expect(server.setupRoutes).toHaveBeenCalled();
      expect(logger.info).toHaveBeenCalledWith('GraphQL server initialized successfully');
    });

    it('should log and throw error on failure (Given)', async () => {
      // Given: ApolloServer throws error
      ApolloServer.mockImplementation(() => { throw new Error('fail'); });
      server.setupMiddleware = jest.fn();
      server.setupRoutes = jest.fn();

      // When/Then: initialize throws and logs
      await expect(server.initialize()).rejects.toThrow('fail');
      expect(logger.error).toHaveBeenCalledWith('Failed to initialize GraphQL server', expect.any(Object));
    });
  });

  describe('setupMiddleware', () => {
    it('should set up all middlewares (Given)', () => {
      // Given: Express app mock
      const use = jest.fn();
      server.app.use = use;
      // When: setupMiddleware is called
      server.setupMiddleware();
      // Then: use called multiple times
      expect(use).toHaveBeenCalled();
    });
  });

  describe('setupRoutes', () => {
    it('should set up all routes (Given)', () => {
      // Given: Express app mock
      const get = jest.fn();
      const use = jest.fn();
      server.app.get = get;
      server.app.use = use;
      // When: setupRoutes is called
      server.setupRoutes();
      // Then: get and use called
      expect(get).toHaveBeenCalled();
      expect(use).toHaveBeenCalled();
    });
  });
});
