package com.growbolt.sdk.ongoing

import android.content.Context
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
import com.growbolt.sdk.databinding.GrowboltItemOngoingBinding
import com.growbolt.sdk.network.model.OngoingItem
import com.squareup.picasso.Picasso

internal class OngoingAdapter(
    private val onItemClick: (OngoingItem) -> Unit
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

            // Subtitle with clock + hold period — same pattern as offer list
            tvSubtitle.setOngoingMeta(
                subtitle = item.subtitle?.takeIf { it.isNotBlank() } ?: "Register Now",
                holdTime = item.holdPeriod?.takeIf { it.isNotBlank() } ?: ""
            )

            // Payout
            tvPayout.text = item.payout?.display?.takeIf { it.isNotBlank() }
                ?: item.payout?.amount?.let { amount ->
                    val formatted = if (amount == amount.toLong().toDouble()) {
                        "%,d".format(amount.toLong())
                    } else {
                        "%,.2f".format(amount)
                    }
                    val currencyName = item.payout?.currencyName?.takeIf { it.isNotBlank() }
                    if (currencyName != null) "$formatted $currencyName" else formatted
                } ?: "0"

            // Currency icon
            val currencyIconUrl = item.payout?.currencyIcon
            if (!currencyIconUrl.isNullOrBlank()) {
                Picasso.get()
                    .load(currencyIconUrl)
                    .placeholder(R.drawable.growbolt_ic_coin)
                    .error(R.drawable.growbolt_ic_coin)
                    .fit().centerCrop()
                    .into(ivPayoutCurrencyIcon)
            } else {
                ivPayoutCurrencyIcon.setImageResource(R.drawable.growbolt_ic_coin)
            }

            // Status badge
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
                    .fit().centerCrop()
                    .into(ivLogo)
            } else {
                ivLogo.setImageResource(R.drawable.growbolt_offer_placeholder)
            }

            // Click — only progress items navigate to offer detail
            root.setOnClickListener {
                if (item.status.lowercase() == "progress") {
                    onItemClick(item)
                }
            }
            root.isClickable = item.status.lowercase() == "progress"
            root.alpha = if (item.status.lowercase() == "progress") 1.0f else 0.85f
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<OngoingItem>() {
            override fun areItemsTheSame(a: OngoingItem, b: OngoingItem) = a.id == b.id
            override fun areContentsTheSame(a: OngoingItem, b: OngoingItem) = a == b
        }
    }
}

// ── Same CenteredImageSpan used in OfferListAdapter ───────────────────────────
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

// ── Meta text: "Register Now  •  🕐 7" ───────────────────────────────────────
private fun TextView.setOngoingMeta(subtitle: String, holdTime: String) {
    val context = this.context
    val greenColor = 0xFF10B981.toInt()
    val secondaryColor = ContextCompat.getColor(context, R.color.growbolt_black_color)
    val ssb = SpannableStringBuilder()

    // Subtitle
    ssb.append(subtitle)
    ssb.setSpan(
        ForegroundColorSpan(secondaryColor),
        0, ssb.length,
        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
    )

    // Only show clock + holdTime if holdTime is not blank
    if (holdTime.isNotBlank()) {
        // Dot separator
        val dotStart = ssb.length
        ssb.append("  •  ")
        ssb.setSpan(
            ForegroundColorSpan(secondaryColor),
            dotStart, ssb.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Clock icon
        val clockDrawable = ContextCompat.getDrawable(context, R.drawable.growbolt_ic_clock)
        clockDrawable?.let {
            val wrapped = DrawableCompat.wrap(it).mutate()
            DrawableCompat.setTint(wrapped, greenColor)
            val size = (paint.textSize).toInt()
            wrapped.setBounds(0, 0, size, size)
            val iconStart = ssb.length
            ssb.append(" ")
            ssb.setSpan(
                CenteredImageSpan(wrapped),
                iconStart, ssb.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        // Hold time number in green
        val holdStart = ssb.length
        ssb.append(" $holdTime")
        ssb.setSpan(
            ForegroundColorSpan(greenColor),
            holdStart, ssb.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }

    this.text = ssb
}