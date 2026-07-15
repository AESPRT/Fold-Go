package com.aesprt.foldgo.presentation.machines

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aesprt.foldgo.core.util.IdGeneratorUtils
import com.aesprt.foldgo.data.local.PreferenceManager
import com.aesprt.foldgo.domain.model.*
import com.aesprt.foldgo.domain.model.enums.*
import com.aesprt.foldgo.domain.usecase.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class MachineUiState(
    val machines: List<Machine> = emptyList(),
    val categories: List<MachineCategory> = emptyList(),
    val activeOrders: List<OrderWithBatches> = emptyList(),
    val selectedMachine: Machine? = null,
    val selectedOrder: Order? = null,
    val isLoading: Boolean = false
)

data class OrderWithBatches(
    val order: Order,
    val batches: List<OrderBatch>? = null
)

class MachineViewModel(
    private val getMachinesUseCase: GetMachinesUseCase,
    private val getMachineCategoriesUseCase: GetMachineCategoriesUseCase,
    getAllOrdersUseCase: GetAllOrdersUseCase,
    private val upsertOrderUseCase: UpsertOrderUseCase,
    private val getOrderByIdUseCase: GetOrderByIdUseCase,
    private val startMachineCycleUseCase: StartMachineCycleUseCase,
    private val finishMachineCycleUseCase: FinishMachineCycleUseCase,
    private val updateMachineStatusUseCase: UpdateMachineStatusUseCase,
    private val getBatchesByOrderIdUseCase: GetBatchesByOrderIdUseCase,
    private val upsertOrderBatchUseCase: UpsertOrderBatchUseCase,
    private val addMachineUseCase: AddMachineUseCase,
    private val addMachineCategoryUseCase: AddMachineCategoryUseCase,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _filteredStatus = MutableStateFlow<MachineStatus?>(null)
    private val _selectedMachineId = MutableStateFlow<String?>(null)

    val uiState: StateFlow<MachineUiState> = combine(
        getMachinesUseCase(),
        getMachineCategoriesUseCase(),
        getAllOrdersUseCase(),
        _filteredStatus,
        _selectedMachineId
    ) { machines, categories, orders, filter, selectedId ->
        val filteredMachines = if (filter == null) machines else machines.filter { it.status == filter }
        val sortedMachines = filteredMachines.sortedBy { it.name }

        val ordersWithBatches = orders.filter {
            it.status != OrderStatus.READY && it.status != OrderStatus.DELIVERED
        }.map { order ->
            val batches = getBatchesByOrderIdUseCase(order.orderId).first()
            val batchesDomain = batches.map { it }
            OrderWithBatches(order, batchesDomain)
        }

        val selectedMachine = machines.find { it.machineId == selectedId }
        val selectedOrder = orders.find { it.orderId == selectedMachine?.assignedOrderId }

        MachineUiState(
            machines = sortedMachines,
            categories = categories,
            activeOrders = ordersWithBatches,
            selectedMachine = selectedMachine,
            selectedOrder = selectedOrder,
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
                        MachineCategory("cat_washer", "Washer"),
                        MachineCategory("cat_dryer", "Dryer"),
                        MachineCategory("cat_washer_dryer", "Washer & Dryer"),
                        MachineCategory("cat_iron", "Iron"),
                        MachineCategory("cat_steamer", "Steamer")
                    )
                    defaultCategories.forEach { addMachineCategoryUseCase(it) }
                }
            }
        }
    }

    fun onFilterStatusChanged(status: MachineStatus?) {
        _filteredStatus.value = status
    }

    fun selectMachine(machineId: String?) {
        _selectedMachineId.value = machineId
    }

    fun addMachine(name: String, capacity: Double) {
        viewModelScope.launch {
            val shopId = preferenceManager.currentShopId.first() ?: return@launch
            val newMachine = Machine(
                machineId = IdGeneratorUtils.generateMachineId(),
                shopId = shopId,
                name = name,
                capacityKg = capacity,
                status = MachineStatus.IDLE,
                lastMaintenanceDate = System.currentTimeMillis()
            )
            addMachineUseCase(newMachine)
        }
    }

    fun addCategory(name: String) {
        viewModelScope.launch {
            val category = MachineCategory(
                categoryId = "cat_${System.currentTimeMillis()}",
                name = name
            )
            addMachineCategoryUseCase(category)
        }
    }

    fun updateStatus(machineId: String, status: MachineStatus) {
        viewModelScope.launch {
            updateMachineStatusUseCase(machineId, status.name)
        }
    }

    fun startCycle(machineId: String) {
        viewModelScope.launch {
            // Find machine to get assignedOrderId
            val machines = getMachinesUseCase().first()
            val machine = machines.find { it.machineId == machineId } ?: return@launch
            val orderId = machine.assignedOrderId ?: return@launch
            
            // Trigger domain logic
            startMachineCycleUseCase(machineId, orderId, 30) // Default 30 mins for now
            
            // Update machine status to WASHING as per spec (first state in sequence)
            updateMachineStatusUseCase(machineId, MachineStatus.WASHING.name)
        }
    }

    fun finishCycle(machineId: String) {
        viewModelScope.launch {
            finishMachineCycleUseCase(machineId)
        }
    }

    /**
     * Determine order status when a batch is being started.
     * Looks at the phases of all batches to determine what phase the order is in.
     */
    private fun determineBatchOrderStatus(order: Order, allBatches: List<OrderBatch>): OrderStatus {
        if (allBatches.isEmpty()) return order.status

        // Check what phases have active batches
        val hasWashingBatches = allBatches.any { it.status == BatchStatus.WASHING }
        val hasDryingBatches = allBatches.any { it.status == BatchStatus.DRYING }
        val hasFoldingBatches = allBatches.any { it.status == BatchStatus.FOLDING }

        return when {
            hasWashingBatches -> OrderStatus.WASHING
            hasDryingBatches -> OrderStatus.DRYING
            hasFoldingBatches -> OrderStatus.FOLDING
            else -> order.status
        }
    }
}