import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:graphql_flutter/graphql_flutter.dart';
import 'package:intl/intl.dart';

import '../../core/auth/auth_controller.dart';
import '../../core/graphql/client.dart';
import '../../core/graphql/error_handler.dart';
import '../../core/graphql/operations.dart';
import '../../models/activity.dart';

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
      setState(() {
        _error = extractErrorMessage(result.exception, fallback: 'Failed to load activity');
        _loading = false;
      });
      return;
    }
    final data = result.data?['getMyActivityLogs']?['data'] as Map<String, dynamic>?;
    final list = (data?['activities'] as List?) ?? const [];
    final parsed = list.cast<Map<String, dynamic>>().map(Activity.fromJson).toList();
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

  @override
  Widget build(BuildContext context) {
    final fmt = DateFormat.yMMMd().add_Hm();
    final canLoadMore = _items.length < _total;
    return Scaffold(
      appBar: AppBar(
        title: const Text('My activity'),
        leading: IconButton(
          icon: const Icon(Icons.arrow_back),
          onPressed: () => context.go('/home'),
        ),
      ),
      body: RefreshIndicator(
        onRefresh: () => _load(reset: true),
        child: _error != null
            ? ListView(
                children: [
                  Padding(
                    padding: const EdgeInsets.all(24),
                    child: Text(_error!, style: const TextStyle(color: Colors.red)),
                  ),
                ],
              )
            : ListView.separated(
                itemCount: _items.length + (canLoadMore ? 1 : 0),
                separatorBuilder: (_, _) => const Divider(height: 1),
                itemBuilder: (_, i) {
                  if (i >= _items.length) {
                    return Padding(
                      padding: const EdgeInsets.all(16),
                      child: Center(
                        child: _loading
                            ? const CircularProgressIndicator()
                            : TextButton(
                                onPressed: () => _load(),
                                child: const Text('Load more'),
                              ),
                      ),
                    );
                  }
                  final a = _items[i];
                  return ListTile(
                    title: Text(a.activityType),
                    subtitle: Text(a.description),
                    trailing: Text(
                      fmt.format(a.timestamp.toLocal()),
                      style: Theme.of(context).textTheme.labelSmall,
                    ),
                  );
                },
              ),
      ),
    );
  }
}
