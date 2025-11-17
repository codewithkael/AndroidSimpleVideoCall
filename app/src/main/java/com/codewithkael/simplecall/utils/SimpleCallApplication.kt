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

}