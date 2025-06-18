import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:dmtools_styleguide/theme/app_theme.dart';
import 'package:dmtools_styleguide/screens/styleguide_home.dart';

class StyleguideApp extends StatelessWidget {
  const StyleguideApp({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return ChangeNotifierProvider(
      create: (_) => ThemeProvider(),
      child: Consumer<ThemeProvider>(
        builder: (context, themeProvider, child) {
          return MaterialApp(
            title: 'DMTools Styleguide',
            theme: AppTheme.lightTheme,
            darkTheme: AppTheme.darkTheme,
            themeMode: themeProvider.currentThemeMode,
            home: const StyleguideHome(),
          );
        },
      ),
    );
  }
} 