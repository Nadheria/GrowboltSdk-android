package com.growbolt.sdk.network

import com.growbolt.sdk.GrowboltConfig
import com.growbolt.sdk.core.DeviceInfo
import com.growbolt.sdk.core.TokenManager
import com.growbolt.sdk.network.api.OffersApi
import com.growbolt.sdk.network.api.OngoingApi
import com.growbolt.sdk.network.api.TokenApi
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Builds and exposes Retrofit-backed API services.
 * All services share one OkHttpClient that injects the SdkToken on every call.
 */
internal class GrowboltApiClient(
    config: GrowboltConfig,
    tokenManager: TokenManager
) {
    private val baseUrl: String = config.baseUrl.trimEnd('/') + "/api/v1/"

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(
                SdkTokenInterceptor(
                    tokenManager = tokenManager,
                    userAgent = DeviceInfo.getUserAgent()
                )
            )
            .apply {
                if (config.debug) {
                    addInterceptor(
                        HttpLoggingInterceptor().apply {
                            level = HttpLoggingInterceptor.Level.BODY
                        }
                    )
                }
            }
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(
                GsonConverterFactory.create(
                    GsonBuilder().setLenient().create()
                )
            )
            .build()
    }

    val tokenApi: TokenApi by lazy { retrofit.create(TokenApi::class.java) }
    val offersApi: OffersApi by lazy { retrofit.create(OffersApi::class.java) }
    val ongoingApi: OngoingApi by lazy { retrofit.create(OngoingApi::class.java) }
}
