const messageResolvers = require('../src/resolvers/messageResolvers');
const restApiClient = require('../src/restApiClient');
const logger = require('../src/logger');

jest.mock('../src/restApiClient');
jest.mock('../src/logger');

describe('messageResolvers', () => {
  describe('Query.getMessageHistory', () => {
    const sessionId = 'session123';
    const userId = 'user456';
    const username = 'testuser';
    const pagination = { page: 1, size: 10 };

    beforeEach(() => {
      jest.clearAllMocks();
    });

    it('should throw error if not authenticated (Given)', async () => {
      // Given: No sessionId or userId
      // When: Resolver is called
      // Then: Throws authentication error
      await expect(
        messageResolvers.Query.getMessageHistory({}, { username, pagination }, {})
      ).rejects.toThrow('Authentication required');
    });

    it('should return message history (Given)', async () => {
      // Given: Valid sessionId, userId, and mock API response
      const mockResult = { data: { total: 1, messages: [{ id: 1, text: 'Hello' }] } };
      restApiClient.getMessageHistory.mockResolvedValue(mockResult);

      // When: Resolver is called
      const result = await messageResolvers.Query.getMessageHistory(
        {},
        { username, pagination },
        { sessionId, userId }
      );

      // Then: Returns result
      expect(result).toEqual(mockResult);
      expect(restApiClient.getMessageHistory).toHaveBeenCalledWith(
        username,
        sessionId,
        userId,
        pagination.size,
        pagination.page * pagination.size
      );
    });

    it('should handle API error (Given)', async () => {
      // Given: API throws error
      restApiClient.getMessageHistory.mockRejectedValue(new Error('API error'));

      // When: Resolver is called
      // Then: Throws error
      await expect(
        messageResolvers.Query.getMessageHistory(
          {},
          { username, pagination },
          { sessionId, userId }
        )
      ).rejects.toThrow('API error');
    });
  });
});
