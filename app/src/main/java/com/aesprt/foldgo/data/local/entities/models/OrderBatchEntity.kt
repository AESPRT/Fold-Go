package com.aesprt.foldgo.data.local.entities.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.aesprt.foldgo.domain.model.enums.BatchStatus
import com.aesprt.foldgo.domain.model.enums.ServiceType

@Entity(tableName = "order_batches")
data class OrderBatchEntity(
    @PrimaryKey val batchId: String,
    val orderId: String,
    val machineId: String?,
    val weightKg: Double,
    val status: BatchStatus,
    val serviceType: ServiceType = ServiceType.PER_KG,
    val startTime: Long,
    val endTime: Long? = null
)
