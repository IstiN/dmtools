import 'package:flutter/material.dart';
import '../../theme/app_colors.dart';

enum ButtonStyle {
  primary,
  secondary,
  outline,
  tertiary,
}

class AppButton extends StatelessWidget {
  final String text;
  final VoidCallback onPressed;
  final ButtonStyle style;
  final bool isFullWidth;
  final IconData? icon;
  final bool isLoading;
  final bool isDisabled;

  const AppButton({
    Key? key,
    required this.text,
    required this.onPressed,
    this.style = ButtonStyle.primary,
    this.isFullWidth = false,
    this.icon,
    this.isLoading = false,
    this.isDisabled = false,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final isIconButton = icon != null;
    
    Widget buttonContent = Row(
      mainAxisSize: MainAxisSize.min,
      children: [
        if (isLoading)
          Container(
            width: 16,
            height: 16,
            margin: const EdgeInsets.only(right: 8),
            child: CircularProgressIndicator(
              strokeWidth: 2,
              valueColor: AlwaysStoppedAnimation<Color>(_getContentColor()),
            ),
          )
        else if (isIconButton)
          Padding(
            padding: const EdgeInsets.only(right: 8),
            child: Icon(icon, size: 18, color: _getContentColor()),
          ),
        Text(
          text,
          style: TextStyle(
            fontSize: 14,
            fontWeight: FontWeight.w500,
            color: _getContentColor(),
          ),
        ),
      ],
    );

    Widget button;
    
    switch (style) {
      case ButtonStyle.primary:
        button = ElevatedButton(
          onPressed: isDisabled || isLoading ? null : onPressed,
          style: ElevatedButton.styleFrom(
            backgroundColor: AppColors.primary,
            foregroundColor: Colors.white,
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(8),
            ),
          ),
          child: buttonContent,
        );
        break;
      case ButtonStyle.secondary:
        button = ElevatedButton(
          onPressed: isDisabled || isLoading ? null : onPressed,
          style: ElevatedButton.styleFrom(
            backgroundColor: AppColors.secondary,
            foregroundColor: Colors.white,
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(8),
            ),
          ),
          child: buttonContent,
        );
        break;
      case ButtonStyle.outline:
        button = OutlinedButton(
          onPressed: isDisabled || isLoading ? null : onPressed,
          style: OutlinedButton.styleFrom(
            foregroundColor: AppColors.primary,
            side: BorderSide(color: isDisabled ? AppColors.textDisabled : AppColors.primary),
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(8),
            ),
          ),
          child: buttonContent,
        );
        break;
      case ButtonStyle.tertiary:
        button = TextButton(
          onPressed: isDisabled || isLoading ? null : onPressed,
          style: TextButton.styleFrom(
            foregroundColor: AppColors.primary,
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(8),
            ),
          ),
          child: buttonContent,
        );
        break;
    }

    if (isFullWidth) {
      return SizedBox(
        width: double.infinity,
        child: button,
      );
    }

    return button;
  }

  Color _getContentColor() {
    if (isDisabled) {
      return AppColors.textDisabled;
    }

    switch (style) {
      case ButtonStyle.primary:
      case ButtonStyle.secondary:
        return Colors.white;
      case ButtonStyle.outline:
      case ButtonStyle.tertiary:
        return AppColors.primary;
    }
  }
} 