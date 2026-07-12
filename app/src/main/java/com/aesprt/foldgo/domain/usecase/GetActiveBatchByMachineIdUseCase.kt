package com.aesprt.foldgo.domain.usecase

import com.aesprt.foldgo.domain.model.OrderBatch
import com.aesprt.foldgo.domain.repository.OrderBatchRepository
import kotlinx.coroutines.flow.Flow

class GetActiveBatchByMachineIdUseCase(private val repository: OrderBatchRepository) {
    operator fun invoke(machineId: String): Flow<OrderBatch?> = repository.getActiveBatchByMachineId(machineId)
}
