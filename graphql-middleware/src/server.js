const express = require('express');
const { ApolloServer } = require('apollo-server-express');
const cors = require('cors');
const helmet = require('helmet');
const compression = require('compression');
require('dotenv').config();

const typeDefs = require('./schema');
const resolvers = require('./resolvers');
const logger = require('./logger');

class GraphQLServer {
  constructor() {
    this.app = express();
    this.server = null;
    this.port = process.env.PORT || 4000;
  }

  async initialize() {
    try {
      // Create Apollo Server
      this.server = new ApolloServer({
        typeDefs,
        resolvers,
        context: ({ req }) => {
          // Extract session and user information from headers
          const sessionId = req.headers['x-session-id'];
          const userId = req.headers['x-user-id'];
          
          // Log the request
          logger.debug('GraphQL Request', {
            operation: req.body?.operationName,
            sessionId: sessionId ? sessionId.substring(0, 8) + '...' : undefined,
            userId
          });

          return {
            sessionId,
            userId,
            req
          };
        },
        formatError: (error) => {
          logger.error('GraphQL Error', {
            message: error.message,
            path: error.path,
            extensions: error.extensions
          });

          // Don't expose internal errors to client in production
          if (process.env.NODE_ENV === 'production' && !error.extensions?.code) {
            return new Error('Internal server error');
          }

          return error;
        },
        formatResponse: (response, { request }) => {
          logger.debug('GraphQL Response', {
            operation: request.operationName,
            hasErrors: !!response.errors,
            errorCount: response.errors?.length || 0
          });

          return response;
        },
        playground: process.env.GRAPHQL_PLAYGROUND === 'true',
        introspection: process.env.GRAPHQL_INTROSPECTION === 'true',
        debug: process.env.GRAPHQL_DEBUG === 'true'
      });

      await this.server.start();
      
      this.setupMiddleware();
      this.server.applyMiddleware({ 
        app: this.app, 
        path: '/graphql',
        cors: false // We handle CORS ourselves
      });
      
      this.setupRoutes();
      
      logger.info('GraphQL server initialized successfully');
    } catch (error) {
      logger.error('Failed to initialize GraphQL server', { error: error.message });
      throw error;
    }
  }

  setupMiddleware() {
    // Security middleware
    this.app.use(helmet({
      contentSecurityPolicy: process.env.NODE_ENV === 'production',
      crossOriginEmbedderPolicy: false
    }));

    // Compression middleware
    this.app.use(compression());

    // CORS middleware
    const corsOptions = {
      origin: process.env.CORS_ORIGIN === '*' ? true : process.env.CORS_ORIGIN?.split(','),
      credentials: process.env.CORS_CREDENTIALS === 'true',
      methods: ['GET', 'POST', 'OPTIONS'],
      allowedHeaders: ['Content-Type', 'Authorization', 'X-Session-Id', 'X-User-Id'],
      exposedHeaders: ['X-Session-Id', 'X-User-Id']
    };
    this.app.use(cors(corsOptions));

    // Request logging middleware
    this.app.use((req, res, next) => {
      const start = Date.now();
      
      res.on('finish', () => {
        const duration = Date.now() - start;
        logger.info('HTTP Request', {
          method: req.method,
          url: req.url,
          statusCode: res.statusCode,
          duration,
          userAgent: req.get('User-Agent'),
          ip: req.ip || req.connection.remoteAddress
        });
      });
      
      next();
    });

    // Parse JSON bodies
    this.app.use(express.json({ limit: '10mb' }));
    this.app.use(express.urlencoded({ extended: true, limit: '10mb' }));
  }

  setupRoutes() {
    // Health check endpoints
    this.app.get('/health', (req, res) => {
      res.json({
        status: 'UP',
        timestamp: new Date().toISOString(),
        service: 'turknet-messaging-graphql-middleware',
        version: process.env.npm_package_version || '1.0.0'
      });
    });

    this.app.get('/ready', (req, res) => {
      res.json({
        status: 'READY',
        timestamp: new Date().toISOString(),
        service: 'turknet-messaging-graphql-middleware'
      });
    });

    // GraphQL playground redirect for convenience
    this.app.get('/', (req, res) => {
      if (process.env.GRAPHQL_PLAYGROUND === 'true') {
        res.redirect('/graphql');
      } else {
        res.json({
          message: 'Turknet Messaging GraphQL Middleware',
          version: process.env.npm_package_version || '1.0.0',
          endpoints: {
            graphql: '/graphql',
            health: '/health',
            readiness: '/ready'
          }
        });
      }
    });

    // 404 handler
    this.app.use('*', (req, res) => {
      res.status(404).json({
        error: 'Not Found',
        message: 'The requested endpoint does not exist',
        availableEndpoints: {
          graphql: '/graphql',
          health: '/health',
          readiness: '/ready'
        }
      });
    });

    // Error handler
    this.app.use((error, req, res, next) => {
      logger.error('Express Error', {
        message: error.message,
        stack: error.stack,
        url: req.url,
        method: req.method
      });

      res.status(error.status || 500).json({
        error: process.env.NODE_ENV === 'production' ? 'Internal Server Error' : error.message,
        timestamp: new Date().toISOString()
      });
    });
  }

  async start() {
    try {
      await this.initialize();
      
      const httpServer = this.app.listen(this.port, () => {
        logger.info('Server started successfully', {
          port: this.port,
          graphqlEndpoint: `http://localhost:${this.port}/graphql`,
          environment: process.env.NODE_ENV || 'development'
        });
      });

      // Graceful shutdown
      const gracefulShutdown = async (signal) => {
        logger.info(`Received ${signal}, shutting down gracefully...`);
        
        httpServer.close(() => {
          logger.info('HTTP server closed');
        });

        if (this.server) {
          await this.server.stop();
          logger.info('GraphQL server stopped');
        }

        process.exit(0);
      };

      process.on('SIGTERM', () => gracefulShutdown('SIGTERM'));
      process.on('SIGINT', () => gracefulShutdown('SIGINT'));

      return httpServer;
    } catch (error) {
      logger.error('Failed to start server', { error: error.message });
      process.exit(1);
    }
  }
}

module.exports = GraphQLServer;
