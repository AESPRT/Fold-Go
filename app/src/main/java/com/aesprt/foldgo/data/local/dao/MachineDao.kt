package com.aesprt.foldgo.data.local.dao

import androidx.room.*
import com.aesprt.foldgo.data.local.entities.MachineEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MachineDao {
    @Query("SELECT * FROM machines")
    fun getAllMachines(): Flow<List<MachineEntity>>

    @Upsert
    suspend fun upsertMachine(machine: MachineEntity)

    @Query("UPDATE machines SET status = :status WHERE machineId = :machineId")
    suspend fun updateMachineStatus(machineId: String, status: String)
}
