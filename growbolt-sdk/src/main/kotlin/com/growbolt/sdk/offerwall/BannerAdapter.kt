package com.growbolt.sdk.offerwall

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.growbolt.sdk.R
import com.growbolt.sdk.databinding.GrowboltItemBannerBinding
import com.growbolt.sdk.network.model.Banner
import com.squareup.picasso.Picasso

internal class BannerAdapter(
    private val banners: List<Banner>,
    private val baseUrl: String,
    private val onBannerClick: (Banner) -> Unit
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

        fun bind(banner: Banner) {
            // Prepend baseUrl for relative image paths like /media/banners/xxx.png
            val imageUrl = if (banner.image?.startsWith("http") == true) {
                banner.image
            } else {
                "${baseUrl.trimEnd('/')}${banner.image}"
            }

            Picasso.get()
                .load(imageUrl)
                .placeholder(R.drawable.growbolt_banner_placeholder)
                .error(R.drawable.growbolt_banner_placeholder)
                .fit()
                .centerCrop()
                .into(binding.ivBanner)

            binding.root.setOnClickListener { onBannerClick(banner) }
        }
    }
}