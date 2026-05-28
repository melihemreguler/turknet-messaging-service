import 'package:flutter/material.dart';

const Color _seed = Color(0xFF4F46E5); // indigo-600

ThemeData buildAppTheme() {
  final scheme = ColorScheme.fromSeed(seedColor: _seed, brightness: Brightness.light);
  return ThemeData(
    useMaterial3: true,
    colorScheme: scheme,
    scaffoldBackgroundColor: scheme.surface,
    appBarTheme: AppBarTheme(
      backgroundColor: scheme.surface,
      foregroundColor: scheme.onSurface,
      surfaceTintColor: Colors.transparent,
      elevation: 0,
      scrolledUnderElevation: 1,
      centerTitle: false,
      titleTextStyle: TextStyle(
        color: scheme.onSurface,
        fontSize: 20,
        fontWeight: FontWeight.w600,
      ),
    ),
    cardTheme: CardThemeData(
      elevation: 0,
      color: scheme.surfaceContainerLow,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      margin: EdgeInsets.zero,
    ),
    listTileTheme: const ListTileThemeData(
      contentPadding: EdgeInsets.symmetric(horizontal: 16, vertical: 4),
    ),
    inputDecorationTheme: InputDecorationTheme(
      filled: true,
      fillColor: scheme.surfaceContainerHighest,
      border: OutlineInputBorder(
        borderRadius: BorderRadius.circular(12),
        borderSide: BorderSide.none,
      ),
      enabledBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(12),
        borderSide: BorderSide.none,
      ),
      focusedBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(12),
        borderSide: BorderSide(color: scheme.primary, width: 1.5),
      ),
      contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
    ),
    filledButtonTheme: FilledButtonThemeData(
      style: FilledButton.styleFrom(
        minimumSize: const Size.fromHeight(52),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
        textStyle: const TextStyle(fontSize: 16, fontWeight: FontWeight.w600),
      ),
    ),
    floatingActionButtonTheme: const FloatingActionButtonThemeData(
      elevation: 2,
      highlightElevation: 4,
    ),
    navigationBarTheme: NavigationBarThemeData(
      height: 68,
      backgroundColor: scheme.surface,
      surfaceTintColor: Colors.transparent,
      indicatorColor: scheme.primaryContainer,
      labelTextStyle: WidgetStatePropertyAll(
        TextStyle(fontSize: 12, fontWeight: FontWeight.w500, color: scheme.onSurfaceVariant),
      ),
    ),
    dividerTheme: DividerThemeData(color: scheme.outlineVariant, space: 1, thickness: 1),
  );
}

/// Deterministic color from a username, used for avatars.
Color avatarColorFor(String key) {
  if (key.isEmpty) return _seed;
  final hash = key.codeUnits.fold<int>(0, (acc, c) => (acc * 31 + c) & 0x7fffffff);
  final palette = <Color>[
    const Color(0xFF4F46E5),
    const Color(0xFF0EA5E9),
    const Color(0xFF10B981),
    const Color(0xFFF59E0B),
    const Color(0xFFEF4444),
    const Color(0xFF8B5CF6),
    const Color(0xFFEC4899),
    const Color(0xFF14B8A6),
  ];
  return palette[hash % palette.length];
}

String avatarInitials(String name) {
  if (name.isEmpty) return '?';
  final cleaned = name.trim();
  final parts = cleaned.split(RegExp(r'[\s_.-]+')).where((p) => p.isNotEmpty).toList();
  if (parts.isEmpty) return cleaned.substring(0, 1).toUpperCase();
  if (parts.length == 1) return parts.first.substring(0, 1).toUpperCase();
  return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
}
