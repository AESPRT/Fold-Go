package com.aesprt.foldgo.domain.repository

import com.aesprt.foldgo.domain.model.Machine
import com.aesprt.foldgo.domain.model.MachineCategory
import kotlinx.coroutines.flow.Flow

interface MachineRepository {
    fun getAllMachines(): Flow<List<Machine>>
    suspend fun upsertMachine(machine: Machine)
    suspend fun updateMachineStatus(machineId: String, status: String)
    suspend fun startMachineCycle(machineId: String, orderId: String, durationMinutes: Int)
    suspend fun finishMachineCycle(machineId: String)
    suspend fun assignOrder(machineId: String, orderId: String?)
    
    fun getAllCategories(): Flow<List<MachineCategory>>
    suspend fun upsertCategory(category: MachineCategory)
}
