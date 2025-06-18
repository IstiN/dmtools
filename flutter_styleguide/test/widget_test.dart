// This is a basic Flutter widget test.
//
// To perform an interaction with a widget in your test, use the WidgetTester
// utility in the flutter_test package. For example, you can send tap and scroll
// gestures. You can also use WidgetTester to find child widgets in the widget
// tree, read text, and verify that the values of widget properties are correct.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:provider/provider.dart';

import 'package:dmtools_styleguide/styleguide_app.dart';
import 'package:dmtools_styleguide/theme/app_theme.dart';

void main() {
  testWidgets('Styleguide app loads correctly', (WidgetTester tester) async {
    // Build our app and trigger a frame.
    await tester.pumpWidget(
      ChangeNotifierProvider(
        create: (_) => ThemeProvider(),
        child: const StyleguideApp(),
      ),
    );

    // Verify app loads without errors - just check basic structure
    expect(find.byType(MaterialApp), findsOneWidget);
    
    // Verify that a scaffold exists (basic app structure)
    expect(find.byType(Scaffold), findsOneWidget);
    
    // Ignore network image errors in the test
    tester.takeException();
  });
}
