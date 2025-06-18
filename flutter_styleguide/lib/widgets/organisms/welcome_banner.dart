import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../core/theme/app_theme.dart';
import '../../core/theme/app_colors.dart';
import '../atoms/app_button.dart';

class WelcomeBanner extends StatelessWidget {
  final String title;
  final String subtitle;
  final String description;
  final VoidCallback primaryAction;
  final VoidCallback secondaryAction;
  final String primaryActionText;
  final String secondaryActionText;
  final bool? isTestMode;
  final bool? testDarkMode;

  const WelcomeBanner({
    super.key,
    required this.title,
    required this.subtitle,
    required this.description,
    required this.primaryAction,
    required this.secondaryAction,
    this.primaryActionText = 'Get Started',
    this.secondaryActionText = 'Learn More',
    this.isTestMode,
    this.testDarkMode,
  });

  @override
  Widget build(BuildContext context) {
    final themeProvider = Provider.of<ThemeProvider>(context);
    final isDarkMode = themeProvider.isDarkMode;
    final colors = isDarkMode ? AppColors.dark : AppColors.light;
    
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(24),
      decoration: BoxDecoration(
        gradient: LinearGradient(
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
          colors: [
            colors.gradientStart,
            colors.gradientEnd,
          ],
        ),
        borderRadius: BorderRadius.circular(12),
        boxShadow: [
          BoxShadow(
            color: colors.cardShadow,
            blurRadius: 8,
            offset: const Offset(0, 4),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Icon
          Container(
            width: 56,
            height: 56,
            decoration: BoxDecoration(
              color: Colors.white.withOpacity(0.2),
              borderRadius: BorderRadius.circular(12),
            ),
            child: const Icon(
              Icons.auto_awesome,
              color: Colors.white,
              size: 32,
            ),
          ),
          
          const SizedBox(height: 24),
          
          // Title
          Text(
            title,
            style: const TextStyle(
              fontSize: 28,
              fontWeight: FontWeight.bold,
              color: Colors.white,
            ),
          ),
          
          const SizedBox(height: 8),
          
          // Subtitle
          Text(
            subtitle,
            style: const TextStyle(
              fontSize: 18,
              fontWeight: FontWeight.w500,
              color: Colors.white,
            ),
          ),
          
          const SizedBox(height: 16),
          
          // Description
          Text(
            description,
            style: const TextStyle(
              fontSize: 16,
              color: Colors.white,
              height: 1.5,
            ),
          ),
          
          const SizedBox(height: 24),
          
          // Actions
          Row(
            children: [
              AppButton(
                text: primaryActionText,
                onPressed: primaryAction,
                variant: ButtonVariant.primary,
                size: ButtonSize.large,
                isTestMode: isTestMode ?? false,
                testDarkMode: testDarkMode ?? false,
              ),
              const SizedBox(width: 16),
              AppButton(
                text: secondaryActionText,
                onPressed: secondaryAction,
                variant: ButtonVariant.outline,
                size: ButtonSize.large,
                isTestMode: isTestMode ?? false,
                testDarkMode: testDarkMode ?? false,
              ),
            ],
          ),
        ],
      ),
    );
  }
} 