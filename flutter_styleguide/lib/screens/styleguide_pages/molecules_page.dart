import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../theme/app_theme.dart';
import '../../widgets/styleguide/component_display.dart';
import '../../widgets/styleguide/theme_switch.dart';
import '../../widgets/molecules/custom_card.dart';
import '../../widgets/molecules/search_form.dart';
import '../../widgets/molecules/section_header.dart';
import '../../widgets/atoms/view_all_link.dart';
import '../../widgets/molecules/agent_card.dart';
import '../../core/models/agent.dart';
import '../../widgets/molecules/login_provider_selector.dart';
import '../../widgets/molecules/application_item.dart';
import '../../widgets/molecules/empty_state.dart';
import '../../widgets/molecules/chat_message.dart';
import '../../widgets/molecules/chat_input_group.dart';
import '../../widgets/molecules/action_button_group.dart';
import '../../widgets/atoms/app_button.dart';
import '../../widgets/molecules/notification_message.dart';
import '../../widgets/molecules/user_profile_button.dart';
import '../../theme/app_colors.dart';
import '../../widgets/atoms/status_dot.dart';

class MoleculesPage extends StatelessWidget {
  const MoleculesPage({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final themeProvider = Provider.of<ThemeProvider>(context);

    return ListView(
      padding: const EdgeInsets.all(16.0),
      children: [
        ComponentDisplay(
          title: 'Theme Switch',
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Text('The theme switch component allows users to toggle between light and dark modes.'),
              const SizedBox(height: 16),
              ThemeSwitch(
                isDarkMode: themeProvider.isDarkMode,
                onToggle: () {
                  Provider.of<ThemeProvider>(context, listen: false).toggleTheme();
                },
              ),
            ],
          ),
        ),
        const SizedBox(height: 32),
        ComponentDisplay(
          title: 'Card',
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Text('Cards are used to group related information. The base style provides background, border, shadow, and rounded corners.'),
              const SizedBox(height: 16),
              SizedBox(
                width: 300,
                child: CustomCard(
                  child: Padding(
                    padding: const EdgeInsets.all(16.0),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text('Card Title', style: Theme.of(context).textTheme.titleLarge),
                        const SizedBox(height: 8),
                        Text('This is some content within a basic card.', style: Theme.of(context).textTheme.bodyMedium),
                      ],
                    ),
                  ),
                ),
              ),
            ],
          ),
        ),
        const SizedBox(height: 32),
        const ComponentDisplay(
          title: 'Search Form',
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text('A common molecule for search functionality, combining an input field and a button.'),
              SizedBox(height: 16),
              SearchForm(),
            ],
          ),
        ),
        const SizedBox(height: 32),
        ComponentDisplay(
          title: 'Section Header',
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Text('A common pattern for section headers with a title and a "view all" link.'),
              const SizedBox(height: 16),
              SectionHeader(
                title: 'Section Title',
                onViewAll: () {},
              ),
            ],
          ),
        ),
        const SizedBox(height: 32),
        ComponentDisplay(
          title: 'Agent Card',
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Text('Cards specifically designed for displaying agent information with status, description, and actions.'),
              const SizedBox(height: 16),
              SizedBox(
                width: 400,
                child: AgentCard(
                  title: 'Sample Agent',
                  description: 'This is a sample agent description that explains what the agent does and its capabilities.',
                  status: StatusType.online,
                  statusLabel: 'Active',
                  tags: const ['Category'],
                  runCount: 5,
                  lastRunTime: 'Today',
                  onRun: () {},
                ),
              ),
            ],
          ),
        ),
        const SizedBox(height: 32),
        const ComponentDisplay(
          title: 'Login Provider Selector',
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text('A component that allows users to choose from multiple authentication providers.'),
              SizedBox(height: 16),
              LoginProviderSelector(),
            ],
          ),
        ),
        const SizedBox(height: 32),
        ComponentDisplay(
          title: 'Application Item',
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Text('List items specifically designed for displaying application information with metadata.'),
              const SizedBox(height: 16),
              ApplicationItem(onOpen: () {}),
            ],
          ),
        ),
        const SizedBox(height: 32),
        ComponentDisplay(
          title: 'Empty State',
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Text('A pattern for displaying empty states or call-to-action areas when no content is available.'),
              const SizedBox(height: 16),
              Row(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Expanded(
                    child: EmptyState(
                      icon: Icons.add,
                      title: 'Create New Agent',
                      message: 'Configure automation for your tasks',
                      onPressed: () {},
                    ),
                  ),
                  const SizedBox(width: 16),
                  Expanded(
                    child: EmptyState(
                      icon: Icons.add,
                      title: 'Create New Item',
                      message: 'Get started by creating your first item',
                      onPressed: () {},
                    ),
                  ),
                ],
              ),
            ],
          ),
        ),
        const SizedBox(height: 32),
        const ComponentDisplay(
          title: 'Chat Message',
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text('Displays a single message in a chat interface, with variations for sender.'),
              SizedBox(height: 16),
              ChatMessage(
                text: 'This is a message from the user.',
                sender: MessageSender.user,
              ),
              SizedBox(height: 8),
              ChatMessage(
                text: 'This is a message from the agent.',
                sender: MessageSender.agent,
              ),
              SizedBox(height: 8),
              ChatMessage(
                text: 'This is a system notification.',
                sender: MessageSender.system,
              ),
            ],
          ),
        ),
        const SizedBox(height: 32),
        const ComponentDisplay(
          title: 'Chat Input Group',
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text('A group containing a textarea for message input and action buttons like send or attach.'),
              SizedBox(height: 16),
              ChatInputGroup(),
            ],
          ),
        ),
        const SizedBox(height: 32),
        ComponentDisplay(
          title: 'Action Button Group',
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Text('A simple horizontal group of action buttons, typically used for form submissions or main actions in a section.'),
              const SizedBox(height: 16),
              ActionButtonGroup(
                buttons: [
                  AppButton(
                    text: 'Generate Action',
                    onPressed: () {},
                    variant: ButtonVariant.secondary,
                  ),
                  AppButton(
                    text: 'Save Action',
                    onPressed: () {},
                    variant: ButtonVariant.primary,
                  ),
                ],
              ),
            ],
          ),
        ),
        const SizedBox(height: 32),
        const ComponentDisplay(
          title: 'Notification Message',
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text('Floating messages to provide feedback to the user (success, error, info, warning).'),
              SizedBox(height: 16),
              NotificationMessage(
                message: 'This is a success message!',
                type: NotificationType.success,
              ),
              SizedBox(height: 8),
              NotificationMessage(
                message: 'This is an error message!',
                type: NotificationType.error,
              ),
              SizedBox(height: 8),
              NotificationMessage(
                message: 'This is an informational message.',
                type: NotificationType.info,
              ),
              SizedBox(height: 8),
              NotificationMessage(
                message: 'This is a warning message.',
                type: NotificationType.warning,
              ),
            ],
          ),
        ),
        const SizedBox(height: 32),
        ComponentDisplay(
          title: 'User Profile Button',
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Text('Displays user information with avatar, name, and optional dropdown indicator.'),
              const SizedBox(height: 16),
              Row(
                children: [
                  UserProfileButton(
                    userName: 'John Doe',
                    avatarUrl: 'https://ui-avatars.com/api/?name=John+Doe&background=667eea&color=fff&size=48',
                    onPressed: () {},
                  ),
                  const SizedBox(width: 16),
                  UserProfileButton(
                    userName: 'Jane Smith',
                    onPressed: () {},
                  ),
                ],
              ),
            ],
          ),
        ),
      ],
    );
  }
} 