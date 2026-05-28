import 'package:graphql_flutter/graphql_flutter.dart';

/// Extracts a human-readable message from a GraphQL [OperationException].
///
/// Middleware wraps Spring API errors so `extensions` looks like:
///   `{ code: 'UNAUTHORIZED'|'BAD_REQUEST'|..., statusCode: int,
///     details: { success: false, message: string, data: payload|null } }`
///
/// For Spring's `MethodArgumentNotValidException` the payload is a
/// `ValidationErrorResponse` whose `errors` is a `{ field: message }` map —
/// surface those field-level messages instead of the generic "Validation failed".
String extractErrorMessage(OperationException? exception, {String fallback = 'Something went wrong'}) {
  if (exception == null) return fallback;

  for (final err in exception.graphqlErrors) {
    final details = err.extensions?['details'];
    if (details is Map) {
      final data = details['data'];
      if (data is Map && data['errors'] is Map) {
        final errors = (data['errors'] as Map).values
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
