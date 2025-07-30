# GraphQL Middleware - API Examples

This file contains comprehensive API examples for Turknet Messaging GraphQL Middleware.

##  Getting Started

### Starting GraphQL Middleware

```bash
# Start GraphQL Middleware
node index.js
```

## 🔍 Health Check Endpoints

### 1. REST Health Check

```bash
curl http://localhost:4000/health

# JSON format
curl -s http://localhost:4000/health | jq .
```

**Response:**
```json
{
  "status": "UP",
  "timestamp": "2025-07-30T11:40:47.499Z",
  "service": "turknet-messaging-graphql-middleware",
  "version": "1.0.0"
}
```

### 2. REST Readiness Check

```bash
curl -s http://localhost:4000/ready | jq .
```

**Response:**
```json
{
  "status": "READY",
  "timestamp": "2025-07-30T11:41:25.114Z",
  "service": "turknet-messaging-graphql-middleware"
}
```

### 3. GraphQL Health Query

```bash
curl -s -X POST http://localhost:4000/graphql \
   -H "Content-Type: application/json" \
   -d '{"query": "query { health { status timestamp } }"}' | jq .
```

**Response:**
```json
{
  "data": {
    "health": {
      "status": "UP",
      "timestamp": "2025-07-30T12:12:48.788Z"
    }
  }
}
```

##  Authentication

### 4. User Registration

```bash
curl -s -X POST http://localhost:4000/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "mutation RegisterUser($input: RegisterInput!, $clientInfo: ClientInfoInput) { register(input: $input, clientInfo: $clientInfo) { success message sessionId userId data { id username createdAt } } }",
    "variables": {
      "input": {
        "username": "graphqltest02",
        "email": "graphqltest02@example.com",
        "password": "testPassword123"
      },
      "clientInfo": {
        "ipAddress": "127.0.0.1",
        "userAgent": "curl/GraphQL-E2E-Test"
      }
    }
  }' | jq .
```

**Response:**
```json
{
  "data": {
    "register": {
      "success": true,
      "message": "User registered successfully",
      "sessionId": "551cf82b-0067-4d21-9f66-950012f27558",
      "userId": "688a05d5bf6ccf222474b6a5",
      "data": {
        "id": "688a05d5bf6ccf222474b6a5",
        "username": "graphqltest02",
        "createdAt": "2025-07-30T11:45:25.392Z"
      }
    }
  }
}
```

### 5. User Login

```bash
curl -s -X POST http://localhost:4000/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "mutation LoginUser($input: LoginInput!, $clientInfo: ClientInfoInput) { login(input: $input, clientInfo: $clientInfo) { success message sessionId userId } }",
    "variables": {
      "input": {
        "username": "graphqltest02",
        "password": "testPassword123"
      },
      "clientInfo": {
        "ipAddress": "127.0.0.1",
        "userAgent": "curl/GraphQL-E2E-Test"
      }
    }
  }' | jq .
```

**Response:**
```json
{
  "data": {
    "login": {
      "success": true,
      "message": "Login successful",
      "sessionId": "62b6a7bf-0a6e-4569-8f98-b865144edd83",
      "userId": "688a05d5bf6ccf222474b6a5"
    }
  }
}
```

### 6. User Logout

```bash
curl -X POST http://localhost:4000/graphql \
  -H "Content-Type: application/json" \
  -H "X-Session-Id: 091ca818-0a99-4b1a-b82d-b06a348138d7" \
  -H "X-User-Id: 688912feefdc7d27c3c2e9df" \
  -d '{
    "query": "mutation Logout { logout { success message data } }"
  }' | jq .
```

**Response:**
```json
{
  "data": {
    "logout": {
      "success": true,
      "message": "Logout successful",
      "data": "Session invalidated"
    }
  }
}
```

##  Messaging

### 7. Send Message

```bash
curl -X POST http://localhost:4000/graphql \
  -H "Content-Type: application/json" \
  -H "X-Session-Id: 85332927-de0a-44cf-b1fb-b7c47b767a7b" \
  -H "X-User-Id: 688a0ca4bf6ccf222474b6a7" \
  -d '{
    "query": "mutation SendMessage($input: SendMessageInput!) { sendMessage(input: $input) { success message data { threadId sender content timestamp } } }",
    "variables": {
      "input": {
        "receiverUsername": "testuser10",
        "content": "GraphQL middleware successful test message!"
      }
    }
  }' | jq .
```

**Response:**
```json
{
  "data": {
    "sendMessage": {
      "success": true,
      "message": "Message sent successfully",
      "data": {
        "threadId": "688912feefdc7d27c3c2e9df-688a0ca4bf6ccf222474b6a7",
        "sender": "688a0ca4bf6ccf222474b6a7",
        "content": "GraphQL middleware successful test message!",
        "timestamp": "2025-07-30T12:17:52.664Z"
      }
    }
  }
}
```

### 8. Get Message History

```bash
curl -X POST http://localhost:4000/graphql \
  -H "Content-Type: application/json" \
  -H "X-Session-Id: 091ca818-0a99-4b1a-b82d-b06a348138d7" \
  -H "X-User-Id: 688912feefdc7d27c3c2e9df" \
  -d '{
    "query": "query GetMessageHistory($username: String!, $pagination: PaginationInput) { getMessageHistory(username: $username, pagination: $pagination) { success message data { data { id threadId senderId senderUsername content timestamp } total limit offset } } }",
    "variables": {
      "username": "testuser2",
      "pagination": {
        "page": 0,
        "size": 5
      }
    }
  }' | jq .
```

**Response:**
```json
{
  "data": {
    "getMessageHistory": {
      "success": true,
      "message": "Conversation retrieved successfully",
      "data": {
        "data": [
          {
            "id": "688919c60eb2bb81a5192509",
            "threadId": "688783843ee7316aae30495a-688912feefdc7d27c3c2e9df",
            "senderId": "688912feefdc7d27c3c2e9df",
            "senderUsername": "testuser10",
            "content": "I sent this message from docker profile",
            "timestamp": "2025-07-29T18:58:14.536Z"
          },
          {
            "id": "68891add0eb2bb81a519250b",
            "threadId": "688783843ee7316aae30495a-688912feefdc7d27c3c2e9df",
            "senderId": "688783843ee7316aae30495a",
            "senderUsername": "testuser2",
            "content": "I sent this as a reply message",
            "timestamp": "2025-07-29T19:02:53.119Z"
          },
          {
            "id": "68892968d7ef6eb3b50d7e68",
            "threadId": "688783843ee7316aae30495a-688912feefdc7d27c3c2e9df",
            "senderId": "688783843ee7316aae30495a",
            "senderUsername": "testuser2",
            "content": "I sent this as a reply message",
            "timestamp": "2025-07-29T20:04:56.355Z"
          },
          {
            "id": "688934ab59f8953910f1b5d1",
            "threadId": "688783843ee7316aae30495a-688912feefdc7d27c3c2e9df",
            "senderId": "688783843ee7316aae30495a",
            "senderUsername": "testuser2",
            "content": "I sent this as a reply message",
            "timestamp": "2025-07-29T20:52:59.986Z"
          },
          {
            "id": "6889e7a1f5bbb838cb4bcffa",
            "threadId": "688783843ee7316aae30495a-688912feefdc7d27c3c2e9df",
            "senderId": "688783843ee7316aae30495a",
            "senderUsername": "testuser2",
            "content": "I sent this as a reply message",
            "timestamp": "2025-07-30T09:36:33.611Z"
          }
        ],
        "total": 10,
        "limit": 5,
        "offset": 0
      }
    }
  }
}
```

## Activity Logs

### 9. Get Activity Logs

```bash
curl -s -X POST http://localhost:4000/graphql \
  -H "Content-Type: application/json" \
  -H "X-Session-Id: 85332927-de0a-44cf-b1fb-b7c47b767a7b" \
  -H "X-User-Id: 688a0ca4bf6ccf222474b6a7" \
  -d '{
    "query": "query GetMyActivities { getMyActivityLogs(pagination: { page: 0, size: 10 }) { success message data { totalElements totalPages currentPage activities { id activityType description timestamp ipAddress } } } }"
  }' | jq .
```

**Response:**
```json
{
  "data": {
    "getMyActivityLogs": {
      "success": true,
      "message": "Activity logs retrieved successfully",
      "data": {
        "totalElements": 2,
        "totalPages": 1,
        "currentPage": 0,
        "activities": [
          {
            "id": "688a05d5bf6ccf222474b6a5-2025-07-30T14:45:25.394",
            "activityType": "USER_CREATION",
            "description": "USER_CREATION - Success",
            "timestamp": "2025-07-30T11:45:25.394Z",
            "ipAddress": "127.0.0.1"
          },
          {
            "id": "688a05d5bf6ccf222474b6a5-2025-07-30T14:46:01.878",
            "activityType": "LOGIN_ATTEMPT",
            "description": "LOGIN_ATTEMPT - Success",
            "timestamp": "2025-07-30T11:46:01.878Z",
            "ipAddress": "127.0.0.1"
          }
        ]
      }
    }
  }
}
```

## GraphQL Only Queries

### 10. Health Check (GraphQL)

```graphql
query HealthCheck {
  health {
    status
    timestamp
    details
  }
}
```

### 11. Readiness Check (GraphQL)

```graphql
query ReadinessCheck {
  readiness {
    status
    timestamp
    details
  }
}
```

### 12. User Registration (GraphQL)

```graphql
mutation RegisterUser($input: RegisterInput!, $clientInfo: ClientInfoInput) {
  register(input: $input, clientInfo: $clientInfo) {
    success
    message
    sessionId
    userId
    data {
      id
      username
      email
      createdAt
    }
  }
}
```

**Variables:**
```json
{
  "input": {
    "username": "graphqltest02",
    "email": "graphqltest02@example.com",
    "password": "testPassword123"
  },
  "clientInfo": {
    "ipAddress": "127.0.0.1",
    "userAgent": "curl/GraphQL-E2E-Test"
  }
}
```

### 13. User Login (GraphQL)

```graphql
mutation LoginUser($input: LoginInput!, $clientInfo: ClientInfoInput) {
  login(input: $input, clientInfo: $clientInfo) {
    success
    message
    sessionId
    userId
  }
}
```

**Variables:**
```json
{
  "input": {
    "username": "graphqltest02",
    "password": "testPassword123"
  },
  "clientInfo": {
    "ipAddress": "127.0.0.1",
    "userAgent": "curl/GraphQL-E2E-Test"
  }
}
```

### 14. Send Message (GraphQL)

**Headers Required:** `X-Session-Id`, `X-User-Id`

```graphql
mutation SendMessage($input: SendMessageInput!) {
  sendMessage(input: $input) {
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
```

**Variables:**
```json
{
  "input": {
    "receiverUsername": "testuser10",
    "content": "Test message sent via GraphQL!"
  }
}
```

### 15. Get Message History (GraphQL)

**Headers Required:** `X-Session-Id`, `X-User-Id`

```graphql
query GetMessageHistory($username: String!, $pagination: PaginationInput) {
  getMessageHistory(username: $username, pagination: $pagination) {
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

**Variables:**
```json
{
  "username": "testuser2",
  "pagination": {
    "page": 0,
    "size": 5
  }
}
```

### 16. Get Activity Logs (GraphQL)

**Headers Required:** `X-Session-Id`, `X-User-Id`

```graphql
query GetMyActivities {
  getMyActivityLogs(pagination: { page: 0, size: 10 }) {
    success
    message
    data {
      totalElements
      totalPages
      currentPage
      activities {
        id
        activityType
        description
        timestamp
        ipAddress
        userAgent
        metadata
      }
    }
  }
}
```

### 17. Logout (GraphQL)

**Headers Required:** `X-Session-Id`, `X-User-Id`

```graphql
mutation Logout {
  logout {
    success
    message
    data
  }
}
```

## 🔄 Complete User Flow

### 18. End-to-End Workflow

```bash
# 1. Register User
SESSION_DATA=$(curl -s -X POST http://localhost:4000/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "mutation RegisterUser($input: RegisterInput!, $clientInfo: ClientInfoInput) { register(input: $input, clientInfo: $clientInfo) { success message sessionId userId } }",
    "variables": {
      "input": {
        "username": "testflow",
        "email": "testflow@example.com",
        "password": "testPassword123"
      },
      "clientInfo": {
        "ipAddress": "127.0.0.1",
        "userAgent": "curl/E2E-Test"
      }
    }
  }')

# Extract sessionId and userId
SESSION_ID=$(echo $SESSION_DATA | jq -r '.data.register.sessionId')
USER_ID=$(echo $SESSION_DATA | jq -r '.data.register.userId')

# 2. Send Message
curl -X POST http://localhost:4000/graphql \
  -H "Content-Type: application/json" \
  -H "X-Session-Id: $SESSION_ID" \
  -H "X-User-Id: $USER_ID" \
  -d '{
    "query": "mutation SendMessage($input: SendMessageInput!) { sendMessage(input: $input) { success message } }",
    "variables": {
      "input": {
        "receiverUsername": "testuser10",
        "content": "E2E test message"
      }
    }
  }'

# 3. Get Activity Logs
curl -X POST http://localhost:4000/graphql \
  -H "Content-Type: application/json" \
  -H "X-Session-Id: $SESSION_ID" \
  -H "X-User-Id: $USER_ID" \
  -d '{
    "query": "query { getMyActivityLogs(pagination: { page: 0, size: 5 }) { success data { totalElements activities { activityType description timestamp } } } }"
  }'

# 4. Logout
curl -X POST http://localhost:4000/graphql \
  -H "Content-Type: application/json" \
  -H "X-Session-Id: $SESSION_ID" \
  -H "X-User-Id: $USER_ID" \
  -d '{
    "query": "mutation { logout { success message } }"
  }'
```

##  JavaScript/TypeScript Examples

### 19. Apollo Client Setup

```javascript
import { ApolloClient, InMemoryCache, gql, createHttpLink } from '@apollo/client';
import { setContext } from '@apollo/client/link/context';

// HTTP link
const httpLink = createHttpLink({
  uri: 'http://localhost:4000/graphql',
});

// Auth link for session headers
const authLink = setContext((_, { headers }) => {
  const sessionId = localStorage.getItem('sessionId');
  const userId = localStorage.getItem('userId');
  
  return {
    headers: {
      ...headers,
      'X-Session-Id': sessionId,
      'X-User-Id': userId,
    }
  };
});

// Create Apollo Client
const client = new ApolloClient({
  link: authLink.concat(httpLink),
  cache: new InMemoryCache(),
});

// Usage example
const SEND_MESSAGE = gql`
  mutation SendMessage($input: SendMessageInput!) {
    sendMessage(input: $input) {
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
`;

async function sendMessage(receiverUsername, content) {
  try {
    const { data } = await client.mutate({
      mutation: SEND_MESSAGE,
      variables: {
        input: { receiverUsername, content }
      }
    });
    return data.sendMessage;
  } catch (error) {
    console.error('Message send failed:', error);
    throw error;
  }
}
```

### 20. Fetch API Example

```javascript
async function graphqlRequest(query, variables = {}, headers = {}) {
  const response = await fetch('http://localhost:4000/graphql', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...headers
    },
    body: JSON.stringify({ query, variables })
  });
  
  return response.json();
}

// Register user
const registerResult = await graphqlRequest(`
  mutation RegisterUser($input: RegisterInput!, $clientInfo: ClientInfoInput) {
    register(input: $input, clientInfo: $clientInfo) {
      success
      message
      sessionId
      userId
    }
  }
`, {
  input: {
    username: 'jstest',
    email: 'jstest@example.com',
    password: 'testPassword123'
  },
  clientInfo: {
    ipAddress: '127.0.0.1',
    userAgent: navigator.userAgent
  }
});

// Send message with session headers
const messageResult = await graphqlRequest(`
  mutation SendMessage($input: SendMessageInput!) {
    sendMessage(input: $input) {
      success
      message
      data {
        threadId
        content
        timestamp
      }
    }
  }
`, {
  input: {
    receiverUsername: 'testuser10',
    content: 'Message from JavaScript!'
  }
}, {
  'X-Session-Id': registerResult.data.register.sessionId,
  'X-User-Id': registerResult.data.register.userId
});
```

## 🛠️ Schema Introspection

### 21. Get Schema Types

```graphql
query GetSchemaTypes {
  __schema {
    types {
      name
      kind
      description
      fields {
        name
        type {
          name
        }
      }
    }
  }
}
```

### 22. Get Query Fields

```graphql
query GetQueries {
  __schema {
    queryType {
      fields {
        name
        description
        args {
          name
          type {
            name
          }
        }
      }
    }
  }
}
```

### 23. Get Mutation Fields

```graphql
query GetMutations {
  __schema {
    mutationType {
      fields {
        name
        description
        args {
          name
          type {
            name
          }
        }
      }
    }
  }
}
```

## Error Examples

### 24. Authentication Error

```bash
# Missing headers - will fail
curl -X POST http://localhost:4000/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "query { getMyActivityLogs { success } }"
  }'
```

**Error Response:**
```json
{
  "errors": [
    {
      "message": "Authentication required",
      "extensions": {
        "code": "UNAUTHENTICATED"
      }
    }
  ]
}
```

### 25. Invalid Input Error

```bash
curl -X POST http://localhost:4000/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "mutation { register(input: { username: \"\", email: \"invalid\", password: \"123\" }) { success } }"
  }'
```

**Error Response:**
```json
{
  "errors": [
    {
      "message": "Validation error",
      "extensions": {
        "code": "BAD_USER_INPUT",
        "details": {
          "username": "Username cannot be empty",
          "email": "Invalid email format",
          "password": "Password must be at least 8 characters"
        }
      }
    }
  ]
}
```

## Environment Variables

```bash
# GraphQL Middleware Configuration
export REST_API_BASE_URL=http://localhost:8080
export REST_API_TIMEOUT=30000
export NODE_ENV=development
export LOG_LEVEL=info

# Start with custom config
REST_API_BASE_URL=http://localhost:8080 \
REST_API_TIMEOUT=30000 \
node index.js
```

## Testing Checklist

- [ ] ✅ Health checks work
- [ ] ✅ User registration works
- [ ] ✅ User login works  
- [ ] ✅ Message sending works
- [ ] ✅ Message history retrieval works
- [ ] ✅ Activity logs work
- [ ] ✅ User logout works
- [ ] ✅ Authentication headers work
- [ ] ✅ Error handling works
- [ ] ✅ Schema introspection works
