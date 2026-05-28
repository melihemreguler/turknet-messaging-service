class Session {
  final String sessionId;
  final String userId;
  final String username;

  const Session({
    required this.sessionId,
    required this.userId,
    required this.username,
  });

  Map<String, String> toMap() => {
        'sessionId': sessionId,
        'userId': userId,
        'username': username,
      };

  static Session? fromMap(Map<String, String?> map) {
    final s = map['sessionId'];
    final u = map['userId'];
    final n = map['username'];
    if (s == null || u == null || n == null) return null;
    return Session(sessionId: s, userId: u, username: n);
  }
}
