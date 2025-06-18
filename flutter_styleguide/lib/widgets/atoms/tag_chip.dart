import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../core/theme/app_theme.dart';
import '../../core/theme/app_colors.dart';

enum TagChipVariant {
  primary,
  secondary,
  success,
  warning,
  danger,
  info,
}

class TagChip extends StatelessWidget {
  final String label;
  final TagChipVariant variant;
  final bool isOutlined;
  final bool? isTestMode;
  final bool? testDarkMode;

  const TagChip({
    Key? key,
    required this.label,
    this.variant = TagChipVariant.primary,
    this.isOutlined = false,
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
    
    // Define colors based on variant
    Color bgColor;
    Color textColor;
    Color borderColor;
    
    switch (variant) {
      case TagChipVariant.primary:
        bgColor = colors.accentColor;
        textColor = Colors.white;
        borderColor = colors.accentColor;
        break;
      case TagChipVariant.secondary:
        bgColor = isDarkMode ? Color(0xFF2A2A2A) : Colors.grey.shade200;
        textColor = isDarkMode ? Colors.grey.shade300 : Colors.grey.shade700;
        borderColor = isDarkMode ? Colors.grey.shade700 : Colors.grey.shade300;
        break;
      case TagChipVariant.success:
        bgColor = colors.successColor;
        textColor = Colors.white;
        borderColor = colors.successColor;
        break;
      case TagChipVariant.warning:
        bgColor = colors.warningColor;
        textColor = Colors.white;
        borderColor = colors.warningColor;
        break;
      case TagChipVariant.danger:
        bgColor = colors.dangerColor;
        textColor = Colors.white;
        borderColor = colors.dangerColor;
        break;
      case TagChipVariant.info:
        bgColor = colors.infoColor;
        textColor = Colors.white;
        borderColor = colors.infoColor;
        break;
    }
    
    // Apply outlined style if needed
    if (isOutlined) {
      textColor = bgColor;
      bgColor = Colors.transparent;
    }

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
      decoration: BoxDecoration(
        color: bgColor,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: borderColor),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Text(
            label,
            style: TextStyle(
              color: textColor,
              fontSize: 12,
              fontWeight: FontWeight.w500,
            ),
          ),
        ],
      ),
    );
  }
} 