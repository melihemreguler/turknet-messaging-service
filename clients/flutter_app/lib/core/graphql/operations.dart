const String kRegisterMutation = r'''
mutation Register($input: RegisterInput!, $clientInfo: ClientInfoInput) {
  register(input: $input, clientInfo: $clientInfo) {
    success
    message
    sessionId
    userId
    data { id username }
  }
}
''';

const String kLoginMutation = r'''
mutation Login($input: LoginInput!, $clientInfo: ClientInfoInput) {
  login(input: $input, clientInfo: $clientInfo) {
    success
    message
    sessionId
    userId
  }
}
''';

const String kLogoutMutation = r'''
mutation Logout {
  logout { success message }
}
''';

const String kSendMessageMutation = r'''
mutation SendMessage($input: SendMessageInput!) {
  sendMessage(input: $input) {
    success
    message
    data { threadId content timestamp }
  }
}
''';

const String kGetInboxQuery = r'''
query GetInbox($pagination: PaginationInput) {
  getInbox(pagination: $pagination) {
    success
    message
    data {
      total
      data {
        threadId
        otherUserId
        otherUsername
        lastMessage {
          id
          threadId
          senderId
          senderUsername
          content
          timestamp
        }
      }
    }
  }
}
''';

const String kGetMessageHistoryQuery = r'''
query GetMessageHistory($username: String!, $pagination: PaginationInput) {
  getMessageHistory(username: $username, pagination: $pagination) {
    success
    message
    data {
      data { id threadId senderId senderUsername content timestamp }
      total
    }
  }
}
''';

const String kGetActivityLogsQuery = r'''
query GetMyActivities($pagination: PaginationInput) {
  getMyActivityLogs(pagination: $pagination) {
    success
    message
    data {
      totalElements
      activities { id activityType description timestamp }
    }
  }
}
''';
