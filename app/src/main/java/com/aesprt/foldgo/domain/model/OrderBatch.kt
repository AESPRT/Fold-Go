package com.aesprt.foldgo.domain.model

import com.aesprt.foldgo.domain.model.enums.OrderStatus
import com.aesprt.foldgo.domain.model.enums.ServiceType

data class OrderBatch(
    val batchId: String,
    val orderId: String,
    val machineId: String?,
    val weightKg: Double,
    val status: OrderStatus,
    val serviceType: ServiceType = ServiceType.WASH_DRY,
    val startTime: Long,
    val endTime: Long? = null
)
