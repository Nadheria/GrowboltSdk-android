package com.growbolt.sdk.network.api

import com.growbolt.sdk.network.model.SdkTokenResponse
import retrofit2.Response
import retrofit2.http.POST

internal interface TokenApi {
    /** Rotate the current SDK token — revokes old, returns new key. */
    @POST("sdk/token/regenerate/")
    suspend fun regenerateToken(): Response<SdkTokenResponse>

    /** Self-revoke the current SDK token. Returns 204 No Content. */
    @POST("sdk/token/revoke/")
    suspend fun revokeToken(): Response<Unit>
}
