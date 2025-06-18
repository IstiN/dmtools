import 'package:flutter/material.dart';

class TagChip extends StatelessWidget {
  final String label;

  const TagChip({
    super.key,
    required this.label,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final bool isDark = theme.brightness == Brightness.dark;
    
    return Chip(
      label: Text(label),
      backgroundColor: isDark ? Colors.white.withOpacity(0.1) : Colors.black.withOpacity(0.05),
      labelStyle: TextStyle(
        color: theme.textTheme.bodyLarge?.color,
        fontWeight: FontWeight.w500,
        fontSize: 12,
      ),
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(16),
        side: BorderSide(color: Colors.transparent),
      ),
    );
  }
} 