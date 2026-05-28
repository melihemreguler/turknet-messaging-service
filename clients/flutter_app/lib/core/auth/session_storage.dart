import 'package:flutter_secure_storage/flutter_secure_storage.dart';

import 'session.dart';

class SessionStorage {
  static const _storage = FlutterSecureStorage(
    aOptions: AndroidOptions(encryptedSharedPreferences: true),
  );

  static const _kSession = 'sessionId';
  static const _kUser = 'userId';
  static const _kUsername = 'username';

  Future<Session?> read() async {
    final map = await _storage.readAll();
    return Session.fromMap({
      'sessionId': map[_kSession],
      'userId': map[_kUser],
      'username': map[_kUsername],
    });
  }

  Future<void> write(Session session) async {
    await _storage.write(key: _kSession, value: session.sessionId);
    await _storage.write(key: _kUser, value: session.userId);
    await _storage.write(key: _kUsername, value: session.username);
  }

  Future<void> clear() async {
    await _storage.delete(key: _kSession);
    await _storage.delete(key: _kUser);
    await _storage.delete(key: _kUsername);
  }
}
