package com.codewithkael.simplecall.utils

object Constants {
    const val TIME_OUT_DURATION_MS = 20000L
    fun getWebSocketUrl(username:String,token:String) = "ws://10.0.2.2:3007/?token=$token&username=$username"
//    fun getWebSocketUrl(username:String) = "ws://95.217.13.89:3007/?username=$username"
}