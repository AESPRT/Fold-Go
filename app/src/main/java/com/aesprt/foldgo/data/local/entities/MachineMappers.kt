package com.aesprt.foldgo.data.local.entities

import com.aesprt.foldgo.data.local.entities.models.MachineEntity
import com.aesprt.foldgo.domain.model.Machine
import com.aesprt.foldgo.domain.model.enums.*

fun MachineEntity.toDomain() = Machine(
    machineId = machineId,
    shopId = shopId,
    name = name,
    capacityKg = capacityKg,
    status = status,
    lastMaintenanceDate = lastMaintenanceDate,
    endTime = endTime,
    cyclesCount = cyclesCount,
    assignedOrderId = assignedOrderId
)

fun Machine.toEntity() = MachineEntity(
    machineId = machineId,
    shopId = shopId,
    name = name,
    capacityKg = capacityKg,
    status = status,
    lastMaintenanceDate = lastMaintenanceDate,
    endTime = endTime,
    cyclesCount = cyclesCount,
    assignedOrderId = assignedOrderId
)
