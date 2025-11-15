package com.codewithkael.simplecall.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.codewithkael.simplecall.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CallActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val callId = intent.getStringExtra("callId") ?: return
        val callerId = intent.getStringExtra("callerId") ?: ""
        when (intent.action) {
            MyFirebaseMessagingService.ACTION_ACCEPT -> {
                Log.d("Masoud TAG", "onReceive: Accepted $intent")
                // stop ringtone + cancel notification
                RingtonePlayer.stop()
                NotificationManagerCompat.from(context).cancel(MyFirebaseMessagingService.NOTIF_ID + callId.hashCode())

                // Open the app's call activity
//                val activityIntent = Intent(context, CallAcceptedActivity::class.java).apply {
                val activityIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra("callId", callId)
                    putExtra("callerId", callerId)
                }
                context.startActivity(activityIntent)

                // Notify server that call accepted
                CoroutineScope(Dispatchers.IO).launch {
                    try {
//                        ApiClient.apiService.callResponse(mapOf(
//                            "callId" to callId,
//                            "responderId" to myUserId,
//                            "response" to "accepted"
//                        ))
                    } catch (e: Exception) { }
                }
            }

            MyFirebaseMessagingService.ACTION_REJECT -> {
                Log.d("Masoud TAG", "onReceive: REJECTED $intent")

                RingtonePlayer.stop()
                NotificationManagerCompat.from(context).cancel(MyFirebaseMessagingService.NOTIF_ID + callId.hashCode())
                CoroutineScope(Dispatchers.IO).launch {
                    try {
//                        ApiClient.apiService.callResponse(mapOf(
//                            "callId" to callId,
//                            "responderId" to myUserId,
//                            "response" to "rejected"
//                        ))
                    } catch (e: Exception) { }
                }
            }
        }
    }
}
