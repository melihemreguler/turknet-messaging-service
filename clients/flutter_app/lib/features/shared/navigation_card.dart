import 'package:flutter/material.dart';

import 'user_avatar.dart';

class NavigationCard extends StatelessWidget {
  final Widget leading;
  final String title;
  final String? subtitle;
  final VoidCallback? onTap;

  const NavigationCard._({
    required this.leading,
    required this.title,
    this.subtitle,
    this.onTap,
  });

  factory NavigationCard.icon({
    Key? key,
    required IconData icon,
    required String title,
    String? subtitle,
    VoidCallback? onTap,
    Color? iconColor,
    Color? iconBackground,
  }) {
    return NavigationCard._(
      leading: _IconLeading(
        icon: icon,
        background: iconBackground,
        foreground: iconColor,
      ),
      title: title,
      subtitle: subtitle,
      onTap: onTap,
    );
  }

  factory NavigationCard.avatar({
    Key? key,
    required String username,
    required String title,
    String? subtitle,
    VoidCallback? onTap,
  }) {
    return NavigationCard._(
      leading: UserAvatar(username: username, size: 44),
      title: title,
      subtitle: subtitle,
      onTap: onTap,
    );
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Material(
      color: theme.colorScheme.surfaceContainerLow,
      borderRadius: BorderRadius.circular(16),
      clipBehavior: Clip.antiAlias,
      child: InkWell(
        onTap: onTap,
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 12),
          child: Row(
            children: [
              leading,
              const SizedBox(width: 14),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      title,
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                      style: theme.textTheme.titleSmall?.copyWith(
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                    if (subtitle != null) ...[
                      const SizedBox(height: 2),
                      Text(
                        subtitle!,
                        maxLines: 1,
                        overflow: TextOverflow.ellipsis,
                        style: theme.textTheme.bodySmall?.copyWith(
                          color: theme.colorScheme.onSurfaceVariant,
                        ),
                      ),
                    ],
                  ],
                ),
              ),
              const SizedBox(width: 8),
              Icon(
                Icons.chevron_right,
                color: theme.colorScheme.onSurfaceVariant,
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _IconLeading extends StatelessWidget {
  final IconData icon;
  final Color? background;
  final Color? foreground;

  const _IconLeading({
    required this.icon,
    this.background,
    this.foreground,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Container(
      width: 44,
      height: 44,
      decoration: BoxDecoration(
        color: background ?? theme.colorScheme.secondaryContainer,
        shape: BoxShape.circle,
      ),
      alignment: Alignment.center,
      child: Icon(
        icon,
        color: foreground ?? theme.colorScheme.onSecondaryContainer,
        size: 22,
      ),
    );
  }
}
