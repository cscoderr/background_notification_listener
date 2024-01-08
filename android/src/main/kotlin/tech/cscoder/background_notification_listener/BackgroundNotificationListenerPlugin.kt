package tech.cscoder.background_notification_listener

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.annotation.NonNull

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

/** BackgroundNotificationListenerPlugin */
class BackgroundNotificationListenerPlugin: FlutterPlugin, ActivityAware,
  EventChannel.StreamHandler {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private lateinit var channel : MethodChannel
  private lateinit var handler: BackgroundNotificationListenerPluginImpl
  private var eventSink: EventChannel.EventSink? = null

  override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "background_notification_listener")
    handler = BackgroundNotificationListenerPluginImpl(flutterPluginBinding.applicationContext, null, channel)
    channel.setMethodCallHandler(handler)

    val receiver = NotificationReceiver()
    val intentFilter = IntentFilter()
    intentFilter.addAction(NotificationListener.NOTIFICATION_INTENT)
    flutterPluginBinding.applicationContext.registerReceiver(receiver, intentFilter)
  }

  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
    handler?.setActivity(null)
  }

  override fun onAttachedToActivity(binding: ActivityPluginBinding) {
    handler?.setActivity(binding.activity)
  }

  override fun onDetachedFromActivityForConfigChanges() {
    onDetachedFromActivity()
  }

  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    onAttachedToActivity(binding)
  }

  override fun onDetachedFromActivity() {
    handler?.setActivity(null)
  }

  override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
    eventSink = events
  }

  override fun onCancel(arguments: Any?) {
    eventSink = null
  }

  internal inner class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
      eventSink?.success(intent.getStringExtra(NotificationListener.NOTIFICATION_INTENT_KEY)?:"{}")
    }
  }

  companion object {
    fun isServiceRunning(context: Context, serviceClass: Class<*>): Boolean {
      return null != getRunningService(context, serviceClass)
    }

    private fun getRunningService(context: Context, serviceClass: Class<*>): ActivityManager.RunningServiceInfo? {
      val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
      for (service in manager!!.getRunningServices(Int.MAX_VALUE)) {
        if (serviceClass.name == service.service.className) {
          return service
        }
      }

      return null
    }
  }

}
