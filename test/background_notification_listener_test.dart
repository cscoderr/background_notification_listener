import 'package:flutter_test/flutter_test.dart';
import 'package:background_notification_listener/background_notification_listener.dart';
import 'package:background_notification_listener/background_notification_listener_platform_interface.dart';
import 'package:background_notification_listener/background_notification_listener_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockBackgroundNotificationListenerPlatform
    with MockPlatformInterfaceMixin
    implements BackgroundNotificationListenerPlatform {

  @override
  Future<String?> getPlatformVersion() => Future.value('42');
}

void main() {
  final BackgroundNotificationListenerPlatform initialPlatform = BackgroundNotificationListenerPlatform.instance;

  test('$MethodChannelBackgroundNotificationListener is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelBackgroundNotificationListener>());
  });

  test('getPlatformVersion', () async {
    BackgroundNotificationListener backgroundNotificationListenerPlugin = BackgroundNotificationListener();
    MockBackgroundNotificationListenerPlatform fakePlatform = MockBackgroundNotificationListenerPlatform();
    BackgroundNotificationListenerPlatform.instance = fakePlatform;

    expect(await backgroundNotificationListenerPlugin.getPlatformVersion(), '42');
  });
}
