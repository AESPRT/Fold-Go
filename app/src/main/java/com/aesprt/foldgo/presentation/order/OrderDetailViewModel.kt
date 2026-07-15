package com.aesprt.foldgo.presentation.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aesprt.foldgo.data.local.PreferenceManager
import com.aesprt.foldgo.domain.model.*
import com.aesprt.foldgo.domain.model.enums.*
import com.aesprt.foldgo.domain.usecase.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class OrderDetailUiState(
    val order: Order? = null,
    val machine: Machine? = null,
    val allMachines: List<Machine> = emptyList(),
    val availableAddOns: List<AddOn> = emptyList(),
    val batches: List<OrderBatch> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val showSmsPrompt: Boolean = false,
    val pendingOrder: Order? = null,
    val isSendingSms: Boolean = false
)

class OrderDetailViewModel(
    private val orderId: String,
    private val getOrderByIdUseCase: GetOrderByIdUseCase,
    private val upsertOrderUseCase: UpsertOrderUseCase,
    private val getMachinesUseCase: GetMachinesUseCase,
    private val getBatchesByOrderIdUseCase: GetBatchesByOrderIdUseCase,
    private val sendSmsUseCase: SendSmsUseCase,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrderDetailUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadOrderDetail()
        loadAddOns()
    }

    private fun loadAddOns() {
        // Placeholder for loading add-ons from repository
        _uiState.update { 
            it.copy(
                availableAddOns = listOf(
                    AddOn("ao1", "Fabric Softener", "Adds softener to wash cycle", 30.0, ServiceScope.ALL, true),
                    AddOn("ao2", "Extra Rinse", "One additional rinse cycle", 25.0, ServiceScope.WASH_ONLY, true),
                    AddOn("ao3", "Express Service", "Ready in under 2 hours", 100.0, ServiceScope.ALL, true),
                    AddOn("ao4", "Stain Treatment", "Pre-treat visible stains", 50.0, ServiceScope.ALL, true)
                )
            )
        }
    }

    private fun loadOrderDetail() {
        viewModelScope.launch {
            combine(
                getOrderByIdUseCase(orderId),
                getMachinesUseCase(),
                getBatchesByOrderIdUseCase(orderId)
            ) { order, machines, batches ->
                val machine = machines.find { it.machineId == order?.machineId || it.assignedOrderId == order?.orderId }
                Triple(order, machine, batches)
            }.collect { (order, machine, batches) ->
                _uiState.update { 
                    it.copy(
                        order = order,
                        machine = machine,
                        batches = batches,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun updateOrderStatus(status: OrderStatus) {
        val currentOrder = uiState.value.order ?: return
        viewModelScope.launch {
            upsertOrderUseCase(currentOrder.copy(
                status = status,
                updatedAt = System.currentTimeMillis()
            ))
        }
    }

    fun updateOrderPaymentAndDelivery(method: DeliveryMethod, amountPaid: Double) {
        val currentOrder = uiState.value.order ?: return
        viewModelScope.launch {
            val deliveryFee = if (method == DeliveryMethod.DELIVERY) 50.0 else 0.0
            val finalTotal = currentOrder.totalAmount + deliveryFee
            
            val change = (amountPaid - finalTotal).coerceAtLeast(0.0)
            val actualPaymentTowardsOrder = if (amountPaid >= finalTotal) finalTotal else amountPaid
            
            val totalPaidSoFar = currentOrder.paidAmount + actualPaymentTowardsOrder
            val paymentStatus = if (totalPaidSoFar >= finalTotal) PaymentStatus.PAID else PaymentStatus.PARTIAL
            
            val updatedOrder = currentOrder.copy(
                status = OrderStatus.READY,
                deliveryMethod = method,
                paidAmount = totalPaidSoFar,
                changeDue = change,
                paymentStatus = paymentStatus,
                updatedAt = System.currentTimeMillis()
            )

            val isSmsEnabled = preferenceManager.isSmsEnabled.first()
            val credits = preferenceManager.smsCredits.first()

            if (isSmsEnabled && credits > 0) {
                _uiState.update { it.copy(pendingOrder = updatedOrder, showSmsPrompt = true) }
            } else {
                _uiState.update { it.copy(pendingOrder = updatedOrder) }
                completePendingUpdate()
            }
        }
    }

    fun markAsDelivered() {
        val currentOrder = uiState.value.order ?: return
        viewModelScope.launch {
            val updatedOrder = currentOrder.copy(
                status = OrderStatus.DELIVERED,
                updatedAt = System.currentTimeMillis()
            )
            upsertOrderUseCase(updatedOrder)
        }
    }

    fun sendSmsAndComplete(message: String) {
        val order = uiState.value.pendingOrder ?: uiState.value.order ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSendingSms = true) }
            val result = sendSmsUseCase(order.customerPhone, message)
            if (result.isSuccess) {
                preferenceManager.deductSmsCredit()
                completePendingUpdate()
            } else {
                _uiState.update { it.copy(isSendingSms = false, error = "Failed to send SMS. Please check internet connection.") }
            }
        }
    }

    fun completePendingUpdate() {
        val orderToSave = uiState.value.pendingOrder ?: return
        viewModelScope.launch {
            upsertOrderUseCase(orderToSave)
            _uiState.update { it.copy(pendingOrder = null, showSmsPrompt = false, isSendingSms = false) }
        }
    }

    fun dismissSmsPrompt() {
        if (uiState.value.pendingOrder != null) {
            completePendingUpdate()
        } else {
            _uiState.update { it.copy(showSmsPrompt = false) }
        }
    }
}
