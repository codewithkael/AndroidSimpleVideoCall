package com.codewithkael.simplecall.utils

import android.content.Context
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri

object RingtonePlayer {
    private var ringtone: Ringtone? = null

    fun start(context: Context) {
        try {
            val notification: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            ringtone = RingtoneManager.getRingtone(context.applicationContext, notification)
            ringtone?.play()
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun stop() {
        try {
            ringtone?.stop()
        } catch (e: Exception) { e.printStackTrace() }
    }
}
