import 'package:flutter_secure_storage/flutter_secure_storage.dart';

import 'app_locale.dart';

class LocaleStorage {
  static const _storage = FlutterSecureStorage(
    aOptions: AndroidOptions(encryptedSharedPreferences: true),
  );
  static const _key = 'appLocale';

  Future<AppLocale?> read() async {
    final code = await _storage.read(key: _key);
    if (code == null) return null;
    return AppLocale.fromCode(code);
  }

  Future<void> write(AppLocale locale) => _storage.write(key: _key, value: locale.code);
}
