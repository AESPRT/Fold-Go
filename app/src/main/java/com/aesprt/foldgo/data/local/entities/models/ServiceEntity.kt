package com.aesprt.foldgo.data.local.entities.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.aesprt.foldgo.domain.model.enums.ServiceType

@Entity(tableName = "services")
data class ServiceEntity(
    @PrimaryKey val serviceId: String,
    val shopId: String,
    val name: String,
    val defaultQuantity: Double,
    val unit: String,
    val pricePerUnit: Double,
    val type: ServiceType
)
