package com.growbolt.sdk.offerwall

import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.growbolt.sdk.GrowboltSdk
import com.growbolt.sdk.R
import com.growbolt.sdk.databinding.GrowboltActivityOfferDetailBinding
import com.growbolt.sdk.network.model.OfferDetail
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch

internal class OfferDetailActivity : AppCompatActivity() {

    private lateinit var binding: GrowboltActivityOfferDetailBinding
    private lateinit var subEventAdapter: SubEventAdapter
    private val viewModel: OfferwallViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = GrowboltActivityOfferDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val offerId = intent.getIntExtra(EXTRA_OFFER_ID, -1)
        if (offerId == -1) { finish(); return }

        binding.ibBack.setOnClickListener { finish() }

        // Each payment renders as its own card — no wrapper card needed
        subEventAdapter = SubEventAdapter(GrowboltSdk.config.currencySymbol)
        binding.rvSubEvents.apply {
            layoutManager = LinearLayoutManager(this@OfferDetailActivity)
            adapter = subEventAdapter
            isNestedScrollingEnabled = false
        }

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
        val totalPayout = detail.totalPayout
        val payoutFormatted = "$currencySymbol${"%.2f".format(totalPayout)}"
        val payoutShort = "$currencySymbol${"%.0f".format(totalPayout)}"

        // ── CARD 1: Offer summary ─────────────────────────────────────────────
        binding.tvTitle.text = detail.title
        binding.tvPayout.text = payoutFormatted
        binding.tvEventDescription.text = detail.descriptionLang
        val holdDisplay = detail.holdPeriodDisplay
        if (holdDisplay.isNotBlank()) {
            binding.layoutHoldChip.visibility = View.VISIBLE
            binding.tvHoldPeriod.text = holdDisplay
        } else {
            binding.layoutHoldChip.visibility = View.GONE
        }

        if (!detail.logoUrl.isNullOrBlank()) {
            Picasso.get()
                .load(detail.logoUrl)
                .placeholder(R.drawable.growbolt_offer_placeholder)
                .error(R.drawable.growbolt_offer_placeholder)
                .fit().centerCrop()
                .into(binding.ivOfferLogo)
            Picasso.get()
                .load(detail.logoUrl)
                .fit().centerCrop()
                .into(binding.ivBannerDetail)
        } else {
            binding.ivOfferLogo.setImageResource(R.drawable.growbolt_offer_placeholder)
            binding.ivBannerDetail.setImageResource(R.drawable.growbolt_banner_placeholder)
        }

        // ── Payment cards — each payment = its own card in RecyclerView ───────
        val payments = detail.payments
            ?.filter { (it.total?.toDoubleOrNull() ?: 0.0) > 0.0 }
            ?.sortedBy { it.position ?: 0 }
            ?: emptyList()

        subEventAdapter.submitList(payments)

        // ── Steps card (HTML kpi/description — shown only when present) ────────
//        val stepsHtml = detail.kpi?.takeIf { it.isNotBlank() }
//            ?: detail.descriptionLang?.takeIf { it.isNotBlank() }
//
//        if (!stepsHtml.isNullOrBlank()) {
//            binding.cardSteps.visibility = View.VISIBLE
//            binding.tvStepsPayoutBadge.text = payoutShort
//            binding.layoutSteps.removeAllViews()
//            val stepsView = TextView(this).apply {
//                text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                    Html.fromHtml(stepsHtml, Html.FROM_HTML_MODE_LEGACY)
//                } else {
//                    @Suppress("DEPRECATION")
//                    Html.fromHtml(stepsHtml)
//                }
//                textSize = 14f
//                setTextColor(getColor(R.color.growbolt_text_secondary))
//                setLineSpacing(0f, 1.4f)
//            }
//            binding.layoutSteps.addView(stepsView)
//        } else {
//            binding.cardSteps.visibility = View.GONE
//        }

        // ── Warning card ──────────────────────────────────────────────────────
        val importantNote = detail.note?.takeIf { it.isNotBlank() }
            ?: detail.disclaimer?.takeIf { it.isNotBlank() }
        binding.cardWarning.visibility = View.VISIBLE
        binding.tvImportantNote.text = importantNote
            ?: "You will not be rewarded if you have installed this app before."
        binding.tvWarning.text =
            "Fake installs will not be entertained and will lead to deactivation of your account."

        // ── CTA button ────────────────────────────────────────────────────────
        binding.btnStartOffer.text = "Claim $payoutFormatted"
        binding.btnStartOffer.setOnClickListener {
            lifecycleScope.launch {
                OfferClickManager.handleClick(
                    context = this@OfferDetailActivity,
                    offerId = detail.id,
                    onLoading = { loading ->
                        binding.progressBar.visibility =
                            if (loading) View.VISIBLE else View.GONE
                        binding.btnStartOffer.isEnabled = !loading
                    },
                    onError = { error ->
                        android.widget.Toast.makeText(
                            this@OfferDetailActivity,
                            error,
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            }
        }
    }

    companion object {
        const val EXTRA_OFFER_ID = "extra_offer_id"
    }
}
