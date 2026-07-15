package com.aesprt.foldgo.domain.model

import com.aesprt.foldgo.domain.model.enums.MachineStatus

data class Machine(
    val machineId: String,
    val shopId: String,
    val name: String,
    val capacityKg: Double,
    val status: MachineStatus,
    val lastMaintenanceDate: Long,
    val endTime: Long? = null,
    val cyclesCount: Int = 0,
    val assignedOrderId: String? = null
)
