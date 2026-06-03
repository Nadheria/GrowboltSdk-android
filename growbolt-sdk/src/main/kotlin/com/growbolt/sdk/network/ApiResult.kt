package com.growbolt.sdk.network

/**
 * Wraps API call results into a clean sealed class so ViewModels
 * never need to catch exceptions directly.
 */
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val code: Int, val message: String) : ApiResult<Nothing>()
    data class Exception(val throwable: Throwable) : ApiResult<Nothing>()
}

/**
 * Executes a suspend Retrofit call and wraps the result.
 */
internal suspend fun <T> safeApiCall(call: suspend () -> retrofit2.Response<T>): ApiResult<T> {
    return try {
        val response = call()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) ApiResult.Success(body)
            else ApiResult.Error(response.code(), "Empty response body")
        } else {
            ApiResult.Error(response.code(), response.errorBody()?.string() ?: "HTTP ${response.code()}")
        }
    } catch (e: Throwable) {
        ApiResult.Exception(e)
    }
}
