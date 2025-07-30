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
  input RegisterInput {
    username: String!
    email: String!
    password: String!
  }

  input LoginInput {
    username: String!
    password: String!
  }

  input SendMessageInput {
    receiverUsername: String!
    content: String!
  }

  input PaginationInput {
    page: Int = 0
    size: Int = 20
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
