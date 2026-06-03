package com.growbolt.sdk

/**
 * Configuration for the Growbolt SDK.
 *
 * @param sdkToken       Opaque SdkToken issued by your backend via POST /api/v1/sdk/token/.
 * @param userId         Stable user identifier — used as sub4 in Ongoing lookups.
 * @param baseUrl        Your API base URL. Defaults to production.
 * @param currencySymbol Symbol shown next to reward amounts. Defaults to rupee.
 * @param debug          Enable verbose logging. Always set false in production.
 */
data class GrowboltConfig(
    val sdkToken: String,
    val userId: String,
    val baseUrl: String = "https://admin.growbolt.ai",
    val currencySymbol: String = "₹",
    val debug: Boolean = false
) {
    init {
        require(sdkToken.isNotBlank()) { "GrowboltConfig: sdkToken must not be blank." }
        require(userId.isNotBlank()) { "GrowboltConfig: userId must not be blank." }
        require(baseUrl.isNotBlank()) { "GrowboltConfig: baseUrl must not be blank." }
    }
}
