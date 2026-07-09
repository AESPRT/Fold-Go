package com.aesprt.foldgo.data.local.entities

import com.aesprt.foldgo.domain.model.Machine

fun MachineEntity.toDomain() = Machine(
    machineId = machineId,
    shopId = shopId,
    name = name,
    type = type,
    capacityKg = capacityKg,
    status = status,
    lastMaintenanceDate = lastMaintenanceDate
)

fun Machine.toEntity() = MachineEntity(
    machineId = machineId,
    shopId = shopId,
    name = name,
    type = type,
    capacityKg = capacityKg,
    status = status,
    lastMaintenanceDate = lastMaintenanceDate
)
