import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'app_locale.dart';
import 'app_strings.dart';
import 'locale_storage.dart';

class LocaleController extends StateNotifier<AppLocale> {
  final LocaleStorage _storage;

  LocaleController(this._storage) : super(AppLocale.tr) {
    _bootstrap();
  }

  Future<void> _bootstrap() async {
    final stored = await _storage.read();
    if (stored != null) state = stored;
  }

  Future<void> setLocale(AppLocale locale) async {
    state = locale;
    await _storage.write(locale);
  }
}

final localeStorageProvider = Provider<LocaleStorage>((_) => LocaleStorage());

final localeControllerProvider = StateNotifierProvider<LocaleController, AppLocale>((ref) {
  return LocaleController(ref.watch(localeStorageProvider));
});

final stringsProvider = Provider<AppStrings>((ref) {
  return AppStrings(ref.watch(localeControllerProvider));
});
