package com.aesprt.foldgo.data.local.entities

import com.aesprt.foldgo.data.local.entities.models.*
import com.aesprt.foldgo.domain.model.*
import com.aesprt.foldgo.domain.model.enums.*
import com.aesprt.foldgo.domain.model.enums.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun OrderBatchEntity.toDomain() = OrderBatch(
    batchId = batchId,
    orderId = orderId,
    machineId = machineId,
    weightKg = weightKg,
    status = status,
    startTime = startTime,
    endTime = endTime
)

fun OrderBatch.toEntity() = OrderBatchEntity(
    batchId = batchId,
    orderId = orderId,
    machineId = machineId,
    weightKg = weightKg,
    status = status,
    startTime = startTime,
    endTime = endTime
)

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
        mobileNumber = mobileNumber,
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
        mobileNumber = mobileNumber,
        ownerId = ownerId,
        pin = pin,
        settings = Json.encodeToString(settings),
        createdAt = createdAt
    )
}

fun StaffEntity.toDomain(): Staff {
    return Staff(
        staffId = staffId,
        shopId = shopId,
        name = name,
        role = role,
        isActive = isActive,
        createdAt = createdAt
    )
}

fun Staff.toEntity(): StaffEntity {
    return StaffEntity(
        staffId = staffId,
        shopId = shopId,
        name = name,
        role = role,
        isActive = isActive,
        createdAt = createdAt
    )
}

fun MachineCategoryEntity.toDomain(): MachineCategory {
    return MachineCategory(
        categoryId = categoryId,
        name = name,
        type = type,
        iconName = iconName,
        colorHex = colorHex
    )
}

fun MachineCategory.toEntity(): MachineCategoryEntity {
    return MachineCategoryEntity(
        categoryId = categoryId,
        name = name,
        type = type,
        iconName = iconName,
        colorHex = colorHex
    )
}

fun ServiceEntity.toDomain(): Service {
    return Service(
        serviceId = serviceId,
        shopId = shopId,
        name = name,
        defaultQuantity = defaultQuantity,
        unit = unit,
        pricePerUnit = pricePerUnit,
        type = type
    )
}

fun Service.toEntity(): ServiceEntity {
    return ServiceEntity(
        serviceId = serviceId,
        shopId = shopId,
        name = name,
        defaultQuantity = defaultQuantity,
        unit = unit,
        pricePerUnit = pricePerUnit,
        type = type
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
        customerAddress = customerAddress,
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
        staffName = staffName,
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
        customerAddress = customerAddress,
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
        staffName = staffName,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isSynced = isSynced
    )
}
