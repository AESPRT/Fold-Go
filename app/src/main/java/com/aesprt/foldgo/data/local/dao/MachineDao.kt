package com.aesprt.foldgo.data.local.dao

import androidx.room.*
import com.aesprt.foldgo.data.local.entities.models.MachineEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MachineDao {
    @Query("SELECT * FROM machines")
    fun getAllMachines(): Flow<List<MachineEntity>>

    @Upsert
    suspend fun upsertMachine(machine: MachineEntity)

    @Query("UPDATE machines SET status = :status WHERE machineId = :machineId")
    suspend fun updateMachineStatus(machineId: String, status: String)

    @Query("SELECT * FROM machines WHERE machineId = :machineId")
    suspend fun getMachineById(machineId: String): MachineEntity?

    @Query("UPDATE machines SET assignedOrderId = :orderId WHERE machineId = :machineId")
    suspend fun assignOrder(machineId: String, orderId: String?)

    @Query("UPDATE machines SET status = 'BUSY', endTime = :endTime, cyclesCount = cyclesCount + 1, assignedOrderId = :orderId WHERE machineId = :machineId")
    suspend fun startCycle(machineId: String, endTime: Long, orderId: String)

    @Query("UPDATE machines SET status = 'IDLE', endTime = NULL, assignedOrderId = NULL WHERE machineId = :machineId")
    suspend fun finishCycle(machineId: String)
}
