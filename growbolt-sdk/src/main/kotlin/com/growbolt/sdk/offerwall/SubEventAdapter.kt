package com.growbolt.sdk.offerwall

import android.os.Build
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.growbolt.sdk.R
import com.growbolt.sdk.databinding.GrowboltItemSubEventBinding
import com.growbolt.sdk.network.model.Payment

internal class SubEventAdapter(
    private val currencySymbol: String
) : ListAdapter<Payment, SubEventAdapter.SubEventViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubEventViewHolder {
        val binding = GrowboltItemSubEventBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SubEventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SubEventViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    inner class SubEventViewHolder(
        private val binding: GrowboltItemSubEventBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(payment: Payment, position: Int) {
            val amount = payment.total?.toDoubleOrNull() ?: 0.0
            val isFirstCard = position == 0

            // Title
            binding.tvSubEventGoal.text = payment.title ?: "Step ${position + 1}"

            // Payout badge
            binding.tvSubEventPayout.text = "$currencySymbol${"%.0f".format(amount)}"

            // Description — render as HTML
            val desc = payment.description?.takeIf { it.isNotBlank() }

            if (!desc.isNullOrBlank()) {
                // Has description — show it, hide locked message
                binding.tvSubEventDescription.visibility = View.VISIBLE
                binding.tvLockedMessage.visibility = View.GONE
                binding.tvSubEventDescription.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Html.fromHtml(desc, Html.FROM_HTML_MODE_LEGACY)
                } else {
                    @Suppress("DEPRECATION")
                    Html.fromHtml(desc)
                }
            } else if (!isFirstCard) {
                // No description + not first card = locked state
                binding.tvSubEventDescription.visibility = View.GONE
                binding.tvLockedMessage.visibility = View.VISIBLE
            } else {
                // First card, no description
                binding.tvSubEventDescription.visibility = View.GONE
                binding.tvLockedMessage.visibility = View.GONE
            }

            // Icon: clock for first card, lock for subsequent
            if (isFirstCard) {
                binding.ivEventIcon.setImageResource(R.drawable.ic_clock)
            } else {
                binding.ivEventIcon.setImageResource(R.drawable.ic_lock)
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Payment>() {
            override fun areItemsTheSame(old: Payment, new: Payment) = old.id == new.id
            override fun areContentsTheSame(old: Payment, new: Payment) = old == new
        }
    }
}
