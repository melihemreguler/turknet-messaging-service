# Turknet Messaging GraphQL Middleware

A Node.js GraphQL Backend-for-Frontend (BFF) middleware that provides a GraphQL interface to the Turknet Messaging Service REST API.

## Features

- **GraphQL API**: Provides a unified GraphQL interface for frontend applications
- **REST API Proxy**: Converts GraphQL operations to REST API calls
- **Authentication**: Handles session-based authentication with header forwarding
- **Structured Logging**: Uses Winston for comprehensive logging with Elasticsearch integration
- **Error Handling**: Comprehensive error handling with proper GraphQL error formatting
- **Health Checks**: Built-in health and readiness endpoints
- **Docker Ready**: Fully containerized with Docker support
- **Testing**: Unit tests with Jest

## Architecture

This middleware serves as a Backend-for-Frontend (BFF) pattern implementation:

```
Frontend Apps → GraphQL Middleware → REST API (Spring Boot)
```

The middleware:
1. Receives GraphQL queries/mutations from frontend applications
2. Converts them to appropriate REST API calls
3. Handles authentication by forwarding session headers
4. Returns GraphQL-formatted responses

## API Schema

### Authentication
- `register(input: RegisterInput!, clientInfo: ClientInfoInput): AuthResponse!`
- `login(input: LoginInput!, clientInfo: ClientInfoInput): AuthResponse!`
- `logout: GenericResponse!`

### Messages
- `sendMessage(input: SendMessageInput!): MessageResponse!`
- `getMessageHistory(username: String!, pagination: PaginationInput): MessageHistoryResponse!`

### Activities
- `getMyActivityLogs(pagination: PaginationInput): ActivityResponse!`

### Health
- `health: HealthStatus!`
- `readiness: HealthStatus!`

## Getting Started

### Prerequisites

- Node.js 18+
- npm or yarn
- Running Turknet Messaging Service (Spring Boot API)

### Installation

1. Install dependencies:
```bash
npm install
```

2. Copy environment configuration:
```bash
cp .env.example .env
```

3. Configure environment variables in `.env`:
```env
NODE_ENV=development
PORT=4000
REST_API_BASE_URL=http://localhost:8080
LOG_LEVEL=info
GRAPHQL_PLAYGROUND=true
GRAPHQL_INTROSPECTION=true
```

### Running

#### Development
```bash
npm run dev
```

#### Production
```bash
npm start
```

### Testing

```bash
# Run tests
npm test

# Run tests in watch mode
npm run test:watch

# Run with coverage
npm test -- --coverage
```

### Linting

```bash
# Check code style
npm run lint

# Fix code style issues
npm run lint:fix
```

## Usage Examples

### Using GraphQL Playground

When `GRAPHQL_PLAYGROUND=true`, visit `http://localhost:4000/graphql` for interactive queries.

### Authentication Flow

1. **Register a user:**
```graphql
mutation RegisterUser {
  register(
    input: {
      username: "johndoe"
      email: "john@example.com"
      password: "securePassword123"
    }
    clientInfo: {
      ipAddress: "192.168.1.100"
      userAgent: "MyApp/1.0.0"
    }
  ) {
    success
    message
    data {
      id
      username
      email
      createdAt
    }
    sessionId
    userId
  }
}
```

2. **Login:**
```graphql
mutation LoginUser {
  login(
    input: {
      username: "johndoe"
      password: "securePassword123"
    }
    clientInfo: {
      ipAddress: "192.168.1.100"
      userAgent: "MyApp/1.0.0"
    }
  ) {
    success
    message
    sessionId
    userId
  }
}
```

3. **Use the returned sessionId and userId in headers for authenticated requests:**
```javascript
// In your GraphQL client
const headers = {
  'X-Session-Id': 'returned-session-id',
  'X-User-Id': 'returned-user-id'
};
```

### Messaging

```graphql
# Send a message (requires authentication headers)
mutation SendMessage {
  sendMessage(
    input: {
      receiverUsername: "janedoe"
      content: "Hello, how are you?"
    }
  ) {
    success
    message
    data {
      threadId
      sender
      content
      timestamp
    }
  }
}

# Get message history (requires authentication headers)
query GetMessageHistory {
  getMessageHistory(
    username: "targetuser"
    pagination: {
      page: 0
      size: 20
    }
  ) {
    success
    message
    data {
      data {
        id
        threadId
        senderId
        senderUsername
        content
        timestamp
      }
      total
      limit
      offset
    }
  }
}
```

### Activity Logs

```graphql
# Get user activity logs (requires authentication headers)
query GetMyActivities {
  getMyActivityLogs(
    pagination: {
      page: 0
      size: 50
    }
  ) {
    success
    message
    data {
      activities {
        id
        activityType
        description
        timestamp
        ipAddress
        userAgent
        metadata
      }
      totalElements
      totalPages
      currentPage
      pageSize
    }
  }
}
```

## Authentication

The middleware handles authentication by:

1. **Session Headers**: Requires `X-Session-Id` and `X-User-Id` headers for protected operations
2. **Header Forwarding**: Forwards authentication headers to the REST API
3. **Context Management**: Makes session information available to all resolvers

### Protected Operations

All operations except `register`, `login`, `health`, and `readiness` require authentication headers.

## Error Handling

The middleware provides comprehensive error handling:

- **GraphQL Errors**: Proper GraphQL error formatting with extensions
- **HTTP Status Mapping**: Maps REST API HTTP status codes to GraphQL error codes
- **Logging**: All errors are logged with context information
- **Security**: Sensitive information is filtered from logs and responses

## Logging

Uses Winston for structured logging with:

- **JSON Format**: Structured logs for Elasticsearch integration
- **Log Levels**: Configurable log levels (error, warn, info, debug)
- **Context**: Rich contextual information for debugging
- **Security**: Sensitive data filtering (session IDs, passwords)

## Health Checks

- **`/health`**: General health status
- **`/ready`**: Readiness probe for Kubernetes
- **GraphQL Health**: `health` and `readiness` queries

## Docker Integration

The service is designed to run in Docker and integrates with the main docker-compose.yml:

```yaml
services:
  graphql-middleware:
    image: turknet-messaging-graphql-middleware:latest
    ports:
      - "4000:4000"
    environment:
      - NODE_ENV=production
      - REST_API_BASE_URL=http://messaging-command-service:8080
    depends_on:
      - messaging-command-service
```

## Performance Considerations

- **Connection Pooling**: Axios client with keep-alive
- **Compression**: gzip compression for responses
- **Timeout Handling**: Configurable timeouts for REST API calls
- **Caching**: Response caching can be added for read-heavy operations

## Security

- **CORS**: Configurable CORS settings
- **Helmet**: Security headers with Helmet.js
- **Input Validation**: GraphQL schema validation
- **Header Sanitization**: Sensitive headers filtered from logs

## Development

### Project Structure

```
src/
├── index.js              # Application entry point
├── server.js             # Express + Apollo Server setup
├── schema.js             # GraphQL schema definitions
├── logger.js             # Winston logger configuration
├── restApiClient.js      # REST API client with axios
└── resolvers/
    ├── index.js          # Resolver aggregation
    ├── authResolvers.js  # Authentication resolvers
    ├── messageResolvers.js # Message resolvers
    ├── activityResolvers.js # Activity resolvers
    └── healthResolvers.js   # Health check resolvers
```

### Adding New Resolvers

1. Define types in `schema.js`
2. Create resolver file in `src/resolvers/`
3. Add REST API methods to `restApiClient.js`
4. Import and merge in `src/resolvers/index.js`
5. Add tests in `tests/`

## License

MIT License - see LICENSE file for details.
