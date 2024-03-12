package com.unibalance

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.provider.Settings
import android.util.Log
import java.lang.Exception
import java.lang.IllegalStateException

class Sound(val context: Context) {
    private val TAG = "AlarmSound"

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val userVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)
    private val mediaPlayer = MediaPlayer()

    fun play(sound: String) {
        val soundUri = getSoundUri(sound)
        playSound(soundUri)
    }
    fun stop() {
        try {
            if (mediaPlayer.isPlaying) {
                stopSound()
                mediaPlayer.release()
            }
        } catch (e: IllegalStateException) {
            Log.e(TAG, "Sound has probably been released already")
        }
    }

    private fun playSound(soundUri: Uri) {
        try {
            if (!mediaPlayer.isPlaying) {
                mediaPlayer.setScreenOnWhilePlaying(true)
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM)
                mediaPlayer.setDataSource(context, soundUri)
                mediaPlayer.setVolume(100F, 100F)
                mediaPlayer.isLooping = true
                mediaPlayer.prepare()
                mediaPlayer.start()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play sound: $e")
        }
    }

    private fun stopSound() {
        try {
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, userVolume, AudioManager.FLAG_PLAY_SOUND)
            mediaPlayer.stop()
            mediaPlayer.reset()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "ringtone: ${e.message}")
        }
    }

    private fun getSoundUri(soundName: String): Uri {
        return Settings.System.DEFAULT_ALARM_ALERT_URI
    }
}