import 'package:flutter/material.dart';

import '../../core/i18n/app_strings.dart';

class ActivityPresentation {
  final String title;
  final String? statusLabel;
  final String? reason;
  final IconData icon;
  final bool isFailure;

  const ActivityPresentation({
    required this.title,
    required this.icon,
    this.statusLabel,
    this.reason,
    this.isFailure = false,
  });
}

final _statusRegex = RegExp(r' - (Success|Failed)(?:: (.+))?$');

ActivityPresentation presentActivity({
  required String activityType,
  required String description,
  required AppStrings strings,
}) {
  final status = _parseStatus(description, strings);
  final isFailure = status?.successful == false;

  return switch (activityType) {
    'USER_CREATION' => ActivityPresentation(
        title: strings.accountCreated,
        icon: Icons.person_add_alt_1_outlined,
        statusLabel: status?.label,
        reason: status?.reason,
        isFailure: isFailure,
      ),
    'LOGIN_ATTEMPT' => ActivityPresentation(
        title: isFailure ? strings.signInFailed : strings.signedIn,
        icon: Icons.login,
        statusLabel: status?.label,
        reason: status?.reason,
        isFailure: isFailure,
      ),
    _ => ActivityPresentation(
        title: _humanizeFallback(activityType, strings),
        icon: Icons.event_note_outlined,
        statusLabel: status?.label,
        reason: status?.reason,
        isFailure: isFailure,
      ),
  };
}

_Status? _parseStatus(String description, AppStrings strings) {
  final match = _statusRegex.firstMatch(description);
  if (match == null) return null;
  final raw = match.group(1);
  final reason = match.group(2);
  final successful = raw == 'Success';
  return _Status(
    successful: successful,
    label: successful ? strings.statusSuccess : strings.statusFailed,
    reason: reason,
  );
}

String _humanizeFallback(String raw, AppStrings strings) {
  if (raw.isEmpty) return strings.unknownEvent;
  return raw
      .split('_')
      .where((part) => part.isNotEmpty)
      .map((part) =>
          part.substring(0, 1).toUpperCase() + part.substring(1).toLowerCase())
      .join(' ');
}

class _Status {
  final bool successful;
  final String label;
  final String? reason;
  const _Status({
    required this.successful,
    required this.label,
    this.reason,
  });
}
