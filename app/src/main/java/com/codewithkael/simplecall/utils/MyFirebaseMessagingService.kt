package com.codewithkael.simplecall.utils

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.codewithkael.simplecall.R
import com.codewithkael.simplecall.ui.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    companion object {
        const val CHANNEL_ID = "call_channel"
        const val NOTIF_ID = 1001
        const val ACTION_ACCEPT = "com.example.ACTION_ACCEPT"
        const val ACTION_REJECT = "com.example.ACTION_REJECT"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // send to server (similar to earlier)
        // keep simple: use an IntentService or coroutine + Retrofit
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        remoteMessage.data.let { data ->
            Log.d("Masoud TAG", "onMessageReceived: $data")
            when (data["type"]) {
                "incoming_call" -> {
                    val callId = data["callId"] ?: return
                    val callerId = data["callerId"] ?: "unknown"
                    val callerName = data["callerName"] ?: "Unknown"
                    showIncomingCallNotification(callId, callerId, callerName)
                }
                // other types...
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun showIncomingCallNotification(callId: String, callerId: String, callerName: String) {
        createNotificationChannel()

//        // Full-screen intent - opens your incoming call Activity
//        val fullScreenIntent = Intent(this, IncomingCallActivity::class.java).apply {
//            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
//            putExtra("callId", callId)
//            putExtra("callerId", callerId)
//            putExtra("callerName", callerName)
//        }
//        val fullScreenPendingIntent = PendingIntent.getActivity(
//            this, callId.hashCode(), fullScreenIntent,
//            PendingIntent.FLAG_UPDATE_CURRENT or getImmutableFlag()
//        )
//
        // Accept action (BroadcastReceiver)
//        val acceptIntent = Intent(this, CallActionReceiver::class.java).apply {
        val acceptIntent = Intent(this, MainActivity::class.java).apply {
            action = ACTION_ACCEPT
            putExtra("callId", callId)
            putExtra("callerId", callerId)
        }
        val acceptPending = PendingIntent.getBroadcast(
            this, callId.hashCode() + 1, acceptIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or getImmutableFlag()
        )

        // Reject action
        val rejectIntent = Intent(this, MainActivity::class.java).apply {
            action = ACTION_REJECT
            putExtra("callId", callId)
            putExtra("callerId", callerId)
        }
        val rejectPending = PendingIntent.getBroadcast(
            this, callId.hashCode() + 2, rejectIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or getImmutableFlag()
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_call) // add icon
            .setContentTitle("$callerName is calling")
            .setContentText("Tap to answer")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
//            .setFullScreenIntent(fullScreenPendingIntent, true)
            .addAction(NotificationCompat.Action.Builder(
                android.R.drawable.sym_action_call, "Accept", acceptPending
            ).build())
            .addAction(NotificationCompat.Action.Builder(
                android.R.drawable.ic_menu_close_clear_cancel, "Reject", rejectPending
            ).build())
            .setAutoCancel(true)

        // Play ringtone
        RingtonePlayer.start(this)

        with(NotificationManagerCompat.from(this)) {
            notify(NOTIF_ID + callId.hashCode(), builder.build())
        }

        // Optionally set a timeout to auto-decline after N seconds
        Handler(Looper.getMainLooper()).postDelayed({
            // if still ringing, auto-reject
            // implement logic to check if call accepted; for brevity, just broadcast reject
            sendBroadcast(Intent(ACTION_REJECT).apply {
                putExtra("callId", callId)
                putExtra("callerId", callerId)
            })
        }, 30000) // 30s timeout
    }

    private fun getImmutableFlag(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(CHANNEL_ID, "Calls", NotificationManager.IMPORTANCE_HIGH)
            channel.description = "Incoming call notifications"
            channel.setSound(null, null) // we play ringtone ourselves
            nm.createNotificationChannel(channel)
        }
    }
}
