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
    @SerializedName("payments")        val payments: List<Payment>?,
    @SerializedName("hold_period")     val holdPeriod: Int?,        // ← now Int
    @SerializedName("hold_type")       val holdType: String?,
    @SerializedName("categories")      val categories: List<String>?,
    @SerializedName("full_categories") val fullCategories: List<OfferCategory>?,
    @SerializedName("tags")            val tags: List<String>?,
    @SerializedName("description")     val description: String?,
    @SerializedName("url")             val clickUrl: String?        // ← was click_url
) {
    // Helper to get payout amount from payments array

    val holdPeriodDisplay: String get() = when {
        holdPeriod == null || holdPeriod == 0 -> ""
        holdType == "days" -> "$holdPeriod Days"
        else -> "$holdPeriod $holdType"
    }
}

data class OfferDetail(
    @SerializedName("id")               val id: Int,
    @SerializedName("title")            val title: String,
    @SerializedName("logo")             val logo: String?,
    @SerializedName("description")      val description: String?,
    @SerializedName("description_lang") val descriptionLang: String?,
    @SerializedName("kpi")              val kpi: String?,
    @SerializedName("hold_period")      val holdPeriod: Int?,
    @SerializedName("hold_type")        val holdType: String?,
    @SerializedName("url")              val clickUrl: String?,
    @SerializedName("payments")         val payments: List<Payment>?,
    @SerializedName("categories")       val categories: List<String>?,
    @SerializedName("full_categories")  val fullCategories: List<OfferCategory>?,
    @SerializedName("tags")             val tags: List<String>?,
    @SerializedName("disclaimer")       val disclaimer: String?,
    @SerializedName("note")             val note: String?
) {
    // Sum all payments with total > 0
    val totalPayout: Double get() = payments
        ?.sumOf { it.total?.toDoubleOrNull() ?: 0.0 }
        ?: 0.0

    val holdPeriodDisplay: String get() = when {
        holdPeriod == null || holdPeriod == 0 -> ""
        holdType == "days" -> "$holdPeriod Days"
        else -> "$holdPeriod ${holdType ?: ""}"
    }

    val logoUrl: String? get() = logo?.takeIf { it.isNotBlank() }

    // Steps from payments — skip payments with total = 0 (like "Install" goal)
    val steps: List<Payment> get() = payments
        ?.filter { (it.total?.toDoubleOrNull() ?: 0.0) > 0.0 }
        ?: emptyList()
}

data class RedeemResponse(
    @SerializedName("url")        val url: String,
    @SerializedName("offer_id")   val offerId: Int?,
    @SerializedName("click_id")   val clickId: String?,
    @SerializedName("sub1")       val sub1: String?,
    @SerializedName("domain_url") val domainUrl: String?,

)

data class RedeemRequest(

    @SerializedName("sub3") val sub3: String,  // Advertising ID (GAID)
    @SerializedName("sub4") val sub4: String,  // User ID from SDK config
    @SerializedName("sub5") val sub5: String,  // Android Device ID
    @SerializedName("sub6") val sub6: String,  // App version e.g. "V1.0.0"
    @SerializedName("sub7") val sub7: String

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
    @SerializedName("id")       val id: String?,
    @SerializedName("title")    val title: String?,   // "Registration"
    @SerializedName("goal")     val goal: String?,    // "register"
    @SerializedName("type")     val type: String?,
    @SerializedName("revenue")  val revenue: String?, // our cut e.g. "5.0000"
    @SerializedName("total")    val total: String?,   // user earns e.g. "10.0000"
    @SerializedName("currency") val currency: String?, // "inr"
    @SerializedName("position") val position: Int?,
    @SerializedName("description") val description: String?,
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
    @SerializedName("status")      val status: String, // ← was Double
    @SerializedName("hold_period") val holdPeriod: String?,
    @SerializedName("subtitle")     val subtitle: String?,   // ← new field
    @SerializedName("created_at")  val createdAt: String?,
    @SerializedName("status_label") val statusLabel: String?,  // ← new field "PROGRESS"
    @SerializedName("payout")       val payout: OngoingPayout?,  // ← now object
)

data class OngoingCounts(
    @SerializedName("progress")  val progress: Int,
    @SerializedName("completed") val completed: Int,
    @SerializedName("failed")    val failed: Int
)

data class OngoingPayout(
    @SerializedName("amount")   val amount: String?,
    @SerializedName("currency") val currency: String?,
    @SerializedName("display")  val display: String?
) {
    val amountDouble: Double get() = amount?.toDoubleOrNull() ?: 0.0
}
data class Pagination(
    @SerializedName("page")       val page: Int,
    @SerializedName("page_size")  val pageSize: Int,
    @SerializedName("total")      val total: Int,


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
