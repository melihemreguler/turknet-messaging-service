import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../core/auth/auth_controller.dart';
import '../features/activity/activity_screen.dart';
import '../features/auth/login_screen.dart';
import '../features/auth/register_screen.dart';
import '../features/conversation/new_conversation_screen.dart';
import '../features/conversation/thread_screen.dart';
import '../features/inbox/inbox_screen.dart';
import '../features/profile/profile_screen.dart';
import '../features/settings/settings_screen.dart';

class _AuthRefresh extends ChangeNotifier {
  _AuthRefresh(this._ref) {
    _ref.listen<AuthState>(authControllerProvider, (_, _) => notifyListeners());
  }
  // ignore: unused_field
  final Ref _ref;
}

final routerProvider = Provider<GoRouter>((ref) {
  final refresh = _AuthRefresh(ref);
  return GoRouter(
    initialLocation: '/',
    refreshListenable: refresh,
    redirect: (context, state) {
      final auth = ref.read(authControllerProvider);
      if (auth.initializing) return null;
      final loggedIn = auth.session != null;
      final loc = state.matchedLocation;
      final atAuthRoute = loc == '/login' || loc == '/register';
      if (!loggedIn && !atAuthRoute) return '/login';
      if (loggedIn && (atAuthRoute || loc == '/' || loc == '/home')) {
        return '/inbox';
      }
      return null;
    },
    routes: [
      GoRoute(path: '/', builder: (_, _) => const _SplashScreen()),
      GoRoute(path: '/login', builder: (_, _) => const LoginScreen()),
      GoRoute(path: '/register', builder: (_, _) => const RegisterScreen()),
      GoRoute(path: '/inbox', builder: (_, _) => const InboxScreen()),
      GoRoute(path: '/settings', builder: (_, _) => const SettingsScreen()),
      GoRoute(path: '/profile', builder: (_, _) => const ProfileScreen()),
      GoRoute(path: '/activity', builder: (_, _) => const ActivityScreen()),
      GoRoute(path: '/new', builder: (_, _) => const NewConversationScreen()),
      GoRoute(
        path: '/thread/:username',
        builder: (_, s) =>
            ThreadScreen(otherUsername: s.pathParameters['username']!),
      ),
    ],
  );
});

class _SplashScreen extends StatelessWidget {
  const _SplashScreen();
  @override
  Widget build(BuildContext context) =>
      const Scaffold(body: Center(child: CircularProgressIndicator()));
}
