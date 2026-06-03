package com.growbolt.sdk.offerwall

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.growbolt.sdk.GrowboltSdk
import com.growbolt.sdk.GrowboltSdk.config
import com.growbolt.sdk.R
import com.growbolt.sdk.databinding.GrowboltActivityOfferDetailBinding
import com.growbolt.sdk.network.model.OfferDetail
import com.squareup.picasso.Picasso

internal class OfferDetailActivity : AppCompatActivity() {

    private lateinit var binding: GrowboltActivityOfferDetailBinding
    private val viewModel: OfferwallViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = GrowboltActivityOfferDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val offerId = intent.getIntExtra(EXTRA_OFFER_ID, -1)
        if (offerId == -1) { finish(); return }

        binding.ibBack.setOnClickListener { finish() }
        observeViewModel()
        viewModel.loadOfferDetail(offerId)
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }

        viewModel.offerDetail.observe(this) { detail ->
            detail ?: return@observe
            bindDetail(detail)
        }
    }

    private fun bindDetail(detail: OfferDetail) {
        val config = GrowboltSdk.config
        val currencySymbol = config.currencySymbol

        // ── Offer summary card ────────────────────────────────────────────
        binding.tvTitle.text = detail.title
        binding.tvHoldPeriod.text = detail.holdPeriod ?: ""
        binding.tvPayout.text = "$currencySymbol${"%.2f".format(detail.payout)}"
        binding.tvPayoutBadge.text = "$currencySymbol${"%.0f".format(detail.payout)}"

        // Logo
        if (!detail.logo.isNullOrBlank()) {
            Picasso.get()
                .load(detail.logo)
                .placeholder(R.drawable.growbolt_offer_placeholder)
                .error(R.drawable.growbolt_offer_placeholder)
                .fit()
                .centerCrop()
                .into(binding.ivOfferLogo)
        } else {
            binding.ivOfferLogo.setImageResource(R.drawable.growbolt_offer_placeholder)
        }

        // Banner image (use first banner or offer logo as fallback)
        if (!detail.logo.isNullOrBlank()) {
            Picasso.get()
                .load(detail.logo)
                .fit()
                .centerCrop()
                .into(binding.ivBannerDetail)
        }

        // ── Steps card ────────────────────────────────────────────────────
        binding.tvStepsTitle.text = detail.title

        // Parse KPI/description into numbered steps
        val stepsText = detail.kpi ?: detail.descriptionLang ?: ""
        val steps = parseSteps(stepsText)
        binding.layoutSteps.removeAllViews()
        steps.forEachIndexed { index, step ->
            val stepView = TextView(this).apply {
                text = "${index + 1}. $step"
                textSize = 14f
                setTextColor(getColor(R.color.growbolt_text_secondary))
                setPadding(0, 0, 0, 12)
                setLineSpacing(0f, 1.4f)  // ← replace lineSpacingMultiplier = 1.4f
            }
            binding.layoutSteps.addView(stepView)
        }


        // ── Warning card ──────────────────────────────────────────────────
        // Use description_lang for important note, kpi for warning
        val importantNote = detail.descriptionLang
            ?: "You will not be rewarded if you have installed this app before."
        val warning = "Fake installs will not be entertained and will lead to deactivation of your account."

        binding.tvImportantNote.text = importantNote
        binding.tvWarning.text = warning

        // ── CTA button ────────────────────────────────────────────────────
        binding.btnStartOffer.text = "Claim $currencySymbol${"%.2f".format(detail.payout)}"
        binding.btnStartOffer.setOnClickListener {
            detail.clickUrl?.let { clickUrl ->
                val finalUrl = clickUrl
                    .replace("{sub4}", config.userId)
                    .replace("%7Bsub4%7D", config.userId)
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(finalUrl)))
            }
        }
    }

    /**
     * Splits KPI/description text into individual steps.
     * Handles newline-separated, numbered (1. 2. 3.), and sentence-separated text.
     */
    private fun parseSteps(text: String): List<String> {
        if (text.isBlank()) return emptyList()

        // Try splitting by existing numbered list: "1. step 2. step"
        val numberedRegex = Regex("""(?=\d+\.\s)""")
        val numberedSplit = text.split(numberedRegex)
            .map { it.replace(Regex("""^\d+\.\s*"""), "").trim() }
            .filter { it.isNotBlank() }
        if (numberedSplit.size > 1) return numberedSplit

        // Try newline split
        val newlineSplit = text.split("\n")
            .map { it.trim() }
            .filter { it.isNotBlank() }
        if (newlineSplit.size > 1) return newlineSplit

        // Fallback: return as single step
        return listOf(text.trim())
    }

    companion object {
        const val EXTRA_OFFER_ID = "extra_offer_id"
    }
}
