import 'package:flutter/material.dart';

class ThemeSwitch extends StatelessWidget {
  final bool isDarkMode;
  final VoidCallback onToggle;

  const ThemeSwitch({
    Key? key,
    required this.isDarkMode,
    required this.onToggle,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return IconButton(
      icon: Stack(
        alignment: Alignment.center,
          children: [
          // Sun icon
          AnimatedOpacity(
            opacity: isDarkMode ? 0.0 : 1.0,
            duration: const Duration(milliseconds: 200),
            child: const Icon(
              Icons.light_mode,
              size: 20,
              ),
            ),
          // Moon icon
          AnimatedOpacity(
            opacity: isDarkMode ? 1.0 : 0.0,
            duration: const Duration(milliseconds: 200),
            child: const Icon(
              Icons.dark_mode,
              size: 20,
                ),
              ),
          ],
        ),
      onPressed: onToggle,
      tooltip: isDarkMode ? 'Switch to light mode' : 'Switch to dark mode',
      padding: const EdgeInsets.all(8),
    );
  }
} 