package com.aesprt.foldgo.presentation.machines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aesprt.foldgo.domain.model.Machine
import com.aesprt.foldgo.domain.model.MachineStatus
import com.aesprt.foldgo.domain.model.MachineType
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
    val availableTypes: List<MachineType> = emptyList(),
    val activeOrders: List<Order> = emptyList(),
    val filteredType: MachineType? = null,
    val isLoading: Boolean = false
)

class MachineViewModel(
    private val repository: MachineRepository,
    private val orderRepository: OrderRepository,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _filteredType = MutableStateFlow<MachineType?>(null)
    
    val uiState: StateFlow<MachineUiState> = combine(
        repository.getAllMachines(),
        orderRepository.getAllOrders(),
        _filteredType
    ) { machines, orders, filter ->
        val sortedMachines = if (filter == null) {
            machines.sortedBy { it.type.name }
        } else {
            machines.filter { it.type == filter }
        }

        MachineUiState(
            machines = sortedMachines,
            availableTypes = machines.map { it.type }.distinct().sortedBy { it.name },
            activeOrders = orders.filter { 
                it.status == OrderStatus.INTAKE || 
                it.status == OrderStatus.WASHING || 
                it.status == OrderStatus.WASHED ||
                it.status == OrderStatus.DRYING ||
                it.status == OrderStatus.DRIED ||
                it.status == OrderStatus.IRONING ||
                it.status == OrderStatus.IRONED
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

    fun onFilterTypeChanged(type: MachineType?) {
        _filteredType.value = type
    }

    fun addMachine(name: String, type: MachineType, capacity: Double) {
        viewModelScope.launch {
            val shopId = preferenceManager.currentShopId.first() ?: "default_shop"
            val newMachine = Machine(
                machineId = UUID.randomUUID().toString(),
                shopId = shopId,
                name = name,
                type = type,
                capacityKg = capacity,
                status = MachineStatus.IDLE,
                lastMaintenanceDate = System.currentTimeMillis()
            )
            repository.upsertMachine(newMachine)
        }
    }

    fun updateStatus(machineId: String, status: MachineStatus) {
        viewModelScope.launch {
            repository.updateMachineStatus(machineId, status.name)
        }
    }

    fun startCycle(machineId: String, durationMinutes: Int, orderId: String? = null) {
        viewModelScope.launch {
            repository.startMachineCycle(machineId, durationMinutes)
            orderId?.let { id ->
                val order = orderRepository.getOrderById(id).first()
                val machine = repository.getAllMachines().first().find { it.machineId == machineId }
                if (order != null && machine != null) {
                    val newStatus = when (machine.type) {
                        MachineType.WASHER -> OrderStatus.WASHING
                        MachineType.DRYER -> OrderStatus.DRYING
                        MachineType.IRON -> OrderStatus.IRONING
                        else -> order.status
                    }
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
            val associatedOrder = orders.find { 
                it.machineId == machineId && 
                (it.status == OrderStatus.WASHING || it.status == OrderStatus.DRYING || it.status == OrderStatus.IRONING) 
            }
            associatedOrder?.let { order ->
                val nextStatus = when (order.status) {
                    OrderStatus.WASHING -> OrderStatus.WASHED
                    OrderStatus.DRYING -> OrderStatus.DRIED
                    OrderStatus.IRONING -> OrderStatus.IRONED
                    else -> order.status
                }
                orderRepository.upsertOrder(order.copy(
                    status = nextStatus,
                    machineId = null,
                    updatedAt = System.currentTimeMillis()
                ))
            }
        }
    }
}
