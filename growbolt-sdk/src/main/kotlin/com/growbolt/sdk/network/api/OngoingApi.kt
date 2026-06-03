package com.growbolt.sdk.network.api

import com.growbolt.sdk.network.model.OngoingResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

internal interface OngoingApi {
    /**
     * GET /sdk/ongoing/?sub4=...&tab=all|progress|completed|failed
     * &date_from=YYYY-MM-DD&date_to=YYYY-MM-DD
     */
    @GET("sdk/ongoing/")
    suspend fun getOngoing(
        @Query("sub4")      sub4: String,
        @Query("tab")       tab: String = "all",
        @Query("date_from") dateFrom: String? = null,
        @Query("date_to")   dateTo: String? = null
    ): Response<OngoingResponse>
}
