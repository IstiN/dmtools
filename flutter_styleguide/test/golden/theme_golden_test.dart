import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:golden_toolkit/golden_toolkit.dart';
import 'package:dmtools_styleguide/theme/app_colors.dart';
import 'package:dmtools_styleguide/theme/app_theme.dart';
import '../golden_test_helper.dart';

void main() {
  setUpAll(() async {
    await loadAppFonts();
  });

  group('Theme Golden Tests', () {
    testGoldens('Light Theme Colors', (WidgetTester tester) async {
      final colorWidgets = <Widget>[];

      // Base colors
      colorWidgets.add(const Text('Base Colors', style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold)));
      colorWidgets.add(const SizedBox(height: 16));
      colorWidgets.add(_buildColorGrid({
        'bgColor': AppColors.lightBgColor,
        'cardBg': AppColors.lightCardBg,
        'textColor': AppColors.lightTextColor,
        'textSecondary': AppColors.lightTextSecondary,
        'textMuted': AppColors.lightTextMuted,
        'borderColor': AppColors.lightBorderColor,
      }));

      // Accent colors
      colorWidgets.add(const SizedBox(height: 24));
      colorWidgets.add(const Text('Accent Colors', style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold)));
      colorWidgets.add(const SizedBox(height: 16));
      colorWidgets.add(_buildColorGrid({
        'accentColor': AppColors.accentColor,
        'accentLight': AppColors.accentLight,
        'accentHover': AppColors.accentHover,
      }));

      // Button colors
      colorWidgets.add(const SizedBox(height: 24));
      colorWidgets.add(const Text('Button Colors', style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold)));
      colorWidgets.add(const SizedBox(height: 16));
      colorWidgets.add(_buildColorGrid({
        'buttonBg': AppColors.buttonBg,
        'buttonHover': AppColors.buttonHover,
        'hoverBg': AppColors.hoverBg,
      }));

      // Feedback colors
      colorWidgets.add(const SizedBox(height: 24));
      colorWidgets.add(const Text('Feedback Colors', style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold)));
      colorWidgets.add(const SizedBox(height: 16));
      colorWidgets.add(_buildColorGrid({
        'successColor': AppColors.successColor,
        'warningColor': AppColors.warningColor,
        'dangerColor': AppColors.dangerColor,
        'infoColor': AppColors.infoColor,
      }));

      // Input colors
      colorWidgets.add(const SizedBox(height: 24));
      colorWidgets.add(const Text('Input Colors', style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold)));
      colorWidgets.add(const SizedBox(height: 16));
      colorWidgets.add(_buildColorGrid({
        'inputBg': AppColors.inputBg,
        'inputFocusBorder': AppColors.inputFocusBorder,
      }));

      final builder = GoldenTestHelper.createDeviceBuilder(
        widgets: colorWidgets,
        name: 'Light Theme Colors',
        isDarkMode: false,
      );

      await tester.pumpDeviceBuilder(builder);
      await screenMatchesGolden(tester, 'light_theme_colors');
    });

    testGoldens('Dark Theme Colors', (WidgetTester tester) async {
      final colorWidgets = <Widget>[];

      // Base colors
      colorWidgets.add(const Text('Base Colors', style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold)));
      colorWidgets.add(const SizedBox(height: 16));
      colorWidgets.add(_buildColorGrid({
        'bgColor': AppColors.darkBgColor,
        'cardBg': AppColors.darkCardBg,
        'textColor': AppColors.darkTextColor,
        'textSecondary': AppColors.darkTextSecondary,
        'textMuted': AppColors.darkTextMuted,
        'borderColor': AppColors.darkBorderColor,
      }));

      // Accent colors
      colorWidgets.add(const SizedBox(height: 24));
      colorWidgets.add(const Text('Accent Colors', style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold)));
      colorWidgets.add(const SizedBox(height: 16));
      colorWidgets.add(_buildColorGrid({
        'accentColor': AppColors.accentColor,
        'accentLight': AppColors.accentLight,
        'accentHover': AppColors.accentHover,
      }));

      // Button colors
      colorWidgets.add(const SizedBox(height: 24));
      colorWidgets.add(const Text('Button Colors', style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold)));
      colorWidgets.add(const SizedBox(height: 16));
      colorWidgets.add(_buildColorGrid({
        'buttonBg': AppColors.buttonBg,
        'buttonHover': AppColors.buttonHover,
        'hoverBg': AppColors.hoverBg,
      }));

      // Feedback colors
      colorWidgets.add(const SizedBox(height: 24));
      colorWidgets.add(const Text('Feedback Colors', style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold)));
      colorWidgets.add(const SizedBox(height: 16));
      colorWidgets.add(_buildColorGrid({
        'successColor': AppColors.successColor,
        'warningColor': AppColors.warningColor,
        'dangerColor': AppColors.dangerColor,
        'infoColor': AppColors.infoColor,
      }));

      // Input colors
      colorWidgets.add(const SizedBox(height: 24));
      colorWidgets.add(const Text('Input Colors', style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold)));
      colorWidgets.add(const SizedBox(height: 16));
      colorWidgets.add(_buildColorGrid({
        'inputBg': AppColors.darkCardBg,  // Dark theme uses card background for inputs
        'inputFocusBorder': AppColors.inputFocusBorder,
      }));

      final builder = GoldenTestHelper.createDeviceBuilder(
        widgets: colorWidgets,
        name: 'Dark Theme Colors',
        isDarkMode: true,
      );

      await tester.pumpDeviceBuilder(builder);
      await screenMatchesGolden(tester, 'dark_theme_colors');
    });

    testGoldens('Typography', (WidgetTester tester) async {
      final theme = AppTheme.lightTheme;
      
      final typographyWidgets = <Widget>[
        const Text('Typography', style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold)),
        const SizedBox(height: 24),
        
        Text('Headline 1', style: theme.textTheme.displayLarge),
        const SizedBox(height: 8),
        
        Text('Headline 2', style: theme.textTheme.displayMedium),
        const SizedBox(height: 8),
        
        Text('Headline 3', style: theme.textTheme.displaySmall),
        const SizedBox(height: 8),
        
        Text('Headline 4', style: theme.textTheme.headlineMedium),
        const SizedBox(height: 8),
        
        Text('Headline 5', style: theme.textTheme.headlineSmall),
        const SizedBox(height: 8),
        
        Text('Headline 6', style: theme.textTheme.titleLarge),
        const SizedBox(height: 8),
        
        Text('Subtitle 1', style: theme.textTheme.titleMedium),
        const SizedBox(height: 8),
        
        Text('Subtitle 2', style: theme.textTheme.titleSmall),
        const SizedBox(height: 8),
        
        Text('Body 1', style: theme.textTheme.bodyLarge),
        const SizedBox(height: 8),
        
        Text('Body 2', style: theme.textTheme.bodyMedium),
        const SizedBox(height: 8),
        
        Text('Caption', style: theme.textTheme.bodySmall),
        const SizedBox(height: 8),
      ];

      final builder = GoldenTestHelper.createDeviceBuilder(
        widgets: typographyWidgets,
        name: 'Typography',
        isDarkMode: false,
      );

      await tester.pumpDeviceBuilder(builder);
      await screenMatchesGolden(tester, 'typography_light');
      
      // Dark theme typography
      final darkTheme = AppTheme.darkTheme;
      
      final darkTypographyWidgets = <Widget>[
        const Text('Typography (Dark)', style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold)),
        const SizedBox(height: 24),
        
        Text('Headline 1', style: darkTheme.textTheme.displayLarge),
        const SizedBox(height: 8),
        
        Text('Headline 2', style: darkTheme.textTheme.displayMedium),
        const SizedBox(height: 8),
        
        Text('Headline 3', style: darkTheme.textTheme.displaySmall),
        const SizedBox(height: 8),
        
        Text('Headline 4', style: darkTheme.textTheme.headlineMedium),
        const SizedBox(height: 8),
        
        Text('Headline 5', style: darkTheme.textTheme.headlineSmall),
        const SizedBox(height: 8),
        
        Text('Headline 6', style: darkTheme.textTheme.titleLarge),
        const SizedBox(height: 8),
        
        Text('Subtitle 1', style: darkTheme.textTheme.titleMedium),
        const SizedBox(height: 8),
        
        Text('Subtitle 2', style: darkTheme.textTheme.titleSmall),
        const SizedBox(height: 8),
        
        Text('Body 1', style: darkTheme.textTheme.bodyLarge),
        const SizedBox(height: 8),
        
        Text('Body 2', style: darkTheme.textTheme.bodyMedium),
        const SizedBox(height: 8),
        
        Text('Caption', style: darkTheme.textTheme.bodySmall),
        const SizedBox(height: 8),
      ];

      final darkBuilder = GoldenTestHelper.createDeviceBuilder(
        widgets: darkTypographyWidgets,
        name: 'Typography Dark',
        isDarkMode: true,
      );

      await tester.pumpDeviceBuilder(darkBuilder);
      await screenMatchesGolden(tester, 'typography_dark');
    });
  });
}

Widget _buildColorGrid(Map<String, Color> colors) {
  return Wrap(
    spacing: 16,
    runSpacing: 16,
    children: colors.entries.map((entry) {
      return Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Container(
            width: 120,
            height: 80,
            decoration: BoxDecoration(
              color: entry.value,
              borderRadius: BorderRadius.circular(8),
              border: Border.all(color: Colors.grey.withOpacity(0.3)),
            ),
          ),
          const SizedBox(height: 4),
          Text(entry.key, style: const TextStyle(fontWeight: FontWeight.w500)),
          Text(
            '#${entry.value.value.toRadixString(16).substring(2).toUpperCase()}',
            style: const TextStyle(fontSize: 12),
          ),
        ],
      );
    }).toList(),
  );
} 