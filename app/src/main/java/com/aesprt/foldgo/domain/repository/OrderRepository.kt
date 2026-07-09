package com.aesprt.foldgo.domain.repository

import com.aesprt.foldgo.domain.model.Order
import kotlinx.coroutines.flow.Flow

interface OrderRepository {
    fun getAllOrders(): Flow<List<Order>>
    fun getOrderById(orderId: String): Flow<Order?>
    suspend fun upsertOrder(order: Order)
    suspend fun deleteOrder(order: Order)
}
