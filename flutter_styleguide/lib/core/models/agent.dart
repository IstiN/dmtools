class Agent {
  final String title;
  final bool isActive;
  final String description;
  final List<String> tags;
  final int runCount;
  final String lastRun;
  final String icon;

  const Agent({
    required this.title,
    required this.isActive,
    required this.description,
    required this.tags,
    required this.runCount,
    required this.lastRun,
    required this.icon,
  });
} 