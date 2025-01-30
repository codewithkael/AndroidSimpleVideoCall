package com.codewithkael.simplecall.utils

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import java.util.UUID

@HiltAndroidApp
class SimpleCallApplication : Application() {
    companion object {
        val USER_ID = UUID.randomUUID().toString().substring(0,5)
    }
}