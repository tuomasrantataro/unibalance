package com.unibalance.receivers

import android.app.Notification
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast

import com.unibalance.AlarmService
import com.unibalance.Helper

class AlarmReceiver : BroadcastReceiver() {
    private val TAG = "AlarmReceiver"

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "onReceive")
        if (intent != null && context != null) {
            val alarmUid = intent.getStringExtra("ALARM_UID")
            //Log.d(TAG, "received alarm. $alarmUid")
            Toast.makeText(context, "received alarm: $alarmUid", Toast.LENGTH_LONG).show()
            val serviceIntent = Intent(context, AlarmService::class.java)
            serviceIntent.putExtra("ALARM_UID", alarmUid)
            serviceIntent.putExtras(serviceIntent)

            context.startForegroundService(serviceIntent)
        }
    }
}