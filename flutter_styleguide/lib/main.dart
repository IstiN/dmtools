import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'styleguide_app.dart';
import 'theme/app_theme.dart';

void main() {
  runApp(
    ChangeNotifierProvider(
      create: (_) => ThemeProvider(),
      child: const StyleguideApp(),
    ),
  );
} 