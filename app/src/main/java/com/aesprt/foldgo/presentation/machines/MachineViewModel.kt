package com.aesprt.foldgo.presentation.machines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aesprt.foldgo.core.util.IdGeneratorUtils
import com.aesprt.foldgo.domain.model.Machine
import com.aesprt.foldgo.domain.model.MachineCategory
import com.aesprt.foldgo.domain.model.MachineStatus
import com.aesprt.foldgo.domain.model.MachineType
import com.aesprt.foldgo.domain.model.Order
import com.aesprt.foldgo.domain.model.OrderStatus
import com.aesprt.foldgo.domain.model.ServiceType
import com.aesprt.foldgo.domain.usecase.*
import com.aesprt.foldgo.data.local.PreferenceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MachineUiState(
    val machines: List<Machine> = emptyList(),
    val categories: List<MachineCategory> = emptyList(),
    val availableTypes: List<MachineType> = emptyList(),
    val activeOrders: List<Order> = emptyList(),
    val filteredType: MachineType? = null,
    val isLoading: Boolean = false
)

class MachineViewModel(
    private val getMachinesUseCase: GetMachinesUseCase,
    private val getMachineCategoriesUseCase: GetMachineCategoriesUseCase,
    private val addMachineUseCase: AddMachineUseCase,
    private val addMachineCategoryUseCase: AddMachineCategoryUseCase,
    private val getAllOrdersUseCase: GetAllOrdersUseCase,
    private val upsertOrderUseCase: UpsertOrderUseCase,
    private val getOrderByIdUseCase: GetOrderByIdUseCase,
    private val startMachineCycleUseCase: StartMachineCycleUseCase,
    private val finishMachineCycleUseCase: FinishMachineCycleUseCase,
    private val updateMachineStatusUseCase: UpdateMachineStatusUseCase,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _filteredType = MutableStateFlow<MachineType?>(null)
    
    val uiState: StateFlow<MachineUiState> = combine(
        getMachinesUseCase(),
        getMachineCategoriesUseCase(),
        getAllOrdersUseCase(),
        _filteredType
    ) { machines, categories, orders, filter ->
        val sortedMachines = if (filter == null) {
            machines.sortedBy { it.type.name }
        } else {
            machines.filter { it.type == filter }
        }

        MachineUiState(
            machines = sortedMachines,
            categories = categories,
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

    init {
        seedCategories()
    }

    private fun seedCategories() {
        viewModelScope.launch {
            getMachineCategoriesUseCase().first().let { categories ->
                if (categories.isEmpty()) {
                    val defaultCategories = listOf(
                        MachineCategory("cat_washer", "Washer", MachineType.WASHER),
                        MachineCategory("cat_dryer", "Dryer", MachineType.DRYER),
                        MachineCategory("cat_washer_dryer", "Washer & Dryer", MachineType.WASHER_DRYER),
                        MachineCategory("cat_iron", "Iron", MachineType.IRON),
                        MachineCategory("cat_steamer", "Steamer", MachineType.STEAMER)
                    )
                    defaultCategories.forEach { addMachineCategoryUseCase(it) }
                }
            }
        }
    }

    fun onFilterTypeChanged(type: MachineType?) {
        _filteredType.value = type
    }

    fun addMachine(name: String, type: MachineType, capacity: Double) {
        viewModelScope.launch {
            val shopId = preferenceManager.currentShopId.first() ?: return@launch
            val newMachine = Machine(
                machineId = IdGeneratorUtils.generateMachineId(),
                shopId = shopId,
                name = name,
                type = type,
                capacityKg = capacity,
                status = MachineStatus.IDLE,
                lastMaintenanceDate = System.currentTimeMillis()
            )
            addMachineUseCase(newMachine)
        }
    }

    fun addCategory(name: String, type: MachineType) {
        viewModelScope.launch {
            val category = MachineCategory(
                categoryId = "cat_${System.currentTimeMillis()}",
                name = name,
                type = type
            )
            addMachineCategoryUseCase(category)
        }
    }

    fun updateStatus(machineId: String, status: MachineStatus) {
        viewModelScope.launch {
            updateMachineStatusUseCase(machineId, status.name)
        }
    }

    fun startCycle(machineId: String, durationMinutes: Int, orderId: String? = null) {
        viewModelScope.launch {
            startMachineCycleUseCase(machineId, durationMinutes)
            orderId?.let { id ->
                val order = getOrderByIdUseCase(id).first()
                val machine = getMachinesUseCase().first().find { it.machineId == machineId }
                if (order != null && machine != null) {
                    val newStatus = when (machine.type) {
                        MachineType.WASHER -> OrderStatus.WASHING
                        MachineType.DRYER -> OrderStatus.DRYING
                        MachineType.WASHER_DRYER -> {
                            if (order.status == OrderStatus.INTAKE) OrderStatus.WASHING
                            else if (order.status == OrderStatus.WASHED) OrderStatus.DRYING
                            else order.status
                        }
                        MachineType.IRON -> OrderStatus.IRONING
                        MachineType.STEAMER -> OrderStatus.IRONING
                        else -> order.status
                    }
                    upsertOrderUseCase(order.copy(
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
            finishMachineCycleUseCase(machineId)
            val machines = getMachinesUseCase().first()
            val machine = machines.find { it.machineId == machineId }
            val orders = getAllOrdersUseCase().first()
            val associatedOrder = orders.find { 
                it.machineId == machineId && 
                (it.status == OrderStatus.WASHING || it.status == OrderStatus.DRYING || it.status == OrderStatus.IRONING) 
            }
            associatedOrder?.let { order ->
                val hasDryItems = order.items.any { it.type == ServiceType.DRY || it.type == ServiceType.WASH_DRY }
                
                val nextStatus = when {
                    machine?.type == MachineType.WASHER_DRYER -> OrderStatus.DRIED
                    order.status == OrderStatus.WASHING -> {
                        if (hasDryItems) OrderStatus.WASHED else OrderStatus.DRIED
                    }
                    order.status == OrderStatus.DRYING -> OrderStatus.DRIED
                    order.status == OrderStatus.IRONING -> OrderStatus.IRONED
                    else -> order.status
                }
                upsertOrderUseCase(order.copy(
                    status = nextStatus,
                    machineId = null,
                    updatedAt = System.currentTimeMillis()
                ))
            }
        }
    }
}
