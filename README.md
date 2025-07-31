# Turknet Messaging Service

![Java](https://img.shields.io/badge/Java-17-orange) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.4-green) ![Node.js](https://img.shields.io/badge/Node.js-18+-green) ![GraphQL](https://img.shields.io/badge/GraphQL-16.8.1-pink) ![MongoDB](https://img.shields.io/badge/MongoDB-8.0.12-green) ![Kafka](https://img.shields.io/badge/Apache%20Kafka-Latest-orange) ![Elasticsearch](https://img.shields.io/badge/Elasticsearch-8.11.0-yellow) ![Docker](https://img.shields.io/badge/Docker-Ready-blue)

**Event-driven, scalable messaging service** - A modern microservice architecture where users can securely communicate with each other, with comprehensive tracking of all activities and system logs.

## Project Overview

This project is an enterprise-level messaging system built using **Event-Driven Architecture (EDA)** and **CQRS** patterns, supported by a **Backend-for-Frontend (BFF)** approach. The system provides real-time user activity tracking, scalable log management, and high-performance message processing capabilities.

### Key Features

- **Secure Session Management**: SessionId + UserId based authentication
- **Comprehensive Activity Tracking**: Detailed logging of all user operations
- **Event-Driven Architecture**: Kafka-based asynchronous processing flow
- **Scalable Architecture**: Microservice-based loosely coupled structure
- **Advanced Monitoring**: Real-time log analysis with Elasticsearch + Kibana
- **GraphQL BFF**: Unified API layer for frontend applications
- **Docker Ready**: Full containerized deployment support

## System Architecture

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────────┐
│   Frontend      │    │  GraphQL         │    │  Spring Boot        │
│   Application   │◄──►│  Middleware      │◄──►│  Command Service    │
│                 │    │  (Node.js BFF)   │    │  (REST API)         │
└─────────────────┘    └──────────────────┘    └─────────────────────┘
                                                           │
                                                           ▼
                       ┌─────────────────────────────────────────────────┐
                       │              Apache Kafka                       │
                       │  ┌─────────────────────────────────────────┐   │
                       │  │          Event Topics                  │   │
                       │  │  • user-commands                      │   │
                       │  │  • message-commands                   │   │
                       │  │  • session-commands                   │   │
                       │  │  • retry topics                       │   │
                       │  └─────────────────────────────────────────┘   │
                       └─────────────────────────────────────────────────┘
                                                           │
                                                           ▼
       ┌──────────────────┐    ┌─────────────────────────────────────┐
       │    MongoDB       │◄──►│     Messaging Consumer              │
       │                  │    │     (Event Processor)               │
       │  • users         │    │                                     │
       │  • messages      │    │  • Strategy Pattern                 │
       │  • sessions      │    │  • Retry Logic                      │
       │  • activities    │    │  • Error Handling                   │
       └──────────────────┘    └─────────────────────────────────────┘
                                                           │
                                                           ▼
                               ┌─────────────────────────────────────┐
                               │         Elasticsearch              │
                               │         + Kibana                   │
                               │                                    │
                               │  • Application Logs                │
                               │  • Error Tracking                  │
                               │  • Performance Metrics            │
                               │  • Real-time Monitoring            │
                               └─────────────────────────────────────┘
```

## Project Structure

```
turknet-messaging-service/
├── messaging-command-service/     # REST API Servisi (Spring Boot)
│   ├── src/main/java/com/github/melihemreguler/turknetmessagingservice/
│   │   ├── controller/           # REST Controllers
│   │   ├── service/              # Business Logic
│   │   ├── model/                # Entity & DTO Classes
│   │   ├── config/               # Configuration Classes
│   │   └── interceptor/          # Session Interceptor
│   ├── src/main/resources/
│   │   ├── application.yaml      # Multi-profile Configuration
│   │   └── logback-spring.xml    # Logging Configuration
│   └── pom.xml                   # Maven Dependencies
│
├── messaging-consumer/            # Kafka Event Consumer (Spring Boot)
│   ├── src/main/java/com/github/melihemreguler/messagingconsumer/
│   │   ├── consumer/             # Kafka Consumers
│   │   ├── strategy/             # Command Strategy Pattern
│   │   ├── service/              # Event Processing Logic
│   │   └── exception/            # Exception Handling
│   ├── src/main/resources/
│   │   ├── application.yaml      # Consumer Configuration
│   │   └── logback-spring.xml    # Logging Configuration
│   └── pom.xml
│
├── graphql-middleware/            # GraphQL BFF (Node.js)
│   ├── src/
│   │   ├── server.js             # GraphQL Server
│   │   ├── schema.js             # GraphQL Schema
│   │   ├── resolvers/            # GraphQL Resolvers
│   │   │   ├── authResolvers.js
│   │   │   ├── messageResolvers.js
│   │   │   └── activityResolvers.js
│   │   ├── restApiClient.js      # REST API Integration
│   │   └── logger.js             # Winston Logger
│   ├── tests/                    # Unit Tests
│   ├── package.json
│   └── Dockerfile
│
├── docker-compose.yml             # Multi-service Docker Setup
└── README.md
```

## Quick Start

### Prerequisites

- **Java 17+** (OpenJDK recommended)
- **Node.js 18+** & npm
- **Docker** & Docker Compose
- **Maven 3.8+**

### 1. Full System Setup with Docker

```bash
# Clone the repository
git clone https://github.com/melihemreguler/turknet-messaging-service.git
cd turknet-messaging-service

# Build Spring Boot Docker images with Jib
cd messaging-command-service
mvn clean compile
mvn jib:dockerBuild

cd ../messaging-consumer
mvn clean compile
mvn jib:dockerBuild

# Return to root directory
cd ..

# Build and start all services
docker-compose up -d --build

# Check service status
docker-compose ps
```

### 2. Local Development Setup

#### Backend Services (Spring Boot)

```bash
# Start infrastructure first
docker-compose up -d mongodb kafka elasticsearch kibana

# Build Docker images for Spring Boot services (optional for local development)
cd messaging-command-service
mvn clean compile
mvn jib:dockerBuild

cd ../messaging-consumer
mvn clean compile
mvn jib:dockerBuild

# Return to root directory
cd ..

# Start Command Service locally
cd messaging-command-service
./mvnw spring-boot:run -Dspring-boot.run.profiles=local

# Start Consumer Service (new terminal)
cd messaging-consumer
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

#### GraphQL Middleware (Node.js)

```bash
cd graphql-middleware

# Install dependencies
npm install

# Configure environment variables
cp .env.example .env
# Edit .env file with your configuration settings

# Start in development mode
npm run dev
```

### 3. Service Access Points

| Service | URL | Description |
|---------|-----|-------------|
| **GraphQL Playground** | http://localhost:4000/graphql | GraphQL API Explorer |
| **REST API Swagger** | http://localhost:8080/api/swagger-ui/index.html | REST API Documentation |
| **Consumer Health** | http://localhost:8081/actuator/health | Consumer Service Status |
| **Kibana Dashboard** | http://localhost:5601 | Log Analysis & Monitoring |
| **Kafka UI** | http://localhost:8082 | Kafka Topic Management |

## Configuration

### Profile Management

The system works with 4 different profiles:

- **`local`**: Development environment (localhost MongoDB, Kafka)
- **`docker`**: Docker Compose environment
- **`staging`**: Staging environment (separate topics)
- **`prod`**: Production environment (optimized logging)

### Kafka Topic Configuration

```yaml
app:
  kafka:
    topics:
      user-commands: turknet.transformers.user.commands.0
      user-commands-retry: turknet.transformers.user.commands.retry.0
      message-commands: turknet.transformers.message.commands.0
      message-commands-retry: turknet.transformers.message.commands.retry.0
      session-commands: turknet.transformers.session.commands.0
      session-commands-retry: turknet.transformers.session.commands.retry.0
```
### Session Management

```yaml
app:
  session:
    expiration-hours: 24        # Session duration
    cleanup-interval-minutes: 60   # Cleanup interval
```

## API Usage

### GraphQL API (Recommended)

#### User Registration
```graphql
mutation RegisterUser($input: RegisterInput!, $clientInfo: ClientInfoInput) {
  register(input: $input, clientInfo: $clientInfo) {
    success
    message
    data {
      id
      username
      email
    }
    sessionId
    userId
  }
}
```

#### Send Message
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

#### Query Message History
```graphql
query GetMessageHistory($username: String!, $pagination: PaginationInput) {
  getMessageHistory(username: $username, pagination: $pagination) {
    success
    data {
      data {
        id
        content
        senderUsername
        timestamp
      }
      total
      limit
      offset
    }
  }
}
```

### REST API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/auth/register` | User registration |
| `POST` | `/api/auth/login` | User login |
| `POST` | `/api/auth/logout` | Logout |
| `POST` | `/api/messages/send` | Send message |
| `GET` | `/api/messages/history` | Message history |
| `GET` | `/api/activities/logs` | Activity logs |

## Database Structure

### MongoDB Collections

```javascript
// users collection
{
  "_id": "ObjectId",
  "username": "string",
  "email": "string", 
  "passwordHash": "string",
  "createdAt": "Date",
  "updatedAt": "Date"
  // Additional fields can be added as needed
  // "isActive": "boolean"
}

// messages collection  
{
  "_id": "ObjectId",
  "threadId": "string",      // "userId1-userId2" format
  "senderId": "string",
  "senderUsername": "string",
  "content": "string",
  "timestamp": "Date",
  "status": "string", // "sent" ("delivered", "read" could be added later)
}

// sessions collection
{
  "_id": "ObjectId", 
  "hashedSessionId": "string",
  "userId": "string",
  "expiresAt": "Date",
  "createdAt": "Date",
  "lastAccessedAt": "Date",
  "ipAddress": "string",
  "userAgent": "string",
  "isActive": "boolean"
}

// activity_logs collection
{
  "_id": "ObjectId",
  "userId": "string",
  "logs": [
    {
      "successful": "boolean",
      "ipAddress": "string",
      "userAgent": "string",
      "timestamp": "Date",
      "action": "string"
    }
  ]
}
```

## Testing Structure

### Unit Test Coverage

```bash
# For Java Services
./mvnw test

# For GraphQL Middleware  
cd graphql-middleware
npm test

# Coverage report
npm run test:coverage
```

### Test Scenarios

- ✅ Authentication & Authorization Tests
- ✅ Message Processing Tests  
- ✅ Session Management Tests
- ✅ Kafka Event Processing Tests
- ✅ GraphQL Resolver Tests
- ✅ Error Handling Tests

## Monitoring & Logging

### Elasticsearch Log Indices

- **`turknet-messaging-logs`**: Spring Boot services logs
- **`turknet-messaging-graphql-logs`**: GraphQL middleware logs

### Kibana Dashboard Examples

1. **User Activity Tracking**: Real-time tracking of user activities
2. **Message Flow Analysis**: Message flow and performance metrics  
3. **Error Rate Monitoring**: Error rates and trend analysis
4. **Session Analytics**: Session durations and usage statistics

### Log Levels

| Environment | Spring Boot | GraphQL | Kafka |
|-------------|-------------|---------|--------|
| **Local** | DEBUG | INFO | INFO |
| **Docker** | INFO | INFO | WARN |
| **Staging** | INFO | INFO | WARN |
| **Production** | WARN | WARN | ERROR |

## Security

### Authentication Flow

1. User performs login/register operation
2. System generates SessionId and UserId
3. These credentials are carried in headers:
   - `X-Session-Id`: Session identifier
   - `X-User-Id`: User identifier
4. SessionInterceptor validates every request
5. Session status is checked in MongoDB

### Session Management

- **Expiration**: 24 hours (configurable)
- **Cleanup**: Automatic cleanup every 60 minutes
- **Multi-replica Support**: MongoDB-based fail-safe session storage

## Deployment

### Production Deployment

```bash
# Build Spring Boot Docker images with production profile
cd messaging-command-service
mvn clean compile -Pprod
mvn jib:dockerBuild

cd ../messaging-consumer
mvn clean compile -Pprod
mvn jib:dockerBuild

# Return to root directory and deploy
cd ..

# Build with production profile
docker-compose -f docker-compose.prod.yml up -d --build

# Environment variables
export SPRING_PROFILES_ACTIVE=prod
export KAFKA_BOOTSTRAP_SERVERS=prod-kafka-cluster:9092
export MONGODB_URI=mongodb://admin:password@mongodb:27017/turknet-messaging-db
```

## Event Flow Diagram

```
User Action → Command Service → MongoDB (Immediate Response) 
                ↓
            Kafka Event → Consumer Service → Process Event
                ↓                              ↓
        Retry Logic                      Update MongoDB
                ↓                              ↓  
        Retry Topics                  Log to Elasticsearch
                ↓
        Log to Elasticsearch
```

## Development Guide

### Code Structure Standards

- **Java**: Clean Architecture + SOLID principles
- **Node.js**: Functional programming + async/await patterns  
- **Error Handling**: Comprehensive exception hierarchy
- **Logging**: Structured logging with Winston (Node.js) and Logback (Java)

### Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/amazing-feature`)  
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open Pull Request

## Changelog

### v1.0.0 (Current)
- ✅ Initial release
- ✅ Complete microservice architecture
- ✅ GraphQL BFF implementation
- ✅ Comprehensive monitoring setup
- ✅ Multi-environment support


## Support & Contact

- **GitHub Issues**: [Project Issues](https://github.com/melihemreguler/turknet-messaging-service/issues)
- **Documentation**: This README and in-code documentation
- **Developer**: [Melih Emre Güler](https://melihemre.dev)
