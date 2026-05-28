import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../core/auth/auth_controller.dart';
import '../../core/i18n/app_locale.dart';
import '../../core/i18n/app_strings.dart';
import '../../core/i18n/locale_controller.dart';
import '../shared/navigation_card.dart';

class SettingsScreen extends ConsumerWidget {
  const SettingsScreen({super.key});

  void _back(BuildContext context) {
    if (context.canPop()) {
      context.pop();
    } else {
      context.go('/inbox');
    }
  }

  Future<void> _pickLanguage(
      BuildContext context, WidgetRef ref, AppStrings s) async {
    final current = ref.read(localeControllerProvider);
    final picked = await showDialog<AppLocale>(
      context: context,
      builder: (ctx) => SimpleDialog(
        title: Text(s.languagePickerTitle),
        children: [
          for (final l in AppLocale.values)
            ListTile(
              title: Text(l.label),
              trailing: l == current
                  ? Icon(Icons.check, color: Theme.of(ctx).colorScheme.primary)
                  : null,
              onTap: () => Navigator.pop(ctx, l),
            ),
        ],
      ),
    );
    if (picked != null) {
      await ref.read(localeControllerProvider.notifier).setLocale(picked);
    }
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final session = ref.watch(authControllerProvider).session;
    final s = ref.watch(stringsProvider);
    final locale = ref.watch(localeControllerProvider);
    final username = session?.username ?? '';

    return Scaffold(
      appBar: AppBar(
        title: Text(s.settingsTitle),
        leading: IconButton(
          icon: const Icon(Icons.arrow_back),
          onPressed: () => _back(context),
        ),
      ),
      body: ListView(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 16),
        children: [
          _SectionHeader(text: s.sectionAccount),
          const SizedBox(height: 8),
          NavigationCard.avatar(
            username: username.isEmpty ? '?' : username,
            title: username.isEmpty ? s.profileEntryTitle : '@$username',
            subtitle: s.profileEntrySubtitle,
            onTap: () => context.push('/profile'),
          ),
          const SizedBox(height: 24),
          _SectionHeader(text: s.sectionApp),
          const SizedBox(height: 8),
          NavigationCard.icon(
            icon: Icons.language_outlined,
            title: s.languageEntryTitle,
            subtitle: locale.label,
            onTap: () => _pickLanguage(context, ref, s),
          ),
        ],
      ),
    );
  }
}

class _SectionHeader extends StatelessWidget {
  final String text;
  const _SectionHeader({required this.text});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 4),
      child: Text(
        text,
        style: theme.textTheme.labelLarge?.copyWith(
          color: theme.colorScheme.onSurfaceVariant,
          fontWeight: FontWeight.w600,
        ),
      ),
    );
  }
}
