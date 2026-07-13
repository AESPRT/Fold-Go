package com.aesprt.foldgo.domain.usecase

import com.aesprt.foldgo.domain.model.OrderBatch
import com.aesprt.foldgo.domain.repository.OrderBatchRepository
import kotlinx.coroutines.flow.Flow

class GetBatchesByOrderIdUseCase(private val repository: OrderBatchRepository) {
    operator fun invoke(orderId: String): Flow<List<OrderBatch>> = repository.getBatchesByOrderId(orderId)
}

class UpsertOrderBatchUseCase(private val repository: OrderBatchRepository) {
    suspend operator fun invoke(batch: OrderBatch) = repository.upsertBatch(batch)
}

class DeleteOrderBatchUseCase(private val repository: OrderBatchRepository) {
    suspend operator fun invoke(batch: OrderBatch) = repository.deleteBatch(batch)
}
