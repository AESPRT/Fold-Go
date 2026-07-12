package com.aesprt.foldgo.domain.model

import com.aesprt.foldgo.domain.model.enums.OrderStatus

data class OrderBatch(
    val batchId: String,
    val orderId: String,
    val machineId: String?,
    val weightKg: Double,
    val status: OrderStatus,
    val startTime: Long,
    val endTime: Long? = null
)
