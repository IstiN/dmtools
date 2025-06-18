import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../theme/app_theme.dart';
import '../../theme/app_colors.dart';

class ComponentDisplay extends StatelessWidget {
  final String title;
  final String? description;
  final Widget child;

  const ComponentDisplay({
    Key? key,
    required this.title,
    this.description,
    required this.child,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final themeProvider = Provider.of<ThemeProvider>(context);
    final isDarkMode = themeProvider.isDarkMode;
    final textTheme = Theme.of(context).textTheme;

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          title,
          style: textTheme.headlineSmall?.copyWith(color: isDarkMode ? AppColors.dark.textColor : AppColors.light.textColor),
        ),
        if (description != null) ...[
          const SizedBox(height: 8),
          Text(
            description!,
            style: textTheme.bodyMedium?.copyWith(color: isDarkMode ? AppColors.dark.textSecondary : AppColors.light.textSecondary),
          ),
        ],
        const SizedBox(height: 16),
        child,
      ],
    );
  }
} 