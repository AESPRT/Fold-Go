package com.aesprt.foldgo.presentation.shop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aesprt.foldgo.core.util.IdGeneratorUtils
import com.aesprt.foldgo.data.local.PreferenceManager
import com.aesprt.foldgo.domain.model.Shop
import com.aesprt.foldgo.domain.usecase.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ShopRegistrationUiState(
    val shopName: String = "",
    val address: String = "",
    val ownerName: String = "",
    val pin: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

class ShopRegistrationViewModel(
    private val upsertShopUseCase: UpsertShopUseCase,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShopRegistrationUiState())
    val uiState = _uiState.asStateFlow()

    fun onShopNameChange(name: String) {
        _uiState.update { it.copy(shopName = name) }
    }

    fun onAddressChange(address: String) {
        _uiState.update { it.copy(address = address) }
    }

    fun onOwnerNameChange(name: String) {
        _uiState.update { it.copy(ownerName = name) }
    }

    fun onPinChange(pin: String) {
        if (pin.length <= 4) {
            _uiState.update { it.copy(pin = pin) }
        }
    }

    fun registerShop() {
        val state = _uiState.value
        if (state.shopName.isBlank() || state.address.isBlank() || state.pin.length < 4) {
            _uiState.update { it.copy(error = "Please fill in all details including a 4-digit PIN") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val shopId = IdGeneratorUtils.generateShopId()
                val shop = Shop(
                    shopId = shopId,
                    name = state.shopName,
                    address = state.address,
                    ownerId = IdGeneratorUtils.generateUniqueId("owner"),
                    pin = state.pin,
                    settings = emptyMap(),
                    createdAt = System.currentTimeMillis()
                )
                upsertShopUseCase(shop)
                preferenceManager.setCurrentShopId(shopId)
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
