import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'app_colors.dart';

class AppTheme {
  static final ThemeData lightTheme = _buildLightTheme();
  static final ThemeData darkTheme = _buildDarkTheme();

  static ThemeData _buildLightTheme() {
    final ThemeData base = ThemeData.light();
    final TextTheme textTheme = _buildTextTheme(
      base: Typography.blackMountainView, 
      color: AppColors.light.textColor, 
      secondaryColor: AppColors.light.textSecondary,
    );
    
    return base.copyWith(
      brightness: Brightness.light,
      primaryColor: AppColors.light.accentColor,
      scaffoldBackgroundColor: AppColors.light.bgColor,
      cardColor: AppColors.light.cardBg,
      dividerColor: AppColors.light.borderColor,
      hintColor: AppColors.light.textMuted,
      
      colorScheme: ColorScheme.light(
        primary: AppColors.light.accentColor,
        secondary: AppColors.light.accentLight,
        surface: AppColors.light.cardBg,
        background: AppColors.light.bgColor,
        error: AppColors.light.dangerColor,
        onPrimary: Colors.white,
        onSecondary: Colors.white,
        onSurface: AppColors.light.textColor,
        onBackground: AppColors.light.textColor,
        onError: Colors.white,
      ),

      textTheme: GoogleFonts.interTextTheme(textTheme),

      elevatedButtonTheme: ElevatedButtonThemeData(
        style: ButtonStyle(
          backgroundColor: MaterialStateProperty.resolveWith<Color>((states) {
            if (states.contains(MaterialState.disabled)) {
              return AppColors.light.buttonBg.withOpacity(0.5);
            }
            if (states.contains(MaterialState.hovered)) {
              return AppColors.light.buttonHover;
            }
            return AppColors.light.buttonBg;
          }),
          foregroundColor: MaterialStateProperty.all(Colors.white),
          padding: MaterialStateProperty.all(
            const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
          ),
          shape: MaterialStateProperty.all(
            RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
          ),
        ),
      ),

      inputDecorationTheme: InputDecorationTheme(
        fillColor: AppColors.light.inputBg,
        filled: true,
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(8),
          borderSide: BorderSide(color: AppColors.light.borderColor),
        ),
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(8),
          borderSide: BorderSide(color: AppColors.light.borderColor),
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(8),
          borderSide: BorderSide(color: AppColors.light.inputFocusBorder, width: 2.0),
        ),
        contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
      ),
    );
  }

  static ThemeData _buildDarkTheme() {
    final ThemeData base = ThemeData.dark();
    final TextTheme textTheme = _buildTextTheme(
      base: Typography.whiteMountainView, 
      color: AppColors.dark.textColor, 
      secondaryColor: AppColors.dark.textSecondary,
    );
    
    return base.copyWith(
      brightness: Brightness.dark,
      primaryColor: AppColors.dark.accentColor,
      scaffoldBackgroundColor: AppColors.dark.bgColor,
      cardColor: AppColors.dark.cardBg,
      dividerColor: AppColors.dark.borderColor,
      hintColor: AppColors.dark.textMuted,

      colorScheme: ColorScheme.dark(
        primary: AppColors.dark.accentColor,
        secondary: AppColors.dark.accentLight,
        surface: AppColors.dark.cardBg,
        background: AppColors.dark.bgColor,
        error: AppColors.dark.dangerColor,
        onPrimary: Colors.white,
        onSecondary: Colors.white,
        onSurface: AppColors.dark.textColor,
        onBackground: AppColors.dark.textColor,
        onError: Colors.white,
      ),

      textTheme: GoogleFonts.interTextTheme(textTheme),

      elevatedButtonTheme: ElevatedButtonThemeData(
        style: ButtonStyle(
          backgroundColor: MaterialStateProperty.resolveWith<Color>((states) {
            if (states.contains(MaterialState.disabled)) {
              return AppColors.dark.buttonBg.withOpacity(0.5);
            }
            if (states.contains(MaterialState.hovered)) {
              return AppColors.dark.buttonHover;
            }
            return AppColors.dark.buttonBg;
          }),
          foregroundColor: MaterialStateProperty.all(Colors.white),
          padding: MaterialStateProperty.all(
            const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
          ),
          shape: MaterialStateProperty.all(
            RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
          ),
        ),
      ),

      inputDecorationTheme: InputDecorationTheme(
        fillColor: AppColors.dark.inputBg,
        filled: true,
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(8),
          borderSide: BorderSide(color: AppColors.dark.borderColor),
        ),
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(8),
          borderSide: BorderSide(color: AppColors.dark.borderColor),
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(8),
          borderSide: BorderSide(color: AppColors.dark.inputFocusBorder, width: 2.0),
        ),
        contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
      ),
    );
  }

  static TextTheme _buildTextTheme({required TextTheme base, required Color color, required Color secondaryColor}) {
    return base.copyWith(
      displayLarge: base.displayLarge!.copyWith(color: color, fontWeight: FontWeight.w300),
      displayMedium: base.displayMedium!.copyWith(color: color, fontWeight: FontWeight.w300),
      displaySmall: base.displaySmall!.copyWith(color: color, fontWeight: FontWeight.normal),
      headlineLarge: base.headlineLarge!.copyWith(color: color, fontWeight: FontWeight.bold),
      headlineMedium: base.headlineMedium!.copyWith(color: color, fontWeight: FontWeight.bold),
      headlineSmall: base.headlineSmall!.copyWith(color: color, fontWeight: FontWeight.bold),
      titleLarge: base.titleLarge!.copyWith(color: color, fontWeight: FontWeight.w600),
      titleMedium: base.titleMedium!.copyWith(color: color, fontWeight: FontWeight.w600),
      titleSmall: base.titleSmall!.copyWith(color: color, fontWeight: FontWeight.w600),
      bodyLarge: base.bodyLarge!.copyWith(color: color),
      bodyMedium: base.bodyMedium!.copyWith(color: color),
      bodySmall: base.bodySmall!.copyWith(color: secondaryColor),
      labelLarge: base.labelLarge!.copyWith(color: color, fontWeight: FontWeight.w500),
      labelMedium: base.labelMedium!.copyWith(color: secondaryColor),
      labelSmall: base.labelSmall!.copyWith(color: secondaryColor),
    );
  }
}

class ThemeProvider extends ChangeNotifier {
  bool _isDarkMode = false;

  bool get isDarkMode => _isDarkMode;
  ThemeMode get currentThemeMode => _isDarkMode ? ThemeMode.dark : ThemeMode.light;

  void toggleTheme() {
    _isDarkMode = !_isDarkMode;
    notifyListeners();
  }

  ThemeData get currentTheme => _isDarkMode ? AppTheme.darkTheme : AppTheme.lightTheme;
} 