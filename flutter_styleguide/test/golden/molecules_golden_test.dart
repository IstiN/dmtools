import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:golden_toolkit/golden_toolkit.dart';
import 'package:dmtools_styleguide/widgets/molecules/empty_state.dart';
import 'package:dmtools_styleguide/widgets/molecules/search_form.dart';
import 'package:dmtools_styleguide/widgets/molecules/agent_card.dart';
import 'package:dmtools_styleguide/widgets/molecules/section_header.dart';
import 'package:dmtools_styleguide/widgets/molecules/user_profile_button.dart';
import 'package:dmtools_styleguide/widgets/atoms/status_dot.dart';
import '../golden_test_helper.dart';

void main() {
  setUpAll(() async {
    await loadAppFonts();
  });

  group('Molecules Golden Tests - Individual Components', () {
    testGoldens('Empty State', (WidgetTester tester) async {
      await GoldenTestHelper.testWidgetInBothThemes(
        tester: tester,
        name: 'empty_state',
        widget: EmptyState(
          icon: Icons.add,
          title: 'Create New Item',
          message: 'Get started by creating your first item',
          onPressed: () {},
        ),
        width: 400,
        height: 300,
      );
    });

    testGoldens('Search Form', (WidgetTester tester) async {
      await GoldenTestHelper.testWidgetInBothThemes(
        tester: tester,
        name: 'search_form',
        widget: SearchForm(
          hintText: 'Search...',
          onSearch: (_) {},
        ),
        width: 400,
        height: 100,
      );
    });

    testGoldens('Agent Card', (WidgetTester tester) async {
      await GoldenTestHelper.testWidgetInBothThemes(
        tester: tester,
        name: 'agent_card',
        widget: AgentCard(
          title: 'Test Agent',
          description: 'This is a test agent description that might be a bit longer to show how it wraps.',
          status: StatusType.online,
          statusLabel: 'Active',
          tags: const ['AI', 'Productivity', 'Automation'],
          runCount: 42,
          lastRunTime: '2 hours ago',
          onRun: () {},
        ),
        width: 400,
        height: 300,
      );
    });

    testGoldens('Section Header', (WidgetTester tester) async {
      await GoldenTestHelper.testWidgetInBothThemes(
        tester: tester,
        name: 'section_header',
        widget: SectionHeader(
          title: 'Section Title',
          viewAllText: 'View All',
          onViewAll: () {},
        ),
        width: 400,
        height: 80,
      );
    });

    testGoldens('User Profile Button', (WidgetTester tester) async {
      await GoldenTestHelper.testWidgetInBothThemes(
        tester: tester,
        name: 'user_profile_button',
        widget: UserProfileButton(
          userName: 'John Doe',
          onPressed: () {},
        ),
        width: 200,
        height: 80,
      );
    });
  });

  group('Molecules Golden Tests - Collection', () {
    testGoldens('All Molecules', (tester) async {
      final agent = Agent(
        id: '1',
        name: 'Test Agent',
        description: 'This is a test agent description that might be a bit longer to show how it wraps.',
        category: 'Development',
        status: AgentStatus.active,
        icon: 'default-avatar.svg',
      );

      final builder = GoldenTestHelper.createDeviceBuilder(
        widgets: [
          const Text('Empty State', style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold)),
          const SizedBox(height: 16),
          EmptyState(
            icon: Icons.add,
            title: 'Create New Item',
            message: 'Get started by creating your first item',
            onPressed: () {},
          ),
          const SizedBox(height: 32),
          
          const Text('Search Form', style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold)),
          const SizedBox(height: 16),
          SearchForm(
            controller: TextEditingController(),
            onSearch: (_) {},
            hint: 'Search...',
          ),
          const SizedBox(height: 32),
          
          const Text('Section Header', style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold)),
          const SizedBox(height: 16),
          SectionHeader(
            title: 'Recent Agents',
            actionText: 'View All',
            onActionTap: () {},
          ),
          const SizedBox(height: 32),
          
          const Text('Agent Card', style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold)),
          const SizedBox(height: 16),
          AgentCard(
            agent: agent,
            onTap: () {},
          ),
          const SizedBox(height: 32),
          
          const Text('User Profile Button', style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold)),
          const SizedBox(height: 16),
          UserProfileButton(
            username: 'John Doe',
            onTap: () {},
          ),
        ],
        name: 'All Molecules',
        isDarkMode: false,
      );

      await tester.pumpDeviceBuilder(builder);
      await screenMatchesGolden(tester, 'molecules_collection_light');

      // Dark theme
      final darkBuilder = GoldenTestHelper.createDeviceBuilder(
        widgets: [
          const Text('Empty State', style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold)),
          const SizedBox(height: 16),
          EmptyState(
            icon: Icons.add,
            title: 'Create New Item',
            message: 'Get started by creating your first item',
            onPressed: () {},
          ),
          const SizedBox(height: 32),
          
          const Text('Search Form', style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold)),
          const SizedBox(height: 16),
          SearchForm(
            controller: TextEditingController(),
            onSearch: (_) {},
            hint: 'Search...',
          ),
          const SizedBox(height: 32),
          
          const Text('Section Header', style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold)),
          const SizedBox(height: 16),
          SectionHeader(
            title: 'Recent Agents',
            actionText: 'View All',
            onActionTap: () {},
          ),
          const SizedBox(height: 32),
          
          const Text('Agent Card', style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold)),
          const SizedBox(height: 16),
          AgentCard(
            agent: agent,
            onTap: () {},
          ),
          const SizedBox(height: 32),
          
          const Text('User Profile Button', style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold)),
          const SizedBox(height: 16),
          UserProfileButton(
            username: 'John Doe',
            onTap: () {},
          ),
        ],
        name: 'All Molecules',
        isDarkMode: true,
      );

      await tester.pumpDeviceBuilder(darkBuilder);
      await screenMatchesGolden(tester, 'molecules_collection_dark');
    });
  });
} 