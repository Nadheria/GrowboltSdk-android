package com.growbolt.sdk.offerwall

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.growbolt.sdk.R
import com.growbolt.sdk.core.GrowboltBaseActivity
import com.growbolt.sdk.databinding.GrowboltActivityOfferStatusBinding
import com.growbolt.sdk.network.model.OngoingCounts
import com.growbolt.sdk.network.model.OngoingItem
import com.growbolt.sdk.ongoing.OngoingAdapter
import com.growbolt.sdk.ongoing.OngoingTab
import com.growbolt.sdk.ongoing.OngoingViewModel

internal class OfferStatusActivity : GrowboltBaseActivity() {

    private lateinit var binding: GrowboltActivityOfferStatusBinding
    private val viewModel: OngoingViewModel by viewModels()
    private lateinit var ongoingAdapter: OngoingAdapter
    private var counts: OngoingCounts? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = GrowboltActivityOfferStatusBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBackButton()
        setupTabs()
        setupRecyclerView()
        observeViewModel()

        // Load counts first via "all" tab, then switch to completed (new default first tab)
        viewModel.loadOngoing(OngoingTab.ALL)
    }

    private fun setupBackButton() {
        binding.ibBack.setOnClickListener { finish() }
        binding.btnExploreOffers.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun setupTabs() {
        // Tab order: Completed → Pending → Failed
        binding.tabCompleted.setOnClickListener { selectTab(OngoingTab.COMPLETED) }
        binding.tabPending.setOnClickListener { selectTab(OngoingTab.PROGRESS) }
        binding.tabFailed.setOnClickListener { selectTab(OngoingTab.FAILED) }
        updateTabUI(OngoingTab.COMPLETED) // default selected tab is now Completed
    }

    private fun selectTab(tab: OngoingTab) {
        updateTabUI(tab)
        viewModel.loadOngoing(tab)
    }

    private fun updateTabUI(tab: OngoingTab) {
        // Reset all tabs to inactive
        listOf(binding.tabCompleted, binding.tabPending, binding.tabFailed).forEach { btn ->
            btn.setBackgroundResource(R.drawable.growbolt_tab_inactive)
            btn.setTextAppearance(R.style.GrowboltTabInactive)
        }
        // Activate selected tab
        val activeBtn = when (tab) {
            OngoingTab.COMPLETED -> binding.tabCompleted
            OngoingTab.PROGRESS  -> binding.tabPending
            OngoingTab.FAILED    -> binding.tabFailed
            OngoingTab.ALL       -> null
        }
        activeBtn?.setBackgroundResource(R.drawable.growbolt_tab_active)
        activeBtn?.setTextAppearance(R.style.GrowboltTabActive)

        counts?.let { updateTabLabels(it) }
    }

    private fun updateTabLabels(c: OngoingCounts) {
        binding.tabCompleted.text = getString(R.string.growbolt_tab_completed, c.completed)
        binding.tabPending.text   = getString(R.string.growbolt_tab_pending, c.progress)
        binding.tabFailed.text    = getString(R.string.growbolt_tab_failed, c.failed)
    }

    private fun setupRecyclerView() {
        ongoingAdapter = OngoingAdapter(
            onItemClick = { item -> navigateToOfferDetail(item) }
        )
        binding.rvOngoing.apply {
            layoutManager = LinearLayoutManager(this@OfferStatusActivity)
            adapter = ongoingAdapter
        }
    }

    // Navigate to offer detail only for pending/progress offers
    private fun navigateToOfferDetail(item: OngoingItem) {
        if (item.status.lowercase() == "progress") {
            val offerId = item.offerId ?: return
            startActivity(
                Intent(this, OfferDetailActivity::class.java).apply {
                    putExtra(OfferDetailActivity.EXTRA_OFFER_ID, offerId)
                }
            )
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }

        viewModel.counts.observe(this) { c ->
            c ?: return@observe
            counts = c
            updateTabLabels(c)
            // After loading ALL to get counts, switch to COMPLETED view (new default)
            if (viewModel.currentTab == OngoingTab.ALL) {
                selectTab(OngoingTab.COMPLETED)
            }
        }

        viewModel.items.observe(this) { items ->
            if (items.isEmpty()) {
                binding.rvOngoing.visibility = View.GONE
                binding.layoutEmpty.visibility = View.VISIBLE
            } else {
                binding.rvOngoing.visibility = View.VISIBLE
                binding.layoutEmpty.visibility = View.GONE
                ongoingAdapter.submitList(items)
            }
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                binding.tvError.visibility = View.VISIBLE
                binding.tvError.text = it
            } ?: run { binding.tvError.visibility = View.GONE }
        }
    }
}