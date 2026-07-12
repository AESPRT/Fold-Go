package com.aesprt.foldgo.data.local.entities.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventory")
data class InventoryEntity(
    @PrimaryKey val itemId: String,
    val shopId: String,
    val name: String,
    val currentStock: Double,
    val unit: String,
    val lowStockThreshold: Double
)
