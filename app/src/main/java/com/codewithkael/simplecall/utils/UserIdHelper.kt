package com.codewithkael.simplecall.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import java.util.UUID

class UserIdHelper(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)

    // Key for storing the user ID
    private val USER_ID_KEY = "USER_ID"

    // Get the user ID, generate and save it if it's the first time
    fun getUserId(): String {
        // Check if the user ID already exists in SharedPreferences
        val savedUserId = sharedPreferences.getString(USER_ID_KEY, null)

        return if (savedUserId != null) {
            // If already exists, return the saved ID
            savedUserId
        } else {
            // If not, generate a new one, save it, and return it
            val newUserId = generateRandomUserId()
            saveUserId(newUserId)
            newUserId
        }
    }

    // Generate a random 5-character user ID
    private fun generateRandomUserId(): String {
        return UUID.randomUUID().toString().substring(0, 5)
    }

    // Save the generated user ID to SharedPreferences
    private fun saveUserId(userId: String) {
        sharedPreferences.edit {
            putString(USER_ID_KEY, userId)
        }
    }
}
