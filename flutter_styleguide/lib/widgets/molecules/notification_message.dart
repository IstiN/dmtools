import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:dmtools_styleguide/theme/app_theme.dart';
import '../../theme/app_colors.dart';

enum NotificationType { success, error, info, warning }

class NotificationMessage extends StatelessWidget {
  final String message;
  final NotificationType type;

  const NotificationMessage({
    Key? key,
    required this.message,
    required this.type,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final theme = Provider.of<ThemeProvider>(context);
    final colors = theme.isDarkMode ? AppColors.dark : AppColors.light;

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
      decoration: BoxDecoration(
        color: _getBackgroundColor(colors),
        borderRadius: BorderRadius.circular(8),
      ),
      child: Row(
        children: [
          Icon(_getIcon(), color: Colors.white),
          const SizedBox(width: 12),
          Text(message, style: const TextStyle(color: Colors.white)),
        ],
      ),
    );
  }

  Color _getBackgroundColor(ThemeColorSet colors) {
    switch (type) {
      case NotificationType.success:
        return colors.successColor;
      case NotificationType.error:
        return colors.dangerColor;
      case NotificationType.info:
        return colors.infoColor;
      case NotificationType.warning:
        return colors.warningColor;
    }
  }

  IconData _getIcon() {
    switch (type) {
      case NotificationType.success:
        return Icons.check_circle;
      case NotificationType.error:
        return Icons.cancel;
      case NotificationType.info:
        return Icons.info;
      case NotificationType.warning:
        return Icons.warning;
    }
  }
} 