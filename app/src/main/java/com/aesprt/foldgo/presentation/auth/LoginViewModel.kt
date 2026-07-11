package com.aesprt.foldgo.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aesprt.foldgo.data.local.PreferenceManager
import com.aesprt.foldgo.domain.usecase.GetShopByIdUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val shopId: String = "",
    val pin: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val loginSuccess: Boolean = false
)

class LoginViewModel(
    private val getShopByIdUseCase: GetShopByIdUseCase,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    fun onShopIdChange(id: String) {
        _uiState.update { it.copy(shopId = id, error = null) }
    }

    fun onPinChange(pin: String) {
        if (pin.length <= 4) {
            _uiState.update { it.copy(pin = pin, error = null) }
        }
    }

    fun login() {
        val state = _uiState.value
        if (state.shopId.isBlank() || state.pin.length < 4) {
            _uiState.update { it.copy(error = "Enter valid Shop ID and 4-digit PIN") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val shop = getShopByIdUseCase(state.shopId).first()
            
            if (shop != null && shop.pin == state.pin) {
                preferenceManager.setCurrentShopId(shop.shopId)
                _uiState.update { it.copy(isLoading = false, loginSuccess = true) }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Invalid Shop ID or PIN") }
            }
        }
    }
}
