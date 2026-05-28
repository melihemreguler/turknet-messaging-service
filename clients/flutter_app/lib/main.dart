import 'package:flutter/material.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'app/router.dart';
import 'app/theme.dart';
import 'core/i18n/app_locale.dart';
import 'core/i18n/locale_controller.dart';

void main() {
  runApp(const ProviderScope(child: TurknetMessagingApp()));
}

class TurknetMessagingApp extends ConsumerWidget {
  const TurknetMessagingApp({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final router = ref.watch(routerProvider);
    final locale = ref.watch(localeControllerProvider);
    return MaterialApp.router(
      title: 'Messaging',
      theme: buildAppTheme(),
      routerConfig: router,
      debugShowCheckedModeBanner: false,
      locale: locale.toLocale(),
      supportedLocales: AppLocale.values.map((l) => l.toLocale()).toList(),
      localizationsDelegates: const [
        GlobalMaterialLocalizations.delegate,
        GlobalWidgetsLocalizations.delegate,
        GlobalCupertinoLocalizations.delegate,
      ],
    );
  }
}
