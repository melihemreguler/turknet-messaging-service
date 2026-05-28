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
    return Align(
      alignment: isMine ? Alignment.centerRight : Alignment.centerLeft,
      child: Container(
        margin: const EdgeInsets.symmetric(vertical: 4, horizontal: 12),
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
        constraints: BoxConstraints(
          maxWidth: MediaQuery.of(context).size.width * 0.75,
        ),
        decoration: BoxDecoration(
          color: isMine
              ? theme.colorScheme.primaryContainer
              : theme.colorScheme.surfaceContainerHighest,
          borderRadius: BorderRadius.circular(12),
        ),
        child: Column(
          crossAxisAlignment:
              isMine ? CrossAxisAlignment.end : CrossAxisAlignment.start,
          children: [
            Text(message.content),
            const SizedBox(height: 2),
            Text(time,
                style: theme.textTheme.labelSmall?.copyWith(
                  color: theme.colorScheme.outline,
                )),
          ],
        ),
      ),
    );
  }
}
