import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'app_colors.dart';

class ThemeProvider extends ChangeNotifier {
  bool _isDarkMode = false;

  bool get isDarkMode => _isDarkMode;

  ThemeMode get currentThemeMode => _isDarkMode ? ThemeMode.dark : ThemeMode.light;

  void toggleTheme() {
    _isDarkMode = !_isDarkMode;
    notifyListeners();
  }
}

class AppTheme {
  // Define the font family to match HTML styleguide
  static const String fontFamily = 'Inter';
  
  // Font families for fallback matching the HTML styleguide
  static const List<String> fontFamilyFallback = [
    'Segoe UI', 
    'Roboto', 
    'Helvetica Neue', 
    'Arial', 
    'sans-serif'
  ];

  static TextStyle _createTextStyle({
    required Color color,
    required double fontSize,
    FontWeight fontWeight = FontWeight.normal,
  }) {
    return GoogleFonts.inter(
      color: color,
      fontSize: fontSize,
      fontWeight: fontWeight,
    );
  }

  static ThemeData get lightTheme {
    final textTheme = TextTheme(
      displayLarge: _createTextStyle(
        color: AppColors.light.textColor,
        fontSize: 32,
        fontWeight: FontWeight.bold,
      ),
      displayMedium: _createTextStyle(
        color: AppColors.light.textColor,
        fontSize: 28,
        fontWeight: FontWeight.bold,
      ),
      displaySmall: _createTextStyle(
        color: AppColors.light.textColor,
        fontSize: 24,
        fontWeight: FontWeight.bold,
      ),
      headlineLarge: _createTextStyle(
        color: AppColors.light.textColor,
        fontSize: 22,
        fontWeight: FontWeight.bold,
      ),
      headlineMedium: _createTextStyle(
        color: AppColors.light.textColor,
        fontSize: 20,
        fontWeight: FontWeight.bold,
      ),
      headlineSmall: _createTextStyle(
        color: AppColors.light.textColor,
        fontSize: 18,
        fontWeight: FontWeight.bold,
      ),
      titleLarge: _createTextStyle(
        color: AppColors.light.textColor,
        fontSize: 16,
        fontWeight: FontWeight.bold,
      ),
      bodyLarge: _createTextStyle(
        color: AppColors.light.textColor,
        fontSize: 16,
      ),
      bodyMedium: _createTextStyle(
        color: AppColors.light.textColor,
        fontSize: 14,
      ),
      bodySmall: _createTextStyle(
        color: AppColors.light.textMuted,
        fontSize: 12,
      ),
    );

    return ThemeData(
      primarySwatch: Colors.blue,
      visualDensity: VisualDensity.adaptivePlatformDensity,
      scaffoldBackgroundColor: const Color(0xFFF8F9FA),
      textTheme: textTheme,
      colorScheme: ColorScheme.light(
        primary: AppColors.accentColor,
        secondary: AppColors.accentColor,
        surface: AppColors.lightCardBg,
        background: AppColors.light.bgColor,
      ),
      cardColor: AppColors.light.cardBg,
      appBarTheme: AppBarTheme(
        elevation: 0,
        backgroundColor: const Color(0xFFF8F9FA),
        foregroundColor: AppColors.light.textColor,
        surfaceTintColor: Colors.transparent,
        shape: Border(
          bottom: BorderSide(
            color: AppColors.light.borderColor,
            width: 1,
          ),
        ),
      ),
    );
  }

  static ThemeData get darkTheme {
    final textTheme = TextTheme(
      displayLarge: _createTextStyle(
        color: AppColors.dark.textColor,
        fontSize: 32,
        fontWeight: FontWeight.bold,
      ),
      displayMedium: _createTextStyle(
        color: AppColors.dark.textColor,
        fontSize: 28,
        fontWeight: FontWeight.bold,
      ),
      displaySmall: _createTextStyle(
        color: AppColors.dark.textColor,
        fontSize: 24,
        fontWeight: FontWeight.bold,
      ),
      headlineLarge: _createTextStyle(
        color: AppColors.dark.textColor,
        fontSize: 22,
        fontWeight: FontWeight.bold,
      ),
      headlineMedium: _createTextStyle(
        color: AppColors.dark.textColor,
        fontSize: 20,
        fontWeight: FontWeight.bold,
      ),
      headlineSmall: _createTextStyle(
        color: AppColors.dark.textColor,
        fontSize: 18,
        fontWeight: FontWeight.bold,
      ),
      titleLarge: _createTextStyle(
        color: AppColors.dark.textColor,
        fontSize: 16,
        fontWeight: FontWeight.bold,
      ),
      bodyLarge: _createTextStyle(
        color: AppColors.dark.textColor,
        fontSize: 16,
      ),
      bodyMedium: _createTextStyle(
        color: AppColors.dark.textColor,
        fontSize: 14,
      ),
      bodySmall: _createTextStyle(
        color: AppColors.dark.textMuted,
        fontSize: 12,
      ),
    );

    return ThemeData(
      primarySwatch: Colors.blue,
      visualDensity: VisualDensity.adaptivePlatformDensity,
      scaffoldBackgroundColor: AppColors.darkBgColor,
      textTheme: textTheme,
      colorScheme: ColorScheme.dark(
        primary: AppColors.accentColor,
        secondary: AppColors.accentColor,
        surface: AppColors.darkCardBg,
        background: AppColors.dark.bgColor,
      ),
      cardColor: AppColors.dark.cardBg,
      appBarTheme: AppBarTheme(
        elevation: 0,
        backgroundColor: const Color(0xFF2D2E30),
        foregroundColor: AppColors.dark.textColor,
        surfaceTintColor: Colors.transparent,
        shape: Border(
          bottom: BorderSide(
            color: AppColors.dark.borderColor,
            width: 1,
          ),
        ),
      ),
    );
  }
} 