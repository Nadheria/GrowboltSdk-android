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

        // Apply status-bar top inset to the green header FrameLayout so the
        // back button and title are never drawn under the status bar.
        // This works on all API levels (21+) and all nav modes (gesture / 3-button).
        applyStatusBarInsets(binding.root.getChildAt(0)) // first child = green FrameLayout header

        setupBackButton()
        setupTabs()
        setupRecyclerView()
        observeViewModel()

        // Load counts first via "all" tab, then switch to completed (default first tab)
        viewModel.loadOngoing(OngoingTab.ALL)
    }

    private fun setupBackButton() {
        binding.ibBack.setOnClickListener { finish() }
        binding.btnExploreOffers.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun setupTabs() {
        binding.tabCompleted.setOnClickListener { selectTab(OngoingTab.COMPLETED) }
        binding.tabPending.setOnClickListener { selectTab(OngoingTab.PROGRESS) }
        binding.tabFailed.setOnClickListener { selectTab(OngoingTab.FAILED) }
        updateTabUI(OngoingTab.COMPLETED)
    }

    private fun selectTab(tab: OngoingTab) {
        updateTabUI(tab)
        viewModel.loadOngoing(tab)
    }

    private fun updateTabUI(tab: OngoingTab) {
        listOf(binding.tabCompleted, binding.tabPending, binding.tabFailed).forEach { btn ->
            btn.setBackgroundResource(R.drawable.growbolt_tab_inactive)
            btn.setTextAppearance(R.style.GrowboltTabInactive)
        }
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