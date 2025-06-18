import 'package:flutter/material.dart';
import 'package:dmtools_styleguide/widgets/molecules/custom_card.dart';
import 'package:dmtools_styleguide/widgets/atoms/tag_chip.dart';

class ApplicationItem extends StatelessWidget {
  final String title;
  final String version;
  final String description;
  final String category;
  final double rating;
  final int downloadCount;
  final VoidCallback onOpen;
  final IconData icon;

  const ApplicationItem({
    Key? key,
    this.title = 'Sample Application',
    this.version = 'v1.0.0',
    this.description = 'Sample application description that explains the functionality and purpose.',
    this.category = 'Productivity',
    this.rating = 4.8,
    this.downloadCount = 1200,
    required this.onOpen,
    this.icon = Icons.apps,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.textTheme;

    return CustomCard(
      padding: const EdgeInsets.all(16),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Icon(icon, size: 40, color: theme.colorScheme.primary),
          const SizedBox(width: 16),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(title, style: textTheme.titleLarge),
                const SizedBox(height: 4),
                Text(version, style: textTheme.bodySmall),
                const SizedBox(height: 8),
                Text(description, style: textTheme.bodyMedium, maxLines: 2, overflow: TextOverflow.ellipsis),
                const SizedBox(height: 12),
                Row(
                  children: [
                    TagChip(label: category),
                    const Spacer(),
                    Icon(Icons.star, size: 16, color: theme.colorScheme.secondary),
                    const SizedBox(width: 4),
                    Text(rating.toString(), style: textTheme.bodyMedium),
                    const SizedBox(width: 16),
                    Icon(Icons.download_outlined, size: 16, color: theme.hintColor),
                    const SizedBox(width: 4),
                    Text('${(downloadCount / 1000).toStringAsFixed(1)}k', style: textTheme.bodyMedium),
                  ],
                ),
              ],
            ),
          ),
          const SizedBox(width: 16),
          ElevatedButton(
            onPressed: onOpen,
            child: const Text('Open'),
          ),
        ],
      ),
    );
  }
} 