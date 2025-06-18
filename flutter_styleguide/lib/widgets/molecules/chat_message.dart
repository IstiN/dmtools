import 'package:flutter/material.dart';

enum MessageSender { user, agent, system }

class ChatMessage extends StatelessWidget {
  final String text;
  final MessageSender sender;

  const ChatMessage({
    Key? key,
    required this.text,
    required this.sender,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    if (sender == MessageSender.system) {
      return _buildSystemMessage(context);
    }

    final isUser = sender == MessageSender.user;

    return Container(
      margin: const EdgeInsets.symmetric(vertical: 8.0),
      child: Row(
        mainAxisAlignment: isUser ? MainAxisAlignment.end : MainAxisAlignment.start,
        crossAxisAlignment: CrossAxisAlignment.end,
        children: [
          if (!isUser) ...[
            const CircleAvatar(
              child: Icon(Icons.smart_toy_outlined),
            ),
            const SizedBox(width: 8),
          ],
          Flexible(
            child: Container(
              padding: const EdgeInsets.symmetric(vertical: 10, horizontal: 16),
              decoration: BoxDecoration(
                color: _getBackgroundColor(theme),
                borderRadius: BorderRadius.circular(20),
                border: sender == MessageSender.agent ? Border.all(color: theme.dividerColor) : null,
              ),
              child: Text(
                text,
                style: TextStyle(color: _getTextColor(theme)),
              ),
            ),
          ),
          if (isUser) ...[
            const SizedBox(width: 8),
            const CircleAvatar(
              child: Icon(Icons.person_outline),
            ),
          ],
        ],
      ),
    );
  }

  Widget _buildSystemMessage(BuildContext context) {
    final theme = Theme.of(context);
    return Container(
      margin: const EdgeInsets.symmetric(vertical: 8.0),
      alignment: Alignment.center,
      child: Text(
        text,
        style: theme.textTheme.bodySmall?.copyWith(color: theme.hintColor),
      ),
    );
  }

  Color _getBackgroundColor(ThemeData theme) {
    switch (sender) {
      case MessageSender.user:
        return theme.colorScheme.primary;
      case MessageSender.agent:
        return theme.cardColor;
      case MessageSender.system:
        return Colors.transparent;
    }
  }

  Color? _getTextColor(ThemeData theme) {
    switch (sender) {
      case MessageSender.user:
        return theme.colorScheme.onPrimary;
      default:
        return theme.textTheme.bodyLarge?.color;
    }
  }
} 