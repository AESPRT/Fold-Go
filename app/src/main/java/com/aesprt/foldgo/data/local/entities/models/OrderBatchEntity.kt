package com.aesprt.foldgo.data.local.entities.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.aesprt.foldgo.domain.model.enums.OrderStatus

@Entity(tableName = "order_batches")
data class OrderBatchEntity(
    @PrimaryKey val batchId: String,
    val orderId: String,
    val machineId: String?,
    val weightKg: Double,
    val status: OrderStatus,
    val startTime: Long,
    val endTime: Long? = null
)
