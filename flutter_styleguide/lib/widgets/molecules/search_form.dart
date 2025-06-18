import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../theme/app_theme.dart';
import '../../theme/app_colors.dart';

typedef SearchCallback = void Function(String query);

class SearchForm extends StatefulWidget {
  final SearchCallback? onSearch;
  final String hintText;
  final bool autofocus;

  const SearchForm({
    super.key,
    this.onSearch,
    this.hintText = 'Search...',
    this.autofocus = false,
  });

  @override
  State<SearchForm> createState() => _SearchFormState();
}

class _SearchFormState extends State<SearchForm> {
  final _controller = TextEditingController();

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  void _handleSearch() {
    if (widget.onSearch != null) {
      widget.onSearch!(_controller.text);
    }
  }

  @override
  Widget build(BuildContext context) {
    final themeProvider = Provider.of<ThemeProvider>(context);
    final isDarkMode = themeProvider.isDarkMode;
    final ThemeColorSet colors = isDarkMode ? AppColors.dark : AppColors.light;
    
    return Container(
      height: 40,
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(4),
      ),
      child: TextField(
        controller: _controller,
        autofocus: widget.autofocus,
        textAlignVertical: TextAlignVertical.center,
        onSubmitted: (value) => _handleSearch(),
        style: TextStyle(
          color: colors.textColor,
          fontSize: 14,
        ),
        decoration: InputDecoration(
          hintText: widget.hintText,
          hintStyle: TextStyle(
            color: colors.textMuted,
            fontSize: 14,
          ),
          contentPadding: const EdgeInsets.symmetric(vertical: 10, horizontal: 16),
          border: OutlineInputBorder(
            borderRadius: BorderRadius.circular(4),
            borderSide: BorderSide(color: colors.borderColor, width: 1),
          ),
          enabledBorder: OutlineInputBorder(
            borderRadius: BorderRadius.circular(4),
            borderSide: BorderSide(color: colors.borderColor, width: 1),
          ),
          focusedBorder: OutlineInputBorder(
            borderRadius: BorderRadius.circular(4),
            borderSide: BorderSide(color: colors.accentColor, width: 1),
          ),
          filled: true,
          fillColor: colors.inputBg,
          suffixIcon: Material(
            color: Colors.transparent,
            child: InkWell(
              onTap: _handleSearch,
              borderRadius: const BorderRadius.only(
                topRight: Radius.circular(4),
                bottomRight: Radius.circular(4),
              ),
              hoverColor: colors.accentHover,
              child: Ink(
                width: 100,
                decoration: BoxDecoration(
                  color: colors.accentColor,
                  borderRadius: const BorderRadius.only(
                    topRight: Radius.circular(4),
                    bottomRight: Radius.circular(4),
                  ),
                ),
                child: const Center(
                  child: Text(
                    'Search',
                    style: TextStyle(
                      color: Colors.white,
                      fontWeight: FontWeight.w500,
                      fontSize: 16,
                    ),
                  ),
                ),
              ),
            ),
          ),
          suffixIconConstraints: const BoxConstraints(
            minWidth: 100,
            minHeight: 40,
          ),
        ),
      ),
    );
  }
} 