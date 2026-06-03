package com.growbolt.sdk.offerwall

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.growbolt.sdk.GrowboltSdk
import com.growbolt.sdk.databinding.GrowboltActivityOfferwallBinding
import com.growbolt.sdk.network.model.Banner
import com.growbolt.sdk.offerwall.ui.BannerAdapter
import com.growbolt.sdk.offerwall.ui.BannerItem
import com.growbolt.sdk.offerwall.ui.CategoryChipHelper
import com.growbolt.sdk.offerwall.ui.OfferListAdapter

internal class OfferwallActivity : AppCompatActivity() {

    private lateinit var binding: GrowboltActivityOfferwallBinding
    private val viewModel: OfferwallViewModel by viewModels()
    private lateinit var offerAdapter: OfferListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = GrowboltActivityOfferwallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val currencySymbol = GrowboltSdk.config.currencySymbol




        setupBanner()
        setupOfferList(currencySymbol)
        setupOfferStatus()
        observeViewModel()
        viewModel.loadBanners()
        viewModel.loadOffers()
        viewModel.loadCategories()
    }

    private fun setupBanner() {
        val placeholderBanners = listOf(
            BannerItem(null, "Groceries delivered in 10 minutes", null),
            BannerItem(null, "Earn rewards this week", null)
        )
        binding.viewPagerBanner.adapter = BannerAdapter(placeholderBanners) { }
    }

    private fun setupOfferList(currencySymbol: String) {
        offerAdapter = OfferListAdapter(currencySymbol) { offer ->
            val intent = Intent(this, OfferDetailActivity::class.java).apply {
                putExtra(OfferDetailActivity.EXTRA_OFFER_ID, offer.id)
            }
            startActivity(intent)
        }
        binding.rvOffers.apply {
            layoutManager = LinearLayoutManager(this@OfferwallActivity)
            adapter = offerAdapter
        }
    }

    private fun setupOfferStatus() {
        binding.cardOfferStatus.setOnClickListener {
            startActivity(Intent(this, OfferStatusActivity::class.java))
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }

        viewModel.banners.observe(this) { banners ->
            if (banners.isNotEmpty()) {
                setupBanner(banners)
            }
        }

        viewModel.offers.observe(this) { offers ->
            offerAdapter.submitList(offers)
        }

        viewModel.categories.observe(this) { categories ->
            if (categories.isNotEmpty()) {
                CategoryChipHelper.populate(
                    context = this,
                    chipGroup = binding.chipGroupFilter,
                    categories = categories.map { it.title },
                    onCategorySelected = { viewModel.filterByCategory(it) }
                )
            }
        }

        viewModel.error.observe(this) { error ->
            if (error != null) {
                binding.tvError.visibility = View.VISIBLE
                binding.tvError.text = error
                android.util.Log.e("GrowboltDebug", "ERROR: $error")  // ← ADD THIS
            } else {
                binding.tvError.visibility = View.GONE
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        GrowboltSdk.offerwallCallback?.onOfferwallClosed()
    }

    private fun setupBanner(banners: List<Banner>) {
        val baseUrl = GrowboltSdk.config.baseUrl
        binding.viewPagerBanner.adapter = BannerAdapter(
            banners = banners,
            baseUrl = baseUrl,
            onBannerClick = { banner ->
                // If banner has an offer_id, open that offer detail
                banner.offerId?.let { offerId ->
                    val intent = Intent(this, OfferDetailActivity::class.java).apply {
                        putExtra(OfferDetailActivity.EXTRA_OFFER_ID, offerId)
                    }
                    startActivity(intent)
                }
            }
        )
        // Auto-slide every 3 seconds
        startBannerAutoSlide()
    }

    private fun startBannerAutoSlide() {
        val handler = android.os.Handler(mainLooper)
        val runnable = object : Runnable {
            override fun run() {
                val count = binding.viewPagerBanner.adapter?.itemCount ?: 0
                if (count > 1) {
                    val next = (binding.viewPagerBanner.currentItem + 1) % count
                    binding.viewPagerBanner.setCurrentItem(next, true)
                }
                handler.postDelayed(this, 3000)
            }
        }
        handler.postDelayed(runnable, 3000)
    }


}