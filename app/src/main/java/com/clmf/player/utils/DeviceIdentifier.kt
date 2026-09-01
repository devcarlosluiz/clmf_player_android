package com.clmf.player.utils

import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.WifiManager
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reads device identifiers used for license activation.
 *
 * Android 6+ (API 23+) always returns the sentinel "02:00:00:00:00:00" for
 * the Wi-Fi MAC address regardless of the real hardware address — this is an
 * OS-level privacy restriction, not something this app can bypass without
 * root. Because that sentinel is identical on every device, it cannot be
 * used to uniquely identify a device for licensing. [deviceId] (backed by
 * `Settings.Secure.ANDROID_ID`) is used for that purpose instead; [macAddress]
 * is exposed only for display purposes.
 */
@Singleton
class DeviceIdentifier @Inject constructor(
    @ApplicationContext private val context: Context
) {
    @SuppressLint("HardwareIds")
    fun deviceId(): String =
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "UNKNOWN"

    @Suppress("DEPRECATION")
    fun macAddress(): String {
        return try {
            val wifiManager = context.applicationContext
                .getSystemService(Context.WIFI_SERVICE) as? WifiManager
            wifiManager?.connectionInfo?.macAddress?.takeIf { it.isNotBlank() } ?: FALLBACK_MAC
        } catch (t: Throwable) {
            AppLogger.warning("Unable to read MAC address: ${t.message}")
            FALLBACK_MAC
        }
    }

    /** True when [macAddress] returned the OS privacy sentinel instead of a real address. */
    fun isMacRestricted(): Boolean = macAddress() == FALLBACK_MAC

    companion object {
        private const val FALLBACK_MAC = "02:00:00:00:00:00"
    }
}
