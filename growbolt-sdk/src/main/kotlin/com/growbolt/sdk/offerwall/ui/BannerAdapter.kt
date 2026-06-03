package com.growbolt.sdk.offerwall.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.growbolt.sdk.databinding.GrowboltItemBannerBinding

internal data class BannerItem(
    val imageUrl: String?,
    val title: String?,
    val clickUrl: String?
)

internal class BannerAdapter(
    private val banners: List<BannerItem>,
    private val onClick: (BannerItem) -> Unit
) : RecyclerView.Adapter<BannerAdapter.BannerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
        val binding = GrowboltItemBannerBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return BannerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BannerViewHolder, position: Int) =
        holder.bind(banners[position])

    override fun getItemCount() = banners.size

    inner class BannerViewHolder(
        private val binding: GrowboltItemBannerBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(banner: BannerItem) {
            Glide.with(binding.root.context)
                .load(banner.imageUrl)
                .centerCrop()
                .into(binding.ivBanner)
            binding.root.setOnClickListener { onClick(banner) }
        }
    }
}
