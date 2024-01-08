package tech.cscoder.background_notification_listener

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.util.Log

class  SharedPreferenceService(context: Context) {


    private var sharedPreferences: SharedPreferences? = null

    companion object {

        @Volatile
        private var instance: SharedPreferenceService? = null

        fun getInstance(context: Context) = instance?: synchronized(this) {
            instance?: SharedPreferenceService(context).also { instance = it }
        }
        private val PREFKEY: String = "CallingUiKitSharedPreferences"
        const val BACKGROUND_CALLBACK = "callStateBackgroundCallbackHandler"
        const val BACKGROUND_MAIN_CALLBACK = "callStateBackgroundMainCallbackHandler"
    }

    init {
        sharedPreferences = context.getSharedPreferences(PREFKEY, Context.MODE_PRIVATE)
    }

    fun saveString(key:String, value: String) {
        sharedPreferences?.edit()?.putString(key, value)?.apply()
    }

    fun addCallbackHandler(callback: Long) {
        sharedPreferences?.edit()?.putLong(BACKGROUND_CALLBACK, callback)?.apply()
    }

    fun getCallbackHandler(): Long? {
        return sharedPreferences?.getLong(BACKGROUND_CALLBACK, 0)
    }

    fun addMainCallbackHandler(callback: Long) {
        sharedPreferences?.edit()?.putLong(BACKGROUND_MAIN_CALLBACK, callback)?.apply()
    }

    fun getMainCallbackHandler(): Long? {
        return sharedPreferences?.getLong(BACKGROUND_MAIN_CALLBACK, 0)
    }

    fun removeHandlers() {
        sharedPreferences?.edit()?.remove(BACKGROUND_CALLBACK)?.apply()
        sharedPreferences?.edit()?.remove(BACKGROUND_MAIN_CALLBACK)?.apply()
    }

    fun getString(key:String): String? = sharedPreferences?.getString(key, "")

    fun removeString(key:String) = sharedPreferences?.edit()?.remove(key)?.apply()
}
