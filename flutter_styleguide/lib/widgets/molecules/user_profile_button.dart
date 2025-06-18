import 'package:flutter/material.dart';

class UserProfileButton extends StatelessWidget {
  final String userName;
  final String? avatarUrl;
  final VoidCallback? onPressed;

  const UserProfileButton({
    Key? key,
    required this.userName,
    this.avatarUrl,
    this.onPressed,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return TextButton(
      onPressed: onPressed,
      style: TextButton.styleFrom(
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
      ),
      child: Row(
        children: [
          CircleAvatar(
            radius: 14,
            backgroundImage: avatarUrl != null ? NetworkImage(avatarUrl!) : null,
            child: avatarUrl == null ? const Icon(Icons.person_outline, size: 16) : null,
          ),
          const SizedBox(width: 8),
          Text(
            userName,
            style: Theme.of(context).textTheme.bodyMedium,
          ),
          const SizedBox(width: 4),
          const Icon(Icons.keyboard_arrow_down, size: 16),
        ],
      ),
    );
  }
} 