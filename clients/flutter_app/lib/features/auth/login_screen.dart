import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../core/auth/auth_controller.dart';
import '../../core/graphql/error_handler.dart';
import '../../core/i18n/locale_controller.dart';

class LoginScreen extends ConsumerStatefulWidget {
  const LoginScreen({super.key});

  @override
  ConsumerState<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends ConsumerState<LoginScreen> {
  final _formKey = GlobalKey<FormState>();
  final _username = TextEditingController();
  final _password = TextEditingController();
  bool _loading = false;
  String? _error;

  @override
  void dispose() {
    _username.dispose();
    _password.dispose();
    super.dispose();
  }

  Future<void> _submit() async {
    if (!_formKey.currentState!.validate()) return;
    final s = ref.read(stringsProvider);
    setState(() {
      _loading = true;
      _error = null;
    });
    final result = await ref.read(authControllerProvider.notifier).login(
          username: _username.text.trim(),
          password: _password.text,
        );
    if (!mounted) return;
    setState(() => _loading = false);
    if (result.success) {
      context.go('/inbox');
    } else {
      final raw = result.message ?? s.loginFailed;
      setState(() => _error = localizeServerMessage(raw, s));
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final s = ref.watch(stringsProvider);
    return Scaffold(
      body: SafeArea(
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(24),
          child: Form(
            key: _formKey,
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                const SizedBox(height: 32),
                Icon(Icons.forum, size: 56, color: theme.colorScheme.primary),
                const SizedBox(height: 16),
                Text(s.loginWelcome,
                    textAlign: TextAlign.center,
                    style: theme.textTheme.headlineSmall
                        ?.copyWith(fontWeight: FontWeight.w600)),
                const SizedBox(height: 4),
                Text(s.loginSubtitle,
                    textAlign: TextAlign.center,
                    style: theme.textTheme.bodyMedium?.copyWith(
                      color: theme.colorScheme.onSurfaceVariant,
                    )),
                const SizedBox(height: 32),
                TextFormField(
                  controller: _username,
                  decoration: InputDecoration(
                    hintText: s.usernameHint,
                    prefixIcon: const Icon(Icons.person_outline),
                  ),
                  validator: (v) =>
                      (v == null || v.trim().isEmpty) ? s.usernameRequired : null,
                  autofillHints: const [AutofillHints.username],
                ),
                const SizedBox(height: 12),
                TextFormField(
                  controller: _password,
                  obscureText: true,
                  decoration: InputDecoration(
                    hintText: s.passwordHint,
                    prefixIcon: const Icon(Icons.lock_outline),
                  ),
                  validator: (v) =>
                      (v == null || v.isEmpty) ? s.passwordRequired : null,
                  autofillHints: const [AutofillHints.password],
                ),
                const SizedBox(height: 24),
                if (_error != null)
                  Padding(
                    padding: const EdgeInsets.only(bottom: 12),
                    child: Text(_error!,
                        style: TextStyle(color: theme.colorScheme.error)),
                  ),
                FilledButton(
                  onPressed: _loading ? null : _submit,
                  child: _loading
                      ? const SizedBox(
                          width: 20,
                          height: 20,
                          child: CircularProgressIndicator(strokeWidth: 2),
                        )
                      : Text(s.signInButton),
                ),
                TextButton(
                  onPressed: _loading ? null : () => context.go('/register'),
                  child: Text(s.noAccountPrompt),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
