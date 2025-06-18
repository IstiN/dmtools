# DMTools Flutter Styleguide

A Flutter implementation of the DMTools design system, showcasing UI components, design patterns, and visual guidelines.

## Overview

This styleguide serves as a living document that demonstrates the UI components and design patterns used in the DMTools application. It helps maintain consistency across the application and serves as a reference for developers and designers.

## Features

- **Theme Support**: Light and dark mode themes
- **Color System**: Consistent color palette for both themes
- **Typography**: Standardized text styles
- **Components**: Reusable UI components organized by complexity
  - **Atoms**: Basic building blocks (buttons, tags, status indicators)
  - **Molecules**: Combinations of atoms (cards, form fields)
  - **Organisms**: Complex UI components (headers, navigation)
- **Design Patterns**: Common UI patterns used throughout the application

## Running the Styleguide

```bash
cd flutter_styleguide
flutter run -d chrome
```

## Structure

- `/lib/theme`: Theme definitions and color system
- `/lib/widgets`: Reusable UI components
  - `/atoms`: Basic components
  - `/molecules`: Composite components
  - `/organisms`: Complex components
- `/lib/screens`: Styleguide pages

## Dependencies

- Flutter SDK
- Provider - For state management
- Google Fonts - For typography
- Flutter SVG - For SVG icons

## License

This project is part of DMTools and follows its licensing.
