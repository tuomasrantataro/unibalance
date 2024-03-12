package com.unibalance.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
//import com.unibalance.Manager

class BootReceiver : BroadcastReceiver() {
    val TAG = "AlarmBootReceiver"

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action
        if (action != null && action.equals("android.intent.action.BOOT_COMPLETED")) {
            Log.d(TAG, "received on boot intent: $action")
            //Manger.reschedule(...)
        }
    }
}