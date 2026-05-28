import 'package:intl/intl.dart';

String formatRelativeShort(DateTime when, {DateTime? now}) {
  final reference = now ?? DateTime.now();
  final local = when.toLocal();
  final diff = reference.difference(local);
  if (diff.inSeconds < 60) return 'now';
  if (diff.inMinutes < 60) return '${diff.inMinutes}m';
  if (diff.inHours < 24 && reference.day == local.day) {
    return DateFormat.Hm().format(local);
  }
  if (diff.inDays < 7) return DateFormat.E().format(local);
  if (reference.year == local.year) return DateFormat.MMMd().format(local);
  return DateFormat.yMd().format(local);
}
