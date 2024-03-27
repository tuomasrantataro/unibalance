package com.unibalance

import android.content.Context
import android.util.Log
import java.time.Instant
import java.time.format.DateTimeFormatter

class Manager {
    //private val TAG = "AlarmManager"

    //private lateinit var sound: Sound
    //private var activeAlarmUid: String? = null

    companion object {
        private val TAG = "AlarmManager"
        private var activeAlarmUid: String? = null
        private var sound: Sound? = null

        var started = false;
        //private var id: Int = 0
        fun schedule(context: Context, alarm: Alarm) {
            //val time = System.currentTimeMillis() + 1000 * 30   // 30 sec from now
            val time = alarm.getTime()
            //id = (Math.random() * 10000000).toInt()

            val timeString = DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochMilli(time))


            Log.d(TAG, "Scheduled alarm, time: ${alarm.hour} : ${alarm.minutes - alarm.duration} -- remove ${alarm.duration} minutes -- $timeString")

            Helper.scheduleAlarm(context, alarm.uid, time, 3)
            Storage.saveAlarm(context, alarm)
        }

        fun removeAll(context: Context) {
            val alarms = Storage.getAllAlarms(context)
            for (alarm in alarms) {
                remove(context, alarm.uid)
            }
        }
        fun remove(context: Context, alarmUid: String) {
            sound?.stop()
            val alarm = Storage.getAlarm(context, alarmUid)
            Storage.removeAlarm(context, alarm.uid)
            Helper.cancelAlarm(context, 3)
        }

        fun start(context: Context, alarmUid: String) {
            // TODO: vaihda käynnistämään pollaus

            if (started) {
                return
            }

            activeAlarmUid = alarmUid
            sound = Sound(context)
            sound?.play("default")

            started = true

            Log.d(TAG, "Starting $activeAlarmUid")
        }

        fun stop(context: Context) {
            Log.d(TAG, "Stopping $activeAlarmUid")
            started = false

            sound?.stop()
            val alarm = Storage.getAlarm(context, activeAlarmUid)

            alarm.active = false
            Storage.saveAlarm(context, alarm)

            activeAlarmUid = null
        }
    }
}