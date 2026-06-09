package com.growbolt.sdk.offerwall

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.growbolt.sdk.GrowboltSdk
import com.growbolt.sdk.network.ApiResult
import com.growbolt.sdk.network.model.RedeemRequest
import com.growbolt.sdk.network.model.RedeemResponse
import com.growbolt.sdk.network.safeApiCall
import com.growbolt.sdk.util.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

internal object OfferClickManager {

    private const val TAG = "OfferClickManager"
    private const val MAX_REDIRECTS = 10

    /**
     * Full flow matching BonusBuddy exactly:
     * 1. POST /sdk/offers/{id}/redeem/ → get ready-made URL
     * 2. Resolve HTTP redirects (hides tracking chain)
     * 3. Open final URL — handles http/https and market:// deep links
     */
    suspend fun handleClick(
        context: android.content.Context,
        offerId: Int,
        onLoading: (Boolean) -> Unit,
        onError: (String) -> Unit
    ) {
        onLoading(true)

        val config = GrowboltSdk.config

        // Collect all sub params
        val deviceId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        ) ?: "unknown"



        val sub3 = withContext(Dispatchers.IO) {
            try {
                com.google.android.gms.ads.identifier.AdvertisingIdClient
                    .getAdvertisingIdInfo(context).id ?: deviceId
            } catch (e: Exception) {
                Logger.w(TAG, "GAID unavailable, using device ID as fallback")
                deviceId
            }
        }

        val sub4 = config.userId
        val sub5 = deviceId
        val sub6 = "V1.0.0"
        val sub7 = "${Build.MANUFACTURER} ${Build.MODEL}".trim()

        Logger.d(TAG, "Redeeming offerId=$offerId sub3=$sub3 sub4=$sub4 sub5=$sub5 sub6=$sub6 sub7=$sub7")

        // Step 1 — call redeem API
        val result = safeApiCall {
            GrowboltSdk.apiClient.offersApi.redeemOffer(
                offerId,
                RedeemRequest(
                    sub3 = sub3,
                    sub4 = sub4,
                    sub5 = sub5,
                    sub6 = sub6,
                    sub7 = sub7
                )
            )
        }

        when (result) {
            is ApiResult.Success<*> -> {
                @Suppress("UNCHECKED_CAST")
                val data = (result as ApiResult.Success<RedeemResponse>).data
                val initialUrl = data.url
                Logger.d(TAG, "Redeem success — initialUrl: $initialUrl click_id: ${data.clickId}")

                // Step 2 — resolve redirects to get final URL (hides tracking chain)
                val finalUrl = resolveRedirects(initialUrl)
                Logger.d(TAG, "Final URL after redirect resolution: $finalUrl")

                onLoading(false)

                // Step 3 — open final URL
                openUrl(context, finalUrl ?: initialUrl)
            }

            is ApiResult.Error -> {
                onLoading(false)
                val msg = "Error ${result.code}: ${result.message}"
                Logger.e(TAG, msg)
                onError(msg)
            }

            is ApiResult.Exception -> {
                onLoading(false)
                val msg = result.throwable.message ?: "Network error"
                Logger.e(TAG, msg, result.throwable)
                onError(msg)
            }
        }
    }

    /**
     * Follows HTTP 3xx redirects exactly like BonusBuddy's resolveRedirects().
     * Returns the final URL after all redirects are resolved.
     * Handles market:// Play Store deep links.
     */
    private suspend fun resolveRedirects(initialUrl: String): String? {
        return withContext(Dispatchers.IO) {
            var connection: HttpURLConnection? = null
            try {
                var currentUrl = initialUrl
                var redirectCount = 0

                while (true) {
                    connection = (URL(currentUrl).openConnection() as HttpURLConnection).apply {
                        instanceFollowRedirects = false  // manual redirect following
                        connectTimeout = 5000
                        readTimeout = 5000
                        setRequestProperty(
                            "User-Agent",
                            "GrowboltSDK/1.0.0 Android/${Build.VERSION.RELEASE}"
                        )
                    }

                    val responseCode = connection.responseCode
                    val locationHeader = connection.getHeaderField("Location")

                    Logger.d(TAG, "Redirect [$redirectCount] code=$responseCode location=$locationHeader")

                    // Not a redirect — this is the final URL
                    if (responseCode / 100 != 3 || locationHeader == null) {
                        break
                    }

                    // Play Store deep link detected
                    if (locationHeader.startsWith("market://")) {
                        Logger.d(TAG, "Market URL detected: $locationHeader")
                        return@withContext locationHeader
                    }

                    // Resolve relative redirect URLs
                    currentUrl = URL(URL(currentUrl), locationHeader).toString()

                    if (++redirectCount >= MAX_REDIRECTS) {
                        Logger.w(TAG, "Max redirects ($MAX_REDIRECTS) reached")
                        break
                    }

                    connection.disconnect()
                }

                connection?.url?.toString() ?: currentUrl

            } catch (e: Exception) {
                Logger.e(TAG, "Redirect resolution failed: ${e.message}", e)
                null  // caller falls back to initialUrl
            } finally {
                connection?.disconnect()
            }
        }
    }

    /**
     * Opens a URL safely.
     * Handles:
     *   - https:// and http:// → browser
     *   - market:// → Play Store
     * Shows error if no app can handle the URL.
     */
    private fun openUrl(context: android.content.Context, url: String) {
        try {
            val uri = Uri.parse(url)
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val canHandle = context.packageManager
                .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
                .isNotEmpty()

            if (canHandle) {
                context.startActivity(intent)
            } else {
                Logger.w(TAG, "No app can handle URL: $url")
                android.widget.Toast.makeText(
                    context,
                    "No application can handle this request. Please install a browser.",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to open URL: $url", e)
        }
    }


}