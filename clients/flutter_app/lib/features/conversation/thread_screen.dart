import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:graphql_flutter/graphql_flutter.dart';

import '../../core/auth/auth_controller.dart';
import '../../core/graphql/client.dart';
import '../../core/graphql/error_handler.dart';
import '../../core/graphql/operations.dart';
import '../../models/message.dart';
import 'message_bubble.dart';

const int _pageSize = 50;
const double _loadOlderThreshold = 200;

class ThreadScreen extends ConsumerStatefulWidget {
  final String otherUsername;
  const ThreadScreen({super.key, required this.otherUsername});

  @override
  ConsumerState<ThreadScreen> createState() => _ThreadScreenState();
}

class _ThreadScreenState extends ConsumerState<ThreadScreen>
    with WidgetsBindingObserver {
  final _composeController = TextEditingController();
  final _scrollController = ScrollController();

  // Messages kept in ASC (oldest → newest) order. ListView uses reverse: true
  // so index 0 in the list renders at the bottom; we map indices manually.
  List<Message> _messages = [];
  int _total = 0;
  int _oldestLoadedPage = -1;
  bool _loading = true;
  bool _loadingOlder = false;
  bool _sending = false;
  String? _error;
  Timer? _pollTimer;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
    _scrollController.addListener(_onScroll);
    _loadNewest();
    _startPolling();
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    _scrollController.removeListener(_onScroll);
    _pollTimer?.cancel();
    _composeController.dispose();
    _scrollController.dispose();
    super.dispose();
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    if (state == AppLifecycleState.resumed) {
      _startPolling();
      _loadNewest(silent: true);
    } else {
      _pollTimer?.cancel();
    }
  }

  void _startPolling() {
    _pollTimer?.cancel();
    _pollTimer = Timer.periodic(
      const Duration(seconds: 3),
      (_) => _loadNewest(silent: true),
    );
  }

  void _onScroll() {
    // ListView is reversed: scrolling visually up = position approaches
    // maxScrollExtent. Trigger older-page fetch when within threshold.
    if (!_scrollController.hasClients) return;
    final pos = _scrollController.position;
    if (pos.maxScrollExtent - pos.pixels < _loadOlderThreshold) {
      _loadOlder();
    }
  }

  bool get _hasOlder {
    if (_total == 0) return false;
    return _messages.length < _total;
  }

  Future<bool> _handleAuthFailure(OperationException? exc) async {
    if (!isUnauthorized(exc)) return false;
    _pollTimer?.cancel();
    await ref.read(authControllerProvider.notifier).clearLocal();
    if (mounted) context.go('/login');
    return true;
  }

  Future<void> _loadNewest({bool silent = false}) async {
    final client = ref.read(graphQLClientProvider).value;
    try {
      final result = await client.query(QueryOptions(
        document: gql(kGetMessageHistoryQuery),
        variables: {
          'username': widget.otherUsername,
          'pagination': {'page': 0, 'size': _pageSize},
        },
        fetchPolicy: FetchPolicy.networkOnly,
      ));
      if (!mounted) return;
      if (result.hasException) {
        if (await _handleAuthFailure(result.exception)) return;
        if (isNotFound(result.exception)) {
          setState(() {
            _messages = [];
            _total = 0;
            _oldestLoadedPage = 0;
            _loading = false;
            _error = null;
          });
          return;
        }
        if (!silent) {
          setState(() {
            _error = extractErrorMessage(result.exception, fallback: 'Failed to load messages');
            _loading = false;
          });
        }
        return;
      }
      final data = result.data?['getMessageHistory'] as Map<String, dynamic>?;
      final list = (data?['data']?['data'] as List?) ?? const [];
      final total = (data?['data']?['total'] as int?) ?? 0;
      final fetched = list.cast<Map<String, dynamic>>().map(Message.fromJson).toList();

      setState(() {
        _messages = _merge(_messages, fetched);
        _total = total;
        if (_oldestLoadedPage < 0) _oldestLoadedPage = 0;
        _loading = false;
        _error = null;
      });
    } catch (e) {
      if (!silent && mounted) {
        setState(() {
          _error = e.toString();
          _loading = false;
        });
      }
    }
  }

  Future<void> _loadOlder() async {
    if (_loadingOlder || !_hasOlder || _oldestLoadedPage < 0) return;
    setState(() => _loadingOlder = true);
    final client = ref.read(graphQLClientProvider).value;
    final nextPage = _oldestLoadedPage + 1;
    final result = await client.query(QueryOptions(
      document: gql(kGetMessageHistoryQuery),
      variables: {
        'username': widget.otherUsername,
        'pagination': {'page': nextPage, 'size': _pageSize},
      },
      fetchPolicy: FetchPolicy.networkOnly,
    ));
    if (!mounted) return;
    if (result.hasException) {
      if (await _handleAuthFailure(result.exception)) return;
      // Stop trying to fetch older pages on error to avoid loops.
      setState(() => _loadingOlder = false);
      return;
    }
    final data = result.data?['getMessageHistory'] as Map<String, dynamic>?;
    final list = (data?['data']?['data'] as List?) ?? const [];
    final total = (data?['data']?['total'] as int?) ?? _total;
    final fetched = list.cast<Map<String, dynamic>>().map(Message.fromJson).toList();
    setState(() {
      _messages = _merge(_messages, fetched);
      _total = total;
      _oldestLoadedPage = nextPage;
      _loadingOlder = false;
    });
  }

  /// Merge fetched messages into the existing list, dedupe by id, sort ASC.
  List<Message> _merge(List<Message> existing, List<Message> fetched) {
    final byId = {for (final m in existing) m.id: m};
    for (final m in fetched) {
      byId[m.id] = m;
    }
    final combined = byId.values.toList()
      ..sort((a, b) => a.timestamp.compareTo(b.timestamp));
    return combined;
  }

  Future<void> _send() async {
    final text = _composeController.text.trim();
    if (text.isEmpty || _sending) return;
    setState(() => _sending = true);
    final client = ref.read(graphQLClientProvider).value;
    final result = await client.mutate(MutationOptions(
      document: gql(kSendMessageMutation),
      variables: {
        'input': {
          'receiverUsername': widget.otherUsername,
          'content': text,
        },
      },
    ));
    if (!mounted) return;
    setState(() => _sending = false);
    if (result.hasException) {
      if (await _handleAuthFailure(result.exception)) return;
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text(extractErrorMessage(result.exception, fallback: 'Send failed'))),
      );
      return;
    }
    _composeController.clear();
    await _loadNewest(silent: true);
  }

  @override
  Widget build(BuildContext context) {
    final session = ref.watch(authControllerProvider).session;
    final myId = session?.userId;
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.otherUsername),
        leading: IconButton(
          icon: const Icon(Icons.arrow_back),
          onPressed: () => context.go('/home'),
        ),
      ),
      body: Column(
        children: [
          if (_error != null)
            Container(
              width: double.infinity,
              color: Colors.red.shade100,
              padding: const EdgeInsets.all(8),
              child: Text(_error!, style: const TextStyle(color: Colors.red)),
            ),
          Expanded(
            child: _loading
                ? const Center(child: CircularProgressIndicator())
                : _messages.isEmpty
                    ? const Center(child: Text('No messages yet'))
                    : ListView.builder(
                        controller: _scrollController,
                        reverse: true,
                        padding: const EdgeInsets.symmetric(vertical: 8),
                        itemCount: _messages.length + (_hasOlder ? 1 : 0),
                        itemBuilder: (_, i) {
                          // reverse: true → i=0 renders at bottom (newest).
                          if (i >= _messages.length) {
                            return const Padding(
                              padding: EdgeInsets.all(12),
                              child: Center(
                                child: SizedBox(
                                  width: 20,
                                  height: 20,
                                  child: CircularProgressIndicator(strokeWidth: 2),
                                ),
                              ),
                            );
                          }
                          final m = _messages[_messages.length - 1 - i];
                          return MessageBubble(
                            message: m,
                            isMine: m.senderId == myId,
                          );
                        },
                      ),
          ),
          SafeArea(
            top: false,
            child: Padding(
              padding: const EdgeInsets.all(8),
              child: Row(
                children: [
                  Expanded(
                    child: TextField(
                      controller: _composeController,
                      decoration: const InputDecoration(
                        hintText: 'Message',
                        border: OutlineInputBorder(),
                        isDense: true,
                      ),
                      onSubmitted: (_) => _send(),
                    ),
                  ),
                  IconButton(
                    icon: _sending
                        ? const SizedBox(
                            width: 18,
                            height: 18,
                            child: CircularProgressIndicator(strokeWidth: 2),
                          )
                        : const Icon(Icons.send),
                    onPressed: _sending ? null : _send,
                  ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}
