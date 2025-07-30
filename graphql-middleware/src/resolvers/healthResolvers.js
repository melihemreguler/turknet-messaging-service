const restApiClient = require('../restApiClient');
const logger = require('../logger');
const { GraphQLScalarType, Kind } = require('graphql');

const healthResolvers = {
  Query: {
    health: async () => {
      try {
        logger.debug('Health check request');
        const result = await restApiClient.getHealth();
        
        return {
          status: result.status || 'UP',
          timestamp: new Date(),
          details: JSON.stringify(result)
        };
      } catch (error) {
        logger.error('Health check failed', { error: error.message });
        
        return {
          status: 'DOWN',
          timestamp: new Date(),
          details: `Health check failed: ${error.message}`
        };
      }
    },

    readiness: async () => {
      try {
        logger.debug('Readiness check request');
        const result = await restApiClient.getReadiness();
        
        return {
          status: result.status || 'READY',
          timestamp: new Date(),
          details: JSON.stringify(result)
        };
      } catch (error) {
        logger.error('Readiness check failed', { error: error.message });
        
        return {
          status: 'NOT_READY',
          timestamp: new Date(),
          details: `Readiness check failed: ${error.message}`
        };
      }
    }
  },

  // Custom scalar for DateTime
  DateTime: new GraphQLScalarType({
    name: 'DateTime',
    description: 'Date custom scalar type',
    serialize(value) {
      if (value instanceof Date) {
        return value.toISOString();
      }
      if (typeof value === 'string') {
        return new Date(value).toISOString();
      }
      throw new Error('Value must be a Date or string');
    },
    parseValue(value) {
      if (typeof value === 'string') {
        return new Date(value);
      }
      throw new Error('Value must be a string');
    },
    parseLiteral(ast) {
      if (ast.kind === Kind.STRING) {
        return new Date(ast.value);
      }
      throw new Error('Value must be a string');
    }
  })
};

module.exports = healthResolvers;
