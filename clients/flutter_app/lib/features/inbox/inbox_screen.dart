import 'dart:async';

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
import '../../models/conversation.dart';
import '../shared/relative_time.dart';
import '../shared/user_avatar.dart';

const Duration _pollInterval = Duration(seconds: 5);
const int _pageSize = 30;

class InboxScreen extends ConsumerStatefulWidget {
  const InboxScreen({super.key});

  @override
  ConsumerState<InboxScreen> createState() => _InboxScreenState();
}

class _InboxScreenState extends ConsumerState<InboxScreen>
    with WidgetsBindingObserver {
  List<Conversation> _items = [];
  bool _loading = true;
  String? _error;
  Timer? _pollTimer;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
    _load(initial: true);
    _startPolling();
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    _pollTimer?.cancel();
    super.dispose();
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    if (state == AppLifecycleState.resumed) {
      _startPolling();
      _load();
    } else {
      _pollTimer?.cancel();
    }
  }

  void _startPolling() {
    _pollTimer?.cancel();
    _pollTimer = Timer.periodic(_pollInterval, (_) => _load());
  }

  Future<void> _load({bool initial = false}) async {
    final client = ref.read(graphQLClientProvider).value;
    final result = await client.query(QueryOptions(
      document: gql(kGetInboxQuery),
      variables: const {
        'pagination': {'page': 0, 'size': _pageSize},
      },
      fetchPolicy: FetchPolicy.networkOnly,
    ));
    if (!mounted) return;
    if (result.hasException) {
      if (isUnauthorized(result.exception)) {
        _pollTimer?.cancel();
        await ref.read(authControllerProvider.notifier).clearLocal();
        if (mounted) context.go('/login');
        return;
      }
      if (initial) {
        final s = ref.read(stringsProvider);
        setState(() {
          _error = extractErrorMessage(result.exception,
              fallback: s.inboxLoadFailed, strings: s);
          _loading = false;
        });
      }
      return;
    }
    final data = result.data?['getInbox']?['data'] as Map<String, dynamic>?;
    final list = (data?['data'] as List?) ?? const [];
    final parsed = list.cast<Map<String, dynamic>>().map(Conversation.fromJson).toList()
      ..sort((a, b) {
        final ta = a.lastTimestamp;
        final tb = b.lastTimestamp;
        if (ta == null && tb == null) return 0;
        if (ta == null) return 1;
        if (tb == null) return -1;
        return tb.compareTo(ta);
      });
    setState(() {
      _items = parsed;
      _loading = false;
      _error = null;
    });
  }

  @override
  Widget build(BuildContext context) {
    final session = ref.watch(authControllerProvider).session;
    final theme = Theme.of(context);
    final s = ref.watch(stringsProvider);

    return Scaffold(
      appBar: AppBar(
        titleSpacing: 16,
        title: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          mainAxisSize: MainAxisSize.min,
          children: [
            Text(s.inboxTitle),
            if (session != null)
              Text(
                '@${session.username}',
                style: theme.textTheme.bodySmall?.copyWith(
                  color: theme.colorScheme.onSurfaceVariant,
                ),
              ),
          ],
        ),
        actions: [
          PopupMenuButton<String>(
            tooltip: s.more,
            icon: const Icon(Icons.more_vert),
            onSelected: (value) {
              if (value == 'settings') context.push('/settings');
            },
            itemBuilder: (_) => [
              PopupMenuItem<String>(
                value: 'settings',
                child: Row(
                  children: [
                    const Icon(Icons.settings_outlined),
                    const SizedBox(width: 12),
                    Text(s.settingsTitle),
                  ],
                ),
              ),
            ],
          ),
        ],
      ),
      body: RefreshIndicator(
        onRefresh: () => _load(),
        child: _buildBody(theme, s),
      ),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: () => context.push('/new'),
        icon: const Icon(Icons.edit_outlined),
        label: Text(s.inboxFabNew),
      ),
    );
  }

  Widget _buildBody(ThemeData theme, AppStrings s) {
    if (_loading) {
      return const Center(child: CircularProgressIndicator());
    }
    if (_error != null) {
      return ListView(
        children: [
          const SizedBox(height: 96),
          Icon(Icons.error_outline,
              size: 48, color: theme.colorScheme.error),
          const SizedBox(height: 12),
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 24),
            child: Text(_error!, textAlign: TextAlign.center),
          ),
          const SizedBox(height: 16),
          Center(
            child: TextButton(
              onPressed: () {
                setState(() => _loading = true);
                _load(initial: true);
              },
              child: Text(s.retry),
            ),
          ),
        ],
      );
    }
    if (_items.isEmpty) {
      return ListView(
        physics: const AlwaysScrollableScrollPhysics(),
        children: [
          const SizedBox(height: 120),
          Icon(Icons.forum_outlined,
              size: 64, color: theme.colorScheme.onSurfaceVariant),
          const SizedBox(height: 16),
          Center(
            child: Text(
              s.inboxEmptyTitle,
              style: theme.textTheme.titleMedium,
            ),
          ),
          const SizedBox(height: 8),
          Center(
            child: Text(
              s.inboxEmptySubtitle,
              style: theme.textTheme.bodyMedium?.copyWith(
                color: theme.colorScheme.onSurfaceVariant,
              ),
            ),
          ),
        ],
      );
    }
    return ListView.separated(
      physics: const AlwaysScrollableScrollPhysics(),
      itemCount: _items.length,
      separatorBuilder: (_, _) => Divider(
        indent: 76,
        height: 1,
        color: theme.dividerTheme.color,
      ),
      itemBuilder: (_, i) {
        final c = _items[i];
        return _ConversationTile(conversation: c);
      },
    );
  }
}

class _ConversationTile extends ConsumerWidget {
  final Conversation conversation;
  const _ConversationTile({required this.conversation});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final theme = Theme.of(context);
    final session = ref.watch(authControllerProvider).session;
    final s = ref.watch(stringsProvider);
    final last = conversation.lastMessage;
    final mine = last != null && session != null && last.senderId == session.userId;
    final preview = last == null
        ? s.noMessagesYet
        : (mine ? '${s.youPrefix}: ${last.content}' : last.content);
    final time = conversation.lastTimestamp == null
        ? ''
        : formatRelativeShort(conversation.lastTimestamp!);
    return InkWell(
      onTap: () => context.push('/thread/${conversation.otherUsername}'),
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
        child: Row(
          children: [
            UserAvatar(username: conversation.otherUsername),
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      Expanded(
                        child: Text(
                          conversation.otherUsername,
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis,
                          style: theme.textTheme.titleSmall?.copyWith(
                            fontWeight: FontWeight.w600,
                          ),
                        ),
                      ),
                      Text(
                        time,
                        style: theme.textTheme.labelSmall?.copyWith(
                          color: theme.colorScheme.onSurfaceVariant,
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 2),
                  Text(
                    preview,
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    style: theme.textTheme.bodyMedium?.copyWith(
                      color: theme.colorScheme.onSurfaceVariant,
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}
