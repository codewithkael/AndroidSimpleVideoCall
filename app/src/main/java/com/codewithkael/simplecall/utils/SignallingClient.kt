package com.codewithkael.simplecall.utils

import android.app.Application
import com.codewithkael.simplecall.remote.socket.SignalMessageModel
import com.codewithkael.simplecall.remote.socket.SignalMessageType
import com.codewithkael.simplecall.remote.socket.SocketClient
import javax.inject.Inject

class SignallingClient @Inject constructor(
    private val socketClient: SocketClient,
    private val application:Application
) {
    private val userID= UserIdHelper(application).getUserId()

    fun findUser(target: String) {
        socketClient.sendDataToHost(
            SignalMessageModel(
                type = SignalMessageType.FindUser,
                sender = userID,
                target = target
            )
        )
    }

    fun sendAutoStartCallSignal(target: String) {
        socketClient.sendDataToHost(
            SignalMessageModel(
                type = SignalMessageType.AutoStartCall,
                sender = userID,
                target = target
            )
        )
    }

    fun sendStartCallSignal(target: String) {
        socketClient.sendDataToHost(
            SignalMessageModel(
                type = SignalMessageType.StartCall,
                sender = userID,
                target = target
            )
        )
    }

    fun sendRejectCall(target: String) {
        socketClient.sendDataToHost(
            SignalMessageModel(
                type = SignalMessageType.RejectCall,
                sender = userID,
                target = target
            )
        )
    }

    fun sendAcceptCall(target: String) {
        socketClient.sendDataToHost(
            SignalMessageModel(
                type = SignalMessageType.AcceptCall,
                sender = userID,
                target = target
            )
        )
    }

    fun sendEndCall(target: String) {
        socketClient.sendDataToHost(
            SignalMessageModel(
                type = SignalMessageType.EndCall,
                sender = userID,
                target = target
            )
        )
    }
}