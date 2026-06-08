package com.growbolt.sdk.ongoing

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.growbolt.sdk.R
import com.growbolt.sdk.databinding.GrowboltItemOngoingBinding
import com.growbolt.sdk.network.model.OngoingItem
import com.squareup.picasso.Picasso

internal class OngoingAdapter(
    private val currencySymbol: String
) : ListAdapter<OngoingItem, OngoingAdapter.OngoingViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OngoingViewHolder {
        val binding = GrowboltItemOngoingBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return OngoingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OngoingViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class OngoingViewHolder(
        private val binding: GrowboltItemOngoingBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: OngoingItem) = with(binding) {
            tvTitle.text = item.title

            // Subtitle from API e.g. "PartyCodeGenerated", "Install"
            tvSubtitle.text = item.subtitle?.takeIf { it.isNotBlank() } ?: "Register Now"

            // Hold period
            tvHold.text = item.holdPeriod?.takeIf { it.isNotBlank() } ?: ""

            // Payout — now an object, use display string directly
            tvPayout.text = item.payout?.display?.takeIf { it.isNotBlank() }
                ?: item.payout?.amount?.toDoubleOrNull()
                    ?.let { "$currencySymbol${"%.2f".format(it)}" }
                        ?: "$currencySymbol 0"

            // Status badge — use statusLabel from API ("PROGRESS", "COMPLETED", "FAILED")
            val statusText = item.statusLabel ?: item.status.uppercase()
            tvStatus.text = statusText

            val ctx: Context = root.context
            when (item.status.lowercase()) {
                "progress" -> {
                    tvStatus.setTextColor(ContextCompat.getColor(ctx, R.color.growbolt_status_pending_text))
                    tvStatus.setBackgroundResource(R.drawable.growbolt_badge_pending)
                }
                "completed" -> {
                    tvStatus.setTextColor(ContextCompat.getColor(ctx, R.color.growbolt_status_completed_text))
                    tvStatus.setBackgroundResource(R.drawable.growbolt_badge_completed)
                }
                "failed" -> {
                    tvStatus.setTextColor(ContextCompat.getColor(ctx, R.color.growbolt_status_failed_text))
                    tvStatus.setBackgroundResource(R.drawable.growbolt_badge_failed)
                }
                else -> {
                    tvStatus.setTextColor(ContextCompat.getColor(ctx, R.color.growbolt_text_secondary))
                    tvStatus.setBackgroundResource(R.drawable.growbolt_badge_pending)
                }
            }

            // Logo
            if (!item.logo.isNullOrBlank()) {
                Picasso.get()
                    .load(item.logo)
                    .placeholder(R.drawable.growbolt_offer_placeholder)
                    .error(R.drawable.growbolt_offer_placeholder)
                    .fit()
                    .centerCrop()
                    .into(ivLogo)
            } else {
                ivLogo.setImageResource(R.drawable.growbolt_offer_placeholder)
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<OngoingItem>() {
            override fun areItemsTheSame(a: OngoingItem, b: OngoingItem) = a.id == b.id
            override fun areContentsTheSame(a: OngoingItem, b: OngoingItem) = a == b
        }
    }
}