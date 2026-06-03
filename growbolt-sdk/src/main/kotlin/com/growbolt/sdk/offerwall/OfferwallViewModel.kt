package com.growbolt.sdk.offerwall

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.growbolt.sdk.GrowboltSdk
import com.growbolt.sdk.network.ApiResult
import com.growbolt.sdk.network.model.Banner
import com.growbolt.sdk.network.model.Offer
import com.growbolt.sdk.network.model.OfferCategory
import com.growbolt.sdk.network.model.OfferDetail
import com.growbolt.sdk.network.safeApiCall
import com.growbolt.sdk.util.Logger
import kotlinx.coroutines.launch

internal class OfferwallViewModel : ViewModel() {

    private val _offers = MutableLiveData<List<Offer>>()
    val offers: LiveData<List<Offer>> = _offers

    private val _banners = MutableLiveData<List<Banner>>()
    val banners: LiveData<List<Banner>> = _banners
    private val _categories = MutableLiveData<List<OfferCategory>>()
    val categories: LiveData<List<OfferCategory>> = _categories

    private val _offerDetail = MutableLiveData<OfferDetail?>()
    val offerDetail: LiveData<OfferDetail?> = _offerDetail

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var allOffers: List<Offer> = emptyList()
    private var activeCategory: String? = null

    fun loadOffers(
        search: String? = null,
        category: String? = null,
        tag: String? = null,
        os: String? = null
    ) {
        _isLoading.value = true
        _error.value = null
        GrowboltSdk.offerwallCallback?.onOfferwallLoadingStarted()

        viewModelScope.launch {
            val result = safeApiCall {
                GrowboltSdk.apiClient.offersApi.listOffers(
                    search = search,
                    category = category,
                    tag = tag,
                    os = os
                )
            }
            _isLoading.value = false

            when (result) {
                is ApiResult.Success<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    val data = (result as ApiResult.Success<com.growbolt.sdk.network.model.OffersResponse>).data
                    allOffers = data.offers
                    _offers.value = allOffers
                    GrowboltSdk.offerwallCallback?.onOfferwallLoadingFinished()
                    Logger.d(TAG, "Loaded ${allOffers.size} offers")
                }
                is ApiResult.Error -> {
                    val msg = "Error ${result.code}: ${result.message}"
                    _error.value = msg
                    GrowboltSdk.offerwallCallback?.onOfferwallLoadingFailed(msg)
                }
                is ApiResult.Exception -> {
                    val msg = result.throwable.message ?: "Network error"
                    _error.value = msg
                    GrowboltSdk.offerwallCallback?.onOfferwallLoadingFailed(msg)
                }
            }
        }
    }

    fun loadCategories() {
        viewModelScope.launch {
            val result = safeApiCall { GrowboltSdk.apiClient.offersApi.listCategories() }
            when (result) {
                is ApiResult.Success<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    val data = (result as ApiResult.Success<com.growbolt.sdk.network.model.CategoriesResponse>).data
                    _categories.value = data.categories
                }
                else -> { /* chips just won't populate — non-fatal */ }
            }
        }
    }


    fun loadBanners() {
        viewModelScope.launch {
            val result = safeApiCall { GrowboltSdk.apiClient.offersApi.listBanners() }
            when (result) {
                is ApiResult.Success<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    val data = (result as ApiResult.Success<List<Banner>>).data
                    // Only show active banners, sorted by display_order
                    _banners.value = data
                        .filter { it.isActive }
                        .sortedBy { it.displayOrder }
                    Logger.d(TAG, "Loaded ${data.size} banners")
                }
                is ApiResult.Error -> Logger.e(TAG, "Banners error: ${result.message}")
                is ApiResult.Exception -> Logger.e(TAG, "Banners exception", result.throwable)
            }
        }
    }

    fun loadOfferDetail(offerId: Int) {
        viewModelScope.launch {
            val result = safeApiCall { GrowboltSdk.apiClient.offersApi.getOfferDetail(offerId) }
            when (result) {
                is ApiResult.Success<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    _offerDetail.value = (result as ApiResult.Success<OfferDetail>).data
                }
                is ApiResult.Error -> _error.value = result.message
                is ApiResult.Exception -> _error.value = result.throwable.message
            }
        }
    }

    fun filterByCategory(category: String?) {
        activeCategory = category
        _offers.value = if (category.isNullOrBlank()) allOffers
        else allOffers.filter { offer ->
            offer.fullCategories?.any { it.title.equals(category, ignoreCase = true) } == true
                    || offer.categories?.any { it.equals(category, ignoreCase = true) } == true
        }
    }



    companion object { private const val TAG = "OfferwallViewModel" }
}