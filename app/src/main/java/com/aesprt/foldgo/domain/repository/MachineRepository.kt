package com.aesprt.foldgo.domain.repository

import com.aesprt.foldgo.domain.model.Machine
import kotlinx.coroutines.flow.Flow

interface MachineRepository {
    fun getAllMachines(): Flow<List<Machine>>
    suspend fun upsertMachine(machine: Machine)
    suspend fun updateMachineStatus(machineId: String, status: String)
}
