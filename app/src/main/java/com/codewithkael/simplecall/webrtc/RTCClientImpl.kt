package com.codewithkael.simplecall.webrtc

import android.app.Application
import com.codewithkael.simplecall.remote.socket.SignalMessageModel
import com.codewithkael.simplecall.remote.socket.SignalMessageType
import com.codewithkael.simplecall.utils.SimpleCallApplication
import com.codewithkael.simplecall.utils.UserIdHelper
import com.google.gson.Gson
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.PeerConnection
import org.webrtc.SessionDescription

class RTCClientImpl(
    connection: PeerConnection,
    private val transferListener:TransferDataToServerCallBack,
    private val gson:Gson,
    private val application: Application
) : RTCClient {
    private val userID= UserIdHelper(application).getUserId()

    private val mediaConstraints = MediaConstraints().apply {
        mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo","true"))
        mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio","true"))
    }

    override val peerConnection: PeerConnection = connection

    override fun onDestroy() {
        runCatching {
            peerConnection.close()
        }
    }

    override fun offer(target: String) {
        peerConnection.createOffer(object :MySdpObserver(){
            override fun onCreateSuccess(desc: SessionDescription?) {
                super.onCreateSuccess(desc)
                peerConnection.setLocalDescription(MySdpObserver(),desc)
                transferListener.onTransferEventToSocket(
                    SignalMessageModel(
                        type = SignalMessageType.Offer,
                        sender = userID,
                        target = target,
                        data = desc?.description
                    )
                )
            }
        },mediaConstraints)
    }

    override fun answer(target: String) {
        peerConnection.createAnswer(object : MySdpObserver(){
            override fun onCreateSuccess(desc: SessionDescription?) {
                super.onCreateSuccess(desc)
                peerConnection.setLocalDescription(MySdpObserver(),desc)
                transferListener.onTransferEventToSocket(
                    SignalMessageModel(
                        type= SignalMessageType.Answer,
                        sender = userID,
                        target = target,
                        data = desc?.description
                    )
                )
            }
        },mediaConstraints)
    }

    override fun onRemoteSessionReceived(sessionDescription: SessionDescription) {
        peerConnection.setRemoteDescription(MySdpObserver(),sessionDescription)
    }

    override fun onIceCandidateReceived(iceCandidate: IceCandidate) {
        peerConnection.addIceCandidate(iceCandidate)
    }

    override fun onLocalIceCandidateGenerated(iceCandidate: IceCandidate, target: String) {
        peerConnection.addIceCandidate(iceCandidate)
        transferListener.onTransferEventToSocket(
            SignalMessageModel(
                type = SignalMessageType.ICE,
                sender = userID,
                target = target,
                data = gson.toJson(iceCandidate)
            )
        )
    }


    interface TransferDataToServerCallBack{
        fun onTransferEventToSocket(data:SignalMessageModel)
    }


}