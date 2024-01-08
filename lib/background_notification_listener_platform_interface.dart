import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'background_notification_listener_method_channel.dart';

abstract class BackgroundNotificationListenerPlatform
    extends PlatformInterface {
  /// Constructs a BackgroundNotificationListenerPlatform.
  BackgroundNotificationListenerPlatform() : super(token: _token);

  static final Object _token = Object();

  static BackgroundNotificationListenerPlatform _instance =
      MethodChannelBackgroundNotificationListener();

  /// The default instance of [BackgroundNotificationListenerPlatform] to use.
  ///
  /// Defaults to [MethodChannelBackgroundNotificationListener].
  static BackgroundNotificationListenerPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [BackgroundNotificationListenerPlatform] when
  /// they register themselves.
  static set instance(BackgroundNotificationListenerPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<void> initializeNotification(
      int callbackHandler, int mainCallbackHandler) {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<bool?> hasPermission() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<bool?> openSettings() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }
}
