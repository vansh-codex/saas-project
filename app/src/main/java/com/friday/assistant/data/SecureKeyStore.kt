package com.friday.assistant.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Persists secrets in an [EncryptedSharedPreferences] file whose master key lives in the
 * Android keystore.
 *
 * No API key is ever compiled into the app. The value only exists after the user enters it
 * on the Settings screen, and it never leaves the device except as a request header to the
 * provider the user configured.
 */
class SecureKeyStore(context: Context) {

    private val appContext: Context = context.applicationContext

    private val preferences: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(appContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            appContext,
            FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    fun readGeminiApiKey(): String? =
        preferences.getString(KEY_GEMINI_API_KEY, null)?.takeIf { it.isNotBlank() }

    fun writeGeminiApiKey(apiKey: String?) {
        val editor = preferences.edit()
        if (apiKey.isNullOrBlank()) {
            editor.remove(KEY_GEMINI_API_KEY)
        } else {
            editor.putString(KEY_GEMINI_API_KEY, apiKey.trim())
        }
        editor.apply()
    }

    companion object {
        /** Also referenced from the backup exclusion rules in `res/xml`. */
        const val FILE_NAME: String = "friday_secure_settings"

        private const val KEY_GEMINI_API_KEY: String = "gemini_api_key"
    }
}
