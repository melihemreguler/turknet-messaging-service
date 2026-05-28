class Activity {
  final String id;
  final String activityType;
  final String description;
  final DateTime timestamp;

  Activity({
    required this.id,
    required this.activityType,
    required this.description,
    required this.timestamp,
  });

  factory Activity.fromJson(Map<String, dynamic> json) => Activity(
        id: (json['id'] ?? '') as String,
        activityType: (json['activityType'] ?? '') as String,
        description: (json['description'] ?? '') as String,
        timestamp: DateTime.parse(json['timestamp'] as String),
      );
}
