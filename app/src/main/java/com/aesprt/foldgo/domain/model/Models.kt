package com.aesprt.foldgo.domain.model

import kotlinx.serialization.Serializable

enum class OrderStatus {
    INTAKE, WASHING, WASHED, DRYING, DRIED, FOLDING, READY, DELIVERED
}

enum class DeliveryMethod {
    PICKUP, DELIVERY
}

enum class PaymentStatus {
    PENDING, PAID, PARTIAL
}

@Serializable
data class ServiceItem(
    val name: String,
    val quantity: Double,
    val unit: String,
    val pricePerUnit: Double,
    val totalPrice: Double
)

data class Order(
    val orderId: String,
    val shopId: String,
    val customerId: String,
    val customerName: String,
    val customerPhone: String,
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
    val createdAt: Long,
    val updatedAt: Long
)

data class Machine(
    val machineId: String,
    val shopId: String,
    val name: String,
    val type: String,
    val capacityKg: Double,
    val status: String,
    val lastMaintenanceDate: Long,
    val endTime: Long? = null,
    val cyclesCount: Int = 0
)

data class Inventory(
    val itemId: String,
    val shopId: String,
    val name: String,
    val currentStock: Double,
    val unit: String,
    val lowStockThreshold: Double
)

data class Shop(
    val shopId: String,
    val name: String,
    val address: String,
    val ownerId: String,
    val settings: Map<String, String>,
    val createdAt: Long
)
