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
    val availableTypes: List<MachineType> = emptyList(),
    val activeOrders: List<OrderWithBatches> = emptyList(),
    val filteredType: MachineType? = null,
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

        val ordersWithBatches = orders.filter {
            it.status != OrderStatus.READY && it.status != OrderStatus.DELIVERED
        }.map { order ->
            val batches = getBatchesByOrderIdUseCase(order.orderId).first()
            OrderWithBatches(order, batches)
        }

        MachineUiState(
            machines = sortedMachines,
            categories = categories,
            availableTypes = machines.map { it.type }.distinct().sortedBy { it.name },
            activeOrders = ordersWithBatches,
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

    fun startCycle(machineId: String, durationMinutes: Int, orderId: String? = null, assignedWeight: Double? = null) {
        viewModelScope.launch {
            Log.e("adriel-testing", "machineId: $machineId")
            Log.e("adriel-testing", "orderId: $orderId")
            orderId?.let { id ->
                startMachineCycleUseCase(machineId, id,durationMinutes)
                val order = getOrderByIdUseCase(id).firstOrNull()
                val machine = getMachinesUseCase().first().find { it.machineId == machineId }

                if (order != null && machine != null) {
                    Log.e("adriel-testing", "machine: Name: ${machine.name}, Id: ${machine.machineId}")
                    val allBatches = getBatchesByOrderIdUseCase(id).first()

                    val processedWeight = allBatches.sumOf { it.weightKg }
                    val remainingWeight = (order.items.sumOf { it.quantity } - processedWeight).coerceAtLeast(0.0)

                    val weight = assignedWeight ?: remainingWeight

                    // 1. Determine the status for this new batch/segment
                    val batchStatus = when (machine.type) {
                        MachineType.WASHER_DRYER -> {
                            // For washer-dryer, start with washing if NOT ALL weight has been washed+dried yet
                            val washedAndDriedWeight = allBatches.filter {
                                it.status in listOf(
                                    OrderStatus.WASHED_AND_DRIED,
                                    OrderStatus.DRIED,
                                    OrderStatus.WASHED,
                                    OrderStatus.IRONING,
                                    OrderStatus.IRONED,
                                    OrderStatus.FOLDING,
                                    OrderStatus.READY
                                )
                            }.sumOf { it.weightKg }

                            val totalWeight = order.items.sumOf { it.quantity }
                            if (washedAndDriedWeight < totalWeight) OrderStatus.WASHING_AND_DRYING else OrderStatus.FOLDING
                        }
                        MachineType.WASHER -> OrderStatus.WASHING
                        MachineType.DRYER -> OrderStatus.DRYING
                        MachineType.IRON -> OrderStatus.IRONING
                        else -> OrderStatus.FOLDING
                    }

                    // 2. ONLY create a Batch object if we are splitting the order
                    // (remaining weight > capacity) OR if we already have batches for this order
                    if (remainingWeight > (machine.capacityKg + 0.01) || allBatches.isNotEmpty()) {
                        val batch = OrderBatch(
                            batchId = IdGeneratorUtils.generateUniqueId("batch"),
                            orderId = id,
                            machineId = machineId,
                            weightKg = weight,
                            status = batchStatus,
                            startTime = System.currentTimeMillis(),
                            endTime = System.currentTimeMillis() + (durationMinutes * 60000)
                        )
                        upsertOrderBatchUseCase(batch)
                    }

                    // 3. Update order status based on what we just started
                    val updatedBatches = if (remainingWeight > (machine.capacityKg + 0.01) || allBatches.isNotEmpty()) {
                        allBatches + listOf(OrderBatch(
                            batchId = "temp", orderId = id, machineId = machineId, weightKg = weight,
                            status = batchStatus, startTime = System.currentTimeMillis()
                        ))
                    } else emptyList()

                    val newStatus = if (updatedBatches.isNotEmpty()) {
                        determineBatchOrderStatus(order, updatedBatches)
                    } else {
                        batchStatus // If no split batches, order status IS the batch status
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
        }
    }

    /**
     * Determine order status when a batch is being started.
     * Looks at the phases of all batches to determine what phase the order is in.
     */
    private fun determineBatchOrderStatus(order: Order, allBatches: List<OrderBatch>): OrderStatus {
        if (allBatches.isEmpty()) return order.status

        // Check what phases have active batches
        val hasWashingDryingBatches = allBatches.any { it.status == OrderStatus.WASHING_AND_DRYING }
        val hasWashingBatches = allBatches.any { it.status == OrderStatus.WASHING }
        val hasDryingBatches = allBatches.any { it.status == OrderStatus.DRYING }
        val hasIroningBatches = allBatches.any { it.status == OrderStatus.IRONING }

        return when {
            hasWashingDryingBatches -> OrderStatus.WASHING_AND_DRYING
            hasWashingBatches -> OrderStatus.WASHING
            hasDryingBatches -> OrderStatus.DRYING
            hasIroningBatches -> OrderStatus.IRONING
            else -> order.status
        }
    }
}