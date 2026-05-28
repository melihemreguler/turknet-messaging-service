import 'package:flutter/material.dart';
import 'package:intl/intl.dart';

import '../../models/message.dart';

class MessageBubble extends StatelessWidget {
  final Message message;
  final bool isMine;
  const MessageBubble({super.key, required this.message, required this.isMine});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final time = DateFormat.Hm().format(message.timestamp.toLocal());
    final radius = const Radius.circular(18);
    final corner = const Radius.circular(4);
    final bg = isMine
        ? theme.colorScheme.primary
        : theme.colorScheme.surfaceContainerHighest;
    final fg = isMine
        ? theme.colorScheme.onPrimary
        : theme.colorScheme.onSurface;

    return Align(
      alignment: isMine ? Alignment.centerRight : Alignment.centerLeft,
      child: Container(
        margin: const EdgeInsets.symmetric(vertical: 3, horizontal: 12),
        padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
        constraints: BoxConstraints(
          maxWidth: MediaQuery.of(context).size.width * 0.75,
        ),
        decoration: BoxDecoration(
          color: bg,
          borderRadius: BorderRadius.only(
            topLeft: radius,
            topRight: radius,
            bottomLeft: isMine ? radius : corner,
            bottomRight: isMine ? corner : radius,
          ),
        ),
        child: Column(
          crossAxisAlignment:
              isMine ? CrossAxisAlignment.end : CrossAxisAlignment.start,
          children: [
            Text(message.content, style: TextStyle(color: fg, height: 1.35)),
            const SizedBox(height: 2),
            Text(
              time,
              style: theme.textTheme.labelSmall?.copyWith(
                color: fg.withValues(alpha: 0.7),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
