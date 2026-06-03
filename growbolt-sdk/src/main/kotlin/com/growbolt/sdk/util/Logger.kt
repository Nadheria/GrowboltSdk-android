package com.growbolt.sdk.util

import android.util.Log

internal object Logger {
    var isEnabled: Boolean = false
    private const val ROOT_TAG = "GrowboltSDK"

    fun d(tag: String, msg: String) { if (isEnabled) Log.d("$ROOT_TAG/$tag", msg) }
    fun i(tag: String, msg: String) { if (isEnabled) Log.i("$ROOT_TAG/$tag", msg) }
    fun w(tag: String, msg: String) { if (isEnabled) Log.w("$ROOT_TAG/$tag", msg) }
    fun e(tag: String, msg: String, throwable: Throwable? = null) {
        if (isEnabled) Log.e("$ROOT_TAG/$tag", msg, throwable)
        else if (throwable != null) Log.e("$ROOT_TAG/$tag", msg, throwable)
    }
}
