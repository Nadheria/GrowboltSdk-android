package com.growbolt.sdk.ongoing

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.growbolt.sdk.GrowboltSdk
import com.growbolt.sdk.network.ApiResult
import com.growbolt.sdk.network.model.OngoingCounts
import com.growbolt.sdk.network.model.OngoingItem
import com.growbolt.sdk.network.safeApiCall
import com.growbolt.sdk.util.Logger
import kotlinx.coroutines.launch

internal class OngoingViewModel : ViewModel() {

    private val _items = MutableLiveData<List<OngoingItem>>()
    val items: LiveData<List<OngoingItem>> = _items

    private val _counts = MutableLiveData<OngoingCounts?>()
    val counts: LiveData<OngoingCounts?> = _counts

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val api get() = GrowboltSdk.apiClient.ongoingApi
    private val config get() = GrowboltSdk.config

    var currentTab: OngoingTab = OngoingTab.PROGRESS
        private set

    fun loadOngoing(tab: OngoingTab = currentTab) {
        currentTab = tab
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            val result = safeApiCall {
                api.getOngoing(sub4 = GrowboltSdk.config.userId, tab = tab.apiValue)
            }
            _isLoading.value = false
            when (result) {
                is ApiResult.Success -> {
                    _items.value = result.data.items
                    _counts.value = result.data.counts
                    Logger.d(TAG, "Loaded ${result.data.items.size} ongoing items for tab=${tab.apiValue}")
                }
                is ApiResult.Error -> {
                    _error.value = "Error ${result.code}: ${result.message}"
                    Logger.e(TAG, _error.value!!)
                }
                is ApiResult.Exception -> {
                    _error.value = result.throwable.message ?: "Network error"
                    Logger.e(TAG, _error.value!!, result.throwable)
                }
            }
        }
    }

    companion object { private const val TAG = "OngoingViewModel" }
}
