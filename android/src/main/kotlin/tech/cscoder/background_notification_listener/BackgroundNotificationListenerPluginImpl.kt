package tech.cscoder.background_notification_listener

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.EventChannel.StreamHandler
import io.flutter.plugin.common.EventChannel.EventSink

class BackgroundNotificationListenerPluginImpl(
    val context: Context,
    private var activity: Activity?,
    val  methodChannel: MethodChannel,
): MethodCallHandler {

    companion object {
        private val TAG = "BackgroundNotificationsListenerService"

        private const val ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"

        private const val ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners"
    }

    fun setActivity(activity: Activity?) {
        this.activity = activity
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        try {
            when(call.method) {
                "initializeNotification" -> {
                    val arguments = call.arguments as ArrayList<*>?
                    if(arguments?.size == 2) {
                        SharedPreferenceService.getInstance((this.activity?: context)).addCallbackHandler(arguments[0] as Long);
                        SharedPreferenceService.getInstance((this.activity?: context)).addMainCallbackHandler(arguments[1] as Long);
                        if (!hasPermission(context)) {
                            Log.e(TAG, "can't get permission to start service.")
                            result.success(false)
                        }
                        val intent = Intent(context, NotificationListener::class.java)
                        context.startService(intent)
                        Log.d(NotificationListener.TAG, "Service initialized...")
                        result.success(true)
                    } else {
                        result.notImplemented()
                    }
                }
                "startService" -> {
                    startService(context = this.activity?: context)
                    return result.success(true)
                }
                "stopNotificationService" -> {
                    try {
                        SharedPreferenceService.getInstance((this.activity?: context)).removeHandlers()
                        val receiver = ComponentName(context, NotificationListener::class.java)
                        (this.activity?: context).packageManager.setComponentEnabledSetting(receiver,
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                            PackageManager.DONT_KILL_APP
                        )
                        result.success(true)
                    } catch (e: Exception) {
                        result.error(NotificationListener.TAG, null, e)
                    }
                }
                "hasPermission" -> {
                    val response = hasPermission(this.activity?: context)
                    return result.success(response)
                }
                "openSettings" -> {
                    val response = openPermissionSettings(this.activity?: context)
                    return result.success(response)
                }
            }
        } catch (e: Exception) {
            result.error(TAG, null, e)
        }
    }

    fun startService(context: Context): Boolean {
        // store the config
        return true
    }

    fun stopService(context: Context): Boolean {
//        if (!isServiceRunning(context, NotificationsHandlerService::class.java)) return true
//
//        val intent = Intent(context, NotificationsHandlerService::class.java)
//        intent.action = NotificationsHandlerService.ACTION_SHUTDOWN
//        context.startService(intent)
        return true
    }

    private fun openPermissionSettings(context: Context): Boolean {
        context.startActivity(Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        return true
    }

    private fun hasPermission(context: Context): Boolean {
        val packageName = context.packageName
        val flat = Settings.Secure.getString(context.contentResolver, ENABLED_NOTIFICATION_LISTENERS)
        if (!TextUtils.isEmpty(flat)) {
            val names = flat.split(":").toTypedArray()
            for (name in names) {
                val componentName = ComponentName.unflattenFromString(name)
                val nameMatch = TextUtils.equals(packageName, componentName?.packageName)
                if (nameMatch) {
                    return true
                }
            }
        }

        return false
    }


}