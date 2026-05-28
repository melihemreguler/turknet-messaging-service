# Turknet Messaging — Flutter Client

Mobile client for the Turknet Messaging service. Talks to the public GraphQL
middleware at `https://messaging.melihemre.dev/graphql` by default.

## Stack

- `flutter_riverpod` — state
- `graphql_flutter` — GraphQL client with a custom link that injects
  `X-Session-Id` / `X-User-Id` headers from secure storage
- `flutter_secure_storage` — session persistence (Keychain / EncryptedSharedPreferences)
- `go_router` — auth-aware navigation
- `intl`, `package_info_plus`

## Layout

```
lib/
├── main.dart
├── app/                      # router, theme
├── core/
│   ├── graphql/              # client, operations
│   ├── auth/                 # session model, storage, controller
│   └── client_info.dart      # userAgent for clientInfo input
├── features/
│   ├── auth/                 # login, register
│   ├── home/                 # home menu
│   ├── conversation/         # new chat, thread (3s polling)
│   └── activity/             # paginated activity log
└── models/                   # message, activity
```

## Run

```bash
flutter pub get
flutter analyze

# Default: production backend
flutter run

# Override backend (e.g. local middleware on Android emulator)
flutter run --dart-define=GRAPHQL_URL=http://10.0.2.2:4000/graphql
```

For iOS you'll need Xcode + CocoaPods; for Android, an SDK + emulator.

## Launching on macOS

Android needs these env vars exported (add to `~/.zshrc` to persist):

```bash
export ANDROID_HOME=/opt/homebrew/share/android-commandlinetools
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export PATH=$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$PATH
```

Start the emulators:

```bash
# iOS — boot a specific device, then open Simulator UI
xcrun simctl boot "iPhone 17 Pro"
open -a Simulator

# Android
flutter emulators --launch flutter_avd

# Wait until both show up here before running:
flutter devices
```

If `flutter devices` only lists macOS/Chrome, the emulators haven't finished
booting yet — wait a few seconds and re-run it.

Run the app:

```bash
flutter pub get
cd ios && pod install && cd ..              # after pubspec changes
flutter run -d <device-id>                  # device-id from `flutter devices`
```

The default `GRAPHQL_URL` (`https://messaging.melihemre.dev/graphql`) is HTTPS,
so no Android cleartext config or iOS ATS exception is required.

## Smoke test

1. Register `mobiletest1` on device/emulator A.
2. Register `mobiletest2` on device/emulator B.
3. From `mobiletest1`: Home → New message → enter `mobiletest2` → send a message.
4. On `mobiletest2`'s thread screen, the message should appear within ~3s (polling).
5. Activity screen shows `USER_CREATION`, `LOGIN_ATTEMPT`, and message events.
6. Logout clears the session and returns to `/login`.

## Out of scope

- Push notifications (no backend webhook)
- Realtime subscriptions (schema does not expose any)
- Inbox / conversation list (backend has no endpoint for it)
