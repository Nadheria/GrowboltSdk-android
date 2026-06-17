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
import com.squareup.picasso.Picasso

internal class SubEventAdapter(
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
            val isFirstCard = position == 0

            // Title
            binding.tvSubEventGoal.text = payment.title ?: "Step ${position + 1}"

            // Payout badge — use payment.currency (e.g. "Gems") not currencySymbol (₹).
            // currencySymbol is the SDK's app-currency (rupees) and is unrelated to the
            // offer's reward currency, which varies per offer (Gems, Coins, etc.) and
            // comes from payment.currency — same source OfferListAdapter uses via currencyReward.
            val amount = payment.userPayout?.toDoubleOrNull()
                ?: payment.total?.toDoubleOrNull()

            binding.tvSubEventPayout.text = amount?.let { value ->
                val formatted = if (value == value.toLong().toDouble()) {
                    "%,d".format(value.toLong())
                } else {
                    "%,.2f".format(value)
                }
                val currencyName = payment.currency?.takeIf { it.isNotBlank() } ?: ""
                if (currencyName.isNotBlank()) "$formatted $currencyName" else formatted
            } ?: ""

            val iconUrl = payment.currencyIcon
            if (!iconUrl.isNullOrBlank()) {
                Picasso.get()
                    .load(iconUrl)
                    .placeholder(R.drawable.growbolt_ic_coin)
                    .error(R.drawable.growbolt_ic_coin)
                    .fit().centerCrop()
                    .into(binding.ivPayoutCurrencyIcon)
                binding.ivPayoutCurrencyIcon.visibility = View.VISIBLE
            } else {
                binding.ivPayoutCurrencyIcon.setImageResource(R.drawable.growbolt_ic_coin)
                binding.ivPayoutCurrencyIcon.visibility = View.VISIBLE
            }

            // Description — render as HTML
            val desc = payment.description?.takeIf { it.isNotBlank() }
            if (!desc.isNullOrBlank()) {
                binding.tvSubEventDescription.visibility = View.VISIBLE
                binding.tvLockedMessage.visibility = View.GONE
                binding.tvSubEventDescription.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Html.fromHtml(desc, Html.FROM_HTML_MODE_LEGACY)
                } else {
                    @Suppress("DEPRECATION")
                    Html.fromHtml(desc)
                }
            } else if (!isFirstCard) {
                binding.tvSubEventDescription.visibility = View.GONE
                binding.tvLockedMessage.visibility = View.VISIBLE
            } else {
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