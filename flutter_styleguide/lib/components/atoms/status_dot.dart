import 'package:flutter/material.dart';
import '../../theme/app_colors.dart';

enum StatusType {
  success,
  warning,
  error,
  info,
  neutral,
}

class StatusDot extends StatelessWidget {
  final StatusType status;
  final double size;

  const StatusDot({
    Key? key,
    required this.status,
    this.size = 10.0,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Container(
      width: size,
      height: size,
      decoration: BoxDecoration(
        color: _getColorForStatus(),
        shape: BoxShape.circle,
      ),
    );
  }

  Color _getColorForStatus() {
    switch (status) {
      case StatusType.success:
        return AppColors.success;
      case StatusType.warning:
        return AppColors.warning;
      case StatusType.error:
        return AppColors.error;
      case StatusType.info:
        return AppColors.info;
      case StatusType.neutral:
        return AppColors.textSecondary;
    }
  }
} 