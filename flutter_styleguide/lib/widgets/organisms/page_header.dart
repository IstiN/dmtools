import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../core/theme/app_theme.dart';
import '../../core/theme/app_colors.dart';
import '../styleguide/theme_switch.dart';

class PageHeader extends StatelessWidget {
  final String title;
  final VoidCallback onThemeToggle;
  final List<Widget> actions;
  final bool? isTestMode;
  final bool? testDarkMode;

  const PageHeader({
    super.key,
    required this.title,
    required this.onThemeToggle,
    this.actions = const [],
    this.isTestMode = false,
    this.testDarkMode = false,
  });

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
    
    return Container(
      width: double.infinity,
      height: 64,
      color: colors.accentColor,
      padding: const EdgeInsets.symmetric(horizontal: 16),
      child: Row(
        children: [
          // Logo/Title
          Expanded(
            child: Row(
              children: [
                Icon(
                  Icons.auto_awesome,
                  color: Colors.white,
                  size: 24,
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Text(
                    title,
                    style: const TextStyle(
                      fontSize: 18,
                      fontWeight: FontWeight.bold,
                      color: Colors.white,
                    ),
                    overflow: TextOverflow.ellipsis,
                  ),
                ),
              ],
            ),
          ),
          
          const Spacer(),
          
          // Theme toggle
          ThemeSwitch(
            isDarkMode: isDarkMode,
            onToggle: onThemeToggle,
          ),
          
          // Actions
          if (actions.isNotEmpty) ...[
            const SizedBox(width: 8),
            ...actions,
          ],
        ],
      ),
    );
  }
} 