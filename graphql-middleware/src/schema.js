const { gql } = require('apollo-server-express');

const typeDefs = gql`
  # Basic types
  scalar DateTime

  # User types
  type User {
    id: ID!
    username: String!
    email: String
    createdAt: DateTime!
  }

  # Auth types
  type AuthResponse {
    success: Boolean!
    message: String!
    data: User
    sessionId: String
    userId: String
  }

  # Message types
  type Message {
    threadId: String!
    sender: String!
    content: String!
    timestamp: DateTime!
  }

  type MessageDto {
    id: ID!
    threadId: String!
    senderId: String!
    senderUsername: String!
    content: String!
    timestamp: DateTime!
  }

  type MessageResponse {
    success: Boolean!
    message: String!
    data: Message
  }

  type MessageHistoryResponse {
    success: Boolean!
    message: String!
    data: MessageHistory
  }

  type MessageHistory {
    data: [MessageDto!]!
    total: Int!
    limit: Int!
    offset: Int!
  }

  # Activity types
  type ActivityLog {
    id: ID!
    userId: String!
    activityType: String!
    description: String!
    ipAddress: String
    userAgent: String
    timestamp: DateTime!
    metadata: String
  }

  type ActivityResponse {
    success: Boolean!
    message: String!
    data: ActivityPage
  }

  type ActivityPage {
    activities: [ActivityLog!]!
    totalElements: Int!
    totalPages: Int!
    currentPage: Int!
    pageSize: Int!
  }

  # Health types
  type HealthStatus {
    status: String!
    timestamp: DateTime!
    details: String
  }

  # Input types
  "User registration input data"
  input RegisterInput {
    "Username for the new user"
    username: String!
    "Email address for the new user"
    email: String!
    "Password for the new user"
    password: String!
  }

  "User login input data"
  input LoginInput {
    "Username or email for login"
    username: String!
    "User password"
    password: String!
  }

  "Message sending input data"
  input SendMessageInput {
    "Username of the message recipient"
    receiverUsername: String!
    "Message content to send"
    content: String!
  }

  "Pagination parameters"
  input PaginationInput {
    "Page number (0-based)"
    page: Int = 0
    "Number of items per page"
    size: Int = 20
  }

  "Client information for tracking"
  input ClientInfoInput {
    "Client IP address"
    ipAddress: String
    "User agent string"
    userAgent: String
  }

  # Root types
  type Query {
    # Message queries
    getMessageHistory(
      username: String!
      pagination: PaginationInput
    ): MessageHistoryResponse!

    # Activity queries
    getMyActivityLogs(pagination: PaginationInput): ActivityResponse!

    # Health queries
    health: HealthStatus!
    readiness: HealthStatus!
  }

  type Mutation {
    # Auth mutations
    register(
      input: RegisterInput!
      clientInfo: ClientInfoInput
    ): AuthResponse!

    login(
      input: LoginInput!
      clientInfo: ClientInfoInput
    ): AuthResponse!

    logout: GenericResponse!

    # Message mutations
    sendMessage(input: SendMessageInput!): MessageResponse!
  }

  # Helper types
  input ClientInfoInput {
    ipAddress: String
    userAgent: String
  }

  type GenericResponse {
    success: Boolean!
    message: String!
    data: String
  }
`;

module.exports = typeDefs;
