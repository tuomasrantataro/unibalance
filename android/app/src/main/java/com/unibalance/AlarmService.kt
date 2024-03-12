package com.unibalance

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import android.util.Log

class AlarmService : Service() {
    private val TAG = "AlarmService"

    override fun onBind(intent: Intent?): IBinder? {
        Log.d(TAG, "On bind " + intent?.extras.toString())
        return null
    }
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Creating service")
    }
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Stopping service")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Log.d(TAG, "On start command $startId")

        if (intent != null) {
            val alarmUid = intent.getStringExtra("ALARM_UID") ?: "2024"
            val alarm = Storage.getAlarm(applicationContext, alarmUid)
            val notification = Helper.getAlarmNotification(this, alarm, 3) // 1
            Manager.start(applicationContext, alarmUid)

            Helper.sendNotification(applicationContext, alarm, 3)

            startForeground(3, notification) // 1

            //Helper.sendNotification(applicationContext, alarm, 3)
            //val noti = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            //noti.notify(4, notification)

            Log.d(TAG, "Alarm: $alarmUid $notification")
        }

        return START_STICKY
    }
}