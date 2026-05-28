import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../core/i18n/locale_controller.dart';

class NewConversationScreen extends ConsumerStatefulWidget {
  const NewConversationScreen({super.key});

  @override
  ConsumerState<NewConversationScreen> createState() =>
      _NewConversationScreenState();
}

class _NewConversationScreenState extends ConsumerState<NewConversationScreen> {
  final _controller = TextEditingController();
  final _formKey = GlobalKey<FormState>();

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  void _open() {
    if (!_formKey.currentState!.validate()) return;
    final target = _controller.text.trim();
    context.go('/thread/$target');
  }

  void _back() {
    if (context.canPop()) {
      context.pop();
    } else {
      context.go('/inbox');
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final s = ref.watch(stringsProvider);
    return Scaffold(
      appBar: AppBar(
        title: Text(s.newConversationTitle),
        leading: IconButton(
          icon: const Icon(Icons.arrow_back),
          onPressed: _back,
        ),
      ),
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.all(24),
          child: Form(
            key: _formKey,
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                Text(
                  s.to,
                  style: theme.textTheme.labelLarge?.copyWith(
                    color: theme.colorScheme.onSurfaceVariant,
                  ),
                ),
                const SizedBox(height: 8),
                TextFormField(
                  controller: _controller,
                  autofocus: true,
                  textInputAction: TextInputAction.go,
                  decoration: InputDecoration(
                    hintText: s.usernameHint,
                    prefixIcon: const Icon(Icons.alternate_email),
                  ),
                  validator: (v) => (v == null || v.trim().isEmpty)
                      ? s.usernameRequired
                      : null,
                  onFieldSubmitted: (_) => _open(),
                ),
                const SizedBox(height: 24),
                FilledButton.icon(
                  onPressed: _open,
                  icon: const Icon(Icons.chat_outlined),
                  label: Text(s.startChat),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
