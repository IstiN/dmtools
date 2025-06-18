import 'package:flutter/material.dart';
import 'package:dmtools_styleguide/widgets/atoms/app_button.dart';

class ChatInputGroup extends StatelessWidget {
  const ChatInputGroup({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(8),
      child: Row(
        children: [
          const Expanded(
            child: TextField(
              decoration: InputDecoration(
                hintText: 'Type your message...',
                border: OutlineInputBorder(),
              ),
            ),
          ),
          const SizedBox(width: 8),
          AppButton(
            text: '',
            icon: Icons.attach_file,
            onPressed: () {},
            variant: ButtonVariant.icon,
          ),
          const SizedBox(width: 8),
          AppButton(
            text: 'Send',
            onPressed: () {},
            variant: ButtonVariant.primary,
          ),
        ],
      ),
    );
  }
} 