import 'dart:io' show Platform;

import 'package:package_info_plus/package_info_plus.dart';

class ClientInfo {
  final String ipAddress;
  final String userAgent;

  const ClientInfo({required this.ipAddress, required this.userAgent});

  Map<String, dynamic> toJson() => {
        'ipAddress': ipAddress,
        'userAgent': userAgent,
      };

  static Future<ClientInfo> resolve() async {
    final info = await PackageInfo.fromPlatform();
    final platform = Platform.isAndroid
        ? 'Android'
        : Platform.isIOS
            ? 'iOS'
            : Platform.operatingSystem;
    return ClientInfo(
      ipAddress: '0.0.0.0',
      userAgent: 'TurknetMessaging/${info.version} ($platform)',
    );
  }
}
