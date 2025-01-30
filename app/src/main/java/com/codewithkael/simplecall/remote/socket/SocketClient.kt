package com.codewithkael.simplecall.remote.socket

import android.util.Log
import com.google.gson.Gson
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SocketClient @Inject constructor(
    private val gson:Gson
) {

    private var socketClient:WebSocketClient? = null
    fun init(
        socketUrl:String,
        listener:SocketCallback
    ){
        if (socketClient==null){
            socketClient = object :WebSocketClient(URI(socketUrl)){
                override fun onOpen(handshakedata: ServerHandshake?) {
                    listener.onRemoteSocketClientOpened()
                }

                override fun onMessage(message: String?) {
                    Log.d("TAG", "onMessage Receive: $message")
                    runCatching {
                        gson.fromJson(message.toString(),SignalMessageModel::class.java)
                    }.onSuccess {
                        listener.onRemoteSocketClientNewMessage(it)
                    }
                }

                override fun onClose(code: Int, reason: String?, remote: Boolean) {
                    listener.onRemoteSocketClientClosed()
                }

                override fun onError(ex: java.lang.Exception?) {
                    listener.onRemoteSocketClientConnectionError(ex)
                }
            }.apply {
                connect()
            }
        } else {
            listener.onRemoteSocketClientOpened()
        }
    }

    fun sendDataToHost(data:Any){
        Log.d("TAG", "onMessage Send: $data")

        runCatching {
            socketClient?.send(gson.toJson(data))
        }
    }

    fun close(){
        socketClient?.close()
    }


    interface SocketCallback {
        fun onRemoteSocketClientOpened()
        fun onRemoteSocketClientClosed()
        fun onRemoteSocketClientConnectionError(e: Exception?)
        fun onRemoteSocketClientNewMessage(message: SignalMessageModel)
    }
}