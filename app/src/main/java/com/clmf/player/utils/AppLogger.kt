package com.clmf.player.utils

import android.util.Log
import com.clmf.player.BuildConfig

/**
 * Central logging facade. All logging is stripped in release builds and every
 * message is sanitized so credentials/tokens never reach Logcat.
 */
object AppLogger {

    private const val DEFAULT_TAG = "CLMFPlayer"

    fun debug(message: String, tag: String = DEFAULT_TAG) {
        if (BuildConfig.DEBUG) Log.d(tag, sanitize(message))
    }

    fun info(message: String, tag: String = DEFAULT_TAG) {
        if (BuildConfig.DEBUG) Log.i(tag, sanitize(message))
    }

    fun warning(message: String, tag: String = DEFAULT_TAG) {
        if (BuildConfig.DEBUG) Log.w(tag, sanitize(message))
    }

    fun error(message: String, throwable: Throwable? = null, tag: String = DEFAULT_TAG) {
        if (BuildConfig.DEBUG) Log.e(tag, sanitize(message), throwable)
    }

    /**
     * Strips query parameters (username/password/tokens) and common credential
     * key-value pairs from any string before it is ever logged.
     */
    private fun sanitize(message: String): String {
        var sanitized = message
        sanitized = sanitized.replace(Regex("(?i)(username|password|user|pass|token)=[^&\\s\"']+"), "$1=***")
        sanitized = sanitized.replace(Regex("(?i)(https?://[^\\s]*?)\\?[^\\s\"']+"), "$1?***")
        return sanitized
    }
}
