package com.aesprt.foldgo.domain.model

import com.aesprt.foldgo.domain.model.enums.MachineStatus
import com.aesprt.foldgo.domain.model.enums.MachineType

data class Machine(
    val machineId: String,
    val shopId: String,
    val name: String,
    val type: MachineType,
    val capacityKg: Double,
    val status: MachineStatus,
    val lastMaintenanceDate: Long,
    val endTime: Long? = null,
    val cyclesCount: Int = 0
)
