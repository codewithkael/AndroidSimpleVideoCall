package com.codewithkael.simplecall.remote.socket

data class SignalMessageModel(
    val type: SignalMessageType,
    val sender:String,
    val target:String,
    val data:Any?=null
)

enum class SignalMessageType {
    FindUser,UserOnline,UserOffline,StartCall,AutoStartCall,AcceptCall,RejectCall,Offer,Answer,ICE,EndCall,UserOfflineWithNotification
}
