import 'package:background_notification_listener/background_notification_listener.dart'
    as noti;
import 'package:flutter/material.dart';

@pragma('vm:entry-point')
void backgroundListener(noti.Notification notification) {
  print("=====================");
  print(notification.packageName);
  print("=====================");
}

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  @override
  void initState() {
    super.initState();
    initialize();
  }

  Future<void> initialize() async {
    final hasPermission =
        await noti.BackgroundNotificationListener.hasPermission();
    if (hasPermission ?? false) {
      noti.BackgroundNotificationListener.initializeCallState(
          backgroundListener);
      return;
    }
    await noti.BackgroundNotificationListener.openSettings();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: const Center(
          child: Text('Running on:'),
        ),
      ),
    );
  }
}
