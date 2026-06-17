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
 *   // In Application.onCreate() — before the user has logged in:
 *   GrowboltSdk.init(context, GrowboltConfig(sdkToken = "..."))
 *
 *   // After your app's own login flow gives you a user identifier:
 *   GrowboltSdk.identify(userId = "user-phone-or-email-or-id")
 *
 *   // On logout:
 *   GrowboltSdk.reset()
 *
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
     * Safe to call before the user has logged in — pass [GrowboltConfig.userId] as null
     * (or simply omit it) and call [identify] later once your app knows who the user is.
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

        Logger.d("GrowboltSdk", "Initialised — userId=${config.userId ?: "not set"} baseUrl=${config.baseUrl}")
    }

    /**
     * Attach (or update) the user identifier after your app's own login flow completes.
     * Call this as soon as you have a stable identifier for the user (phone, email, internal id).
     *
     * Safe to call multiple times — e.g. if the identifier changes after a profile update.
     *
     * @param userId Stable user identifier — used as sub4 in redeem/ongoing lookups.
     */
    @JvmStatic
    fun identify(userId: String) {
        checkInitialised()
        require(userId.isNotBlank()) { "identify: userId must not be blank." }
        _config = config.copy(userId = userId)
        Logger.isEnabled = config.debug
        Logger.d("GrowboltSdk", "Identified — userId=$userId")
    }

    /**
     * Clear the current user identifier — call on logout.
     * The SDK remains initialised (token/config retained); only [GrowboltConfig.userId] is cleared.
     */
    @JvmStatic
    fun reset() {
        checkInitialised()
        _config = config.copy(userId = null)
        Logger.isEnabled = config.debug
        Logger.d("GrowboltSdk", "Reset — userId cleared.")
    }

    /**
     * Turn verbose SDK logging on or off at runtime, without re-initialising the SDK.
     *
     * Use this instead of calling [init] again just to flip logging — re-calling [init]
     * would also recreate the token manager and API client, which is unnecessary overhead.
     *
     * Note: this only controls [Logger] output (d/i/w/e calls throughout the SDK).
     * The OkHttp request/response body logging interceptor is wired up once at [init] time
     * based on [GrowboltConfig.debug] and cannot be toggled afterward without re-initialising.
     *
     * @param enabled true to enable verbose logcat output, false to silence it.
     */
    @JvmStatic
    fun setDebugEnabled(enabled: Boolean) {
        checkInitialised()
        _config = config.copy(debug = enabled)
        Logger.isEnabled = enabled
        Logger.d("GrowboltSdk", "Debug logging ${if (enabled) "enabled" else "disabled"}.")
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