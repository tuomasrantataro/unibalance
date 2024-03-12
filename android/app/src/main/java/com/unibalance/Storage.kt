package com.unibalance

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

import kotlin.collections.Map
import kotlin.collections.List

class Storage {

    companion object {

        fun saveAlarm(context: Context, alarm: Alarm) {
            val editor = getEditor(context)
            editor.putString(alarm.uid, Alarm.toJson(alarm))
            editor.apply()
        }

        fun getAllAlarms(context: Context): List<Alarm> {
            val alarms: MutableList<Alarm> = mutableListOf()
            val preferences = getSharedPreferences(context)
            val keyMap: Map<String, *> = preferences.all
            for (entry in keyMap) {
                alarms.add(Alarm.fromJson(entry.value as String))
            }
            return alarms.toList()
        }

        fun getAlarm(context: Context, alarmUid: String?): Alarm {
            val preferences = getSharedPreferences(context)
            val pref = preferences.getString(alarmUid, null)
            Log.d("AlarmStorage", "getAlarm: $alarmUid $preferences $pref")
            return Alarm.fromJson(pref)
        }

        fun removeAlarm(context: Context, alarmUid: String) {
            remove(context, alarmUid)
        }

        private fun remove(context: Context, id: String) {
            val preferences = getSharedPreferences(context)
            val editor = preferences.edit()
            editor.remove(id)
            editor.apply()
        }


        private fun getEditor(context: Context): SharedPreferences.Editor {
            val sharedPreferences = getSharedPreferences(context)
            return sharedPreferences.edit()
        }

        private fun getSharedPreferences(context: Context): SharedPreferences {
            val fileKey = context.resources.getString(R.string.notification_channel_id)
            return context.getSharedPreferences(fileKey, Context.MODE_PRIVATE)
        }
    }
}