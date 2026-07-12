package com.aesprt.foldgo.domain.model

import com.aesprt.foldgo.domain.model.enums.DeliveryMethod
import com.aesprt.foldgo.domain.model.enums.OrderStatus
import com.aesprt.foldgo.domain.model.enums.PaymentStatus

data class Order(
    val orderId: String,
    val shopId: String,
    val customerId: String,
    val customerName: String,
    val customerPhone: String,
    val customerAddress: String = "",
    val orderNumber: String,
    val items: List<ServiceItem>,
    val totalAmount: Double,
    val paidAmount: Double,
    val changeDue: Double = 0.0,
    val status: OrderStatus,
    val deliveryMethod: DeliveryMethod = DeliveryMethod.PICKUP,
    val paymentStatus: PaymentStatus = PaymentStatus.PENDING,
    val intakePhotos: List<String>,
    val machineId: String?,
    val staffId: String,
    val staffName: String,
    val createdAt: Long,
    val updatedAt: Long
)
