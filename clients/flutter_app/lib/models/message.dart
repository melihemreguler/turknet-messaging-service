class Message {
  final String id;
  final String threadId;
  final String senderId;
  final String senderUsername;
  final String content;
  final DateTime timestamp;

  Message({
    required this.id,
    required this.threadId,
    required this.senderId,
    required this.senderUsername,
    required this.content,
    required this.timestamp,
  });

  factory Message.fromJson(Map<String, dynamic> json) => Message(
        id: json['id'] as String,
        threadId: (json['threadId'] ?? '') as String,
        senderId: (json['senderId'] ?? '') as String,
        senderUsername: (json['senderUsername'] ?? '') as String,
        content: (json['content'] ?? '') as String,
        timestamp: DateTime.parse(json['timestamp'] as String),
      );
}
