const restApiClient = require('../restApiClient');
const logger = require('../logger');

const authResolvers = {
  Mutation: {
    register: async (_, { input, clientInfo }) => {
      try {
        logger.info('Register user request', { username: input.username, email: input.email });
        
        const ipAddress = clientInfo?.ipAddress || '127.0.0.1';
        const userAgent = clientInfo?.userAgent || 'GraphQL-Client/1.0.0';
        
        const result = await restApiClient.register(input, ipAddress, userAgent);
        
        logger.info('User registered successfully', { 
          username: input.username,
          userId: result.userId 
        });
        
        return result;
      } catch (error) {
        logger.error('Register user failed', { 
          username: input.username,
          error: error.message 
        });
        throw error;
      }
    },

    login: async (_, { input, clientInfo }) => {
      try {
        logger.info('Login request', { username: input.username });
        
        const ipAddress = clientInfo?.ipAddress || '127.0.0.1';
        const userAgent = clientInfo?.userAgent || 'GraphQL-Client/1.0.0';
        
        const result = await restApiClient.login(input, ipAddress, userAgent);
        
        logger.info('User logged in successfully', { 
          username: input.username,
          userId: result.userId 
        });
        
        return result;
      } catch (error) {
        logger.error('Login failed', { 
          username: input.username,
          error: error.message 
        });
        throw error;
      }
    },

    logout: async (_, __, { sessionId }) => {
      try {
        if (!sessionId) {
          throw new Error('Session ID required for logout');
        }

        logger.info('Logout request', { sessionId: sessionId.substring(0, 8) + '...' });
        
        const result = await restApiClient.logout(sessionId);
        
        logger.info('User logged out successfully');
        
        return result;
      } catch (error) {
        logger.error('Logout failed', { error: error.message });
        throw error;
      }
    }
  }
};

module.exports = authResolvers;
