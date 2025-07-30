const axios = require('axios');
const logger = require('./logger');

class RestApiClient {
  constructor() {
    this.baseURL = process.env.REST_API_BASE_URL || 'http://localhost:8080';
    this.timeout = parseInt(process.env.REST_API_TIMEOUT || '30000');
    
    this.client = axios.create({
      baseURL: this.baseURL,
      timeout: this.timeout,
      headers: {
        'Content-Type': 'application/json',
        'User-Agent': 'GraphQL-Middleware/1.0.0'
      }
    });

    this.setupInterceptors();
  }

  setupInterceptors() {
    // Request interceptor for logging
    this.client.interceptors.request.use(
      (config) => {
        logger.debug('REST API Request', {
          method: config.method?.toUpperCase(),
          url: config.url,
          headers: this.sanitizeHeaders(config.headers)
        });
        return config;
      },
      (error) => {
        logger.error('REST API Request Error', { error: error.message });
        return Promise.reject(error);
      }
    );

    // Response interceptor for logging
    this.client.interceptors.response.use(
      (response) => {
        logger.debug('REST API Response', {
          status: response.status,
          url: response.config.url,
          headers: this.sanitizeHeaders(response.headers)
        });
        return response;
      },
      (error) => {
        logger.error('REST API Response Error', {
          status: error.response?.status,
          url: error.config?.url,
          message: error.message,
          data: error.response?.data
        });
        return Promise.reject(error);
      }
    );
  }

  // Remove sensitive headers from logs
  sanitizeHeaders(headers) {
    const sanitized = { ...headers };
    delete sanitized['X-Session-Id'];
    delete sanitized['Authorization'];
    return sanitized;
  }

  // Auth endpoints
  async register(userData, ipAddress, userAgent) {
    try {
      const response = await this.client.post('/api/auth/register', userData, {
        headers: {
          'X-Forwarded-For': ipAddress,
          'User-Agent': userAgent
        }
      });
      return this.handleAuthResponse(response);
    } catch (error) {
      throw this.handleError(error, 'register');
    }
  }

  async login(credentials, ipAddress, userAgent) {
    try {
      const response = await this.client.post('/api/auth/login', credentials, {
        headers: {
          'X-Forwarded-For': ipAddress,
          'User-Agent': userAgent
        }
      });
      return this.handleAuthResponse(response);
    } catch (error) {
      throw this.handleError(error, 'login');
    }
  }

  async logout(sessionId) {
    try {
      const response = await this.client.post('/api/auth/logout', {}, {
        headers: {
          'X-Session-Id': sessionId
        }
      });
      return response.data;
    } catch (error) {
      throw this.handleError(error, 'logout');
    }
  }

  // Message endpoints
  async sendMessage(messageData, sessionId, userId) {
    try {
      const response = await this.client.post('/api/messages/send', messageData, {
        headers: {
          'X-Session-Id': sessionId,
          'X-User-Id': userId
        }
      });
      return response.data;
    } catch (error) {
      throw this.handleError(error, 'sendMessage');
    }
  }

  async getMessageHistory(username, sessionId, userId, limit = 20, offset = 0) {
    try {
      logger.debug('getMessageHistory params', { username, sessionId, userId, limit, offset });
      
      const params = new URLSearchParams({
        username,
        limit: limit.toString(),
        offset: offset.toString()
      });
      
      logger.debug('Request URL', { url: `/api/messages/history?${params}` });
      
      const response = await this.client.get(`/api/messages/history?${params}`, {
        headers: {
          'X-Session-Id': sessionId,
          'X-User-Id': userId
        }
      });
      return response.data;
    } catch (error) {
      throw this.handleError(error, 'getMessageHistory');
    }
  }

  // Activity endpoints
  async getActivityLogs(sessionId, userId, page = 0, size = 20) {
    try {
      const params = new URLSearchParams({
        page: page.toString(),
        size: size.toString()
      });
      
      const response = await this.client.get(`/api/activities/logs?${params}`, {
        headers: {
          'X-Session-Id': sessionId,
          'X-User-Id': userId
        }
      });
      return response.data;
    } catch (error) {
      throw this.handleError(error, 'getActivityLogs');
    }
  }

  // Health endpoints
  async getHealth() {
    try {
      const response = await this.client.get('/api/health');
      return response.data;
    } catch (error) {
      throw this.handleError(error, 'getHealth');
    }
  }

  async getReadiness() {
    try {
      const response = await this.client.get('/api/ready');
      return response.data;
    } catch (error) {
      throw this.handleError(error, 'getReadiness');
    }
  }

  // Helper methods
  handleAuthResponse(response) {
    const sessionId = response.headers['x-session-id'];
    const userId = response.headers['x-user-id'];
    
    return {
      ...response.data,
      sessionId,
      userId
    };
  }

  handleError(error, operation) {
    const statusCode = error.response?.status || 500;
    const message = error.response?.data?.message || error.message || 'Unknown error';
    const details = error.response?.data || {};

    logger.error(`REST API Error in ${operation}`, {
      statusCode,
      message,
      details,
      operation
    });

    // Convert REST API errors to GraphQL errors
    const graphqlError = new Error(message);
    graphqlError.extensions = {
      code: this.getErrorCode(statusCode),
      statusCode,
      details,
      operation
    };

    return graphqlError;
  }

  getErrorCode(statusCode) {
    switch (statusCode) {
      case 400:
        return 'BAD_REQUEST';
      case 401:
        return 'UNAUTHORIZED';
      case 403:
        return 'FORBIDDEN';
      case 404:
        return 'NOT_FOUND';
      case 409:
        return 'CONFLICT';
      case 422:
        return 'VALIDATION_ERROR';
      case 500:
        return 'INTERNAL_SERVER_ERROR';
      default:
        return 'UNKNOWN_ERROR';
    }
  }
}

module.exports = new RestApiClient();
