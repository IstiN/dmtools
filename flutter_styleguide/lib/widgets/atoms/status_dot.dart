import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../core/theme/app_theme.dart';
import '../../core/theme/app_colors.dart';

enum StatusType {
  online,
  offline,
  warning,
  error,
}

class StatusDot extends StatelessWidget {
  final StatusType status;
  final double size;
  final String? label;
  final bool showLabel;
  final bool? isTestMode;
  final bool? testDarkMode;

  const StatusDot({
    Key? key,
    required this.status,
    this.size = 10,
    this.label,
    this.showLabel = false,
    this.isTestMode = false,
    this.testDarkMode = false,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    bool isDarkMode;
    AppColorScheme colors;
    
    if (isTestMode == true) {
      isDarkMode = testDarkMode ?? false;
      colors = isDarkMode ? AppColors.dark : AppColors.light;
    } else {
      try {
        final themeProvider = Provider.of<ThemeProvider>(context);
        isDarkMode = themeProvider.isDarkMode;
        colors = isDarkMode ? AppColors.dark : AppColors.light;
      } catch (e) {
        // Fallback for tests
        isDarkMode = false;
        colors = AppColors.light;
      }
    }
    
    Color statusColor;
    String statusText = label ?? '';
    
    switch (status) {
      case StatusType.online:
        statusColor = colors.successColor;
        statusText = label ?? 'Online';
        break;
      case StatusType.offline:
        statusColor = isDarkMode ? Colors.grey.shade600 : Colors.grey.shade500;
        statusText = label ?? 'Offline';
        break;
      case StatusType.warning:
        statusColor = colors.warningColor;
        statusText = label ?? 'Warning';
        break;
      case StatusType.error:
        statusColor = colors.dangerColor;
        statusText = label ?? 'Error';
        break;
    }

    return Row(
      mainAxisSize: MainAxisSize.min,
      children: [
        Container(
          width: size,
          height: size,
          decoration: BoxDecoration(
            color: statusColor,
            shape: BoxShape.circle,
            boxShadow: [
              BoxShadow(
                color: statusColor.withOpacity(0.4),
                blurRadius: 4,
                spreadRadius: 1,
              ),
            ],
          ),
        ),
        if (showLabel) ...[
          const SizedBox(width: 6),
          Text(
            statusText,
            style: TextStyle(
              fontSize: size * 1.2,
              fontWeight: FontWeight.w500,
              color: isDarkMode ? statusColor : statusColor.withOpacity(0.9),
            ),
          ),
        ],
      ],
    );
  }
} 