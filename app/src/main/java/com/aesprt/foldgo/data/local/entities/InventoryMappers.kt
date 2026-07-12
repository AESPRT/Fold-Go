package com.aesprt.foldgo.data.local.entities

import com.aesprt.foldgo.data.local.entities.models.InventoryEntity
import com.aesprt.foldgo.domain.model.Inventory
import com.aesprt.foldgo.domain.model.enums.*

fun InventoryEntity.toDomain() = Inventory(
    itemId = itemId,
    shopId = shopId,
    name = name,
    currentStock = currentStock,
    unit = unit,
    lowStockThreshold = lowStockThreshold
)

fun Inventory.toEntity() = InventoryEntity(
    itemId = itemId,
    shopId = shopId,
    name = name,
    currentStock = currentStock,
    unit = unit,
    lowStockThreshold = lowStockThreshold
)
