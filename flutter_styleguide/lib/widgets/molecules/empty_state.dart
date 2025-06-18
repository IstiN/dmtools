import 'package:flutter/material.dart';
import 'dart:ui';

class EmptyState extends StatefulWidget {
  final IconData icon;
  final String title;
  final String message;
  final VoidCallback? onPressed;

  const EmptyState({
    Key? key,
    required this.icon,
    required this.title,
    required this.message,
    this.onPressed,
  }) : super(key: key);

  @override
  State<EmptyState> createState() => _EmptyStateState();
}

class _EmptyStateState extends State<EmptyState> {
  bool _isHovering = false;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;

    // Colors based on the web version
    final borderColor = _isHovering 
        ? const Color(0xFF6078F0).withOpacity(0.8)
        : (isDark ? Colors.white.withOpacity(0.2) : Colors.grey.withOpacity(0.3));
    
    final iconColor = _isHovering
        ? const Color(0xFF6078F0)
        : const Color(0xFF6C757D);
    
    final titleColor = _isHovering
        ? const Color(0xFF6078F0)
        : (isDark ? Colors.white : const Color(0xFF212529));
    
    final messageColor = isDark ? Colors.white70 : const Color(0xFF6C757D);

    return MouseRegion(
      onEnter: (_) => setState(() => _isHovering = true),
      onExit: (_) => setState(() => _isHovering = false),
      cursor: widget.onPressed != null ? SystemMouseCursors.click : SystemMouseCursors.basic,
      child: GestureDetector(
        onTap: widget.onPressed,
        child: AnimatedContainer(
          duration: const Duration(milliseconds: 200),
          decoration: BoxDecoration(
            color: Colors.transparent,
            borderRadius: BorderRadius.circular(12),
          ),
          child: CustomPaint(
            painter: DashedBorderPainter(borderColor: borderColor),
            child: Padding(
              padding: const EdgeInsets.symmetric(vertical: 48.0, horizontal: 24.0),
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                crossAxisAlignment: CrossAxisAlignment.center,
                children: [
                  AnimatedContainer(
                    duration: const Duration(milliseconds: 200),
                    child: Icon(
                      widget.icon,
                      size: 40,
                      color: iconColor,
                    ),
                  ),
                  const SizedBox(height: 20),
                  AnimatedDefaultTextStyle(
                    duration: const Duration(milliseconds: 200),
                    style: TextStyle(
                      fontWeight: FontWeight.w600,
                      color: titleColor,
                      fontSize: 24,
                    ),
                    child: Text(
                      widget.title,
                      textAlign: TextAlign.center,
                    ),
                  ),
                  const SizedBox(height: 8),
                  Text(
                    widget.message,
                    style: TextStyle(
                      color: messageColor,
                      fontSize: 16,
                    ),
                    textAlign: TextAlign.center,
                  ),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }
}

class DashedBorderPainter extends CustomPainter {
  final Color borderColor;
  
  DashedBorderPainter({
    required this.borderColor,
  });
  
  @override
  void paint(Canvas canvas, Size size) {
    final Paint paint = Paint()
      ..color = borderColor
      ..strokeWidth = 2
      ..style = PaintingStyle.stroke;
    
    final Path path = Path()
      ..addRRect(RRect.fromRectAndRadius(
        Rect.fromLTWH(0, 0, size.width, size.height),
        const Radius.circular(12),
      ));
    
    final Path dashPath = Path();
    
    const double dashWidth = 6.0;
    const double dashSpace = 4.0;
    
    double distance = 0.0;
    
    for (PathMetric pathMetric in path.computeMetrics()) {
      while (distance < pathMetric.length) {
        dashPath.addPath(
          pathMetric.extractPath(distance, distance + dashWidth),
          Offset.zero,
        );
        distance += dashWidth;
        distance += dashSpace;
      }
    }
    
    canvas.drawPath(dashPath, paint);
  }
  
  @override
  bool shouldRepaint(covariant DashedBorderPainter oldDelegate) {
    return oldDelegate.borderColor != borderColor;
  }
} 