package com.aesprt.foldgo.domain.model

import kotlinx.serialization.Serializable

enum class OrderStatus {
    INTAKE, WASHING, DRYING, FOLDING, READY, DELIVERED
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
    val orderNumber: String,
    val items: List<ServiceItem>,
    val totalAmount: Double,
    val paidAmount: Double,
    val status: OrderStatus,
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
    val lastMaintenanceDate: Long
)

data class Inventory(
    val itemId: String,
    val shopId: String,
    val name: String,
    val currentStock: Double,
    val unit: String,
    val lowStockThreshold: Double
)
