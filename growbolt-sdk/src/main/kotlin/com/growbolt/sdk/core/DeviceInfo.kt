package com.growbolt.sdk.core

import android.content.Context
import android.os.Build
import android.provider.Settings

internal object DeviceInfo {

    private const val SDK_VERSION = "1.0.0"

    fun getDeviceId(context: Context): String =
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            ?: "unknown-device"

    fun getUserAgent(): String =
        "GrowboltSDK/$SDK_VERSION Android/${Build.VERSION.RELEASE} (${Build.MANUFACTURER} ${Build.MODEL})"
}