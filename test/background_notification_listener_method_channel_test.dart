import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:background_notification_listener/background_notification_listener_method_channel.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  MethodChannelBackgroundNotificationListener platform = MethodChannelBackgroundNotificationListener();
  const MethodChannel channel = MethodChannel('background_notification_listener');

  setUp(() {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger.setMockMethodCallHandler(
      channel,
      (MethodCall methodCall) async {
        return '42';
      },
    );
  });

  tearDown(() {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger.setMockMethodCallHandler(channel, null);
  });

  test('getPlatformVersion', () async {
    expect(await platform.getPlatformVersion(), '42');
  });
}
