package com.aesprt.foldgo.presentation.machines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aesprt.foldgo.core.notification.NotificationHelper
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
    val selectedMachine: Machine? = null,
    val selectedOrder: Order? = null,
    val isLoading: Boolean = false
)

class MachineViewModel(
    private val getMachinesUseCase: GetMachinesUseCase,
    private val getMachineCategoriesUseCase: GetMachineCategoriesUseCase,
    private val getAllOrdersUseCase: GetAllOrdersUseCase,
    private val upsertOrderUseCase: UpsertOrderUseCase,
    private val updateMachineStatusUseCase: UpdateMachineStatusUseCase,
    private val addMachineUseCase: AddMachineUseCase,
    private val addMachineCategoryUseCase: AddMachineCategoryUseCase,
    private val notificationHelper: NotificationHelper,
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

        val selectedMachine = machines.find { it.machineId == selectedId }
        val selectedOrder = orders.find { it.orderId == selectedMachine?.assignedOrderId }

        MachineUiState(
            machines = sortedMachines,
            categories = categories,
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

    fun updateStatus(machineId: String, status: MachineStatus) {
        viewModelScope.launch {
            val machine = getMachinesUseCase().first().find { it.machineId == machineId } ?: return@launch
            val orderId = machine.assignedOrderId
            
            updateMachineStatusUseCase(machineId, status.name)

            if (orderId != null) {
                val order = getAllOrdersUseCase().first().find { it.orderId == orderId }
                if (order != null) {
                    val newOrderStatus = when (status) {
                        MachineStatus.WASHING -> OrderStatus.WASHING
                        MachineStatus.DRYING -> OrderStatus.DRYING
                        MachineStatus.IRONING -> OrderStatus.IRONING
                        MachineStatus.FOLDING -> OrderStatus.FOLDING
                        MachineStatus.READY -> OrderStatus.READY
                        else -> order.status
                    }
                    
                    if (newOrderStatus != order.status) {
                        upsertOrderUseCase(order.copy(
                            status = newOrderStatus,
                            updatedAt = System.currentTimeMillis()
                        ))
                        
                        // Show notification for status completion/transition
                        val statusLabel = when (status) {
                            MachineStatus.WASHING -> "wash cycle"
                            MachineStatus.DRYING -> "dry cycle"
                            MachineStatus.IRONING -> "iron cycle"
                            MachineStatus.FOLDING -> "folding"
                            MachineStatus.READY -> "processing"
                            else -> "cycle"
                        }
                        
                        notificationHelper.showBatchCompletionNotification(
                            machineName = machine.name,
                            batchWeight = order.items.sumOf { it.quantity },
                            batchStatus = statusLabel,
                            orderNumber = order.orderNumber,
                            batchId = orderId, // Using orderId since no batches
                            orderId = orderId
                        )
                    }
                }
            }
        }
    }
}
