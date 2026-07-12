package com.aesprt.foldgo.domain.usecase

import com.aesprt.foldgo.domain.model.Machine
import com.aesprt.foldgo.domain.model.MachineCategory
import com.aesprt.foldgo.domain.repository.MachineRepository
import kotlinx.coroutines.flow.Flow

class GetMachinesUseCase(private val repository: MachineRepository) {
    operator fun invoke(): Flow<List<Machine>> = repository.getAllMachines()
}

class AddMachineUseCase(private val repository: MachineRepository) {
    suspend operator fun invoke(machine: Machine) = repository.upsertMachine(machine)
}

class GetMachineCategoriesUseCase(private val repository: MachineRepository) {
    operator fun invoke(): Flow<List<MachineCategory>> = repository.getAllCategories()
}

class AddMachineCategoryUseCase(private val repository: MachineRepository) {
    suspend operator fun invoke(category: MachineCategory) = repository.upsertCategory(category)
}

class UpdateMachineStatusUseCase(private val repository: MachineRepository) {
    suspend operator fun invoke(machineId: String, status: String) = repository.updateMachineStatus(machineId, status)
}

class StartMachineCycleUseCase(private val repository: MachineRepository) {
    suspend operator fun invoke(machineId: String, orderId: String, durationMinutes: Int) = repository.startMachineCycle(machineId, orderId, durationMinutes)
}

class FinishMachineCycleUseCase(private val repository: MachineRepository) {
    suspend operator fun invoke(machineId: String) = repository.finishMachineCycle(machineId)
}
