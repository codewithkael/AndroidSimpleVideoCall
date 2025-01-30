package com.codewithkael.simplecall.ui.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codewithkael.simplecall.remote.socket.SignalMessageModel
import com.codewithkael.simplecall.remote.socket.SignalMessageType
import com.codewithkael.simplecall.remote.socket.SignalMessageType.AcceptCall
import com.codewithkael.simplecall.remote.socket.SignalMessageType.Answer
import com.codewithkael.simplecall.remote.socket.SignalMessageType.EndCall
import com.codewithkael.simplecall.remote.socket.SignalMessageType.ICE
import com.codewithkael.simplecall.remote.socket.SignalMessageType.Offer
import com.codewithkael.simplecall.remote.socket.SignalMessageType.RejectCall
import com.codewithkael.simplecall.remote.socket.SignalMessageType.StartCall
import com.codewithkael.simplecall.remote.socket.SignalMessageType.UserOnline
import com.codewithkael.simplecall.remote.socket.SocketClient
import com.codewithkael.simplecall.utils.ConnectionState
import com.codewithkael.simplecall.utils.ConnectionState.CallingTarget
import com.codewithkael.simplecall.utils.ConnectionState.New
import com.codewithkael.simplecall.utils.ConnectionState.WaitingForCall
import com.codewithkael.simplecall.utils.Constants.TIME_OUT_DURATION_MS
import com.codewithkael.simplecall.utils.Constants.getWebSocketUrl
import com.codewithkael.simplecall.utils.SignallingClient
import com.codewithkael.simplecall.utils.SimpleCallApplication
import com.codewithkael.simplecall.webrtc.MyPeerObserver
import com.codewithkael.simplecall.webrtc.RTCAudioManager
import com.codewithkael.simplecall.webrtc.RTCClient
import com.codewithkael.simplecall.webrtc.RTCClientImpl
import com.codewithkael.simplecall.webrtc.WebRTCFactory
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.SessionDescription
import org.webrtc.SurfaceViewRenderer
import javax.inject.Inject

@SuppressLint("StaticFieldLeak")
@HiltViewModel
class MainViewModel @Inject constructor(
    private val socketClient: SocketClient,
    private val signalSender: SignallingClient,
    private val webrtcFactory: WebRTCFactory,
    private val gson :Gson,
    application: Application
) : ViewModel() {

    var connectionState: MutableStateFlow<ConnectionState> = MutableStateFlow(New)
    private fun setConnectionState(state: ConnectionState) {
        connectionState.value = state
    }

    val eventState: MutableSharedFlow<String> = MutableSharedFlow(replay = 0)
    private var target: String = ""
    private var callTimeoutJob: Job? = null
    private var rtcClient: RTCClient? = null
    private var remoteSurface:SurfaceViewRenderer?=null
    private val rtcAudioManager by lazy { RTCAudioManager.create(application) }

    init {
        rtcAudioManager.setDefaultAudioDevice(RTCAudioManager.AudioDevice.SPEAKER_PHONE)
    }

    fun connectSocket() {
        socketClient.init(getWebSocketUrl(SimpleCallApplication.USER_ID),
            object : SocketClient.SocketCallback {
                override fun onRemoteSocketClientOpened() {
                    setConnectionState(WaitingForCall)
                }

                override fun onRemoteSocketClientClosed() {
                }

                override fun onRemoteSocketClientConnectionError(e: Exception?) {
                }

                override fun onRemoteSocketClientNewMessage(message: SignalMessageModel) {
                    handleIncomingMessage(message)
                }
            })
    }

    private fun handleIncomingMessage(message: SignalMessageModel) {
        when (message.type) {
            UserOnline -> handleUserOnline(message)
            SignalMessageType.UserOffline -> handleUserOffline(message)
            StartCall -> handleStartCall(message)
            AcceptCall -> handleAcceptCall(message)
            RejectCall -> handleRejectCall()
            Offer -> handleOffer(message)
            Answer -> handleAnswer(message)
            ICE -> handleICE(message)
            EndCall -> handleEndCall()
            else -> {}
        }
    }

    private fun handleEndCall() {
        finishCall()
    }

    private fun handleICE(message: SignalMessageModel) {
        runCatching {
            val iceCandidate = gson.fromJson(message.data.toString(),IceCandidate::class.java)
            rtcClient?.onIceCandidateReceived(iceCandidate)
        }
    }

    private fun handleAnswer(message: SignalMessageModel) {
        val sessionDescription =
            SessionDescription(SessionDescription.Type.ANSWER,message.data.toString())
        rtcClient?.onRemoteSessionReceived(sessionDescription)
    }

    private fun handleOffer(message: SignalMessageModel) {
        this.target = message.sender
        val sessionDescription =
            SessionDescription(SessionDescription.Type.OFFER,message.data.toString())
        setupRTCConnection(message.sender)?.also {
            it.onRemoteSessionReceived(sessionDescription)
            it.answer(message.sender)
        }
    }

    private fun handleAcceptCall(message: SignalMessageModel) {
        setConnectionState(ConnectionState.OnCall(message.sender))
        setupRTCConnection(message.sender)?.offer(message.sender)
    }

    private fun handleRejectCall() {
        setConnectionState(WaitingForCall)
        viewModelScope.launch {
            eventState.emit("Call Rejected")
        }
        webrtcFactory.onDestroy()
    }

    private fun handleStartCall(message: SignalMessageModel) {
        if (connectionState.value is ConnectionState.OnCall) {
            signalSender.sendRejectCall(message.sender)
            return
        }
        setConnectionState(ConnectionState.ReceivedCall(message.sender))
    }

    private fun handleUserOnline(message: SignalMessageModel) {
        setConnectionState(CallingTarget(message.target))
        startCallWithTime(message.target)
    }

    private fun startCallWithTime(target: String) {
        this.target = target
        signalSender.sendStartCallSignal(target)
        callTimeoutJob?.cancel()
        callTimeoutJob = viewModelScope.launch {
            delay(TIME_OUT_DURATION_MS)
            if (connectionState.value is CallingTarget) {
                setConnectionState(WaitingForCall)
                webrtcFactory.onDestroy()
            }
        }
    }

    private fun handleUserOffline(message: SignalMessageModel) {
        setConnectionState(ConnectionState.UserOffline(message.target))
        viewModelScope.launch {
            eventState.emit("${message.target} is Offline")
        }
    }

    fun findUser(target: String) {
        if (target == SimpleCallApplication.USER_ID) {
            viewModelScope.launch {
                eventState.emit("You cannot call yourself")
            }
            return
        }
        setConnectionState(WaitingForCall)
        //send signal to the server
        signalSender.findUser(target)
    }

    fun incomingCallDismissed() {
        setConnectionState(WaitingForCall)
        webrtcFactory.onDestroy()
    }

    fun acceptIncomingCall(target: String) {
        setConnectionState(ConnectionState.OnCall(target))
        signalSender.sendAcceptCall(target)
    }

    fun rejectIncomingCall(target: String) {
        setConnectionState(WaitingForCall)
        signalSender.sendRejectCall(target)

    }

    fun onSurfaceLocalReady(localRenderer: SurfaceViewRenderer) {
        webrtcFactory.prepareLocalStream(localRenderer)
    }

    private fun setupRTCConnection(target: String): RTCClient? {
        runCatching {
            rtcClient?.onDestroy()
        }
        rtcClient = null
        rtcClient = webrtcFactory.createRTCClient(object : MyPeerObserver(){
            override fun onIceCandidate(p0: IceCandidate?) {
                super.onIceCandidate(p0)
                p0?.let { rtcClient?.onLocalIceCandidateGenerated(it,target) }
            }

            override fun onAddStream(p0: MediaStream?) {
                super.onAddStream(p0)
                p0?.let { stream ->
                    runCatching {
                        remoteSurface?.let { remote->
                            stream.videoTracks[0]?.addSink(remote)
                        }
                    }
                }
            }
        },
            object :RTCClientImpl.TransferDataToServerCallBack {
                override fun onTransferEventToSocket(data: SignalMessageModel) {
                    socketClient.sendDataToHost(data)
                }
            })
        return rtcClient
    }

    override fun onCleared() {
        super.onCleared()
        remoteSurface?.release()
        remoteSurface = null
        webrtcFactory.onDestroy()
        socketClient.close()
    }

    fun onSurfaceRemoteReady(remoteRenderer: SurfaceViewRenderer) {
        this.remoteSurface = remoteRenderer
        webrtcFactory.initSurfaceView(remoteRenderer)
    }

    fun switchCamera() {
        webrtcFactory.switchCamera()
    }

    fun endCall() {
        signalSender.sendEndCall(target)
        finishCall()
    }

    private fun finishCall(){
        rtcClient?.onDestroy()
        rtcClient = null
        webrtcFactory.onDestroy()
        setConnectionState(WaitingForCall)
    }

    fun toggleMic(enabled: Boolean) {
        webrtcFactory.toggleMic(enabled)
    }

    fun toggleCamera(enabled: Boolean){
        webrtcFactory.toggleCamera(enabled)
    }

    fun toggleSpeaker(speaker: Boolean) {
        if (speaker){
            rtcAudioManager.setDefaultAudioDevice(RTCAudioManager.AudioDevice.SPEAKER_PHONE)
        }else{
            rtcAudioManager.setDefaultAudioDevice(RTCAudioManager.AudioDevice.EARPIECE)
        }
    }
}