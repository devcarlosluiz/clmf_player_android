package com.clmf.player.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.clmf.player.utils.DeviceIdentifier
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

private val Context.licenseDataStore by preferencesDataStore(name = "clmf_license")

sealed class LicenseStatus {
    data class Trial(val daysRemaining: Int) : LicenseStatus()
    data object Licensed : LicenseStatus()
    data object Expired : LicenseStatus()
}

/**
 * Offline license gate: a 7-day trial starts on first launch, after which a
 * device-bound activation key is required. The key is an HMAC-SHA256 of the
 * device ID keyed with [LICENSE_SECRET] — anyone holding that secret (the
 * seller) can compute a valid key for a customer's device ID. This is a
 * client-side check only: it deters casual copying but, like any offline
 * license scheme, can be bypassed by someone willing to decompile the APK.
 * Change [LICENSE_SECRET] before shipping a build you intend to sell.
 */
@Singleton
class LicenseManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val deviceIdentifier: DeviceIdentifier
) {
    private object Keys {
        val TRIAL_START = longPreferencesKey("trial_start_millis")
        val ACTIVATION_KEY = stringPreferencesKey("activation_key")
    }

    val status: Flow<LicenseStatus> = context.licenseDataStore.data.map { prefs ->
        val activationKey = prefs[Keys.ACTIVATION_KEY]
        if (activationKey != null && isValidKey(activationKey)) {
            LicenseStatus.Licensed
        } else {
            val trialStart = prefs[Keys.TRIAL_START]
            val daysElapsed = trialStart?.let {
                TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - it)
            } ?: 0L
            val daysRemaining = (TRIAL_DAYS - daysElapsed).toInt()
            if (daysRemaining > 0) LicenseStatus.Trial(daysRemaining) else LicenseStatus.Expired
        }
    }

    suspend fun ensureTrialStarted() {
        context.licenseDataStore.edit { prefs ->
            if (prefs[Keys.TRIAL_START] == null) {
                prefs[Keys.TRIAL_START] = System.currentTimeMillis()
            }
        }
    }

    suspend fun activate(rawKey: String): Boolean {
        val normalized = normalizeKey(rawKey)
        if (!isValidKey(normalized)) return false
        context.licenseDataStore.edit { prefs -> prefs[Keys.ACTIVATION_KEY] = normalized }
        return true
    }

    fun deviceId(): String = deviceIdentifier.deviceId()

    private fun isValidKey(key: String): Boolean =
        normalizeKey(key) == expectedKeyFor(deviceIdentifier.deviceId())

    private fun normalizeKey(key: String): String = key.trim().uppercase().replace("-", "")

    companion object {
        const val TRIAL_DAYS = 7L

        // Keep this identical to LICENSE_SECRET in tools/generate_license_key.py.
        // Never set this to a device ID — it must be a secret only you know,
        // independent of any customer's device.
        private const val LICENSE_SECRET = "50a733709fa9fc397a720c3179b0693081e51fe3415b6b31"

        /** Same algorithm used by tools/generate_license_key.py — keep both in sync. */
        fun expectedKeyFor(deviceId: String): String {
            val mac = Mac.getInstance("HmacSHA256")
            mac.init(SecretKeySpec(LICENSE_SECRET.toByteArray(), "HmacSHA256"))
            val digest = mac.doFinal(deviceId.toByteArray())
            return digest.joinToString("") { "%02X".format(it) }.take(16)
        }

        /** Same formatting as the keygen script, for display purposes (XXXX-XXXX-XXXX-XXXX). */
        fun formatKey(rawKey: String): String = rawKey.chunked(4).joinToString("-")
    }
}
