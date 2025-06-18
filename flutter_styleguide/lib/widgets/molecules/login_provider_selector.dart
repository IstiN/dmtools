import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:dmtools_styleguide/widgets/molecules/custom_card.dart';

class LoginProviderSelector extends StatefulWidget {
  const LoginProviderSelector({Key? key}) : super(key: key);

  @override
  _LoginProviderSelectorState createState() => _LoginProviderSelectorState();
}

class _LoginProviderSelectorState extends State<LoginProviderSelector> {
  late TapGestureRecognizer _termsRecognizer;
  late TapGestureRecognizer _privacyRecognizer;

  @override
  void initState() {
    super.initState();
    _termsRecognizer = TapGestureRecognizer()..onTap = _handleTermsTap;
    _privacyRecognizer = TapGestureRecognizer()..onTap = _handlePrivacyTap;
  }

  @override
  void dispose() {
    _termsRecognizer.dispose();
    _privacyRecognizer.dispose();
    super.dispose();
  }

  void _handleTermsTap() {
    // TODO: Implement navigation to Terms page
    print('Terms tapped');
  }

  void _handlePrivacyTap() {
    // TODO: Implement navigation to Privacy Policy page
    print('Privacy Policy tapped');
  }

  Widget _buildProviderButton({
    required BuildContext context,
    required String text,
    required IconData icon,
    required Color iconColor,
    required Color borderColor,
    required VoidCallback onPressed,
  }) {
    final theme = Theme.of(context);
    return SizedBox(
      width: double.infinity,
      child: ElevatedButton.icon(
        icon: Icon(icon, color: iconColor),
        label: Text(text),
        onPressed: onPressed,
        style: ElevatedButton.styleFrom(
          padding: const EdgeInsets.symmetric(vertical: 16),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(8),
            side: BorderSide(color: borderColor),
          ),
          backgroundColor: theme.cardColor,
          foregroundColor: theme.textTheme.bodyLarge?.color,
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.textTheme;

    return CustomCard(
      padding: const EdgeInsets.all(32),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Text('Welcome Back', style: textTheme.headlineSmall),
          const SizedBox(height: 8),
          Text(
            'Choose your preferred login method',
            style: textTheme.titleMedium?.copyWith(color: theme.hintColor),
          ),
          const SizedBox(height: 32),
          _buildProviderButton(
            context: context,
            text: 'Continue with Google',
            icon: Icons.g_mobiledata, // Placeholder
            iconColor: Colors.red,
            borderColor: Colors.red.withOpacity(0.5),
            onPressed: () {},
          ),
          const SizedBox(height: 16),
          _buildProviderButton(
            context: context,
            text: 'Continue with Microsoft',
            icon: Icons.window, // Placeholder
            iconColor: Colors.blue,
            borderColor: Colors.blue.withOpacity(0.5),
            onPressed: () {},
          ),
          const SizedBox(height: 16),
          _buildProviderButton(
            context: context,
            text: 'Continue with GitHub',
            icon: Icons.code, // Placeholder
            iconColor: theme.colorScheme.onSurface,
            borderColor: theme.colorScheme.onSurface.withOpacity(0.5),
            onPressed: () {},
          ),
          const SizedBox(height: 24),
          Row(
            children: [
              const Expanded(child: Divider()),
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 16.0),
                child: Text('or', style: textTheme.bodySmall),
              ),
              const Expanded(child: Divider()),
            ],
          ),
          const SizedBox(height: 24),
          SizedBox(
            width: double.infinity,
            child: ElevatedButton.icon(
              icon: const Icon(Icons.vpn_key),
              label: const Text('Custom OAuth Provider'),
              onPressed: () {},
              style: ElevatedButton.styleFrom(
                padding: const EdgeInsets.symmetric(vertical: 16),
              ),
            ),
          ),
          const SizedBox(height: 32),
          RichText(
            textAlign: TextAlign.center,
            text: TextSpan(
              style: textTheme.bodySmall,
              children: [
                const TextSpan(text: 'By continuing, you agree to our '),
                TextSpan(
                  text: 'Terms',
                  style: TextStyle(
                    color: theme.colorScheme.primary,
                    decoration: TextDecoration.underline,
                  ),
                  recognizer: _termsRecognizer,
                ),
                const TextSpan(text: ' and '),
                TextSpan(
                  text: 'Privacy Policy',
                  style: TextStyle(
                    color: theme.colorScheme.primary,
                    decoration: TextDecoration.underline,
                  ),
                  recognizer: _privacyRecognizer,
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
} 