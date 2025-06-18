import 'package:flutter/material.dart';
import 'package:flutter_svg/flutter_svg.dart';
import 'package:provider/provider.dart';
import '../theme/app_theme.dart';
import '../theme/app_colors.dart';
import '../widgets/styleguide/theme_switch.dart';
import '../widgets/molecules/user_profile_button.dart';
import '../widgets/molecules/search_form.dart';
import 'styleguide_pages/colors_typography_page.dart';
import 'styleguide_pages/atoms_page.dart';
import 'styleguide_pages/molecules_page.dart';
import 'styleguide_pages/organisms_page.dart';
import 'styleguide_pages/icons_logos_page.dart';

class StyleguideHome extends StatefulWidget {
  const StyleguideHome({super.key});

  @override
  State<StyleguideHome> createState() => _StyleguideHomeState();
}

class _NavigationItem {
  final IconData icon;
  final String label;
  final int? badgeCount;

  const _NavigationItem({
    required this.icon, 
    required this.label, 
    this.badgeCount,
  });
}

class _StyleguideHomeState extends State<StyleguideHome> {
  int _selectedIndex = 0;
  
  final List<Widget> _pages = [
    const WelcomePage(),
    const ColorsTypographyPage(),
    const AtomsPage(),
    const MoleculesPage(),
    const OrganismsPage(),
    const IconsLogosPage(),
  ];
  
  final List<String> _pageTitles = [
    'Welcome',
    'Colors & Typography',
    'Atoms',
    'Molecules',
    'Organisms',
    'Icons & Logos',
  ];

  final List<_NavigationItem> _navItems = [
    _NavigationItem(icon: Icons.home_outlined, label: 'Welcome'),
    _NavigationItem(icon: Icons.palette_outlined, label: 'Colors & Typography'),
    _NavigationItem(icon: Icons.grain_outlined, label: 'Atoms'),
    _NavigationItem(icon: Icons.view_module_outlined, label: 'Molecules'),
    _NavigationItem(icon: Icons.view_quilt_outlined, label: 'Organisms'),
    _NavigationItem(icon: Icons.image_outlined, label: 'Icons & Logos'),
  ];

  @override
  Widget build(BuildContext context) {
    return LayoutBuilder(
      builder: (context, constraints) {
        if (constraints.maxWidth > 768) {
          // Desktop layout
          return _buildDesktopLayout();
        } else {
          // Mobile layout
          return _buildMobileLayout();
        }
      },
    );
  }

  Widget _buildDesktopLayout() {
    final theme = Theme.of(context);
    final themeProvider = Provider.of<ThemeProvider>(context);
    final isDarkMode = themeProvider.isDarkMode;

    return Scaffold(
      appBar: AppBar(
        automaticallyImplyLeading: false,
        toolbarHeight: 56,
        title: Row(
          children: [
            SvgPicture.asset(
              isDarkMode 
                ? 'assets/img/dmtools-logo-network-nodes-dark.svg'
                : 'assets/img/dmtools-logo-network-nodes.svg', 
              height: 40,
              placeholderBuilder: (BuildContext context) => Container(
                height: 40,
                width: 120,
                color: Colors.transparent,
                child: Center(
                  child: Text(
                    'DM Tools',
                    style: TextStyle(
                      fontSize: 18,
                      fontWeight: FontWeight.bold,
                      color: isDarkMode ? Colors.white : Colors.black,
                    ),
                  ),
                ),
              ),
            ),
            const SizedBox(width: 64),
            Expanded(
              child: Center(
                child: SizedBox(
                  width: 400,
                  child: _buildSearchBar(isDarkMode),
                ),
              ),
            ),
          ],
        ),
        actions: [
          IconButton(
            icon: const Icon(Icons.settings_outlined),
            onPressed: () {},
            tooltip: 'Settings',
          ),
          ThemeSwitch(
            isDarkMode: isDarkMode,
            onToggle: () => themeProvider.toggleTheme(),
          ),
          Container(
            margin: const EdgeInsets.symmetric(vertical: 8, horizontal: 4),
            decoration: BoxDecoration(
              border: Border.all(color: theme.dividerColor),
              borderRadius: BorderRadius.circular(8),
            ),
            child: const UserProfileButton(
              userName: 'Vladimir Klysh...',
              avatarUrl: 'https://ui-avatars.com/api/?name=Vladimir+Klyshevich&background=667eea&color=fff&size=48',
            ),
          ),
          const SizedBox(width: 8),
        ],
      ),
      body: Row(
        children: [
          _buildSidebar(context, isMobile: false),
          Container(
            width: 1,
            color: isDarkMode ? const Color(0xFF2A2A2A) : const Color(0xFFEAEDF1),
          ),
          Expanded(
            child: Padding(
              padding: const EdgeInsets.all(24.0),
              child: _pages[_selectedIndex],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildSearchBar(bool isDarkMode) {
    return SizedBox(
      height: 40,
      child: SearchForm(
        hintText: 'Search agents and apps...',
        onSearch: (query) {
          // Handle search
          debugPrint('Searching for: $query');
        },
      ),
    );
  }

  Widget _buildMobileLayout() {
    final themeProvider = Provider.of<ThemeProvider>(context);
    final isDarkMode = themeProvider.isDarkMode;

    return Scaffold(
      appBar: AppBar(
        toolbarHeight: 56,
        title: SvgPicture.asset(
          isDarkMode 
            ? 'assets/img/dmtools-logo-network-nodes-dark.svg'
            : 'assets/img/dmtools-logo-network-nodes.svg',
          height: 36,
          placeholderBuilder: (BuildContext context) => Container(
            height: 36,
            width: 110,
            color: Colors.transparent,
            child: Center(
              child: Text(
                'DM Tools',
                style: TextStyle(
                  fontSize: 16,
                  fontWeight: FontWeight.bold,
                  color: isDarkMode ? Colors.white : Colors.black,
                ),
              ),
            ),
          ),
        ),
        actions: [
          ThemeSwitch(
            isDarkMode: isDarkMode,
            onToggle: () => themeProvider.toggleTheme(),
          ),
          const SizedBox(width: 8),
        ],
      ),
      drawer: Drawer(
        child: _buildSidebar(context, isMobile: true),
      ),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: _pages[_selectedIndex],
      ),
    );
  }
  
  Widget _buildSidebar(BuildContext context, {required bool isMobile}) {
    final theme = Theme.of(context);
    final themeProvider = Provider.of<ThemeProvider>(context);
    final isDarkMode = themeProvider.isDarkMode;
    
    final Color bgColor = isDarkMode 
        ? const Color(0xFF202124)
        : Colors.white;
    
    final Color textColor = isDarkMode 
        ? Colors.white70
        : const Color(0xFF495057);
    
    final Color headerColor = isDarkMode 
        ? Colors.white 
        : const Color(0xFF212529);
    
    final Color dividerColor = isDarkMode
        ? const Color(0xFF2A2A2A) 
        : const Color(0xFFEAEDF1);
    
    return Container(
      width: 240,
      color: bgColor,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          if (isMobile) ...[
            Container(
              padding: const EdgeInsets.all(12),
              child: SvgPicture.asset(
                isDarkMode 
                  ? 'assets/img/dmtools-logo-network-nodes-dark.svg'
                  : 'assets/img/dmtools-logo-network-nodes.svg',
                height: 40,
                placeholderBuilder: (BuildContext context) => Container(
                  height: 40,
                  width: 120,
                  color: Colors.transparent,
                  child: Center(
                    child: Text(
                      'DM Tools',
                      style: TextStyle(
                        fontSize: 18,
                        fontWeight: FontWeight.bold,
                        color: isDarkMode ? Colors.white : Colors.black,
                      ),
                    ),
                  ),
                ),
              ),
            ),
            Divider(color: dividerColor, height: 1),
          ],
          const SizedBox(height: 24),
          Expanded(
            child: ListView(
              padding: EdgeInsets.zero,
              children: [
                for (int i = 0; i < _navItems.length; i++)
                  _buildNavItem(i, context, isMobile: isMobile),
              ],
            ),
          ),
          if (!isMobile) ...[
            Divider(color: dividerColor, height: 1),
            Padding(
              padding: const EdgeInsets.all(12),
              child: Text(
                '© 2025 DMTools. All rights reserved.',
                style: TextStyle(
                  fontSize: 11,
                  color: textColor,
                ),
              ),
            ),
          ],
        ],
      ),
    );
  }
  
  Widget _buildNavItem(int index, BuildContext context, {required bool isMobile}) {
    final theme = Theme.of(context);
    final themeProvider = Provider.of<ThemeProvider>(context);
    final isDarkMode = themeProvider.isDarkMode;
    final isSelected = _selectedIndex == index;
    
    final Color textColor = isDarkMode ? Colors.white70 : const Color(0xFF495057);
    final Color selectedTextColor = Colors.white;
    final Color selectedBgColor = const Color(0xFF6078F0);
    final Color hoverBgColor = isDarkMode 
        ? const Color(0xFF5B7BF0).withOpacity(0.15)
        : const Color(0xFF466AF1).withOpacity(0.08);

    return Container(
      margin: EdgeInsets.zero,
      decoration: BoxDecoration(
        color: isSelected ? selectedBgColor : Colors.transparent,
        borderRadius: BorderRadius.zero,
      ),
      child: Material(
        color: Colors.transparent,
        child: InkWell(
          borderRadius: BorderRadius.zero,
          hoverColor: isSelected ? Colors.transparent : hoverBgColor,
          onTap: () {
            setState(() {
              _selectedIndex = index;
            });
            if (isMobile) {
              Navigator.pop(context);
            }
          },
          child: Container(
            height: 50,
            padding: const EdgeInsets.symmetric(horizontal: 16),
            child: Row(
              children: [
                Icon(
                  _navItems[index].icon,
                  color: isSelected ? selectedTextColor : textColor,
                  size: 20,
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Text(
                    _navItems[index].label,
                    overflow: TextOverflow.ellipsis,
                    style: TextStyle(
                      color: isSelected ? selectedTextColor : textColor,
                      fontWeight: isSelected ? FontWeight.w600 : FontWeight.w500,
                      fontSize: 14,
                    ),
                  ),
                ),
                if (_navItems[index].badgeCount != null)
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                    decoration: BoxDecoration(
                      color: const Color(0xFF5B7BF0),
                      borderRadius: BorderRadius.circular(10),
                    ),
                    child: Text(
                      '${_navItems[index].badgeCount}',
                      style: const TextStyle(
                        color: Colors.white,
                        fontSize: 12,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                  ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}

class WelcomePage extends StatelessWidget {
  const WelcomePage({super.key});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    
    return SingleChildScrollView(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'Welcome to the DMTools Style Guide',
            style: theme.textTheme.headlineLarge,
          ),
          const SizedBox(height: 16),
          Text(
            'This style guide is a living document that showcases the UI components, design patterns, and visual guidelines for the DMTools application. Use the navigation to explore different categories of components.',
            style: theme.textTheme.bodyLarge,
          ),
          const SizedBox(height: 16),
          Text(
            'The goal is to create a consistent and high-quality user experience across all parts of DMTools. This guide helps developers and designers to:',
            style: theme.textTheme.bodyLarge,
          ),
          const SizedBox(height: 12),
          _buildBulletPoint('Understand the available UI building blocks.', theme),
          _buildBulletPoint('Reuse components to ensure consistency.', theme),
          _buildBulletPoint('Test components in isolation.', theme),
          _buildBulletPoint('Quickly reference design specifications.', theme),
          
          const SizedBox(height: 24),
          Text(
            'Dependencies',
            style: theme.textTheme.headlineMedium,
          ),
          const SizedBox(height: 12),
          Text(
            'The DMTools component library requires the following external dependencies:',
            style: theme.textTheme.bodyLarge,
          ),
          const SizedBox(height: 12),
          _buildBulletPoint('Google Fonts - Used for typography throughout the application.', theme),
          _buildBulletPoint('Flutter SVG - Used for rendering SVG icons.', theme),
          _buildBulletPoint('Provider - Used for state management and theming.', theme),
          
          const SizedBox(height: 24),
          Text(
            'Default view is Colors & Typography.',
            style: theme.textTheme.bodyLarge,
          ),
        ],
      ),
    );
  }
  
  Widget _buildBulletPoint(String text, ThemeData theme) {
    return Padding(
      padding: const EdgeInsets.only(left: 16, bottom: 8),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text('• ', style: theme.textTheme.bodyLarge),
          Expanded(
            child: Text(text, style: theme.textTheme.bodyLarge),
          ),
        ],
      ),
    );
  }
} 