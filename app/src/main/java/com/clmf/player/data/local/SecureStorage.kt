package com.clmf.player.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.clmf.player.utils.AppLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Encrypts IPTV connection passwords at rest using the platform Keystore
 * (via Jetpack Security). Only opaque encrypted blobs are ever persisted in
 * Room; the master key itself never leaves the Android Keystore.
 */
@Singleton
class SecureStorage @Inject constructor(
    @ApplicationContext context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "clmf_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun encryptPassword(connectionId: Long, plainPassword: String): String {
        val key = "conn_pwd_$connectionId"
        prefs.edit().putString(key, plainPassword).apply()
        return key
    }

    fun decryptPassword(reference: String): String {
        return prefs.getString(reference, null).also {
            if (it == null) AppLogger.warning("Missing secure password reference")
        } ?: ""
    }

    fun removePassword(reference: String) {
        prefs.edit().remove(reference).apply()
    }
}
