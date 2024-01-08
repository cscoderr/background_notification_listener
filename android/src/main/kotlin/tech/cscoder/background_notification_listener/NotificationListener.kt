package tech.cscoder.background_notification_listener

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.annotation.RequiresApi
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.dart.DartExecutor
import io.flutter.embedding.engine.loader.FlutterLoader
import io.flutter.plugin.common.MethodChannel
import io.flutter.view.FlutterCallbackInformation
import java.io.ByteArrayOutputStream
import java.math.BigInteger
import java.security.MessageDigest
import java.util.concurrent.atomic.AtomicBoolean

class NotificationListener: NotificationListenerService() {

    private val queue = ArrayDeque<Notification>()
    private var backgroundFlutterEngine: FlutterEngine? = null
    private var channel: MethodChannel? = null
    private var callbackHandler: Long? = null
    private var mainCallbackHandler: Long? = null
    private lateinit var context: Context
    private val sServiceStarted = AtomicBoolean(false)
    private val eventsCache = HashMap<String, Notification>()

    companion object {
        val TAG = "BackgroundNotificationsListener"

        private const val CHANNEL_ID = "background_notifications_listener_channel"

        private var notificationListenerInstance: NotificationListener? = null

        const val NOTIFICATION_INTENT = "notification_event"
        const val NOTIFICATION_INTENT_KEY = "object"
    }

    override fun onCreate() {
        super.onCreate()

        context = this
        notificationListenerInstance = this

        startListenerService(this)
    }

    @Synchronized
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)

        val notification = Notification(context, sbn)

        // store the evt to cache
        eventsCache[notification.uid] = notification

        synchronized(sServiceStarted) {
            if (!sServiceStarted.get()) {
                Log.d(TAG, "service is not start try to queue the event")
                queue.add(notification)
            } else {
                Log.d(TAG, "send event to flutter side immediately!")
                Handler(context.mainLooper).post { updateFlutterEngine(notification) }
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        if (sbn == null) return

        val notification = Notification(context, sbn)
        Log.d(TAG, "notification removed: ${notification.uid}")
    }

    private fun startListenerService(context: Context) {
        Log.d(TAG, "start listener service")
        synchronized(sServiceStarted) {

            // we should to update
            Log.d(TAG, "service's flutter engine is null, should update one")

            sServiceStarted.set(true)
        }
        Log.d(TAG, "service start finished")
    }

    private fun updateFlutterEngine(notification: Notification) {
        val arguments = ArrayList<Any?>()
        if (backgroundFlutterEngine == null) {
            callbackHandler = SharedPreferenceService.getInstance(context = context).getCallbackHandler()
            mainCallbackHandler = SharedPreferenceService.getInstance(context = context).getMainCallbackHandler()
            if (callbackHandler == 0L || mainCallbackHandler == 0L) {
                Log.e(TAG, "Fatal: No callback registered")
                return
            }
            Log.d(TAG, "Found callback handler $callbackHandler")
            Log.d(TAG, "Found user callback handler $mainCallbackHandler")

            // Retrieve the actual callback information needed to invoke it.
            val callbackInfo = FlutterCallbackInformation.lookupCallbackInformation(callbackHandler!!)
            if (callbackInfo == null) {
                Log.e(TAG, "Fatal: failed to find callback")
                return
            }
            val flutterLoader = FlutterLoader()
            flutterLoader.startInitialization(context)
            flutterLoader.ensureInitializationComplete(context, null)

            backgroundFlutterEngine = FlutterEngine(context)
            val args = DartExecutor.DartCallback(
                context.assets,
                flutterLoader.findAppBundlePath(),
                callbackInfo
            )

            // Start running callback dispatcher code in our background FlutterEngine instance.
            backgroundFlutterEngine!!.dartExecutor.executeDartCallback(args)
        }
        // Create the MethodChannel used to communicate between the callback
        // dispatcher and this instance.
        channel = MethodChannel(
            backgroundFlutterEngine!!.dartExecutor.binaryMessenger,
            CHANNEL_ID
        )

        arguments.add(callbackHandler)
        arguments.add(mainCallbackHandler)
        arguments.add(notification.data)
        Log.e(TAG, "Send notification ${notification.data}")
        channel!!.invokeMethod("notification_event", arguments)
    }
}

class Notification(context: Context, sbn: StatusBarNotification) {
    var data: Map<String, Any?> = emptyMap()

    val uid: String
        get() = data["_id"] as String

    init {
        data = sbnToMap(context, sbn)
    }

    companion object {
        private fun sbnToMap(context: Context, sbn: StatusBarNotification): Map<String, Any?> {

            val map = extraToMap(context, sbn.notification.extras)

            map["packageName"] = sbn.packageName
            map["timestamp"] = sbn.postTime
            map["id"] = sbn.id
            map["canTap"] = sbn.notification.contentIntent != null
            map["flags"] = sbn.notification.flags

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                map["uid"] = sbn.uid
                map["channelId"] = sbn.notification.channelId
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                map["key"] = sbn.key
                map["_id"] = genKey(sbn.key)
            } else {
                map["_id"] = genKey(
                    map["packageName"],
                    map["channelId"],
                    map["id"]
                )
            }
            return map
        }

        fun genKey(vararg items: Any?): String {
            return md5(items.joinToString(separator="-"){ "$it" }).slice(IntRange(0, 12))
        }



        fun Drawable.toBitmap(): Bitmap {
            if (this is BitmapDrawable) {
                return this.bitmap
            }

            val bitmap = Bitmap.createBitmap(this.intrinsicWidth, this.intrinsicHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            this.setBounds(0, 0, canvas.width, canvas.height)
            this.draw(canvas)

            return bitmap
        }

        fun md5(input:String): String {
            val md = MessageDigest.getInstance("MD5")
            return BigInteger(1, md.digest(input.toByteArray())).toString(16).padStart(32, '0')
        }

        private fun extraToMap(context: Context, extras: Bundle?): HashMap<String, Any?> {
            val map = HashMap<String, Any?>()
            if (extras == null) return map
            val ks: Set<String> = extras.keySet()
            val iterator = ks.iterator()
            while (iterator.hasNext()) {
                val key = iterator.next()
                if (!EXTRA_KEYS_WHITE_LIST.contains(key)) continue

                val bits = key.split(".")
                val nKey = bits[bits.size - 1]

                map[nKey] = marshalled(context, extras.get(key))
            }
            return map
        }
        private fun marshalled(context: Context, v: Any?): Any? {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                when (v) {
                    is Icon -> {
                        convertIconToByteArray(context, v)
                    }
                    else -> internalMarshalled(context, v)
                }
            } else {
                internalMarshalled(context, v)
            }
        }

        private fun internalMarshalled(context: Context, v: Any?): Any? {
            return when (v) {
                is CharSequence -> v.toString()
                is Array<*> -> v.map { marshalled(context, it) }
                is Bitmap -> convertBitmapToByteArray(v)
                // TODO: turn other types which cause exception
                else -> v
            }
        }

        @RequiresApi(Build.VERSION_CODES.M)
        private fun convertIconToByteArray(context: Context, icon: Icon): ByteArray {
            return convertBitmapToByteArray(icon.loadDrawable(context)!!.toBitmap())
        }

        private fun convertBitmapToByteArray(bitmap: Bitmap): ByteArray {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            return stream.toByteArray()
        }

        private val EXTRA_KEYS_WHITE_LIST = arrayOf(
            android.app.Notification.EXTRA_TITLE,
            android.app.Notification.EXTRA_TEXT,
            android.app.Notification.EXTRA_SUB_TEXT,
            android.app.Notification.EXTRA_SUMMARY_TEXT,
            android.app.Notification.EXTRA_TEXT_LINES,
            android.app.Notification.EXTRA_BIG_TEXT,
            android.app.Notification.EXTRA_INFO_TEXT,
            android.app.Notification.EXTRA_SHOW_WHEN,
            android.app.Notification.EXTRA_LARGE_ICON
            // Notification.EXTRA_LARGE_ICON_BIG
        )
    }
}