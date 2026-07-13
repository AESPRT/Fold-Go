package com.aesprt.foldgo.domain.model

import com.aesprt.foldgo.domain.model.enums.ServiceType

data class Service(
    val serviceId: String,
    val shopId: String,
    val name: String,
    val defaultQuantity: Double,
    val unit: String,
    val pricePerUnit: Double,
    val type: ServiceType = ServiceType.WASH_DRY
)
