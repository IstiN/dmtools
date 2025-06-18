import 'package:flutter/material.dart';
import 'package:dmtools_mobile/core/theme/app_colors.dart';

enum StatusDotType {
  success,
  warning,
  error,
  info,
  offline,
  processing,
}

class StatusDot extends StatelessWidget {
  final StatusDotType type;
  final double size;

  const StatusDot({
    super.key,
    this.type = StatusDotType.success,
    this.size = 10.0,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      width: size,
      height: size,
      decoration: BoxDecoration(
        color: _getColor(context),
        shape: BoxShape.circle,
      ),
    );
  }

  Color _getColor(BuildContext context) {
    final bool isDark = Theme.of(context).brightness == Brightness.dark;
    switch (type) {
      case StatusDotType.success:
        return lightSuccessColor;
      case StatusDotType.warning:
        return lightWarningColor;
      case StatusDotType.error:
        return lightDangerColor;
      case StatusDotType.info:
        return lightInfoColor;
      case StatusDotType.offline:
        return isDark ? darkTextMuted : lightTextMuted;
      case StatusDotType.processing:
        return isDark ? darkAccentColor : lightAccentColor;
      default:
        return lightSuccessColor;
    }
  }
} 