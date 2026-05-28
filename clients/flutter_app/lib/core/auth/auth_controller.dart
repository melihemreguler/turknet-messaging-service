import 'package:flutter/foundation.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:graphql_flutter/graphql_flutter.dart';

import '../client_info.dart';
import '../graphql/client.dart';
import '../graphql/error_handler.dart';
import '../graphql/operations.dart';
import 'session.dart';
import 'session_storage.dart';

class AuthState {
  final Session? session;
  final bool initializing;
  const AuthState({this.session, this.initializing = true});

  AuthState copyWith({Session? session, bool? initializing, bool clear = false}) {
    return AuthState(
      session: clear ? null : (session ?? this.session),
      initializing: initializing ?? this.initializing,
    );
  }
}

class AuthController extends StateNotifier<AuthState> {
  final SessionStorage _storage;
  final ValueNotifier<GraphQLClient> _client;

  AuthController(this._storage, this._client) : super(const AuthState()) {
    _bootstrap();
  }

  Future<void> _bootstrap() async {
    final session = await _storage.read();
    state = AuthState(session: session, initializing: false);
  }

  Future<({bool success, String? message})> register({
    required String username,
    required String email,
    required String password,
  }) async {
    final clientInfo = (await ClientInfo.resolve()).toJson();
    final result = await _client.value.mutate(MutationOptions(
      document: gql(kRegisterMutation),
      variables: {
        'input': {'username': username, 'email': email, 'password': password},
        'clientInfo': clientInfo,
      },
    ));
    if (result.hasException) {
      return (success: false, message: extractErrorMessage(result.exception, fallback: 'Register failed'));
    }
    final data = result.data?['register'] as Map<String, dynamic>?;
    if (data == null || data['success'] != true) {
      return (success: false, message: data?['message'] as String? ?? 'Register failed');
    }
    await _storeSession(
      sessionId: data['sessionId'] as String,
      userId: data['userId'] as String,
      username: username,
    );
    return (success: true, message: data['message'] as String?);
  }

  Future<({bool success, String? message})> login({
    required String username,
    required String password,
  }) async {
    final clientInfo = (await ClientInfo.resolve()).toJson();
    final result = await _client.value.mutate(MutationOptions(
      document: gql(kLoginMutation),
      variables: {
        'input': {'username': username, 'password': password},
        'clientInfo': clientInfo,
      },
    ));
    if (result.hasException) {
      return (success: false, message: extractErrorMessage(result.exception, fallback: 'Login failed'));
    }
    final data = result.data?['login'] as Map<String, dynamic>?;
    if (data == null || data['success'] != true) {
      return (success: false, message: data?['message'] as String? ?? 'Login failed');
    }
    await _storeSession(
      sessionId: data['sessionId'] as String,
      userId: data['userId'] as String,
      username: username,
    );
    return (success: true, message: data['message'] as String?);
  }

  Future<void> logout() async {
    try {
      await _client.value.mutate(MutationOptions(document: gql(kLogoutMutation)));
    } catch (_) {
      // Ignore — we'll clear locally anyway.
    }
    await _storage.clear();
    state = state.copyWith(clear: true, initializing: false);
  }

  Future<void> clearLocal() async {
    await _storage.clear();
    state = state.copyWith(clear: true, initializing: false);
  }

  Future<void> _storeSession({
    required String sessionId,
    required String userId,
    required String username,
  }) async {
    final session = Session(sessionId: sessionId, userId: userId, username: username);
    await _storage.write(session);
    state = AuthState(session: session, initializing: false);
  }
}

final authControllerProvider = StateNotifierProvider<AuthController, AuthState>((ref) {
  return AuthController(
    ref.watch(sessionStorageProvider),
    ref.watch(graphQLClientProvider),
  );
});
