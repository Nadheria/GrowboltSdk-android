package com.growbolt.sdk.offerwall.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.growbolt.sdk.R
import com.growbolt.sdk.databinding.GrowboltItemOfferBinding
import com.growbolt.sdk.network.model.Offer
import com.squareup.picasso.Picasso

internal class OfferListAdapter(
    private val currencySymbol: String,
    private val onOfferClick: (Offer) -> Unit
) : ListAdapter<Offer, OfferListAdapter.OfferViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OfferViewHolder {
        val binding = GrowboltItemOfferBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return OfferViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OfferViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class OfferViewHolder(
        private val binding: GrowboltItemOfferBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(offer: Offer) = with(binding) {
            tvOfferTitle.text = offer.title
            tvOfferSubtitle.text = offer.description ?: root.context.getString(R.string.growbolt_complete_now)
            tvOfferHold.text = offer.holdPeriod?.let { "⏱ $it" } ?: ""
            tvOfferPrice.text = offer.payout?.revenue
                ?.let { "$currencySymbol${"%.2f".format(it)}" }
                ?: offer.payout?.display
                        ?: ""

            if (!offer.logo.isNullOrBlank()) {
                Picasso.get()
                    .load(offer.logo)
                    .placeholder(R.drawable.growbolt_offer_placeholder)
                    .error(R.drawable.growbolt_offer_placeholder)
                    .fit()
                    .centerCrop()
                    .into(ivOfferLogo)
            } else {
                ivOfferLogo.setImageResource(R.drawable.growbolt_offer_placeholder)
            }

            root.setOnClickListener { onOfferClick(offer) }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Offer>() {
            override fun areItemsTheSame(old: Offer, new: Offer) = old.id == new.id
            override fun areContentsTheSame(old: Offer, new: Offer) = old == new
        }
    }
}
