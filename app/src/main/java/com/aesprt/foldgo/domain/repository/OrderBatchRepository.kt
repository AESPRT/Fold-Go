package com.aesprt.foldgo.domain.repository

import com.aesprt.foldgo.domain.model.OrderBatch
import kotlinx.coroutines.flow.Flow

interface OrderBatchRepository {
    fun getBatchesByOrderId(orderId: String): Flow<List<OrderBatch>>
    fun getActiveBatchByMachineId(machineId: String): Flow<OrderBatch?>
    suspend fun upsertBatch(batch: OrderBatch)
    suspend fun deleteBatch(batch: OrderBatch)
    suspend fun deleteBatchesByOrderId(orderId: String)
}
