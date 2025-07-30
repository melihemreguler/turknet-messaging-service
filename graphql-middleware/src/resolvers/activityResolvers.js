const restApiClient = require('../restApiClient');
const logger = require('../logger');

const activityResolvers = {
  Query: {
    getMyActivityLogs: async (_, { pagination }, { sessionId, userId }) => {
      try {
        if (!sessionId || !userId) {
          throw new Error('Authentication required');
        }

        logger.info('Get activity logs request', { 
          userId,
          pagination 
        });
        
        const page = pagination?.page || 0;
        const size = pagination?.size || 20;
        
        const result = await restApiClient.getActivityLogs(
          sessionId, 
          userId, 
          page, 
          size
        );
        
        // Transform the response to match GraphQL schema
        if (result.success && result.data && result.data.data) {
          const transformedData = {
            activities: result.data.data.map(activity => ({
              id: `${userId}-${activity.timestamp}`, // Generate a unique ID
              userId: userId,
              activityType: activity.action || 'UNKNOWN',
              description: `${activity.action} - ${activity.successful ? 'Success' : 'Failed'}${activity.failureReason ? ': ' + activity.failureReason : ''}`,
              ipAddress: activity.ipAddress,
              userAgent: activity.userAgent,
              timestamp: activity.timestamp,
              metadata: JSON.stringify({ successful: activity.successful, failureReason: activity.failureReason })
            })),
            totalElements: result.data.total || 0,
            totalPages: Math.ceil((result.data.total || 0) / size),
            currentPage: page,
            pageSize: size
          };

          logger.info('Activity logs retrieved successfully', { 
            userId,
            totalElements: transformedData.totalElements
          });

          return {
            success: result.success,
            message: result.message,
            data: transformedData
          };
        }
        
        return result;
      } catch (error) {
        logger.error('Get activity logs failed', { 
          userId,
          error: error.message 
        });
        throw error;
      }
    }
  }
};

module.exports = activityResolvers;
