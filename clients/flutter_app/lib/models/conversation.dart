import 'message.dart';

class Conversation {
  final String threadId;
  final String otherUserId;
  final String otherUsername;
  final Message? lastMessage;

  const Conversation({
    required this.threadId,
    required this.otherUserId,
    required this.otherUsername,
    required this.lastMessage,
  });

  DateTime? get lastTimestamp => lastMessage?.timestamp;

  factory Conversation.fromJson(Map<String, dynamic> json) {
    final last = json['lastMessage'] as Map<String, dynamic>?;
    return Conversation(
      threadId: (json['threadId'] ?? '') as String,
      otherUserId: (json['otherUserId'] ?? '') as String,
      otherUsername: (json['otherUsername'] ?? '') as String,
      lastMessage: last == null ? null : Message.fromJson(last),
    );
  }
}
