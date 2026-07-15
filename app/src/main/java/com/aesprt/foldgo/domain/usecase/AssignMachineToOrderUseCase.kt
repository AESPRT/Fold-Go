package com.aesprt.foldgo.domain.usecase

import com.aesprt.foldgo.domain.repository.MachineRepository

class AssignMachineToOrderUseCase(private val repository: MachineRepository) {
    suspend operator fun invoke(machineId: String, orderId: String?) {
        repository.assignOrder(machineId, orderId)
    }
}
