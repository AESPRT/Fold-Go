package com.aesprt.foldgo.data.repository

import com.aesprt.foldgo.data.local.dao.MachineDao
import com.aesprt.foldgo.data.local.entities.toDomain
import com.aesprt.foldgo.data.local.entities.toEntity
import com.aesprt.foldgo.domain.model.Machine
import com.aesprt.foldgo.domain.repository.MachineRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MachineRepositoryImpl(
    private val machineDao: MachineDao
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

    override suspend fun startMachineCycle(machineId: String, durationMinutes: Int) {
        val endTime = System.currentTimeMillis() + (durationMinutes * 60 * 1000)
        machineDao.startCycle(machineId, endTime)
    }

    override suspend fun finishMachineCycle(machineId: String) {
        machineDao.finishCycle(machineId)
    }
}
