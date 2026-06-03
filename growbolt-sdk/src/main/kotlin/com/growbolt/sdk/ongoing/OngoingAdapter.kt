package com.growbolt.sdk.ongoing

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
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
            tvSubtitle.text = root.context.getString(R.string.growbolt_complete_now)
            tvHold.text = item.holdPeriod?.let { "⏱ $it" } ?: ""
            tvPayout.text = "$currencySymbol${"%.0f".format(item.payout)}"
            tvStatus.text = item.status.uppercase()

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
            }

            Picasso.get()
                .load(item.logo)
                .placeholder(R.drawable.growbolt_offer_placeholder)
                .error(R.drawable.growbolt_offer_placeholder)
                .fit()
                .centerCrop()
                .into(ivLogo)
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<OngoingItem>() {
            override fun areItemsTheSame(a: OngoingItem, b: OngoingItem) = a.id == b.id
            override fun areContentsTheSame(a: OngoingItem, b: OngoingItem) = a == b
        }
    }
}
