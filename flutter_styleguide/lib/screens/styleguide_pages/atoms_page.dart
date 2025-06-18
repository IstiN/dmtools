import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../theme/app_theme.dart';
import '../../theme/app_colors.dart';
import '../../widgets/styleguide/component_display.dart';
import '../../widgets/styleguide/component_item.dart';
import '../../widgets/atoms/app_button.dart';
import '../../widgets/atoms/status_dot.dart';
import '../../widgets/atoms/tag_chip.dart';
import '../../widgets/atoms/form_elements.dart';
import '../../widgets/atoms/view_all_link.dart';

class AtomsPage extends StatefulWidget {
  const AtomsPage({Key? key}) : super(key: key);

  @override
  _AtomsPageState createState() => _AtomsPageState();
}

class _AtomsPageState extends State<AtomsPage> {
  String _dropdownValue = 'Option 1';
  bool _checkbox1Value = false;
  bool _checkbox2Value = true;
  String _radioValue = 'B';

  @override
  Widget build(BuildContext context) {
    final themeProvider = Provider.of<ThemeProvider>(context);
    final isDarkMode = themeProvider.isDarkMode;

    return ListView(
      padding: const EdgeInsets.all(16.0),
      children: [
        ComponentDisplay(
          title: 'Buttons',
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const AppButton(text: 'Primary', variant: ButtonVariant.primary),
              const SizedBox(height: 8),
              const AppButton(text: 'Secondary', variant: ButtonVariant.secondary),
              const SizedBox(height: 8),
              const AppButton(text: 'Outline', variant: ButtonVariant.outline),
              const SizedBox(height: 8),
              const AppButton(text: 'Text', variant: ButtonVariant.text),
              const SizedBox(height: 16),
              const AppButton(text: 'Base Button', variant: ButtonVariant.base),
              const SizedBox(height: 8),
              const AppButton(
                text: 'Run',
                variant: ButtonVariant.run,
                icon: Icons.play_arrow,
              ),
              const SizedBox(height: 16),
              Wrap(
                spacing: 16,
                runSpacing: 16,
                crossAxisAlignment: WrapCrossAlignment.center,
                children: [
                  const AppButton(
                    text: 'Settings',
                    variant: ButtonVariant.icon,
                    icon: Icons.settings,
                  ),
                  const Text('Dark Theme:'),
                  Theme(
                    data: ThemeData.dark(),
                    child: const AppButton(
                      text: 'Settings',
                      variant: ButtonVariant.icon,
                      icon: Icons.settings,
                    ),
                  ),
                  const AppButton(
                    text: 'Loading',
                    variant: ButtonVariant.primary,
                    isLoading: true,
                  ),
                  const AppButton(
                    text: 'Disabled',
                    variant: ButtonVariant.primary,
                    isDisabled: true,
                  ),
                ],
              ),
            ],
          ),
        ),
        const SizedBox(height: 32),
        ComponentDisplay(
          title: 'Form Inputs',
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              SizedBox(
                width: 300,
                child: FormGroup(
                  label: 'Text Label',
                  child: const TextInput(
                    placeholder: 'Enter text...',
                  ),
                ),
              ),
              const SizedBox(height: 24),
              SizedBox(
                width: 300,
                child: FormGroup(
                  label: 'Password Label',
                  child: const PasswordInput(
                    placeholder: 'Enter password...',
                  ),
                ),
              ),
              const SizedBox(height: 24),
              SizedBox(
                width: 300,
                child: FormGroup(
                  label: 'Select Label',
                  child: SelectDropdown(
                    value: _dropdownValue,
                    items: ['Option 1', 'Option 2', 'Option 3']
                        .map((String value) => DropdownMenuItem<String>(
                              value: value,
                              child: Text(value),
                            ))
                        .toList(),
                    onChanged: (value) {
                      setState(() {
                        _dropdownValue = value ?? 'Option 1';
                      });
                    },
                  ),
                ),
              ),
              const SizedBox(height: 24),
              ComponentItem(
                title: 'Checkbox',
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    CheckboxInput(
                      label: 'Option 1 (Unchecked)',
                      value: _checkbox1Value,
                      onChanged: (value) {
                        setState(() {
                          _checkbox1Value = value ?? false;
                        });
                      },
                    ),
                    CheckboxInput(
                      label: 'Option 2 (Checked)',
                      value: _checkbox2Value,
                      onChanged: (value) {
                        setState(() {
                          _checkbox2Value = value ?? true;
                        });
                      },
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 24),
              ComponentItem(
                title: 'Radio Buttons',
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    RadioInput(
                      label: 'Option A',
                      value: 'A',
                      groupValue: _radioValue,
                      onChanged: (value) {
                        setState(() {
                          _radioValue = value ?? 'A';
                        });
                      },
                    ),
                    RadioInput(
                      label: 'Option B',
                      value: 'B',
                      groupValue: _radioValue,
                      onChanged: (value) {
                        setState(() {
                          _radioValue = value ?? 'B';
                        });
                      },
                    ),
                  ],
                ),
              ),
            ],
          ),
        ),
        const SizedBox(height: 32),
        const ComponentDisplay(
          title: 'Tags & Status',
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              ComponentItem(
                title: 'Status Dots',
                child: Wrap(
                  spacing: 24,
                  runSpacing: 16,
                  crossAxisAlignment: WrapCrossAlignment.center,
                  children: [
                    StatusDot(status: StatusType.online, showLabel: true),
                    StatusDot(status: StatusType.offline, showLabel: true),
                    StatusDot(status: StatusType.warning, showLabel: true),
                    StatusDot(status: StatusType.error, showLabel: true),
                  ],
                ),
              ),
              SizedBox(height: 24),
              ComponentItem(
                title: 'Tags',
                child: Wrap(
                  spacing: 8,
                  runSpacing: 8,
                  children: [
                    const TagChip(label: 'Default'),
                    const TagChip(label: 'Primary', variant: TagChipVariant.primary),
                    const TagChip(label: 'Success', variant: TagChipVariant.success),
                    const TagChip(label: 'Warning', variant: TagChipVariant.warning),
                    const TagChip(label: 'Danger', variant: TagChipVariant.danger),
                    const TagChip(
                      label: 'Removable',
                      variant: TagChipVariant.primary,
                      // onDeleted: () {}, // Temporarily removed
                    ),
                  ],
                ),
              ),
            ],
          ),
        ),
        const SizedBox(height: 32),
        const ComponentDisplay(
          title: 'Links',
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              ComponentItem(
                title: 'View All Link',
                child: ViewAllLink(
                  text: 'View all items',
                ),
              ),
            ],
          ),
        ),
      ],
    );
  }
} 