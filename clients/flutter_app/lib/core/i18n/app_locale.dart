import 'dart:ui';

enum AppLocale {
  tr('tr', 'Türkçe'),
  en('en', 'English');

  final String code;
  final String label;
  const AppLocale(this.code, this.label);

  Locale toLocale() => Locale(code);

  static AppLocale fromCode(String? code) {
    for (final l in AppLocale.values) {
      if (l.code == code) return l;
    }
    return AppLocale.tr;
  }
}
