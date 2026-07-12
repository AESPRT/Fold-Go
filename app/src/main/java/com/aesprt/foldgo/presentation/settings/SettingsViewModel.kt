package com.aesprt.foldgo.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aesprt.foldgo.data.local.PreferenceManager
import com.aesprt.foldgo.domain.model.Shop
import com.aesprt.foldgo.domain.usecase.GetShopByIdUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SettingsUiState(
    val shop: Shop? = null,
    val staffName: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

class SettingsViewModel(
    private val getShopByIdUseCase: GetShopByIdUseCase,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val shopId = preferenceManager.currentShopId.first()
            val staffName = preferenceManager.currentStaffName.first() ?: ""
            
            _uiState.update { it.copy(staffName = staffName) }

            if (shopId != null) {
                val shop = getShopByIdUseCase(shopId).firstOrNull()
                _uiState.update { it.copy(shop = shop, isLoading = false) }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            preferenceManager.setCurrentStaffId(null)
            preferenceManager.setCurrentStaffName(null)
            onSuccess()
        }
    }

    fun endShift(onSuccess: () -> Unit) {
        // For now, endShift just logs out. 
        // In the future, this can trigger a shift report generation.
        logout(onSuccess)
    }
}
