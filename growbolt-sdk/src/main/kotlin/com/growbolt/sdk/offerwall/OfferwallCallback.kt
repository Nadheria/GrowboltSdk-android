package com.growbolt.sdk.offerwall

/**
 * Callback interface for Offerwall lifecycle and reward events.
 *
 * Register via GrowboltSdk.registerOfferwallCallback(callback)
 * Unregister via GrowboltSdk.unregisterOfferwallCallback() in onDestroy().
 */
interface OfferwallCallback {
    /** Offerwall has started loading offers from the network. */
    fun onOfferwallLoadingStarted() {}

    /** Offerwall has successfully loaded and is visible. */
    fun onOfferwallLoadingFinished() {}

    /** Offerwall failed to load. [error] contains the reason. */
    fun onOfferwallLoadingFailed(error: String) {}

    /**
     * User completed an offer and earned a reward.
     * @param amount   Reward amount from your backend.
     * @param currency Currency symbol (e.g. "₹").
     */
    fun onOfferwallRewardReceived(amount: Double, currency: String) {}

    /** User closed the offerwall. */
    fun onOfferwallClosed() {}
}
