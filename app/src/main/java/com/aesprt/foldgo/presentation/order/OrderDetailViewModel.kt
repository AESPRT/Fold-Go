package com.aesprt.foldgo.presentation.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aesprt.foldgo.domain.model.DeliveryMethod
import com.aesprt.foldgo.domain.model.Machine
import com.aesprt.foldgo.domain.model.Order
import com.aesprt.foldgo.domain.model.OrderStatus
import com.aesprt.foldgo.domain.model.PaymentStatus
import com.aesprt.foldgo.domain.repository.MachineRepository
import com.aesprt.foldgo.domain.repository.OrderRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class OrderDetailUiState(
    val order: Order? = null,
    val machine: Machine? = null,
    val allMachines: List<Machine> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class OrderDetailViewModel(
    private val orderId: String,
    private val orderRepository: OrderRepository,
    private val machineRepository: MachineRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrderDetailUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadOrderDetail()
    }

    private fun loadOrderDetail() {
        viewModelScope.launch {
            combine(
                orderRepository.getOrderById(orderId),
                machineRepository.getAllMachines()
            ) { order, machines ->
                val machine = machines.find { it.machineId == order?.machineId }
                OrderDetailUiState(
                    order = order,
                    machine = machine,
                    allMachines = machines,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun updateOrderStatus(status: OrderStatus) {
        val currentOrder = uiState.value.order ?: return
        viewModelScope.launch {
            orderRepository.upsertOrder(currentOrder.copy(
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
            
            // tendered amount is amountPaid
            val change = (amountPaid - finalTotal).coerceAtLeast(0.0)
            val actualPaymentTowardsOrder = if (amountPaid >= finalTotal) finalTotal else amountPaid
            
            val totalPaidSoFar = currentOrder.paidAmount + actualPaymentTowardsOrder
            val paymentStatus = if (totalPaidSoFar >= finalTotal) PaymentStatus.PAID else PaymentStatus.PARTIAL
            
            orderRepository.upsertOrder(currentOrder.copy(
                status = OrderStatus.READY,
                deliveryMethod = method,
                paidAmount = totalPaidSoFar,
                changeDue = change,
                paymentStatus = paymentStatus,
                updatedAt = System.currentTimeMillis()
            ))
        }
    }

    fun markAsDelivered() {
        val currentOrder = uiState.value.order ?: return
        viewModelScope.launch {
            orderRepository.upsertOrder(currentOrder.copy(
                status = OrderStatus.DELIVERED,
                updatedAt = System.currentTimeMillis()
            ))
        }
    }
}
