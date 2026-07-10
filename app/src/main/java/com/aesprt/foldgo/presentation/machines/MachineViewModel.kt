package com.aesprt.foldgo.presentation.machines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aesprt.foldgo.domain.model.Machine
import com.aesprt.foldgo.domain.model.Order
import com.aesprt.foldgo.domain.model.OrderStatus
import com.aesprt.foldgo.domain.repository.MachineRepository
import com.aesprt.foldgo.domain.repository.OrderRepository
import com.aesprt.foldgo.data.local.PreferenceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

data class MachineUiState(
    val machines: List<Machine> = emptyList(),
    val activeOrders: List<Order> = emptyList(),
    val filteredType: String? = null,
    val isLoading: Boolean = false
)

class MachineViewModel(
    private val repository: MachineRepository,
    private val orderRepository: OrderRepository,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _filteredType = MutableStateFlow<String?>(null)
    
    val uiState: StateFlow<MachineUiState> = combine(
        repository.getAllMachines(),
        orderRepository.getAllOrders(),
        _filteredType
    ) { machines, orders, filter ->
        MachineUiState(
            machines = if (filter == null) machines else machines.filter { it.type == filter },
            activeOrders = orders.filter { 
                it.status == OrderStatus.INTAKE || 
                it.status == OrderStatus.WASHING || 
                it.status == OrderStatus.WASHED ||
                it.status == OrderStatus.DRYING ||
                it.status == OrderStatus.DRIED
            },
            filteredType = filter,
            isLoading = false
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = MachineUiState(isLoading = true)
        )

    fun onFilterTypeChanged(type: String?) {
        _filteredType.value = type
    }

    fun addMachine(name: String, type: String, capacity: Double) {
        viewModelScope.launch {
            val shopId = preferenceManager.currentShopId.first() ?: "default_shop"
            val newMachine = Machine(
                machineId = UUID.randomUUID().toString(),
                shopId = shopId,
                name = name,
                type = type,
                capacityKg = capacity,
                status = "IDLE",
                lastMaintenanceDate = System.currentTimeMillis()
            )
            repository.upsertMachine(newMachine)
        }
    }

    fun updateStatus(machineId: String, status: String) {
        viewModelScope.launch {
            repository.updateMachineStatus(machineId, status)
        }
    }

    fun startCycle(machineId: String, durationMinutes: Int, orderId: String? = null) {
        viewModelScope.launch {
            repository.startMachineCycle(machineId, durationMinutes)
            orderId?.let { id ->
                val order = orderRepository.getOrderById(id).first()
                val machine = repository.getAllMachines().first().find { it.machineId == machineId }
                if (order != null && machine != null) {
                    val newStatus = if (machine.type == "WASHER") OrderStatus.WASHING else OrderStatus.DRYING
                    orderRepository.upsertOrder(order.copy(
                        status = newStatus,
                        machineId = machineId,
                        updatedAt = System.currentTimeMillis()
                    ))
                }
            }
        }
    }

    fun finishCycle(machineId: String) {
        viewModelScope.launch {
            repository.finishMachineCycle(machineId)
            val orders = orderRepository.getAllOrders().first()
            val associatedOrder = orders.find { it.machineId == machineId && (it.status == OrderStatus.WASHING || it.status == OrderStatus.DRYING) }
            associatedOrder?.let { order ->
                val nextStatus = if (order.status == OrderStatus.WASHING) OrderStatus.WASHED else OrderStatus.DRIED
                orderRepository.upsertOrder(order.copy(
                    status = nextStatus,
                    machineId = null,
                    updatedAt = System.currentTimeMillis()
                ))
            }
        }
    }
}
