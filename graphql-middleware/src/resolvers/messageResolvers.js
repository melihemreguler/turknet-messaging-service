const restApiClient = require('../restApiClient');
const logger = require('../logger');

const messageResolvers = {
  Query: {
    getMessageHistory: async (_, { username, pagination }, { sessionId, userId }) => {
      try {
        if (!sessionId || !userId) {
          throw new Error('Authentication required');
        }

        logger.info('Get message history request', { 
          username, 
          userId,
          pagination 
        });
        
        const limit = pagination?.size || 20;
        const offset = pagination?.page * limit || 0;
        
        const result = await restApiClient.getMessageHistory(
          username, 
          sessionId, 
          userId, 
          limit, 
          offset
        );
        
        logger.info('Message history retrieved successfully', { 
          username,
          userId,
          total: result.data?.total || 0
        });
        
        return result;
      } catch (error) {
        logger.error('Get message history failed', { 
          username,
          userId,
          error: error.message 
        });
        throw error;
      }
    }
  },

  Mutation: {
    sendMessage: async (_, { input }, { sessionId, userId }) => {
      try {
        if (!sessionId || !userId) {
          throw new Error('Authentication required');
        }

        logger.info('Send message request', { 
          receiverUsername: input.receiverUsername,
          senderId: userId,
          contentLength: input.content.length
        });
        
        // Transform GraphQL input to Spring Boot API format
        const messageData = {
          recipient: input.receiverUsername,
          content: input.content
        };
        
        const result = await restApiClient.sendMessage(messageData, sessionId, userId);
        
        logger.info('Message sent successfully', { 
          receiverUsername: input.receiverUsername,
          senderId: userId,
          messageId: result.data?.id
        });
        
        return result;
      } catch (error) {
        logger.error('Send message failed', { 
          receiverUsername: input.receiverUsername,
          senderId: userId,
          error: error.message 
        });
        throw error;
      }
    }
  }
};

module.exports = messageResolvers;
