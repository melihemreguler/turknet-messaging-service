import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

class NewConversationScreen extends StatefulWidget {
  const NewConversationScreen({super.key});

  @override
  State<NewConversationScreen> createState() => _NewConversationScreenState();
}

class _NewConversationScreenState extends State<NewConversationScreen> {
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

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('New message'),
        leading: IconButton(
          icon: const Icon(Icons.arrow_back),
          onPressed: () => context.go('/home'),
        ),
      ),
      body: Padding(
        padding: const EdgeInsets.all(24),
        child: Form(
          key: _formKey,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              TextFormField(
                controller: _controller,
                decoration: const InputDecoration(
                  labelText: 'Recipient username',
                ),
                validator: (v) =>
                    (v == null || v.trim().isEmpty) ? 'Username required' : null,
                onFieldSubmitted: (_) => _open(),
              ),
              const SizedBox(height: 24),
              FilledButton(onPressed: _open, child: const Text('Open chat')),
            ],
          ),
        ),
      ),
    );
  }
}
