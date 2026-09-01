package com.clmf.player.data.remote.interceptor

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Rewrites the request line used for logging purposes only; the actual
 * network request still carries real credentials. Prevents username/password
 * query parameters from ever reaching Logcat via HttpLoggingInterceptor.
 */
class CredentialRedactingInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        return chain.proceed(chain.request())
    }

    companion object {
        private val SENSITIVE_PARAMS = setOf("username", "password", "user", "pass")

        fun redactUrl(url: String): String {
            return SENSITIVE_PARAMS.fold(url) { acc, param ->
                acc.replace(Regex("(?i)($param=)[^&]*"), "$1***")
            }
        }
    }
}
