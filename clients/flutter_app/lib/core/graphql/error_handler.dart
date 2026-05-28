import 'package:graphql_flutter/graphql_flutter.dart';

import '../i18n/app_strings.dart';

/// Extracts a human-readable message from a GraphQL [OperationException].
///
/// When [strings] is provided, the resulting message is also fed through
/// [localizeServerMessage] so backend English strings (e.g. "Recipient not
/// found: foo") are mapped to the active app locale.
String extractErrorMessage(
  OperationException? exception, {
  String fallback = 'Something went wrong',
  AppStrings? strings,
}) {
  final raw = _extractRaw(exception, fallback);
  if (strings == null) return raw;
  return localizeServerMessage(raw, strings);
}

String _extractRaw(OperationException? exception, String fallback) {
  if (exception == null) return fallback;

  for (final err in exception.graphqlErrors) {
    final details = err.extensions?['details'];
    if (details is Map) {
      final data = details['data'];
      if (data is Map && data['errors'] is Map) {
        final errors = (data['errors'] as Map)
            .values
            .whereType<String>()
            .map((s) => s.trim())
            .where((s) => s.isNotEmpty)
            .toList();
        if (errors.isNotEmpty) return errors.join('\n');
      }
      if (details['message'] is String) {
        final msg = (details['message'] as String).trim();
        if (msg.isNotEmpty) return msg;
      }
    }
    final msg = err.message.trim();
    if (msg.isNotEmpty) return msg;
  }

  final link = exception.linkException;
  if (link is NetworkException) {
    return 'Network error. Check your connection and try again.';
  }
  if (link is HttpLinkServerException) {
    return 'Server error (${link.response.statusCode}). Please try again.';
  }
  if (link is ServerException) {
    return link.parsedResponse?.errors?.firstOrNull?.message ?? fallback;
  }

  return fallback;
}

/// Maps known backend English messages to the active app locale.
/// Unknown messages are returned as-is so localized backends or future
/// changes do not get mangled.
String localizeServerMessage(String raw, AppStrings s) {
  final msg = raw.trim();
  if (msg.isEmpty) return raw;

  // "Service error occurred: <inner>" wrapper — recurse on inner.
  final svcMatch = RegExp(r'^Service error occurred:\s*(.+)$').firstMatch(msg);
  if (svcMatch != null) return localizeServerMessage(svcMatch.group(1)!, s);

  // "Recipient not found: <id>" → "Alıcı bulunamadı: <id>"
  final recip = RegExp(r'^Recipient not found:\s*(.+)$').firstMatch(msg);
  if (recip != null) return '${s.errRecipientNotFoundPrefix}: ${recip.group(1)}';

  final sender = RegExp(r'^Sender not found:\s*(.+)$').firstMatch(msg);
  if (sender != null) return '${s.errSenderNotFoundPrefix}: ${sender.group(1)}';

  final user = RegExp(r'^User not found:\s*(.+)$').firstMatch(msg);
  if (user != null) return '${s.errUserNotFoundPrefix}: ${user.group(1)}';

  // Network/server pre-localized strings emitted by _extractRaw above.
  if (msg.startsWith('Network error')) return s.networkError;
  final serverErr = RegExp(r'^Server error \((\d+)\)').firstMatch(msg);
  if (serverErr != null) return '${s.serverError} (${serverErr.group(1)})';

  switch (msg) {
    case 'One or both users not found':
      return s.errUsersNotFound;
    case 'Access denied to this conversation':
      return s.errAccessDeniedConversation;
    case 'Either userId or username parameter must be provided':
      return s.errUserIdentifierMissing;
    case 'Username already exists':
      return s.errUsernameTaken;
    case 'Invalid credentials':
      return s.errInvalidCredentials;
    case 'Invalid password':
      return s.errInvalidPassword;
    case 'Failed to process message':
      return s.errMessageProcessing;
    case 'Message delivery service temporarily unavailable':
      return s.errDeliveryUnavailable;
    case 'Session cleanup operation failed':
      return s.errSessionCleanup;
    case 'Internal server error':
      return s.errInternal;
    case 'Not Found':
      return s.errNotFound;
    case 'Validation failed':
      return s.errValidation;
    case 'Authentication required':
      return s.errAuthRequired;
    case 'Something went wrong':
      return s.somethingWentWrong;
  }

  return raw;
}

/// True when the operation hit a Spring 404 (e.g. `ThreadNotFoundException`).
/// For history queries this is a normal empty-state, not a user-visible error.
bool isNotFound(OperationException? exception) {
  if (exception == null) return false;
  for (final err in exception.graphqlErrors) {
    final code = err.extensions?['code'];
    final status = err.extensions?['statusCode'];
    if (code == 'NOT_FOUND' || status == 404) return true;
  }
  return false;
}

/// True when the failure is an auth/session problem and the user should be
/// pushed back to /login. Middleware maps Spring's 401 to `code: 'UNAUTHORIZED'`
/// (see restApiClient.js getErrorCode).
bool isUnauthorized(OperationException? exception) {
  if (exception == null) return false;
  for (final err in exception.graphqlErrors) {
    final code = err.extensions?['code'];
    final status = err.extensions?['statusCode'];
    if (code == 'UNAUTHORIZED' || code == 'UNAUTHENTICATED' || status == 401) {
      return true;
    }
  }
  return false;
}
