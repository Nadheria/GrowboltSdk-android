package com.growbolt.sdk.offerwall

import android.os.Bundle
import android.view.View
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

        binding.scrollContent.visibility = View.INVISIBLE
        binding.progressBar.visibility = View.VISIBLE

        subEventAdapter = SubEventAdapter()
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
            if (loading) {
                binding.progressBar.visibility = View.VISIBLE
                binding.scrollContent.visibility = View.INVISIBLE
            }
        }

        viewModel.offerDetail.observe(this) { detail ->
            detail ?: return@observe
            bindDetail(detail)
            binding.progressBar.visibility = View.GONE
            binding.scrollContent.apply {
                alpha = 0f
                visibility = View.VISIBLE
                animate().alpha(1f).setDuration(250).start()
            }
        }

        viewModel.error.observe(this) { error ->
            error ?: return@observe
            binding.progressBar.visibility = View.GONE
            android.widget.Toast.makeText(this, error, android.widget.Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun bindDetail(detail: OfferDetail) {
        val payoutFormatted = detail.payoutFormatted
        val payoutShort = detail.payoutShort

        // ── CARD 1: Offer summary ─────────────────────────────────────────────
        binding.tvTitle.text = detail.title
        binding.tvPayout.text = detail.currencyReward?.display ?: payoutFormatted
        binding.tvEventDescription.text = detail.descriptionLang

        val holdDisplay = detail.holdPeriodDisplay
        if (holdDisplay.isNotBlank()) {
            binding.layoutHoldChip.visibility = View.VISIBLE
            binding.tvHoldPeriod.text = holdDisplay
        } else {
            binding.layoutHoldChip.visibility = View.GONE
        }

        // Currency icon in earn button — from currency_reward or first payment
        val currencyIconUrl = detail.currencyIconUrl
        if (!currencyIconUrl.isNullOrBlank()) {
            Picasso.get()
                .load(currencyIconUrl)
                .placeholder(R.drawable.growbolt_ic_coin)
                .error(R.drawable.growbolt_ic_coin)
                .fit().centerCrop()
                .into(binding.ivDetailCurrencyIcon)
        } else {
            binding.ivDetailCurrencyIcon.setImageResource(R.drawable.growbolt_ic_coin)
        }

        // Banner
        val bannerUrl = detail.bannerUrl
        if (!bannerUrl.isNullOrBlank()) {
            Picasso.get()
                .load(bannerUrl)
                .placeholder(R.drawable.offer_place_holder)
                .error(R.drawable.offer_place_holder)
                .fit().centerCrop()
                .into(binding.ivBannerDetail)
        } else {
            binding.ivBannerDetail.setImageResource(R.drawable.offer_place_holder)
        }

        // Logo
        if (!detail.logoUrl.isNullOrBlank()) {
            Picasso.get()
                .load(detail.logoUrl)
                .placeholder(R.drawable.growbolt_offer_placeholder)
                .error(R.drawable.growbolt_offer_placeholder)
                .fit().centerCrop()
                .into(binding.ivOfferLogo)
        } else {
            binding.ivOfferLogo.setImageResource(R.drawable.growbolt_offer_placeholder)
        }

        // ── Payment cards ─────────────────────────────────────────────────────
        val payments = detail.payments
            ?.filter { it.earnAmount > 0.0 }
            ?.sortedBy { it.position ?: 0 }
            ?: emptyList()

        subEventAdapter.submitList(payments)

        // ── Warning card ──────────────────────────────────────────────────────
        val importantNote = detail.note?.takeIf { it.isNotBlank() }

            ?: detail.disclaimer?.takeIf { it.isNotBlank() }
        binding.cardWarning.visibility = View.VISIBLE
        binding.tvImportantNote.text = importantNote
            ?: "You will not be rewarded if you have installed this app before."
        binding.tvWarning.text = detail.disclaimer

        // ── CTA button ────────────────────────────────────────────────────────
        binding.btnStartOffer.text = "Claim ${detail.currencyReward?.display ?: payoutFormatted}"
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