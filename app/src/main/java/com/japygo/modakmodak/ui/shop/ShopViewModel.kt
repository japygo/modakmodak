package com.japygo.modakmodak.ui.shop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.japygo.modakmodak.data.entity.Inventory
import com.japygo.modakmodak.data.entity.ShopItem
import com.japygo.modakmodak.data.entity.User
import com.japygo.modakmodak.data.repository.ModakRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ShopViewModel(
    private val repository: ModakRepository,
) : ViewModel() {

    val user: StateFlow<User?> = repository.userFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val shopItems: StateFlow<List<ShopItem>> = repository.shopItemsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val inventory: StateFlow<List<Inventory>> = repository.inventoryFlow
        .map { list -> list.filter { it.quantity > 0 } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 0 = Shop, 1 = Inventory
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _shopEvent = kotlinx.coroutines.flow.MutableSharedFlow<ShopEvent>(extraBufferCapacity = 1)
    val shopEvent = _shopEvent.asSharedFlow()

    sealed class ShopEvent {
        data class Bought(val itemId: String) : ShopEvent()
        data class BuyFailed(val itemId: String) : ShopEvent()
        object Used : ShopEvent()
        object UseFailed : ShopEvent()
        data class AdRewardEarned(val amount: Int) : ShopEvent()
        object AdLoadFailed : ShopEvent()
    }

    fun setTab(index: Int) {
        _selectedTab.value = index
    }

    fun buyItem(item: ShopItem, quantity: Int) {
        viewModelScope.launch {
            if (repository.buyItem(item.id, quantity)) {
                _shopEvent.emit(ShopEvent.Bought(item.id))
            } else {
                _shopEvent.emit(ShopEvent.BuyFailed(item.id))
            }
        }
    }

    fun useItem(item: Inventory, quantity: Int) {
        viewModelScope.launch {
            if (repository.useItem(item.itemId, quantity)) {
                _shopEvent.emit(ShopEvent.Used)
            } else {
                _shopEvent.emit(ShopEvent.UseFailed)
            }
        }
    }

    // Ad Logic
    private val settingsRepository = repository.settingsRepository
    
    private val _dailyAdCount = MutableStateFlow(0)
    val dailyAdCount: StateFlow<Int> = _dailyAdCount.asStateFlow()

    private val _dailyLimit = 3
    
    val isAdLoaded: StateFlow<Boolean> = com.japygo.modakmodak.utils.AdMobManager.isShopAdLoaded
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    init {
        viewModelScope.launch {
            checkDailyAdLimit()
        }
    }

    private suspend fun checkDailyAdLimit() {
        val today = java.time.LocalDate.now().toString() // ISO 8601
        val lastDate = settingsRepository.lastAdViewDate.first()
        val count = settingsRepository.dailyAdViewCount.first()

        if (lastDate != today) {
            // New day, reset
            settingsRepository.updateAdViewCount(today, 0)
            _dailyAdCount.value = 0
        } else {
            _dailyAdCount.value = count
        }
    }

    fun watchAdForCoins(activity: android.app.Activity) {
        viewModelScope.launch {
            checkDailyAdLimit() // Refresh first
            if (_dailyAdCount.value >= _dailyLimit) {
                return@launch
            }

            com.japygo.modakmodak.utils.AdMobManager.showRewardedAd(
                activity = activity,
                type = com.japygo.modakmodak.utils.AdMobManager.AdType.SHOP,
                onUserEarnedReward = { 
                    viewModelScope.launch {
                        val rewardAmount = 120
                        repository.addCoins(rewardAmount)
                        val newCount = _dailyAdCount.value + 1
                        val today = java.time.LocalDate.now().toString()
                        settingsRepository.updateAdViewCount(today, newCount)
                        _dailyAdCount.value = newCount
                        _shopEvent.emit(ShopEvent.AdRewardEarned(rewardAmount))
                    }
                },
                onAdDismissed = {},
                onAdFailed = {
                    viewModelScope.launch {
                        _shopEvent.emit(ShopEvent.AdLoadFailed)
                    }
                }
            )
        }
    }
}
