package com.codewithkael.simplecall.utils

import android.app.Application
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp
import java.util.UUID

@HiltAndroidApp
class SimpleCallApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
    companion object {
        val USER_ID = UUID.randomUUID().toString().substring(0,5)
    }
}