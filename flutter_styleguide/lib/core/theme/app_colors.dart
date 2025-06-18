import 'package:flutter/material.dart';

/// DMTools Design System Colors
/// Based on CSS variables from styleguide.css
class AppColors {
  static const AppColorScheme light = AppColorScheme(
    bgColor: Color(0xFFF8F9FA),
    cardBg: Colors.white,
    textColor: Color(0xFF202124),
    textSecondary: Color(0xFF5F6368),
    textMuted: Color(0xFF9AA0A6),
    headerColor: Color(0xFF202124),
    borderColor: Color(0xFFDADCE0),
    cardShadow: Color(0x1A000000),
    inputBg: Colors.white,
    inputFocusBorder: Color(0xFF4285F4),
    hoverBg: Color(0xFFF0F2F5),
    
    accentColor: Color(0xFF4285F4),
    accentLight: Color(0xFF8AB4F8),
    accentHover: Color(0xFF3367D6),
    
    buttonBg: Color(0xFF4285F4),
    buttonHover: Color(0xFF3367D6),
    
    successColor: Color(0xFF34A853),
    warningColor: Color(0xFFFBBC05),
    dangerColor: Color(0xFFEA4335),
    infoColor: Color(0xFF4285F4),
    
    gradientStart: Color(0xFF4285F4),
    gradientEnd: Color(0xFF8AB4F8),
  );

  static const AppColorScheme dark = AppColorScheme(
    bgColor: Color(0xFF202124),
    cardBg: Color(0xFF2D2E30),
    textColor: Color(0xFFE8EAED),
    textSecondary: Color(0xFFBDC1C6),
    textMuted: Color(0xFF9AA0A6),
    headerColor: Color(0xFFE8EAED),
    borderColor: Color(0xFF5F6368),
    cardShadow: Color(0x33000000),
    inputBg: Color(0xFF2D2E30),
    inputFocusBorder: Color(0xFF8AB4F8),
    hoverBg: Color(0xFF323248),
    
    accentColor: Color(0xFF8AB4F8),
    accentLight: Color(0xFFAECBFA),
    accentHover: Color(0xFF669DF6),
    
    buttonBg: Color(0xFF8AB4F8),
    buttonHover: Color(0xFF669DF6),
    
    successColor: Color(0xFF81C995),
    warningColor: Color(0xFFFDD663),
    dangerColor: Color(0xFFFA7B6C),
    infoColor: Color(0xFF8AB4F8),
    
    gradientStart: Color(0xFF8AB4F8),
    gradientEnd: Color(0xFFAECBFA),
  );
}

class AppColorScheme {
  final Color bgColor;
  final Color cardBg;
  final Color textColor;
  final Color textSecondary;
  final Color textMuted;
  final Color headerColor;
  final Color borderColor;
  final Color cardShadow;
  final Color inputBg;
  final Color inputFocusBorder;
  final Color hoverBg;
  
  final Color accentColor;
  final Color accentLight;
  final Color accentHover;
  
  final Color buttonBg;
  final Color buttonHover;
  
  final Color successColor;
  final Color warningColor;
  final Color dangerColor;
  final Color infoColor;
  
  final Color gradientStart;
  final Color gradientEnd;

  const AppColorScheme({
    required this.bgColor,
    required this.cardBg,
    required this.textColor,
    required this.textSecondary,
    required this.textMuted,
    required this.headerColor,
    required this.borderColor,
    required this.cardShadow,
    required this.inputBg,
    required this.inputFocusBorder,
    required this.hoverBg,
    required this.accentColor,
    required this.accentLight,
    required this.accentHover,
    required this.buttonBg,
    required this.buttonHover,
    required this.successColor,
    required this.warningColor,
    required this.dangerColor,
    required this.infoColor,
    required this.gradientStart,
    required this.gradientEnd,
  });
} 