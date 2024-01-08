import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'background_notification_listener_platform_interface.dart';

/// An implementation of [BackgroundNotificationListenerPlatform] that uses method channels.
class MethodChannelBackgroundNotificationListener
    extends BackgroundNotificationListenerPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('background_notification_listener');

  @override
  Future<void> initializeNotification(
      int callbackHandler, int mainCallbackHandler) async {
    try {
      await methodChannel.invokeMethod('initializeNotification', <dynamic>[
        callbackHandler,
        mainCallbackHandler,
      ]);
    } on PlatformException catch (_) {
      throw Exception('Unable to initialize call state background');
    }
  }

  @override
  Future<bool?> hasPermission() async {
    final hasPermission =
        await methodChannel.invokeMethod<bool>('hasPermission');
    return hasPermission;
  }

  @override
  Future<bool?> openSettings() async {
    final openSettings = await methodChannel.invokeMethod<bool>('openSettings');
    return openSettings;
  }
}
