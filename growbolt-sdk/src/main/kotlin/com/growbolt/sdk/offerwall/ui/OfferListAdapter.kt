package com.growbolt.sdk.offerwall.ui

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.ImageSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.growbolt.sdk.R
import com.growbolt.sdk.databinding.GrowboltItemOfferBinding
import com.growbolt.sdk.network.model.Offer
import com.squareup.picasso.Picasso

internal class OfferListAdapter(
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

            tvOfferMeta.setOfferMeta(
                subtitle = offer.descriptionLang ?: "",
                holdTime = offer.expiry ?: ""
            )

            // Use currency_reward.display if available, else format user_payout
            tvOfferPrice.text = offer.payout?.currencyReward?.display
                ?: offer.payout?.userPayout?.toDoubleOrNull()
                    ?.let { "$${"%.0f".format(it)}" }
                        ?: offer.payout?.display.orEmpty()

            // Currency icon — from payout.currency_reward.currency_icon first,
            // then payout.currency_icon, then fallback to coin drawable
            val iconUrl = offer.payout?.currencyReward?.currencyIcon
                ?: offer.payout?.currencyIcon

            if (!iconUrl.isNullOrBlank()) {
                Picasso.get()
                    .load(iconUrl)
                    .placeholder(R.drawable.growbolt_ic_coin)
                    .error(R.drawable.growbolt_ic_coin)
                    .fit().centerCrop()
                    .into(ivCurrencyIcon)
            } else {
                ivCurrencyIcon.setImageResource(R.drawable.growbolt_ic_coin)
            }

            // Offer logo
            if (!offer.logo.isNullOrBlank()) {
                Picasso.get()
                    .load(offer.logo)
                    .placeholder(R.drawable.growbolt_offer_placeholder)
                    .error(R.drawable.growbolt_offer_placeholder)
                    .fit().centerCrop()
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

private class CenteredImageSpan(drawable: Drawable) : ImageSpan(drawable) {
    override fun draw(
        canvas: Canvas, text: CharSequence?, start: Int, end: Int,
        x: Float, top: Int, y: Int, bottom: Int, paint: Paint
    ) {
        val d = drawable
        val fm = paint.fontMetricsInt
        val transY = y + fm.descent - d.bounds.bottom -
                ((fm.descent - fm.ascent - d.bounds.height()) / 2)
        canvas.save()
        canvas.translate(x, transY.toFloat())
        d.draw(canvas)
        canvas.restore()
    }
}

private fun TextView.setOfferMeta(subtitle: String, holdTime: String) {
    val context = this.context
    val greenColor = 0xFF10B981.toInt()
    val secondaryColor = ContextCompat.getColor(context, R.color.growbolt_black_color)
    val ssb = SpannableStringBuilder()

    ssb.append(subtitle)
    ssb.setSpan(ForegroundColorSpan(secondaryColor), 0, ssb.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

    val dotStart = ssb.length
    ssb.append("  •  ")
    ssb.setSpan(ForegroundColorSpan(secondaryColor), dotStart, ssb.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

    val clockDrawable = ContextCompat.getDrawable(context, R.drawable.growbolt_ic_clock)
    clockDrawable?.let {
        val wrapped = DrawableCompat.wrap(it).mutate()
        DrawableCompat.setTint(wrapped, greenColor)
        val size = (paint.textSize).toInt()
        wrapped.setBounds(0, 0, size, size)
        val iconStart = ssb.length
        ssb.append(" ")
        ssb.setSpan(CenteredImageSpan(wrapped), iconStart, ssb.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    if (holdTime.isNotBlank()) {
        val holdStart = ssb.length
        ssb.append(" $holdTime")
        ssb.setSpan(ForegroundColorSpan(greenColor), holdStart, ssb.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    this.text = ssb
}