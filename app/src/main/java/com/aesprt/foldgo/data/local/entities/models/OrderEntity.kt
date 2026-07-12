package com.aesprt.foldgo.data.local.entities.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.aesprt.foldgo.domain.model.enums.DeliveryMethod
import com.aesprt.foldgo.domain.model.enums.OrderStatus
import com.aesprt.foldgo.domain.model.enums.PaymentStatus

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val orderId: String,
    val shopId: String,
    val customerId: String,
    val customerName: String,
    val customerPhone: String,
    val customerAddress: String = "",
    val orderNumber: String,
    val itemsJson: String,
    val totalAmount: Double,
    val paidAmount: Double,
    val changeDue: Double,
    val status: OrderStatus,
    val deliveryMethod: DeliveryMethod,
    val paymentStatus: PaymentStatus,
    val intakePhotosJson: String?,
    val machineId: String?,
    val staffId: String,
    val staffName: String,
    val createdAt: Long,
    val updatedAt: Long,
    val isSynced: Boolean = false
)
