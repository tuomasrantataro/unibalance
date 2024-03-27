package com.unibalance
//import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
//import com.facebook.react.bridge.ReactContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.Promise

import android.util.Log
import com.unibalance.bleconnection.BLEManager

class AlarmModule(private val reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
    //private val manager = Manager()

    init {
        Helper.createNotificationChannel(reactContext)
    }

    override fun getName() = "AlarmModule"

    /*@ReactMethod fun createAlarm(hour: Int, minutes: Int) {
        val alarm = Alarm("2024", "hälytitteli", "joku description", hour, minutes, true)
        Manager.schedule(reactContext, alarm)
        Log.d("AlarmModule", "Create event with time $hour : $minutes  -- ")
    }*/

    @ReactMethod fun bleDebug(promise: Promise) {
        Log.d("AlarmModule", "BLE things started")
        BLEManager.start(reactContext)
    }

    @ReactMethod fun bleStop(promise: Promise) {
        BLEManager.reset(reactContext)
    }

    @ReactMethod fun set (details: ReadableMap, promise: Promise) {
        val alarm = parseAlarmObject(details)
        Log.d("AlarmModule", "Set event with: $alarm")
        Manager.schedule(reactContext, alarm)
        promise.resolve(null)
    }

    @ReactMethod fun stop (promise: Promise) {
        Log.d("AlarmModule", "stop alarm")
        Manager.stop(reactContext)
        promise.resolve(null)
    }

    @ReactMethod fun cancel(promise: Promise) {
        Log.d("AlarmModule", "cancel alarm")
        Manager.removeAll(reactContext)
        promise.resolve(null)
    }

    private fun parseAlarmObject (alarm: ReadableMap): Alarm {
        val uid = alarm.getString("uid") ?: "2023"
        val title = alarm.getString("title") ?: "Herätys"
        val hour = alarm.getInt("hour")
        val minutes = alarm.getInt("minutes")
        val duration = alarm.getInt("duration")
        val active = true //alarm.getBoolean("active")
        val description = alarm.getString("description") ?: "UniBalance herätys klo ${hour.toString().padStart(2, '0')}.${minutes.toString().padStart(2, '0')}"

        return Alarm(uid, title, description, hour, minutes, duration, active)
    }
}