import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:golden_toolkit/golden_toolkit.dart';
import 'package:dmtools_styleguide/widgets/organisms/page_header.dart';
import 'package:dmtools_styleguide/widgets/organisms/welcome_banner.dart';
import 'package:dmtools_styleguide/widgets/organisms/chat_module.dart';
import '../golden_test_helper.dart';

void main() {
  group('Organisms Golden Tests - Individual Components', () {
    testGoldens('Page Header', (WidgetTester tester) async {
      await GoldenTestHelper.testWidgetInBothThemes(
        tester: tester,
        name: 'page_header',
        widget: PageHeader(
          title: 'DMTools Styleguide',
          onThemeToggle: () {},
          isTestMode: true,
          testDarkMode: false,
        ),
      );
    });

    testGoldens('Welcome Banner', (WidgetTester tester) async {
      await GoldenTestHelper.testWidgetInBothThemes(
        tester: tester,
        name: 'welcome_banner',
        widget: WelcomeBanner(
          title: 'Welcome!',
          subtitle: 'This is a subtitle',
          description: 'This is a description of the banner.',
          primaryAction: () {},
          secondaryAction: () {},
          isTestMode: true,
          testDarkMode: false,
        ),
      );
    });

    testGoldens('Chat Module', (WidgetTester tester) async {
      await GoldenTestHelper.testWidgetInBothThemes(
        tester: tester,
        name: 'chat_module',
        widget: ChatModule(
          messages: [
            ChatMessage(text: 'Hello!', isUser: false),
            ChatMessage(text: 'Hi there!', isUser: true),
          ],
          onSendMessage: (_) {},
          isTestMode: true,
          testDarkMode: false,
        ),
        height: 500,
      );
    });
  });

  group('Organisms Golden Tests - Collection', () {
    testGoldens('All Organisms', (WidgetTester tester) async {
      final lightThemeBuilder = GoldenTestHelper.createDeviceBuilder(
        name: 'All Organisms Light',
        isDarkMode: false,
        widgets: [
          PageHeader(
            title: 'DMTools Styleguide',
            onThemeToggle: () {},
            isTestMode: true,
            testDarkMode: false,
          ),
          const SizedBox(height: 16),
          WelcomeBanner(
            title: 'Welcome!',
            subtitle: 'This is a subtitle',
            description: 'This is a description of the banner.',
            primaryAction: () {},
            secondaryAction: () {},
            isTestMode: true,
            testDarkMode: false,
          ),
          const SizedBox(height: 16),
          ChatModule(
            messages: [
              ChatMessage(text: 'Hello!', isUser: false),
              ChatMessage(text: 'Hi there!', isUser: true),
            ],
            onSendMessage: (_) {},
            isTestMode: true,
            testDarkMode: false,
          ),
        ],
      );

      final darkThemeBuilder = GoldenTestHelper.createDeviceBuilder(
        name: 'All Organisms Dark',
        isDarkMode: true,
        widgets: [
          PageHeader(
            title: 'DMTools Styleguide',
            onThemeToggle: () {},
            isTestMode: true,
            testDarkMode: true,
          ),
          const SizedBox(height: 16),
          WelcomeBanner(
            title: 'Welcome!',
            subtitle: 'This is a subtitle',
            description: 'This is a description of the banner.',
            primaryAction: () {},
            secondaryAction: () {},
            isTestMode: true,
            testDarkMode: true,
          ),
          const SizedBox(height: 16),
          ChatModule(
            messages: [
              ChatMessage(text: 'Hello!', isUser: false),
              ChatMessage(text: 'Hi there!', isUser: true),
            ],
            onSendMessage: (_) {},
            isTestMode: true,
            testDarkMode: true,
          ),
        ],
      );

      await tester.pumpDeviceBuilder(lightThemeBuilder);
      await screenMatchesGolden(tester, 'goldens/all_organisms_light');

      await tester.pumpDeviceBuilder(darkThemeBuilder);
      await screenMatchesGolden(tester, 'goldens/all_organisms_dark');
    });
  });
} 