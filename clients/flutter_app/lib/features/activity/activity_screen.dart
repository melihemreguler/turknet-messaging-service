import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:graphql_flutter/graphql_flutter.dart';

import '../../core/auth/auth_controller.dart';
import '../../core/graphql/client.dart';
import '../../core/graphql/error_handler.dart';
import '../../core/graphql/operations.dart';
import '../../core/i18n/app_strings.dart';
import '../../core/i18n/locale_controller.dart';
import '../../models/activity.dart';
import '../shared/relative_time.dart';
import 'activity_labels.dart';

class ActivityScreen extends ConsumerStatefulWidget {
  const ActivityScreen({super.key});

  @override
  ConsumerState<ActivityScreen> createState() => _ActivityScreenState();
}

class _ActivityScreenState extends ConsumerState<ActivityScreen> {
  static const _pageSize = 25;
  final List<Activity> _items = [];
  int _page = 0;
  int _total = 0;
  bool _loading = false;
  String? _error;

  @override
  void initState() {
    super.initState();
    _load(reset: true);
  }

  Future<void> _load({bool reset = false}) async {
    if (_loading) return;
    setState(() => _loading = true);
    final client = ref.read(graphQLClientProvider).value;
    final result = await client.query(QueryOptions(
      document: gql(kGetActivityLogsQuery),
      variables: {
        'pagination': {'page': reset ? 0 : _page, 'size': _pageSize},
      },
      fetchPolicy: FetchPolicy.networkOnly,
    ));
    if (!mounted) return;
    if (result.hasException) {
      if (isUnauthorized(result.exception)) {
        await ref.read(authControllerProvider.notifier).clearLocal();
        if (mounted) context.go('/login');
        return;
      }
      final s = ref.read(stringsProvider);
      setState(() {
        _error = extractErrorMessage(result.exception,
            fallback: s.activityLoadFailed, strings: s);
        _loading = false;
      });
      return;
    }
    final data =
        result.data?['getMyActivityLogs']?['data'] as Map<String, dynamic>?;
    final list = (data?['activities'] as List?) ?? const [];
    final parsed =
        list.cast<Map<String, dynamic>>().map(Activity.fromJson).toList();
    setState(() {
      if (reset) {
        _items
          ..clear()
          ..addAll(parsed);
        _page = 1;
      } else {
        _items.addAll(parsed);
        _page += 1;
      }
      _total = (data?['totalElements'] as int?) ?? _items.length;
      _loading = false;
      _error = null;
    });
  }

  void _back() {
    if (context.canPop()) {
      context.pop();
    } else {
      context.go('/profile');
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final s = ref.watch(stringsProvider);
    final canLoadMore = _items.length < _total;
    return Scaffold(
      appBar: AppBar(
        title: Text(s.activityTitle),
        leading: IconButton(
          icon: const Icon(Icons.arrow_back),
          onPressed: _back,
        ),
      ),
      body: RefreshIndicator(
        onRefresh: () => _load(reset: true),
        child: _buildBody(theme, s, canLoadMore),
      ),
    );
  }

  Widget _buildBody(ThemeData theme, AppStrings s, bool canLoadMore) {
    if (_error != null) {
      return ListView(
        children: [
          Padding(
            padding: const EdgeInsets.all(24),
            child: Text(
              _error!,
              style: TextStyle(color: theme.colorScheme.error),
            ),
          ),
        ],
      );
    }
    if (_items.isEmpty && !_loading) {
      return ListView(
        physics: const AlwaysScrollableScrollPhysics(),
        children: [
          const SizedBox(height: 120),
          Icon(
            Icons.history_toggle_off,
            size: 64,
            color: theme.colorScheme.onSurfaceVariant,
          ),
          const SizedBox(height: 16),
          Center(
            child: Text(
              s.activityEmpty,
              style: theme.textTheme.titleMedium,
            ),
          ),
        ],
      );
    }
    return ListView.separated(
      itemCount: _items.length + (canLoadMore ? 1 : 0),
      separatorBuilder: (_, _) => Divider(
        height: 1,
        indent: 72,
        color: theme.dividerTheme.color,
      ),
      itemBuilder: (_, i) {
        if (i >= _items.length) {
          return Padding(
            padding: const EdgeInsets.all(16),
            child: Center(
              child: _loading
                  ? const CircularProgressIndicator()
                  : TextButton(
                      onPressed: () => _load(),
                      child: Text(s.loadMore),
                    ),
            ),
          );
        }
        return _ActivityTile(activity: _items[i], strings: s);
      },
    );
  }
}

class _ActivityTile extends StatelessWidget {
  final Activity activity;
  final AppStrings strings;
  const _ActivityTile({required this.activity, required this.strings});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final p = presentActivity(
      activityType: activity.activityType,
      description: activity.description,
      strings: strings,
    );

    final iconBg = p.isFailure
        ? theme.colorScheme.errorContainer
        : theme.colorScheme.secondaryContainer;
    final iconFg = p.isFailure
        ? theme.colorScheme.onErrorContainer
        : theme.colorScheme.onSecondaryContainer;

    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.center,
        children: [
          Container(
            width: 40,
            height: 40,
            decoration: BoxDecoration(
              color: iconBg,
              shape: BoxShape.circle,
            ),
            alignment: Alignment.center,
            child: Icon(p.icon, size: 20, color: iconFg),
          ),
          const SizedBox(width: 16),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  p.title,
                  style: theme.textTheme.titleSmall?.copyWith(
                    fontWeight: FontWeight.w600,
                  ),
                ),
                if (p.statusLabel != null) ...[
                  const SizedBox(height: 4),
                  _StatusLine(
                    label: p.statusLabel!,
                    reason: p.reason,
                    isFailure: p.isFailure,
                  ),
                ],
              ],
            ),
          ),
          const SizedBox(width: 8),
          Text(
            formatRelativeShort(activity.timestamp),
            style: theme.textTheme.labelSmall?.copyWith(
              color: theme.colorScheme.onSurfaceVariant,
            ),
          ),
        ],
      ),
    );
  }
}

class _StatusLine extends StatelessWidget {
  final String label;
  final String? reason;
  final bool isFailure;
  const _StatusLine({
    required this.label,
    required this.reason,
    required this.isFailure,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final pillBg = isFailure
        ? theme.colorScheme.errorContainer
        : theme.colorScheme.secondaryContainer;
    final pillFg = isFailure
        ? theme.colorScheme.onErrorContainer
        : theme.colorScheme.onSecondaryContainer;
    return Row(
      children: [
        Container(
          padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
          decoration: BoxDecoration(
            color: pillBg,
            borderRadius: BorderRadius.circular(999),
          ),
          child: Text(
            label,
            style: theme.textTheme.labelSmall?.copyWith(
              color: pillFg,
              fontWeight: FontWeight.w600,
            ),
          ),
        ),
        if (reason != null && reason!.isNotEmpty) ...[
          const SizedBox(width: 6),
          Expanded(
            child: Text(
              reason!,
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
              style: theme.textTheme.bodySmall?.copyWith(
                color: theme.colorScheme.onSurfaceVariant,
              ),
            ),
          ),
        ],
      ],
    );
  }
}
