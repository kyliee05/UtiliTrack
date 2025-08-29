package com.example.reportactivity.datastore // Make sure this matches your package name

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extension function for accessing DataStore instance
val Context.dataStore by preferencesDataStore(name = "user_preferences")

class UserPreferences(private val context: Context) {

    // Define the keys for storing preferences
    private val emailKey = stringPreferencesKey("user_email")

    // Save email to DataStore
    suspend fun saveEmail(email: String) {
        context.dataStore.edit { preferences ->
            preferences[emailKey] = email
        }
    }

    // Get saved email
    val getEmail: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[emailKey] // Get the stored email, returns null if not set
        }
}
