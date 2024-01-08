package tech.cscoder.background_notification_listener

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.service.notification.NotificationListenerService
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import io.flutter.embedding.engine.loader.FlutterLoader

class NotificationReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_REBOOT, Intent.ACTION_BOOT_COMPLETED -> {
                Log.i(NotificationListener.TAG, "Registering notification listener, after reboot!")
//                FlutterNotificationListenerPlugin.registerAfterReboot(context)
            }
            else -> {
                Log.i(NotificationListener.TAG, intent.action.toString())
            }
        }
        Log.d(NotificationListener.TAG, "New broadcast event received...")
        if (notificationListener == null) {
            val flutterLoader = FlutterLoader()
            flutterLoader.startInitialization(context)
            flutterLoader.ensureInitializationComplete(context, null)
            notificationListener = NotificationListener()
        }
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var notificationListener: NotificationListener? = null
    }
}