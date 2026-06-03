package com.growbolt.sdk.core

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Securely persists the opaque SdkToken using Jetpack EncryptedSharedPreferences
 * (AES-256 GCM encryption backed by Android Keystore).
 */
internal class TokenManager(context: Context) {

    private val prefs by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context, PREFS_FILE, masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun saveToken(token: String) = prefs.edit().putString(KEY_SDK_TOKEN, token).apply()
    fun getToken(): String? = prefs.getString(KEY_SDK_TOKEN, null)
    fun clearToken() = prefs.edit().remove(KEY_SDK_TOKEN).apply()
    fun hasToken(): Boolean = !getToken().isNullOrBlank()

    companion object {
        private const val PREFS_FILE = "growbolt_secure_prefs"
        private const val KEY_SDK_TOKEN = "sdk_token"
    }
}
