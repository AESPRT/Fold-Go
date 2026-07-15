package com.aesprt.foldgo.data.local.entities.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.aesprt.foldgo.domain.model.enums.MachineStatus

@Entity(tableName = "machines")
data class MachineEntity(
    @PrimaryKey val machineId: String,
    val shopId: String,
    val name: String,
    val capacityKg: Double,
    val status: MachineStatus,
    val lastMaintenanceDate: Long,
    val endTime: Long? = null,
    val cyclesCount: Int = 0,
    val assignedOrderId: String? = null
)
