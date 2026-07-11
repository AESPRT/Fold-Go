package com.aesprt.foldgo.presentation.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aesprt.foldgo.core.util.IdGeneratorUtils
import com.aesprt.foldgo.data.local.PreferenceManager
import com.aesprt.foldgo.domain.model.*
import com.aesprt.foldgo.domain.usecase.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class OrderEntryUiState(
    val shopId: String = "",
    val customerName: String = "",
    val phoneNumber: String = "",
    val selectedItems: List<ServiceItem> = emptyList(),
    val availableServices: List<Service> = emptyList(),
    val isSaving: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
) {
    val totalAmount: Double get() = selectedItems.sumOf { it.totalPrice }
}

class OrderEntryViewModel(
    private val upsertOrderUseCase: UpsertOrderUseCase,
    private val getServicesUseCase: GetServicesUseCase,
    private val upsertServiceUseCase: UpsertServiceUseCase,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrderEntryUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadServices()
    }

    private fun loadServices() {
        viewModelScope.launch {
            val shopId = preferenceManager.currentShopId.first() ?: return@launch
            _uiState.update { it.copy(shopId = shopId) }
            getServicesUseCase(shopId).collect { services ->
                if (services.isEmpty()) {
                    seedServices(shopId)
                } else {
                    _uiState.update { it.copy(availableServices = services) }
                }
            }
        }
    }

    private fun seedServices(shopId: String) {
        viewModelScope.launch {
            val defaultServices = listOf(
                Service(IdGeneratorUtils.generateUniqueId("svc"), shopId, "Wash & Dry", 1.0, "KG", 65.0, ServiceType.WASH_DRY),
                Service(IdGeneratorUtils.generateUniqueId("svc"), shopId, "Wash Only", 1.0, "KG", 45.0, ServiceType.WASH),
                Service(IdGeneratorUtils.generateUniqueId("svc"), shopId, "Dry Only", 1.0, "KG", 40.0, ServiceType.DRY),
                Service(IdGeneratorUtils.generateUniqueId("svc"), shopId, "Ironing", 1.0, "PCS", 25.0, ServiceType.IRON)
            )
            defaultServices.forEach { upsertServiceUseCase(it) }
        }
    }

    fun addPredefinedService(name: String, qty: Double, unit: String, price: Double, type: ServiceType = ServiceType.WASH_DRY) {
        val shopId = _uiState.value.shopId
        if (shopId.isBlank()) return
        
        viewModelScope.launch {
            val service = Service(
                serviceId = IdGeneratorUtils.generateUniqueId("svc"),
                shopId = shopId,
                name = name,
                defaultQuantity = qty,
                unit = unit,
                pricePerUnit = price,
                type = type
            )
            upsertServiceUseCase(service)
        }
    }

    fun onCustomerNameChange(name: String) {
        _uiState.update { it.copy(customerName = name) }
    }

    fun onPhoneNumberChange(phone: String) {
        _uiState.update { it.copy(phoneNumber = phone) }
    }

    fun addItem(name: String, quantity: Double, unit: String, pricePerUnit: Double, type: ServiceType = ServiceType.WASH_DRY) {
        val newItem = ServiceItem(
            name = name,
            quantity = quantity,
            unit = unit,
            pricePerUnit = pricePerUnit,
            totalPrice = quantity * pricePerUnit,
            type = type
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
                val shopId = preferenceManager.currentShopId.first() ?: ""
                val staffId = preferenceManager.currentStaffId.first() ?: ""
                val staffName = preferenceManager.currentStaffName.first() ?: "Unknown"
                
                if (shopId.isBlank() || staffId.isBlank()) {
                    _uiState.update { it.copy(error = "Session error. Please login again.") }
                    return@launch
                }

                val order = Order(
                    orderId = IdGeneratorUtils.generateOrderId(),
                    shopId = shopId,
                    customerId = IdGeneratorUtils.generateCustomerId(),
                    customerName = currentState.customerName,
                    customerPhone = currentState.phoneNumber,
                    orderNumber = "FG-${System.currentTimeMillis().toString().takeLast(4)}",
                    items = currentState.selectedItems,
                    totalAmount = currentState.totalAmount,
                    paidAmount = 0.0,
                    status = OrderStatus.INTAKE,
                    deliveryMethod = DeliveryMethod.PICKUP,
                    paymentStatus = PaymentStatus.PENDING,
                    intakePhotos = emptyList(),
                    machineId = null,
                    staffId = staffId,
                    staffName = staffName,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                upsertOrderUseCase(order)
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
