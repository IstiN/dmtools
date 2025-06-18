import 'package:flutter/material.dart';
import 'package:dmtools_styleguide/widgets/atoms/app_button.dart';

class ActionButtonGroup extends StatelessWidget {
  final List<Widget> buttons;

  const ActionButtonGroup({Key? key, required this.buttons}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Wrap(
      spacing: 16,
      children: buttons,
    );
  }
} 