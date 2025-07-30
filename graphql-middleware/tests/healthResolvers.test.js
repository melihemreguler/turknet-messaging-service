const healthResolvers = require('../src/resolvers/healthResolvers');
const restApiClient = require('../src/restApiClient');
const logger = require('../src/logger');

jest.mock('../src/restApiClient');
jest.mock('../src/logger');

describe('healthResolvers', () => {
  describe('Query.health', () => {
    beforeEach(() => {
      jest.clearAllMocks();
    });

    it('should return health status UP (Given)', async () => {
      // Given: API returns status
      const mockResult = { status: 'UP', details: 'ok' };
      restApiClient.getHealth.mockResolvedValue(mockResult);

      // When: Resolver is called
      const result = await healthResolvers.Query.health();

      // Then: Returns status UP
      expect(result.status).toBe('UP');
      expect(restApiClient.getHealth).toHaveBeenCalled();
    });

    it('should handle health check error (Given)', async () => {
      // Given: API throws error
      restApiClient.getHealth.mockRejectedValue(new Error('Health error'));

      // When: Resolver is called
      const result = await healthResolvers.Query.health();

      // Then: Returns status DOWN
      expect(result.status).toBe('DOWN');
      expect(result.details).toContain('Health check failed');
    });
  });

  describe('Query.readiness', () => {
    beforeEach(() => {
      jest.clearAllMocks();
    });

    it('should return readiness status READY (Given)', async () => {
      // Given: API returns status
      const mockResult = { status: 'READY', details: 'ok' };
      restApiClient.getReadiness.mockResolvedValue(mockResult);

      // When: Resolver is called
      const result = await healthResolvers.Query.readiness();

      // Then: Returns status READY
      expect(result.status).toBe('READY');
      expect(restApiClient.getReadiness).toHaveBeenCalled();
    });

    it('should handle readiness check error (Given)', async () => {
      // Given: API throws error
      restApiClient.getReadiness.mockRejectedValue(new Error('Readiness error'));

      // When: Resolver is called
      const result = await healthResolvers.Query.readiness();

      // Then: Returns status NOT_READY
      expect(result.status).not.toBe('READY');
      expect(result.details).toContain('Readiness check failed');
    });
  });
});
