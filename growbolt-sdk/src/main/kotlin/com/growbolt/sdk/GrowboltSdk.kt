package com.growbolt.sdk

import android.content.Context
import android.content.Intent
import com.growbolt.sdk.GrowboltSdk._tokenManager
import com.growbolt.sdk.core.TokenManager
import com.growbolt.sdk.network.GrowboltApiClient
import com.growbolt.sdk.offerwall.OfferwallActivity
import com.growbolt.sdk.offerwall.OfferwallCallback
import com.growbolt.sdk.util.Logger

/**
 * Main entry point for the Growbolt SDK.
 *
 * Usage:
 *   GrowboltSdk.init(context, GrowboltConfig(sdkToken = "...", userId = "..."))
 *   GrowboltSdk.showOfferwall(activity, callback)
 */
object GrowboltSdk {

    private var _config: GrowboltConfig? = null
    private var _tokenManager: TokenManager? = null
    private var _apiClient: GrowboltApiClient? = null

    internal val config: GrowboltConfig
        get() = _config ?: error("GrowboltSdk not initialised. Call GrowboltSdk.init() first.")

    internal val tokenManager: TokenManager
        get() = _tokenManager ?: error("GrowboltSdk not initialised.")

    internal val apiClient: GrowboltApiClient
        get() = _apiClient ?: error("GrowboltSdk not initialised.")

    // Holds the single callback registered by the host app
    internal var offerwallCallback: OfferwallCallback? = null

    /**
     * Initialise the SDK. Call once from Application.onCreate() or your first Activity.
     *
     * @param context Any context — the SDK retains only the application context.
     * @param config  SDK configuration including the SdkToken from your backend.
     */
    @JvmStatic
    fun init(context: Context, config: GrowboltConfig) {
        val appContext = context.applicationContext
        Logger.isEnabled = config.debug

        _config = config
        _tokenManager = TokenManager(appContext).also { it.saveToken(config.sdkToken) }
        _apiClient = GrowboltApiClient(config, _tokenManager!!)

        Logger.d("GrowboltSdk", "Initialised — userId=${config.userId} baseUrl=${config.baseUrl}")
    }

    /**
     * Show the full-screen offerwall.
     *
     * @param context  Activity or application context.
     * @param callback Lifecycle + reward callbacks. The SDK holds a strong reference;
     *                 unregister via [unregisterOfferwallCallback] in onDestroy().
     */
    @JvmStatic
    fun showOfferwall(context: Context, callback: OfferwallCallback? = null) {
        checkInitialised()
        callback?.let { offerwallCallback = it }
        val intent = Intent(context, OfferwallActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    /**
     * Register an offerwall callback separately (e.g. before calling showOfferwall).
     */
    @JvmStatic
    fun registerOfferwallCallback(callback: OfferwallCallback) {
        offerwallCallback = callback
    }

    /**
     * Unregister the callback — call in Activity.onDestroy() to avoid leaks.
     */
    @JvmStatic
    fun unregisterOfferwallCallback() {
        offerwallCallback = null
    }

    /**
     * Update the SDK token (e.g. after your backend issues a fresh one).
     */
    @JvmStatic
    fun updateToken(newToken: String) {
        checkInitialised()
        require(newToken.isNotBlank()) { "updateToken: token must not be blank." }
        tokenManager.saveToken(newToken)
        Logger.d("GrowboltSdk", "Token updated.")
    }

    /**
     * Returns true if the SDK has been initialised.
     */
    @JvmStatic
    fun isInitialised(): Boolean = _config != null

    private fun checkInitialised() {
        if (_config == null) error("GrowboltSdk not initialised. Call GrowboltSdk.init() first.")
    }
}
