package com.growbolt.sdk.core

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

internal abstract class GrowboltBaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // 1. Clear any stale fullscreen / translucent flags from the host app or older SDK code.
        window.clearFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
        )

        // 2. Tell the framework we are handling insets ourselves.
        //    This is the modern replacement for fitsSystemWindows="true" on the root
        //    and for windowTranslucentStatus in the theme.
        //    Works on API 21+ via the Compat wrapper; on API 35+ it also opts out of
        //    the forced edge-to-edge introduced in Android 15.
        WindowCompat.setDecorFitsSystemWindows(window, true)

        super.onCreate(savedInstanceState)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) forceSystemUiVisible()
    }

    private fun forceSystemUiVisible() {
        val controller = WindowInsetsControllerCompat(window, window.decorView)

        // Make sure both bars are shown.
        controller.show(WindowInsetsCompat.Type.statusBars())
        controller.show(WindowInsetsCompat.Type.navigationBars())

        // Dark icons on the light-grey nav bar; white icons on the green status bar.
        controller.isAppearanceLightStatusBars = false
        controller.isAppearanceLightNavigationBars = true
    }

    /**
     * Apply top padding to [view] so its content starts below the status bar.
     * Call this on your root content view or toolbar if you opted out of
     * [WindowCompat.setDecorFitsSystemWindows] and are managing insets manually.
     */
    protected fun applyStatusBarInsets(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            v.setPadding(
                v.paddingLeft,
                statusBarHeight,
                v.paddingRight,
                v.paddingBottom
            )
            insets
        }
    }

    /**
     * Apply bottom padding to [view] so it sits above the gesture/nav bar.
     * Call this on any fixed-bottom CTA or bottom bar in the layout.
     */
    protected fun applyNavigationBarInsets(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val navBarHeight = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            v.setPadding(
                v.paddingLeft,
                v.paddingTop,
                v.paddingRight,
                navBarHeight + 16.dpToPx()
            )
            insets
        }
    }

    private fun Int.dpToPx(): Int =
        (this * resources.displayMetrics.density).toInt()
}