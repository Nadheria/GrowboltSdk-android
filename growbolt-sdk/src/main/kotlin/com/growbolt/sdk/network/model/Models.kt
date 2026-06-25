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

fun currencySymbol(code: String?): String = when (code?.lowercase()) {
    "inr"  -> "₹"
    "usd"  -> "$"
    "eur"  -> "€"
    "gbp"  -> "£"
    "jpy"  -> "¥"
    "aud"  -> "A$"
    "cad"  -> "C$"
    else   -> code?.uppercase() ?: ""
}

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
    @SerializedName("user_payout") val userPayout: String?,
    @SerializedName("payments")        val payments: List<Payment>?,
    @SerializedName("hold_period")     val holdPeriod: Int?,
    @SerializedName("hold_type")       val holdType: String?,
    @SerializedName("categories")      val categories: List<String>?,
    @SerializedName("full_categories") val fullCategories: List<OfferCategory>?,
    @SerializedName("tags")            val tags: List<String>?,
    @SerializedName("description")     val description: String?,
    @SerializedName("description_lang")val descriptionLang: String?,
    @SerializedName("url")             val clickUrl: String?,
    @SerializedName("expiry")          val expiry: String?,
) {
    val holdPeriodDisplay: String get() = when {
        holdPeriod == null || holdPeriod == 0 -> ""
        holdType == "days" -> "$holdPeriod Days"
        else -> "$holdPeriod $holdType"
    }
}


data class CurrencyReward(
    @SerializedName("amount")        val amount: Double?,
    @SerializedName("currency_name") val currencyName: String?,
    @SerializedName("currency_icon") val currencyIcon: String?,
    @SerializedName("display")       val display: String?
)



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
    @SerializedName("note")             val note: String?,
    @SerializedName("status")           val status: String?,
    @SerializedName("creatives")        val creatives: List<Creative>?,
    @SerializedName("currency_reward") val currencyReward: CurrencyReward?,
    @SerializedName("expiry")          val expiry: String?,
) {
    // Currency from first payment that has one

    val currencyIconUrl: String? get() = currencyReward?.currencyIcon
        ?: payments?.firstOrNull { !it.currencyIcon.isNullOrBlank() }?.currencyIcon
    val currency: String? get() = payments?.firstOrNull { it.currency != null }?.currency

    val symbol: String get() = currencySymbol(currency)

    // Sum userPayout first, fallback to total — only active payments
    val userPayout: Double get() = payments
        ?.filter { it.earnAmount > 0.0 }
        ?.sumOf { it.earnAmount }
        ?: 0.0

    // ← These two were missing — now added
    val payoutFormatted: String get() = "$symbol${(userPayout)}"
    val payoutShort: String get() = "$symbol${"%.0f".format(userPayout)}"

    val holdPeriodDisplay: String get() = when {
        holdPeriod == null || holdPeriod == 0 -> ""
        holdType == "days" -> "$holdPeriod Days"
        else -> "$holdPeriod ${holdType ?: ""}"
    }

    val logoUrl: String? get() = logo?.takeIf { it.isNotBlank() }

    val steps: List<Payment> get() = payments
        ?.filter { it.earnAmount > 0.0 }
        ?: emptyList()

    val bannerUrl: String? get() = creatives
        ?.firstOrNull { it.type?.startsWith("image") == true && !it.url.isNullOrBlank() }
        ?.url
        ?: logoUrl
}

data class RedeemResponse(
    @SerializedName("url")        val url: String,
    @SerializedName("offer_id")   val offerId: Int?,
    @SerializedName("click_id")   val clickId: String?,
    @SerializedName("sub1")       val sub1: String?,
    @SerializedName("domain_url") val domainUrl: String?,
)

data class RedeemRequest(
    @SerializedName("sub3") val sub3: String,
    @SerializedName("sub4") val sub4: String,
    @SerializedName("sub5") val sub5: String,
    @SerializedName("sub6") val sub6: String,
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
    @SerializedName("id")          val id: String?,
    @SerializedName("title")       val title: String?,
    @SerializedName("goal")        val goal: String?,
    @SerializedName("type")        val type: String?,
    @SerializedName("revenue")     val revenue: String?,
    @SerializedName("total")       val total: String?,
    @SerializedName("currency")    val currency: String?,
    @SerializedName("position")    val position: Int?,
    @SerializedName("description") val description: String?,
    @SerializedName("user_payout") val userPayout: String?,
    @SerializedName("currency_icon") val currencyIcon: String?,
) {
    val symbol: String get() = currencySymbol(currency)

    // userPayout first, fallback to total
    val earnAmount: Double get() = userPayout?.toDoubleOrNull()
        ?: total?.toDoubleOrNull()
        ?: 0.0

    val payoutFormatted: String get() = "$symbol${"%.2f".format(earnAmount)}"
    val payoutShort: String get() = "$symbol${"%.0f".format(earnAmount)}"
}

// ── Ongoing ───────────────────────────────────────────────────────────────────

data class OngoingResponse(
    @SerializedName("items")      val items: List<OngoingItem>,
    @SerializedName("counts")     val counts: OngoingCounts,
    @SerializedName("pagination") val pagination: Pagination?
)

data class OngoingItem(
    @SerializedName("id")           val id: String,
    @SerializedName("title")        val title: String,
    @SerializedName("logo")         val logo: String?,
    @SerializedName("status")       val status: String,
    @SerializedName("hold_period")  val holdPeriod: String?,
    @SerializedName("subtitle")     val subtitle: String?,
    @SerializedName("created_at")   val createdAt: String?,
    @SerializedName("status_label") val statusLabel: String?,
    @SerializedName("payout")       val payout: OngoingPayout?,
    @SerializedName("offer_id") val offerId: Int?,

    )

data class OngoingCounts(
    @SerializedName("progress")  val progress: Int,
    @SerializedName("completed") val completed: Int,
    @SerializedName("failed")    val failed: Int,
    @SerializedName("expired")   val expired: Int = 0
)

data class OngoingPayout(
    @SerializedName("amount")        val amount: Double?,
    @SerializedName("currency_name") val currencyName: String?,
    @SerializedName("currency_icon") val currencyIcon: String?,
    @SerializedName("display")       val display: String?
) {
    val amountDouble: Double get() = amount ?: 0.0
}

data class Pagination(
    @SerializedName("page")        val page: Int,
    @SerializedName("per_page")    val perPage: Int,
    @SerializedName("total_count") val totalCount: Int,
)

data class Payout(
    @SerializedName("amount")   val amount: Double?,
    @SerializedName("currency") val currency: String?,
    @SerializedName("display")  val display: String?,
    @SerializedName("revenue")  val revenue: Double?,
    @SerializedName("total")    val total: Double?,
    @SerializedName("user_payout") val userPayout: String?,
    @SerializedName("currency_icon") val currencyIcon: String?,
    @SerializedName("currency_reward")  val currencyReward: CurrencyReward?
)

data class Creative(
    @SerializedName("id")        val id: String?,
    @SerializedName("url")       val url: String?,
    @SerializedName("type")      val type: String?,
    @SerializedName("title")     val title: String?,
    @SerializedName("width")     val width: Int?,
    @SerializedName("height")    val height: Int?,
    @SerializedName("file_name") val fileName: String?
)

data class Category(
    @SerializedName("id")    val id: String,
    @SerializedName("title") val title: String
)

data class ResponseModel(
    @SerializedName("full_categories") val fullCategories: List<Category>
)

data class Banner(
    @SerializedName("id")            val id: Int,
    @SerializedName("name")          val name: String?,
    @SerializedName("offer_id")      val offerId: Int?,
    @SerializedName("image")         val image: String?,
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