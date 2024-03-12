package com.unibalance

import com.google.gson.Gson
import java.util.Calendar
import kotlin.random.Random

class Alarm(val uid: String, val title: String, val description: String, hour: Int, minutes: Int, duration: Int, active: Boolean) : Cloneable {
    var hour = hour
    var minutes = minutes
    var active = active
    val duration = (duration - Random.nextInt(duration)) % 60
    fun getTime(): Long {
        val c = Calendar.getInstance()
        if (hour == 0 && minutes < duration) {  // alarm around midnight. Breaks at new year.
            c.set(Calendar.DAY_OF_YEAR, c.get(Calendar.DAY_OF_YEAR) - 1)
        }

        // alarm window goes to the previous day
        if (minutes < duration) {
            c.set(Calendar.HOUR_OF_DAY, (24 + hour - 1) % 24)
        } else {
            c.set(Calendar.HOUR_OF_DAY, hour)
        }

        c.set(Calendar.MINUTE, (60 + minutes - duration) % 60)

        // time has passed today, move alarm to tomorrow
        return if (c.before(Calendar.getInstance())) {
            c.timeInMillis + 1000 * 60 * 60 * 24
        } else {
            c.timeInMillis
        }
    }

    companion object {
        fun fromJson(json: String?): Alarm = Gson().fromJson(json, Alarm::class.java)

        fun toJson(alarm: Alarm): String = Gson().toJson(alarm)

    }

    @Throws(CloneNotSupportedException::class)
    override fun clone(): Any {
        return super.clone()
    }

    override fun equals(other: Any?): Boolean {
        if (other == this) return true
        if (other == null) return false
        if (other !is Alarm) return false
        return (
                this.hour == other.hour && this.minutes == other.minutes &&
                this.uid.equals(other.uid) &&
                this.title.equals(other.title) &&
                this.description.equals(other.description)
                )
    }
}