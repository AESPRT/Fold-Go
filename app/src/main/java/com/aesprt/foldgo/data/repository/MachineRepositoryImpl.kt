package com.aesprt.foldgo.data.repository

import com.aesprt.foldgo.data.local.dao.MachineCategoryDao
import com.aesprt.foldgo.data.local.dao.MachineDao
import com.aesprt.foldgo.domain.model.Machine
import com.aesprt.foldgo.domain.model.MachineCategory
import com.aesprt.foldgo.data.local.entities.*
import com.aesprt.foldgo.domain.repository.MachineRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MachineRepositoryImpl(
    private val machineDao: MachineDao,
    private val categoryDao: MachineCategoryDao
) : MachineRepository {
    override fun getAllMachines(): Flow<List<Machine>> {
        return machineDao.getAllMachines().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun upsertMachine(machine: Machine) {
        machineDao.upsertMachine(machine.toEntity())
    }

    override suspend fun updateMachineStatus(machineId: String, status: String) {
        machineDao.updateMachineStatus(machineId, status)
    }

    override suspend fun startMachineCycle(machineId: String, orderId: String, durationMinutes: Int) {
        // No longer using automated cycles with timers. 
        // Logic moved to manual status updates.
    }

    override suspend fun finishMachineCycle(machineId: String) {
        machineDao.finishCycle(machineId)
    }

    override suspend fun assignOrder(machineId: String, orderId: String?) {
        machineDao.assignOrder(machineId, orderId)
    }

    override fun getAllCategories(): Flow<List<MachineCategory>> {
        return categoryDao.getAllCategories().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun upsertCategory(category: MachineCategory) {
        categoryDao.upsertCategory(category.toEntity())
    }
}
