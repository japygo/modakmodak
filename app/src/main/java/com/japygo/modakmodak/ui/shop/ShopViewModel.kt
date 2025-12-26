package com.japygo.modakmodak.ui.shop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.japygo.modakmodak.data.entity.Inventory
import com.japygo.modakmodak.data.entity.ShopItem
import com.japygo.modakmodak.data.entity.User
import com.japygo.modakmodak.data.repository.ModakRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
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

    private val _purchaseStatus = MutableStateFlow<PurchaseStatus?>(null) // Toasts/Feedback
    val purchaseStatus: StateFlow<PurchaseStatus?> = _purchaseStatus.asStateFlow()

    sealed class PurchaseStatus {
        data class Bought(val itemId: String) : PurchaseStatus()
        data class BuyFailed(val itemId: String) : PurchaseStatus()
        object Used : PurchaseStatus()
        object UseFailed : PurchaseStatus()
    }

    fun setTab(index: Int) {
        _selectedTab.value = index
    }

    fun buyItem(item: ShopItem, quantity: Int) {
        viewModelScope.launch {
            if (repository.buyItem(item.id, quantity)) {
                _purchaseStatus.value = PurchaseStatus.Bought(item.id)
            } else {
                _purchaseStatus.value = PurchaseStatus.BuyFailed(item.id)
            }
        }
    }

    fun useItem(item: Inventory, quantity: Int) {
        viewModelScope.launch {
            if (repository.useItem(item.itemId, quantity)) {
                _purchaseStatus.value = PurchaseStatus.Used
            } else {
                _purchaseStatus.value = PurchaseStatus.UseFailed
            }
        }
    }

    fun clearStatus() {
        _purchaseStatus.value = null
    }
}
