const authResolvers = require('../src/resolvers/authResolvers');
const restApiClient = require('../src/restApiClient');
const logger = require('../src/logger');

jest.mock('../src/restApiClient');
jest.mock('../src/logger');

describe('authResolvers', () => {
  describe('Mutation.register', () => {
    const input = { username: 'test', email: 'test@test.com' };
    const clientInfo = { ipAddress: '1.2.3.4', userAgent: 'agent' };

    beforeEach(() => {
      jest.clearAllMocks();
    });

    it('should register user successfully (Given)', async () => {
      // Given: Valid input and mock API response
      const mockResult = { userId: 'user123', username: 'test' };
      restApiClient.register.mockResolvedValue(mockResult);

      // When: Resolver is called
      const result = await authResolvers.Mutation.register({}, { input, clientInfo });

      // Then: Returns result and logs
      expect(result).toEqual(mockResult);
      expect(restApiClient.register).toHaveBeenCalledWith(input, clientInfo.ipAddress, clientInfo.userAgent);
    });

    it('should handle registration error (Given)', async () => {
      // Given: API throws error
      restApiClient.register.mockRejectedValue(new Error('Register error'));

      // When: Resolver is called
      // Then: Throws error
      await expect(authResolvers.Mutation.register({}, { input, clientInfo })).rejects.toThrow('Register error');
    });
  });

  describe('Mutation.login', () => {
    const input = { username: 'test', password: 'pass' };
    const clientInfo = { ipAddress: '1.2.3.4', userAgent: 'agent' };

    beforeEach(() => {
      jest.clearAllMocks();
    });

    it('should login user successfully (Given)', async () => {
      // Given: Valid input and mock API response
      const mockResult = { token: 'token123', userId: 'user123' };
      restApiClient.login.mockResolvedValue(mockResult);

      // When: Resolver is called
      const result = await authResolvers.Mutation.login({}, { input, clientInfo });

      // Then: Returns result and logs
      expect(result).toEqual(mockResult);
      expect(restApiClient.login).toHaveBeenCalledWith(input, clientInfo.ipAddress, clientInfo.userAgent);
    });

    it('should handle login error (Given)', async () => {
      // Given: API throws error
      restApiClient.login.mockRejectedValue(new Error('Login error'));

      // When: Resolver is called
      // Then: Throws error
      await expect(authResolvers.Mutation.login({}, { input, clientInfo })).rejects.toThrow('Login error');
    });
  });
});
