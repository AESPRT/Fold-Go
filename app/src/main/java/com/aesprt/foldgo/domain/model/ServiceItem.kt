package com.aesprt.foldgo.domain.model

import com.aesprt.foldgo.domain.model.enums.ServiceType
import kotlinx.serialization.Serializable

@Serializable
data class ServiceItem(
    val name: String,
    val quantity: Double,
    val unit: String,
    val pricePerUnit: Double,
    val totalPrice: Double,
    val type: ServiceType = ServiceType.WASH_DRY
)
