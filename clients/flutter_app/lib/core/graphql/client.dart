import 'package:flutter/foundation.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:graphql_flutter/graphql_flutter.dart';

import '../auth/session_storage.dart';

const String kGraphQlUrl = String.fromEnvironment(
  'GRAPHQL_URL',
  defaultValue: 'https://messaging.melihemre.dev/graphql',
);

final sessionStorageProvider = Provider<SessionStorage>((_) => SessionStorage());

class _SessionHeaderLink extends Link {
  final SessionStorage storage;
  _SessionHeaderLink(this.storage);

  @override
  Stream<Response> request(Request request, [NextLink? forward]) async* {
    final session = await storage.read();
    var updated = request;
    if (session != null) {
      updated = request.updateContextEntry<HttpLinkHeaders>(
        (entry) => HttpLinkHeaders(
          headers: {
            ...?entry?.headers,
            'X-Session-Id': session.sessionId,
            'X-User-Id': session.userId,
          },
        ),
      );
    }
    yield* forward!(updated);
  }
}

final graphQLClientProvider = Provider<ValueNotifier<GraphQLClient>>((ref) {
  final storage = ref.watch(sessionStorageProvider);
  final link = Link.from([_SessionHeaderLink(storage), HttpLink(kGraphQlUrl)]);
  return ValueNotifier(
    GraphQLClient(
      link: link,
      cache: GraphQLCache(),
      defaultPolicies: DefaultPolicies(
        query: Policies(fetch: FetchPolicy.networkOnly),
        mutate: Policies(fetch: FetchPolicy.networkOnly),
      ),
    ),
  );
});
