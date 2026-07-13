package com.aesprt.foldgo.data.repository

import com.aesprt.foldgo.data.local.dao.OrderDao
import com.aesprt.foldgo.domain.model.Order
import com.aesprt.foldgo.data.local.entities.*
import com.aesprt.foldgo.domain.repository.OrderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OrderRepositoryImpl(
    private val orderDao: OrderDao
) : OrderRepository {

    override fun getAllOrders(): Flow<List<Order>> {
        return orderDao.getAllOrders().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getOrderById(orderId: String): Flow<Order?> {
        return orderDao.getOrderById(orderId).map { it?.toDomain() }
    }

    override suspend fun upsertOrder(order: Order) {
        orderDao.upsertOrder(order.toEntity())
    }

    override suspend fun deleteOrder(order: Order) {
        orderDao.deleteOrder(order.toEntity())
    }
}
