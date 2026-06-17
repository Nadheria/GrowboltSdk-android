package com.growbolt.sdk

/**
 * Configuration for the Growbolt SDK.
 *
 * @param sdkToken Opaque SdkToken issued by your backend via POST /api/v1/sdk/token/.
 * @param userId   Stable user identifier (phone, email, internal user id, etc.) — used as
 *                 sub4 in redeem/ongoing lookups. Pass null if calling init() before login
 *                 (e.g. from Application.onCreate()); call GrowboltSdk.identify(userId)
 *                 once the user logs in, and GrowboltSdk.reset() on logout.
 * @param baseUrl  Your API base URL. Defaults to production.
 * @param debug    Enable verbose logging. Always set false in production.
 */
data class GrowboltConfig(
    val sdkToken: String,
    val userId: String? = null,
    val baseUrl: String = "https://admin.growbolt.ai",
    val debug: Boolean
) {
    init {
        require(sdkToken.isNotBlank()) { "GrowboltConfig: sdkToken must not be blank." }
        require(baseUrl.isNotBlank()) { "GrowboltConfig: baseUrl must not be blank." }
    }
}