import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:provider/provider.dart';
import '../../theme/app_theme.dart';
import '../../theme/app_colors.dart';
import '../../widgets/styleguide/color_swatch.dart';

class ColorsTypographyPage extends StatelessWidget {
  const ColorsTypographyPage({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final themeProvider = Provider.of<ThemeProvider>(context);
    final isDarkMode = themeProvider.isDarkMode;
    final ThemeColorSet colors = isDarkMode ? AppColors.dark : AppColors.light;
    
    return SingleChildScrollView(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Padding(
            padding: const EdgeInsets.all(24.0),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  'Color Palette (from CSS Variables)',
                  style: Theme.of(context).textTheme.headlineMedium,
                ),
                const SizedBox(height: 24),
                
                // Base Colors
                Text(
                  'Base Colors',
                  style: Theme.of(context).textTheme.titleLarge,
                ),
                const SizedBox(height: 16),
                Wrap(
                  spacing: 16,
                  runSpacing: 16,
                  children: [
                    ColorSwatchItem(
                      name: '--bg-color',
                      color: colors.bgColor,
                      hexCode: isDarkMode ? '#2D2E30' : '#F8F9FA',
                    ),
                    ColorSwatchItem(
                      name: '--card-bg',
                      color: colors.cardBg,
                      hexCode: isDarkMode ? '#1E1F21' : '#FFFFFF',
                    ),
                    ColorSwatchItem(
                      name: '--text-color',
                      color: colors.textColor,
                      hexCode: isDarkMode ? '#FFFFFF' : '#212529',
                    ),
                    ColorSwatchItem(
                      name: '--text-secondary',
                      color: colors.textSecondary,
                      hexCode: isDarkMode ? '#E9ECEF' : '#343A40',
                    ),
                    ColorSwatchItem(
                      name: '--text-muted',
                      color: colors.textMuted,
                      hexCode: isDarkMode ? '#ADB5BD' : '#6C757D',
                    ),
                    ColorSwatchItem(
                      name: '--border-color',
                      color: colors.borderColor,
                      hexCode: isDarkMode ? '#495057' : '#DFE1E5',
                    ),
                  ],
                ),
                
                const SizedBox(height: 32),
                
                // Accent Colors
                Text(
                  'Accent Colors',
                  style: Theme.of(context).textTheme.titleLarge,
                ),
                const SizedBox(height: 16),
                Wrap(
                  spacing: 16,
                  runSpacing: 16,
                  children: [
                    ColorSwatchItem(
                      name: '--accent-color',
                      color: colors.accentColor,
                      hexCode: '#6078F0',
                    ),
                    ColorSwatchItem(
                      name: '--accent-light',
                      color: colors.accentLight,
                      hexCode: '#E8EBFD',
                    ),
                    ColorSwatchItem(
                      name: '--accent-hover',
                      color: colors.accentHover,
                      hexCode: '#4A61C0',
                    ),
                  ],
                ),
                
                const SizedBox(height: 32),
                
                // Button & Interaction Colors
                Text(
                  'Button & Interaction Colors',
                  style: Theme.of(context).textTheme.titleLarge,
                ),
                const SizedBox(height: 16),
                Wrap(
                  spacing: 16,
                  runSpacing: 16,
                  children: [
                    ColorSwatchItem(
                      name: '--button-bg',
                      color: colors.buttonBg,
                      hexCode: '#6078F0',
                    ),
                    ColorSwatchItem(
                      name: '--button-hover',
                      color: colors.buttonHover,
                      hexCode: '#4A61C0',
                    ),
                    ColorSwatchItem(
                      name: '--hover-bg',
                      color: colors.hoverBg,
                      hexCode: isDarkMode ? '#495057' : '#F8F9FA',
                    ),
                  ],
                ),
                
                const SizedBox(height: 32),
                
                // Feedback Colors
                Text(
                  'Feedback Colors',
                  style: Theme.of(context).textTheme.titleLarge,
                ),
                const SizedBox(height: 16),
                Wrap(
                  spacing: 16,
                  runSpacing: 16,
                  children: [
                    ColorSwatchItem(
                      name: '--success-color',
                      color: colors.successColor,
                      hexCode: '#28A745',
                    ),
                    ColorSwatchItem(
                      name: '--warning-color',
                      color: colors.warningColor,
                      hexCode: '#FFC107',
                    ),
                    ColorSwatchItem(
                      name: '--danger-color',
                      color: colors.dangerColor,
                      hexCode: '#DC3545',
                    ),
                    ColorSwatchItem(
                      name: '--info-color',
                      color: colors.infoColor,
                      hexCode: '#17A2B8',
                    ),
                  ],
                ),
                
                const SizedBox(height: 32),
                
                // Input Colors
                Text(
                  'Input Colors',
                  style: Theme.of(context).textTheme.titleLarge,
                ),
                const SizedBox(height: 16),
                Wrap(
                  spacing: 16,
                  runSpacing: 16,
                  children: [
                    ColorSwatchItem(
                      name: '--input-bg',
                      color: colors.inputBg,
                      hexCode: isDarkMode ? '#343A40' : '#FFFFFF',
                    ),
                    ColorSwatchItem(
                      name: '--input-focus-border',
                      color: colors.inputFocusBorder,
                      hexCode: '#6078F0',
                    ),
                  ],
                ),
                
                const SizedBox(height: 48),
                
                // Typography Section
                Text(
                  'Typography',
                  style: Theme.of(context).textTheme.headlineMedium,
                ),
                const SizedBox(height: 16),
                Text(
                  'Base font family: "Inter", "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif',
                  style: GoogleFonts.inter(
                    color: colors.textColor,
                    fontSize: 14,
                  ),
                ),
                const SizedBox(height: 32),
                
                // Heading Examples
                Card(
                  color: colors.cardBg,
                  elevation: 0,
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(8),
                    side: BorderSide(color: colors.borderColor),
                  ),
                  child: Padding(
                    padding: const EdgeInsets.all(24.0),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          'Heading 1',
                          style: GoogleFonts.inter(
                            fontSize: 32,
                            fontWeight: FontWeight.bold,
                            color: colors.textColor,
                          ),
                        ),
                        const SizedBox(height: 16),
                        Text(
                          'Heading 2',
                          style: GoogleFonts.inter(
                            fontSize: 28,
                            fontWeight: FontWeight.bold,
                            color: colors.textColor,
                          ),
                        ),
                        const SizedBox(height: 16),
                        Divider(color: colors.borderColor),
                        const SizedBox(height: 16),
                        Text(
                          'Heading 3',
                          style: GoogleFonts.inter(
                            fontSize: 24,
                            fontWeight: FontWeight.bold,
                            color: colors.textColor,
                          ),
                        ),
                        const SizedBox(height: 16),
                        Text(
                          'Heading 4',
                          style: GoogleFonts.inter(
                            fontSize: 20,
                            fontWeight: FontWeight.bold,
                            color: colors.textColor,
                          ),
                        ),
                        const SizedBox(height: 8),
                        Text(
                          'Heading 5',
                          style: GoogleFonts.inter(
                            fontSize: 16,
                            fontWeight: FontWeight.bold,
                            color: colors.textColor,
                          ),
                        ),
                        const SizedBox(height: 8),
                        Text(
                          'Heading 6',
                          style: GoogleFonts.inter(
                            fontSize: 14,
                            fontWeight: FontWeight.bold,
                            color: colors.textColor,
                          ),
                        ),
                        const SizedBox(height: 16),
                        Text(
                          'This is a standard paragraph. Lorem ipsum dolor sit amet, consectetur adipiscing elit.',
                          style: GoogleFonts.inter(
                            fontSize: 16,
                            color: colors.textColor,
                          ),
                        ),
                        const SizedBox(height: 8),
                        Text(
                          'This is small text, often used for less important details or var(--text-muted).',
                          style: GoogleFonts.inter(
                            fontSize: 14,
                            color: colors.textMuted,
                          ),
                        ),
                        const SizedBox(height: 8),
                        Text.rich(
                          TextSpan(
                            children: [
                              TextSpan(
                                text: 'This is a hyperlink, typically using ',
                                style: GoogleFonts.inter(
                                  fontSize: 16,
                                  color: colors.textColor,
                                ),
                              ),
                              TextSpan(
                                text: 'var(--accent-color)',
                                style: GoogleFonts.inter(
                                  fontSize: 16,
                                  color: colors.accentColor,
                                  decoration: TextDecoration.underline,
                                ),
                              ),
                              TextSpan(
                                text: '.',
                                style: GoogleFonts.inter(
                                  fontSize: 16,
                                  color: colors.textColor,
                                ),
                              ),
                            ],
                          ),
                        ),
                        const SizedBox(height: 8),
                        Text.rich(
                          TextSpan(
                            children: [
                              TextSpan(
                                text: 'This is bold text. ',
                                style: GoogleFonts.inter(
                                  fontSize: 16,
                                  fontWeight: FontWeight.bold,
                                  color: colors.textColor,
                                ),
                              ),
                              TextSpan(
                                text: 'This is italic text.',
                                style: GoogleFonts.inter(
                                  fontSize: 16,
                                  fontStyle: FontStyle.italic,
                                  color: colors.textColor,
                                ),
                              ),
                            ],
                          ),
                        ),
                        const SizedBox(height: 8),
                        Container(
                          padding: const EdgeInsets.all(8),
                          decoration: BoxDecoration(
                            color: isDarkMode ? const Color(0xFF343A40) : const Color(0xFFF8F9FA),
                            borderRadius: BorderRadius.circular(4),
                          ),
                          child: Text(
                            'This is inline code.',
                            style: GoogleFonts.sourceCodePro(
                              fontSize: 14,
                              color: colors.textColor,
                            ),
                          ),
                        ),
                        const SizedBox(height: 8),
                        Container(
                          width: double.infinity,
                          padding: const EdgeInsets.all(16),
                          decoration: BoxDecoration(
                            color: isDarkMode ? const Color(0xFF343A40) : const Color(0xFFF8F9FA),
                            borderRadius: BorderRadius.circular(4),
                            border: Border.all(color: colors.borderColor),
                          ),
                          child: Text(
                            'This is a blockquote. It can be used to highlight a section of text.',
                            style: GoogleFonts.inter(
                              fontSize: 16,
                              fontStyle: FontStyle.italic,
                              color: colors.textColor,
                            ),
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class _ColorInfo {
  final String name;
  final Color color;
  final String hexCode;
  final String description;
  
  _ColorInfo({
    required this.name,
    required this.color,
    required this.hexCode,
    required this.description,
  });
} 