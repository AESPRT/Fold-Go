package com.aesprt.foldgo.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aesprt.foldgo.domain.model.Machine
import com.aesprt.foldgo.domain.model.Order
import com.aesprt.foldgo.domain.model.OrderStatus
import com.aesprt.foldgo.domain.repository.MachineRepository
import com.aesprt.foldgo.domain.repository.OrderRepository
import com.aesprt.foldgo.domain.usecase.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class OrderWithMachine(
    val order: Order,
    val machine: Machine?
)

data class DashboardUiState(
    val orders: List<OrderWithMachine> = emptyList(),
    val totalIntakeAmount: Double = 0.0,
    val totalSalesAmount: Double = 0.0,
    val activeOrdersCount: Int = 0,
    val isLoading: Boolean = false
)

class DashboardViewModel(
    private val getAllOrdersUseCase: GetAllOrdersUseCase,
    private val getMachinesUseCase: GetMachinesUseCase,
    private val finishMachineCycleUseCase: FinishMachineCycleUseCase,
    private val upsertOrderUseCase: UpsertOrderUseCase
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = combine(
        getAllOrdersUseCase(),
        getMachinesUseCase()
    ) { orders, machines ->
        val activeOrders = orders.filter { it.status != OrderStatus.DELIVERED }
        val ordersWithMachines = activeOrders.map { order ->
            OrderWithMachine(order, machines.find { it.machineId == order.machineId })
        }
        DashboardUiState(
            orders = ordersWithMachines,
            totalIntakeAmount = orders.sumOf { it.totalAmount },
            totalSalesAmount = orders.sumOf { it.paidAmount },
            activeOrdersCount = activeOrders.size,
            isLoading = false
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DashboardUiState(isLoading = true)
        )

    fun autoFinishCycle(machineId: String) {
        viewModelScope.launch {
            finishMachineCycleUseCase(machineId)
            val orders = getAllOrdersUseCase().first()
            val associatedOrder = orders.find { it.machineId == machineId && (it.status == OrderStatus.WASHING || it.status == OrderStatus.DRYING) }
            associatedOrder?.let { order ->
                val nextStatus = if (order.status == OrderStatus.WASHING) OrderStatus.WASHED else OrderStatus.DRIED
                upsertOrderUseCase(order.copy(
                    status = nextStatus,
                    machineId = null,
                    updatedAt = System.currentTimeMillis()
                ))
            }
        }
    }
}
