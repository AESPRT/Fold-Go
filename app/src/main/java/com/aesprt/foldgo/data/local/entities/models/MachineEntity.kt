package com.aesprt.foldgo.data.local.entities.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.aesprt.foldgo.domain.model.enums.MachineStatus
import com.aesprt.foldgo.domain.model.enums.MachineType

@Entity(tableName = "machines")
data class MachineEntity(
    @PrimaryKey val machineId: String,
    val shopId: String,
    val name: String,
    val type: MachineType,
    val capacityKg: Double,
    val status: MachineStatus,
    val lastMaintenanceDate: Long,
    val endTime: Long? = null,
    val cyclesCount: Int = 0
)
