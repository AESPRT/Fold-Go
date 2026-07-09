package com.aesprt.foldgo.data.local.entities

import com.aesprt.foldgo.domain.model.Order
import com.aesprt.foldgo.domain.model.ServiceItem
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun OrderEntity.toDomain(): Order {
    val items = try {
        Json.decodeFromString<List<ServiceItem>>(itemsJson)
    } catch (e: Exception) {
        emptyList()
    }
    val photos = try {
        intakePhotosJson?.let { Json.decodeFromString<List<String>>(it) } ?: emptyList()
    } catch (e: Exception) {
        emptyList()
    }

    return Order(
        orderId = orderId,
        shopId = shopId,
        customerId = customerId,
        orderNumber = orderNumber,
        items = items,
        totalAmount = totalAmount,
        paidAmount = paidAmount,
        status = status,
        intakePhotos = photos,
        machineId = machineId,
        staffId = staffId,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun Order.toEntity(isSynced: Boolean = false): OrderEntity {
    return OrderEntity(
        orderId = orderId,
        shopId = shopId,
        customerId = customerId,
        orderNumber = orderNumber,
        itemsJson = Json.encodeToString(items),
        totalAmount = totalAmount,
        paidAmount = paidAmount,
        status = status,
        intakePhotosJson = Json.encodeToString(intakePhotos),
        machineId = machineId,
        staffId = staffId,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isSynced = isSynced
    )
}
