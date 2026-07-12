package com.aesprt.foldgo.data.repository

import com.aesprt.foldgo.data.local.dao.OrderBatchDao
import com.aesprt.foldgo.domain.model.OrderBatch
import com.aesprt.foldgo.data.local.entities.*
import com.aesprt.foldgo.domain.repository.OrderBatchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OrderBatchRepositoryImpl(
    private val orderBatchDao: OrderBatchDao
) : OrderBatchRepository {
    override fun getBatchesByOrderId(orderId: String): Flow<List<OrderBatch>> {
        return orderBatchDao.getBatchesByOrderId(orderId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getActiveBatchByMachineId(machineId: String): Flow<OrderBatch?> {
        return orderBatchDao.getActiveBatchByMachineId(machineId).map { it?.toDomain() }
    }

    override suspend fun upsertBatch(batch: OrderBatch) {
        orderBatchDao.upsertBatch(batch.toEntity())
    }

    override suspend fun deleteBatch(batch: OrderBatch) {
        orderBatchDao.deleteBatch(batch.toEntity())
    }

    override suspend fun deleteBatchesByOrderId(orderId: String) {
        orderBatchDao.deleteBatchesByOrderId(orderId)
    }
}
