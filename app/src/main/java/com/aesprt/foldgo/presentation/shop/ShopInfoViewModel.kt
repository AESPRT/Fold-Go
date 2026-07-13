package com.aesprt.foldgo.presentation.shop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aesprt.foldgo.data.local.PreferenceManager
import com.aesprt.foldgo.domain.model.Shop
import com.aesprt.foldgo.domain.repository.ShopRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ShopInfoUiState(
    val shop: Shop? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val isUpdateSuccess: Boolean = false
)

class ShopInfoViewModel(
    private val shopRepository: ShopRepository,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShopInfoUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadShop()
    }

    private fun loadShop() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val shopId = preferenceManager.currentShopId.first()
            if (shopId != null) {
                shopRepository.getShop(shopId).collect { shop ->
                    _uiState.update { it.copy(shop = shop, isLoading = false) }
                }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Shop not found") }
            }
        }
    }

    fun updateShop(updatedShop: Shop) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                shopRepository.upsertShop(updatedShop)
                _uiState.update { it.copy(isSaving = false, isUpdateSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }

    fun clearSuccess() {
        _uiState.update { it.copy(isUpdateSuccess = false) }
    }
}
