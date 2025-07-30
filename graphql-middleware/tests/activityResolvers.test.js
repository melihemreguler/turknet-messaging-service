const activityResolvers = require('../src/resolvers/activityResolvers');
const restApiClient = require('../src/restApiClient');
const logger = require('../src/logger');

jest.mock('../src/restApiClient');
jest.mock('../src/logger');

describe('activityResolvers', () => {
  describe('Query.getMyActivityLogs', () => {
    const sessionId = 'session123';
    const userId = 'user456';
    const pagination = { page: 1, size: 10 };

    beforeEach(() => {
      jest.clearAllMocks();
    });

    it('should throw error if not authenticated (Given)', async () => {
      // Given: No sessionId or userId
      // When: Resolver is called
      // Then: Throws authentication error
      await expect(
        activityResolvers.Query.getMyActivityLogs({}, { pagination }, {})
      ).rejects.toThrow('Authentication required');
    });

    it('should return transformed activity logs (Given)', async () => {
      // Given: Valid sessionId, userId, and mock API response
      const mockData = {
        success: true,
        data: {
          data: [
            {
              action: 'LOGIN',
              successful: true,
              timestamp: 123,
              ipAddress: '1.2.3.4',
              userAgent: 'agent',
              failureReason: null,
            },
          ],
          total: 1,
        },
      };
      restApiClient.getActivityLogs.mockResolvedValue(mockData);

      // When: Resolver is called
      const result = await activityResolvers.Query.getMyActivityLogs(
        {},
        { pagination },
        { sessionId, userId }
      );

      // Then: Returns transformed data
      expect(result.data.activities.length).toBe(1);
      expect(result.data.totalElements).toBe(1);
      expect(restApiClient.getActivityLogs).toHaveBeenCalledWith(
        sessionId,
        userId,
        pagination.page,
        pagination.size
      );
    });

    it('should handle API error (Given)', async () => {
      // Given: API throws error
      restApiClient.getActivityLogs.mockRejectedValue(new Error('API error'));

      // When: Resolver is called
      // Then: Throws error
      await expect(
        activityResolvers.Query.getMyActivityLogs(
          {},
          { pagination },
          { sessionId, userId }
        )
      ).rejects.toThrow('API error');
    });
  });
});
