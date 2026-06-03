package com.growbolt.sdk.network.api

import com.growbolt.sdk.network.model.Banner
import com.growbolt.sdk.network.model.CategoriesResponse
import com.growbolt.sdk.network.model.Offer
import com.growbolt.sdk.network.model.OfferDetail
import com.growbolt.sdk.network.model.OffersResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query


internal interface OffersApi {

    @GET("sdk/offers/")
    suspend fun listOffers(
        @Query("search")   search: String?   = null,
        @Query("category") category: String? = null,
        @Query("tag")      tag: String?      = null,
        @Query("os")       os: String?       = null
    ): Response<OffersResponse>

    // NEW — powers the chip filter row
    @GET("sdk/offers/categories/")
    suspend fun listCategories(): Response<CategoriesResponse>

    // Root is a plain array — List<Banner> directly
    @GET("sdk/banners/")
    suspend fun listBanners(): Response<List<Banner>>

    @GET("sdk/offers/{id}/")
    suspend fun getOfferDetail(@Path("id") offerId: Int): Response<OfferDetail>
}