class Agent {
  final String id;
  final String name;
  final String description;
  final String status;
  final List<String> tags;
  final String? avatarUrl;
  final DateTime createdAt;
  final DateTime? lastActive;

  Agent({
    required this.id,
    required this.name,
    required this.description,
    required this.status,
    required this.tags,
    this.avatarUrl,
    required this.createdAt,
    this.lastActive,
  });

  // Factory method to create an Agent from JSON
  factory Agent.fromJson(Map<String, dynamic> json) {
    return Agent(
      id: json['id'] as String,
      name: json['name'] as String,
      description: json['description'] as String,
      status: json['status'] as String,
      tags: List<String>.from(json['tags']),
      avatarUrl: json['avatarUrl'] as String?,
      createdAt: DateTime.parse(json['createdAt'] as String),
      lastActive: json['lastActive'] != null
          ? DateTime.parse(json['lastActive'] as String)
          : null,
    );
  }

  // Convert Agent to JSON
  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'name': name,
      'description': description,
      'status': status,
      'tags': tags,
      'avatarUrl': avatarUrl,
      'createdAt': createdAt.toIso8601String(),
      'lastActive': lastActive?.toIso8601String(),
    };
  }
} 