package com.unibalance.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.unibalance.AlarmService
import com.unibalance.Manager

class DismissReceiver : BroadcastReceiver() {
    private val TAG = "AlarmDismissReceiver"

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent != null && context != null) {
            Log.d(TAG, "dismissed alarm notification for: ${intent.getStringExtra("ALARM_UID")}")
            Manager.stop(context)
        }
    }
}