package com.aesprt.foldgo.presentation.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aesprt.foldgo.domain.model.Order
import com.aesprt.foldgo.domain.model.OrderStatus
import com.aesprt.foldgo.domain.model.ServiceItem
import com.aesprt.foldgo.domain.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class OrderEntryUiState(
    val customerName: String = "",
    val phoneNumber: String = "",
    val selectedItems: List<ServiceItem> = emptyList(),
    val isSaving: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
) {
    val totalAmount: Double get() = selectedItems.sumOf { it.totalPrice }
}

class OrderEntryViewModel(
    private val repository: OrderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrderEntryUiState())
    val uiState = _uiState.asStateFlow()

    fun onCustomerNameChange(name: String) {
        _uiState.update { it.copy(customerName = name) }
    }

    fun onPhoneNumberChange(phone: String) {
        _uiState.update { it.copy(phoneNumber = phone) }
    }

    fun addItem(name: String, quantity: Double, unit: String, pricePerUnit: Double) {
        val newItem = ServiceItem(
            name = name,
            quantity = quantity,
            unit = unit,
            pricePerUnit = pricePerUnit,
            totalPrice = quantity * pricePerUnit
        )
        _uiState.update { it.copy(selectedItems = it.selectedItems + newItem) }
    }

    fun removeItem(item: ServiceItem) {
        _uiState.update { it.copy(selectedItems = it.selectedItems - item) }
    }

    fun saveOrder() {
        val currentState = _uiState.value
        if (currentState.customerName.isBlank() || currentState.selectedItems.isEmpty()) {
            _uiState.update { it.copy(error = "Please fill in all details") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                val order = Order(
                    orderId = UUID.randomUUID().toString(),
                    shopId = "default_shop", // TODO: Get from Auth/Session
                    customerId = "cust_${currentState.phoneNumber}",
                    orderNumber = "FG-${System.currentTimeMillis().toString().takeLast(4)}",
                    items = currentState.selectedItems,
                    totalAmount = currentState.totalAmount,
                    paidAmount = 0.0,
                    status = OrderStatus.INTAKE,
                    intakePhotos = emptyList(),
                    machineId = null,
                    staffId = "staff_01",
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                repository.upsertOrder(order)
                _uiState.update { it.copy(isSaving = false, isSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
