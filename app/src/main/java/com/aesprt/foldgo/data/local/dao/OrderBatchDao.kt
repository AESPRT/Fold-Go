package com.aesprt.foldgo.data.local.dao

import androidx.room.*
import com.aesprt.foldgo.data.local.entities.models.OrderBatchEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderBatchDao {
    @Query("SELECT * FROM order_batches WHERE orderId = :orderId")
    fun getBatchesByOrderId(orderId: String): Flow<List<OrderBatchEntity>>

    @Query("SELECT * FROM order_batches WHERE machineId = :machineId AND status NOT IN ('READY', 'DELIVERED')")
    fun getActiveBatchByMachineId(machineId: String): Flow<OrderBatchEntity?>

    @Query("SELECT * FROM order_batches WHERE machineId = :machineId AND orderId = :orderId AND status NOT IN ('READY', 'DELIVERED')")
    fun getActiveBatchByMachineOrderId(machineId: String, orderId: String): Flow<OrderBatchEntity?>

    @Query("SELECT * FROM order_batches WHERE batchId = :batchId")
    suspend fun getBatchById(batchId: String): OrderBatchEntity?

    @Upsert
    suspend fun upsertBatch(batch: OrderBatchEntity)

    @Delete
    suspend fun deleteBatch(batch: OrderBatchEntity)
    
    @Query("DELETE FROM order_batches WHERE orderId = :orderId")
    suspend fun deleteBatchesByOrderId(orderId: String)
}
