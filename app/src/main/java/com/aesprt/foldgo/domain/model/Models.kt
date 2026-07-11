package com.aesprt.foldgo.domain.model

import kotlinx.serialization.Serializable

enum class OrderStatus {
    INTAKE, WASHING, WASHED, DRYING, DRIED, IRONING, IRONED, FOLDING, READY, DELIVERED
}

enum class DeliveryMethod {
    PICKUP, DELIVERY
}

enum class PaymentStatus {
    PENDING, PAID, PARTIAL
}

enum class ServiceType {
    WASH, DRY, WASH_DRY, IRON, OTHER
}

@Serializable
data class ServiceItem(
    val name: String,
    val quantity: Double,
    val unit: String,
    val pricePerUnit: Double,
    val totalPrice: Double,
    val type: ServiceType = ServiceType.WASH_DRY
)

data class Service(
    val serviceId: String,
    val shopId: String,
    val name: String,
    val defaultQuantity: Double,
    val unit: String,
    val pricePerUnit: Double,
    val type: ServiceType = ServiceType.WASH_DRY
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
    val staffName: String,
    val createdAt: Long,
    val updatedAt: Long
)

enum class MachineType {
    WASHER, DRYER, WASHER_DRYER, IRON, STEAMER
}

data class MachineCategory(
    val categoryId: String,
    val name: String,
    val type: MachineType,
    val iconName: String? = null,
    val colorHex: String? = null
)

enum class MachineStatus {
    IDLE, BUSY, OUT_OF_ORDER
}

data class Machine(
    val machineId: String,
    val shopId: String,
    val name: String,
    val type: MachineType,
    val capacityKg: Double,
    val status: MachineStatus,
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
    val pin: String,
    val settings: Map<String, String>,
    val createdAt: Long
)

data class Staff(
    val staffId: String,
    val shopId: String,
    val name: String,
    val role: String,
    val isActive: Boolean = true,
    val createdAt: Long
)
