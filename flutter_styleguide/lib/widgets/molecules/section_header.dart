import 'package:flutter/material.dart';

class SectionHeader extends StatelessWidget {
  final String title;
  final VoidCallback? onViewAll;
  final String? viewAllText;

  const SectionHeader({
    super.key,
    required this.title,
    this.onViewAll,
    this.viewAllText = 'View All',
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: [
        Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              title,
              style: theme.textTheme.headlineSmall?.copyWith(fontSize: 18),
            ),
            const SizedBox(height: 4),
            Container(
              height: 2,
              width: 40,
              color: theme.colorScheme.primary,
            )
          ],
        ),
        if (onViewAll != null)
          InkWell(
            onTap: onViewAll,
            borderRadius: BorderRadius.circular(4),
            child: Padding(
              padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
              child: Row(
                children: [
                  Text(
                    viewAllText!,
                    style: TextStyle(
                      fontSize: 14,
                      color: theme.colorScheme.primary,
                      fontWeight: FontWeight.w500,
                    ),
                  ),
                  const SizedBox(width: 4),
                  Icon(
                    Icons.chevron_right,
                    size: 16,
                    color: theme.colorScheme.primary,
                  ),
                ],
              ),
            ),
          ),
      ],
    );
  }
} 