package com.growbolt.sdk.network.model

import com.google.gson.annotations.SerializedName

// ── Token ─────────────────────────────────────────────────────────────────────

data class SdkTokenResponse(
    @SerializedName("key")        val key: String,
    @SerializedName("token_type") val tokenType: String,
    @SerializedName("device_id")  val deviceId: String?,
    @SerializedName("user_agent") val userAgent: String?,
    @SerializedName("created_at") val createdAt: String?
)

data class RegenerateTokenRequest(
    @SerializedName("device_id")  val deviceId: String? = null,
    @SerializedName("user_agent") val userAgent: String? = null
)

// ── Offers ────────────────────────────────────────────────────────────────────

data class OffersResponse(
    @SerializedName("offers")  val offers: List<Offer>,
    @SerializedName("count")   val count: Int,
    @SerializedName("search")  val search: String?,
    @SerializedName("filters") val filters: OfferFilters?
)

data class OfferFilters(
    @SerializedName("category") val category: List<String>?,
    @SerializedName("tag")      val tag: List<String>?,
    @SerializedName("os")       val os: String?
)

data class Offer(
    @SerializedName("id")              val id: Int,
    @SerializedName("title")           val title: String,
    @SerializedName("logo")            val logo: String?,
    @SerializedName("payout")          val payout: Payout?,
    @SerializedName("hold_period")     val holdPeriod: String?,
    @SerializedName("hold_type")       val holdType: String?,
    @SerializedName("categories")      val categories: List<String>?,
    @SerializedName("full_categories") val fullCategories: List<OfferCategory>?,
    @SerializedName("tags")            val tags: List<String>?,
    @SerializedName("strictly_os")     val strictlyOs: StrictlyOs?,
    @SerializedName("description")     val description: String?,
    @SerializedName("click_url")       val clickUrl: String?
)

data class OfferDetail(
    @SerializedName("id")               val id: Int,
    @SerializedName("title")            val title: String,
    @SerializedName("logo")             val logo: String?,
    @SerializedName("payout")           val payout: Double?,
    @SerializedName("hold_period")      val holdPeriod: String?,
    @SerializedName("description_lang") val descriptionLang: String?,
    @SerializedName("kpi")              val kpi: String?,
    @SerializedName("payments")         val payments: List<Payment>?,
    @SerializedName("click_url")        val clickUrl: String?,
    @SerializedName("categories")       val categories: List<String>?
)

data class OfferCategory(
    @SerializedName("id")    val id: String,
    @SerializedName("title") val title: String
)

data class StrictlyOs(
    @SerializedName("items") val items: Map<String, Any>?
)

data class CategoriesResponse(
    @SerializedName("categories") val categories: List<OfferCategory>,
    @SerializedName("count")      val count: Int
)


data class Payment(
    @SerializedName("goal")    val goal: String?,
    @SerializedName("revenue") val revenue: Double?,
    @SerializedName("payout")  val payout: Double?
)

// ── Ongoing ───────────────────────────────────────────────────────────────────

data class OngoingResponse(
    @SerializedName("items")      val items: List<OngoingItem>,
    @SerializedName("counts")     val counts: OngoingCounts,
    @SerializedName("pagination") val pagination: Pagination?
)

data class OngoingItem(
    @SerializedName("id")          val id: String,
    @SerializedName("title")       val title: String,
    @SerializedName("logo")        val logo: String?,
    @SerializedName("status")      val status: String,
    @SerializedName("payout")      val payout: Payout?,   // ← was Double
    @SerializedName("hold_period") val holdPeriod: String?,
    @SerializedName("created_at")  val createdAt: String?
)

data class OngoingCounts(
    @SerializedName("progress")  val progress: Int,
    @SerializedName("completed") val completed: Int,
    @SerializedName("failed")    val failed: Int
)

data class Pagination(
    @SerializedName("page")       val page: Int,
    @SerializedName("page_size")  val pageSize: Int,
    @SerializedName("total")      val total: Int
)

data class Payout(
    @SerializedName("amount")   val amount: Double?,
    @SerializedName("currency") val currency: String?,
    @SerializedName("display")  val display: String?,
    @SerializedName("revenue")  val revenue: Double?

)

data class Category(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String
)

data class ResponseModel(
    @SerializedName("full_categories") val fullCategories: List<Category>
)


data class Banner(
    @SerializedName("id")            val id: Int,
    @SerializedName("name")          val name: String?,
    @SerializedName("offer_id")      val offerId: Int?,
    @SerializedName("image")         val image: String?,   // relative path e.g. /media/banners/xxx.png
    @SerializedName("is_active")     val isActive: Boolean,
    @SerializedName("display_order") val displayOrder: Int,
    @SerializedName("created_at")    val createdAt: String?
)


// ── Generic Error ─────────────────────────────────────────────────────────────

data class ApiError(
    @SerializedName("detail") val detail: String?,
    @SerializedName("error")  val error: String?
) {
    val message: String get() = detail ?: error ?: "Unknown error"
}
