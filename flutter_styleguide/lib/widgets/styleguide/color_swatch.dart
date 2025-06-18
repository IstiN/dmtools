import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class ColorSwatchItem extends StatelessWidget {
  final Color color;
  final String name;
  final String hexCode;
  final Color? textColor;

  const ColorSwatchItem({
    Key? key,
    required this.color,
    required this.name,
    required this.hexCode,
    this.textColor,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final contrastColor = textColor ?? _getContrastColor(color);
    
    return Container(
      width: 180,
      height: 120,
      decoration: BoxDecoration(
        color: color,
        borderRadius: BorderRadius.circular(8),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.1),
            blurRadius: 4,
            offset: const Offset(0, 2),
          ),
        ],
      ),
      child: Material(
        color: Colors.transparent,
        child: InkWell(
          onTap: () {
            Clipboard.setData(ClipboardData(text: hexCode));
            ScaffoldMessenger.of(context).showSnackBar(
              SnackBar(
                content: Text('$hexCode copied to clipboard'),
                duration: const Duration(seconds: 2),
              ),
            );
          },
          borderRadius: BorderRadius.circular(8),
          child: Padding(
            padding: const EdgeInsets.all(16),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              mainAxisAlignment: MainAxisAlignment.end,
              children: [
                Text(
                  name,
                  style: TextStyle(
                    color: contrastColor,
                    fontSize: 16,
                    fontWeight: FontWeight.bold,
                  ),
                ),
                const SizedBox(height: 4),
                Text(
                  hexCode,
                  style: TextStyle(
                    color: contrastColor.withOpacity(0.8),
                    fontSize: 14,
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
  
  Color _getContrastColor(Color backgroundColor) {
    // Calculate relative luminance
    double luminance = 0.299 * backgroundColor.red + 
                       0.587 * backgroundColor.green + 
                       0.114 * backgroundColor.blue;
    
    // Use white text on dark backgrounds and black text on light backgrounds
    return luminance > 128 ? Colors.black : Colors.white;
  }
} 