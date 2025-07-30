const restApiClient = require('../src/restApiClient');

// Mock axios
jest.mock('axios');
const axios = require('axios');

describe('RestApiClient', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    
    // Setup axios mock
    axios.create.mockReturnValue({
      interceptors: {
        request: { use: jest.fn() },
        response: { use: jest.fn() }
      },
      post: jest.fn(),
      get: jest.fn()
    });
  });

  describe('Error handling', () => {
    test('should convert 400 error to BAD_REQUEST', () => {
      const error = {
        response: {
          status: 400,
          data: { message: 'Bad request' }
        }
      };

      const graphqlError = restApiClient.handleError(error, 'test');
      
      expect(graphqlError.extensions.code).toBe('BAD_REQUEST');
      expect(graphqlError.extensions.statusCode).toBe(400);
    });

    test('should convert 401 error to UNAUTHORIZED', () => {
      const error = {
        response: {
          status: 401,
          data: { message: 'Unauthorized' }
        }
      };

      const graphqlError = restApiClient.handleError(error, 'test');
      
      expect(graphqlError.extensions.code).toBe('UNAUTHORIZED');
      expect(graphqlError.extensions.statusCode).toBe(401);
    });

    test('should handle unknown errors', () => {
      const error = {
        message: 'Network error'
      };

      const graphqlError = restApiClient.handleError(error, 'test');
      
      expect(graphqlError.extensions.code).toBe('UNKNOWN_ERROR');
      expect(graphqlError.extensions.statusCode).toBe(500);
    });
  });

  describe('sanitizeHeaders', () => {
    test('should remove sensitive headers', () => {
      const headers = {
        'Content-Type': 'application/json',
        'X-Session-Id': 'secret-session',
        'Authorization': 'Bearer token',
        'User-Agent': 'test-agent'
      };

      const sanitized = restApiClient.sanitizeHeaders(headers);

      expect(sanitized).toEqual({
        'Content-Type': 'application/json',
        'User-Agent': 'test-agent'
      });
      expect(sanitized['X-Session-Id']).toBeUndefined();
      expect(sanitized['Authorization']).toBeUndefined();
    });
  });
});
