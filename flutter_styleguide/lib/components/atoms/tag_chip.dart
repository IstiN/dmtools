import 'package:flutter/material.dart';
import '../../theme/app_colors.dart';

class TagChip extends StatelessWidget {
  final String label;
  final Color? backgroundColor;
  final Color? textColor;
  final VoidCallback? onRemove;

  const TagChip({
    Key? key,
    required this.label,
    this.backgroundColor,
    this.textColor,
    this.onRemove,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final bgColor = backgroundColor ?? AppColors.primary.withOpacity(0.1);
    final txtColor = textColor ?? AppColors.primary;
    
    return Container(
      padding: EdgeInsets.symmetric(
        horizontal: onRemove != null ? 8 : 12,
        vertical: 4,
      ),
      decoration: BoxDecoration(
        color: bgColor,
        borderRadius: BorderRadius.circular(16),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Text(
            label,
            style: TextStyle(
              color: txtColor,
              fontSize: 12,
              fontWeight: FontWeight.w500,
            ),
          ),
          if (onRemove != null)
            GestureDetector(
              onTap: onRemove,
              child: Padding(
                padding: const EdgeInsets.only(left: 4),
                child: Icon(
                  Icons.close,
                  size: 14,
                  color: txtColor,
                ),
              ),
            ),
        ],
      ),
    );
  }
} 