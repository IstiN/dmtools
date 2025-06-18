import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../theme/app_theme.dart';
import '../../theme/app_colors.dart';

class ComponentItem extends StatelessWidget {
  final String title;
  final Widget child;
  final String? codeSnippet;

  const ComponentItem({
    Key? key,
    required this.title,
    required this.child,
    this.codeSnippet,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          title,
          style: Theme.of(context).textTheme.titleMedium,
        ),
        const SizedBox(height: 12),
        child,
        if (codeSnippet != null) ...[
          const SizedBox(height: 12),
          SelectableText(
            codeSnippet!,
            style: const TextStyle(
              fontFamily: 'monospace',
              fontSize: 13,
            ),
          ),
        ],
      ],
    );
  }
} 