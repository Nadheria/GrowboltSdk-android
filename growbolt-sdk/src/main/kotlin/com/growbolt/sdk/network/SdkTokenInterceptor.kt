package com.growbolt.sdk.network

import com.growbolt.sdk.core.TokenManager
import com.growbolt.sdk.util.Logger
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Injects "Authorization: SdkToken <key>" header on every /sdk/... request.
 * Also appends the SDK User-Agent header.
 */
internal class SdkTokenInterceptor(
    private val tokenManager: TokenManager,
    private val userAgent: String
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenManager.getToken()
        val requestBuilder = chain.request().newBuilder()
            .header("User-Agent", userAgent)

        if (!token.isNullOrBlank()) {
            requestBuilder.header("Authorization", "SdkToken $token")
            Logger.d(TAG, "Injecting SdkToken header")
        } else {
            Logger.w(TAG, "No SdkToken available — request will be unauthenticated")
        }

        val response = chain.proceed(requestBuilder.build())

        if (response.code == 401) {
            Logger.w(TAG, "401 Unauthorized — token may have been revoked. Call GrowboltSdk.updateToken().")
        }

        return response
    }

    companion object {
        private const val TAG = "SdkTokenInterceptor"
    }
}
