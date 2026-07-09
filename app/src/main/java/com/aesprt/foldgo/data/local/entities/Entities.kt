package com.aesprt.foldgo.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.aesprt.foldgo.domain.model.Order
import com.aesprt.foldgo.domain.model.OrderStatus

@Entity(tableName = "shops")
data class ShopEntity(
    @PrimaryKey val shopId: String,
    val name: String,
    val address: String,
    val ownerId: String,
    val settings: String,
    val createdAt: Long
)

@Entity(tableName = "machines")
data class MachineEntity(
    @PrimaryKey val machineId: String,
    val shopId: String,
    val name: String,
    val type: String,
    val capacityKg: Double,
    val status: String,
    val lastMaintenanceDate: Long,
    val endTime: Long? = null,
    val cyclesCount: Int = 0
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val orderId: String,
    val shopId: String,
    val customerId: String,
    val orderNumber: String,
    val itemsJson: String,
    val totalAmount: Double,
    val paidAmount: Double,
    val status: OrderStatus,
    val deliveryMethod: com.aesprt.foldgo.domain.model.DeliveryMethod,
    val paymentStatus: com.aesprt.foldgo.domain.model.PaymentStatus,
    val intakePhotosJson: String?,
    val machineId: String?,
    val staffId: String,
    val createdAt: Long,
    val updatedAt: Long,
    val isSynced: Boolean = false
)

@Entity(tableName = "inventory")
data class InventoryEntity(
    @PrimaryKey val itemId: String,
    val shopId: String,
    val name: String,
    val currentStock: Double,
    val unit: String,
    val lowStockThreshold: Double
)

@Entity(tableName = "sync_outbox")
data class SyncOutboxEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entityType: String,
    val entityId: String,
    val operation: String,
    val payloadJson: String,
    val createdAt: Long
)
