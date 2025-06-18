import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:golden_toolkit/golden_toolkit.dart';
import 'package:dmtools_styleguide/widgets/atoms/app_button.dart';
import 'package:dmtools_styleguide/widgets/atoms/status_dot.dart';
import 'package:dmtools_styleguide/widgets/atoms/tag_chip.dart';
import 'package:dmtools_styleguide/widgets/atoms/form_elements.dart';
import 'package:dmtools_styleguide/widgets/atoms/view_all_link.dart';
import '../golden_test_helper.dart';

void main() {
  group('Atoms Golden Tests - Individual Components', () {
    testGoldens('AppButton - Primary', (WidgetTester tester) async {
      await GoldenTestHelper.testWidgetInBothThemes(
        tester: tester,
        name: 'app_button_primary',
        widget: AppButton(
          text: 'Primary Button',
          onPressed: () {},
          variant: ButtonVariant.primary,
          isTestMode: true,
          testDarkMode: false,
        ),
      );
    });

    testGoldens('AppButton - Secondary', (WidgetTester tester) async {
      await GoldenTestHelper.testWidgetInBothThemes(
        tester: tester,
        name: 'app_button_secondary',
        widget: AppButton(
          text: 'Secondary Button',
          onPressed: () {},
          variant: ButtonVariant.secondary,
          isTestMode: true,
          testDarkMode: false,
        ),
      );
    });

    testGoldens('AppButton - Base', (WidgetTester tester) async {
      await GoldenTestHelper.testWidgetInBothThemes(
        tester: tester,
        name: 'app_button_base',
        widget: AppButton(
          text: 'Base Button',
          onPressed: () {},
          variant: ButtonVariant.base,
          isTestMode: true,
          testDarkMode: false,
        ),
      );
    });

    testGoldens('AppButton - Icon', (WidgetTester tester) async {
      await GoldenTestHelper.testWidgetInBothThemes(
        tester: tester,
        name: 'app_button_icon',
        widget: AppButton(
          text: 'Icon',
          onPressed: () {},
          variant: ButtonVariant.icon,
          icon: Icons.add,
          isTestMode: true,
          testDarkMode: false,
        ),
      );
    });

    testGoldens('Status Dot - Online', (WidgetTester tester) async {
      await GoldenTestHelper.testWidgetInBothThemes(
        tester: tester,
        name: 'status_dot_online',
        widget: StatusDot(
          status: StatusType.online,
          isTestMode: true,
          testDarkMode: false,
        ),
        width: 100,
        height: 100,
      );
    });

    testGoldens('Status Dot - Warning', (WidgetTester tester) async {
      await GoldenTestHelper.testWidgetInBothThemes(
        tester: tester,
        name: 'status_dot_warning',
        widget: StatusDot(
          status: StatusType.warning,
          isTestMode: true,
          testDarkMode: false,
        ),
        width: 100,
        height: 100,
      );
    });

    testGoldens('Status Dot - Error', (WidgetTester tester) async {
      await GoldenTestHelper.testWidgetInBothThemes(
        tester: tester,
        name: 'status_dot_error',
        widget: StatusDot(
          status: StatusType.error,
          isTestMode: true,
          testDarkMode: false,
        ),
        width: 100,
        height: 100,
      );
    });

    testGoldens('Tag Chip', (WidgetTester tester) async {
      await GoldenTestHelper.testWidgetInBothThemes(
        tester: tester,
        name: 'tag_chip',
        widget: TagChip(
          label: 'Tag Label',
          isTestMode: true,
          testDarkMode: false,
        ),
        width: 150,
        height: 100,
      );
    });

    testGoldens('Form Elements - Text Input', (WidgetTester tester) async {
      await GoldenTestHelper.testWidgetInBothThemes(
        tester: tester,
        name: 'form_text_input',
        widget: TextInput(
          controller: TextEditingController(text: 'Input text'),
          placeholder: 'Placeholder text',
          isTestMode: true,
          testDarkMode: false,
        ),
        height: 100,
      );
    });

    testGoldens('Form Elements - Password Input', (WidgetTester tester) async {
      await GoldenTestHelper.testWidgetInBothThemes(
        tester: tester,
        name: 'form_password_input',
        widget: PasswordInput(
          controller: TextEditingController(text: 'password'),
          placeholder: 'Enter password',
          isTestMode: true,
          testDarkMode: false,
        ),
        height: 100,
      );
    });

    testGoldens('View All Link', (WidgetTester tester) async {
      await GoldenTestHelper.testWidgetInBothThemes(
        tester: tester,
        name: 'view_all_link',
        widget: ViewAllLink(
          text: 'View all items',
          onTap: () {},
          isTestMode: true,
          testDarkMode: false,
        ),
        width: 150,
        height: 50,
      );
    });
  });
} 