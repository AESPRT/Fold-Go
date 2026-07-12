package com.aesprt.foldgo.presentation.shop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aesprt.foldgo.core.util.IdGeneratorUtils
import com.aesprt.foldgo.data.local.PreferenceManager
import com.aesprt.foldgo.domain.model.Shop
import com.aesprt.foldgo.domain.model.Staff
import com.aesprt.foldgo.domain.usecase.UpsertShopUseCase
import com.aesprt.foldgo.domain.usecase.UpsertStaffUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ShopRegistrationUiState(
    val shopName: String = "",
    val address: String = "",
    val mobileNumber: String = "",
    val ownerName: String = "",
    val pin: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
)

class ShopRegistrationViewModel(
    private val upsertShopUseCase: UpsertShopUseCase,
    private val upsertStaffUseCase: UpsertStaffUseCase,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShopRegistrationUiState())
    val uiState: StateFlow<ShopRegistrationUiState> = _uiState.asStateFlow()

    fun onShopNameChange(name: String) {
        _uiState.update { it.copy(shopName = name) }
    }

    fun onAddressChange(address: String) {
        _uiState.update { it.copy(address = address) }
    }

    fun onMobileNumberChange(number: String) {
        val digitsOnly = number.filter { it.isDigit() }
        val formatted = when {
            digitsOnly.isEmpty() -> ""
            digitsOnly.startsWith("09") -> "+639" + digitsOnly.drop(2)
            digitsOnly.startsWith("9") && !digitsOnly.startsWith("99") -> "+639" + digitsOnly.drop(1)
            digitsOnly.startsWith("639") -> "+$digitsOnly"
            else -> digitsOnly
        }.take(13)
        _uiState.update { it.copy(mobileNumber = formatted) }
    }

    fun onOwnerNameChange(name: String) {
        _uiState.update { it.copy(ownerName = name) }
    }

    fun onPinChange(pin: String) {
        if (pin.length <= 4 && pin.all { it.isDigit() }) {
            _uiState.update { it.copy(pin = pin) }
        }
    }

    fun registerShop() {
        val currentState = _uiState.value
        if (currentState.shopName.isBlank() ||
            currentState.ownerName.isBlank() ||
            currentState.pin.length < 4 ||
            currentState.mobileNumber.length < 13
        ) {
            _uiState.update { it.copy(error = "Please fill in all details correctly.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val shopId = IdGeneratorUtils.generateShopId()
                val ownerId = IdGeneratorUtils.generateStaffId()

                // Create Owner as first Staff
                val ownerStaff = Staff(
                    staffId = ownerId,
                    shopId = shopId,
                    name = currentState.ownerName,
                    role = "Owner",
                    createdAt = System.currentTimeMillis()
                )
                upsertStaffUseCase(ownerStaff)

                val shop = Shop(
                    shopId = shopId,
                    name = currentState.shopName,
                    address = currentState.address,
                    mobileNumber = currentState.mobileNumber.replace("+", ""),
                    ownerId = ownerId,
                    pin = currentState.pin,
                    settings = emptyMap(),
                    createdAt = System.currentTimeMillis()
                )
                upsertShopUseCase(shop)

                preferenceManager.setCurrentShopId(shopId)
                preferenceManager.setCurrentStaffId(ownerId)
                preferenceManager.setCurrentStaffName(currentState.ownerName)
                preferenceManager.setOnboardingCompleted(true)

                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
