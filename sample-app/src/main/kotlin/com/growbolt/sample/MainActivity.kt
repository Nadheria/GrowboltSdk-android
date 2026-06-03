package com.growbolt.sample

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.growbolt.sdk.GrowboltConfig
import com.growbolt.sdk.GrowboltSdk
import com.growbolt.sdk.offerwall.OfferwallCallback

/**
 * Demonstrates a minimal Growbolt SDK integration.
 *
 * Production flow:
 *   1. App → your backend: "give me an SDK token for this user"
 *   2. Backend → POST /api/v1/sdk/token/ (with dashboard JWT) → receives opaque SdkToken
 *   3. Backend → App: returns the SdkToken
 *   4. App → GrowboltSdk.init(config with that token)
 */
class MainActivity : AppCompatActivity() {

    private val offerwallCallback = object : OfferwallCallback {
        override fun onOfferwallLoadingStarted() {
            android.util.Log.d("Sample", "Offerwall loading started")
        }
        override fun onOfferwallLoadingFinished() {
            android.util.Log.d("Sample", "Offerwall loaded")
        }
        override fun onOfferwallLoadingFailed(error: String) {
            Toast.makeText(this@MainActivity, "Error: $error", Toast.LENGTH_SHORT).show()
        }
        override fun onOfferwallRewardReceived(amount: Double, currency: String) {
            Toast.makeText(this@MainActivity, "You earned $currency$amount!", Toast.LENGTH_LONG).show()
        }
        override fun onOfferwallClosed() {
            android.util.Log.d("Sample", "Offerwall closed")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        GrowboltSdk.init(
            context = this,
            config = GrowboltConfig(
                sdkToken = "tGbHWiQcj9HqHgZZmOv25rLlez63GtX8jJ8vXtLAnPc",  // Fetch from your backend
                userId = "user-123",
                baseUrl = "https://admin.growbolt.ai",
                currencySymbol = "₹",
                debug = true  // false in production
            )
        )

        GrowboltSdk.registerOfferwallCallback(offerwallCallback)

        findViewById<Button>(R.id.btnShowOfferwall).setOnClickListener {
            GrowboltSdk.showOfferwall(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        GrowboltSdk.unregisterOfferwallCallback()
    }
}
