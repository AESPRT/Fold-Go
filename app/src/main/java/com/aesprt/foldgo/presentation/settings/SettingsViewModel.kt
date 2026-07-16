package com.aesprt.foldgo.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aesprt.foldgo.data.local.PreferenceManager
import com.aesprt.foldgo.domain.model.Shop
import com.aesprt.foldgo.domain.usecase.GetShopByIdUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SettingsUiState(
    val shop: Shop? = null,
    val staffName: String = "",
    val deliveryFees: List<Double> = emptyList(),
    val smsCredits: Int = 0,
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

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val staffName = preferenceManager.currentStaffName.first() ?: ""
            
            combine(
                preferenceManager.currentShopId.flatMapLatest { id ->
                    if (id != null) getShopByIdUseCase(id) else flowOf(null)
                },
                preferenceManager.deliveryFees,
                preferenceManager.smsCredits
            ) { shop, fees, credits ->
                _uiState.update { 
                    it.copy(
                        shop = shop,
                        staffName = staffName,
                        deliveryFees = fees,
                        smsCredits = credits,
                        isLoading = false
                    )
                }
            }.collect()
        }
    }

    fun addDeliveryFee(fee: Double) {
        viewModelScope.launch {
            val currentFees = uiState.value.deliveryFees
            if (!currentFees.contains(fee)) {
                preferenceManager.setDeliveryFees(currentFees + fee)
            }
        }
    }

    fun removeDeliveryFee(fee: Double) {
        viewModelScope.launch {
            val currentFees = uiState.value.deliveryFees
            preferenceManager.setDeliveryFees(currentFees - fee)
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
