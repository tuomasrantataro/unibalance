package com.unibalance

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import com.unibalance.receivers.AlarmReceiver

import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
//import androidx.core.content.getSystemService
import com.unibalance.receivers.DismissReceiver

class Helper {
    //private val TAG = "AlarmHelper"

    companion object {
        private val TAG = "AlarmHelper"
        fun scheduleAlarm(context: Context, alarmUid: String, triggerAtMillis: Long, notificationID: Int) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, AlarmReceiver::class.java)
            Log.d(TAG, "intent: $alarmUid  $intent")
            intent.putExtra("ALARM_UID", alarmUid)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                notificationID,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            try {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                Log.d(TAG, "succeeded setting alarm: $pendingIntent")
            } catch (e: SecurityException) {
                Log.e(TAG, "Failed to set Exact alarm: $e")
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            }

            //Log.d(TAG, "SDK version: ${Build.VERSION.SDK_INT}")
            Log.d(TAG, "scheduling alarm with notification id: $notificationID")
            Log.d(TAG, "alarm scheduled to fire in ${(triggerAtMillis - System.currentTimeMillis()).toDouble() / (1000 * 60)} min")
        }

        fun cancelAlarm(context: Context, notificationID: Int) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                notificationID,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
            Log.d(TAG, "canceling alarm with notification id: $notificationID")
        }

        fun sendNotification(context: Context, alarm: Alarm, notificationID: Int) {
            try {
                val mBuilder = getAlarmNotification(context, alarm, notificationID)
                val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                mNotificationManager.notify(notificationID, mBuilder)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        fun getAlarmNotification(context: Context, alarm: Alarm, notificationID: Int): Notification {
            return getNotification(context, notificationID, alarm.uid, alarm.title, alarm.description)
        }
        fun cancelNotification(context: Context, notificationID: Int) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.cancel(notificationID)
            manager.cancelAll()
        }

        fun createNotificationChannel(context: Context) {
            val id = context.resources.getString(R.string.notification_channel_id)
            val name = context.resources.getString(R.string.notification_channel_name)
            val description = context.resources.getString(R.string.notification_channel_desc)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(id, name, importance)
            channel.description = description
            channel.enableLights(true)
            channel.lightColor = Color.RED
            channel.enableVibration(true)
            channel.vibrationPattern = listOf<Long>(100, 200, 300, 400, 500, 400, 300, 200, 400).toLongArray()

            val notificationManager = ContextCompat.getSystemService(context, NotificationManager::class.java)!!
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "created a notification channel $channel")
        }
        protected fun getNotification(context: Context, id: Int, alarmUid: String, title: String, description: String): Notification {
            //val res = context.resources
            //val packageName = context.packageName
            //val smallIconResId = res.getIdentifier("ic_launcher", "mipmap", packageName)
            //val smallIconResId2 = res.getIdentifier("ic_launcher2", "mipmap", packageName)
            //Log.d(TAG, "icons: $smallIconResId, $smallIconResId2")
            val channelId = context.resources.getString(R.string.notification_channel_id)
            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setTicker(null)
                .setContentText(description)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(false)
                .setSound(null)
                .setVibrate(null)
                .setContentIntent(createOnClickedIntent(context, alarmUid, id))
                .setDeleteIntent(createOnDismissedIntent(context, alarmUid, id))
            return builder.build()
        }

        private fun createOnClickedIntent(context: Context, alarmUid: String, notificationID: Int): PendingIntent {
            val resultIntent = Intent(context, Helper.getMainActivityClass(context))
            resultIntent.putExtra("ALARM_UID", alarmUid)
            return PendingIntent.getActivity(
                context,
                notificationID,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
        private fun createOnDismissedIntent(context: Context, alarmUid: String, notificationId: Int): PendingIntent {
            val intent = Intent(context, DismissReceiver::class.java)
            intent.putExtra("NOTIFICATION_ID", notificationId)
            intent.putExtra("ALARM_UID", alarmUid)
            return PendingIntent.getBroadcast(context.applicationContext, notificationId, intent, PendingIntent.FLAG_IMMUTABLE)
        }
        fun getMainActivityClass(context: Context): Class<*>? {
            val packageName = context.packageName
            val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)

            val className = launchIntent?.component?.className ?: return null
            return Class.forName(className)

        }
    }
}