import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../core/auth/auth_controller.dart';
import '../features/activity/activity_screen.dart';
import '../features/auth/login_screen.dart';
import '../features/auth/register_screen.dart';
import '../features/conversation/new_conversation_screen.dart';
import '../features/conversation/thread_screen.dart';
import '../features/home/home_screen.dart';

class _AuthRefresh extends ChangeNotifier {
  _AuthRefresh(this._ref) {
    _ref.listen<AuthState>(authControllerProvider, (_, _) => notifyListeners());
  }
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
      final atAuthRoute = state.matchedLocation == '/login' ||
          state.matchedLocation == '/register';
      if (!loggedIn && !atAuthRoute) return '/login';
      if (loggedIn && (atAuthRoute || state.matchedLocation == '/')) {
        return '/home';
      }
      return null;
    },
    routes: [
      GoRoute(path: '/', builder: (_, _) => const _SplashScreen()),
      GoRoute(path: '/login', builder: (_, _) => const LoginScreen()),
      GoRoute(path: '/register', builder: (_, _) => const RegisterScreen()),
      GoRoute(path: '/home', builder: (_, _) => const HomeScreen()),
      GoRoute(path: '/new', builder: (_, _) => const NewConversationScreen()),
      GoRoute(
        path: '/thread/:username',
        builder: (_, s) =>
            ThreadScreen(otherUsername: s.pathParameters['username']!),
      ),
      GoRoute(path: '/activity', builder: (_, _) => const ActivityScreen()),
    ],
  );
});

class _SplashScreen extends StatelessWidget {
  const _SplashScreen();
  @override
  Widget build(BuildContext context) =>
      const Scaffold(body: Center(child: CircularProgressIndicator()));
}
