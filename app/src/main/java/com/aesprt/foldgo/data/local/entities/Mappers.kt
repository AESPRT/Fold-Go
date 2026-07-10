package com.aesprt.foldgo.data.local.entities

import com.aesprt.foldgo.domain.model.Order
import com.aesprt.foldgo.domain.model.ServiceItem
import com.aesprt.foldgo.domain.model.Shop
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun ShopEntity.toDomain(): Shop {
    val settingsMap = try {
        Json.decodeFromString<Map<String, String>>(settings)
    } catch (e: Exception) {
        emptyMap()
    }
    return Shop(
        shopId = shopId,
        name = name,
        address = address,
        ownerId = ownerId,
        pin = pin,
        settings = settingsMap,
        createdAt = createdAt
    )
}

fun Shop.toEntity(): ShopEntity {
    return ShopEntity(
        shopId = shopId,
        name = name,
        address = address,
        ownerId = ownerId,
        pin = pin,
        settings = Json.encodeToString(settings),
        createdAt = createdAt
    )
}

fun StaffEntity.toDomain(): com.aesprt.foldgo.domain.model.Staff {
    return com.aesprt.foldgo.domain.model.Staff(
        staffId = staffId,
        shopId = shopId,
        name = name,
        role = role,
        isActive = isActive,
        createdAt = createdAt
    )
}

fun com.aesprt.foldgo.domain.model.Staff.toEntity(): StaffEntity {
    return StaffEntity(
        staffId = staffId,
        shopId = shopId,
        name = name,
        role = role,
        isActive = isActive,
        createdAt = createdAt
    )
}

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
        customerName = customerName,
        customerPhone = customerPhone,
        orderNumber = orderNumber,
        items = items,
        totalAmount = totalAmount,
        paidAmount = paidAmount,
        changeDue = changeDue,
        status = status,
        deliveryMethod = deliveryMethod,
        paymentStatus = paymentStatus,
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
        customerName = customerName,
        customerPhone = customerPhone,
        orderNumber = orderNumber,
        itemsJson = Json.encodeToString(items),
        totalAmount = totalAmount,
        paidAmount = paidAmount,
        changeDue = changeDue,
        status = status,
        deliveryMethod = deliveryMethod,
        paymentStatus = paymentStatus,
        intakePhotosJson = Json.encodeToString(intakePhotos),
        machineId = machineId,
        staffId = staffId,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isSynced = isSynced
    )
}
