package com.aesprt.foldgo.presentation.order

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aesprt.foldgo.core.util.IdGeneratorUtils
import com.aesprt.foldgo.data.local.PreferenceManager
import com.aesprt.foldgo.domain.model.*
import com.aesprt.foldgo.domain.model.enums.*
import com.aesprt.foldgo.domain.usecase.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class OrderEntryUiState(
    val shopId: String = "",
    val customerName: String = "",
    val phoneNumber: String = "",
    val phoneTextFieldValue: TextFieldValue = TextFieldValue(""),
    val customerAddress: String = "",
    val suggestedCustomers: List<Customer> = emptyList(),
    val deliveryMethod: DeliveryMethod = DeliveryMethod.PICKUP,
    val selectedServiceIds: Set<String> = emptySet(),
    val weight: String = "5.0",
    val selectedItems: List<ServiceItem> = emptyList(),
    val availableServices: List<Service> = emptyList(),
    val availableAddOns: List<AddOn> = emptyList(),
    val selectedAddOns: List<AddOn> = emptyList(),
    val availableMachines: List<Machine> = emptyList(),
    val assignedMachine: Machine? = null,
    val deliveryFee: String = "50.0",
    val availableDeliveryFees: List<Double> = emptyList(),
    val isSaving: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
) {
    val servicesTotal: Double get() = when {
        selectedItems.isEmpty() -> 0.0
        else -> {
            selectedItems.sumOf { it.totalPrice }
        }
    }
    val addOnsTotal: Double get() = selectedAddOns.sumOf { it.price }
    val totalAmount: Double get() {
        val base = servicesTotal + addOnsTotal
        return if (deliveryMethod == DeliveryMethod.DELIVERY) {
            base + (deliveryFee.toDoubleOrNull() ?: 0.0)
        } else {
            base
        }
    }
}

class OrderEntryViewModel(
    private val upsertOrderUseCase: UpsertOrderUseCase,
    private val getServicesUseCase: GetServicesUseCase,
    private val getMachinesUseCase: GetMachinesUseCase,
    private val assignMachineToOrderUseCase: AssignMachineToOrderUseCase,
    private val updateMachineStatusUseCase: UpdateMachineStatusUseCase,
    private val sendSmsUseCase: SendSmsUseCase,
    private val upsertCustomerUseCase: UpsertCustomerUseCase,
    private val searchCustomersUseCase: SearchCustomersUseCase,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrderEntryUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadServices()
        loadMachines()
        loadAddOns()
        loadDeliveryFees()
    }

    private fun loadDeliveryFees() {
        viewModelScope.launch {
            preferenceManager.deliveryFees.collect { fees ->
                _uiState.update { it.copy(availableDeliveryFees = fees) }
            }
        }
    }

    fun onDeliveryFeeChange(fee: String) {
        _uiState.update { it.copy(deliveryFee = fee) }
    }

    private fun loadMachines() {
        viewModelScope.launch {
            getMachinesUseCase().collect { machines ->
                val available = machines.filter { it.status == MachineStatus.IDLE && it.assignedOrderId == null }
                _uiState.update { it.copy(availableMachines = available) }
            }
        }
    }

    private fun loadAddOns() {
        // Placeholder for loading add-ons from repository
        _uiState.update { 
            it.copy(
                availableAddOns = listOf(
                    AddOn("ao1", "Fabric Softener", "Adds softener to wash cycle", 30.0, ServiceScope.ALL, true),
                    AddOn("ao2", "Extra Rinse", "One additional rinse cycle", 25.0, ServiceScope.WASH_ONLY, true),
                    AddOn("ao3", "Express Service", "Ready in under 2 hours", 100.0, ServiceScope.ALL, true)
                )
            )
        }
    }

    fun toggleAddOn(addOn: AddOn) {
        _uiState.update { state ->
            val newSelected = if (state.selectedAddOns.contains(addOn)) {
                state.selectedAddOns - addOn
            } else {
                state.selectedAddOns + addOn
            }
            state.copy(selectedAddOns = newSelected)
        }
    }

    fun assignMachine(machine: Machine) {
        _uiState.update { it.copy(assignedMachine = machine) }
    }

    private fun loadServices() {
        viewModelScope.launch {
            val shopId = preferenceManager.currentShopId.first() ?: return@launch
            _uiState.update { it.copy(shopId = shopId) }
            getServicesUseCase(shopId).collect { services ->
                if (services.isNotEmpty()) {
                    _uiState.update { it.copy(availableServices = services) }
                }
            }
        }
    }

    fun onCustomerNameChange(name: String) {
        _uiState.update { it.copy(customerName = name) }
        searchCustomers(name)
    }

    private fun searchCustomers(query: String) {
        if (query.length < 2) {
            _uiState.update { it.copy(suggestedCustomers = emptyList()) }
            return
        }
        viewModelScope.launch {
            searchCustomersUseCase(query).collect { customers ->
                _uiState.update { it.copy(suggestedCustomers = customers) }
            }
        }
    }

    fun selectCustomer(customer: Customer) {
        _uiState.update { 
            it.copy(
                customerName = customer.name,
                phoneNumber = customer.phone,
                phoneTextFieldValue = TextFieldValue(customer.phone),
                customerAddress = customer.address,
                suggestedCustomers = emptyList()
            )
        }
    }

    fun onPhoneNumberChange(value: TextFieldValue) {
        val phone = value.text
        val digitsOnly = phone.filter { it.isDigit() }
        
        val formatted = when {
            digitsOnly.startsWith("09") -> "+639" + digitsOnly.drop(2)
            digitsOnly.startsWith("9") && !digitsOnly.startsWith("99") -> "+639" + digitsOnly.drop(1)
            digitsOnly.startsWith("639") -> "+$digitsOnly"
            else -> phone
        }.take(13) // +639 + 9 digits

        val newSelection = if (formatted != value.text) {
            TextRange(formatted.length)
        } else {
            value.selection
        }

        _uiState.update { 
            it.copy(
                phoneNumber = formatted,
                phoneTextFieldValue = value.copy(text = formatted, selection = newSelection)
            ) 
        }
    }

    fun onCustomerAddressChange(address: String) {
        _uiState.update { it.copy(customerAddress = address) }
    }

    fun onDeliveryMethodChange(method: DeliveryMethod) {
        _uiState.update { it.copy(deliveryMethod = method) }
    }

    fun toggleService(serviceId: String) {
        _uiState.update { state ->
            val isAlreadySelected = state.selectedServiceIds.contains(serviceId)
            val newIds = if (isAlreadySelected) {
                state.selectedServiceIds - serviceId
            } else {
                state.selectedServiceIds + serviceId
            }
            state.copy(selectedServiceIds = newIds)
        }
        updateSelectedItems()
    }

    fun onWeightChange(weight: String) {
        _uiState.update { it.copy(weight = weight) }
        updateSelectedItems()
    }

    private fun updateSelectedItems() {
        _uiState.update { state ->
            val weightDouble = state.weight.toDoubleOrNull() ?: 0.0
            val newItems = state.availableServices
                .filter { state.selectedServiceIds.contains(it.serviceId) }
                .map { service ->
                    val totalPrice = when {
                        service.type == ServiceType.PER_KG -> weightDouble * service.pricePerUnit
                        else -> service.pricePerUnit
                    }

                    ServiceItem(
                        name = service.name,
                        quantity = weightDouble,
                        unit = service.unit,
                        pricePerUnit = service.pricePerUnit,
                        totalPrice = totalPrice,
                        type = service.type
                    )
                }
            state.copy(selectedItems = newItems)
        }
    }

    fun saveOrder() {
        val currentState = _uiState.value
        if (currentState.customerName.isBlank() || currentState.selectedItems.isEmpty()) {
            val errorMsg = when {
                currentState.customerName.isBlank() -> "Customer name is required"
                currentState.selectedItems.isEmpty() -> "Select at least one service item"
                else -> "Please fill in all details"
            }
            _uiState.update { it.copy(error = errorMsg) }
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

                val dbPhoneNumber = currentState.phoneNumber.replace("+", "").ifBlank { "" }
                val orderId = IdGeneratorUtils.generateOrderId()

                val order = Order(
                    orderId = orderId,
                    shopId = shopId,
                    customerId = IdGeneratorUtils.generateCustomerId(),
                    customerName = currentState.customerName,
                    customerPhone = dbPhoneNumber,
                    customerAddress = currentState.customerAddress,
                    orderNumber = "FG-${System.currentTimeMillis().toString().takeLast(4)}",
                    items = currentState.selectedItems,
                    totalAmount = currentState.totalAmount,
                    deliveryFee = if (currentState.deliveryMethod == DeliveryMethod.DELIVERY) currentState.deliveryFee.toDoubleOrNull() ?: 0.0 else 0.0,
                    paidAmount = 0.0,
                    status = if (currentState.assignedMachine != null) OrderStatus.QUEUED else OrderStatus.PENDING,
                    deliveryMethod = currentState.deliveryMethod,
                    paymentStatus = PaymentStatus.PENDING,
                    intakePhotos = emptyList(),
                    machineId = currentState.assignedMachine?.machineId,
                    staffId = staffId,
                    staffName = staffName,
                    selectedAddOns = currentState.selectedAddOns.map { 
                        OrderAddOnSelection(orderId, it.id, it.price)
                    },
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                upsertOrderUseCase(order)
                
                // Persist machine assignment if any
                currentState.assignedMachine?.let { machine ->
                    assignMachineToOrderUseCase(machine.machineId, orderId)
                    updateMachineStatusUseCase(machine.machineId, MachineStatus.QUEUED.name)
                }
                
                // Persist customer info
                upsertCustomerUseCase(
                    Customer(
                        customerId = currentState.suggestedCustomers.find { it.name == currentState.customerName }?.customerId 
                            ?: IdGeneratorUtils.generateUniqueId("cust"),
                        name = currentState.customerName,
                        phone = dbPhoneNumber,
                        address = currentState.customerAddress
                    )
                )

                // Send Confirmation SMS
                if (order.customerPhone.isNotBlank()) {
                    val statusText = if (order.status == OrderStatus.QUEUED) "QUEUED" else "PENDING (Waiting for Machine)"
                    val message = "FoldGo ${order.orderNumber}\nAmt: P${order.totalAmount}\nStatus: $statusText\nWe will text you once your laundry is ready."
                    sendSmsUseCase(order.customerPhone, message, order.orderId)
                }

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
