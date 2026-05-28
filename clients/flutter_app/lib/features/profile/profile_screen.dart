import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:package_info_plus/package_info_plus.dart';

import '../../core/auth/auth_controller.dart';
import '../../core/i18n/app_strings.dart';
import '../../core/i18n/locale_controller.dart';
import '../shared/navigation_card.dart';
import '../shared/user_avatar.dart';

class ProfileScreen extends ConsumerWidget {
  const ProfileScreen({super.key});

  void _back(BuildContext context) {
    if (context.canPop()) {
      context.pop();
    } else {
      context.go('/settings');
    }
  }

  Future<void> _confirmLogout(
      BuildContext context, WidgetRef ref, AppStrings s) async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: Text(s.signOutConfirmTitle),
        content: Text(s.signOutConfirmBody),
        actions: [
          TextButton(
              onPressed: () => Navigator.pop(ctx, false),
              child: Text(s.cancel)),
          FilledButton.tonal(
              onPressed: () => Navigator.pop(ctx, true),
              child: Text(s.signOut)),
        ],
      ),
    );
    if (confirmed != true) return;
    await ref.read(authControllerProvider.notifier).logout();
    if (context.mounted) context.go('/login');
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final session = ref.watch(authControllerProvider).session;
    final theme = Theme.of(context);
    final s = ref.watch(stringsProvider);
    return Scaffold(
      appBar: AppBar(
        title: Text(s.profileTitle),
        leading: IconButton(
          icon: const Icon(Icons.arrow_back),
          onPressed: () => _back(context),
        ),
      ),
      body: ListView(
        padding: const EdgeInsets.symmetric(horizontal: 16),
        children: [
          const SizedBox(height: 24),
          Center(
            child: Column(
              children: [
                UserAvatar(username: session?.username ?? '?', size: 96),
                const SizedBox(height: 16),
                Text(
                  session?.username ?? '',
                  style: theme.textTheme.titleLarge?.copyWith(
                    fontWeight: FontWeight.w600,
                  ),
                ),
                if (session != null) ...[
                  const SizedBox(height: 4),
                  Text(
                    '${s.userIdLabel}: ${session.userId}',
                    style: theme.textTheme.bodySmall?.copyWith(
                      color: theme.colorScheme.onSurfaceVariant,
                    ),
                  ),
                ],
              ],
            ),
          ),
          const SizedBox(height: 32),
          _SectionHeader(text: s.sectionActivity),
          const SizedBox(height: 8),
          NavigationCard.icon(
            icon: Icons.history_outlined,
            title: s.activityEntryTitle,
            subtitle: s.activityEntrySubtitle,
            onTap: () => context.push('/activity'),
          ),
          const SizedBox(height: 24),
          _SectionHeader(text: s.sectionSession),
          const SizedBox(height: 8),
          Material(
            color: theme.colorScheme.errorContainer.withValues(alpha: 0.4),
            borderRadius: BorderRadius.circular(16),
            clipBehavior: Clip.antiAlias,
            child: ListTile(
              leading: Icon(
                Icons.logout,
                color: theme.colorScheme.error,
              ),
              title: Text(
                s.signOut,
                style: TextStyle(
                  color: theme.colorScheme.error,
                  fontWeight: FontWeight.w600,
                ),
              ),
              onTap: () => _confirmLogout(context, ref, s),
            ),
          ),
          const SizedBox(height: 24),
          _AppVersionTile(label: s.versionLabel),
          const SizedBox(height: 24),
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

class _AppVersionTile extends StatelessWidget {
  final String label;
  const _AppVersionTile({required this.label});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return FutureBuilder<PackageInfo>(
      future: PackageInfo.fromPlatform(),
      builder: (_, snap) {
        final v = snap.data == null
            ? ''
            : '${snap.data!.version} (${snap.data!.buildNumber})';
        return Padding(
          padding: const EdgeInsets.symmetric(horizontal: 4),
          child: Text(
            '$label $v',
            textAlign: TextAlign.center,
            style: theme.textTheme.bodySmall?.copyWith(
              color: theme.colorScheme.onSurfaceVariant,
            ),
          ),
        );
      },
    );
  }
}
