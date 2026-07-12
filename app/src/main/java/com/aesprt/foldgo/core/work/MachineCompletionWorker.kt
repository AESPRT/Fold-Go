package com.aesprt.foldgo.core.work

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.aesprt.foldgo.core.notification.NotificationHelper
import com.aesprt.foldgo.data.local.FoldGoDatabase
import com.aesprt.foldgo.data.local.entities.toDomain
import com.aesprt.foldgo.data.local.entities.toEntity
import com.aesprt.foldgo.domain.model.Order
import com.aesprt.foldgo.domain.model.OrderBatch
import com.aesprt.foldgo.domain.model.enums.MachineType
import com.aesprt.foldgo.domain.model.enums.OrderStatus
import com.aesprt.foldgo.domain.model.enums.ServiceType
import com.aesprt.foldgo.presentation.machines.OrderWithBatches
import kotlinx.coroutines.flow.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.collections.map

class MachineCompletionWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams), KoinComponent {

    private val database: FoldGoDatabase by inject()
    private val notificationHelper: NotificationHelper by inject()

    override suspend fun doWork(): Result {
        val machineId = inputData.getString("machineId") ?: return Result.failure()
        val orderIdFromInput = inputData.getString("orderId") ?: return Result.failure()

        val machine = database.machineDao.getMachineById(machineId)

        if (machine != null) {
            // Ensure machine is set to IDLE when cycle completes
            database.machineDao.finishCycle(machineId)

            // Get batch to determine next phase
            var currentProcessedStatus: OrderStatus?
            val activeBatch = database.orderBatchDao.getActiveBatchByMachineOrderId(machineId, orderIdFromInput).firstOrNull()
            val activeBatchModel = activeBatch?.toDomain()

            val orders = database.orderDao.getAllOrders().first()
            val ordersWithBatches = orders.filter {
                it.status != OrderStatus.READY && it.status != OrderStatus.DELIVERED
            }.map { order ->
                val batches = database.orderBatchDao.getBatchesByOrderId(order.orderId).first()
                val batchesDomain = batches.map {
                    it.toDomain()
                }
                OrderWithBatches(order.toDomain(), batchesDomain)
            }

            val targetOrderId = activeBatchModel?.orderId ?: run {
                // If no batch, maybe the order itself is assigned to the machine (no split)
                ordersWithBatches.find { it.order.machineId == machineId }?.order?.orderId
            } ?: orderIdFromInput

            val order = database.orderDao.getOrderById(targetOrderId).firstOrNull()?.toDomain()

            if (order != null) {
                var latestBatches: List<OrderBatch> = emptyList()
                var batchId: String?

                if (activeBatchModel != null) {
                    batchId = activeBatchModel.batchId
                    Log.e("adriel-testing", "Order: ${order.orderNumber}")
                    Log.e("adriel-testing", "machine: ${machine.machineId}")
                    Log.e("adriel-testing", "activeBatch: ${activeBatch.status}")

                    // Handle Split Batch Completion
                    val nextBatchStatus = when {
                        activeBatchModel.status == OrderStatus.WASHING_AND_DRYING -> OrderStatus.WASHED_AND_DRIED
                        activeBatchModel.status == OrderStatus.WASHING -> OrderStatus.WASHED
                        activeBatchModel.status == OrderStatus.DRYING -> OrderStatus.DRIED
                        activeBatchModel.status == OrderStatus.IRONING -> OrderStatus.IRONED
                        else -> activeBatchModel.status
                    }

                    Log.e("adriel-testing", "nextBatchStatus: $nextBatchStatus")

                    val updatedBatch = activeBatchModel.copy(
                        status = nextBatchStatus,
                        machineId = null
                    )

                    database.orderBatchDao.upsertBatch(updatedBatch.toEntity())

                    val allBatches = database.orderBatchDao.getBatchesByOrderId(targetOrderId).first()
                    val allBatchesDomain = allBatches.map {
                        it.toDomain()
                    }

                    latestBatches = allBatchesDomain.map { if (it.batchId == updatedBatch.batchId) updatedBatch else it }
                    currentProcessedStatus = nextBatchStatus
                } else {
                    batchId = null
                    // Handle Single Cycle (No Batch) Completion
                    currentProcessedStatus = when (machine.type) {
                        MachineType.WASHER_DRYER -> OrderStatus.WASHED_AND_DRIED
                        MachineType.WASHER -> OrderStatus.WASHED
                        MachineType.DRYER -> OrderStatus.DRIED
                        else -> OrderStatus.IRONED
                    }
                }

                Log.e("adriel-testing", "currentProcessedStatus: $currentProcessedStatus")
                // Determine new order status
                val newOrderStatus = determineOrderStatusAfterBatchCompletion(order, latestBatches, currentProcessedStatus)
                Log.e("adriel-testing", "newOrderStatus: $newOrderStatus")

                // Only clear machineId if no other batches are currently on THIS machine for this order
                // Or if it was a single cycle, we clear it.
                val otherBatchesOnMachine = if (batchId != null) {
                    database.orderBatchDao.getBatchesByOrderId(targetOrderId).first()
                        .any { it.machineId == machineId && it.batchId != batchId }
                } else false

                val orderEntity = order.copy(
                    status = newOrderStatus,
                    machineId = if (otherBatchesOnMachine) order.machineId else null,
                    updatedAt = System.currentTimeMillis()
                ).toEntity()
                database.orderDao.upsertOrder(orderEntity)

                // Fallback if no batch info
                if (batchId == null) {
                    notificationHelper.showMachineCompletionNotification(machine.name, targetOrderId)
                    return Result.success()
                } else {
                    val nextStatus = when (currentProcessedStatus) {
                        OrderStatus.WASHED_AND_DRIED -> "wash and dry cycle"
                        OrderStatus.WASHED -> "wash cycle"
                        OrderStatus.DRIED -> "dry cycle"
                        OrderStatus.IRONED -> "iron cycle"
                        else -> "cycle"
                    }

                    notificationHelper.showBatchCompletionNotification(
                        machineName = machine.name,
                        batchWeight = activeBatchModel?.weightKg ?: 0.0,
                        batchStatus = nextStatus,
                        orderNumber = order.orderNumber,
                        batchId = batchId,
                        orderId = targetOrderId
                    )
                    return Result.success()
                }
            }
        }

        return Result.failure()
    }

    /**
     * Determine order status after a batch completes.
     * This checks if all weight for the order has completed current phase.
     */
    private fun determineOrderStatusAfterBatchCompletion(
        order: Order,
        allBatches: List<OrderBatch>,
        lastFinishedStatus: OrderStatus? = null
    ): OrderStatus {
        val totalOrderWeight = order.items.sumOf { it.quantity }.coerceAtLeast(0.1)
        val hasWashDryItems = order.items.any { it.type == ServiceType.WASH_DRY }
        val hasDryItems = order.items.any { it.type == ServiceType.DRY }
        val hasIronItems = order.items.any { it.type == ServiceType.IRON }
        val isWashOnly = order.items.any { it.type == ServiceType.WASH }

        val epsilon = 0.01

        // If no batches, we are in a single-cycle flow
        if (allBatches.isEmpty()) {
            return when (lastFinishedStatus) {
                OrderStatus.WASHED_AND_DRIED -> if (hasWashDryItems) OrderStatus.WASHED_AND_DRIED else OrderStatus.FOLDING
                OrderStatus.WASHED -> if (hasWashDryItems || isWashOnly || hasDryItems) OrderStatus.WASHED else OrderStatus.FOLDING
                OrderStatus.DRIED -> if (hasIronItems) OrderStatus.DRIED else OrderStatus.FOLDING
                OrderStatus.IRONED -> OrderStatus.FOLDING
                else -> order.status
            }
        }

        // Calculate weights at each phase for Split Batch flow
        val washedDriedWeight = allBatches.filter {
            it.status in listOf(OrderStatus.WASHED_AND_DRIED, OrderStatus.IRONING, OrderStatus.IRONED, OrderStatus.FOLDING, OrderStatus.READY)
        }.sumOf { it.weightKg }

        val washedWeight = allBatches.filter {
            it.status in listOf(OrderStatus.WASHED, OrderStatus.IRONING, OrderStatus.IRONED, OrderStatus.FOLDING, OrderStatus.READY)
        }.sumOf { it.weightKg }

        val driedWeight = allBatches.filter {
            it.status in listOf(OrderStatus.DRIED, OrderStatus.IRONING, OrderStatus.IRONED, OrderStatus.FOLDING, OrderStatus.READY)
        }.sumOf { it.weightKg }

        val ironedWeight = allBatches.filter {
            it.status in listOf(OrderStatus.IRONED, OrderStatus.FOLDING, OrderStatus.READY)
        }.sumOf { it.weightKg }

        // Determine transition to FOLDING
        val isFinalPhaseComplete = when {
            hasIronItems -> ironedWeight >= (totalOrderWeight - epsilon)
            (hasDryItems || hasWashDryItems) && lastFinishedStatus == OrderStatus.DRIED -> driedWeight >= (totalOrderWeight - epsilon)
            isWashOnly -> washedWeight >= (totalOrderWeight - epsilon)
            else -> washedDriedWeight >= (totalOrderWeight - epsilon)
        }

        Log.e("adriel-testing", "hasIronItems: $hasIronItems")
        Log.e("adriel-testing", "hasDryItems: $hasDryItems")
        Log.e("adriel-testing", "hasWashDryItems: $hasWashDryItems")
        Log.e("adriel-testing", "isWashOnly: $isWashOnly")

        Log.e("adriel-testing", "\ntotalOrderWeight: $totalOrderWeight")
        Log.e("adriel-testing", "washedDriedWeight: $washedDriedWeight")
        Log.e("adriel-testing", "washedWeight: $washedWeight")
        Log.e("adriel-testing", "driedWeight: $driedWeight")
        Log.e("adriel-testing", "ironedWeight: $ironedWeight")

        Log.e("adriel-testing", "isFinalPhaseComplete: $isFinalPhaseComplete")

        if (isFinalPhaseComplete) return OrderStatus.FOLDING

        // Still has pending or active batches for current phase
        val hasWashingDrying = allBatches.any { it.status == OrderStatus.WASHING_AND_DRYING }
        val hasWashing = allBatches.any { it.status == OrderStatus.WASHING }
        val hasDrying = allBatches.any { it.status == OrderStatus.DRYING }
        val hasIroning = allBatches.any { it.status == OrderStatus.IRONING }

        return when {
            hasWashingDrying -> OrderStatus.WASHING_AND_DRYING
            hasWashing -> OrderStatus.WASHING
            hasDrying -> OrderStatus.DRYING
            hasIroning -> OrderStatus.IRONING
            // If nothing is active, but weight is not 100%, keep it in the finished state of the last phase
            washedDriedWeight >= (totalOrderWeight - epsilon) && hasWashDryItems -> OrderStatus.WASHED_AND_DRIED
            (lastFinishedStatus == OrderStatus.WASHED || lastFinishedStatus == OrderStatus.DRIED) && washedWeight >= (totalOrderWeight - epsilon) && driedWeight < (totalOrderWeight - epsilon) && (hasDryItems || hasWashDryItems) -> OrderStatus.WASHED
            driedWeight >= (totalOrderWeight - epsilon) && ironedWeight < (totalOrderWeight - epsilon) && (hasIronItems || hasWashDryItems) -> OrderStatus.DRIED
            else -> OrderStatus.INTAKE
        }
    }
}