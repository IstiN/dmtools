import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../theme/app_theme.dart';
import '../../theme/app_colors.dart';

enum ButtonVariant {
  base,
  primary,
  secondary,
  outline,
  text,
  icon,
  run,
}

enum ButtonSize {
  small,
  medium,
  large,
}

class AppButton extends StatefulWidget {
  final String text;
  final VoidCallback? onPressed;
  final ButtonVariant variant;
  final ButtonSize size;
  final IconData? icon;
  final bool isFullWidth;
  final bool isLoading;
  final bool isDisabled;
  final bool isTestMode;
  final bool testDarkMode;

  const AppButton({
    Key? key,
    required this.text,
    this.onPressed,
    this.variant = ButtonVariant.primary,
    this.size = ButtonSize.medium,
    this.icon,
    this.isFullWidth = false,
    this.isLoading = false,
    this.isDisabled = false,
    this.isTestMode = false,
    this.testDarkMode = false,
  }) : super(key: key);

  @override
  _AppButtonState createState() => _AppButtonState();
}

class _AppButtonState extends State<AppButton> {
  bool _isHovering = false;
  bool _isPressed = false;

  @override
  Widget build(BuildContext context) {
    final isDarkMode = widget.isTestMode
        ? widget.testDarkMode
        : Provider.of<ThemeProvider>(context).isDarkMode;
    final ThemeColorSet colors = isDarkMode ? AppColors.dark : AppColors.light;

    // --- BUTTON STYLE DEFINITIONS FROM CSS ---

    // 1. Sizing
    final paddings = {
      ButtonSize.small: const EdgeInsets.symmetric(horizontal: 12.8, vertical: 6.4), // 0.8rem, 0.4rem
      ButtonSize.medium: const EdgeInsets.symmetric(horizontal: 20, vertical: 9.6),   // 1.25rem, 0.6rem
      ButtonSize.large: const EdgeInsets.symmetric(horizontal: 24, vertical: 12),
    };
    final fontSizes = {
      ButtonSize.small: 13.6,  // 0.85rem
      ButtonSize.medium: 15.2, // 0.95rem
      ButtonSize.large: 16.0,
    };
    final iconSizes = {
      ButtonSize.small: 14.0,
      ButtonSize.medium: 16.0,
      ButtonSize.large: 18.0,
    };
    
    final effectiveOnPressed = widget.isDisabled || widget.isLoading ? null : widget.onPressed;

    // 2. Color and Border Logic
    Color bgColor;
    Color textColor;
    Color hoverBgColor;
    Color hoverTextColor;
    BorderSide borderSide;
    BorderSide hoverBorderSide;
    List<BoxShadow> shadows = [];
    List<BoxShadow> hoverShadows = [];

    // Base styles from .btn
    textColor = colors.textColor;
    hoverTextColor = colors.textColor;
    borderSide = BorderSide.none;
    hoverBorderSide = BorderSide.none;

    switch (widget.variant) {
      case ButtonVariant.primary:
        bgColor = colors.buttonBg;
        textColor = Colors.white;
        hoverBgColor = colors.buttonHover;
        hoverTextColor = Colors.white;
        hoverShadows = [
          BoxShadow(
            color: const Color(0xFF000000).withOpacity(0.15),
            blurRadius: 12,
            offset: const Offset(0, 4),
          )
        ];
        break;
      case ButtonVariant.secondary:
        bgColor = Colors.transparent;
        textColor = colors.accentColor;
        borderSide = BorderSide(color: colors.accentColor, width: 1.5);
        hoverBgColor = colors.hoverBg;
        hoverTextColor = colors.accentColor;
        hoverBorderSide = borderSide;
        break;
      case ButtonVariant.outline:
        bgColor = Colors.transparent;
        textColor = isDarkMode ? Colors.white.withOpacity(0.9) : colors.accentColor;
        borderSide = BorderSide(color: isDarkMode ? Colors.white.withOpacity(0.8) : colors.accentColor, width: 1.5);
        hoverBgColor = isDarkMode ? Colors.white.withOpacity(0.15) : colors.hoverBg;
        hoverTextColor = isDarkMode ? Colors.white : colors.accentColor;
        hoverBorderSide = borderSide;
        break;
      case ButtonVariant.base: // Maps to .btn-tertiary
        bgColor = isDarkMode ? const Color(0xFF3A3A3A) : const Color(0xFFF1F5F9);
        textColor = colors.textSecondary;
        borderSide = BorderSide(color: colors.borderColor, width: 1);
        hoverBgColor = colors.hoverBg;
        hoverTextColor = colors.accentColor;
        hoverBorderSide = BorderSide(color: colors.accentColor, width: 1);
        break;
      case ButtonVariant.icon:
        bgColor = Colors.transparent;
        textColor = colors.textSecondary;
        borderSide = BorderSide(color: colors.borderColor, width: 1);
        hoverBgColor = colors.hoverBg;
        hoverTextColor = colors.accentColor;
        hoverBorderSide = borderSide; // Border does not change color on hover for icon
        break;
      case ButtonVariant.run:
        bgColor = colors.successColor;
        textColor = Colors.white;
        hoverBgColor = isDarkMode ? const Color(0xFF0E9A71) : const Color(0xFF059669);
        hoverTextColor = Colors.white;
        hoverShadows = [
          BoxShadow(
            color: const Color(0xFF000000).withOpacity(0.15),
            blurRadius: 12,
            offset: const Offset(0, 4),
          )
        ];
        break;
      case ButtonVariant.text:
        bgColor = Colors.transparent;
        textColor = colors.accentColor;
        hoverBgColor = colors.hoverBg;
        hoverTextColor = colors.accentColor;
        break;
    }

    // --- DISABLED STATE OVERRIDE ---
    if (widget.isDisabled) {
      bgColor = isDarkMode ? const Color(0xFF2A2A2A) : const Color(0xFFE2E2E2);
      textColor = isDarkMode ? const Color(0xFF707070) : const Color(0xFFAAAAAA);
      borderSide = BorderSide.none;
      hoverShadows = [];
    }

    // --- WIDGET TREE ---

    final isHovering = _isHovering && !widget.isDisabled;
    final isCurrentlyPressed = _isPressed && !widget.isDisabled;
    
    Widget content;
    if (widget.isLoading) {
      content = SizedBox(
        width: fontSizes[widget.size]!,
        height: fontSizes[widget.size]!,
        child: CircularProgressIndicator(
          strokeWidth: 2,
          valueColor: AlwaysStoppedAnimation<Color>(isHovering ? hoverTextColor : textColor),
        ),
      );
    } else if (widget.variant == ButtonVariant.icon) {
      content = Icon(widget.icon, size: iconSizes[widget.size], color: isHovering ? hoverTextColor : textColor);
    } else {
      content = Row(
        mainAxisSize: MainAxisSize.min,
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          if (widget.icon != null) ...[
            Icon(widget.icon, size: iconSizes[widget.size], color: isHovering ? hoverTextColor : textColor),
            const SizedBox(width: 8),
          ],
          Text(
            widget.text,
            style: TextStyle(
              fontSize: fontSizes[widget.size],
              fontWeight: FontWeight.w500,
              color: isHovering ? hoverTextColor : textColor,
            ),
          ),
        ],
      );
    }

    final button = MouseRegion(
      onEnter: (_) => setState(() => _isHovering = true),
      onExit: (_) => setState(() => _isHovering = false),
      cursor: SystemMouseCursors.click,
      child: GestureDetector(
        onTapDown: (_) => setState(() => _isPressed = true),
        onTapUp: (_) => setState(() => _isPressed = false),
        onTapCancel: () => setState(() => _isPressed = false),
        onTap: effectiveOnPressed,
        child: TweenAnimationBuilder<double>(
          tween: Tween(begin: 0.0, end: isHovering ? 1.0 : 0.0),
          duration: const Duration(milliseconds: 200),
          builder: (context, value, child) {
            final currentTransform = isCurrentlyPressed
                ? Matrix4.translationValues(0, -1, 0) // Less of a lift when pressed
                : Matrix4.translationValues(0, -2 * value, 0); // Hover lift

            return Transform(
              transform: currentTransform,
              child: Container(
                width: widget.isFullWidth ? double.infinity : null,
                padding: widget.variant == ButtonVariant.icon ? EdgeInsets.zero : paddings[widget.size],
                height: widget.variant == ButtonVariant.icon ? 36 : null,
                constraints: widget.variant == ButtonVariant.icon
                    ? const BoxConstraints(maxWidth: 36, maxHeight: 36)
                    : null,
                decoration: BoxDecoration(
                  color: Color.lerp(bgColor, hoverBgColor, value),
                  borderRadius: BorderRadius.circular(widget.variant == ButtonVariant.run ? 6 : 8),
                  border: Border.fromBorderSide(BorderSide.lerp(borderSide, hoverBorderSide, value)),
                  boxShadow: BoxShadow.lerpList(shadows, hoverShadows, value),
                ),
                alignment: Alignment.center,
                child: child,
              ),
            );
          },
          child: content,
        ),
      ),
    );

    if (widget.variant == ButtonVariant.icon) {
      return Tooltip(
        message: widget.text,
        child: button,
      );
    }
    return button;
  }
} 