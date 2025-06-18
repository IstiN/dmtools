import 'package:flutter/material.dart';

// Interface for theme colors
abstract class ThemeColorSet {
  // Base Colors
  Color get bgColor;
  Color get cardBg;
  Color get textColor;
  Color get textSecondary;
  Color get textMuted;
  Color get borderColor;
  
  // Accent Colors
  Color get accentColor;
  Color get accentLight;
  Color get accentHover;
  
  // Button & Interaction Colors
  Color get buttonBg;
  Color get buttonHover;
  Color get hoverBg;
  
  // Feedback Colors
  Color get successColor;
  Color get warningColor;
  Color get dangerColor;
  Color get infoColor;
  
  // Input Colors
  Color get inputBg;
  Color get inputFocusBorder;
}

class AppColors {
  // Base Colors (Light Theme Default)
  static const Color lightBgColor = Color(0xFFF8F9FA);     // #f8f9fa
  static const Color lightCardBg = Color(0xFFFFFFFF);      // #ffffff
  static const Color lightTextColor = Color(0xFF212529);   // #212529
  static const Color lightTextSecondary = Color(0xFF495057); // #495057
  static const Color lightTextMuted = Color(0xFF6C757D);   // #6c757d
  static const Color lightBorderColor = Color(0xFFEAEDF1); // #eaedf1
  
  // Base Colors (Dark Theme)
  static const Color darkBgColor = Color(0xFF121212);      // #121212
  static const Color darkCardBg = Color(0xFF1E1E1E);       // #1e1e1e
  static const Color darkTextColor = Color(0xFFE9ECEF);    // #e9ecef
  static const Color darkTextSecondary = Color(0xFFCED4DA); // #ced4da
  static const Color darkTextMuted = Color(0xFFADB5BD);    // #adb5bd
  static const Color darkBorderColor = Color(0xFF2A2A2A);  // #2a2a2a
  
  // Accent Colors
  static const Color accentColor = Color(0xFF466AF1);      // #466af1
  static const Color accentLight = Color(0xFF6988F5);      // #6988f5
  static const Color accentHover = Color(0xFF3155DB);      // #3155db
  
  // Button & Interaction Colors
  static const Color buttonBg = Color(0xFF466AF1);         // #466af1 (same as accent)
  static const Color buttonHover = Color(0xFF3155DB);      // #3155db (same as accent-hover)
  static final Color hoverBg = Color(0xFF466AF1).withOpacity(0.08); // rgba(70,106,241,0.08)
  
  // Feedback Colors
  static const Color successColor = Color(0xFF10B981);     // #10b981
  static const Color warningColor = Color(0xFFF59E0B);     // #f59e0b
  static const Color dangerColor = Color(0xFFEF4444);      // #ef4444
  static const Color infoColor = Color(0xFF3B82F6);        // #3b82f6
  
  // Input Colors
  static const Color inputBg = Color(0xFFFFFFFF);          // #ffffff
  static const Color inputFocusBorder = Color(0xFF466AF1); // #466af1 (same as accent)

  // Theme objects
  static final ThemeColorSet light = _LightColors();
  static final ThemeColorSet dark = _DarkColors();
}

class _LightColors implements ThemeColorSet {
  @override
  final Color bgColor = AppColors.lightBgColor;
  @override
  final Color cardBg = AppColors.lightCardBg;
  @override
  final Color textColor = AppColors.lightTextColor;
  @override
  final Color textSecondary = AppColors.lightTextSecondary;
  @override
  final Color textMuted = AppColors.lightTextMuted;
  @override
  final Color borderColor = AppColors.lightBorderColor;
  @override
  final Color accentColor = AppColors.accentColor;
  @override
  final Color accentLight = AppColors.accentLight;
  @override
  final Color accentHover = AppColors.accentHover;
  @override
  final Color buttonBg = AppColors.buttonBg;
  @override
  final Color buttonHover = AppColors.buttonHover;
  @override
  final Color hoverBg = AppColors.hoverBg;
  @override
  final Color inputBg = AppColors.inputBg;
  @override
  final Color inputFocusBorder = AppColors.inputFocusBorder;
  @override
  final Color successColor = AppColors.successColor;
  @override
  final Color warningColor = AppColors.warningColor;
  @override
  final Color dangerColor = AppColors.dangerColor;
  @override
  final Color infoColor = AppColors.infoColor;
}

class _DarkColors implements ThemeColorSet {
  @override
  final Color bgColor = AppColors.darkBgColor;
  @override
  final Color cardBg = AppColors.darkCardBg;
  @override
  final Color textColor = AppColors.darkTextColor;
  @override
  final Color textSecondary = AppColors.darkTextSecondary;
  @override
  final Color textMuted = AppColors.darkTextMuted;
  @override
  final Color borderColor = AppColors.darkBorderColor;
  @override
  final Color accentColor = AppColors.accentColor;
  @override
  final Color accentLight = AppColors.accentLight;
  @override
  final Color accentHover = AppColors.accentHover;
  @override
  final Color buttonBg = AppColors.buttonBg;
  @override
  final Color buttonHover = AppColors.buttonHover;
  @override
  final Color hoverBg = AppColors.hoverBg;
  @override
  final Color inputBg = AppColors.darkCardBg;
  @override
  final Color inputFocusBorder = AppColors.inputFocusBorder;
  @override
  final Color successColor = AppColors.successColor;
  @override
  final Color warningColor = AppColors.warningColor;
  @override
  final Color dangerColor = AppColors.dangerColor;
  @override
  final Color infoColor = AppColors.infoColor;
} 