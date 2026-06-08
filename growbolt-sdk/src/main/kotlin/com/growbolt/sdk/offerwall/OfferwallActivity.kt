package com.growbolt.sdk.offerwall

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.growbolt.sdk.GrowboltSdk
import com.growbolt.sdk.R
import com.growbolt.sdk.databinding.GrowboltActivityOfferwallBinding
import com.growbolt.sdk.offerwall.ui.BannerAdapter
import com.growbolt.sdk.offerwall.ui.CategoryChipHelper
import com.growbolt.sdk.offerwall.ui.OfferListAdapter

internal class OfferwallActivity : AppCompatActivity() {

    private lateinit var binding: GrowboltActivityOfferwallBinding
    private val viewModel: OfferwallViewModel by viewModels()
    private lateinit var offerAdapter: OfferListAdapter

    // Auto-slide
    private val autoSlideHandler = Handler(Looper.getMainLooper())
    private var autoSlideRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = GrowboltActivityOfferwallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupOfferList()
        setupOfferStatus()
        observeViewModel()

        viewModel.loadBanners()
        viewModel.loadOffers()
        viewModel.loadCategories()
    }

    // ── Offer list — proper RecyclerView, NO NestedScrollView ────────────
    private fun setupOfferList() {
        offerAdapter = OfferListAdapter(GrowboltSdk.config.currencySymbol) { offer ->
            startActivity(
                Intent(this, OfferDetailActivity::class.java).apply {
                    putExtra(OfferDetailActivity.EXTRA_OFFER_ID, offer.id)
                }
            )
        }
        binding.rvOffers.apply {
            layoutManager = LinearLayoutManager(this@OfferwallActivity)
            adapter = offerAdapter
            setHasFixedSize(false)
            setItemViewCacheSize(20)     // cache 20 views off screen
            recycledViewPool.setMaxRecycledViews(0, 30)
        }
    }

    private fun setupOfferStatus() {
        binding.ivOfferStatus.setOnClickListener {
            startActivity(Intent(this, OfferStatusActivity::class.java))
        }
    }

    // ── Observers ─────────────────────────────────────────────────────────
    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }

        viewModel.banners.observe(this) { banners ->
            if (banners.isNotEmpty()) setupBanner(banners)
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

        viewModel.offers.observe(this) { offers ->
            offerAdapter.submitList(offers)
        }

        viewModel.error.observe(this) { error ->
            if (error != null) {
                binding.tvError.visibility = View.VISIBLE
                binding.tvError.text = error
            } else {
                binding.tvError.visibility = View.GONE
            }
        }
    }

    // ── Banner ────────────────────────────────────────────────────────────
    private fun setupBanner(banners: List<com.growbolt.sdk.network.model.Banner>) {
        binding.viewPagerBanner.adapter = BannerAdapter(
            banners = banners,
            baseUrl = GrowboltSdk.config.baseUrl,
            onBannerClick = { banner ->
                banner.offerId?.let { offerId ->
                    startActivity(
                        Intent(this, OfferDetailActivity::class.java).apply {
                            putExtra(OfferDetailActivity.EXTRA_OFFER_ID, offerId)
                        }
                    )
                }
            }
        )

        setupDots(banners.size)

        binding.viewPagerBanner.registerOnPageChangeCallback(
            object : androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    updateDots(position, banners.size)
                }
            }
        )

        startAutoSlide(banners.size)
    }

    // ── Dots ──────────────────────────────────────────────────────────────
    private fun setupDots(count: Int) {
        binding.dotsIndicator.removeAllViews()
        val sizePx = 8.dpToPx()
        val marginPx = 3.dpToPx()
        repeat(count) { index ->
            val dot = View(this).apply {
                layoutParams = android.widget.LinearLayout.LayoutParams(sizePx, sizePx).apply {
                    marginStart = marginPx
                    marginEnd = marginPx
                }
                setBackgroundResource(
                    if (index == 0) R.drawable.growbolt_dot_active
                    else R.drawable.growbolt_dot_inactive
                )
            }
            binding.dotsIndicator.addView(dot)
        }
    }

    private fun updateDots(activeIndex: Int, count: Int) {
        repeat(count) { index ->
            binding.dotsIndicator.getChildAt(index)?.setBackgroundResource(
                if (index == activeIndex) R.drawable.growbolt_dot_active
                else R.drawable.growbolt_dot_inactive
            )
        }
    }

    // ── Auto slide ────────────────────────────────────────────────────────
    private fun startAutoSlide(bannerCount: Int) {
        autoSlideRunnable?.let { autoSlideHandler.removeCallbacks(it) }
        if (bannerCount <= 1) return
        autoSlideRunnable = object : Runnable {
            override fun run() {
                val count = binding.viewPagerBanner.adapter?.itemCount ?: 0
                if (count > 1) {
                    val next = (binding.viewPagerBanner.currentItem + 1) % count
                    binding.viewPagerBanner.setCurrentItem(next, true)
                }
                autoSlideHandler.postDelayed(this, 3000)
            }
        }
        autoSlideHandler.postDelayed(autoSlideRunnable!!, 3000)
    }

    private fun Int.dpToPx(): Int =
        (this * resources.displayMetrics.density).toInt()

    override fun onDestroy() {
        super.onDestroy()
        autoSlideRunnable?.let { autoSlideHandler.removeCallbacks(it) }
        GrowboltSdk.offerwallCallback?.onOfferwallClosed()
    }
}